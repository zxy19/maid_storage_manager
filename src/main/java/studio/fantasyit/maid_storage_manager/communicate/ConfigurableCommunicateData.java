package studio.fantasyit.maid_storage_manager.communicate;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.mutable.MutableInt;
import studio.fantasyit.maid_storage_manager.api.communicate.wish.IActionWish;
import studio.fantasyit.maid_storage_manager.api.communicate.wish.PlaceItemWish;
import studio.fantasyit.maid_storage_manager.api.communicate.wish.RequestItemWish;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;

import java.util.ArrayList;
import java.util.List;

public class ConfigurableCommunicateData {
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

    public List<IActionWish> buildWish(EntityMaid maid) {
        List<IActionWish> list = new ArrayList<>();
        for (Item item : items) {
            List<ItemStack> toTakeItem = new ArrayList<>();
            List<ItemStack> toRequestItem = new ArrayList<>();
            List<ItemStack> hasItem = item.slot.getItemStacks(maid);
            if (item.whiteMode) {
                List<MutableInt> count = item.itemStacks.stream().map(itemStack -> new MutableInt(itemStack.getCount())).toList();
                for (ItemStack itemStack : hasItem) {
                    boolean find = false;
                    for (int i = 0; i < item.itemStacks.size(); i++) {
                        if (ItemStackUtil.isSame(itemStack, item.itemStacks.get(i), item.match)) {
                            find = true;
                            int shouldTake = Math.max(itemStack.getCount() - count.get(i).getValue(), 0);
                            count.get(i).subtract(itemStack.getCount() - shouldTake);
                            if (shouldTake != 0)
                                ItemStackUtil.addToList(toTakeItem, itemStack.copyWithCount(shouldTake), item.match);
                        }
                    }
                    if (!find) {
                        ItemStackUtil.addToList(toRequestItem, itemStack, item.match);
                    }
                }
                for (int i = 0; i < item.itemStacks.size(); i++) {
                    if (count.get(i).getValue() != 0) {
                        ItemStackUtil.addToList(toRequestItem, item.itemStacks.get(i).copyWithCount(count.get(i).getValue()), item.match);
                    }
                }
            } else {
                for (ItemStack itemStack : hasItem) {
                    for (ItemStack itemStack1 : item.itemStacks) {
                        if (ItemStackUtil.isSame(itemStack, itemStack1, item.match)) {
                            ItemStackUtil.addToList(toTakeItem, itemStack.copyWithCount(itemStack.getCount()), item.match);
                        }
                    }
                }
            }

            if (!toTakeItem.isEmpty())
                list.add(new PlaceItemWish(toTakeItem, true, true, item.slot, item.match));
            if (!toRequestItem.isEmpty())
                list.add(new RequestItemWish(toRequestItem, item.match, item.slot));
        }
        return list;
    }
}
