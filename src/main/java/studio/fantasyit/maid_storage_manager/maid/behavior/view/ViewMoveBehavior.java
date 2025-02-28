package studio.fantasyit.maid_storage_manager.maid.behavior.view;

import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidMoveToBlockTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.debug.DebugData;
import studio.fantasyit.maid_storage_manager.maid.behavior.ScheduleBehavior;
import studio.fantasyit.maid_storage_manager.util.Conditions;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;
import studio.fantasyit.maid_storage_manager.util.MoveUtil;
import studio.fantasyit.maid_storage_manager.util.PosUtil;

/**
 * 正常情况下尝试前往附近所有的箱子
 */
public class ViewMoveBehavior extends MaidMoveToBlockTask {
    public ViewMoveBehavior() {
        super((float) Config.viewSpeed, 3);
        this.verticalSearchStart = 1;
        this.setMaxCheckRate(100);
    }

    Pair<ResourceLocation, BlockPos> chestPos = null;

    @Override
    protected boolean checkExtraStartConditions(@NotNull ServerLevel worldIn, @NotNull EntityMaid owner) {
        if (!super.checkExtraStartConditions(worldIn, owner)) return false;
        if (MemoryUtil.getCurrentlyWorking(owner) != ScheduleBehavior.Schedule.VIEW) return false;
        if (Conditions.isWaitingForReturn(owner)) return true;
        if (Conditions.takingRequestList(owner)) return false;
        return Conditions.isInvEmpty(owner);
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long p_22542_) {
        super.start(level, maid, p_22542_);
        this.searchForDestination(level, maid);
        if (!maid.getBrain().hasMemoryValue(InitEntities.TARGET_POS.get())) {
            MemoryUtil.getViewedInventory(maid).removeUnvisited();
            MemoryUtil.getViewedInventory(maid).resetVisitedPos();
            DebugData.getInstance().sendMessage("[VIEW]Reset, waiting");
        } else {
            if (chestPos != null) {
                MemoryUtil.getViewedInventory(maid).setTarget(chestPos.getA(), chestPos.getB());
                maid.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(chestPos.getB()));
            }
        }
    }

    @Override
    protected boolean shouldMoveTo(@NotNull ServerLevel serverLevel,
                                   EntityMaid entityMaid,
                                   @NotNull BlockPos blockPos) {
        if (!PosUtil.isSafePos(serverLevel, blockPos)) return false;
        //寻找当前格子能触碰的箱子
        Pair<ResourceLocation, BlockPos> canTouchChest = MoveUtil.findTargetForPos(serverLevel,
                entityMaid,
                blockPos,
                MemoryUtil.getViewedInventory(entityMaid));
        if (canTouchChest != null) {
            DebugData.getInstance().sendMessage("[VIEW]Target %s (%s)",
                    canTouchChest.getB().toShortString(),
                    canTouchChest.getA().toString()
            );
            chestPos = canTouchChest;
            return true;
        }
        return false;
    }
}
