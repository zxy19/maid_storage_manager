package studio.fantasyit.maid_storage_manager.maid.behavior.view;

import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidMoveToBlockTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.jetbrains.annotations.NotNull;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.items.RequestListItem;
import studio.fantasyit.maid_storage_manager.util.Conditions;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;
import studio.fantasyit.maid_storage_manager.util.PosUtil;

/**
 * 正常情况下尝试前往附近所有的箱子
 */
public class ViewChestMoveBehavior extends MaidMoveToBlockTask {
    public ViewChestMoveBehavior() {
        super((float) Config.viewSpeed, 3);
        this.verticalSearchStart = 1;
        this.setMaxCheckRate(100);
    }

    BlockPos chestPos = null;

    @Override
    protected boolean checkExtraStartConditions(@NotNull ServerLevel worldIn, @NotNull EntityMaid owner) {
        if (!super.checkExtraStartConditions(worldIn, owner)) return false;
        if (Conditions.isWaitingForReturn(owner)) return false;
        if (MemoryUtil.isWorkingRequest(owner)) return false;
        if (Conditions.alreadyArriveTarget(owner)) return false;
        return true;
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long p_22542_) {
        super.start(level, maid, p_22542_);
        this.searchForDestination(level, maid);
        if (!maid.getBrain().hasMemoryValue(InitEntities.TARGET_POS.get())) {
            MemoryUtil.clearCurrentChestPos(maid);
            MemoryUtil.getViewedInventory(maid).resetVisitedPos();
        } else {
            if (chestPos != null) {
                MemoryUtil.setCurrentChestPos(maid, chestPos);
                maid.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(chestPos));
            }
        }
    }

    @Override
    protected boolean shouldMoveTo(@NotNull ServerLevel serverLevel,
                                   EntityMaid entityMaid,
                                   @NotNull BlockPos blockPos) {
        if (blockPos.equals(RequestListItem.getStorageBlock(entityMaid.getMainHandItem())))
            return false;

        //寻找当前格子能触碰的箱子
        BlockPos canTouchChest = PosUtil.findAroundFromStandPos(blockPos, (pos) -> {
            BlockEntity blockEntity = serverLevel.getBlockEntity(pos);
            if (blockEntity == null || !blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).isPresent())
                return false;

            if (MemoryUtil.getViewedInventory(entityMaid).isVisitedPos(blockEntity.getBlockPos()))
                return false;

            return true;
        });
        if (canTouchChest != null) {
            chestPos = canTouchChest;
            return true;
        }
        return false;
    }
}
