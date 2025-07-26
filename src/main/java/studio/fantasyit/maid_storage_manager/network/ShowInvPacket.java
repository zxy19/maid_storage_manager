package studio.fantasyit.maid_storage_manager.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.data.InventoryItem;

public class ShowInvPacket implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ShowInvPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(
                    MaidStorageManager.MODID, "show_inv"
            )
    );

    @Override
    public CustomPacketPayload.Type<ShowInvPacket> type() {
        return TYPE;
    }

    public static StreamCodec<RegistryFriendlyByteBuf, ShowInvPacket> STREAM_CODEC = StreamCodec.composite(
            InventoryItem.STREAM_CODEC,
            t -> t.data,
            ByteBufCodecs.INT,
            t -> t.time,
            ShowInvPacket::new
    );

    InventoryItem data;
    int time;

    public ShowInvPacket(InventoryItem data, int time) {
        this.data = data;
        this.time = time;
    }

}
