package studio.fantasyit.maid_storage_manager.maid.behavior.meal;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.craft.debug.ProgressDebugContext;
import studio.fantasyit.maid_storage_manager.debug.DebugData;
import studio.fantasyit.maid_storage_manager.maid.behavior.ScheduleBehavior;
import studio.fantasyit.maid_storage_manager.storage.MaidStorage;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;
import studio.fantasyit.maid_storage_manager.util.MoveUtil;

public class MealMoveBehavior extends Behavior<EntityMaid> {
    public MealMoveBehavior() {
        super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT, InitEntities.TARGET_POS.get(), MemoryStatus.VALUE_ABSENT));
    }

    @Override
    protected boolean checkExtraStartConditions(@NotNull ServerLevel worldIn, @NotNull EntityMaid owner) {
        return MemoryUtil.getCurrentlyWorking(owner) == ScheduleBehavior.Schedule.MEAL;
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long p_22542_) {
        @Nullable Target target = MemoryUtil.getMeal(maid).getTarget();
        @Nullable Target storage = target == null ? null : MaidStorage.getInstance().isValidTarget(level, maid, target.pos, target.side);
        if (target == null || storage == null) {
            DebugData.sendDebug(maid, ProgressDebugContext.TYPE.MOVE, "[Meal]No Target");
            MemoryUtil.getMeal(maid).clearTarget();
            return;
        }
        //寻找落脚点
        BlockPos goal = MoveUtil.selectPosForTarget(level, maid, target.getPos());
        if (goal == null) {
            return;
        }
        DebugData.sendDebug(maid, ProgressDebugContext.TYPE.MOVE, "[Meal]Target %s", goal.toShortString());
        MemoryUtil.setTarget(maid, goal, (float) Config.collectSpeed);
    }

}
