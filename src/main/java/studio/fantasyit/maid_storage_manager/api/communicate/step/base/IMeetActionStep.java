package studio.fantasyit.maid_storage_manager.api.communicate.step.base;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.MaidPathFindingBFS;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.util.Conditions;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;
import studio.fantasyit.maid_storage_manager.util.PosUtil;

import java.util.Map;

public interface IMeetActionStep extends IActionStep {
    public static final ResourceLocation CHECK_PATH_MEETING = new ResourceLocation("maid_storage_manager:meet");

    @Override
    default boolean isAvailable(Map<ResourceLocation, Boolean> checks, EntityMaid wisher, EntityMaid handler) {
        if (checks.containsKey(CHECK_PATH_MEETING))
            return checks.get(CHECK_PATH_MEETING);
        Pair<BlockPos, BlockPos> blockPosBlockPosPair = getMeetPoint(wisher, handler);
        checks.put(CHECK_PATH_MEETING, blockPosBlockPosPair != null);
        return blockPosBlockPosPair != null;
    }

    private Pair<BlockPos, BlockPos> getMeetPoint(EntityMaid wisher, EntityMaid handler) {
        ServerLevel level = (ServerLevel) wisher.level();
        float restrictRadiusOwner = wisher.hasRestriction() ? wisher.getRestrictRadius() : 5;
        MaidPathFindingBFS pathFinding = new MaidPathFindingBFS(wisher.getNavigation().getNodeEvaluator(), level, wisher, restrictRadiusOwner + 2, (int) (restrictRadiusOwner + 2));
        return PosUtil.pickMeetingPosPair(wisher, handler, pathFinding);
    }

    @Override
    default boolean prepare(EntityMaid wisher, EntityMaid handler) {
        Pair<BlockPos, BlockPos> blockPosBlockPosPair = getMeetPoint(wisher, handler);
        if (blockPosBlockPosPair == null)
            return false;
        MemoryUtil.setTarget(wisher, blockPosBlockPosPair.getA(), 0.5f);
        MemoryUtil.setTarget(handler, blockPosBlockPosPair.getB(), 0.5f);
        return true;
    }

    @Override
    default boolean isPrepareDone(EntityMaid wisher, EntityMaid handler) {
        return Conditions.hasReachedValidTargetOrReset(wisher) && Conditions.hasReachedValidTargetOrReset(handler);
    }

    @Override
    default boolean shouldRunPrepare(EntityMaid wisher, EntityMaid handler, boolean prepared) {
        if (!prepared)
            return true;
        if (!wisher.getBrain().hasMemoryValue(InitEntities.TARGET_POS.get()))
            return true;
        if (!handler.getBrain().hasMemoryValue(InitEntities.TARGET_POS.get()))
            return true;
        return false;
    }
}
