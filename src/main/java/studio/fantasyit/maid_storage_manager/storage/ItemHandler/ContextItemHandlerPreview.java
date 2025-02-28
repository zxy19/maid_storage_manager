package studio.fantasyit.maid_storage_manager.storage.ItemHandler;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageContext;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageInteractContext;

import java.util.function.Function;

public class ContextItemHandlerPreview extends FilterableItemHandler implements IStorageContext {
    @Override
    public void start(EntityMaid maid, ServerLevel level, BlockPos target) {
        super.init(level, target);
    }
}
