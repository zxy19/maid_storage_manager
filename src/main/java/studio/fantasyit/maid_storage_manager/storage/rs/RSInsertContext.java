package studio.fantasyit.maid_storage_manager.storage.rs;

import com.refinedmods.refinedstorage.api.util.Action;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageInsertableContext;

public class RSInsertContext extends AbstractRSContext implements IStorageInsertableContext {
    @Override
    public ItemStack insert(ItemStack item) {
        if (network == null) return item;
        return network.insertItem(item, item.getCount(), Action.PERFORM);
    }
}
