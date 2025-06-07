package studio.fantasyit.maid_storage_manager.storage.create.stock;

import net.minecraft.core.BlockPos;

import java.util.concurrent.ConcurrentHashMap;

public class ChainRouterManager {
    public static class ChainRouterHistory {
        public int lastUpdateTick;
        public BlockPos lastBePos;
        public BlockPos lastConnection;

        public ChainRouterHistory(BlockPos lastBePos, BlockPos lastConnection, int lastUpdateTick) {
            this.lastBePos = lastBePos;
            this.lastConnection = lastConnection;
            this.lastUpdateTick = lastUpdateTick;
        }
    }

    public static final ConcurrentHashMap<String, ChainRouterHistory> chainRouterHistory = new ConcurrentHashMap<>();


    public static void update(String targetId, int tickCount) {
        if (chainRouterHistory.containsKey(targetId))
            chainRouterHistory.get(targetId).lastUpdateTick = tickCount;
    }

    public static boolean isChanged(String targetId, BlockPos lastBePos, BlockPos lastConnection) {
        if (chainRouterHistory.containsKey(targetId)) {
            return !chainRouterHistory.get(targetId).lastBePos.equals(lastBePos) ||
                    !chainRouterHistory.get(targetId).lastConnection.equals(lastConnection);
        }
        return false;
    }

    public static void set(String targetId, BlockPos lastBePos, BlockPos lastConnection, int tickCount) {
        chainRouterHistory.put(targetId, new ChainRouterHistory(lastBePos, lastConnection, tickCount));
    }

    public static int getLastTick(String targetPackageName) {
        if (chainRouterHistory.containsKey(targetPackageName))
            return chainRouterHistory.get(targetPackageName).lastUpdateTick;
        else
            return 0;
    }
}
