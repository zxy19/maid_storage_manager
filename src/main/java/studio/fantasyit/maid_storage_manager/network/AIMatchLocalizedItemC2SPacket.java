package studio.fantasyit.maid_storage_manager.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.ai.GetStorageFunction;

import java.util.ArrayList;
import java.util.List;

public class AIMatchLocalizedItemC2SPacket implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<AIMatchLocalizedItemC2SPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(
                    MaidStorageManager.MODID, "ai_match_localized_c2s"
            )
    );

    @Override
    public CustomPacketPayload.Type<AIMatchLocalizedItemC2SPacket> type() {
        return TYPE;
    }

    public static StreamCodec<RegistryFriendlyByteBuf, AIMatchLocalizedItemC2SPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            t -> t.rpcId,
            ByteBufCodecs.collection(ArrayList::new, GetStorageFunction.ItemStackI18N.CODEC_STREAM),
            t -> t.data,
            AIMatchLocalizedItemC2SPacket::new
    );

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
