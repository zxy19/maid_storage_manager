package studio.fantasyit.maid_storage_manager.craft.work;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.attachment.CraftBlockOccupy;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.data.InvConsumeSimulator;
import studio.fantasyit.maid_storage_manager.debug.DebugData;
import studio.fantasyit.maid_storage_manager.items.RequestListItem;
import studio.fantasyit.maid_storage_manager.maid.ChatTexts;
import studio.fantasyit.maid_storage_manager.maid.data.StorageManagerConfigData;
import studio.fantasyit.maid_storage_manager.maid.memory.RequestProgressMemory;
import studio.fantasyit.maid_storage_manager.maid.memory.ViewedInventoryMemory;
import studio.fantasyit.maid_storage_manager.maid.task.StorageManageTask;
import studio.fantasyit.maid_storage_manager.util.Conditions;
import studio.fantasyit.maid_storage_manager.util.InvUtil;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;

import java.util.*;

public class CraftLayerChain {
    public static final Codec<Pair<UUID, Pair<Integer, UUID>>>
            DISPATCHED_TASK_MAPPING_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("key").forGetter(t -> t.getA().toString()),
                    Codec.INT.fieldOf("Va").forGetter(t -> t.getB().getA()),
                    Codec.STRING.fieldOf("Vb").forGetter(t -> t.getB().getB().toString())
            ).apply(instance, (String a, Integer b, String c) -> new Pair<>(UUID.fromString(a), new Pair<>(b, UUID.fromString(c))))
    );
    public static final Codec<CraftLayerChain> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    CraftLayer.CODEC.listOf().fieldOf("layers").forGetter(t -> t.layers),
                    SolvedCraftLayer.CODEC.listOf().fieldOf("nodes").forGetter(t -> t.nodes),
                    ItemStackUtil.OPTIONAL_CODEC_UNLIMITED.listOf().fieldOf("remainMaterials").forGetter(t -> t.remainMaterials),
                    Codec.INT.fieldOf("freeSlots").forGetter(t -> t.freeSlots),
                    Codec.INT.fieldOf("group").forGetter(t -> t.group),
                    Codec.BOOL.fieldOf("freeze").forGetter(t -> t.freeze),
                    Codec.STRING.fieldOf("isAboutToReschedule").forGetter(t -> t.isStoppingAdding.name()),
                    Codec.BOOL.fieldOf("isMaster").forGetter(t -> t.isMaster),
                    DISPATCHED_TASK_MAPPING_CODEC.listOf().fieldOf("dispatchedTasks").forGetter(t ->
                            t.dispatchedTaskMapping.entrySet().stream().map(p -> new Pair<>(p.getKey(), new Pair<>(p.getValue().getA(), p.getValue().getB()))).toList()
                    ),
                    Codec.INT.fieldOf("maxParallel").forGetter(t -> t.maxParallel),
                    Codec.INT.fieldOf("currentParallel").forGetter(t -> t.currentParallel),
                    InvConsumeSimulator.CODEC.fieldOf("invConsumeSimulator").forGetter(t -> t.invConsumeSimulator)
            ).apply(instance, CraftLayerChain::new)
    );

    /**
     * 已经记录的所有合成层
     */
    protected List<CraftLayer> layers;
    /**
     * 当前合成层
     */
    protected List<SolvedCraftLayer> nodes;

    /**
     * 执行到现在的所有剩余
     */
    public List<ItemStack> remainMaterials;

    /**
     * 模拟背包用量
     */
    private final InvConsumeSimulator invConsumeSimulator;

    public Queue<SolvedCraftLayer> workingQueue;

    /**
     * 当前正在执行的层组ID（直到下一个树根）
     */
    protected int group;
    /**
     * 冻结。已经冻结后不允许修改层信息
     */
    public boolean freeze = false;

    /**
     * 目前消耗的背包格子数
     */
    protected int freeSlots;
    /**
     * 寻路控制记忆
     */
    protected boolean changedTarget;

    /**
     * 是否为主合成发起
     */
    private boolean isMaster;
    /**
     * 分发任务实体和任务ID。用于检查
     * {实体ID: (任务ID,请求工作ID)}
     */
    private Map<UUID, Pair<Integer, UUID>> dispatchedTaskMapping;
    /**
     * 分发任务Tick数。用作超时检测
     */
    private Map<UUID, MutableInt> dispatchedTaskTickCount;
    /**
     * 最大并行
     */
    private int maxParallel;

    /**
     * 当前并行
     */
    private int currentParallel;
    private Component statusMessage = Component.empty();


    protected enum StoppingAdding {
        NONE(false),
        RESCHEDULE(true),
        FAIL(true);
        public final boolean value;

        StoppingAdding(boolean value) {
            this.value = value;
        }
    }

    /**
     * 该选项控制是否允许加入新的任务，同时确保当前已经开始工作的层可以正常完成
     */
    protected StoppingAdding isStoppingAdding;


    protected CraftLayerChain(
            List<CraftLayer> layers,
            List<SolvedCraftLayer> nodes,
            List<ItemStack> remainMaterials,
            int freeSlots,
            int groupId,
            boolean freeze,
            String stoppingAdding,
            boolean isMaster,
            List<Pair<UUID, Pair<Integer, UUID>>> dispatchedTasks,
            int maxParallel,
            int currentParallel,
            InvConsumeSimulator invConsumeSimulator
    ) {
        this.layers = new ArrayList<>(layers);
        this.nodes = new ArrayList<>(nodes);
        this.remainMaterials = new ArrayList<>(remainMaterials);
        this.group = groupId;
        this.freeze = freeze;
        this.freeSlots = freeSlots;
        this.workingQueue = new LinkedList<>();
        this.isMaster = isMaster;
        changedTarget = true;
        this.isStoppingAdding = StoppingAdding.valueOf(stoppingAdding);
        dispatchedTaskMapping = new HashMap<>();
        for (Pair<UUID, Pair<Integer, UUID>> pair : dispatchedTasks)
            dispatchedTaskMapping.put(pair.getA(), pair.getB());
        dispatchedTaskTickCount = new HashMap<>();
        this.maxParallel = maxParallel;
        this.currentParallel = currentParallel;
        this.invConsumeSimulator = invConsumeSimulator;
        if (freeze) {
            addAllLayerToQueue();
        }
        if (isMaster && Config.enableDebug)
            invConsumeSimulator.enableLog = true;
    }

    public CraftLayerChain(EntityMaid maid) {
        this(
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                InvUtil.freeSlots(maid.getAvailableInv(true)),
                0,
                false,
                StoppingAdding.NONE.name(),
                true,
                List.of(),
                maid.getVehicle() != null ? 0 : StorageManagerConfigData.get(maid).maxParallel(),
                0,
                new InvConsumeSimulator()
        );
    }

    public boolean isMaster() {
        return this.isMaster;
    }

    public UUID getMasterUUID() {
        if (dispatchedTaskMapping.isEmpty()) return null;
        return this.dispatchedTaskMapping.keySet().iterator().next();
    }

    public void setMaster(UUID uuid, UUID workUUID) {
        this.isMaster = false;
        this.dispatchedTaskMapping.put(uuid, new Pair<>(0, workUUID));
        invConsumeSimulator.enableLog = false;
    }

    public void setChanged() {
        changedTarget = true;
    }

    public void ifChanged(Runnable runnable) {
        if (changedTarget) {
            changedTarget = false;
            runnable.run();
        }
    }

    private void addAllLayerToQueue() {
        if (!isStoppingAdding.value) {
            for (SolvedCraftLayer node : nodes) {
                if (node.group() != group)
                    continue;
                if (node.progress().getValue() == SolvedCraftLayer.Progress.GATHERING || node.progress().getValue() == SolvedCraftLayer.Progress.WORKING || node.progress().getValue() == SolvedCraftLayer.Progress.IDLE)
                    this.workingQueue.add(node);
                else if (node.progress().getValue() == SolvedCraftLayer.Progress.WAITING) {
                    if (node.inDegree().getValue() == 0) {
                        this.workingQueue.add(node);
                        node.progress().setValue(SolvedCraftLayer.Progress.IDLE);
                    }
                }
            }
        }
        startAny();
    }


    /// ////construct

    public void addLayer(CraftLayer layer) {
        if (freeze)
            throw new RuntimeException("CraftLayerChain is freeze");
        layers.add(layer);
    }

    public void build() {
        List<Pair<Integer, ItemStack>> currentItems = new ArrayList<>();
        MutableInt groupId = new MutableInt(0);
        for (int i = 0; i < layers.size(); i++) {
            CraftLayer layer = layers.get(i);
            ///region 简单估算层的格子消耗量。
            MutableInt inputConsume = new MutableInt(0);
            MutableInt outputConsume = new MutableInt(0);
            for (ItemStack item : layer.getItems())
                inputConsume.add(Math.ceil((double) item.getCount() / item.getMaxStackSize()));
            layer.getCraftData().ifPresent(craftData -> {
                craftData.getAllOutputItemsWithOptional().forEach(item -> {
                    outputConsume.add(Math.ceil((double) item.getCount() * layer.getCount() / item.getMaxStackSize()));
                });
            });
            //因为是估算，可能会得到比背包总格子数更大的数字(逆天)，这里直接将其限制为最大格子数，即要求其必须独立运行
            if (inputConsume.getValue() + outputConsume.getValue() > freeSlots) {
                int diff = inputConsume.getValue() + outputConsume.getValue() - freeSlots;
                if (diff > inputConsume.getValue())
                    outputConsume.subtract(diff - inputConsume.getValue());
                inputConsume.subtract(diff);
                if (inputConsume.getValue() <= 0)
                    inputConsume.setValue(0);
            }
            ///  endregion

            if (layer.shouldPlaceBefore()) {
                currentItems.clear();
                groupId.add(1);
            }

            //构建节点列表
            nodes.add(new SolvedCraftLayer(
                    i,
                    groupId.getValue(),
                    inputConsume.getValue(),
                    outputConsume.getValue(),
                    new ArrayList<>(),
                    new MutableInt(0),
                    new MutableInt(0),
                    new MutableObject<>(SolvedCraftLayer.Progress.WAITING)
            ));

            for (ItemStack item : layer.getItems()) {
                int count = item.getCount();

                //如果当前步骤的输入出现在某个步骤的输出时，说明当前步骤依赖于前一步骤
                for (Pair<Integer, ItemStack> pair : currentItems) {
                    if (pair.getB().getCount() <= 0)
                        continue;
                    if (ItemStackUtil.isSameInCrafting(pair.getB(), item)) {
                        //从当前步骤输入和目标输出中移除相同数量的物品
                        int consumeCound = Math.min(count, pair.getB().getCount());
                        pair.getB().shrink(consumeCound);
                        count -= consumeCound;
                        //将当前节点加入前一节点的后续节点
                        nodes.get(pair.getA()).nextIndex().add(i);
                        nodes.get(i).inDegree().add(1);
                        if (count <= 0) {
                            break;
                        }
                    }
                }
            }

            int finalI = i;
            if (layer.getCraftData().isPresent()) {
                //把当前层的输出存放到零时栈
                CraftGuideData craftGuide = layer.getCraftData().get();
                List<ItemStack> outputs = craftGuide.getOutput();
                for (ItemStack itemStack : outputs) {
                    currentItems.add(
                            new Pair<>(
                                    finalI,
                                    itemStack.copyWithCount(itemStack.getCount() * layer.getCount())
                            )
                    );
                }
            } else {
                currentItems.clear();
                groupId.add(1);
            }
        }
        group = 0;
        freeze = true;
        addAllLayerToQueue();
    }


    public boolean isDone() {
        return workingQueue.isEmpty() && (!isMaster || dispatchedTaskMapping.isEmpty());
    }


    //region LAYER
    public boolean hasCurrent() {
        return !workingQueue.isEmpty();
    }

    public CraftLayer getCurrentLayer() {
        return layers.get(getCurrentNode().index());
    }

    public SolvedCraftLayer getCurrentNode() {
        return workingQueue.peek();
    }

    public List<CraftLayer> getLayers() {
        return layers;
    }

    public CraftLayer getLayer(int i) {
        return layers.get(i);
    }

    public SolvedCraftLayer getNode(int i) {
        return nodes.get(i);
    }

    public int getLayerCount() {
        return layers.size();
    }

    //endregion


    // region 分发层相关的方法
    public @Nullable Pair<CraftLayer, SolvedCraftLayer> getAndDispatchLayer(EntityMaid toMaid) {
        if (workingQueue.size() == 1 && maxParallel != 0) return null;
        int targetFreeSlot = InvUtil.freeSlots(toMaid.getAvailableInv(false));
        int currentIndex = (hasCurrent() && maxParallel != 0) ? getCurrentNode().index() : -1;
        int resultIndex = -1;
        for (int i = 0; i < layers.size(); i++) {
            SolvedCraftLayer node = nodes.get(i);
            if (node.group() != group)
                continue;
            if (node.index() == currentIndex)
                continue;
            if (node.slotConsume() > targetFreeSlot)
                continue;

            CraftLayer layer = layers.get(i);
            invConsumeSimulator.snapshot();
            invConsumeSimulator.addLayerOutput(layer);
            int totalConsume = invConsumeSimulator.getCurrentSlotConsume();
            invConsumeSimulator.restoreSnapshot();
            if (totalConsume > freeSlots) {
                continue;
            }
            if (layer.getCraftData().isEmpty())
                continue;

            if (node.progress().getValue() == SolvedCraftLayer.Progress.IDLE) {
                if (layer.steps.stream().anyMatch(t -> !toMaid.isWithinRestriction(t.storage.pos)))
                    continue;
                if (resultIndex == -1 || nodes.get(resultIndex).lastTouch().getValue() < node.lastTouch().getValue())
                    resultIndex = i;
            }
        }

        if (resultIndex == -1)
            return null;
        return new Pair<>(layers.get(resultIndex), nodes.get(resultIndex));
    }

    public void doDispatchLayer(SolvedCraftLayer node, UUID maidUUID, UUID uuid) {
        invConsumeSimulator.addLayerOutput(layers.get(node.index()));
        node.progress().setValue(SolvedCraftLayer.Progress.DISPATCHED);
        dispatchedTaskMapping.put(maidUUID, new Pair<>(node.index(), uuid));
    }

    public List<ItemStack> getDispatchedRemainItem(CraftLayer outerLayer) {
        List<ItemStack> toTakes = new ArrayList<>();
        for (int i = 0; i < this.remainMaterials.size(); i++) {
            ItemStack toTake = outerLayer.memorizeItem(remainMaterials.get(i), remainMaterials.get(i).getCount());
            if (toTake.isEmpty())
                continue;
            toTakes.add(toTake);
            remainMaterials.get(i).shrink(toTake.getCount());
            if (remainMaterials.get(i).isEmpty()) {
                remainMaterials.remove(i);
                i--;
            }
        }
        return toTakes;
    }

    public void removeDispatchedItems(List<ItemStack> list) {
        list.forEach(itemStack -> invConsumeSimulator.removeConsumeCount(itemStack, itemStack.getCount()));
    }

    /**
     * 检查是否有等待确认分发的女仆到达身边。有，则准备释放当前任务。
     *
     * @param maid
     * @return
     */
    public boolean hasDispatchedWaitingCheck(EntityMaid maid) {
        List<EntityMaid> entities = maid.level()
                .getEntities(
                        EntityTypeTest.forClass(EntityMaid.class),
                        maid.getBoundingBox().inflate(3),
                        toMaid -> {
                            if (!dispatchedTaskMapping.containsKey(toMaid.getUUID()))
                                return false;
                            return MemoryUtil.getCrafting(toMaid).isGatheringDispatched();
                        }
                );
        return !entities.isEmpty();
    }

    /**
     * 分发的节点完成的回调。
     *
     * @param targetMaid
     * @param index
     * @param allSuccess
     * @return
     */
    public boolean dispatchedDone(EntityMaid targetMaid, EntityMaid maid, int index, boolean allSuccess) {
        SolvedCraftLayer node = nodes.get(index);
        CraftLayer layer = layers.get(index);
        dispatchedTaskMapping.remove(targetMaid.getUUID());
        dispatchedTaskTickCount.remove(targetMaid.getUUID());
        if (node.progress().getValue() != SolvedCraftLayer.Progress.DISPATCHED) return false;
        if (allSuccess) {
            finishLayer(node, layer);
            checkAndSwitchGroup(maid);
            checkIsFullInv(maid);
        } else {
            clearAndStopAdding(StoppingAdding.RESCHEDULE);
            handleStopAddingEvent(maid);
        }
        targetMaid.getSchedulePos().restrictTo(targetMaid);
        return true;
    }

    /**
     * 检查所有的dispatched节点。
     *
     * @param maid
     * @return
     */
    public void checkDispatchedValidation(EntityMaid maid) {
        if (!(maid.level() instanceof ServerLevel level)) return;
        HashSet<Map.Entry<UUID, Pair<Integer, UUID>>> entries = new HashSet<>(dispatchedTaskMapping.entrySet());
        for (Map.Entry<UUID, Pair<Integer, UUID>> p : entries) {
            if (!dispatchedTaskTickCount.containsKey(p.getKey())) {
                dispatchedTaskTickCount.put(p.getKey(), new MutableInt(0));
            }
            boolean valid = true;
            // 被分发的女仆
            EntityMaid dispatchedMaid = null;
            if (level.getEntity(p.getKey()) instanceof EntityMaid _dispatchedMaid) {
                dispatchedMaid = _dispatchedMaid;
                if (!dispatchedMaid.getTask().getUid().equals(StorageManageTask.TASK_ID))
                    valid = false;
                else {
                    //获取已被分发的女仆的当前工作状态
                    RequestProgressMemory requestProgress = MemoryUtil.getRequestProgress(dispatchedMaid);

                    //如果工作ID和记录的不同，或者不在工作状态，那么认为分发任务失败。
                    if (!Conditions.takingRequestList(dispatchedMaid))
                        valid = false;
                    else if (!requestProgress.getWorkUUID().equals(p.getValue().getB()))
                        valid = false;
                }
            } else valid = false;
            if (!valid) {
                if (dispatchedTaskTickCount.get(p.getKey()).incrementAndGet() < 20)
                    continue;
                CraftLayer craftLayer = layers.get(p.getValue().getA());
                SolvedCraftLayer node = nodes.get(p.getValue().getA());
                onDispatchedInvalid(maid, dispatchedMaid, node, craftLayer);
                dispatchedTaskMapping.remove(p.getKey());
                dispatchedTaskTickCount.remove(p.getKey());
            } else {
                dispatchedTaskTickCount.remove(p.getKey());
            }
        }
    }

    protected void onDispatchedInvalid(EntityMaid currentMaid, @Nullable EntityMaid dispatchedMaid, SolvedCraftLayer node, CraftLayer craftLayer) {
        //当前是主合成任务，那么dispatched就是子任务承接女仆
        if (isMaster) {
            node.progress().setValue(SolvedCraftLayer.Progress.IDLE);
            //当前任务重新加入队列
            workingQueue.add(node);
            invConsumeSimulator.removeLayerOutput(craftLayer);
        }
        //当前是子合成任务。则当前任务直接判定失败，全部结束即可
        else {
            clearAndStopAdding(StoppingAdding.FAIL);
        }
    }

    public UUID getLayerTaker(int layerIndex) {
        return dispatchedTaskMapping.entrySet()
                .stream().filter(entry -> entry.getValue().getA() == layerIndex)
                .findFirst().map(Map.Entry::getKey).orElse(null);
    }
    // endregion


    // region 当前工作状态
    public boolean isCurrentGathering() {
        if (isDone() || !hasCurrent()) return false;
        return getCurrentNode().progress().getValue() == SolvedCraftLayer.Progress.GATHERING;
    }

    public boolean isCurrentWorking() {
        if (isDone() || !hasCurrent()) return false;
        return getCurrentNode().progress().getValue() == SolvedCraftLayer.Progress.WORKING;
    }
    // endregion

    /// ////infos
    /**
     * 显示合成进度气泡
     *
     * @param maid
     */
    public void showCraftingProgress(EntityMaid maid) {
        if (Config.noBubbleForSub && !isMaster) return;
        int done = 0;
        int total = 0;
        for (SolvedCraftLayer node : nodes) {
            CraftLayer layer = layers.get(node.index());

            if (node.progress().getValue() == SolvedCraftLayer.Progress.FINISHED) {
                done += layer.getCount() * layer.getTotalStep() + 1;
            } else if (node.progress().getValue() == SolvedCraftLayer.Progress.WORKING || node.progress().getValue() == SolvedCraftLayer.Progress.DISPATCHED) {
                done += layer.getDoneCount() * layer.getTotalStep() + layer.getStep() + 1;
            }
            total += layer.getCount() * layer.getTotalStep() + 1;
        }
        MutableComponent toShow = Component.translatable(
                ChatTexts.CHAT_SECONDARY_CRAFTING,
                done,
                total,
                isCurrentWorking() ? Component.translatable(ChatTexts.CHAT_SECONDARY_CRAFTING_WORK) :
                        Component.translatable(ChatTexts.CHAT_SECONDARY_CRAFTING_GATHER)
        );
        toShow.append("\n");
        if (!dispatchedTaskMapping.isEmpty()) {
            if (isMaster) {
                toShow.append(Component.translatable(ChatTexts.CHAT_SECONDARY_CRAFTING_STATUS_MAIN, currentParallel, dispatchedTaskMapping.size()).withStyle(ChatFormatting.GRAY));
            } else {
                toShow.append(Component.translatable(ChatTexts.CHAT_SECONDARY_CRAFTING_STATUS_SUB).withStyle(ChatFormatting.GRAY));
            }
        } else {
            toShow.append(Component.translatable(ChatTexts.CHAT_SECONDARY_CRAFTING_STATUS_NO_DISPATCHING, currentParallel).withStyle(ChatFormatting.GRAY));
        }
        toShow
                .append("\n")
                .append(statusMessage);

        double progress1 = 0;
        if (hasCurrent()) {
            CraftLayer currentLayer = getCurrentLayer();
            SolvedCraftLayer node = getCurrentNode();
            if (node.progress().getValue() == SolvedCraftLayer.Progress.WORKING) {
                progress1 = (double) (currentLayer.getDoneCount() * currentLayer.getTotalStep() + currentLayer.getStep() + 1) / (currentLayer.getCount() * currentLayer.getTotalStep() + 1);
            } else if (node.progress().getValue() == SolvedCraftLayer.Progress.GATHERING) {
                progress1 = 1.0 / (currentLayer.getCount() * currentLayer.getTotalStep() + 1);
            } else if (node.progress().getValue() == SolvedCraftLayer.Progress.FINISHED) {
                progress1 = 1;
            }
        }

        ChatTexts.showSecondaryCrafting(maid, toShow, ((double) done / total), progress1, isStoppingAdding != StoppingAdding.NONE);
    }

    public void setStatusMessage(Component message) {
        statusMessage = message;
    }

    public void setStatusMessage(EntityMaid maid, Component message) {
        setStatusMessage(message);
        showCraftingProgress(maid);
    }

    // region 流程控制

    public int getMaxParallel() {
        return maxParallel;
    }

    public int getCurrentGroup() {
        return group;
    }

    /**
     * 清理，并标记为停止添加新的任务。一般表示当前工作失败，仅用于执行清理工作
     *
     * @param stoppingAdding
     */
    protected void clearAndStopAdding(StoppingAdding stoppingAdding) {
        int count = workingQueue.size();
        for (int i = 0; i < count; i++) {
            SolvedCraftLayer node = workingQueue.poll();
            if (node.progress().getValue() == SolvedCraftLayer.Progress.WORKING)
                workingQueue.add(node);
        }
        isStoppingAdding = stoppingAdding;
    }

    public boolean getIsStoppingAdding() {
        return isStoppingAdding != StoppingAdding.NONE;
    }

    /**
     * 停止添加的检查事件。用于在结束后清空CraftMemory
     *
     * @param maid
     */
    public void handleStopAddingEvent(EntityMaid maid) {
        if (isStoppingAdding == StoppingAdding.NONE)
            return;
        if (hasCurrent()) return;
        if (!dispatchedTaskMapping.isEmpty() && isMaster) return;
        if (isStoppingAdding == StoppingAdding.RESCHEDULE) {
            MemoryUtil.getCrafting(maid).stopAndClearPlan(maid);
        } else if (isStoppingAdding == StoppingAdding.FAIL) {
            handleFailStop(maid);
        }
        isStoppingAdding = StoppingAdding.NONE;
    }

    /**
     * 当前组失败
     *
     * @param maid    女仆
     * @param missing 失败的物品
     */
    public void failCurrent(EntityMaid maid, List<ItemStack> missing) {
        failCurrent(maid, missing, null);
    }

    /**
     * 当前组失败
     *
     * @param maid       女仆
     * @param missing    缺少的物品
     * @param additional 附加信息
     */
    public void failCurrent(EntityMaid maid, List<ItemStack> missing, String additional) {
        CraftLayer layer = getCurrentLayer();
        SolvedCraftLayer node = getCurrentNode();
        removeOccupied((ServerLevel) maid.level(), maid);
        failLayer(maid, missing, additional, layer, node);
    }

    public void failLayer(EntityMaid maid, List<ItemStack> missing, String additional, CraftLayer layer, SolvedCraftLayer node) {
        setStatusMessage(
                maid,
                Component.translatable(
                        ChatTexts.CHAT_CRAFTING_FAIL,
                        layer
                                .getCraftData()
                                .map(CraftGuideData::getOutput)
                                .map(l -> l.get(0).getHoverName())
                                .orElse(Component.empty())
                ).withStyle(ChatFormatting.RED)
        );
        node.progress().setValue(SolvedCraftLayer.Progress.FAILED);
        // 设置缺少物品信息
        for (int i = 0; i < layers.size(); i++) {
            CraftLayer craftLayer = layers.get(i);
            if (craftLayer.getCraftData().isEmpty()) {
                List<ItemStack> targets = craftLayer.getItems();
                // 为其他物品设置缺少信息
                for (ItemStack target : targets) {
                    RequestListItem.setMissingItem(
                            maid.getMainHandItem(),
                            target,
                            missing
                    );
                    RequestListItem.markDone(maid.getMainHandItem(), target);
                    if (additional != null) {
                        RequestListItem.setFailAddition(maid.getMainHandItem(), target, additional);
                    }
                }
                break;
            }
        }
//停止继续处理当前层
        clearAndStopAdding(StoppingAdding.FAIL);
        handleStopAddingEvent(maid);
    }


    protected void handleFailStop(EntityMaid maid) {
        List<ItemStack> targets = null;
        //符合当前合成组的全部标记为失败
        for (int i = 0; i < layers.size(); i++) {
            SolvedCraftLayer node = nodes.get(i);
            CraftLayer craftLayer = layers.get(i);
            if (node.group() != group)
                continue;
            node.progress().setValue(SolvedCraftLayer.Progress.FAILED);
            if (craftLayer.getCraftData().isEmpty()) {
                targets = craftLayer.getItems();
            }
        }

        //对于已有树根的合成（即处理当前合成目标以及标记失败物品）
        if (targets != null) {
            //检测是否存在目标物品，如果是，那么优先进行存放，标记已收集
            CombinedInvWrapper inv = maid.getAvailableInv(true);
            for (int i = 0; i < inv.getSlots(); i++) {
                ItemStack stack = inv.getStackInSlot(i);
                RequestListItem.updateCollectedItem(maid.getMainHandItem(), stack, stack.getCount());
            }
            MemoryUtil.getRequestProgress(maid).setReturn();
        }

        checkAndSwitchGroup(maid);

        setStatusMessage(maid, Component.translatable(ChatTexts.CHAT_CRAFT_FAIL_WAITING).withStyle(ChatFormatting.RED));

        showCraftingProgress(maid);
    }

    /**
     * 完成收集。该步骤会采取下面操作之一：
     * <li>所有物品收集成功，开始合成</li>
     * <li>收集成功但是检测到背包内物品不齐全，继续收集（此时会更改需要的物品的量）</>
     * <li>收集不成功，如果不是最后一步，清空不存在的物品的库存，然后重新计算合成（此时会使用CraftMemory和InventoryMemory</li>
     * <li>失败</li>
     *
     * @param maid
     */
    public void finishGathering(EntityMaid maid) {
        if (!hasCurrent()) return;
        CraftLayer layer = Objects.requireNonNull(this.getCurrentLayer());
        SolvedCraftLayer node = Objects.requireNonNull(this.getCurrentNode());
        if (layer.hasCollectedAll()) {
            //收集完了再次检查满足开始条件。如果否则开始前再次进行补齐
            if (!checkInputInbackpack(maid)) return;
            showCraftingProgress(maid);
            setStatusMessage(maid,
                    Component.translatable(
                            ChatTexts.CHAT_CRAFT_WORK,
                            layer.getCraftData().map(t -> t
                                            .getOutput()
                                            .get(0)
                                            .getHoverName())
                                    .orElse(Component.empty())
                    )
            );
            node.progress().setValue(SolvedCraftLayer.Progress.WORKING);
        } else {
            //如果不是最终步骤，而且没有收集成功，那么意味着记忆存在问题，重置记忆，并再次计算合成树
            if (getCurrentLayer().getCraftData().isPresent()) {
                List<ItemStack> unCollectedItems = layer.getUnCollectedItems();
                ViewedInventoryMemory viewedInventoryMemory = MemoryUtil.getViewedInventory(maid);
                unCollectedItems.forEach(itemStack -> viewedInventoryMemory.removeItemFromAllTargets(itemStack, i -> ItemStackUtil.isSameTagInCrafting(i, itemStack)));
                //子任务，需要将分发者的记忆同步修改
                if (!isMaster) {
                    if (maid.level() instanceof ServerLevel level && level.getEntity(getMasterUUID()) instanceof EntityMaid toMaid) {
                        ViewedInventoryMemory toVi = MemoryUtil.getViewedInventory(toMaid);
                        unCollectedItems.forEach(itemStack -> toVi.removeItemFromAllTargets(itemStack, i -> ItemStackUtil.isSameTagInCrafting(i, itemStack)));
                    }
                }
                clearAndStopAdding(StoppingAdding.RESCHEDULE);
                handleStopAddingEvent(maid);
            } else {
                failCurrent(maid, getCurrentLayer().getUnCollectedItems());
            }
        }
    }

    public boolean checkInputInbackpack(EntityMaid maid) {
        if (!hasCurrent()) return true;
        CraftLayer layer = Objects.requireNonNull(getCurrentLayer());
        if (layer.getStep() != 0) return true;
        SolvedCraftLayer node = Objects.requireNonNull(getCurrentNode());
        CraftGuideData craftData = layer.getCraftData().orElse(null);
        if (craftData == null) return true;
        List<ItemStack> inputs = new ArrayList<>();
        for (ItemStack itemStack : craftData.getInput()) {
            if (itemStack.isEmpty()) continue;
            ItemStackUtil.addToList(inputs, itemStack, false);
        }
        CombinedInvWrapper inv = maid.getAvailableInv(true);
        for (ItemStack itemStack : inputs) {
            for (int i = 0; i < inv.getSlots(); i++) {
                ItemStack item = inv.getStackInSlot(i);
                if (ItemStackUtil.isSameInCrafting(item, itemStack)) {
                    itemStack.shrink(Math.min(itemStack.getCount(), item.getCount()));
                    if (itemStack.isEmpty()) break;
                }
            }
        }
        if (!inputs.stream().allMatch(ItemStack::isEmpty)) {
            for (ItemStack itemStack : inputs) {
                if (!itemStack.isEmpty()) {
                    for (int i = 0; i < layer.getItems().size(); i++) {
                        if (ItemStackUtil.isSameInCrafting(layer.getItems().get(i), itemStack)) {
                            layer.getItems().get(i).grow(itemStack.getCount());
                            break;
                        }
                    }
                }
            }
            node.progress().setValue(SolvedCraftLayer.Progress.GATHERING);
            return false;
        }
        return true;
    }

    public boolean checkStepInputInbackpack(EntityMaid maid) {
        if (!hasCurrent()) return true;
        CraftLayer layer = Objects.requireNonNull(getCurrentLayer());
        SolvedCraftLayer node = Objects.requireNonNull(this.getCurrentNode());
        CraftGuideStepData craftData = layer.getStepData();
        if (craftData == null) return true;
        List<ItemStack> inputs = new ArrayList<>();
        for (ItemStack itemStack : craftData.getInput()) {
            if (itemStack.isEmpty()) continue;
            ItemStackUtil.addToList(inputs, itemStack, false);
        }
        CombinedInvWrapper inv = maid.getAvailableInv(true);
        for (ItemStack itemStack : inputs) {
            for (int i = 0; i < inv.getSlots(); i++) {
                ItemStack item = inv.getStackInSlot(i);
                if (ItemStackUtil.isSameInCrafting(item, itemStack)) {
                    itemStack.shrink(Math.min(itemStack.getCount(), item.getCount()));
                    if (itemStack.isEmpty()) break;
                }
            }
        }
        if (!inputs.stream().allMatch(ItemStack::isEmpty)) {
            for (ItemStack itemStack : inputs) {
                if (!itemStack.isEmpty()) {
                    for (int i = 0; i < layer.getItems().size(); i++) {
                        if (ItemStackUtil.isSameInCrafting(layer.getItems().get(i), itemStack)) {
                            layer.getItems().get(i).grow(itemStack.getCount());
                            break;
                        }
                    }
                }
            }
            node.progress().setValue(SolvedCraftLayer.Progress.GATHERING);
            return false;
        }
        return true;
    }


    /**
     * 停止当前层。将工作状态设置为Finished
     */
    public void finishCurrentLayer(EntityMaid maid) {
        SolvedCraftLayer node = getCurrentNode();
        CraftLayer layer = getCurrentLayer();
        workingQueue.poll();
        finishLayer(node, layer);
        checkAndSwitchGroup(maid);
        checkIsFullInv(maid);
    }


    private void finishLayer(SolvedCraftLayer node, CraftLayer layer) {
        if (node.progress().getValue() != SolvedCraftLayer.Progress.DISPATCHED) {
            //如果非分发层完成，那么我们的必要输入此时应该已经全部完成了，移除这些层。
            invConsumeSimulator.removeLayerInput(layer);
            currentParallel--;
        }
        //如果是分发层，那么这个步骤会在另一处进行

        node.progress().setValue(SolvedCraftLayer.Progress.FINISHED);

        layer.getCraftData().ifPresent(data -> {
            data.getOutput().forEach(itemStack -> {
                if (itemStack.isEmpty()) return;
                this.remainMaterials.add(itemStack.copyWithCount(itemStack.getCount() * layer.getCount()));
            });
        });

        node.nextIndex().forEach(index -> {
            SolvedCraftLayer nextNode = nodes.get(index);
            nextNode.inDegree().decrement();
            nextNode.lastTouch().setValue(Math.max(node.lastTouch().getValue() + 1, nextNode.lastTouch().getValue()));

            if (nextNode.inDegree().getValue() == 0 && !isStoppingAdding.value) {
                workingQueue.add(nextNode);
                nextNode.progress().setValue(SolvedCraftLayer.Progress.IDLE);
            }
        });
        startAny();
    }

    /**
     * 尝试开始一个节点。
     * <li>如果当前节点已经开始/失败或结束，则返回成功/失败</li>
     * <li>如果当前节点已经被分发，则失败</li>
     * <li>如果当前节点有未完成的依赖，则返回失败</li>
     * <li>如果当前节点需要的背包格子数不足，则失败</li>
     * <li>否则成功，并消耗剩余物品。被开启任务进入收集状态</li>
     *
     * @param node
     * @param layer
     * @return
     */
    protected boolean tryStartLayer(SolvedCraftLayer node, CraftLayer layer) {
        if (node.progress().getValue() == SolvedCraftLayer.Progress.FINISHED ||
                node.progress().getValue() == SolvedCraftLayer.Progress.FAILED ||
                node.progress().getValue() == SolvedCraftLayer.Progress.DISPATCHED)
            return false;
        if (node.progress().getValue() == SolvedCraftLayer.Progress.WORKING)
            return true;
        if (node.progress().getValue() == SolvedCraftLayer.Progress.GATHERING && !isStoppingAdding.value)
            return true;
        if (isStoppingAdding.value)
            return false;
        if (node.inDegree().getValue() > 0)
            return false;
        if (currentParallel >= maxParallel)
            //最终层，允许启动
            if (maxParallel != 0 || layer.getCraftData().isPresent())
                return false;

        //测试最大使用格子数
        invConsumeSimulator.snapshot();
        invConsumeSimulator.removeLayerInput(layer);
        invConsumeSimulator.addLayer(layer);
        int totalSlots = invConsumeSimulator.getCurrentSlotConsume();
        invConsumeSimulator.restoreSnapshot();

        if (totalSlots > freeSlots) {
            return false;
        }
        invConsumeSimulator.removeLayerInput(layer);
        invConsumeSimulator.addLayer(layer);
        currentParallel++;
        node.progress().setValue(SolvedCraftLayer.Progress.GATHERING);
        for (int i = 0; i < this.remainMaterials.size(); i++) {
            ItemStack toTake = layer.memorizeItem(remainMaterials.get(i), remainMaterials.get(i).getCount());
            remainMaterials.get(i).shrink(toTake.getCount());
            if (remainMaterials.get(i).isEmpty()) {
                remainMaterials.remove(i);
                i--;
            }
        }
        //来到树根了，清除剩余的材料（因为女仆会尝试放置到附近）
        if (layer.getCraftData().isEmpty()) {
            remainMaterials.clear();
            invConsumeSimulator.clear();
        }
        DebugData.sendDebug(
                "[CRAFT_CHAIN]Starting Layer,%s", layer.getCraftData().map(e -> "Normal").orElse("TreeRoot")
        );
        setChanged();
        return true;
    }

    public boolean startAny() {
        return startAny(false);
    }

    protected boolean startAny(boolean skipLast) {
        int totalCount = workingQueue.size();
        if (skipLast) totalCount--;
        for (int i = 0; i < totalCount; i++) {
            SolvedCraftLayer currentNode = workingQueue.peek();
            if (tryStartLayer(currentNode, layers.get(currentNode.index()))) {
                setChanged();
                return true;
            }
            if (
                    currentNode.progress().getValue() == SolvedCraftLayer.Progress.FAILED ||
                            currentNode.progress().getValue() == SolvedCraftLayer.Progress.FINISHED ||
                            currentNode.progress().getValue() == SolvedCraftLayer.Progress.DISPATCHED
            )
                workingQueue.poll();
            else
                workingQueue.add(workingQueue.poll());
        }
        return false;
    }

    public void checkAndSwitchGroup(EntityMaid maid) {
        for (SolvedCraftLayer node : nodes) {
            if (node.group() != group) continue;

            if (
                    node.progress().getValue() == SolvedCraftLayer.Progress.FAILED ||
                            node.progress().getValue() == SolvedCraftLayer.Progress.FINISHED
            )
                continue;
            return;
        }

        remainMaterials.clear();
        invConsumeSimulator.clear();
        group++;
        addAllLayerToQueue();
        MemoryUtil.getCrafting(maid).setGoPlacingBeforeCraft(true);
    }

    private void checkIsFullInv(EntityMaid maid) {
        if (!isCurrentWorking() && !isCurrentGathering() && dispatchedTaskMapping.isEmpty() && !workingQueue.isEmpty()) {
            MutableBoolean isFull = new MutableBoolean(true);
            //检查如果队列中所有的任务都不满足背包要求，那么进行一次存放
            workingQueue.forEach(node -> {
                CraftLayer layer = layers.get(node.index());
                invConsumeSimulator.snapshot();
                if (maxParallel != 0) {
                    invConsumeSimulator.removeLayerInput(layer);
                    invConsumeSimulator.addLayer(layer);
                } else {
                    invConsumeSimulator.addLayerOutput(layer);
                }
                int totalSlots = invConsumeSimulator.getCurrentSlotConsume();
                invConsumeSimulator.restoreSnapshot();
                if (totalSlots <= freeSlots) {
                    isFull.setFalse();
                }
            });

            if (hasCurrent()) {
                if (getCurrentLayer().getCraftData().isEmpty()) {
                    isFull.setFalse();
                }
            }

            if (isFull.getValue() && !Conditions.isNothingToPlace(maid) && !isStoppingAdding.value) {
                remainMaterials.clear();
                invConsumeSimulator.clear();
                MemoryUtil.getCrafting(maid).setGoPlacingBeforeCraft(true);
            }
        }
    }

    public boolean tryReleaseAndStartNext() {
        SolvedCraftLayer startNode = workingQueue.poll();
        workingQueue.add(startNode);
        return startAny(true);
    }

    // endregion

    //region 方块占用控制
    public boolean tryUseAnotherCraftGuide(ServerLevel level, EntityMaid maid) {
        CraftBlockOccupy craftBlockOccupy = CraftBlockOccupy.get(level);
        CraftLayer layer = getCurrentLayer();
        SolvedCraftLayer node = getCurrentNode();
        if (layer == null) return false;
        Optional<CraftGuideData> craftDataO = layer.getCraftData();
        if (craftDataO.isPresent()) {
            return layer.switchToNonOccupied(level, maid, node.index(), craftBlockOccupy);
        }
        return false;
    }

    public boolean checkIsCurrentOccupied(ServerLevel level, EntityMaid maid) {
        CraftBlockOccupy craftBlockOccupy = CraftBlockOccupy.get(level);
        Optional<CraftGuideData> craftDataO = getCurrentLayer().getCraftData();
        if (!craftDataO.isPresent()) return false;
        CraftGuideData craftData = craftDataO.get();
        int currentIndex = getCurrentNode().index();
        for (CraftGuideStepData stepData : craftData.getSteps()) {
            if (craftBlockOccupy.isOccupiedByNonCurrent(maid, stepData.getStorage().getPos(), currentIndex)) {
                return true;
            }
        }
        return false;
    }

    public void setOccupied(ServerLevel level, EntityMaid maid) {
        CraftBlockOccupy craftBlockOccupy = CraftBlockOccupy.get(level);
        Optional<CraftGuideData> craftDataO = getCurrentLayer().getCraftData();
        if (!craftDataO.isPresent()) return;
        CraftGuideData craftData = craftDataO.get();
        int currentIndex = getCurrentNode().index();
        for (CraftGuideStepData stepData : craftData.getSteps()) {
            if (stepData.actionType.noOccupation()) continue;
            craftBlockOccupy.addOccupy(maid, currentIndex, stepData.getStorage().getPos());
        }
    }

    public void removeOccupied(ServerLevel level, EntityMaid maid) {
        CraftBlockOccupy craftBlockOccupy = CraftBlockOccupy.get(level);
        craftBlockOccupy.removeOccupyFor(maid, getCurrentNode().index());
    }
    // endregion
}