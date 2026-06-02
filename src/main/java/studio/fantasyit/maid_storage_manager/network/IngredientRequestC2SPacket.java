package studio.fantasyit.maid_storage_manager.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;

import java.util.ArrayList;
import java.util.List;

public class IngredientRequestC2SPacket implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<IngredientRequestC2SPacket> TYPE = new CustomPacketPayload.Type<>(
            Identifier.fromNamespaceAndPath(
                    MaidStorageManager.MODID, "ingredient_request"
            )
    );

    @Override
    public CustomPacketPayload.Type<IngredientRequestC2SPacket> type() {
        return TYPE;
    }

    public static StreamCodec<RegistryFriendlyByteBuf, IngredientRequestC2SPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(
                    ArrayList::new,
                    ItemStackUtil.OPTIONAL_STREAM_CODEC
            ),
            t -> t.data,
            ByteBufCodecs.INT,
            t -> t.targetMaidId,
            IngredientRequestC2SPacket::new
    );

    List<ItemStack> data;
    int targetMaidId;

    public IngredientRequestC2SPacket(List<ItemStack> data, int maidId) {
        this.data = data;
        this.targetMaidId = maidId;
    }
}
