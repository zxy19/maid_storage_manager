package studio.fantasyit.maid_storage_manager.storage.ItemHandler;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.storage.base.AbstractFilterableBlockStorage;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageInteractContext;

import java.util.function.Function;

public class ContextItemHandlerCollect extends AbstractFilterableBlockStorage implements IStorageInteractContext {
    private SimulateTargetInteractHelper helper;

    @Override
    public void start(EntityMaid maid, ServerLevel level, Target target) {
        super.start(maid, level,target);
        helper = new SimulateTargetInteractHelper(maid, target.pos,target.side, level);
        helper.open();
    }

    @Override
    public void finish() {
        helper.stop();
    }

    @Override
    public boolean isDone() {
        return helper.doneTaking();
    }

    @Override
    public void reset() {
        helper.reset();
    }

    @Override
    public void tick(Function<ItemStack, ItemStack> process) {
        helper.takeItemTick(process);
    }
}
