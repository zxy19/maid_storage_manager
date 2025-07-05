package studio.fantasyit.maid_storage_manager.craft.work;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.capability.CraftBlockOccupyDataProvider;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.debug.DebugData;
import studio.fantasyit.maid_storage_manager.items.RequestListItem;
import studio.fantasyit.maid_storage_manager.maid.ChatTexts;
import studio.fantasyit.maid_storage_manager.maid.behavior.ScheduleBehavior;
import studio.fantasyit.maid_storage_manager.maid.memory.CraftMemory;
import studio.fantasyit.maid_storage_manager.maid.memory.RequestProgressMemory;
import studio.fantasyit.maid_storage_manager.maid.memory.ViewedInventoryMemory;
import studio.fantasyit.maid_storage_manager.maid.task.StorageManageTask;
import studio.fantasyit.maid_storage_manager.util.InvUtil;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;

import java.util.*;

public class CraftLayerChain {
    public static final Codec<CraftLayerChain> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    CraftLayer.CODEC.listOf().fieldOf("layers").forGetter(t -> t.layers),
                    SolvedCraftLayer.CODEC.listOf().fieldOf("nodes").forGetter(t -> t.nodes),
                    ItemStack.CODEC.listOf().fieldOf("remainMaterials").forGetter(t -> t.remainMaterials),
                    Codec.INT.fieldOf("freeSlots").forGetter(t -> t.freeSlots),
                    Codec.INT.fieldOf("group").forGetter(t -> t.group),
                    Codec.BOOL.fieldOf("freeze").forGetter(t -> t.freeze),
                    Codec.STRING.fieldOf("isAboutToReschedule").forGetter(t -> t.isStoppingAdding.name())
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
    protected StoppingAdding isStoppingAdding = StoppingAdding.NONE;


    protected CraftLayerChain(
            List<CraftLayer> layers,
            List<SolvedCraftLayer> nodes,
            List<ItemStack> remainMaterials,
            int freeSlots,
            int groupId,
            boolean freeze,
            String stoppingAdding
    ) {
        this.layers = new ArrayList<>(layers);
        this.nodes = new ArrayList<>(nodes);
        this.remainMaterials = new ArrayList<>(remainMaterials);
        this.group = groupId;
        this.freeze = freeze;
        this.freeSlots = freeSlots;
        this.workingQueue = new LinkedList<>();
        changedTarget = true;
        this.isStoppingAdding = StoppingAdding.valueOf(stoppingAdding);
        if (freeze) {
            addAllLayerToQueue();
        }
    }

    public CraftLayerChain(EntityMaid maid) {
        this(
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                InvUtil.freeSlots(maid.getAvailableInv(true)),
                0,
                false,
                StoppingAdding.NONE.name()
        );
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
            //简单估算层的格子消耗量。
            MutableInt inputConsume = new MutableInt(0);
            MutableInt outputConsume = new MutableInt(0);
            for (ItemStack item : layer.getItems())
                inputConsume.add(Math.ceil((double) item.getCount() / item.getMaxStackSize()));
            layer.getCraftData().ifPresent(craftData -> {
                craftData.getAllOutputItemsWithOptional().forEach(item -> {
                    outputConsume.add(Math.ceil((double) item.getCount() / item.getMaxStackSize()));
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

            //构建节点列表
            nodes.add(new SolvedCraftLayer(
                    i,
                    groupId.getValue(),
                    inputConsume.getValue(),
                    outputConsume.getValue(),
                    new ArrayList<>(),
                    new MutableInt(0),
                    new MutableObject<>(SolvedCraftLayer.Progress.IDLE)
            ));
        }
        for (int i = 0; i < layers.size(); i++) {
            CraftLayer layer = layers.get(i);
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
            layer.getCraftData()
                    .ifPresentOrElse(
                            //把当前层的输出存放到零时栈
                            craftGuide -> {
                                List<ItemStack> outputs = craftGuide.getOutput();
                                for (ItemStack itemStack : outputs) {
                                    currentItems.add(
                                            new Pair<>(
                                                    finalI,
                                                    itemStack.copyWithCount(itemStack.getCount() * layer.getCount())
                                            )
                                    );
                                }
                            },
                            // 已经结束了一次合成，清空临时栈
                            () -> {
                                currentItems.clear();
                                groupId.add(1);
                            }
                    );
        }
        group = 0;
        freeze = true;
        addAllLayerToQueue();
    }


    public boolean isDone() {
        return workingQueue.isEmpty();
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
    //endregion


    // region 分发层相关的方法
    public @Nullable CraftLayer getAndDispatchLayer() {
        if (workingQueue.size() == 1) return null;
        for (int i = 0; i < layers.size(); i++) {
            SolvedCraftLayer node = nodes.get(i);
            if (node.group() != group)
                continue;
            CraftLayer layer = layers.get(i);

            if (node.progress().getValue() == SolvedCraftLayer.Progress.IDLE) {
                node.progress().setValue(SolvedCraftLayer.Progress.DISPATCHED);
                return layer;
            }
        }
        return null;
    }

    public void getDispatchedRemainItem(CraftLayer outerLayer) {
        for (int i = 0; i < this.remainMaterials.size(); i++) {
            ItemStack toTake = outerLayer.memorizeItem(remainMaterials.get(i), remainMaterials.get(i).getCount());
            remainMaterials.get(i).shrink(toTake.getCount());
            if (remainMaterials.get(i).isEmpty()) {
                remainMaterials.remove(i);
                i--;
            }
        }
    }

    /**
     * 分发的节点完成的回调。通过对比craftGuide和count来判断是否同一个节点
     *
     * @param outerLayer
     * @return
     */
    public boolean dispatchedDone(CraftLayer outerLayer) {
        for (int i = 0; i < layers.size(); i++) {
            SolvedCraftLayer node = nodes.get(i);
            CraftLayer layer = layers.get(i);
            if (node.progress().getValue() != SolvedCraftLayer.Progress.DISPATCHED) continue;
            if (layer.equals(outerLayer)) {
                finishLayer(node, layer);
                return true;
            }
        }
        return false;
    }

    public boolean checkDispatchedValidation(EntityMaid maid) {
        if (!(maid.level() instanceof ServerLevel level)) return false;
        HashSet<Map.Entry<UUID, Pair<Integer, UUID>>> entries = new HashSet<>(dispatchedTaskMapping.entrySet());
        for (Map.Entry<UUID, Pair<Integer, UUID>> p : entries) {
            boolean valid = true;
            if (level.getEntity(p.getKey()) instanceof EntityMaid dispatchedMaid) {
                if (!dispatchedMaid.getTask().getUid().equals(StorageManageTask.TASK_ID))
                    valid = false;
                else {
                    RequestProgressMemory requestProgress = MemoryUtil.getRequestProgress(maid);
                    CraftMemory crafting = MemoryUtil.getCrafting(maid);
                    ScheduleBehavior.Schedule currentWorking = MemoryUtil.getCurrentlyWorking(maid);
                    if (currentWorking != ScheduleBehavior.Schedule.REQUEST)
                        valid = false;
                    else if (!requestProgress.getWorkUUID().equals(p.getValue().getB()))
                        valid = false;
                }
            } else valid = false;
            if (!valid) {
                dispatchedTaskMapping.remove(p.getKey());
            }
        }

        return true;
    }
    // endregion


    // region 当前工作状态
    public boolean isCurrentGathering() {
        if (isDone()) return false;
        return getCurrentNode().progress().getValue() == SolvedCraftLayer.Progress.GATHERING;
    }

    public boolean isCurrentWorking() {
        if (isDone()) return false;
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
        ChatTexts.showSecondary(maid,
                Component.translatable(
                        ChatTexts.CHAT_SECONDARY_CRAFTING,
                        done,
                        total,
                        isCurrentWorking() ? Component.translatable(ChatTexts.CHAT_SECONDARY_CRAFTING_WORK) :
                                Component.translatable(ChatTexts.CHAT_SECONDARY_CRAFTING_GATHER)
                ),
                ((double) total / done)
        );
    }


    // region 流程控制

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

    /**
     * 停止添加的检查事件。用于在结束后清空CraftMemory
     *
     * @param maid
     */
    public void handleStopAddingEvent(EntityMaid maid) {
        if (isStoppingAdding == StoppingAdding.NONE)
            return;
        if (hasCurrent()) return;
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
        ChatTexts.send(
                maid,
                Component.translatable(
                        ChatTexts.CHAT_CRAFTING_FAIL,
                        layer
                                .getCraftData()
                                .map(CraftGuideData::getOutput)
                                .map(l -> l.get(0).getHoverName())
                                .orElse(Component.empty())
                )
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


        //加入下一组的初始合成目标
        group++;
        addAllLayerToQueue();

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
        CraftLayer layer = Objects.requireNonNull(this.getCurrentLayer());
        SolvedCraftLayer node = Objects.requireNonNull(this.getCurrentNode());
        if (layer.hasCollectedAll()) {
            //收集完了再次检查满足开始条件。如果否则开始前再次进行补齐
            if (!checkInputInbackpack(maid)) return;
            showCraftingProgress(maid);
            ChatTexts.send(maid,
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
            return false;
        }
        return true;
    }


    /**
     * 停止当前层。将工作状态设置为Finished
     */
    public void finishCurrentLayer() {
        SolvedCraftLayer node = getCurrentNode();
        CraftLayer layer = getCurrentLayer();
        finishLayer(node, layer);
    }

    private void finishLayer(SolvedCraftLayer node, CraftLayer layer) {
        if (node.progress().getValue() == SolvedCraftLayer.Progress.DISPATCHED)
            freeSlots += node.slotOutput();
        else
            freeSlots += node.slotConsume();
        node.progress().setValue(SolvedCraftLayer.Progress.FINISHED);
        workingQueue.poll();


        layer.getCraftData().ifPresent(data -> {
            data.getOutput().forEach(itemStack -> {
                if (itemStack.isEmpty()) return;
                this.remainMaterials.add(itemStack.copyWithCount(itemStack.getCount() * layer.getCount()));
            });
        });

        node.nextIndex().forEach(index -> {
            SolvedCraftLayer nextNode = nodes.get(index);
            nextNode.inDegree().decrement();

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
        if (node.progress().getValue() == SolvedCraftLayer.Progress.FINISHED || node.progress().getValue() == SolvedCraftLayer.Progress.FAILED || node.progress().getValue() == SolvedCraftLayer.Progress.DISPATCHED)
            return false;
        if (node.progress().getValue() == SolvedCraftLayer.Progress.WORKING)
            return true;
        if (node.progress().getValue() == SolvedCraftLayer.Progress.GATHERING && !isStoppingAdding.value)
            return true;
        if (isStoppingAdding.value)
            return false;
        if (node.inDegree().getValue() > 0)
            return false;
        if (node.slotConsume() > freeSlots)
            return false;
        freeSlots -= node.slotConsume();
        node.progress().setValue(SolvedCraftLayer.Progress.GATHERING);
        for (int i = 0; i < this.remainMaterials.size(); i++) {
            ItemStack toTake = layer.memorizeItem(remainMaterials.get(i), remainMaterials.get(i).getCount());
            remainMaterials.get(i).shrink(toTake.getCount());
            if (remainMaterials.get(i).isEmpty()) {
                remainMaterials.remove(i);
                i--;
            }
        }
        if (layer.hasCollectedAll()) {
            node.progress().setValue(SolvedCraftLayer.Progress.WORKING);
        }
        //来到树根了，清除剩余的材料（因为女仆会尝试放置到附近）
        if (layer.getCraftData().isEmpty()) {
            remainMaterials.clear();
        }
        DebugData.sendDebug(
                "[CRAFT_CHAIN]Starting Layer,%s", layer.getCraftData().map(e -> "Normal").orElse("TreeRoot")
        );
        setChanged();
        return true;
    }

    protected boolean startAny() {
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
            ) continue;
            workingQueue.add(workingQueue.poll());
        }
        return false;
    }

    public boolean tryReleaseAndStartNext() {
        SolvedCraftLayer startNode = workingQueue.poll();
        workingQueue.add(startNode);
        return startAny(true);
    }

    // endregion

    //region 方块占用控制

    public boolean checkIsCurrentOccupied(ServerLevel level, EntityMaid maid) {
        CraftBlockOccupyDataProvider.CraftBlockOccupy craftBlockOccupy = CraftBlockOccupyDataProvider.get(level);
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
        CraftBlockOccupyDataProvider.CraftBlockOccupy craftBlockOccupy = CraftBlockOccupyDataProvider.get(level);
        Optional<CraftGuideData> craftDataO = getCurrentLayer().getCraftData();
        if (!craftDataO.isPresent()) return;
        CraftGuideData craftData = craftDataO.get();
        int currentIndex = getCurrentNode().index();
        for (CraftGuideStepData stepData : craftData.getSteps()) {
            craftBlockOccupy.addOccupy(maid, currentIndex, stepData.getStorage().getPos());
        }
    }

    public void removeOccupied(ServerLevel level, EntityMaid maid) {
        CraftBlockOccupyDataProvider.CraftBlockOccupy craftBlockOccupy = CraftBlockOccupyDataProvider.get(level);
        craftBlockOccupy.removeOccupyFor(maid, getCurrentNode().index());
    }

    // endregion
}