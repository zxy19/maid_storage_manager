package studio.fantasyit.maid_storage_manager.integration.kubejs.wrapped.craft.context;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import studio.fantasyit.maid_storage_manager.craft.context.AbstractCraftActionContext;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.work.CraftLayer;
import studio.fantasyit.maid_storage_manager.integration.kubejs.helper.CraftContextOperator;

public interface IKJSCraftContext {
    AbstractCraftActionContext.Result start(EntityMaid maid, CraftGuideData craftGuideData, CraftGuideStepData craftGuideStepData, CraftLayer layer, CraftContextOperator operator);

    AbstractCraftActionContext.Result tick(EntityMaid maid, CraftGuideData craftGuideData, CraftGuideStepData craftGuideStepData, CraftLayer layer, CraftContextOperator operator);

    void stop(EntityMaid maid, CraftGuideData craftGuideData, CraftGuideStepData craftGuideStepData, CraftLayer layer, CraftContextOperator operator);
}
