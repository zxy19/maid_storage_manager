package studio.fantasyit.maid_storage_manager.storage.ItemHandler;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.storage.Storage;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageInsertableContext;

public class ContextItemHandlerStore extends FilterableItemHandler implements IStorageInsertableContext {
    private SimulateTargetInteractHelper helper;
    private EntityMaid maid;
    private ItemStack filter;

    public ContextItemHandlerStore(Storage storage) {
        super(storage);
    }

    @Override
    public void start(EntityMaid maid, ServerLevel level, Storage target) {
        this.maid = maid;
        helper = new SimulateTargetInteractHelper(maid, target.pos, target.side, level);
        helper.open();
        super.init(level, target);
    }

    @Override
    public void finish() {
        helper.stop();
    }

    @Override
    public ItemStack insert(ItemStack item) {
        if (!this.isAvailable(item)) return item;
        ItemStack copy = item.copy();
        for (int i = 0; i < this.helper.itemHandler.getSlots(); i++) {
            copy = this.helper.itemHandler.insertItem(i, copy, false);
            if (copy.isEmpty()) return ItemStack.EMPTY;
        }
        return copy;
    }
}
