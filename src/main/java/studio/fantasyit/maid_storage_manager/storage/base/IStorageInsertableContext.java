package studio.fantasyit.maid_storage_manager.storage.base;

import net.minecraft.world.item.ItemStack;

/**
 * 可插入物品上下文
 */
public interface IStorageInsertableContext extends IStorageContext {
    /**
     * 插入物品
     * @param item 物品
     * @return 剩余物品
     */
    ItemStack insert(ItemStack item);
}
