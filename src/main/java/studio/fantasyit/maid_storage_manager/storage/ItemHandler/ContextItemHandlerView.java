package studio.fantasyit.maid_storage_manager.storage.ItemHandler;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageInteractContext;

import java.util.function.Function;

public class ContextItemHandlerView extends FilterableItemHandler implements IStorageInteractContext {
    private SimulateTargetInteractHelper helper;

    public ContextItemHandlerView(Target storage) {
        super(storage);
    }

    @Override
    public void start(EntityMaid maid, ServerLevel level, Target target) {
        helper = new SimulateTargetInteractHelper(maid, target.getPos(), target.getSide().orElse(null), level);
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
    public void reset() {
        helper.reset();
    }

    @Override
    public void tick(Function<ItemStack, ItemStack> process) {
        helper.viewItemTick(process::apply);
    }
}
