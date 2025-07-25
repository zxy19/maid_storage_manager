package studio.fantasyit.maid_storage_manager.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;

import java.util.ArrayList;
import java.util.List;

public class RenderEntityPacket implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RenderEntityPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(
                    MaidStorageManager.MODID, "RenderEntityPacket"
            )
    );

    @Override
    public CustomPacketPayload.Type<RenderEntityPacket> type() {
        return TYPE;
    }

    public static StreamCodec<RegistryFriendlyByteBuf, RenderEntityPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(
                    ArrayList::new,
                    ByteBufCodecs.INT
            ),
            t -> t.entityIds,
            RenderEntityPacket::new
    );

    public List<Integer> entityIds;

    public RenderEntityPacket(List<Integer> entityIds) {
        this.entityIds = entityIds;
    }
}
