package studio.fantasyit.maid_storage_manager.maid.behavior.view;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.advancement.AdvancementTypes;
import studio.fantasyit.maid_storage_manager.debug.DebugData;
import studio.fantasyit.maid_storage_manager.maid.behavior.ScheduleBehavior;
import studio.fantasyit.maid_storage_manager.maid.behavior.base.MaidMoveToBlockTaskWithArrivalMap;
import studio.fantasyit.maid_storage_manager.storage.MaidStorage;
import studio.fantasyit.maid_storage_manager.storage.StoragePredictor;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;
import studio.fantasyit.maid_storage_manager.util.MoveUtil;
import studio.fantasyit.maid_storage_manager.util.PosUtil;

/**
 * 正常情况下尝试前往附近所有的箱子
 */
public class ViewMoveBehavior extends MaidMoveToBlockTaskWithArrivalMap {
    public ViewMoveBehavior() {
        super((float) Config.viewSpeed, 3);
        this.verticalSearchStart = 1;
        this.setMaxCheckRate(100);
    }

    Target chestPos = null;

    @Override
    protected boolean checkExtraStartConditions(@NotNull ServerLevel worldIn, @NotNull EntityMaid owner) {
        if (MemoryUtil.getViewedInventory(owner).getMarkChanged().isEmpty())
            if (!super.checkExtraStartConditions(worldIn, owner)) return false;
        if (MemoryUtil.getCurrentlyWorking(owner) != ScheduleBehavior.Schedule.VIEW) return false;
        return true;
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long p_22542_) {
        super.start(level, maid, p_22542_);
        AdvancementTypes.triggerForMaid(maid, AdvancementTypes.STORAGE_MANAGER);
        if (!priorityTarget(level, maid))
            this.searchForDestination(level, maid);
        if (!maid.getBrain().hasMemoryValue(InitEntities.TARGET_POS.get())) {
            if (MemoryUtil.getViewedInventory(maid).confirmNoTarget(2)) {
                MemoryUtil.getViewedInventory(maid).removeUnvisited();
                MemoryUtil.getViewedInventory(maid).resetVisitedPos();
                DebugData.sendDebug("[VIEW]Reset, waiting");
            }
        } else {
            MemoryUtil.getViewedInventory(maid).resetFailCount();
            if (chestPos != null) {
                MemoryUtil.getViewedInventory(maid).setTarget(chestPos);
                maid.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(chestPos.getPos()));
            }
        }
    }


    private boolean priorityTarget(ServerLevel level, EntityMaid maid) {
        Target toCheckTarget = MemoryUtil.getViewedInventory(maid).getMarkChanged().peek();
        if (toCheckTarget != null) {
            @Nullable Target storage = MaidStorage.getInstance().isValidTarget(level, maid, toCheckTarget.getPos(), toCheckTarget.side);
            if (storage != null && StoragePredictor.isViewable(storage)) {
                @Nullable BlockPos target = MoveUtil.selectPosForTarget(level, maid, toCheckTarget.getPos());
                if (target != null) {
                    chestPos = storage;
                    MemoryUtil.setTarget(maid, target, (float) Config.viewChangeSpeed);
                    DebugData.sendDebug("[VIEW]Priority By Change %s", storage);
                    MemoryUtil.getViewedInventory(maid).resetMarkFailTime();
                    return true;
                }
            }
            MemoryUtil.getViewedInventory(maid).markFailTime();
            return true;
        }
        return false;
    }

    @Override
    protected boolean shouldMoveTo(@NotNull ServerLevel serverLevel,
                                   EntityMaid entityMaid,
                                   @NotNull BlockPos blockPos) {
        if (!PosUtil.isSafePos(serverLevel, blockPos)) return false;
        //寻找当前格子能触碰的箱子
        Target canTouchChest = MoveUtil.findTargetForPos(serverLevel,
                entityMaid,
                blockPos,
                MemoryUtil.getViewedInventory(entityMaid),
                false,
                StoragePredictor::isViewable
        );
        if (canTouchChest != null) {
            DebugData.sendDebug("[VIEW]Target %s", canTouchChest);
            chestPos = canTouchChest;
            return true;
        }
        return false;
    }
}
