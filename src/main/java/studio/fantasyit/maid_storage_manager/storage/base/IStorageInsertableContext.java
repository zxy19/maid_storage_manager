package studio.fantasyit.maid_storage_manager.storage.base;

import net.minecraft.world.item.ItemStack;

public interface IStorageInsertableContext extends IStorageContext {
    ItemStack insert(ItemStack item);
}
