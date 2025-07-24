package studio.fantasyit.maid_storage_manager.items.data;


import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ItemStackList {
    public static Codec<ItemStackList> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    ListItem.CODEC.listOf().fieldOf("list").forGetter(ItemStackList::getList)
            ).apply(instance, ItemStackList::new)
    );
    public static StreamCodec<RegistryFriendlyByteBuf, ItemStackList> STREAM_CODEC = StreamCodec.of(
            ItemStackList::encodeNetwork,
            ItemStackList::decodeNetwork
    );

    private static ItemStackList decodeNetwork(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
        ArrayList<ListItem> listItems = registryFriendlyByteBuf.readCollection(ArrayList::new, buffer -> new ListItem(
                ItemStack.parseOptional(registryFriendlyByteBuf.registryAccess(), buffer.readNbt()),
                buffer.readInt()
        ));
        return new ItemStackList(listItems);
    }

    private static void encodeNetwork(RegistryFriendlyByteBuf registryFriendlyByteBuf, ItemStackList itemStackList) {
        registryFriendlyByteBuf.writeCollection(itemStackList.list, (buffer, listItem) -> {
            buffer.writeNbt(listItem.item.save(registryFriendlyByteBuf.registryAccess()));
            buffer.writeInt(listItem.count);
        });
    }

    public static class ListItem {
        public static Codec<ListItem> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ItemStack.CODEC.fieldOf("item").forGetter(ListItem::getItem),
                Codec.INT.fieldOf("count").forGetter(ListItem::getCount)
        ).apply(instance, ListItem::new));
        public ItemStack item;
        public int count;

        public ListItem(ItemStack item, int count) {
            this.item = item;
            this.count = count;
        }

        public ItemStack getItem() {
            return item;
        }

        public int getCount() {
            return count;
        }
    }

    public List<ListItem> list;

    public ItemStackList() {
        list = new ArrayList<>();
    }

    public ItemStackList(List<ListItem> list) {
        this.list = new ArrayList<>(list);
    }

    public List<ListItem> getList() {
        return list;
    }

}