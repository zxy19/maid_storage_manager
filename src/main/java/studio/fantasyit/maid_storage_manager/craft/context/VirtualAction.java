package studio.fantasyit.maid_storage_manager.craft.context;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftLayer;

public class VirtualAction extends AbstractCraftActionContext {

    public VirtualAction(EntityMaid maid, CraftGuideData craftGuideData, CraftGuideStepData craftGuideStepData, CraftLayer layer) {
        super(maid, craftGuideData, craftGuideStepData, layer);
    }

    @Override
    public Result start() {
        return Result.SUCCESS;
    }

    @Override
    public Result tick() {
        return Result.SUCCESS;
    }

    @Override
    public void stop() {

    }
}
