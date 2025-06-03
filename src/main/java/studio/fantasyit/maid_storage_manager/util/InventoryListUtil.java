package studio.fantasyit.maid_storage_manager.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.data.InventoryItem;
import studio.fantasyit.maid_storage_manager.data.InventoryListDataClient;
import studio.fantasyit.maid_storage_manager.items.WrittenInvListItem;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@OnlyIn(Dist.CLIENT)
public class InventoryListUtil {
    public static @Nullable UUID getInventoryListUUIDFromPlayerInv(List<ItemStack> inv) {
        return inv
                .stream()
                .filter(i -> i.is(ItemRegistry.WRITTEN_INVENTORY_LIST.get()) && i.hasTag() && i.getOrCreateTag().contains(WrittenInvListItem.TAG_UUID))
                .map(ItemStack::getOrCreateTag)
                .max(Comparator.comparingLong(i -> i.getLong(WrittenInvListItem.TAG_TIME)))
                .map(i -> i.getUUID(WrittenInvListItem.TAG_UUID))
                .orElse(null);
    }

    public static @Nullable ItemStack getMatchingFromInventory(UUID uuid, List<ItemStack> itemStack) {
        for (ItemStack itemStack1 : itemStack) {
            Optional<CraftGuideData> craftable = InventoryListDataClient.getInstance().get(uuid)
                    .stream()
                    .filter(i -> i.itemStack.is(ItemRegistry.CRAFT_GUIDE.get()))
                    .map(i -> CraftGuideData.fromItemStack(i.itemStack))
                    .filter(i -> i.getOutput().stream().anyMatch(ii -> ii.is(itemStack1.getItem())))
                    .findFirst();
            if (craftable.isPresent())
                return itemStack1;
        }
        for (ItemStack itemStack1 : itemStack) {
            Optional<InventoryItem> inventoryItemStream = InventoryListDataClient.getInstance().get(uuid)
                    .stream()
                    .filter(i -> ItemStackUtil.isSame(i.itemStack, itemStack1, false))
                    .max(Comparator.comparingInt(i -> i.totalCount));
            if (inventoryItemStream.isPresent())
                return itemStack1;
        }
        return null;
    }

    public static ItemStack getMatchingForPlayer(List<ItemStack> itemStack) {
        if( itemStack.isEmpty())
            return ItemStack.EMPTY;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null)
            return itemStack.get(0);
        return getMatchingForPlayerOrFirst(player, itemStack);
    }

    public static ItemStack getMatchingForPlayerOrFirst(LocalPlayer player, List<ItemStack> itemStack) {
        if( itemStack.isEmpty())
            return ItemStack.EMPTY;
        UUID uuid = getInventoryListUUIDFromPlayerInv(player.getInventory().items);
        if (uuid == null)
            return itemStack.get(0);
        ItemStack matchingFromInventory = getMatchingFromInventory(uuid, itemStack);
        return matchingFromInventory == null ? itemStack.get(0) : matchingFromInventory;
    }
}
