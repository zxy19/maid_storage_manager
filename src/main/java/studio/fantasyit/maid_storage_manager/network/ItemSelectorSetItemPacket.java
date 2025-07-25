package studio.fantasyit.maid_storage_manager.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;

import java.util.ArrayList;
import java.util.List;

public class ItemSelectorSetItemPacket implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ItemSelectorSetItemPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(
                    MaidStorageManager.MODID, "ItemSelectorSetItemPacket"
            )
    );

    @Override
    public CustomPacketPayload.Type<ItemSelectorSetItemPacket> type() {
        return TYPE;
    }

    public static StreamCodec<RegistryFriendlyByteBuf, ItemSelectorSetItemPacket> STREAM_CODEC = StreamCodec.of(
            ItemSelectorSetItemPacket::toBytes,
            ItemSelectorSetItemPacket::fromBytes
    );

    List<Pair<Integer, ItemStack>> items;

    public ItemSelectorSetItemPacket(List<Pair<Integer, ItemStack>> items) {
        this.items = items;
    }

    public static ItemSelectorSetItemPacket fromBytes(RegistryFriendlyByteBuf buffer) {
        ArrayList<Pair<Integer, ItemStack>> t = new ArrayList<>();
        ListTag items1 = buffer.readNbt().getList("items", ListTag.TAG_COMPOUND);
        for (int i = 0; i < items1.size(); i++) {
            CompoundTag tmp = items1.getCompound(i);
            t.add(Pair.of(tmp.getInt("index"), ItemStack.parseOptional(buffer.registryAccess(), tmp.getCompound("item"))));
        }
        return new ItemSelectorSetItemPacket(t);
    }

    public static void toBytes(RegistryFriendlyByteBuf buffer, ItemSelectorSetItemPacket packet) {
        ListTag items1 = new ListTag();
        for (Pair<Integer, ItemStack> item : packet.items) {
            CompoundTag tmp = new CompoundTag();
            tmp.putInt("index", item.getLeft());
            tmp.put("item", item.getRight().save(buffer.registryAccess(), new CompoundTag()));
            items1.add(tmp);
        }
        CompoundTag tag = new CompoundTag();
        tag.put("items", items1);
        buffer.writeNbt(tag);
    }
}
