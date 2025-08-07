package studio.fantasyit.maid_storage_manager.data;

import studio.fantasyit.maid_storage_manager.craft.work.ProgressData;

import java.util.HashMap;
import java.util.Map;

public class MaidProgressData {
    private static final Map<ProgressData.ProgressMeta, ProgressData> progressData = new HashMap<>();

    public static void setByMaid(ProgressData.ProgressMeta uuid, ProgressData data) {
        if (progressData.containsKey(uuid)) {
            if (progressData.get(uuid).tickCount > data.tickCount) {
                if (progressData.get(uuid).tickCount - data.tickCount > 40)
                    progressData.remove(uuid);
            } else if (data.tickCount - progressData.get(uuid).tickCount < 10 && progressData.get(uuid).maxSz >= data.maxSz)
                return;
        }
        progressData.put(uuid, data);
    }

    public static ProgressData getByMaid(ProgressData.ProgressMeta uuid) {
        return progressData.get(uuid);
    }

    public static void clearAll() {
        progressData.clear();
    }
}
