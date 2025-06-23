package studio.fantasyit.maid_storage_manager.storage.qio;

import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageInsertableContext;

public class QIOInsertContext extends QIOBaseContext implements IStorageInsertableContext {
    @Override
    public ItemStack insert(ItemStack item) {
        return frequency.addItem(item);
    }
}
