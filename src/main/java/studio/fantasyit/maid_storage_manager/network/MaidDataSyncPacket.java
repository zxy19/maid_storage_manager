package studio.fantasyit.maid_storage_manager.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Objects;

public class MaidDataSyncPacket {
    public enum Type {
        MemoryAssistant,
        CoWork, NoPlaceSort
    }
    public final Type type;
    public final int id;
    public final int value;
    public MaidDataSyncPacket(Type type, int id,int value) {
        this.type = type;
        this.id = id;
        this.value = value;
    }

    public MaidDataSyncPacket(FriendlyByteBuf buffer) {
        CompoundTag tmp = Objects.requireNonNull(buffer.readNbt());
        type = Type.values()[tmp.getInt("type")];
        id = tmp.getInt("id");
        value = tmp.getInt("value");
    }

    public void toBytes(FriendlyByteBuf buffer) {
        CompoundTag tmp = new CompoundTag();
        tmp.putInt("type", type.ordinal());
        tmp.putInt("id", id);
        tmp.putInt("value", value);
        buffer.writeNbt(tmp);
    }
}
