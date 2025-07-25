package studio.fantasyit.maid_storage_manager.data;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BindingData {
    private static List<Integer> entityIds = List.of();
    private static Map<UUID, ItemStack> playerUUIDToItem = new ConcurrentHashMap<>();

    public static void setEntityIds(List<Integer> entityIds) {
        BindingData.entityIds = entityIds;
    }

    public static List<Integer> getEntityIds() {
        return entityIds;
    }

    public static boolean isDifferentAndUpdateItemOnHand(ServerPlayer player) {
        ItemStack itemStack = player.getMainHandItem();
        if (playerUUIDToItem.containsKey(player.getUUID())) {
            ItemStack oldItemStack = playerUUIDToItem.get(player.getUUID());
            if (!ItemStack.isSameItemSameComponents(oldItemStack, itemStack)) {
                playerUUIDToItem.put(player.getUUID(), itemStack);
                return true;
            }
            playerUUIDToItem.put(player.getUUID(), itemStack);
            return false;
        } else {
            playerUUIDToItem.put(player.getUUID(), itemStack);
            return true;
        }
    }

    public static void clearFor(Player entity) {
        playerUUIDToItem.remove(entity.getUUID());
    }
}
