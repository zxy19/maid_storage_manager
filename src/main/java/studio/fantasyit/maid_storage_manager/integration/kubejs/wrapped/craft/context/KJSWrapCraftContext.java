package studio.fantasyit.maid_storage_manager.integration.kubejs.wrapped.craft.context;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import studio.fantasyit.maid_storage_manager.craft.context.AbstractCraftActionContext;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftLayer;
import studio.fantasyit.maid_storage_manager.integration.kubejs.helper.CraftContextOperator;

public class KJSWrapCraftContext extends AbstractCraftActionContext {
    private final IKJSCraftContext context;
    CraftContextOperator operator;

    public KJSWrapCraftContext(EntityMaid maid, CraftGuideData craftGuideData, CraftGuideStepData craftGuideStepData, CraftLayer layer,  IKJSCraftContext context) {
        super(maid, craftGuideData, craftGuideStepData, layer);
        this.context = context;
        operator = new CraftContextOperator(maid, craftGuideStepData);
    }

    @Override
    public Result start() {
        return context.start(maid, craftGuideData, craftGuideStepData, craftLayer,operator);
    }

    @Override
    public Result tick() {
        return context.tick(maid, craftGuideData, craftGuideStepData, craftLayer,operator);
    }

    @Override
    public void stop() {
        context.stop(maid, craftGuideData, craftGuideStepData, craftLayer,operator);
    }
}
