package studio.fantasyit.maid_storage_manager.craft.context;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.work.CraftLayer;

abstract public class AbstractDynamicAddedAction extends AbstractCraftActionContext {

    public AbstractDynamicAddedAction(EntityMaid maid, CraftGuideData craftGuideData, CraftGuideStepData craftGuideStepData, CraftLayer layer) {
        super(maid, craftGuideData, craftGuideStepData, layer);
    }

    int idx = -1;
    boolean started = false;

    abstract public Result getList();

    protected void addStep(CraftGuideStepData stepData) {
        craftLayer.addStep(stepData);
    }

    @Override
    public Result start() {
        if (craftLayer.getTotalStep() > 1) {
            craftLayer.clearNonFirstStep();
        }
        return getList();
    }

    @Override
    public Result tick() {
        return Result.SUCCESS;
    }

    @Override
    public void stop() {
    }
}
