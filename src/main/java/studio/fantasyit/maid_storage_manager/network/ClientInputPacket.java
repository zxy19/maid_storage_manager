package studio.fantasyit.maid_storage_manager.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;

public class ClientInputPacket implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ClientInputPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(
                    MaidStorageManager.MODID, "ClientInputPacket"
            )
    );

    @Override
    public CustomPacketPayload.Type<ClientInputPacket> type() {
        return TYPE;
    }

    public static StreamCodec<ByteBuf, ClientInputPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            t -> t.type.name(),
            ByteBufCodecs.INT,
            t -> t.value,
            ClientInputPacket::new
    );

    public enum Type {
        ALT_SCROLL, SCROLL
    }

    public final Type type;
    public final int value;

    public ClientInputPacket(Type type, int data) {
        this.type = type;
        this.value = data;
    }

    public ClientInputPacket(String type, int data) {
        this.type = Type.valueOf(type);
        this.value = data;
    }
}
