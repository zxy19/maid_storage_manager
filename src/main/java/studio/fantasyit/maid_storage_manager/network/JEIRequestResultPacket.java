package studio.fantasyit.maid_storage_manager.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

public class JEIRequestResultPacket {
    Component result;
    public JEIRequestResultPacket(Component result) {
        this.result = result;
    }

    public JEIRequestResultPacket(FriendlyByteBuf buffer) {
        result = buffer.readComponent();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeComponent(result);
    }
}
