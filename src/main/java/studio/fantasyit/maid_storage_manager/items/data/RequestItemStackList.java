package studio.fantasyit.maid_storage_manager.items.data;


import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class RequestItemStackList {
    public static Codec<RequestItemStackList> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    ListItem.CODEC.listOf().fieldOf("list").forGetter(RequestItemStackList::getList),
                    Codec.BOOL.fieldOf("matchTag").forGetter(RequestItemStackList::isMatchTag),
                    Codec.BOOL.fieldOf("blackList").forGetter(RequestItemStackList::isBlackList),
                    Codec.BOOL.fieldOf("stockMode").forGetter(RequestItemStackList::isStockMode),
                    Codec.BOOL.fieldOf("stockModeChecked").forGetter(RequestItemStackList::isStockModeChecked),
                    Codec.BOOL.fieldOf("blacklistDone").forGetter(RequestItemStackList::isBlacklistDone)
            ).apply(instance, RequestItemStackList::new)
    );
    public static StreamCodec<ByteBuf, RequestItemStackList> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    public static class ListItem {
        public static Codec<ListItem> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ItemStack.CODEC.fieldOf("item").forGetter(ListItem::getItem),
                Codec.INT.fieldOf("requested").forGetter(ListItem::getRequested),
                Codec.INT.fieldOf("collected").forGetter(ListItem::getCollected),
                Codec.INT.fieldOf("stored").forGetter(ListItem::getStored),
                ItemStack.CODEC.listOf().fieldOf("missing").forGetter(ListItem::getMissing),
                Codec.STRING.fieldOf("failAddition").forGetter(ListItem::getFailAddition),
                Codec.BOOL.fieldOf("done").forGetter(ListItem::isDone)
        ).apply(instance, ListItem::new));

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
}