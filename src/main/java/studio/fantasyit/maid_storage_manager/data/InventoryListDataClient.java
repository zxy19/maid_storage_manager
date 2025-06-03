package studio.fantasyit.maid_storage_manager.data;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.mutable.MutableInt;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.items.WrittenInvListItem;
import studio.fantasyit.maid_storage_manager.network.Network;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@OnlyIn(Dist.CLIENT)
public class InventoryListDataClient {
    public static List<Pair<InventoryItem, MutableInt>> showingInv = new ArrayList<>();
    private static InventoryListDataClient instance;

    public static InventoryListDataClient getInstance() {
        if (instance == null) {
            instance = new InventoryListDataClient();
        }
        return instance;
    }

    public Map<UUID, List<InventoryItem>> dataMap = new ConcurrentHashMap<>();

    public static void setShowingInv(InventoryItem inventoryItem, int i) {
        showingInv.add(new Pair<>(inventoryItem, new MutableInt(i)));
    }

    public static void clearShowingInv() {
        showingInv.clear();
    }

    public static void tickShowingInv() {
        for (int i = InventoryListDataClient.showingInv.size() - 1; i >= 0; i--) {
            InventoryListDataClient.showingInv.get(i).getB().subtract(1);
            Integer value = InventoryListDataClient.showingInv.get(i).getB().getValue();
            if (value <= 0) {
                InventoryListDataClient.showingInv.remove(i);
            }
        }
    }

    public void patch(UUID uuid, List<InventoryItem> map) {
        for (InventoryItem pair : map) {
            List<InventoryItem> pairs = dataMap.computeIfAbsent(uuid, k -> new ArrayList<>());
            boolean found = false;
            for (int i = 0; i < pairs.size(); i++) {
                InventoryItem pair1 = pairs.get(i);
                if (ItemStack.isSameItemSameTags(pair1.itemStack, pair.itemStack)) {
                    pairs.set(i, pair);
                    found = true;
                    break;
                }
            }
            if (!found)
                pairs.add(pair);
        }
    }

    Set<UUID> requestSet = new HashSet<>();

    public void requestForDataIfFirstTime(UUID uuid) {
        if (requestSet.contains(uuid))
            return;
        Network.sendRequestListPacket(uuid);
        requestSet.add(uuid);
    }

    public List<InventoryItem> get(UUID uuid) {
        return dataMap.getOrDefault(uuid, new ArrayList<>());
    }

    public void tickRequest() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            for (ItemStack i : player.inventory.items)
                if (i.is(ItemRegistry.WRITTEN_INVENTORY_LIST.get()) &&
                        i.hasTag() &&
                        i.getTag().contains(WrittenInvListItem.TAG_UUID))
                    requestForDataIfFirstTime(i.getTag().getUUID(WrittenInvListItem.TAG_UUID));
        }
    }
}
