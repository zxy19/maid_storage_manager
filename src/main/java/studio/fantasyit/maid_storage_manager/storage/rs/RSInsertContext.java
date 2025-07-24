package studio.fantasyit.maid_storage_manager.storage.rs;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageInsertableContext;

public class RSInsertContext extends AbstractRSContext implements IStorageInsertableContext {
    @Override
    public ItemStack insert(ItemStack item) {
        if (itemStorage == null) return item;

        long insert = itemStorage.insert(ItemResource.ofItemStack(item), item.getCount(), Action.EXECUTE, Actor.EMPTY);

        ItemStack result = item.copy();
        result.shrink((int) insert);
        return result;
    }
}
