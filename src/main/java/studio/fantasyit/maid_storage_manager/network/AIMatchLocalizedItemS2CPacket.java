package studio.fantasyit.maid_storage_manager.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
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
    UUID uuid;
    int rpcId;
    String patten;

    public AIMatchLocalizedItemS2CPacket(int rpcId, UUID uuid, String patten) {
        this.rpcId = rpcId;
        this.uuid = uuid;
        this.patten = patten;
    }

    public AIMatchLocalizedItemS2CPacket(FriendlyByteBuf buffer) {
        rpcId = buffer.readInt();
        uuid = buffer.readUUID();
        patten = buffer.readUtf();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeInt(rpcId);
        buffer.writeUUID(uuid);
        buffer.writeUtf(patten);
    }

    public static void handle(AIMatchLocalizedItemS2CPacket packet) {
        List<InventoryItem> inventoryItems = InventoryListDataClient.getInstance().get(packet.uuid);
        List<GetStorageFunction.ItemStackI18N> results = new ArrayList<>();
        for (InventoryItem inventoryItem : inventoryItems) {
            if (inventoryItem.itemStack.getHoverName().getString().contains(packet.patten)) {
                results.add(new GetStorageFunction.ItemStackI18N(
                        Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(inventoryItem.itemStack.getItem())).toString(),
                        inventoryItem.itemStack.getHoverName().getString(),
                        String.join("\n",inventoryItem.itemStack.getTooltipLines(Minecraft.getInstance().player, TooltipFlag.ADVANCED).stream().map(Component::getString).toList())
                ));
            }
        }
        Network.INSTANCE.sendToServer(new AIMatchLocalizedItemC2SPacket(packet.rpcId, results));
    }
}
