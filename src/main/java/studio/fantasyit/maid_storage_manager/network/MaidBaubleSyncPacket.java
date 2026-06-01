package studio.fantasyit.maid_storage_manager.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;

import java.util.List;

public class MaidBaubleSyncPacket implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MaidBaubleSyncPacket> TYPE = new CustomPacketPayload.Type<>(
            Identifier.fromNamespaceAndPath(MaidStorageManager.MODID, "maid_bauble_sync")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, MaidBaubleSyncPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            t -> t.maidId,
            ItemStack.OPTIONAL_LIST_STREAM_CODEC,
            t -> t.baubles,
            MaidBaubleSyncPacket::new
    );

    public final int maidId;
    public final List<ItemStack> baubles;

    public MaidBaubleSyncPacket(int maidId, List<ItemStack> baubles) {
        this.maidId = maidId;
        this.baubles = baubles;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
