package studio.fantasyit.maid_storage_manager.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;

public class CreateStockManagerPacket implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<CreateStockManagerPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(
                    MaidStorageManager.MODID, "create_stock_manager_packet"
            )
    );

    @Override
    public CustomPacketPayload.Type<CreateStockManagerPacket> type() {
        return TYPE;
    }

    public static StreamCodec<ByteBuf, CreateStockManagerPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            t -> t.data.name(),
            BlockPos.STREAM_CODEC,
            t -> t.ticker,
            ByteBufCodecs.INT,
            t -> t.id,
            CreateStockManagerPacket::new
    );

    public enum Type {
        SHOP_LIST,
        OPEN_SCREEN
    }

    public final Type data;
    public final BlockPos ticker;
    int id;

    public CreateStockManagerPacket(Type data, BlockPos ticker, int maidId) {
        this.data = data;
        this.ticker = ticker;
        this.id = maidId;
    }

    public CreateStockManagerPacket(String data, BlockPos ticker, int maidId) {
        this.data = Type.valueOf(data);
        this.ticker = ticker;
        this.id = maidId;
    }
}
