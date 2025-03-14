package studio.fantasyit.maid_storage_manager.maid.behavior.view;

import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidMoveToBlockTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.debug.DebugData;
import studio.fantasyit.maid_storage_manager.items.RequestListItem;
import studio.fantasyit.maid_storage_manager.maid.behavior.ScheduleBehavior;
import studio.fantasyit.maid_storage_manager.maid.memory.ViewedInventoryMemory;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;
import studio.fantasyit.maid_storage_manager.storage.MaidStorage;
import studio.fantasyit.maid_storage_manager.storage.Storage;
import studio.fantasyit.maid_storage_manager.util.Conditions;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;
import studio.fantasyit.maid_storage_manager.util.MoveUtil;
import studio.fantasyit.maid_storage_manager.util.PosUtil;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 正常情况下尝试前往附近所有的箱子
 */
public class ViewMoveBehavior extends MaidMoveToBlockTask {
    public ViewMoveBehavior() {
        super((float) Config.viewSpeed, 3);
        this.verticalSearchStart = 1;
        this.setMaxCheckRate(100);
    }

    Storage chestPos = null;

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
        if (!priorityTarget(level, maid))
            this.searchForDestination(level, maid);
        if (!maid.getBrain().hasMemoryValue(InitEntities.TARGET_POS.get())) {
            if (MemoryUtil.getViewedInventory(maid).confirmNoTarget(2)) {
                MemoryUtil.getViewedInventory(maid).removeUnvisited();
                MemoryUtil.getViewedInventory(maid).resetVisitedPos();
                DebugData.getInstance().sendMessage("[VIEW]Reset, waiting");
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
        Storage toCheckTarget = MemoryUtil.getViewedInventory(maid).getMarkChanged().peek();
        if (toCheckTarget != null) {
            @Nullable Storage storage = MaidStorage.getInstance().isValidTarget(level, maid, toCheckTarget.getPos(), toCheckTarget.side);
            if (storage != null) {
                @Nullable BlockPos target = MoveUtil.selectPosForTarget(level, maid, toCheckTarget.getPos());
                if (target != null) {
                    chestPos = storage;
                    MemoryUtil.setTarget(maid, target, (float) Config.viewChangeSpeed);
                    DebugData.getInstance().sendMessage("[VIEW]Priority By Change %s", storage);
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
        Storage canTouchChest = MoveUtil.findTargetForPos(serverLevel,
                entityMaid,
                blockPos,
                MemoryUtil.getViewedInventory(entityMaid));
        if (canTouchChest != null) {
            DebugData.getInstance().sendMessage("[VIEW]Target %s", canTouchChest);
            chestPos = canTouchChest;
            return true;
        }
        return false;
    }
}
