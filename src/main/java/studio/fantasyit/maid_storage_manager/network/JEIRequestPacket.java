package studio.fantasyit.maid_storage_manager.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class JEIRequestPacket {
    List<ItemStack> data;
    int targetMaidId;
    public JEIRequestPacket(List<ItemStack> data, int maidId) {
        this.data = data;
        this.targetMaidId = maidId;
    }

    public JEIRequestPacket(FriendlyByteBuf buffer) {
        this.targetMaidId = buffer.readInt();
        this.data = buffer.readList(FriendlyByteBuf::readItem);
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeInt(targetMaidId);
        buffer.writeCollection(data, FriendlyByteBuf::writeItem);
    }
}
