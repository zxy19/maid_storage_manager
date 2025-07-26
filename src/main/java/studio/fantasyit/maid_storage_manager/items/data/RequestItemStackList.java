package studio.fantasyit.maid_storage_manager.items.data;


import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;

import java.util.ArrayList;
import java.util.List;

public class RequestItemStackList {

    public static final Immutable EMPTY = new RequestItemStackList().toImmutable();

    public static class ListItem {
        public boolean done;
        public ItemStack item;
        public int requested;
        public int collected;
        public int stored;
        public List<ItemStack> missing;
        public String failAddition;


        public ListItem(
                ItemStack item,
                int requested,
                int collected,
                int stored,
                List<ItemStack> missing,
                String failAddition,
                boolean done
        ) {
            this.item = item;
            this.requested = requested;
            this.collected = collected;
            this.stored = stored;
            this.missing = new ArrayList<>(missing);
            this.failAddition = failAddition;
            this.done = done;
        }

        public ListItem() {
            this(
                    ItemStack.EMPTY,
                    0,
                    0,
                    0,
                    new ArrayList<>(),
                    "",
                    false
            );
        }

        public ItemStack getItem() {
            return item;
        }

        public int getRequested() {
            return requested;
        }

        public int getCollected() {
            return collected;
        }

        public int getStored() {
            return stored;
        }

        public List<ItemStack> getMissing() {
            return missing;
        }

        public String getFailAddition() {
            return failAddition;
        }

        private boolean isDone() {
            return done;
        }
    }

    public List<ListItem> list;
    public boolean matchTag;
    public boolean blackList;
    public boolean blacklistDone;
    public boolean stockMode;
    public boolean stockModeChecked;


    public RequestItemStackList() {
        list = new ArrayList<>();
        blackList = false;
        matchTag = false;
        stockMode = false;
        stockModeChecked = false;
        blacklistDone = false;
        for (int i = 0; i < 10; i++)
            list.add(new ListItem());
    }

    public RequestItemStackList(
            List<ListItem> list,
            boolean matchTag,
            boolean blackList,
            boolean stockMode,
            boolean stockModeChecked,
            boolean blacklistDone
    ) {
        this.list = new ArrayList<>(list);
        this.matchTag = matchTag;
        this.blackList = blackList;
        this.stockMode = stockMode;
        this.stockModeChecked = stockModeChecked;
        this.blacklistDone = blacklistDone;
    }

    public List<ListItem> getList() {
        return list;
    }

    public boolean isMatchTag() {
        return matchTag;
    }

    public boolean isBlackList() {
        return blackList;
    }

    public boolean isStockMode() {
        return stockMode;
    }

    public boolean isStockModeChecked() {
        return stockModeChecked;
    }

    public boolean isBlacklistDone() {
        return blacklistDone;
    }


    public record ImmutableItem(ItemStack item, int requested, int collected, int stored, boolean done,
                                List<ItemStack> missing, String failAddition) {
        public static Codec<ImmutableItem> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ItemStackUtil.OPTIONAL_CODEC_UNLIMITED.fieldOf("item").forGetter(ImmutableItem::item),
                Codec.INT.fieldOf("requested").forGetter(ImmutableItem::requested),
                Codec.INT.fieldOf("collected").forGetter(ImmutableItem::collected),
                Codec.INT.fieldOf("stored").forGetter(ImmutableItem::stored),
                Codec.BOOL.fieldOf("done").forGetter(ImmutableItem::done),
                ItemStackUtil.OPTIONAL_CODEC_UNLIMITED.listOf().fieldOf("missing").forGetter(ImmutableItem::missing),
                Codec.STRING.fieldOf("failAddition").forGetter(ImmutableItem::failAddition)
        ).apply(instance, ImmutableItem::new));
        public static StreamCodec<RegistryFriendlyByteBuf, ImmutableItem> STREAM_CODEC = StreamCodec.of(
                (t, v) -> {
                    t.writeNbt(ItemStackUtil.saveStack(t.registryAccess(), v.item()));
                    t.writeInt(v.requested());
                    t.writeInt(v.collected());
                    t.writeInt(v.stored());
                    t.writeBoolean(v.done());
                    t.writeCollection(
                            v.missing(),
                            (t1, v1) -> t1.writeNbt(ItemStackUtil.saveStack(t.registryAccess(), v1))
                    );
                    t.writeUtf(v.failAddition);
                },
                (t) -> new ImmutableItem(
                        ItemStackUtil.parseStack(t.registryAccess(), t.readNbt()),
                        t.readInt(),
                        t.readInt(),
                        t.readInt(),
                        t.readBoolean(),
                        t.readCollection(
                                ArrayList::new,
                                RegistryFriendlyByteBuf::readNbt
                        ).stream().map(d -> ItemStackUtil.parseStack(t.registryAccess(), d)).toList(),
                        t.readUtf()
                )
        );

        public ListItem toMutable() {
            return new ListItem(item, requested, collected, stored, missing, failAddition, done);
        }
    }

    public record Immutable(
            List<ImmutableItem> list,
            boolean matchTag,
            boolean blackList,
            boolean stockMode,
            boolean stockModeChecked,
            boolean blacklistDone
    ) {
        public static Codec<Immutable> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                        ImmutableItem.CODEC.listOf().fieldOf("list").forGetter(Immutable::list),
                        Codec.BOOL.fieldOf("matchTag").forGetter(Immutable::matchTag),
                        Codec.BOOL.fieldOf("blackList").forGetter(Immutable::blackList),
                        Codec.BOOL.fieldOf("stockMode").forGetter(Immutable::stockMode),
                        Codec.BOOL.fieldOf("stockModeChecked").forGetter(Immutable::stockModeChecked),
                        Codec.BOOL.fieldOf("blacklistDone").forGetter(Immutable::blacklistDone)
                ).apply(instance, Immutable::new)
        );
        public static StreamCodec<RegistryFriendlyByteBuf, Immutable> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.collection(
                        ArrayList::new,
                        ImmutableItem.STREAM_CODEC
                ),
                Immutable::list,
                ByteBufCodecs.BOOL,
                Immutable::matchTag,
                ByteBufCodecs.BOOL,
                Immutable::blackList,
                ByteBufCodecs.BOOL,
                Immutable::stockMode,
                ByteBufCodecs.BOOL,
                Immutable::stockModeChecked,
                ByteBufCodecs.BOOL,
                Immutable::blacklistDone,
                Immutable::new
        );

        public RequestItemStackList toMutable() {
            return new RequestItemStackList(
                    list.stream().map(ImmutableItem::toMutable).toList(),
                    matchTag,
                    blackList,
                    stockMode,
                    stockModeChecked,
                    blacklistDone
            );
        }
    }

    public Immutable toImmutable() {
        return new Immutable(
                list.stream().map(item -> new ImmutableItem(
                        item.item,
                        item.requested,
                        item.collected,
                        item.stored,
                        item.done,
                        item.missing,
                        item.failAddition
                )).toList(),
                matchTag,
                blackList,
                stockMode,
                stockModeChecked,
                blacklistDone
        );
    }
}