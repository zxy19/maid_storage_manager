package studio.fantasyit.maid_storage_manager.craft.action;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.MaidPathFindingBFS;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import studio.fantasyit.maid_storage_manager.craft.context.AbstractCraftActionContext;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.work.CraftLayer;

import java.util.List;
import java.util.Optional;

public record CraftAction(ResourceLocation type, CraftActionProvider provider,
                          CraftActionPathFindingTargetProvider pathFindingTargetProvider,
                          double pathCloseEnoughThreshold,
                          boolean canBeCommon,
                          long marks,
                          int inputCount,
                          int outputCount,
                          List<ActionOption<?>> options
) {
    public static final long NO_MARKS = 0L;
    public static final long NO_OCCUPATION = 1L;
    public static final long HAND_RELATED = 2L;

    public boolean hasOption(ActionOption<?> optional) {
        return options.stream().anyMatch(o -> o.id().equals(optional.id()));
    }

    public boolean noOccupation() {
        return hasMark(NO_OCCUPATION);
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

    public void assertValid(ActionOption<?> option, CraftGuideStepData craftGuideStepData) {
        if (!craftGuideStepData.action.equals(type))
            throw new IllegalArgumentException("CraftGuideStep is not " + craftGuideStepData.action);
        if (options.stream().noneMatch(o -> o.id().equals(option.id())))
            throw new IllegalArgumentException("Option " + option.id() + " not found on action " + type);
    }

    public <T> Optional<T> getOptionSelection(ActionOption<T> option, CraftGuideStepData craftGuideStepData) {
        assertValid(option, craftGuideStepData);
        CompoundTag extraData = craftGuideStepData.getExtraData();
        if (extraData.contains(option.id().toString()) && extraData.getCompound(option.id().toString()).contains("selection")) {
            return Optional.of(option.converter().ab(extraData.getCompound(option.id().toString()).getInt("selection")));
        }
        return Optional.empty();
    }

    public Optional<Integer> getOptionSelectionId(ActionOption<?> option, CraftGuideStepData craftGuideStepData) {
        assertValid(option, craftGuideStepData);
        CompoundTag extraData = craftGuideStepData.getExtraData();
        if (extraData.contains(option.id().toString()) && extraData.getCompound(option.id().toString()).contains("selection")) {
            return Optional.of(extraData.getCompound(option.id().toString()).getInt("selection"));
        }
        return Optional.empty();
    }

    public String getOptionValue(ActionOption<?> option, CraftGuideStepData craftGuideStepData) {
        assertValid(option, craftGuideStepData);
        CompoundTag extraData = craftGuideStepData.getExtraData();
        if (extraData.contains(option.id().toString()) && extraData.getCompound(option.id().toString()).contains("value")) {
            return extraData.getCompound(option.id().toString()).getString("value");
        }
        return option.defaultValue();
    }

    public <T> void setOptionSelection(ActionOption<T> option, CraftGuideStepData craftGuideStepData, T selection) {
        assertValid(option, craftGuideStepData);
        setOptionSelectionId(option, craftGuideStepData, option.converter().ba(selection));
    }

    public void setOptionSelectionId(ActionOption<?> option, CraftGuideStepData craftGuideStepData, int selection) {
        assertValid(option, craftGuideStepData);
        CompoundTag extraData = craftGuideStepData.getExtraData();
        if (!extraData.contains(option.id().toString()))
            extraData.put(option.id().toString(), new CompoundTag());
        extraData.getCompound(option.id().toString()).putInt("selection", selection);
        craftGuideStepData.setExtraData(extraData);
    }

    public void setOptionValue(ActionOption<?> option, CraftGuideStepData craftGuideStepData, String value) {
        assertValid(option, craftGuideStepData);
        CompoundTag extraData = craftGuideStepData.getExtraData();
        if (!extraData.contains(option.id().toString()))
            extraData.put(option.id().toString(), new CompoundTag());
        extraData.getCompound(option.id().toString()).putString("value", value);
        craftGuideStepData.setExtraData(extraData);
    }

    public boolean hasMark(long mark) {
        return (marks & mark) != 0;
    }
}