package studio.fantasyit.maid_storage_manager.util;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.items.WorkCardItem;
import studio.fantasyit.maid_storage_manager.storage.Target;

public class ViewedInventoryUtil {
    public static void ambitiousAddItemAndSync(EntityMaid maid, ServerLevel level, Target target, ItemStack itemStack) {
        MemoryUtil.getViewedInventory(maid).ambitiousAddItem(level, target, itemStack);
        WorkCardItem.getNearbyMaidsSameGroup(maid, false, true)
                .forEach(maid1 -> MemoryUtil.getViewedInventory(maid1).ambitiousAddItem(level, target, itemStack));
    }

    public static void ambitiousRemoveItemAndSync(EntityMaid maid, ServerLevel level, Target target, ItemStack itemStack, int count) {
        MemoryUtil.getViewedInventory(maid).ambitiousRemoveItem(level, target, itemStack, count);
        WorkCardItem.getNearbyMaidsSameGroup(maid, false, true)
                .forEach(maid1 -> MemoryUtil.getViewedInventory(maid1).ambitiousRemoveItem(level, target, itemStack, count));
    }

    public static void syncStorageOn(EntityMaid maid, Target target, int holdStamp) {
        WorkCardItem.syncStorageOn(maid, target, holdStamp);
    }
}
