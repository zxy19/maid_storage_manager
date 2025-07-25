package studio.fantasyit.maid_storage_manager.network;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.data.InventoryItem;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PartialInventoryListData implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PartialInventoryListData> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(
                    MaidStorageManager.MODID, "PartialInventoryListData"
            )
    );


    @Override
    public CustomPacketPayload.Type<PartialInventoryListData> type() {
        return TYPE;
    }

    public static StreamCodec<RegistryFriendlyByteBuf, PartialInventoryListData> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,
            t -> t.key,
            ByteBufCodecs.collection(
                    ArrayList::new,
                    InventoryItem.STREAM_CODEC
            ),
            t -> t.data,
            PartialInventoryListData::new
    );

    public final UUID key;
    List<InventoryItem> data;

    public PartialInventoryListData(UUID key, List<InventoryItem> data) {
        this.key = key;
        this.data = data;
    }
}
