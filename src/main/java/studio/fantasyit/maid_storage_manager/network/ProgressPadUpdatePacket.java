package studio.fantasyit.maid_storage_manager.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.craft.work.ProgressData;
import studio.fantasyit.maid_storage_manager.data.MaidProgressData;

import java.util.UUID;

public class ProgressPadUpdatePacket implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ProgressPadUpdatePacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(
                    MaidStorageManager.MODID, "progress_pad_update"
            )
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ProgressPadUpdatePacket> STREAM_CODEC = StreamCodec.of(
            (t, c) -> c.toNetwork(t),
            ProgressPadUpdatePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private final ProgressData data;
    private final UUID uuid;

    public ProgressPadUpdatePacket(UUID uuid, ProgressData data) {
        this.uuid = uuid;
        this.data = data;
    }

    public ProgressPadUpdatePacket(RegistryFriendlyByteBuf buf) {
        this.uuid = buf.readUUID();
        this.data = ProgressData.fromNetwork(buf);
    }

    public void toNetwork(RegistryFriendlyByteBuf buf) {
        buf.writeUUID(this.uuid);
        this.data.toNetwork(buf);
    }

    public static void handle(ProgressPadUpdatePacket packet) {
        MaidProgressData.setByMaid(packet.uuid, packet.data);
    }

}
