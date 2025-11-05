package studio.fantasyit.maid_storage_manager.communicate.data;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
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
    public static final Codec<ConfigurableCommunicateData> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(
                    Codec.list(Item.CODEC)
                            .fieldOf("items")
                            .forGetter(t -> t.items)
            ).apply(instance, ConfigurableCommunicateData::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, ConfigurableCommunicateData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(ArrayList::new, Item.STREAM_CODEC),
            (ConfigurableCommunicateData t) -> t.items,
            ConfigurableCommunicateData::new
    );

    public static class Item {
        public static final Codec<Item> CODEC = RecordCodecBuilder.create(instance -> instance
                .group(
                        Codec.list(ItemStackUtil.OPTIONAL_CODEC_UNLIMITED)
                                .fieldOf("requires")
                                .forGetter(t -> t.requires),
                        Codec.BOOL
                                .fieldOf("whiteMode")
                                .forGetter(t -> t.whiteMode),
                        Codec.STRING
                                .fieldOf("match")
                                .xmap(ItemStackUtil.MATCH_TYPE::valueOf, ItemStackUtil.MATCH_TYPE::name)
                                .forGetter(t -> t.match),
                        Codec.STRING
                                .fieldOf("slot")
                                .xmap(SlotType::valueOf, SlotType::name)
                                .forGetter(t -> t.slot),
                        Codec.INT
                                .fieldOf("max")
                                .forGetter(t -> t.max),
                        Codec.INT
                                .fieldOf("min")
                                .forGetter(t -> t.min),
                        Codec.INT
                                .fieldOf("thresholdCount")
                                .forGetter(t -> t.thresholdCount)
                ).apply(instance, Item::new)
        );
        public static final StreamCodec<RegistryFriendlyByteBuf, Item> STREAM_CODEC = StreamCodec.of(
                (t, o) -> {
                    t.writeCollection(o.requires, (StreamCodec) ItemStackUtil.OPTIONAL_STREAM_CODEC);
                    t.writeBoolean(o.whiteMode);
                    t.writeUtf(o.match.name());
                    t.writeUtf(o.slot.name());
                    t.writeInt(o.max);
                    t.writeInt(o.min);
                    t.writeInt(o.thresholdCount);
                },
                (t) -> new Item(
                        (ArrayList) t.readCollection(ArrayList::new, (StreamCodec) ItemStackUtil.OPTIONAL_STREAM_CODEC),
                        t.readBoolean(),
                        ItemStackUtil.MATCH_TYPE.valueOf(t.readUtf()),
                        SlotType.valueOf(t.readUtf()),
                        t.readInt(),
                        t.readInt(),
                        t.readInt()
                )
        );
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

        public CompoundTag toNbt(HolderLookup.Provider p) {
            CompoundTag tag = new CompoundTag();
            ListTag requireTags = new ListTag();
            for (ItemStack req : requires) {
                requireTags.add(ItemStackUtil.saveStack(p,req));
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

        public static Item fromNbt(CompoundTag tag, HolderLookup.Provider p) {
            List<ItemStack> list = new ArrayList<>();
            ListTag requires = tag.getList("requires", 10);
            for (int i = 0; i < requires.size(); i++) {
                list.add(ItemStackUtil.parseStack(p,requires.getCompound(i)));
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

        public Item copy() {
            return new Item(
                    requires.stream().map(ItemStack::copy).toList(),
                    whiteMode,
                    match,
                    slot,
                    max,
                    min,
                    thresholdCount
            );
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Item item) {
                return item.requires.equals(requires) &&
                        item.whiteMode == whiteMode &&
                        item.match == match &&
                        item.slot == slot &&
                        item.max == max &&
                        item.min == min &&
                        item.thresholdCount == thresholdCount;
            }
            return false;
        }

        @Override
        public int hashCode() {
            int hash = 0;
            for (ItemStack itemStack : requires) {
                hash = hash * 17 + itemStack.hashCode();
            }
            return hash * 31 + (whiteMode ? 1 : 0) * 1007 + match.hashCode() * 1001 + slot.hashCode() * 1003 + max * 997 + min * 37 + thresholdCount * 17;
        }
    }

    public List<Item> items;

    public ConfigurableCommunicateData(List<Item> items) {
        this.items = items;
    }


    public CompoundTag toNbt(HolderLookup.Provider p) {
        CompoundTag tag = new CompoundTag();
        ListTag itemsTag = new ListTag();
        for (Item item : items) {
            itemsTag.add(item.toNbt(p));
        }
        tag.put("items", itemsTag);
        return tag;
    }

    public static ConfigurableCommunicateData fromNbt(CompoundTag tag, HolderLookup.Provider p) {
        List<Item> list = new ArrayList<>();
        ListTag itemsTag = tag.getList("items", 10);
        for (int i = 0; i < itemsTag.size(); i++) {
            list.add(Item.fromNbt(itemsTag.getCompound(i), p));
        }
        return new ConfigurableCommunicateData(list);
    }

    public ConfigurableCommunicateData copy() {
        return new ConfigurableCommunicateData(items.stream().map(Item::copy).toList());
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

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ConfigurableCommunicateData ccd) {
            return ccd.items.equals(this.items);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return items.hashCode();
    }
}
