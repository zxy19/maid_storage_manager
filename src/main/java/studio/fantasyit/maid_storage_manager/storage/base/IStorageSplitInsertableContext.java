package studio.fantasyit.maid_storage_manager.storage.base;

import net.minecraft.world.item.ItemStack;

/**
 * 可插入不堆叠物品上下文
 */
public interface IStorageSplitInsertableContext extends IStorageContext {
    /**
     * 插入物品，不与现有的物品堆叠
     * @param item 物品
     * @return 剩余物品
     */
    ItemStack splitInsert(ItemStack item);
}
