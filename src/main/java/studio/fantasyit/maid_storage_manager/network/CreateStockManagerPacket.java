package studio.fantasyit.maid_storage_manager.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public class CreateStockManagerPacket {

    public enum Type {
        SHOP_LIST,
        OPEN_SCREEN
    }

    public final Type data;
    public final BlockPos ticker;
    int id;

    public CreateStockManagerPacket(Type data, BlockPos ticker,int maidId) {
        this.data = data;
        this.ticker = ticker;
        this.id = maidId;
    }

    public CreateStockManagerPacket(FriendlyByteBuf buffer) {
        data = Type.valueOf(buffer.readUtf());
        ticker = buffer.readBlockPos();
        id = buffer.readInt();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeUtf(data.name());
        buffer.writeBlockPos(ticker);
        buffer.writeInt(id);
    }
}
