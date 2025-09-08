package studio.fantasyit.maid_storage_manager.maid.behavior.sorting;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import org.jetbrains.annotations.NotNull;
import studio.fantasyit.maid_storage_manager.maid.behavior.ScheduleBehavior;
import studio.fantasyit.maid_storage_manager.storage.MaidStorage;
import studio.fantasyit.maid_storage_manager.storage.StorageVisitLock;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.storage.base.ISortSlotContext;
import studio.fantasyit.maid_storage_manager.util.BehaviorBreath;
import studio.fantasyit.maid_storage_manager.util.Conditions;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;

import java.util.Map;

public class SortingBehavior extends Behavior<EntityMaid> {
    BehaviorBreath breath = new BehaviorBreath();
    private ISortSlotContext context = null;
    Target target = null;
    int count = 0;
    private StorageVisitLock.LockContext lock;

    public SortingBehavior() {
        super(Map.of());
    }

    @Override
    protected boolean checkExtraStartConditions(@NotNull ServerLevel worldIn, @NotNull EntityMaid maid) {
        if (MemoryUtil.getCurrentlyWorking(maid) != ScheduleBehavior.Schedule.SORTING) return false;
        if (!MemoryUtil.getSorting(maid).hasTarget()) return false;
        return Conditions.hasReachedValidTargetOrReset(maid);
    }

    @Override
    protected boolean canStillUse(ServerLevel p_22545_, EntityMaid maid, long p_22547_) {
        if (Conditions.isWaitingForReturn(maid)) return false;
        return context != null && !context.isDoneSorting();
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long p_22542_) {
        lock = StorageVisitLock.DUMMY;
        if (!MemoryUtil.getSorting(maid).hasTarget()) return;
        MemoryUtil.setWorking(maid, true);
        target = MemoryUtil.getSorting(maid).getTarget();
        context = MaidStorage
                .getInstance()
                .getStorage(target.getType())
                .onStartSorting(level, maid, target);
        if (context != null) {
            context.start(maid, level, target);
            context.startSorting();
            lock = StorageVisitLock.getWriteLock(target);
        }
    }

    @Override
    protected void tick(ServerLevel p_22551_, EntityMaid maid, long p_22553_) {
        if (!lock.checkAndTryGrantLock()) return;
        if (!breath.breathTick(maid)) return;
        if (context != null)
            context.tickSorting();
    }

    @Override
    protected void stop(ServerLevel level, EntityMaid maid, long p_22550_) {
        lock.release();
        MemoryUtil.setWorking(maid, false);
        if (context != null) {
            context.finish();
        }
        MemoryUtil.getSorting(maid).removeNeedToSorting(target);
        MemoryUtil.clearTarget(maid);
    }

    @Override
    protected boolean timedOut(long p_22537_) {
        return false;
    }
}