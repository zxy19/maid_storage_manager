package studio.fantasyit.maid_storage_manager.integration.kubejs.wrapped.craft.context;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.NativeObject;
import dev.latvian.mods.rhino.type.TypeInfo;
import studio.fantasyit.maid_storage_manager.craft.context.AbstractCraftActionContext;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.work.CraftLayer;
import studio.fantasyit.maid_storage_manager.integration.kubejs.helper.CraftContextOperator;
import studio.fantasyit.maid_storage_manager.integration.kubejs.wrapped.base.AbstractObjectWrapped;

public class KJSCraftContext extends AbstractObjectWrapped implements IKJSCraftContext {


    public KJSCraftContext(NativeObject obj, Context context, TypeInfo typeInfo) {
        super(obj, context, typeInfo);
    }

    @Override
    public AbstractCraftActionContext.Result start(EntityMaid maid, CraftGuideData craftGuideData, CraftGuideStepData craftGuideStepData, CraftLayer layer, CraftContextOperator operator) {
        return get("start", this.classTest(AbstractCraftActionContext.Result.class),
                maid, craftGuideData, craftGuideStepData, layer,operator)
                .orElseThrow();
    }

    @Override
    public AbstractCraftActionContext.Result tick(EntityMaid maid, CraftGuideData craftGuideData, CraftGuideStepData craftGuideStepData, CraftLayer layer, CraftContextOperator operator) {
        return get("tick", this.classTest(AbstractCraftActionContext.Result.class),
                maid, craftGuideData, craftGuideStepData, layer,operator)
                .orElseThrow();
    }

    @Override
    public void stop(EntityMaid maid, CraftGuideData craftGuideData, CraftGuideStepData craftGuideStepData, CraftLayer layer, CraftContextOperator operator) {
        get("stop", this::any, maid, craftGuideData, craftGuideStepData, layer,operator);
    }
}
