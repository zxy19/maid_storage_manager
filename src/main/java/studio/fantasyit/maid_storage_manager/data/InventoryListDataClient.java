package studio.fantasyit.maid_storage_manager.data;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import oshi.util.tuples.Pair;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@OnlyIn(Dist.CLIENT)
public class InventoryListDataClient {
    private static InventoryListDataClient instance;
    public static InventoryListDataClient getInstance(){
        if(instance == null){
            instance = new InventoryListDataClient();
        }
        return instance;
    }

    public Map<UUID, Map<String, Integer>> dataMap = new ConcurrentHashMap<>();

    public void patch(UUID uuid, List<Pair<String, Integer>> map){
        for(Pair<String, Integer> pair : map){
            dataMap.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>()).put(pair.getA(), pair.getB());
        }
    }

    public Map<String, Integer> get(UUID uuid){
        return dataMap.getOrDefault(uuid, new ConcurrentHashMap<>());
    }
}
