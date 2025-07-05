package studio.fantasyit.maid_storage_manager.craft.action;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.MaidPathFindingBFS;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import studio.fantasyit.maid_storage_manager.craft.context.AbstractCraftActionContext;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.work.CraftLayer;

public record CraftAction(ResourceLocation type, CraftActionProvider provider,
                          CraftActionPathFindingTargetProvider pathFindingTargetProvider,
                          double pathCloseEnoughThreshold,
                          boolean canBeCommon,
                          boolean noOccupation,
                          int inputCount,
                          int outputCount) {
    @FunctionalInterface
    public interface CraftActionProvider {
        AbstractCraftActionContext create(EntityMaid maid, CraftGuideData craftGuideData, CraftGuideStepData craftGuideStepData, CraftLayer layer);
    }

    @FunctionalInterface
    public interface CraftActionPathFindingTargetProvider {
        BlockPos find(EntityMaid maid, CraftGuideData craftGuideData, CraftGuideStepData craftGuideStepData, CraftLayer layer, MaidPathFindingBFS pathFinding);
    }

    public enum PathEnoughLevel {
        NORMAL(2),
        CLOSER(1.2),
        VERY_CLOSE(0.8);

        public final double value;

        PathEnoughLevel(double value) {
            this.value = value;
        }
    }
}