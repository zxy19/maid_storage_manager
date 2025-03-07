package studio.fantasyit.maid_storage_manager.storage.ItemHandler;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.storage.Storage;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageInteractContext;

import java.util.function.Function;

public class ContextItemHandlerCollect extends FilterableItemHandler implements IStorageInteractContext {
    private SimulateTargetInteractHelper helper;
    private EntityMaid maid;
    private final int currentSlot = 0;

    public ContextItemHandlerCollect(Storage storage) {
        super(storage);
    }

    @Override
    public void start(EntityMaid maid, ServerLevel level, Storage target) {
        this.maid = maid;
        helper = new SimulateTargetInteractHelper(maid, target.pos,target.side, level);
        helper.open();
        super.init(level, target);
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
