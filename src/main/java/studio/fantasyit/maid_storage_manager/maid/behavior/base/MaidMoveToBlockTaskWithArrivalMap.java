package studio.fantasyit.maid_storage_manager.maid.behavior.base;

import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidMoveToBlockTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.MaidPathFindingBFS;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;

abstract public class MaidMoveToBlockTaskWithArrivalMap extends MaidMoveToBlockTask {
    public MaidMoveToBlockTaskWithArrivalMap(float movementSpeed) {
        super(movementSpeed);
    }

    public MaidMoveToBlockTaskWithArrivalMap(float movementSpeed, int verticalSearchRange) {
        super(movementSpeed, verticalSearchRange);
    }

    private MaidPathFindingBFS pathfindingBFS;
    protected @NotNull MaidPathFindingBFS getOrCreateCustomArrivalMap(@NotNull ServerLevel worldIn, @NotNull EntityMaid maid) {
        if (this.pathfindingBFS == null)
            if (maid.hasRestriction())
                this.pathfindingBFS = new MaidPathFindingBFS(maid.getNavigation().getNodeEvaluator(), worldIn, maid, (int) maid.getRestrictRadius() + 1, (int) maid.getRestrictRadius());
            else
                this.pathfindingBFS = new MaidPathFindingBFS(maid.getNavigation().getNodeEvaluator(), worldIn, maid, 7);
        return this.pathfindingBFS;
    }
    @Override
    protected void stop(ServerLevel p_22540_, EntityMaid p_22541_, long p_22542_) {
        super.stop(p_22540_, p_22541_, p_22542_);
    }
}
