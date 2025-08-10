package studio.fantasyit.maid_storage_manager.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;

import java.util.ArrayList;
import java.util.List;

public class ItemSelectorSetItemPacket {
    List<Pair<Integer, ItemStack>> items;

    public ItemSelectorSetItemPacket(List<Pair<Integer, ItemStack>> items) {
        this.items = items;
    }

    public ItemSelectorSetItemPacket(FriendlyByteBuf buffer) {
        items = new ArrayList<>();
        ListTag items1 = buffer.readNbt().getList("items", ListTag.TAG_COMPOUND);
        for (int i = 0; i < items1.size(); i++) {
            CompoundTag tmp = items1.getCompound(i);
            items.add(Pair.of(tmp.getInt("index"), ItemStackUtil.parseStack(tmp.getCompound("item"))));
        }
    }

    public void toBytes(FriendlyByteBuf buffer) {
        ListTag items1 = new ListTag();
        for (Pair<Integer, ItemStack> item : items) {
            CompoundTag tmp = new CompoundTag();
            tmp.putInt("index", item.getLeft());
            tmp.put("item", ItemStackUtil.saveStack(item.getRight()));
            items1.add(tmp);
        }
        CompoundTag tag = new CompoundTag();
        tag.put("items", items1);
        buffer.writeNbt(tag);
    }
}
