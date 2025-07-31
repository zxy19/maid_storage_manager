package studio.fantasyit.maid_storage_manager.network;

import net.minecraft.network.FriendlyByteBuf;
import studio.fantasyit.maid_storage_manager.craft.work.ProgressData;
import studio.fantasyit.maid_storage_manager.data.MaidProgressData;

import java.util.UUID;

public class ProgressPadUpdatePacket {
    private final ProgressData data;
    private final UUID uuid;

    public ProgressPadUpdatePacket(UUID uuid, ProgressData data) {
        this.uuid = uuid;
        this.data = data;
    }

    public ProgressPadUpdatePacket(FriendlyByteBuf buf) {
        this.uuid = buf.readUUID();
        this.data = ProgressData.fromNetwork(buf);
    }

    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeUUID(this.uuid);
        this.data.toNetwork(buf);
    }

    public static void handle(ProgressPadUpdatePacket packet) {
        MaidProgressData.setByMaid(packet.uuid, packet.data);
    }
}
