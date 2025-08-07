package studio.fantasyit.maid_storage_manager.network;

import net.minecraft.network.FriendlyByteBuf;
import studio.fantasyit.maid_storage_manager.data.BoxTip;
import studio.fantasyit.maid_storage_manager.data.InventoryListDataClient;

public class ShowCommonPacket {
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
