package studio.fantasyit.maid_storage_manager.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.data.BoxTip;
import studio.fantasyit.maid_storage_manager.data.InventoryListDataClient;

public class ShowCommonPacket implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ShowCommonPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(
                    MaidStorageManager.MODID, "show_common"
            )
    );

    @Override
    public CustomPacketPayload.Type<ShowCommonPacket> type() {
        return TYPE;
    }

    public static StreamCodec<RegistryFriendlyByteBuf, ShowCommonPacket> STREAM_CODEC = StreamCodec.of(
            (t, c) -> c.toBytes(t),
            ShowCommonPacket::new
    );
    BoxTip data;

    public ShowCommonPacket(BoxTip data) {
        this.data = data;
    }

    public ShowCommonPacket(FriendlyByteBuf buffer) {
        data = BoxTip.fromNetwork(buffer);
    }

    public static void handle(ShowCommonPacket p) {
        InventoryListDataClient.addCommonTip(p.data);
    }

    public void toBytes(FriendlyByteBuf buffer) {
        data.toNetwork(buffer);
    }
}
