package studio.fantasyit.maid_storage_manager.storage.create.stock;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.simibubi.create.content.logistics.BigItemStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageInteractContext;

import java.util.function.Function;

public class CreateViewContext extends AbstractCreateContext implements IStorageInteractContext {

    @Override
    public void start(EntityMaid maid, ServerLevel level, Target target) {
        super.start(maid, level, target);
    }

    @Override
    public void tick(Function<ItemStack, ItemStack> callback) {
        for (int i = 0; i < 20; i++) {
            if (current >= stacks.size()) return;
            BigItemStack bigItemStack = stacks.get(current);
            callback.apply(bigItemStack.stack.copyWithCount(bigItemStack.count));
            current++;
        }
    }
}
