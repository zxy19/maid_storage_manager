package studio.fantasyit.maid_storage_manager.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Objects;

public class DebugDataPacket {
    public final CompoundTag data;
    public final String type;

    public DebugDataPacket(String type, CompoundTag data) {
        this.data = data;
        this.type = type;
    }

    public DebugDataPacket(FriendlyByteBuf buffer) {
        CompoundTag tmp = Objects.requireNonNull(buffer.readNbt());
        this.data = tmp.getCompound("data");
        this.type = tmp.getString("type");
    }

    public void toBytes(FriendlyByteBuf buffer) {
        CompoundTag tmp = new CompoundTag();
        tmp.putString("type", type);
        tmp.put("data", data);
        buffer.writeNbt(tmp);
    }
}
