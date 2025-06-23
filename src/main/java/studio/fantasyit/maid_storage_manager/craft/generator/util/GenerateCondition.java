package studio.fantasyit.maid_storage_manager.craft.generator.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.util.StorageAccessUtil;

public class GenerateCondition {
    public static StorageAccessUtil.Filter getFilterOn(Level level, BlockPos target) {
        return StorageAccessUtil.getFilterForTarget(level, Target.virtual(target, null));
    }
}
