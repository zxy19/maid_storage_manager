package studio.fantasyit.maid_storage_manager.api.communicate.context;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.inventory.handler.BaubleItemHandler;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import net.minecraftforge.items.wrapper.RangedWrapper;
import studio.fantasyit.maid_storage_manager.data.ConfigurableCommunicateData;
import studio.fantasyit.maid_storage_manager.maid.memory.ViewedInventoryMemory;
import studio.fantasyit.maid_storage_manager.util.InvUtil;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;
import studio.fantasyit.maid_storage_manager.util.RequestItemUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class PlaceRequestAndSwapSlot implements IMultiTickContext, IDelayCompleteContext {
    final ConfigurableCommunicateData data;
    int currentProcessing = 0;
    int currentChecking = 0;
    private ViewedInventoryMemory viewedInventory;
    boolean done = false;
    boolean anyFail = false;

    public PlaceRequestAndSwapSlot(ConfigurableCommunicateData data) {
        this.data = data;
    }


    @Override
    public void complete(EntityMaid maid, EntityMaid toCommunicate) {

    }

    @Override
    public void start(EntityMaid maid, EntityMaid toCommunicate) {
        viewedInventory = MemoryUtil.getViewedInventory(maid);
    }

    @Override
    public boolean isFinished(EntityMaid maid, EntityMaid toCommunicate) {
        return done;
    }

    @Override
    public boolean tick(EntityMaid maid, EntityMaid toCommunicate) {
        if (currentProcessing >= data.items.size()) {
            return generateRequestList(maid, toCommunicate);
        }
        if (!InvUtil.hasAnyFree(maid.getAvailableInv(false)))
            return true;
        ConfigurableCommunicateData.Item item = data.items.get(currentProcessing);
        boolean whiteMode = item.whiteMode();
        List<ItemStack> itemStacks = item.itemStacks();
        List<Integer> invThreshold = item.thresholdCount();
        //先执行放置的操作

        if (!whiteMode) {
            //黑名单：遍历每个物品，取走不匹配的物品
            for (int i = 0; i < itemStacks.size(); i++) {
                ItemStack stack = itemStacks.get(i);
                if (stack.isEmpty()) continue;
                if (invThreshold.get(i) != -1 && viewedInventory.getItemCount(stack, item.match()) >= invThreshold.get(i))
                    continue;
                processSlotItems(maid, item.slot(), itemStack -> {
                    if (!ItemStackUtil.isSame(stack, itemStack, item.match())) {
                        ItemStack itemStack1 = InvUtil.tryPlace(maid.getAvailableInv(false), stack);
                        if (!itemStack1.isEmpty())
                            anyFail = true;
                        return itemStack1;
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
            processSlotItems(maid, item.slot(), itemStack -> {
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
                    ItemStack remainNotPlaced = InvUtil.tryPlace(maid.getAvailableInv(false), itemStack.copyWithCount(toPlace));
                    ItemStack result = itemStack.copy();
                    result.shrink(toPlace);
                    result.grow(remainNotPlaced.getCount());
                    return result;
                }
                return itemStack;
            });
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
            List<ItemStack> itemStacks = getItemStacks(maid, item.slot());
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
    public void stop(EntityMaid maid, EntityMaid toCommunicate) {
        //todo
    }

    private List<ItemStack> getItemStacks(EntityMaid maid, ConfigurableCommunicateData.SlotType slot) {
        List<ItemStack> list = new ArrayList<>();
        switch (slot) {
            case ALL -> {
                CombinedInvWrapper availableInv = maid.getAvailableInv(false);
                for (int i = 0; i < availableInv.getSlots(); i++) {
                    list.add(availableInv.getStackInSlot(i));
                }
            }
            case HEAD -> list.add(maid.getItemBySlot(EquipmentSlot.HEAD));
            case CHEST -> list.add(maid.getItemBySlot(EquipmentSlot.CHEST));
            case LEGS -> list.add(maid.getItemBySlot(EquipmentSlot.LEGS));
            case FEET -> list.add(maid.getItemBySlot(EquipmentSlot.FEET));
            case MAIN_HAND -> list.add(maid.getItemBySlot(EquipmentSlot.MAINHAND));
            case OFF_HAND -> list.add(maid.getItemBySlot(EquipmentSlot.OFFHAND));
            case BAUBLE -> {
                BaubleItemHandler bauble = maid.getMaidBauble();
                for (int i = 0; i < bauble.getSlots(); i++) {
                    list.add(bauble.getStackInSlot(i));
                }
            }
            case FLOWER -> {
                RangedWrapper inv = maid.getAvailableBackpackInv();
                if (inv.getSlots() > 5)
                    list.add(inv.getStackInSlot(5));
            }
            case ETA -> {
                RangedWrapper inv = maid.getAvailableBackpackInv();
                for (int i = 0; i < inv.getSlots(); i++) {
                    if (i != 5)
                        list.add(inv.getStackInSlot(i));
                }
            }
        }
        return list;
    }

    private void processSlotItems(EntityMaid maid, ConfigurableCommunicateData.SlotType slot, Function<ItemStack, ItemStack> process) {
        switch (slot) {
            case ALL -> {
                CombinedInvWrapper availableInv = maid.getAvailableInv(false);
                for (int i = 0; i < availableInv.getSlots(); i++) {
                    availableInv.setStackInSlot(i, process.apply(availableInv.getStackInSlot(i)));
                }
            }
            case HEAD -> maid.setItemSlot(EquipmentSlot.HEAD, process.apply(maid.getItemBySlot(EquipmentSlot.HEAD)));
            case CHEST -> maid.setItemSlot(EquipmentSlot.CHEST, process.apply(maid.getItemBySlot(EquipmentSlot.CHEST)));
            case LEGS -> maid.setItemSlot(EquipmentSlot.LEGS, process.apply(maid.getItemBySlot(EquipmentSlot.LEGS)));
            case FEET -> maid.setItemSlot(EquipmentSlot.FEET, process.apply(maid.getItemBySlot(EquipmentSlot.FEET)));
            case MAIN_HAND ->
                    maid.setItemSlot(EquipmentSlot.MAINHAND, process.apply(maid.getItemBySlot(EquipmentSlot.MAINHAND)));
            case OFF_HAND ->
                    maid.setItemSlot(EquipmentSlot.OFFHAND, process.apply(maid.getItemBySlot(EquipmentSlot.OFFHAND)));
            case BAUBLE -> {
                BaubleItemHandler bauble = maid.getMaidBauble();
                for (int i = 0; i < bauble.getSlots(); i++) {
                    bauble.setStackInSlot(i, process.apply(bauble.getStackInSlot(i)));
                }
            }
            case FLOWER -> {
                RangedWrapper inv = maid.getAvailableBackpackInv();
                if (inv.getSlots() > 5)
                    inv.setStackInSlot(5, process.apply(inv.getStackInSlot(5)));
            }
            case ETA -> {
                RangedWrapper inv = maid.getAvailableBackpackInv();
                for (int i = 0; i < inv.getSlots(); i++) {
                    if (i != 5)
                        inv.setStackInSlot(i, process.apply(inv.getStackInSlot(i)));
                }
            }
        }
    }
}
