package studio.fantasyit.maid_storage_manager.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Objects;

public class ClientInputPacket {
    public enum Type {
        ALT_SCROLL, SCROLL
    }
    public final Type type;
    public final int value;
    public ClientInputPacket(Type type,int data) {
        this.type = type;
        this.value = data;
    }

    public ClientInputPacket(FriendlyByteBuf buffer) {
        CompoundTag tmp = Objects.requireNonNull(buffer.readNbt());
        type = Type.values()[tmp.getInt("type")];
        value = tmp.getInt("value");
    }

    public void toBytes(FriendlyByteBuf buffer) {
        CompoundTag tmp = new CompoundTag();
        tmp.putInt("type", type.ordinal());
        tmp.putInt("value", value);
        buffer.writeNbt(tmp);
    }
}
