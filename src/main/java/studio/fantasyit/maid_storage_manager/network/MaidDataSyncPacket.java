package studio.fantasyit.maid_storage_manager.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;

public class MaidDataSyncPacket implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MaidDataSyncPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(
                    MaidStorageManager.MODID, "maid_data_sync"
            )
    );

    @Override
    public CustomPacketPayload.Type<MaidDataSyncPacket> type() {
        return TYPE;
    }

    public static StreamCodec<ByteBuf, MaidDataSyncPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            t -> t.type.name(),
            ByteBufCodecs.INT,
            t -> t.id,
            ByteBufCodecs.INT,
            t -> t.value,
            MaidDataSyncPacket::new
    );

    public enum Type {
        MemoryAssistant,
        CoWork,
        FastSort,
        AllowSeekWorkMeal, MemorizeCraftGuide, MaxParallel, SingleCrafting, NoPlaceSort
    }

    public final Type type;
    public final int id;
    public final int value;

    public MaidDataSyncPacket(Type type, int id, int value) {
        this.type = type;
        this.id = id;
        this.value = value;
    }


    public MaidDataSyncPacket(String type, int id, int value) {
        this.type = Type.valueOf(type);
        this.id = id;
        this.value = value;
    }

}
