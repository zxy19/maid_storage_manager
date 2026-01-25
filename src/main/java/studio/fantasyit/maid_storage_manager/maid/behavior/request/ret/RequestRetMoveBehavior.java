package studio.fantasyit.maid_storage_manager.maid.behavior.request.ret;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.MaidPathFindingBFS;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.craft.debug.ProgressDebugContext;
import studio.fantasyit.maid_storage_manager.debug.DebugData;
import studio.fantasyit.maid_storage_manager.items.RequestListItem;
import studio.fantasyit.maid_storage_manager.maid.behavior.ScheduleBehavior;
import studio.fantasyit.maid_storage_manager.storage.MaidStorage;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.util.*;

import java.util.Map;
import java.util.UUID;

/**
 * 当女仆背包装满或者任务完成，计划回到存储标记点
 */
public class RequestRetMoveBehavior extends Behavior<EntityMaid> {

    public RequestRetMoveBehavior() {
        super(Map.of(
                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT,
                InitEntities.TARGET_POS.get(), MemoryStatus.VALUE_ABSENT
        ));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        if (MemoryUtil.getCurrentlyWorking(maid) != ScheduleBehavior.Schedule.REQUEST) return false;
        if (!Conditions.takingRequestList(maid)) return false;

        //由其他模式发起的返回存储请求。
        if (MemoryUtil.getRequestProgress(maid).isReturning()) return true;


        if (MemoryUtil.getRequestProgress(maid).isCheckingStock()) return false;
        if (MemoryUtil.getRequestProgress(maid).isTryCrafting()) return false;
        return Conditions.listAllDone(maid) || Conditions.inventoryFull(maid);
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long p_22542_) {
        //背包压根没东西，而且是要回去算合成的，那就别回去了，就地进行下一步即可
        if (Conditions.isNothingToPlace(maid) && MemoryUtil.getRequestProgress(maid).isTryCrafting() && !MemoryUtil.getCrafting(maid).hasPlan()) {
            MemoryUtil.getRequestProgress(maid).setReturn(false);
            return;
        }

        @Nullable Target target = RequestListItem.getStorageBlock(maid.getMainHandItem());
        @Nullable Target storage = target == null ? null : MaidStorage.getInstance().isValidTarget(level, maid, target.getPos(), target.side);
        @Nullable UUID uuid = RequestListItem.getStorageEntity(maid.getMainHandItem());
        @Nullable Entity entity = uuid == null ? null : level.getEntity(uuid);
        if (target != null && storage != null) {
            //寻找落脚点
            BlockPos goal = MoveUtil.selectPosForTarget(level, maid, target.pos);

            if (goal == null) {
                DebugData.sendDebug(maid, ProgressDebugContext.TYPE.MOVE, "[REQUEST_RET] Unavailable target, waiting");
                return;
            }
            DebugData.sendDebug(maid, ProgressDebugContext.TYPE.MOVE, "[REQUEST_RET] Return target %s", storage);

            MemoryUtil.setTarget(maid, goal, (float) Config.collectSpeed);
            MemoryUtil.getRequestProgress(maid).setReturn();
            MemoryUtil.getRequestProgress(maid).setTarget(storage);
        } else if (entity != null) {
            setEntityTarget(level, maid, entity);
            MemoryUtil.getRequestProgress(maid).setReturn();
            MemoryUtil.getRequestProgress(maid).setTargetEntity(entity.getUUID());
        } else {
            //如果没有绑定存储位置，那么直接停止任务，扔掉或者存储清单，三十秒后进行日常工作
            if (!MemoryUtil.getRequestProgress(maid).isTryCrafting()) {
                DebugData.sendDebug(maid, ProgressDebugContext.TYPE.MOVE, "[REQUEST_RET] No target");
                RequestListItem.markAllDone(maid.getMainHandItem());
                RequestItemUtil.stopJobAndStoreOrThrowItem(maid, null, null);
                MemoryUtil.setReturnToScheduleAt(maid, level.getServer().getTickCount() + 600);
            } else {
                MemoryUtil.getRequestProgress(maid).setReturn(false);
                MemoryUtil.getRequestProgress(maid).clearTarget();
                MemoryUtil.getCrafting(maid).clearTarget();
                MemoryUtil.clearTarget(maid);
            }
        }
    }

    private void setEntityTarget(ServerLevel level, EntityMaid maid, @Nullable Entity entity) {
        if (!(entity instanceof EntityMaid entityMaid)) {
            MemoryUtil.setTarget(maid, entity, (float) Config.collectSpeed);
            return;
        }
        float restrictRadiusOwner = maid.hasRestriction() ? maid.getRestrictRadius() : 5;
        MaidPathFindingBFS pathFinding = new MaidPathFindingBFS(maid.getNavigation().getNodeEvaluator(), level, maid, restrictRadiusOwner + 2, (int) (restrictRadiusOwner + 2));
        if (pathFinding.canPathReach(entityMaid.blockPosition())) {
            MemoryUtil.setTarget(maid, entity, (float) Config.collectSpeed);
        } else {
            Pair<BlockPos, BlockPos> bbp = PosUtil.pickMeetingPosPair(maid, entityMaid, pathFinding);
            if (bbp == null) return;
            MemoryUtil.setTarget(maid, bbp.getA(), (float) Config.collectSpeed);
            MemoryUtil.setTarget(entityMaid, bbp.getB(), (float) Config.collectSpeed);
        }
    }
}
