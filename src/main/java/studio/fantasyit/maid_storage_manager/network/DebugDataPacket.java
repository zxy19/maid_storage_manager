package studio.fantasyit.maid_storage_manager.network;

import net.minecraft.network.FriendlyByteBuf;

public class DebugDataPacket {
    public final String data;

    public DebugDataPacket(String data) {
        this.data = data;
    }

    public DebugDataPacket(FriendlyByteBuf buffer) {
        data = buffer.readUtf();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeUtf(data);
    }
}
