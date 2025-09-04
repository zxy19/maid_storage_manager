package studio.fantasyit.maid_storage_manager.craft.action;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.MaidPathFindingBFS;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import studio.fantasyit.maid_storage_manager.craft.context.AbstractCraftActionContext;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.work.CraftLayer;

import java.util.List;

public record CraftAction(ResourceLocation type, CraftActionProvider provider,
                          CraftActionPathFindingTargetProvider pathFindingTargetProvider,
                          double pathCloseEnoughThreshold,
                          boolean canBeCommon,
                          long marks,
                          int inputCount,
                          int outputCount,
                          List<ActionOption<?>> options
) {
    public static final long MARK_NO_MARKS = 0L;
    public static final long MARK_NO_OCCUPATION = 1L;
    public static final long MARK_HAND_RELATED = 2L;

    public boolean hasOption(ActionOption<?> optional) {
        return options.stream().anyMatch(o -> o.id().equals(optional.id()));
    }

    public boolean noOccupation() {
        return hasMark(MARK_NO_OCCUPATION);
    }

    public int getOptionIndex(ActionOption<?> option) {
        for (int i = 0; i < options.size(); i++) {
            if (options.get(i).id().equals(option.id()))
                return i;
        }
        throw new IllegalArgumentException("Option " + option.id() + " not found on action " + type);
    }


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

    public boolean hasMark(long mark) {
        return (marks & mark) != 0;
    }
}