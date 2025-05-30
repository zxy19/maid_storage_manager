package studio.fantasyit.maid_storage_manager.data;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.mutable.MutableInt;
import oshi.util.tuples.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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

    public List<InventoryItem> get(UUID uuid) {
        return dataMap.getOrDefault(uuid, new ArrayList<>());
    }
}
