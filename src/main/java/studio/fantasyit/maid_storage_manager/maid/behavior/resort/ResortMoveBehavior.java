package studio.fantasyit.maid_storage_manager.maid.behavior.resort;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.debug.DebugData;
import studio.fantasyit.maid_storage_manager.maid.behavior.ScheduleBehavior;
import studio.fantasyit.maid_storage_manager.storage.MaidStorage;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;
import studio.fantasyit.maid_storage_manager.util.MoveUtil;

import java.util.Map;

public class ResortMoveBehavior extends Behavior<EntityMaid> {
    public ResortMoveBehavior() {
        super(Map.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT));
    }

    @Override
    protected boolean checkExtraStartConditions(@NotNull ServerLevel worldIn, @NotNull EntityMaid owner) {
        return MemoryUtil.getCurrentlyWorking(owner) == ScheduleBehavior.Schedule.RESORT;
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long p_22542_) {
        @Nullable Target target = MemoryUtil.getResorting(maid).getTarget();
        @Nullable Target storage = target == null ? null : MaidStorage.getInstance().isValidTarget(level, maid, target.pos, target.side);

        //整理消失
        if (target == null || storage == null) {
            DebugData.sendDebug("[RESORT]No Target");
            MemoryUtil.getResorting(maid).clearNeedToResort();
            MemoryUtil.getResorting(maid).clearTarget();
            return;
        }

        if (!storage.getType().equals(MemoryUtil.getResorting(maid).getTarget().getType())) {
            DebugData.sendDebug("[RESORT]Target Changed");
            MemoryUtil.getResorting(maid).clearNeedToResort();
            MemoryUtil.getResorting(maid).clearTarget();
            return;
        }
        //寻找落脚点
        BlockPos goal = MoveUtil.selectPosForTarget(level, maid, target.getPos());

        if (goal == null) {
            return;
        }

        DebugData.sendDebug("[RESORT]Target %s", goal.toShortString());
        MemoryUtil.setTarget(maid, goal, (float) Config.collectSpeed);
    }

}
