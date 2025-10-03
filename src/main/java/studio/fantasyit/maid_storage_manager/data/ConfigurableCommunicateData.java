package studio.fantasyit.maid_storage_manager.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;

import java.util.ArrayList;
import java.util.List;

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
    }

    public List<Item> items;
    public boolean manualMode;

    public ConfigurableCommunicateData(List<Item> items, boolean manualMode) {
        this.items = items;
        this.manualMode = manualMode;
    }

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        ListTag itemsTag = new ListTag();
        for (Item item : items) {
            itemsTag.add(item.toNbt());
        }
        tag.put("items", itemsTag);
        tag.putBoolean("manualMode", manualMode);
        return tag;
    }

    public static ConfigurableCommunicateData fromNbt(CompoundTag tag) {
        List<Item> list = new ArrayList<>();
        ListTag itemsTag = tag.getList("items", 10);
        for (int i = 0; i < itemsTag.size(); i++) {
            list.add(Item.fromNbt(itemsTag.getCompound(i)));
        }
        return new ConfigurableCommunicateData(list, tag.getBoolean("manualMode"));
    }
}
