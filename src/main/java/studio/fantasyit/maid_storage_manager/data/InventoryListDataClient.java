package studio.fantasyit.maid_storage_manager.data;

import com.mojang.datafixers.util.Pair;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@OnlyIn(Dist.CLIENT)
public class InventoryListDataClient {
    private static InventoryListDataClient instance;

    public static InventoryListDataClient getInstance() {
        if (instance == null) {
            instance = new InventoryListDataClient();
        }
        return instance;
    }

    public Map<UUID, List<Pair<ItemStack, Integer>>> dataMap = new ConcurrentHashMap<>();

    public void patch(UUID uuid, List<Pair<ItemStack, Integer>> map) {
        for (Pair<ItemStack, Integer> pair : map) {
            List<Pair<ItemStack, Integer>> pairs = dataMap.computeIfAbsent(uuid, k -> new ArrayList<>());
            boolean found = false;
            for (int i = 0; i < pairs.size(); i++) {
                Pair<ItemStack, Integer> pair1 = pairs.get(i);
                if (ItemStack.isSameItemSameTags(pair1.getFirst(), pair.getFirst())) {
                    pairs.set(i, new Pair<>(pair.getFirst(), pair.getSecond()));
                    found = true;
                    break;
                }
            }
            if (!found)
                pairs.add(pair);
        }
    }

    public List<Pair<ItemStack, Integer>> get(UUID uuid) {
        return dataMap.getOrDefault(uuid, new ArrayList<>());
    }
}
