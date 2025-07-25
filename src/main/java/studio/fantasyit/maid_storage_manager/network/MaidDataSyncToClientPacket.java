package studio.fantasyit.maid_storage_manager.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;

public class MaidDataSyncToClientPacket implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MaidDataSyncToClientPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(
                    MaidStorageManager.MODID, "MaidDataSyncToClientPacket"
            )
    );

    @Override
    public CustomPacketPayload.Type<MaidDataSyncToClientPacket> type() {
        return TYPE;
    }

    public static StreamCodec<RegistryFriendlyByteBuf, MaidDataSyncToClientPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            t -> t.type.name(),
            ByteBufCodecs.INT,
            t -> t.id,
            ByteBufCodecs.COMPOUND_TAG,
            t -> t.value,
            MaidDataSyncToClientPacket::new
    );

    public enum Type {
        WORKING,
        BAUBLE
    }

    public final Type type;
    public final int id;
    public final CompoundTag value;

    public MaidDataSyncToClientPacket(Type type, int id, CompoundTag value) {
        this.type = type;
        this.id = id;
        this.value = value;
    }

    public MaidDataSyncToClientPacket(String type, int id, CompoundTag value) {
        this.type = Type.valueOf(type);
        this.id = id;
        this.value = value;
    }
}
