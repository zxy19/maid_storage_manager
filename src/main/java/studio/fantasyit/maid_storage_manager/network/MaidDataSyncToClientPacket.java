package studio.fantasyit.maid_storage_manager.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Objects;

public class MaidDataSyncToClientPacket {
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

    public MaidDataSyncToClientPacket(FriendlyByteBuf buffer) {
        CompoundTag tmp = Objects.requireNonNull(buffer.readNbt());
        type = Type.values()[tmp.getInt("type")];
        id = tmp.getInt("id");
        value = tmp.getCompound("value");
    }

    public void toBytes(FriendlyByteBuf buffer) {
        CompoundTag tmp = new CompoundTag();
        tmp.putInt("type", type.ordinal());
        tmp.putInt("id", id);
        tmp.put("value", value);
        buffer.writeNbt(tmp);
    }
}
