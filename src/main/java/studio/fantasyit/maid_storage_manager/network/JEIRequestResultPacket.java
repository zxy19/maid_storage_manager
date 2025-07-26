package studio.fantasyit.maid_storage_manager.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;

public class JEIRequestResultPacket implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<JEIRequestResultPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(
                    MaidStorageManager.MODID, "jei_request_result"
            )
    );

    @Override
    public CustomPacketPayload.Type<JEIRequestResultPacket> type() {
        return TYPE;
    }

    public static StreamCodec<RegistryFriendlyByteBuf, JEIRequestResultPacket> STREAM_CODEC = StreamCodec.composite(
            ComponentSerialization.STREAM_CODEC,
            t -> t.result,
            JEIRequestResultPacket::new
    );

    Component result;

    public JEIRequestResultPacket(Component result) {
        this.result = result;
    }
}
