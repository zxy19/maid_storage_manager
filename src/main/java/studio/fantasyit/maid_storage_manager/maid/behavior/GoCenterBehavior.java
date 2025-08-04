package studio.fantasyit.maid_storage_manager.maid.behavior;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import studio.fantasyit.maid_storage_manager.util.Conditions;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;

import java.util.Map;

public class GoCenterBehavior extends Behavior<EntityMaid> {
    public GoCenterBehavior() {
        super(Map.of());
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel p_22538_, EntityMaid maid) {
        if (!MemoryUtil.isGoingCenter(maid)) return false;
        return Conditions.hasReachedValidTargetOrReset(maid);
    }

    @Override
    protected void start(ServerLevel p_22540_, EntityMaid maid, long p_22542_) {
        MemoryUtil.setGoingCenter(maid, false);
        MemoryUtil.clearTarget(maid);
    }
}
