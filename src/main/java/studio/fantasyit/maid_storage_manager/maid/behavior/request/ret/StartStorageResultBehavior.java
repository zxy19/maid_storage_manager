package studio.fantasyit.maid_storage_manager.maid.behavior.request.ret;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.items.RequestListItem;
import studio.fantasyit.maid_storage_manager.util.Conditions;
import studio.fantasyit.maid_storage_manager.util.InvUtil;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;
import studio.fantasyit.maid_storage_manager.util.PosUtil;

import java.util.Map;

/**
 * 当女仆背包装满或者任务完成，计划回到存储标记点
 */
public class StartStorageResultBehavior extends Behavior<EntityMaid> {

    public StartStorageResultBehavior() {
        super(Map.of(
                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT,
                InitEntities.TARGET_POS.get(), MemoryStatus.VALUE_ABSENT
        ));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        if (!MemoryUtil.isWorkingRequest(maid)) return false;

        if (!Conditions.takingRequestList(maid)) return false;
        if (Conditions.inventoryFull(maid)) return true;
        if (Conditions.listAllDone(maid)) return true;
        if (Conditions.finishAll(maid)) {
            RequestListItem.markAllDone(maid.getMainHandItem());
            return true;
        }
        return false;
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long p_22542_) {
        @Nullable BlockPos target = RequestListItem.getStorageBlock(maid.getMainHandItem());
        if (target == null) {
            if (Conditions.listNotDone(maid))
                RequestListItem.markAllDone(maid.getMainHandItem());
            //需求清单未能正确插入目标容器
            //设置清单为工作忽略模式，并尝试放置到背包中
            ItemStack reqList = maid.getMainHandItem();
            CompoundTag tag = reqList.getOrCreateTag();
            tag.putBoolean(RequestListItem.TAG_IGNORE_TASK, true);
            reqList.setTag(tag);
            if (!InvUtil.tryPlace(maid.getAvailableBackpackInv(), reqList).isEmpty()) {
                //背包也没空。。扔地上站未来
                ItemEntity itementity = new ItemEntity(level, maid.getX(), maid.getY(), maid.getZ(), reqList);
                maid.getMaxHeadXRot();
                Vec3 direction = Vec3.directionFromRotation(maid.getXRot(), maid.getYRot()).normalize().scale(0.5);
                itementity.setDeltaMovement(direction);
                itementity.setUnlimitedLifetime();
                level.addFreshEntity(itementity);
            }
            MemoryUtil.setWorkingRequest(maid, false);
            MemoryUtil.clearReturnToStorage(maid);
            MemoryUtil.clearPosition(maid);
            MemoryUtil.clearCurrentChestPos(maid);
            maid.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            MemoryUtil.setReturnToScheduleAt(maid, level.getServer().getTickCount() + 600);
            return;
        }
        BlockPos goal = PosUtil.findAroundUpAndDown(target,
                pos -> maid.isWithinRestriction(pos) && maid.canPathReach(pos));

        if (goal == null) return;

        BehaviorUtils.setWalkAndLookTargetMemories(maid, goal, (float) Config.collectSpeed, 0);
        MemoryUtil.setTargetingPos(maid, goal);
        MemoryUtil.returnToStorage(maid);
        MemoryUtil.clearArriveTarget(maid);
        MemoryUtil.clearCurrentChestPos(maid);
    }
}
