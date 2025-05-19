package studio.fantasyit.maid_storage_manager.network;

import net.minecraft.network.FriendlyByteBuf;

import java.util.List;

public class RenderEntityPacket {
    public List<Integer> entityIds;
    public RenderEntityPacket(List<Integer> entityIds) {
        this.entityIds = entityIds;
    }

    public RenderEntityPacket(FriendlyByteBuf buffer) {
        entityIds = buffer.readList(FriendlyByteBuf::readInt);
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeCollection(entityIds, FriendlyByteBuf::writeInt);
    }
}
