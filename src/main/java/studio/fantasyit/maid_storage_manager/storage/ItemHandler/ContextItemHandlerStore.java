package studio.fantasyit.maid_storage_manager.storage.ItemHandler;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.storage.base.AbstractFilterableBlockStorage;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageInsertableContext;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageSplitInsertableContext;

public class ContextItemHandlerStore extends AbstractFilterableBlockStorage implements IStorageInsertableContext, IStorageSplitInsertableContext {
    private SimulateTargetInteractHelper helper;


    @Override
    public void start(EntityMaid maid, ServerLevel level, Target target) {
        super.start(maid, level,target);
        helper = new SimulateTargetInteractHelper(maid, target.pos, target.side, level);
        helper.open();
    }

    @Override
    public void finish() {
        helper.stop();
    }

    @Override
    public ItemStack insert(ItemStack item) {
        if (!this.helper.isStillValid()) return item;
        ItemStack copy = item.copy();
        for (int i = 0; i < this.helper.itemHandler.getSlots(); i++) {
            copy = this.helper.itemHandler.insertItem(i, copy, false);
            if (copy.isEmpty()) return ItemStack.EMPTY;
        }
        return copy;
    }

    @Override
    public ItemStack splitInsert(ItemStack item) {
        if (!this.isAvailable(item)) return item;
        if (!this.helper.isStillValid()) return item;
        ItemStack copy = item.copy();
        for (int i = 0; i < this.helper.itemHandler.getSlots(); i++) {
            if(!this.helper.itemHandler.getStackInSlot(i).isEmpty())
                continue;
            copy = this.helper.itemHandler.insertItem(i, copy, false);
            if (copy.isEmpty()) return ItemStack.EMPTY;
        }
        return copy;
    }
}