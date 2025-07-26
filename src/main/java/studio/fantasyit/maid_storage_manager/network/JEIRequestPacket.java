package studio.fantasyit.maid_storage_manager.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;

import java.util.ArrayList;
import java.util.List;

public class JEIRequestPacket implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<JEIRequestPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(
                    MaidStorageManager.MODID, "jei_request"
            )
    );

    @Override
    public CustomPacketPayload.Type<JEIRequestPacket> type() {
        return TYPE;
    }

    public static StreamCodec<RegistryFriendlyByteBuf, JEIRequestPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(
                    ArrayList::new,
                    ItemStackUtil.OPTIONAL_STREAM_CODEC
            ),
            t -> t.data,
            ByteBufCodecs.INT,
            t -> t.targetMaidId,
            JEIRequestPacket::new
    );

    List<ItemStack> data;
    int targetMaidId;

    public JEIRequestPacket(List<ItemStack> data, int maidId) {
        this.data = data;
        this.targetMaidId = maidId;
    }
}
