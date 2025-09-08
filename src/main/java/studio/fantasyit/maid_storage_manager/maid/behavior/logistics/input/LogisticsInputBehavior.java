package studio.fantasyit.maid_storage_manager.maid.behavior.logistics.input;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftResultContext;
import studio.fantasyit.maid_storage_manager.craft.work.CraftLayer;
import studio.fantasyit.maid_storage_manager.items.FilterListItem;
import studio.fantasyit.maid_storage_manager.items.LogisticsGuide;
import studio.fantasyit.maid_storage_manager.maid.ChatTexts;
import studio.fantasyit.maid_storage_manager.maid.behavior.ScheduleBehavior;
import studio.fantasyit.maid_storage_manager.maid.memory.LogisticsMemory;
import studio.fantasyit.maid_storage_manager.maid.memory.ViewedInventoryMemory;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;
import studio.fantasyit.maid_storage_manager.storage.MaidStorage;
import studio.fantasyit.maid_storage_manager.storage.StorageVisitLock;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.storage.base.IMaidStorage;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageContext;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageExtractableContext;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageInteractContext;
import studio.fantasyit.maid_storage_manager.util.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class LogisticsInputBehavior extends Behavior<EntityMaid> {
    public LogisticsInputBehavior() {
        super(Map.of());
    }

    BehaviorBreath breath = new BehaviorBreath();
    //采集目标
    CraftLayer layer;
    CraftLayer output;
    IStorageContext context;
    IStorageContext contextView;
    private Target target;
    int failCount = 0;
    StorageVisitLock.LockContext lock;

    @Override
    protected boolean canStillUse(ServerLevel level, EntityMaid maid, long p_22547_) {
        if (MemoryUtil.getCurrentlyWorking(maid) != ScheduleBehavior.Schedule.LOGISTICS) return false;
        if (!MemoryUtil.getLogistics(maid).shouldWork()) return false;
        if (MemoryUtil.getLogistics(maid).getStage() != LogisticsMemory.Stage.INPUT) return false;
        if (context == null || contextView == null) return false;
        if (layer != null) {
            if (!layer.hasCollectedAll()) {
                if (context != null && context.isDone()) {
                    context.reset();
                    failCount++;
                    return failCount <= Config.maxLogisticsTries;
                }
                return true;
            } else {
                return false;
            }
        } else if (failCount > Config.maxLogisticsTries) {
            return false;
        }
        return !context.isDone();
    }

    @Override
    protected boolean checkExtraStartConditions(@NotNull ServerLevel level, @NotNull EntityMaid maid) {
        if (MemoryUtil.getCurrentlyWorking(maid) != ScheduleBehavior.Schedule.LOGISTICS) return false;
        if (MemoryUtil.getLogistics(maid).getCurrentLogisticsGuideItem().isEmpty()) return false;
        if (MemoryUtil.getLogistics(maid).getStage() != LogisticsMemory.Stage.INPUT) return false;
        return Conditions.hasReachedValidTargetOrReset(maid);
    }


    @Override
    protected void start(@NotNull ServerLevel level, @NotNull EntityMaid maid, long gameTimeIn) {
        lock = StorageVisitLock.DUMMY;
        target = MemoryUtil.getLogistics(maid).getTarget();
        IMaidStorage storage = MaidStorage.getInstance().getStorage(target.getType());
        if (storage == null)
            return;
        contextView = storage.onStartView(level, maid, target);
        failCount = 0;
        layer = null;
        output = null;

        if (contextView != null)
            contextView.start(maid, level, target);
        else
            return;

        context = storage.onStartCollect(level, maid, target);
        if (context != null)
            context.start(maid, level, target);


        //重算Inv
        MemoryUtil.getViewedInventory(maid).resetViewedInvForPos(target);
        StorageAccessUtil.checkNearByContainers(level, target.getPos(), pos -> {
            MemoryUtil.getViewedInventory(maid).resetViewedInvForPosAsRemoved(target.sameType(pos, null));
        });
        lock = StorageVisitLock.getReadLock(target);
    }

    @Override
    protected void tick(ServerLevel level, EntityMaid maid, long p_22553_) {
        if (context == null || contextView == null) return;
        if (!lock.checkAndTryGrantLock()) return;

        if (!contextView.isDone()) {//第一步，重新计算Inventory
            tickView(level, maid, p_22553_);
        } else if (layer == null) {// 然后根据属性计算搬运的物品以及数量
            calculateLayer(level, maid);
        } else if (layer != null) {// 最后，提取
            tickGathering(level, maid, p_22553_);
        }
    }

    private void calculateLayer(ServerLevel level, EntityMaid maid) {
        ItemStack currentLogisticsGuideItem = MemoryUtil.getLogistics(maid).getCurrentLogisticsGuideItem();
        CraftGuideData craftGuideData = LogisticsGuide.getCraftGuideData(currentLogisticsGuideItem);
        List<ViewedInventoryMemory.ItemCount> itemsAt = MemoryUtil.getViewedInventory(maid).getItemsAt(target)
                .stream().filter(itemCount -> !itemCount.item().is(ItemRegistry.REQUEST_LIST_ITEM.get()) && !itemCount.item().isEmpty()).toList();
        ItemStack filterItemStack = LogisticsGuide.getFilterItemStack(currentLogisticsGuideItem);
        if (!filterItemStack.isEmpty()) {
            List<ItemStack> filteredItems = new ArrayList<>();
            CompoundTag t = filterItemStack.getOrCreateTag();
            ListTag list = t.getList(FilterListItem.TAG_ITEMS, ListTag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag tmp = list.getCompound(i);
                ItemStack item = ItemStackUtil.parseStack(tmp.getCompound(FilterListItem.TAG_ITEMS_ITEM));
                filteredItems.add(item);
            }
            boolean matchNbt = t.getBoolean(FilterListItem.TAG_MATCH_TAG);
            boolean isBlackMode = t.getBoolean(FilterListItem.TAG_BLACK_MODE);
            itemsAt = itemsAt.stream().filter(itemCount -> {
                boolean match = filteredItems.stream().anyMatch(itemStack -> ItemStackUtil.isSame(itemStack, itemCount.item(), matchNbt));
                return (match != isBlackMode);
            }).toList();
        }
        int count = LogisticsGuide.getWorkCount(currentLogisticsGuideItem);
        if (craftGuideData != null) {
            count = getAvailableCountFromCraftAndSetLayer(maid, craftGuideData, itemsAt, count);
            if (count == 0) {
                lock.release();
                lock = StorageVisitLock.getReadLock(target);
                contextView.reset();
                MemoryUtil.getViewedInventory(maid).resetViewedInvForPos(target);
                failCount++;
                layer = null;
                output = null;
            }
        } else if (!itemsAt.isEmpty()) {
            ViewedInventoryMemory.ItemCount itemCount = itemsAt.get(0);
            ItemStack itemStack = itemCount.item();
            if (count == 64)
                itemStack = itemStack.copyWithCount(Math.min(itemCount.count(), itemStack.getMaxStackSize()));
            else
                itemStack = itemStack.copyWithCount(1);

            layer = new CraftLayer(Optional.empty(), List.of(itemStack), 1);
            output = new CraftLayer(Optional.empty(), List.of(itemStack), 1);
        } else {
            contextView.reset();
            MemoryUtil.getViewedInventory(maid).resetViewedInvForPos(target);
            failCount++;
        }

        if (!MemoryUtil.getLogistics(maid).hasMultipleGuide(maid))
            failCount = 0;
    }

    private int getAvailableCountFromCraftAndSetLayer(EntityMaid maid, CraftGuideData craftGuideData, List<ViewedInventoryMemory.ItemCount> itemsAt, int count) {
        List<ItemStack> inputs = craftGuideData.getInput();
        List<ItemStack> costedInputs = inputs.stream().map(ItemStack::copy).toList();
        for (ItemStack output : craftGuideData.getOutput()) {
            int outputCount = output.getCount();
            for (ItemStack itemStack : costedInputs) {
                if (!itemStack.isEmpty() && ItemStackUtil.isSame(output, itemStack, false)) {
                    int remove = Math.min(outputCount, itemStack.getCount());
                    itemStack.shrink(remove);
                    outputCount -= remove;
                }
                if (outputCount <= 0) break;
            }
        }
        for (int i = 0; i < inputs.size(); i++) {
            ItemStack input = inputs.get(i);
            ItemStack costedInput = costedInputs.get(i);
            int availableCount = 0;
            for (ViewedInventoryMemory.ItemCount itemCount : itemsAt) {
                if (ItemStackUtil.isSame(input, itemCount.item(), false)) {
                    availableCount += itemCount.getSecond();
                }
            }
            if (availableCount < input.getCount())
                count = 0;
            else if (costedInput.getCount() != 0)
                count = Math.min(count, (availableCount - input.getCount()) / costedInput.getCount() + 1);
        }
        List<ItemStack> toSimulate = new ArrayList<>(inputs.stream().map(ItemStack::copy).toList());
        List<CraftLayer> toTest = new ArrayList<>();

        //这里使用一个包含了count的层来检查背包空间是否够用
        CraftLayer test = new CraftLayer(Optional.of(craftGuideData), toSimulate, 1);
        for (int i = 0; i < count; i++) {
            toTest.add(test);
        }
        for (; count > 0; count--) {
            for (int i = 0; i < inputs.size(); i++)
                toSimulate.get(i).setCount(costedInputs.get(i).getCount() * count + inputs.get(i).getCount());
            CraftResultContext context = new CraftResultContext(toTest);
            if (context.getSlotConsume() < InvUtil.freeSlots(maid.getAvailableInv(false))) {
                break;
            }
            //如果不符合，那么count-1再次尝试
            toTest.remove(0);
        }

        if (count == 0)
            return 0;

        //构造新的层
        for (int i = 0; i < inputs.size(); i++)
            if (count <= 1)
                toSimulate.get(i).setCount(inputs.get(i).getCount());
            else
                toSimulate.get(i).setCount(costedInputs.get(i).getCount() * (count - 1) + inputs.get(i).getCount());
        layer = new CraftLayer(Optional.of(craftGuideData), toSimulate, count);
        CraftResultContext remainContext = new CraftResultContext(toTest);
        List<ItemStack> remains = new ArrayList<>();
        remainContext.forEachRemaining((itemStack, c) -> ItemStackUtil.addToList(remains, itemStack.copyWithCount(c), false));
        output = new CraftLayer(Optional.empty(),
                remains,
                count);
        return count;
    }

    private void tickView(ServerLevel level, EntityMaid maid, long p22553) {
        if (contextView instanceof IStorageInteractContext isic) {
            isic.tick(itemStack -> {
                MemoryUtil.getViewedInventory(maid).addItem(this.target, itemStack);
                return itemStack;
            });
        }
    }

    /**
     * Tick，获取原材料
     *
     * @param level
     * @param maid
     * @param p_22553_
     */
    protected void tickGathering(ServerLevel level, EntityMaid maid, long p_22553_) {
        Function<ItemStack, ItemStack> taker = (ItemStack itemStack) -> {
            int maxStore = InvUtil.maxCanPlace(maid.getAvailableInv(false), itemStack);
            if (maxStore > 0) {
                ItemStack copy = itemStack.copy();
                ItemStack toTake = layer.memorizeItem(itemStack, maxStore);
                if (toTake.getCount() > 0)
                    ChatTexts.send(maid,
                            Component.translatable(
                                    ChatTexts.CHAT_MOVING_TAKEN,
                                    itemStack.getHoverName(),
                                    String.valueOf(toTake.getCount())
                            )
                    );
                copy.shrink(toTake.getCount());
                ViewedInventoryUtil.ambitiousRemoveItemAndSync(maid, level, target, itemStack, toTake.getCount());
                InvUtil.tryPlace(maid.getAvailableInv(false), toTake);
                return copy;
            }
            return itemStack;
        };
        if (context instanceof IStorageInteractContext isic) {
            isic.tick(taker);
        } else if (context instanceof IStorageExtractableContext isec) {
            if (isec.hasTask())
                isec.tick(taker);
            else
                isec.setExtract(layer.getUnCollectedItems(), ItemStackUtil.MATCH_TYPE.AUTO);
        }
    }

    @Override
    protected void stop(ServerLevel level, EntityMaid maid, long p_22550_) {
        lock.release();
        super.stop(level, maid, p_22550_);
        if (contextView != null) {
            contextView.finish();
            if (contextView.isDone()) {
                Target target = MemoryUtil.getCrafting(maid).getTarget();
                MemoryUtil.getCrafting(maid).addVisitedPos(target);
                StorageAccessUtil.checkNearByContainers(level, target.getPos(), (pos) -> {
                    MemoryUtil.getCrafting(maid).addVisitedPos(target.sameType(pos, null));
                });
            }
        }

        if (context != null) {
            context.finish();
        }

        if (layer != null && output != null) {
            if (layer.getCollectedCounts().stream().anyMatch(i -> i > 0)) {
                MemoryUtil.getLogistics(maid).setCraftAndResultLayer(layer, output);
                if (layer.getCraftData().isPresent()) {
                    MemoryUtil.getLogistics(maid).setStage(LogisticsMemory.Stage.CRAFT);
                    if (!layer.getItems().isEmpty())
                        ChatTexts.send(maid, Component.translatable(ChatTexts.CHAT_CRAFT_WORK, layer.getItems().get(0).getHoverName()));
                } else {
                    MemoryUtil.getLogistics(maid).setStage(LogisticsMemory.Stage.OUTPUT);
                    if (!layer.getItems().isEmpty())
                        ChatTexts.send(maid, Component.translatable(ChatTexts.CHAT_MOVING, layer.getItems().get(0).getHoverName()));
                }
            }
        } else {
            MemoryUtil.getLogistics(maid).setStage(LogisticsMemory.Stage.FINISH);
        }
        if (StorageAccessUtil.findTargetRewrite(level, maid, target, false).isEmpty()) {
            MemoryUtil.getViewedInventory(maid).resetViewedInvForPosAsRemoved(target);
        }
        MemoryUtil.getLogistics(maid).clearTarget();
        MemoryUtil.clearTarget(maid);
    }

    @Override
    protected boolean timedOut(long p_22537_) {
        return false;
    }
}