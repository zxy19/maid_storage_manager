package studio.fantasyit.maid_storage_manager.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.registries.ForgeRegistries;
import studio.fantasyit.maid_storage_manager.ai.GetStorageFunction;
import studio.fantasyit.maid_storage_manager.data.InventoryItem;
import studio.fantasyit.maid_storage_manager.data.InventoryListDataClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class AIMatchLocalizedItemS2CPacket {
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

    public AIMatchLocalizedItemS2CPacket(FriendlyByteBuf buffer) {
        rpcId = buffer.readInt();
        uuid = buffer.readUUID();
        patten = buffer.readUtf();
        queryTooltip = buffer.readBoolean();
        all = buffer.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeInt(rpcId);
        buffer.writeUUID(uuid);
        buffer.writeUtf(patten);
        buffer.writeBoolean(queryTooltip);
        buffer.writeBoolean(all);
    }

    public static void handle(AIMatchLocalizedItemS2CPacket packet) {
        List<InventoryItem> inventoryItems = InventoryListDataClient.getInstance().get(packet.uuid);
        List<GetStorageFunction.ItemStackI18N> results = new ArrayList<>();
        if (packet.all) {
            for(Item item : ForgeRegistries.ITEMS){
                ItemStack itemStack = item.getDefaultInstance();
                String tooltip = String.join("\n",itemStack.getTooltipLines(Minecraft.getInstance().player, TooltipFlag.ADVANCED).stream().map(Component::getString).toList());
                String name = itemStack.getHoverName().getString();
                String id = Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(item)).toString();
                if (id.contains(packet.patten) || name.contains(packet.patten) || (tooltip.contains(packet.patten) && packet.queryTooltip)) {
                    results.add(new GetStorageFunction.ItemStackI18N(id, name, tooltip));
                    if(results.size() > 200)
                        break;
                }
            }
        } else {
            for (InventoryItem inventoryItem : inventoryItems) {
                String tooltip = String.join("\n", inventoryItem.itemStack.getTooltipLines(Minecraft.getInstance().player, TooltipFlag.ADVANCED).stream().map(Component::getString).toList());
                String name = inventoryItem.itemStack.getHoverName().getString();
                String id = Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(inventoryItem.itemStack.getItem())).toString();
                if (id.contains(packet.patten) || name.contains(packet.patten) || (tooltip.contains(packet.patten) && packet.queryTooltip)) {
                    results.add(new GetStorageFunction.ItemStackI18N(id, name, tooltip));
                    if(results.size() > 200)
                        break;
                }
            }
        }
        Network.INSTANCE.sendToServer(new AIMatchLocalizedItemC2SPacket(packet.rpcId, results));
    }
}
