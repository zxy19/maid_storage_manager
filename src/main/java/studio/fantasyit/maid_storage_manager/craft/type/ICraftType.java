package studio.fantasyit.maid_storage_manager.craft.type;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.craft.action.AbstractCraftActionContext;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftLayer;

import java.util.List;

public interface ICraftType {
    ResourceLocation getType();

    default @Nullable List<CraftGuideStepData> getOutputForInputs(List<CraftGuideStepData> inputs) {
        return null;
    }

    default int maxInputCount() {
        return -1;
    }

    default int maxOutputCount() {
        return -1;
    }

    @Nullable AbstractCraftActionContext start(EntityMaid maid, CraftGuideData craftGuideData, CraftGuideStepData craftGuideStepData, CraftLayer layer);
}
