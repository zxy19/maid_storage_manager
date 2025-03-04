package studio.fantasyit.maid_storage_manager.maid.behavior.request.ret;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.debug.DebugData;
import studio.fantasyit.maid_storage_manager.items.RequestListItem;
import studio.fantasyit.maid_storage_manager.maid.behavior.ScheduleBehavior;
import studio.fantasyit.maid_storage_manager.storage.MaidStorage;
import studio.fantasyit.maid_storage_manager.storage.Storage;
import studio.fantasyit.maid_storage_manager.util.Conditions;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;
import studio.fantasyit.maid_storage_manager.util.MoveUtil;
import studio.fantasyit.maid_storage_manager.util.RequestItemUtil;

import java.util.Map;

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
        if (Conditions.listAllDone(maid)) return true;
        return Conditions.inventoryFull(maid);
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long p_22542_) {
        @Nullable Storage target = RequestListItem.getStorageBlock(maid.getMainHandItem());
        @Nullable Storage storage = target == null ? null : MaidStorage.getInstance().isValidTarget(level, maid, target.getPos(), target.side);
        //如果没有绑定存储方块位置，那么直接停止任务，扔掉或者存储清单，三十秒后进行日常工作
        if (target == null || storage == null) {
            DebugData.getInstance().sendMessage("[REQUEST_RET] No target");
            RequestListItem.markAllDone(maid.getMainHandItem());
            RequestItemUtil.stopJobAndStoreOrThrowItem(maid, null);
            MemoryUtil.setReturnToScheduleAt(maid, level.getServer().getTickCount() + 600);
            return;
        }
        //寻找落脚点
        BlockPos goal = MoveUtil.selectPosForTarget(level, maid, target.pos);

        if (goal == null) {
            DebugData.getInstance().sendMessage("[REQUEST_RET] Unavailable target, waiting");
            return;
        }
        DebugData.getInstance().sendMessage("[REQUEST_RET] Return target %s", storage);

        MemoryUtil.setTarget(maid, goal, (float) Config.collectSpeed);
        MemoryUtil.getRequestProgress(maid).setReturn();
        MemoryUtil.getRequestProgress(maid).setTarget(storage);
    }
}
