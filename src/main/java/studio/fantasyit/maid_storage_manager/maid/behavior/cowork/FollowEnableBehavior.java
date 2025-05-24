package studio.fantasyit.maid_storage_manager.maid.behavior.cowork;

import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidCheckRateTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import studio.fantasyit.maid_storage_manager.advancement.AdvancementTypes;
import studio.fantasyit.maid_storage_manager.debug.DebugData;
import studio.fantasyit.maid_storage_manager.maid.ChatTexts;
import studio.fantasyit.maid_storage_manager.maid.data.StorageManagerConfigData;
import studio.fantasyit.maid_storage_manager.registry.MemoryModuleRegistry;

import javax.annotation.Nullable;
import java.util.Map;

public class FollowEnableBehavior extends MaidCheckRateTask {
    public FollowEnableBehavior() {
        super(Map.of(MemoryModuleRegistry.CO_WORK_MODE.get(), MemoryStatus.VALUE_ABSENT));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel worldIn, EntityMaid maid) {
        if (!super.checkExtraStartConditions(worldIn, maid)) return false;
        if (!maid.getOrCreateData(StorageManagerConfigData.KEY, StorageManagerConfigData.Data.getDefault()).coWorkMode())
            return false;
        if (!ownerStateConditions(maid.getOwner()))
            return false;
        if (!maid.isWithinRestriction(maid.getOwner().blockPosition()))
            return false;
        if (maid.distanceTo(maid.getOwner()) > 8 && !maid.hasLineOfSight(maid.getOwner()))
            return false;
        return true;
    }

    @Override
    protected void start(ServerLevel p_22540_, EntityMaid maid, long p_22542_) {
        maid.getBrain().setMemory(MemoryModuleRegistry.CO_WORK_MODE.get(), true);
        AdvancementTypes.triggerForMaid(maid, AdvancementTypes.COWORK);
        DebugData.sendDebug("[CO_WORK]Enable");
        ChatTexts.send(maid, ChatTexts.CHAT_COWORK_ENABLE);
    }

    private boolean ownerStateConditions(@Nullable LivingEntity owner) {
        return owner != null && !owner.isSpectator() && !owner.isDeadOrDying();
    }
}
