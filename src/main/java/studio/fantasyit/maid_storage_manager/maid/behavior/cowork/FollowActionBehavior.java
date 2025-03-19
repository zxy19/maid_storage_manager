package studio.fantasyit.maid_storage_manager.maid.behavior.cowork;

import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidCheckRateTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.phys.Vec3;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.debug.DebugData;
import studio.fantasyit.maid_storage_manager.maid.behavior.ScheduleBehavior;
import studio.fantasyit.maid_storage_manager.registry.MemoryModuleRegistry;
import studio.fantasyit.maid_storage_manager.util.Conditions;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;
import studio.fantasyit.maid_storage_manager.util.MoveUtil;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.Condition;

public class FollowActionBehavior extends MaidCheckRateTask {
    public FollowActionBehavior() {
        super(Map.of(MemoryModuleRegistry.CO_WORK_MODE.get(), MemoryStatus.VALUE_PRESENT));
        this.setMaxCheckRate(10);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel worldIn, EntityMaid owner) {
        return MemoryUtil.getCurrentlyWorking(owner) == ScheduleBehavior.Schedule.CO_WORK;
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long p_22542_) {
        LivingEntity owner = maid.getOwner();
        if (owner == null) return;
        if (maid.distanceTo(owner) < 2) {
            standBack(level, maid, owner);
        } else traceOwner(maid, owner);
    }

    private void standBack(ServerLevel level, EntityMaid maid, LivingEntity owner) {
        Vec3 standBackDirection = maid.position().subtract(owner.position()).normalize().scale(2);
        BlockPos standBackSearchCenter = BlockPos.containing(maid.position().add(standBackDirection));
        BlockPos targetPos = MoveUtil.selectPosForTarget(level, maid, standBackSearchCenter);
        if (targetPos != null) {
            MemoryUtil.setTarget(maid, targetPos, (float) Config.followSpeed);
            if (MemoryUtil.getCoWorkTargetStorage(maid) == null) {
                EntityTracker tracker = new EntityTracker(owner, true);
                maid.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, tracker);
            }
        }
    }

    private void traceOwner(EntityMaid maid, LivingEntity owner) {
        EntityTracker tracker = new EntityTracker(owner, true);
        WalkTarget walktarget = new WalkTarget(
                tracker,
                (float) Config.followSpeed,
                4);
        maid.getBrain().setMemory(MemoryModuleType.WALK_TARGET, walktarget);
        if (MemoryUtil.getCoWorkTargetStorage(maid) == null) {
            maid.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, tracker);
        }
    }
}
