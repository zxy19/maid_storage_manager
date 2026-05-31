package studio.fantasyit.maid_storage_manager.integration.kubejs.helper;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.server.level.ServerLevel;
import studio.fantasyit.maid_storage_manager.craft.context.AbstractCraftActionContext;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.util.MoveUtil;

import java.util.Objects;

public class CraftContextOperator {
    public AbstractCraftActionContext.Result getSuccess() {
        return AbstractCraftActionContext.Result.SUCCESS;
    }

    public AbstractCraftActionContext.Result getFail() {
        return AbstractCraftActionContext.Result.FAIL;
    }

    public AbstractCraftActionContext.Result getContinue() {
        return AbstractCraftActionContext.Result.CONTINUE;
    }

    public AbstractCraftActionContext.Result getNotDone() {
        return AbstractCraftActionContext.Result.NOT_DONE;
    }

    public CraftContextOperator(EntityMaid maid, CraftGuideStepData craftGuideStepData) {
        this.maid = maid;
        this.craftGuideStepData = craftGuideStepData;
    }

    EntityMaid maid;
    CraftGuideStepData craftGuideStepData;

    public boolean moveIfNotArrive() {
        return MoveUtil.setMovementIfNotReached(maid);
    }

    public boolean moveIfCollide() {
        return MoveUtil.setMovementIfColliedTarget((ServerLevel) maid.level(), maid, craftGuideStepData.storage);
    }

    public boolean notStopped() {
        return (maid.getDeltaMovement().length() > 0.1);
    }

    public int getTickCount() {
        return Objects.requireNonNull(maid.level().getServer()).getTickCount();
    }
}
