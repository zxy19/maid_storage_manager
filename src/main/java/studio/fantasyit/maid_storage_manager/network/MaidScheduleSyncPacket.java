package studio.fantasyit.maid_storage_manager.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;

public class MaidScheduleSyncPacket implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MaidScheduleSyncPacket> TYPE = new CustomPacketPayload.Type<>(
            Identifier.fromNamespaceAndPath(MaidStorageManager.MODID, "maid_schedule_sync")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, MaidScheduleSyncPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            t -> t.maidId,
            ByteBufCodecs.INT,
            t -> t.scheduleOrdinal,
            MaidScheduleSyncPacket::new
    );

    public final int maidId;
    public final int scheduleOrdinal;

    public MaidScheduleSyncPacket(int maidId, int scheduleOrdinal) {
        this.maidId = maidId;
        this.scheduleOrdinal = scheduleOrdinal;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
