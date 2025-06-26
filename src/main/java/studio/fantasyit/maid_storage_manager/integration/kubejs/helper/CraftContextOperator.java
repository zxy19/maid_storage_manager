package studio.fantasyit.maid_storage_manager.integration.kubejs.helper;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import studio.fantasyit.maid_storage_manager.craft.context.AbstractCraftActionContext;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;
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
        BlockPos targetPos = MemoryUtil.getTargetPos(maid);
        if (targetPos == null) return false;
        double maidX = maid.getX();
        double maidZ = maid.getZ();
        Vec3 center = targetPos.getCenter();
        double targetCenterX = center.x;
        double targetCenterZ = center.z;
        if (Math.abs(maidX - targetCenterX) > 0.3 || Math.abs(maidZ - targetCenterZ) > 0.3) {
            maid.setDeltaMovement(
                    new Vec3(targetCenterX - maidX, 0, targetCenterZ - maidZ)
                            .normalize()
                            .scale(0.1)
            );
            return true;
        }
        return false;
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
