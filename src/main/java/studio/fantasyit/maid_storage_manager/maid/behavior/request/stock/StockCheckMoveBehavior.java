package studio.fantasyit.maid_storage_manager.maid.behavior.request.stock;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.MaidPathFindingBFS;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.debug.DebugData;
import studio.fantasyit.maid_storage_manager.items.RequestListItem;
import studio.fantasyit.maid_storage_manager.maid.behavior.ScheduleBehavior;
import studio.fantasyit.maid_storage_manager.storage.MaidStorage;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.util.Conditions;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;
import studio.fantasyit.maid_storage_manager.util.MoveUtil;

import java.util.Map;

/**
 * 当女仆背包装满或者任务完成，计划回到存储标记点
 */
public class StockCheckMoveBehavior extends Behavior<EntityMaid> {

    public StockCheckMoveBehavior() {
        super(Map.of(
                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT,
                InitEntities.TARGET_POS.get(), MemoryStatus.VALUE_ABSENT
        ));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        if (MemoryUtil.getCurrentlyWorking(maid) != ScheduleBehavior.Schedule.REQUEST) return false;
        if (!Conditions.takingRequestList(maid)) return false;

        return Conditions.shouldCheckStock(maid);
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long p_22542_) {
        @Nullable Target target = RequestListItem.getStorageBlock(maid.getMainHandItem());
        @Nullable Target storage = target == null ? null : MaidStorage.getInstance().isValidTarget(level, maid, target.getPos(), target.side);
        if (target == null || storage == null) {
            DebugData.sendDebug("[STOCK_CHECK] No target");
        } else {
            MaidPathFindingBFS pathFinding = new MaidPathFindingBFS(maid.getNavigation().getNodeEvaluator(), level, maid);
            BlockPos goal = MoveUtil.selectPosForTarget(level, maid, target.pos);
            if (goal == null) {
                DebugData.sendDebug("[STOCK_CHECK] Unavailable target, waiting");
                return;
            }
            DebugData.sendDebug("[STOCK_CHECK] Return target %s", storage);
            MemoryUtil.setTarget(maid, goal, (float) Config.collectSpeed);
            MemoryUtil.getRequestProgress(maid).setCheckingStock(true);
            MemoryUtil.getRequestProgress(maid).setTarget(storage);
        }
    }
}
