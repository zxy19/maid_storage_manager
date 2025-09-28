package studio.fantasyit.maid_storage_manager.maid.behavior.communicate;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import studio.fantasyit.maid_storage_manager.maid.behavior.ScheduleBehavior;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;

import java.util.Map;

public class MaidCommunicateDelayedCompleteManageBehavior extends Behavior<EntityMaid> {
    public MaidCommunicateDelayedCompleteManageBehavior() {
        super(Map.of());
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel p_22538_, EntityMaid maid) {
        if (MemoryUtil.getCurrentlyWorking(maid) != ScheduleBehavior.Schedule.VIEW && MemoryUtil.getCurrentlyWorking(maid) != ScheduleBehavior.Schedule.NO_SCHEDULE && MemoryUtil.getCurrentlyWorking(maid) != ScheduleBehavior.Schedule.COMMUNICATE)
            return false;
        return MemoryUtil.getCommunicate(maid).hasDelayedComplete();
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long p_22542_) {
        MemoryUtil.getCommunicate(maid).tickDelayedTimeout();
    }
}
