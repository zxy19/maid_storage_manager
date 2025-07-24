package studio.fantasyit.maid_storage_manager.storage.rs;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageInteractContext;
import studio.fantasyit.maid_storage_manager.util.MathUtil;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

public class RSViewContext extends AbstractRSContext implements IStorageInteractContext {
    Iterator<ItemStack> iter = null;

    @Override
    public void start(EntityMaid maid, ServerLevel level, Target target) {
        super.start(maid, level, target);
        reset();
    }

    @Override
    public void reset() {
        if (itemStorage != null) {
            List<ItemStack> tmp = itemStorage.getAll()
                    .stream()
                    .filter(stack -> stack.resource() instanceof ItemResource)
                    .map(stack -> ((ItemResource) stack.resource()).toItemStack().copyWithCount(MathUtil.toIntOrMax(stack.amount())))
                    .toList();
            iter = tmp.iterator();
        }
    }

    @Override
    public void tick(Function<ItemStack, ItemStack> process) {
        for (int i = 0; i < 20; i++) {
            if (!iter.hasNext())
                return;
            ItemStack entry = iter.next();
            process.apply(entry);
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
