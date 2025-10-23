package studio.fantasyit.maid_storage_manager.communicate;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.inventory.handler.BaubleItemHandler;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import net.minecraftforge.items.wrapper.RangedWrapper;
import studio.fantasyit.maid_storage_manager.util.InvUtil;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public enum SlotType {
    ALL,
    HEAD,
    CHEST,
    LEGS,
    FEET,
    MAIN_HAND,
    OFF_HAND,
    FLOWER,
    ETA,
    BAUBLE;


    public List<ItemStack> getItemStacks(EntityMaid maid) {
        List<ItemStack> list = new ArrayList<>();
        switch (this) {
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

    public Optional<Integer> processSlotItemsAndGetIsFinished(EntityMaid maid, int startIndex, BiFunction<ItemStack, Integer, ItemStack> process) {
        switch (this) {
            case ALL -> {
                CombinedInvWrapper availableInv = maid.getAvailableInv(false);
                return resetSlotItemWithProcessAndCheckIfAnyChanged(process, availableInv, startIndex);
            }
            case HEAD -> maid.setItemSlot(EquipmentSlot.HEAD, process.apply(maid.getItemBySlot(EquipmentSlot.HEAD),0));
            case CHEST -> maid.setItemSlot(EquipmentSlot.CHEST, process.apply(maid.getItemBySlot(EquipmentSlot.CHEST),0));
            case LEGS -> maid.setItemSlot(EquipmentSlot.LEGS, process.apply(maid.getItemBySlot(EquipmentSlot.LEGS),0));
            case FEET -> maid.setItemSlot(EquipmentSlot.FEET, process.apply(maid.getItemBySlot(EquipmentSlot.FEET),0));
            case MAIN_HAND ->
                    maid.setItemSlot(EquipmentSlot.MAINHAND, process.apply(maid.getItemBySlot(EquipmentSlot.MAINHAND),0));
            case OFF_HAND ->
                    maid.setItemSlot(EquipmentSlot.OFFHAND, process.apply(maid.getItemBySlot(EquipmentSlot.OFFHAND),0));
            case BAUBLE -> {
                BaubleItemHandler bauble = maid.getMaidBauble();
                return resetSlotItemWithProcessAndCheckIfAnyChanged(process, bauble, startIndex);
            }
            case FLOWER -> {
                RangedWrapper inv = maid.getAvailableBackpackInv();
                if (inv.getSlots() > 5)
                    inv.setStackInSlot(5, process.apply(inv.getStackInSlot(5)));
            }
            case ETA -> {
                RangedWrapper inv = maid.getAvailableBackpackInv();
                CombinedInvWrapper noLast = new CombinedInvWrapper(
                        new RangedWrapper(inv, 0, 5),
                        new RangedWrapper(inv, 6, inv.getSlots())
                );
                return resetSlotItemWithProcessAndCheckIfAnyChanged(process, noLast, startIndex);
            }
        }
        return Optional.empty();
    }

    public void iterItemExceptSlotForMaid(EntityMaid maid, Function<ItemStack, ItemStack> process) {
        if (this == ALL) return;
        CombinedInvWrapper inv = maid.getAvailableInv(true);
        for (int i = 0; i < inv.getSlots(); i++) {
            if (this == MAIN_HAND && i == 0) continue;
            if (this == OFF_HAND && i == 1) continue;
            if (this == FLOWER && i == 7) continue;
            inv.setStackInSlot(i, process.apply(inv.getStackInSlot(i)));
        }
    }

    private Optional<Integer> resetSlotItemWithProcessAndCheckIfAnyChanged(BiFunction<ItemStack, Integer, ItemStack> process, IItemHandlerModifiable bauble, int startIndex) {
        for (int i = startIndex; i < bauble.getSlots(); i++) {
            int oCount = bauble.getStackInSlot(i).getCount();
            ItemStack t = process.apply(bauble.getStackInSlot(i),i);
            bauble.setStackInSlot(i, t);
            if (t.getCount() != oCount)
                return Optional.of(i);
        }
        return Optional.empty();
    }

    public ItemStack tryPlaceItemIn(ItemStack itemStack, EntityMaid maid) {
        return switch (this) {
            case ALL -> InvUtil.tryPlace(maid.getAvailableInv(true), itemStack);
            case HEAD -> placeArmorSlot(itemStack, maid, EquipmentSlot.HEAD);
            case CHEST -> placeArmorSlot(itemStack, maid, EquipmentSlot.CHEST);
            case LEGS -> placeArmorSlot(itemStack, maid, EquipmentSlot.LEGS);
            case FEET -> placeArmorSlot(itemStack, maid, EquipmentSlot.FEET);
            case MAIN_HAND -> placeArmorSlot(itemStack, maid, EquipmentSlot.MAINHAND);
            case OFF_HAND -> placeArmorSlot(itemStack, maid, EquipmentSlot.OFFHAND);
            case BAUBLE -> InvUtil.tryPlace(maid.getMaidBauble(), itemStack);
            case FLOWER -> {
                RangedWrapper backpackInv = maid.getAvailableBackpackInv();
                if (backpackInv.getSlots() > 5) {
                    ItemStack stackInSlot = backpackInv.getStackInSlot(5);
                    if (stackInSlot.isEmpty()) {
                        backpackInv.setStackInSlot(5, itemStack);
                        yield ItemStack.EMPTY;
                    }
                    if (ItemStackUtil.isSame(stackInSlot, itemStack, ItemStackUtil.MATCH_TYPE.MATCHING)) {
                        int finallyCount = Math.min(itemStack.getMaxStackSize(), itemStack.getCount() + stackInSlot.getCount());
                        backpackInv.setStackInSlot(5, itemStack.copyWithCount(finallyCount));
                        yield itemStack.copyWithCount(itemStack.getCount() - finallyCount);
                    }
                }
                yield itemStack;
            }
            case ETA -> {
                RangedWrapper inv = maid.getAvailableBackpackInv();
                CombinedInvWrapper noLast = new CombinedInvWrapper(
                        new RangedWrapper(inv, 0, 5),
                        new RangedWrapper(inv, 6, inv.getSlots())
                );
                yield InvUtil.tryPlace(noLast, itemStack);
            }
        };
    }

    private ItemStack placeArmorSlot(ItemStack itemStack, EntityMaid maid, EquipmentSlot slot) {
        if (maid.getItemBySlot(slot).isEmpty() || ItemStackUtil.isSame(maid.getItemBySlot(slot), itemStack, ItemStackUtil.MATCH_TYPE.MATCHING)) {
            int finallyCount = Math.min(itemStack.getMaxStackSize(), itemStack.getCount() + maid.getItemBySlot(slot).getCount());
            maid.setItemSlot(slot, itemStack.copyWithCount(finallyCount));
            return itemStack.copyWithCount(itemStack.getCount() - finallyCount);
        } else return itemStack;
    }
}