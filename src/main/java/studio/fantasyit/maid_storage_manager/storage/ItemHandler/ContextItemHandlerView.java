package studio.fantasyit.maid_storage_manager.storage.ItemHandler;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageInteractContext;

import java.util.function.Function;

public class ContextItemHandlerView extends FilterableItemHandler implements IStorageInteractContext {
    private SimulateTargetInteractHelper helper;

    @Override
    public void start(EntityMaid maid, ServerLevel level, BlockPos target) {
        helper = new SimulateTargetInteractHelper(maid, target, level);
        helper.open();
        super.init(level, target);
    }


    @Override
    public void finish() {
        helper.stop();
    }

    @Override
    public boolean isDone() {
        return helper.doneViewing();
    }

    @Override
    public void tick(Function<ItemStack, ItemStack> process) {
        helper.viewItemTick(process::apply);
    }
}
