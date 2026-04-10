package studio.fantasyit.maid_storage_manager.maid.behavior.base;

import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidMoveToBlockTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.server.level.ServerLevel;

abstract public class MaidMoveToBlockTaskWithArrivalMap extends MaidMoveToBlockTask {
    public MaidMoveToBlockTaskWithArrivalMap(float movementSpeed) {
        super(movementSpeed);
    }

    public MaidMoveToBlockTaskWithArrivalMap(float movementSpeed, int verticalSearchRange) {
        super(movementSpeed, verticalSearchRange);
    }
    @Override
    protected void stop(ServerLevel p_22540_, EntityMaid p_22541_, long p_22542_) {
        super.stop(p_22540_, p_22541_, p_22542_);
    }
}
