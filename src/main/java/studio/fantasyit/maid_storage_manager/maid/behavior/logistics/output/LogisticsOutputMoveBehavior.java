package studio.fantasyit.maid_storage_manager.maid.behavior.logistics.output;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.items.LogisticsGuide;
import studio.fantasyit.maid_storage_manager.maid.behavior.ScheduleBehavior;
import studio.fantasyit.maid_storage_manager.maid.memory.LogisticsMemory;
import studio.fantasyit.maid_storage_manager.storage.MaidStorage;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;
import studio.fantasyit.maid_storage_manager.util.MoveUtil;

import java.util.Map;

public class LogisticsOutputMoveBehavior extends Behavior<EntityMaid> {

    public LogisticsOutputMoveBehavior() {
        super(Map.of(
                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT,
                InitEntities.TARGET_POS.get(), MemoryStatus.VALUE_ABSENT
        ));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        if (MemoryUtil.getCurrentlyWorking(maid) != ScheduleBehavior.Schedule.LOGISTICS) return false;
        return MemoryUtil.getLogistics(maid).shouldWork() && MemoryUtil.getLogistics(maid).getStage() == LogisticsMemory.Stage.OUTPUT;
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long p_22542_) {
        @Nullable Target target = LogisticsGuide.getOutput(MemoryUtil.getLogistics(maid).getCurrentLogisticsGuideItem());
        @Nullable Target storage = target == null ? null : MaidStorage.getInstance().isValidTarget(level, maid, target.getPos(), target.side);
        if (target != null && storage != null) {
            //寻找落脚点
            BlockPos goal = MoveUtil.selectPosForTarget(level, maid, target.pos);

            if (goal != null) {
                MemoryUtil.getLogistics(maid).resetFailCount();
                MemoryUtil.getLogistics(maid).setTarget(storage);
                MemoryUtil.setTarget(maid, goal, (float) Config.collectSpeed);
                return;
            }
        }
        MemoryUtil.getLogistics(maid).addFailCount();
        if (MemoryUtil.getLogistics(maid).getFailCount() > 20) {
            MemoryUtil.getLogistics(maid).resetFailCount();
            MemoryUtil.getLogistics(maid).setStage(LogisticsMemory.Stage.RECYCLE);
        }
    }
}