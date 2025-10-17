package studio.fantasyit.maid_storage_manager.api.communicate.context;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import net.minecraftforge.items.wrapper.RangedWrapper;
import org.jetbrains.annotations.NotNull;
import studio.fantasyit.maid_storage_manager.communicate.ConfigurableCommunicateData;
import studio.fantasyit.maid_storage_manager.entity.VirtualItemEntity;
import studio.fantasyit.maid_storage_manager.maid.memory.ViewedInventoryMemory;
import studio.fantasyit.maid_storage_manager.util.*;

import java.util.ArrayList;
import java.util.List;

public class PlaceRequestAndSwapSlot implements IMultiTickContext, IDelayCompleteContext {
    final ConfigurableCommunicateData data;
    int currentProcessing = 0;
    int currentChecking = 0;
    private ViewedInventoryMemory viewedInventory;
    boolean done = false;
    boolean anyFail = false;
    VirtualItemEntity virtualItemEntity = null;

    public PlaceRequestAndSwapSlot(ConfigurableCommunicateData data) {
        this.data = data;
    }


    @Override
    public void complete(EntityMaid wisher, EntityMaid handler) {
        for (ConfigurableCommunicateData.Item item : data.items) {
            List<ItemStack> itemStacks = item.getItemStacks(wisher);
            for (int i = 0; i < item.itemStacks().size(); i++) {
                ItemStack required = itemStacks.get(i);
                int count = itemStacks.size();
                for (ItemStack itemStack : itemStacks) {
                    if (ItemStackUtil.isSame(itemStack, required, item.match()))
                        count -= itemStack.getCount();
                    if (count <= 0)
                        break;
                }
                if (count > 0) {
                    ItemStack toTransfer = required.copyWithCount(count);
                    ItemStack canTransfer = InvUtil.tryExtract(wisher.getAvailableInv(false), toTransfer, item.match());
                    if (!canTransfer.isEmpty()) {

                    }
                }
            }
        }
    }

    @Override
    public void start(EntityMaid wisher, EntityMaid handler) {
        viewedInventory = MemoryUtil.getViewedInventory(wisher);
    }

    @Override
    public boolean isFinished(EntityMaid wisher, EntityMaid handler) {
        return done;
    }

    @Override
    public boolean tick(EntityMaid wisher, EntityMaid handler) {
        if (virtualItemEntity != null) {
            InvUtil.pickUpVirtual(wisher, virtualItemEntity);
            if (virtualItemEntity.isAlive())
                InvUtil.pickUpVirtual(handler, virtualItemEntity);
            return false;
        }
        if (currentProcessing >= data.items.size()) {
            return generateRequestList(wisher, handler);
        }
        if (!InvUtil.hasAnyFree(wisher.getAvailableInv(false)))
            return true;
        ConfigurableCommunicateData.Item item = data.items.get(currentProcessing);
        boolean whiteMode = item.whiteMode();
        List<ItemStack> itemStacks = item.itemStacks();
        List<Integer> invThreshold = item.thresholdCount();
        //先执行放置的操作
        boolean goNext = true;

        if (!whiteMode) {
            //黑名单：遍历每个物品，取走不匹配的物品
            for (int i = 0; i < itemStacks.size(); i++) {
                ItemStack stack = itemStacks.get(i);
                if (stack.isEmpty()) continue;
                if (invThreshold.get(i) != -1 && viewedInventory.getItemCount(stack, item.match()) >= invThreshold.get(i))
                    continue;
                goNext = item.processSlotItemsAndGetIsFinished(wisher, itemStack -> {
                    if (!ItemStackUtil.isSame(stack, itemStack, item.match())) {
                        int itemStack1Count = InvUtil.maxCanPlace(wisher.getAvailableInv(false), stack);

                        if (itemStack1Count == 0)
                            anyFail = true;
                        else
                            virtualItemEntity = InvUtil.throwItemVirtual(
                                    handler,
                                    itemStack.copyWithCount(itemStack1Count),
                                    MathUtil.getFromToWithFriction(handler, wisher.position())
                            );

                        return itemStack.copyWithCount(itemStack.getCount() - itemStack1Count);
                    }
                    return stack;
                });
            }
        } else {
            //白名单：遍历每个背包物品，判断超过阈值的物品
            List<Integer> remainAllowed = new ArrayList<>();
            //记录方案中每个物品需要放置的数量
            for (ItemStack stack : itemStacks) remainAllowed.add(stack.getCount());

            //遍历每个物品
            goNext = item.processSlotItemsAndGetIsFinished(wisher, itemStack -> {
                int toPlace = itemStack.getCount();
                //使用方案预算来消费当前物品堆
                for (int i = 0; i < itemStacks.size() && toPlace > 0; i++) {
                    if (remainAllowed.get(i) <= 0) continue;
                    if (!ItemStackUtil.isSame(itemStack, itemStacks.get(i), item.match())) continue;
                    int allowCost = Math.min(remainAllowed.get(i), toPlace);
                    toPlace -= allowCost;
                    remainAllowed.set(i, remainAllowed.get(i) - allowCost);
                }
                //如果物品堆未被方案消费完，那么需要进行放置
                if (toPlace > 0) {
                    int toPlaceCount = InvUtil.maxCanPlace(wisher.getAvailableInv(false), itemStack.copyWithCount(toPlace));
                    if (toPlaceCount == 0)
                        return itemStack;
                    virtualItemEntity = InvUtil.throwItemVirtual(
                            handler,
                            itemStack.copyWithCount(toPlaceCount),
                            MathUtil.getFromToWithFriction(handler, wisher.position())
                    );
                    return itemStack.copyWithCount(itemStack.getCount() - toPlaceCount);
                }
                return itemStack;
            });
        }
        if (goNext) {
            currentProcessing++;
        }
        return false;
    }

    List<ItemStack> finallyRequestedItems = new ArrayList<>();

    private boolean generateRequestList(EntityMaid maid, EntityMaid toCommunicate) {
        if (currentChecking >= data.items.size()) {
            ItemStack vri = RequestItemUtil.makeVirtualItemStack(
                    finallyRequestedItems,
                    null,
                    maid,
                    "SWAP_REQUEST"
            );
            InvUtil.tryPlace(maid.getAvailableInv(true), vri);
            done = true;
            return true;
        }
        if (!InvUtil.hasAnyFree(maid.getAvailableInv(false)))
            return true;
        ConfigurableCommunicateData.Item item = data.items.get(currentChecking);
        if (item.whiteMode()) {
            List<ItemStack> reqs = item.itemStacks();
            List<ItemStack> itemStacks = item.getItemStacks(maid);
            for (int i = 0; i < reqs.size(); i++) {
                int count = 0;
                for (ItemStack itemStack : itemStacks) {
                    if (ItemStackUtil.isSame(itemStack, reqs.get(i), item.match())) {
                        count += itemStack.getCount();
                    }
                }
                if (count < reqs.get(i).getCount()) {
                    ItemStackUtil.addToList(
                            finallyRequestedItems,
                            reqs.get(i).copyWithCount(reqs.get(i).getCount() - count),
                            item.match()
                    );
                }
            }
        }
        currentChecking++;
        return false;
    }

    @Override
    public void stop(EntityMaid wisher, EntityMaid handler) {
    }

    private ItemStack tryPlace(EntityMaid maid, ItemStack itemStack, ConfigurableCommunicateData.SlotType slot) {
        return switch (slot) {
            case ALL -> InvUtil.tryPlace(maid.getAvailableInv(false), itemStack);
            case HEAD -> placeToSlotType(maid, itemStack, EquipmentSlot.HEAD);
            case CHEST -> placeToSlotType(maid, itemStack, EquipmentSlot.CHEST);
            case LEGS -> placeToSlotType(maid, itemStack, EquipmentSlot.LEGS);
            case FEET -> placeToSlotType(maid, itemStack, EquipmentSlot.FEET);
            case MAIN_HAND -> placeToSlotType(maid, itemStack, EquipmentSlot.MAINHAND);
            case OFF_HAND -> placeToSlotType(maid, itemStack, EquipmentSlot.OFFHAND);
            case FLOWER -> {
                RangedWrapper inv = maid.getAvailableBackpackInv();
                RangedWrapper onlyLast = new RangedWrapper(inv, 5, 6);
                yield InvUtil.tryPlace(onlyLast, itemStack);
            }
            case ETA -> {
                RangedWrapper inv = maid.getAvailableBackpackInv();
                CombinedInvWrapper noLast = new CombinedInvWrapper(
                        new RangedWrapper(inv, 0, 5),
                        new RangedWrapper(inv, 6, inv.getSlots())
                );
                yield InvUtil.tryPlace(noLast, itemStack);
            }
            case BAUBLE -> InvUtil.tryPlace(maid.getMaidBauble(), itemStack);
        };
    }

    private @NotNull ItemStack placeToSlotType(EntityMaid maid, ItemStack itemStack, EquipmentSlot slot) {
        ItemStack canPlace = getCanPlace(itemStack, maid.getItemBySlot(slot));
        if (canPlace.isEmpty())
            return itemStack;
        ItemStack toPlace = canPlace.copy();
        toPlace.grow(maid.getItemBySlot(slot).getCount());
        maid.setItemSlot(slot, toPlace);
        return itemStack.copyWithCount(itemStack.getCount() - toPlace.getCount());
    }

    private ItemStack getCanPlace(ItemStack original, ItemStack income) {
        if (original.isEmpty())
            return income;
        if (original.getItem() == income.getItem())
            return original.getCount() + income.getCount() > original.getMaxStackSize() ?
                    original.copyWithCount(original.getMaxStackSize()) :
                    original.copyWithCount(original.getCount() + income.getCount());
        return ItemStack.EMPTY;
    }
}
