package studio.fantasyit.maid_storage_manager.network;

import net.minecraft.network.FriendlyByteBuf;
import studio.fantasyit.maid_storage_manager.ai.GetStorageFunction;

import java.util.ArrayList;
import java.util.List;

public class AIMatchLocalizedItemC2SPacket {
    int rpcId;
    List<GetStorageFunction.ItemStackI18N> data;

    public AIMatchLocalizedItemC2SPacket(int rpcId, List<GetStorageFunction.ItemStackI18N> patten) {
        this.rpcId = rpcId;
        this.data = patten;
    }

    public AIMatchLocalizedItemC2SPacket(FriendlyByteBuf buffer) {
        rpcId = buffer.readInt();
        data = buffer.readCollection(ArrayList::new,(t)->new GetStorageFunction.ItemStackI18N(t.readUtf(),t.readUtf(),t.readUtf()));
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeInt(rpcId);
        buffer.writeCollection(data, (t, c)-> {
            t.writeUtf(c.id());
            t.writeUtf(c.name());
            t.writeUtf(c.tooltip());
        });
    }
}
