package studio.fantasyit.maid_storage_manager.maid.behavior.logistics.input;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftLayer;
import studio.fantasyit.maid_storage_manager.items.LogisticsGuide;
import studio.fantasyit.maid_storage_manager.maid.behavior.ScheduleBehavior;
import studio.fantasyit.maid_storage_manager.maid.memory.LogisticsMemory;
import studio.fantasyit.maid_storage_manager.storage.MaidStorage;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;
import studio.fantasyit.maid_storage_manager.util.MoveUtil;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class LogisticsInputMoveBehavior extends Behavior<EntityMaid> {
    public LogisticsInputMoveBehavior() {
        super(Map.of());
    }


    @Override
    protected boolean checkExtraStartConditions(@NotNull ServerLevel level, @NotNull EntityMaid maid) {
        if (MemoryUtil.getCurrentlyWorking(maid) != ScheduleBehavior.Schedule.LOGISTICS) return false;
        return MemoryUtil.getLogistics(maid).shouldWork() && MemoryUtil.getLogistics(maid).getStage() == LogisticsMemory.Stage.INPUT;
    }


    @Override
    protected void start(@NotNull ServerLevel level, @NotNull EntityMaid maid, long gameTimeIn) {
        @Nullable Target target = LogisticsGuide.getInput(MemoryUtil.getLogistics(maid).getCurrentLogisticsGuideItem());
        @Nullable Target storage = target == null ? null : MaidStorage.getInstance().isValidTarget(level, maid, target.getPos(), target.side);

        if (target != null && storage != null) {
            //寻找落脚点
            BlockPos goal = MoveUtil.selectPosForTarget(level, maid, target.pos);

            if (goal != null) {
                MemoryUtil.getLogistics(maid).setTarget(storage);
                MemoryUtil.setTarget(maid, goal, (float) Config.collectSpeed);
                return;
            }
        } else {
            CraftGuideData craftGuideData = LogisticsGuide.getCraftGuideData(MemoryUtil.getLogistics(maid).getCurrentLogisticsGuideItem());
            if (craftGuideData.getInput().size() == 0) {
                MemoryUtil.getLogistics(maid).setCraftAndResultLayer(
                        new CraftLayer(Optional.of(craftGuideData), List.of(), 1),
                        new CraftLayer(Optional.empty(), craftGuideData.getAllOutputItems(), 1)
                );
                MemoryUtil.getLogistics(maid).setStage(LogisticsMemory.Stage.CRAFT);
            } else {
                MemoryUtil.getLogistics(maid).setStage(LogisticsMemory.Stage.FINISH);
            }
        }
    }
}
