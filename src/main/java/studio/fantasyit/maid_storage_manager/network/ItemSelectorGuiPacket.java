package studio.fantasyit.maid_storage_manager.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;

public class ItemSelectorGuiPacket implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ItemSelectorGuiPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(
                    MaidStorageManager.MODID, "ItemSelectorGuiPacket"
            )
    );

    @Override
    public CustomPacketPayload.Type<ItemSelectorGuiPacket> type() {
        return TYPE;
    }

    public static StreamCodec<ByteBuf, ItemSelectorGuiPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            t -> t.type.name(),
            ByteBufCodecs.INT,
            t -> t.key,
            ByteBufCodecs.INT,
            t -> t.value,
            ItemSelectorGuiPacket::new
    );

    public enum SlotType {
        COUNT,
        MATCH_TAG,
        REPEAT,
        CLEAR,
        BLACKLIST,
        UNITSECOND,
        STOCKMODE
    }

    public final SlotType type;
    public final int key;

    public final int value;

    public ItemSelectorGuiPacket(String type, int key, int value) {
        this.type = SlotType.valueOf(type);
        this.key = key;
        this.value = value;
    }

    public ItemSelectorGuiPacket(SlotType type, int key, int value) {
        this.type = type;
        this.key = key;
        this.value = value;
    }
}
