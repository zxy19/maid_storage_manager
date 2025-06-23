package studio.fantasyit.maid_storage_manager.storage.base;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.util.StorageAccessUtil;

public class AbstractFilterableBlockStorage implements IFilterable, IStorageContext {

    private StorageAccessUtil.Filter filter;

    @Override
    public void start(EntityMaid maid, ServerLevel level, Target target) {
        init(level, target);
    }

    public void init(ServerLevel level, Target target) {
        filter = StorageAccessUtil.getFilterForTarget(level, target);
    }

    @Override
    public boolean isAvailable(ItemStack itemStack) {
        return filter.isAvailable(itemStack);
    }

    @Override
    public boolean isWhitelist() {
        return filter.isWhitelist();
    }
}
