package studio.fantasyit.maid_storage_manager.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;

public class IngredientRequestResultS2CPacket implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<IngredientRequestResultS2CPacket> TYPE = new CustomPacketPayload.Type<>(
            Identifier.fromNamespaceAndPath(
                    MaidStorageManager.MODID, "ingredient_request_result"
            )
    );

    @Override
    public CustomPacketPayload.Type<IngredientRequestResultS2CPacket> type() {
        return TYPE;
    }

    public static StreamCodec<RegistryFriendlyByteBuf, IngredientRequestResultS2CPacket> STREAM_CODEC = StreamCodec.composite(
            ComponentSerialization.STREAM_CODEC,
            t -> t.result,
            IngredientRequestResultS2CPacket::new
    );

    Component result;

    public IngredientRequestResultS2CPacket(Component result) {
        this.result = result;
    }
}
