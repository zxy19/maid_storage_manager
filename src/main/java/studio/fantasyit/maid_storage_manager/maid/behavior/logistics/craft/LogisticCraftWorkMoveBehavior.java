package studio.fantasyit.maid_storage_manager.maid.behavior.logistics.craft;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import org.jetbrains.annotations.NotNull;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftLayer;
import studio.fantasyit.maid_storage_manager.debug.DebugData;
import studio.fantasyit.maid_storage_manager.maid.behavior.ScheduleBehavior;
import studio.fantasyit.maid_storage_manager.maid.memory.LogisticsMemory;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;

/**
 * 合成工作1
 */
public class LogisticCraftWorkMoveBehavior extends Behavior<EntityMaid> {
    public LogisticCraftWorkMoveBehavior() {
        super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT, InitEntities.TARGET_POS.get(), MemoryStatus.VALUE_ABSENT));
    }

    Target target;

    @Override
    protected boolean checkExtraStartConditions(@NotNull ServerLevel worldIn, @NotNull EntityMaid owner) {
        if (MemoryUtil.getCurrentlyWorking(owner) != ScheduleBehavior.Schedule.LOGISTICS) return false;
        if (!MemoryUtil.getLogistics(owner).shouldWork()) return false;
        if (MemoryUtil.getLogistics(owner).getStage() != LogisticsMemory.Stage.CRAFT) return false;
        return true;
    }


    @Override
    protected void start(ServerLevel level, EntityMaid maid, long p_22542_) {
        CraftLayer layer = MemoryUtil.getLogistics(maid).getCraftLayer();
        if (layer == null) {
            MemoryUtil.getLogistics(maid).setStage(LogisticsMemory.Stage.OUTPUT);
            MemoryUtil.getLogistics(maid).clearTarget();
            MemoryUtil.clearTarget(maid);
            return;
        }
        //无配方层。属于不需要进行合成的操作，直接进入Output环节
        CraftGuideStepData step = layer.getStepData();
        if (step == null) {
            MemoryUtil.getLogistics(maid).setStage(LogisticsMemory.Stage.OUTPUT);
            MemoryUtil.getLogistics(maid).clearTarget();
            MemoryUtil.clearTarget(maid);
            return;
        }
        Target storage = step.getStorage();

        DebugData.getInstance().sendMessage(
                String.format("[REQUEST_CRAFT_WORK]Step %d [%d/%d], %s",
                        layer.getStep(),
                        layer.getDoneCount(),
                        layer.getCount(),
                        storage
                )
        );
        BlockPos blockPos = step.actionType.pathFindingTargetProvider().find(maid, layer.getCraftData().get(), step, layer);
        if (blockPos != null) {
            MemoryUtil.setTarget(maid, blockPos, (float) Config.craftWorkSpeed);
            MemoryUtil.getLogistics(maid).setTarget(storage);
            MemoryUtil.setLookAt(maid, storage.getPos());
            MemoryUtil.getCrafting(maid).resetPathFindingFailCount();
        } else {
            MemoryUtil.getCrafting(maid).addPathFindingFailCount();
            if (MemoryUtil.getCrafting(maid).getPathFindingFailCount() > 200) {
                DebugData.getInstance().sendMessage("[LOGISTIC_CRAFT_WORK]Path finding fail.");
                MemoryUtil.getLogistics(maid).setStage(LogisticsMemory.Stage.OUTPUT);
                MemoryUtil.getLogistics(maid).clearTarget();
                MemoryUtil.clearTarget(maid);
                MemoryUtil.getCrafting(maid).resetPathFindingFailCount();
            }
        }
    }
}