package studio.fantasyit.maid_storage_manager.communicate;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.inventory.handler.BaubleItemHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import net.minecraftforge.items.wrapper.RangedWrapper;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ConfigurableCommunicateData {
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
        BAUBLE
    }

    public record Item(
            List<ItemStack> itemStacks,
            boolean whiteMode,
            ItemStackUtil.MATCH_TYPE match,
            SlotType slot,
            List<Integer> thresholdCount
    ) {
        public CompoundTag toNbt() {
            CompoundTag tag = new CompoundTag();
            ListTag itemStacksTags = new ListTag();
            for (ItemStack itemStack : itemStacks) {
                itemStacksTags.add(ItemStackUtil.saveStack(itemStack));
            }
            tag.put("itemStacks", itemStacksTags);
            tag.putBoolean("whiteMode", whiteMode);
            tag.putString("match", match.name());
            tag.putString("slot", slot.name());
            ListTag thresholdCountTag = new ListTag();
            for (int i : thresholdCount) {
                thresholdCountTag.add(IntTag.valueOf(i));
            }
            tag.put("thresholdCount", thresholdCountTag);
            return tag;
        }

        public static Item fromNbt(CompoundTag tag) {
            List<ItemStack> list = new ArrayList<>();
            ListTag itemStacks1 = tag.getList("itemStacks", 10);
            for (int i = 0; i < itemStacks1.size(); i++) {
                list.add(ItemStackUtil.parseStack(itemStacks1.getCompound(i)));
            }
            List<Integer> thresholdCount = new ArrayList<>();
            ListTag thresholdCount1 = tag.getList("thresholdCount", Tag.TAG_INT);
            for (int i = 0; i < thresholdCount1.size(); i++) {
                thresholdCount.add(thresholdCount1.getInt(i));
            }
            return new Item(
                    list,
                    tag.getBoolean("whiteMode"),
                    ItemStackUtil.MATCH_TYPE.valueOf(tag.getString("match")),
                    SlotType.valueOf(tag.getString("slot")),
                    thresholdCount
            );
        }



        public List<ItemStack> getItemStacks(EntityMaid maid) {
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

        public boolean processSlotItemsAndGetIsFinished(EntityMaid maid, Function<ItemStack, ItemStack> process) {
            switch (slot) {
                case ALL -> {
                    CombinedInvWrapper availableInv = maid.getAvailableInv(false);
                    return resetSlotItemWithProcessAndCheckIfAnyChanged(process, availableInv);
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
                    return resetSlotItemWithProcessAndCheckIfAnyChanged(process, bauble);
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
                    return resetSlotItemWithProcessAndCheckIfAnyChanged(process, noLast);
                }
            }
            return true;
        }

        private boolean resetSlotItemWithProcessAndCheckIfAnyChanged(Function<ItemStack, ItemStack> process, IItemHandlerModifiable bauble) {
            for (int i = 0; i < bauble.getSlots(); i++) {
                int oCount = bauble.getStackInSlot(i).getCount();
                ItemStack t = process.apply(bauble.getStackInSlot(i));
                bauble.setStackInSlot(i, t);
                if (t.getCount() != oCount)
                    return true;
            }
            return false;
        }
    }

    public List<Item> items;

    public ConfigurableCommunicateData(List<Item> items) {
        this.items = items;
    }

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        ListTag itemsTag = new ListTag();
        for (Item item : items) {
            itemsTag.add(item.toNbt());
        }
        tag.put("items", itemsTag);
        return tag;
    }

    public static ConfigurableCommunicateData fromNbt(CompoundTag tag) {
        List<Item> list = new ArrayList<>();
        ListTag itemsTag = tag.getList("items", 10);
        for (int i = 0; i < itemsTag.size(); i++) {
            list.add(Item.fromNbt(itemsTag.getCompound(i)));
        }
        return new ConfigurableCommunicateData(list);
    }
}
