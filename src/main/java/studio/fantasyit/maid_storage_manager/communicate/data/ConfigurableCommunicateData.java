package studio.fantasyit.maid_storage_manager.communicate.data;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.api.communicate.wish.IActionWish;
import studio.fantasyit.maid_storage_manager.communicate.wish.PlaceItemWishWithLimitation;
import studio.fantasyit.maid_storage_manager.communicate.wish.RequestItemWish;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;

import java.util.ArrayList;
import java.util.List;

public class ConfigurableCommunicateData {
    public static class Item {
        public List<ItemStack> requires;
        public boolean whiteMode;
        public ItemStackUtil.MATCH_TYPE match;
        public SlotType slot;
        public int max;
        public int min;
        public int thresholdCount;

        public Item(List<ItemStack> requires, boolean whiteMode, ItemStackUtil.MATCH_TYPE match, SlotType slot, int max, int min, int thresholdCount) {
            this.requires = requires;
            this.whiteMode = whiteMode;
            this.match = match;
            this.slot = slot;
            this.max = max;
            this.min = min;
            this.thresholdCount = thresholdCount;
        }

        public CompoundTag toNbt() {
            CompoundTag tag = new CompoundTag();
            ListTag requireTags = new ListTag();
            for (ItemStack req : requires) {
                requireTags.add(ItemStackUtil.saveStack(req));
            }
            tag.put("requires", requireTags);
            tag.putBoolean("whiteMode", whiteMode);
            tag.putString("match", match.name());
            tag.putString("slot", slot.name());
            tag.putInt("max", max);
            tag.putInt("min", min);
            tag.putInt("thresholdCount", thresholdCount);
            return tag;
        }

        public static Item fromNbt(CompoundTag tag) {
            List<ItemStack> list = new ArrayList<>();
            ListTag requires = tag.getList("requires", 10);
            for (int i = 0; i < requires.size(); i++) {
                list.add(ItemStackUtil.parseStack(requires.getCompound(i)));
            }
            return new Item(
                    list,
                    tag.getBoolean("whiteMode"),
                    ItemStackUtil.MATCH_TYPE.valueOf(tag.getString("match")),
                    SlotType.valueOf(tag.getString("slot")),
                    tag.getInt("max"),
                    tag.getInt("min"),
                    tag.getInt("thresholdCount")
            );
        }

        public static Item empty() {
            return new Item(
                    new ArrayList<>(),
                    false,
                    ItemStackUtil.MATCH_TYPE.AUTO,
                    SlotType.ALL,
                    -1,
                    -1,
                    -1
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
                //count表示物品总共存在的数量。在此数量上统计应该放回的物品
                List<MutableInt> count = item.requires.stream().map(itemStack -> new MutableInt(0)).toList();
                for (ItemStack itemStack : hasItem) {
                    if (itemStack.isEmpty()) continue;
                    boolean find = false;
                    for (int i = 0; i < item.requires.size(); i++) {
                        if (ItemStackUtil.isSame(itemStack, item.requires.get(i), item.match)) {
                            find = true;
                            count.get(i).add(itemStack.getCount());
                        }
                    }
                    if (!find) {
                        ItemStackUtil.addToList(toTakeItem, itemStack, item.match);
                    }
                }
                for (int i = 0; i < item.requires.size(); i++) {
                    if (item.requires.get(i).isEmpty()) continue;
                    if (item.min == -1 && item.max == -1)
                        continue;

                    if (item.max != -1 && count.get(i).getValue() > item.max) {
                        int targetCount = item.max;
                        if (item.min != -1)
                            targetCount = (item.max + item.min) / 2;
                        ItemStackUtil.addToList(toTakeItem, item.requires.get(i).copyWithCount(count.get(i).getValue() - targetCount), item.match);
                    }
                    if (item.min != -1 && count.get(i).getValue() < item.min) {
                        int targetCount = item.min;
                        if (item.max != -1)
                            targetCount = (item.max + item.min) / 2;
                        ItemStackUtil.addToList(toRequestItem, item.requires.get(i).copyWithCount(targetCount - count.get(i).getValue()), item.match);
                    }
                }
            } else {
                //先找出所有应该放回的物品
                List<ItemStack> exists = new ArrayList<>();
                for (ItemStack itemStack : hasItem) {
                    if (itemStack.isEmpty()) continue;
                    boolean find = false;
                    for (ItemStack require : item.requires) {
                        if (require.isEmpty()) continue;
                        if (ItemStackUtil.isSame(itemStack, require, item.match)) {
                            ItemStackUtil.addToList(toTakeItem, itemStack.copyWithCount(itemStack.getCount()), item.match);
                            find = true;
                            break;
                        }
                    }
                    //对于不匹配黑名单的物品，匹配最大数量限制
                    if (!find) {
                        ItemStackUtil.addToList(exists, itemStack.copyWithCount(itemStack.getCount()), item.match);
                    }
                }
                if (item.max != -1) {
                    int targetCount = item.max;
                    if (item.min != -1)
                        targetCount = (item.max + item.min) / 2;
                    for (ItemStack itemStack : exists) {
                        if (itemStack.getCount() > item.max) {
                            ItemStackUtil.addToList(toTakeItem, itemStack.copyWithCount(itemStack.getCount() - targetCount), item.match);
                        }
                    }
                }
            }
            if (!toTakeItem.isEmpty())
                list.add(new PlaceItemWishWithLimitation(toTakeItem.stream().map(t -> new Pair<>(t, item.thresholdCount)).toList(), item.slot, item.match));
            if (!toRequestItem.isEmpty())
                list.add(new RequestItemWish(toRequestItem, item.match, item.slot));
        }
        return list;
    }

    public static ConfigurableCommunicateData toFixedLength(@Nullable ConfigurableCommunicateData defaultData) {
        if (defaultData == null)
            defaultData = new ConfigurableCommunicateData(List.of());
        List<Item> fixedLengthItems = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            if (i < defaultData.items.size())
                fixedLengthItems.add(defaultData.items.get(i));
            else
                fixedLengthItems.add(Item.empty());
        }
        for (Item item : fixedLengthItems) {
            if (!(item.requires instanceof ArrayList<ItemStack>)) {
                item.requires = new ArrayList<>(item.requires);
            }
            while (item.requires.size() < 8) {
                item.requires.add(ItemStack.EMPTY);
            }
        }
        return new ConfigurableCommunicateData(fixedLengthItems);
    }
}
