package studio.fantasyit.maid_storage_manager.network;

import net.minecraft.network.FriendlyByteBuf;
import studio.fantasyit.maid_storage_manager.craft.work.ProgressData;
import studio.fantasyit.maid_storage_manager.data.MaidProgressData;

public class ProgressPadUpdatePacket {
    private final ProgressData data;
    private final ProgressData.ProgressMeta meta;

    public ProgressPadUpdatePacket(ProgressData.ProgressMeta meta, ProgressData data) {
        this.meta = meta;
        this.data = data;
    }

    public ProgressPadUpdatePacket(FriendlyByteBuf buf) {
        this.meta = ProgressData.ProgressMeta.fromNetwork(buf);
        this.data = ProgressData.fromNetwork(buf);
    }

    public void toNetwork(FriendlyByteBuf buf) {
        meta.toNetwork(buf);
        data.toNetwork(buf);
    }

    public static void handle(ProgressPadUpdatePacket packet) {
        MaidProgressData.setByMaid(packet.meta, packet.data);
    }
}
