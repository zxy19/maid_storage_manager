package studio.fantasyit.maid_storage_manager.storage.rs;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.refinedmods.refinedstorage.api.util.StackListEntry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageInteractContext;

import java.util.Iterator;
import java.util.function.Function;

public class RSViewContext extends AbstractRSContext implements IStorageInteractContext {
    Iterator<StackListEntry<ItemStack>> iter = null;

    @Override
    public void start(EntityMaid maid, ServerLevel level, Target target) {
        super.start(maid, level, target);
        if (stackListStacks != null)
            iter = stackListStacks.iterator();
    }

    @Override
    public void reset() {
        if (stackListStacks != null)
            iter = stackListStacks.iterator();
    }

    @Override
    public void tick(Function<ItemStack, ItemStack> process) {
        for (int i = 0; i < 20; i++) {
            if (!iter.hasNext())
                return;
            StackListEntry<ItemStack> entry = iter.next();
            process.apply(entry.getStack());
        }
    }

    @Override
    public void finish() {
        iter = null;
    }

    @Override
    public boolean isDone() {
        return super.isDone() || iter == null || !iter.hasNext();
    }
}
