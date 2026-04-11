package studio.fantasyit.maid_storage_manager.network;

import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.network.PacketDistributor;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.ai.GetStorageFunction;
import studio.fantasyit.maid_storage_manager.data.InventoryItem;
import studio.fantasyit.maid_storage_manager.data.InventoryListDataClient;
import studio.fantasyit.maid_storage_manager.util.ClientOps;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class AIMatchLocalizedItemS2CPacket implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<AIMatchLocalizedItemS2CPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(
                    MaidStorageManager.MODID, "ai_match_localized_s2c"
            )
    );

    @Override
    public CustomPacketPayload.Type<AIMatchLocalizedItemS2CPacket> type() {
        return TYPE;
    }

    public static StreamCodec<FriendlyByteBuf, AIMatchLocalizedItemS2CPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            t -> t.rpcId,
            UUIDUtil.STREAM_CODEC,
            t -> t.uuid,
            ByteBufCodecs.STRING_UTF8,
            t -> t.patten,
            ByteBufCodecs.BOOL,
            t -> t.queryTooltip,
            ByteBufCodecs.BOOL,
            t -> t.all,
            AIMatchLocalizedItemS2CPacket::new
    );
    boolean queryTooltip;
    UUID uuid;
    int rpcId;
    String patten;
    boolean all;

    public AIMatchLocalizedItemS2CPacket(int rpcId, UUID uuid, String patten,boolean queryTooltip, boolean all) {
        this.rpcId = rpcId;
        this.uuid = uuid;
        this.patten = patten;
        this.queryTooltip = queryTooltip;
        this.all = all;
    }
    public static void handle(AIMatchLocalizedItemS2CPacket packet) {
        List<InventoryItem> inventoryItems = InventoryListDataClient.getInstance().get(packet.uuid);
        List<GetStorageFunction.ItemStackI18N> results = new ArrayList<>();
        if (packet.all) {
            for(Item item : BuiltInRegistries.ITEM){
                ItemStack itemStack = item.getDefaultInstance();
                String tooltip = String.join("\n",itemStack.getTooltipLines(Item.TooltipContext.of(ClientOps.getLevel()),ClientOps.getPlayer(), TooltipFlag.ADVANCED).stream().map(Component::getString).toList());
                String name = itemStack.getHoverName().getString();
                String id = Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(item)).toString();
                if (id.contains(packet.patten) || name.contains(packet.patten) || (tooltip.contains(packet.patten) && packet.queryTooltip)) {
                    results.add(new GetStorageFunction.ItemStackI18N(id, name, tooltip));
                    if(results.size() > 200)
                        break;
                }
            }
        } else {
            for (InventoryItem inventoryItem : inventoryItems) {
                String tooltip = String.join("\n", inventoryItem.itemStack.getTooltipLines(Item.TooltipContext.of(ClientOps.getLevel()),ClientOps.getPlayer(), TooltipFlag.ADVANCED).stream().map(Component::getString).toList());
                String name = inventoryItem.itemStack.getHoverName().getString();
                String id = Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(inventoryItem.itemStack.getItem())).toString();
                if (id.contains(packet.patten) || name.contains(packet.patten) || (tooltip.contains(packet.patten) && packet.queryTooltip)) {
                    results.add(new GetStorageFunction.ItemStackI18N(id, name, tooltip));
                    if(results.size() > 200)
                        break;
                }
            }
        }
        PacketDistributor.sendToServer(new AIMatchLocalizedItemC2SPacket(packet.rpcId, results));
    }
}
