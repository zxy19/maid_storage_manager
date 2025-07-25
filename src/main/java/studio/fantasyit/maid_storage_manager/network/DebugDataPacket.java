package studio.fantasyit.maid_storage_manager.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;

public class DebugDataPacket implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<DebugDataPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(
                    MaidStorageManager.MODID, "DebugDataPacket"
            )
    );

    @Override
    public CustomPacketPayload.Type<DebugDataPacket> type() {
        return TYPE;
    }

    public static StreamCodec<ByteBuf, DebugDataPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            t -> t.data,
            DebugDataPacket::new
    );

    public final String data;

    public DebugDataPacket(String data) {
        this.data = data;
    }
}
