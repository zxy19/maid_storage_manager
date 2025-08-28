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
                          boolean noOccupation,
                          int inputCount,
                          int outputCount,
                          List<ActionOption<?>> options
) {
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

    public <T> Optional<T> getOptionSelection(ActionOption<T> option, CraftGuideStepData craftGuideStepData) {
        CompoundTag extraData = craftGuideStepData.getExtraData();
        if (extraData.contains(option.id().toString()) && extraData.getCompound(option.id().toString()).contains("selection")) {
            return Optional.of(option.converter().apply(extraData.getCompound(option.id().toString()).getInt("selection")));
        }
        return Optional.empty();
    }

    public String getOptionValue(ActionOption<?> option, CraftGuideStepData craftGuideStepData) {
        CompoundTag extraData = craftGuideStepData.getExtraData();
        if (extraData.contains(option.id().toString()) && extraData.getCompound(option.id().toString()).contains("value")) {
            return extraData.getCompound(option.id().toString()).getString("value");
        }
        return option.defaultValue();
    }

    public void setOptionSelection(ActionOption<?> option, CraftGuideStepData craftGuideStepData, int selection) {
        CompoundTag extraData = craftGuideStepData.getExtraData();
        if (!extraData.contains(option.id().toString()))
            extraData.put(option.id().toString(), new CompoundTag());
        extraData.getCompound(option.id().toString()).putInt("selection", selection);
        craftGuideStepData.setExtraData(extraData);
    }

    public void setOptionValue(ActionOption<?> option, CraftGuideStepData craftGuideStepData, String value) {
        CompoundTag extraData = craftGuideStepData.getExtraData();
        if (!extraData.contains(option.id().toString()))
            extraData.put(option.id().toString(), new CompoundTag());
        extraData.getCompound(option.id().toString()).putString("value", value);
        craftGuideStepData.setExtraData(extraData);
    }
}