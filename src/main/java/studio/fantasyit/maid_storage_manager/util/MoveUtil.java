package studio.fantasyit.maid_storage_manager.util;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.maid.memory.AbstractTargetMemory;
import studio.fantasyit.maid_storage_manager.storage.MaidStorage;
import studio.fantasyit.maid_storage_manager.storage.base.IFilterable;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageContext;

import java.util.Collection;
import java.util.List;

public class MoveUtil {
    public static @Nullable BlockPos selectPosForTarget(ServerLevel level, EntityMaid maid, BlockPos target) {
        //寻找落脚点
        return PosUtil.findAroundUpAndDown(target,
                pos -> {
                    if (!PosUtil.isSafePos(level, pos)) return null;

                    if (maid.isWithinRestriction(pos) && maid.canPathReach(pos) && PosUtil.canTouch(level, pos, target)) {
                        return pos;
                    } else {
                        return null;
                    }
                });
    }

    public static @Nullable Pair<ResourceLocation, BlockPos> findTargetForPos(ServerLevel level, EntityMaid maid, BlockPos blockPos, AbstractTargetMemory memory) {
        return findTargetForPos(level, maid, blockPos, memory, false);
    }

    public static @Nullable Pair<ResourceLocation, BlockPos> findTargetForPos(ServerLevel level, EntityMaid maid, BlockPos blockPos, AbstractTargetMemory memory, boolean allowRequestOnly) {
        return PosUtil.findAroundUpAndDown(blockPos, (pos) -> {
            if (memory.isVisitedPos(pos)) {
                return null;
            }
            Pair<ResourceLocation, BlockPos> validTarget = MaidStorage.getInstance().isValidTarget(level, maid, pos);
            if (validTarget == null || !PosUtil.canTouch(level, blockPos, pos)) return null;
            if (!allowRequestOnly) {
                IStorageContext iStorageContext = MaidStorage.getInstance().getStorage(validTarget.getA()).onPreviewFilter(level, maid, pos);
                if (iStorageContext instanceof IFilterable ift) {
                    iStorageContext.start(maid, level, pos);
                    if (ift.isRequestOnly())
                        return null;
                }
            }
            return validTarget;
        });
    }
}
