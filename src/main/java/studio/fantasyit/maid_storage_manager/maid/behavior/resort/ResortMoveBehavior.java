package studio.fantasyit.maid_storage_manager.maid.behavior.resort;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.debug.DebugData;
import studio.fantasyit.maid_storage_manager.maid.behavior.ScheduleBehavior;
import studio.fantasyit.maid_storage_manager.storage.MaidStorage;
import studio.fantasyit.maid_storage_manager.util.Conditions;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;
import studio.fantasyit.maid_storage_manager.util.MoveUtil;

import java.util.Map;

public class ResortMoveBehavior extends Behavior<EntityMaid> {
    public ResortMoveBehavior() {
        super(Map.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT));
    }

    @Override
    protected boolean checkExtraStartConditions(@NotNull ServerLevel worldIn, @NotNull EntityMaid owner) {
        if (MemoryUtil.getCurrentlyWorking(owner) != ScheduleBehavior.Schedule.RESORT) return false;
        if (Conditions.isWaitingForReturn(owner)) return false;
        if (Conditions.takingRequestList(owner)) return false;
        if (!Conditions.isInvEmpty(owner)) return false;
        return MemoryUtil.getResorting(owner).hasTarget();
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long p_22542_) {
        @Nullable BlockPos target = MemoryUtil.getResorting(maid).getTargetPos();
        @Nullable Pair<ResourceLocation, BlockPos> storage = target == null ? null : MaidStorage.getInstance().isValidTarget(level, maid, target);

        //整理消失
        if (target == null || storage == null) {
            DebugData.getInstance().sendMessage("[RESORT]No Target");
            MemoryUtil.getResorting(maid).clearNeedToResort();
            MemoryUtil.getResorting(maid).clearTarget();
            return;
        }

        if (!storage.getA().equals(MemoryUtil.getResorting(maid).getTargetType())) {
            DebugData.getInstance().sendMessage("[RESORT]Target Changed");
            MemoryUtil.getResorting(maid).clearNeedToResort();
            MemoryUtil.getResorting(maid).clearTarget();
            return;
        }

        //寻找落脚点
        BlockPos goal = MoveUtil.selectPosForTarget(level, maid, target);

        if (goal == null) {
            return;
        }

        DebugData.getInstance().sendMessage("[RESORT]Target %s", goal.toShortString());
        MemoryUtil.setTarget(maid, goal, (float) Config.collectSpeed);
    }

}
