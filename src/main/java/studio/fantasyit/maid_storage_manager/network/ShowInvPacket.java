package studio.fantasyit.maid_storage_manager.network;

import net.minecraft.network.FriendlyByteBuf;
import studio.fantasyit.maid_storage_manager.data.InventoryItem;

public class ShowInvPacket {
    InventoryItem data;
    int time;

    public ShowInvPacket(InventoryItem data,int time) {
        this.data = data;
        this.time = time;
    }

    public ShowInvPacket(FriendlyByteBuf buffer) {
        data = InventoryItem.fromNbt(buffer.readNbt());
        time = buffer.readInt();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeNbt(data.serializeNBT());
        buffer.writeInt(time);
    }
}
