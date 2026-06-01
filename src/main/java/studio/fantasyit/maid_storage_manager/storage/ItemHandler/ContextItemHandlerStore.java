package studio.fantasyit.maid_storage_manager.storage.ItemHandler;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageInsertableContext;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageSplitInsertableContext;

public class ContextItemHandlerStore extends AbstractItemHandlerContext implements IStorageInsertableContext, IStorageSplitInsertableContext {
    @Override
    public void start(EntityMaid maid, ServerLevel level, Target target) {
        super.start(maid, level,target);
    }

    @Override
    public ItemStack insert(ItemStack item) {
        if (!this.helper.isStillValid()) return item;
        ItemStack copy = item.copy();
        for (int i = 0; i < this.helper.itemHandler.size(); i++) {
            try (var tx = Transaction.open(null)) {
                int inserted = this.helper.itemHandler.insert(i, ItemResource.of(copy), copy.getCount(), tx);
                tx.commit();
                copy.shrink(inserted);
                if (copy.isEmpty()) return ItemStack.EMPTY;
            }
        }
        return copy;
    }

    @Override
    public ItemStack splitInsert(ItemStack item) {
        if (!this.isAvailable(item)) return item;
        if (!this.helper.isStillValid()) return item;
        ItemStack copy = item.copy();
        for (int i = 0; i < this.helper.itemHandler.size(); i++) {
            if(!ItemUtil.getStack(this.helper.itemHandler, i).isEmpty())
                continue;
            try (var tx = Transaction.open(null)) {
                int inserted = this.helper.itemHandler.insert(i, ItemResource.of(copy), copy.getCount(), tx);
                tx.commit();
                copy.shrink(inserted);
                if (copy.isEmpty()) return ItemStack.EMPTY;
            }
        }
        return copy;
    }
}
