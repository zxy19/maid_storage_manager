package studio.fantasyit.maid_storage_manager.maid.behavior.cowork;

import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidCheckRateTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import studio.fantasyit.maid_storage_manager.debug.DebugData;
import studio.fantasyit.maid_storage_manager.maid.ChatTexts;
import studio.fantasyit.maid_storage_manager.maid.data.StorageManagerConfigData;
import studio.fantasyit.maid_storage_manager.registry.MemoryModuleRegistry;

import javax.annotation.Nullable;
import java.util.Map;

public class FollowDisableBehavior extends MaidCheckRateTask {
    public FollowDisableBehavior() {
        super(Map.of(MemoryModuleRegistry.CO_WORK_MODE.get(), MemoryStatus.VALUE_PRESENT));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel worldIn, EntityMaid maid) {
        if (!super.checkExtraStartConditions(worldIn, maid)) return false;
        if (!maid.getOrCreateData(StorageManagerConfigData.KEY, StorageManagerConfigData.Data.getDefault()).coWorkMode())
            return true;
        if (!ownerStateConditions(maid.getOwner()))
            return true;
        if (!maid.isWithinRestriction(maid.getOwner().blockPosition()))
            return true;
        if (maid.distanceTo(maid.getOwner()) > 8 && !maid.hasLineOfSight(maid.getOwner()))
            return true;
        return false;
    }

    @Override
    protected void start(ServerLevel p_22540_, EntityMaid maid, long p_22542_) {
        maid.getBrain().eraseMemory(MemoryModuleRegistry.CO_WORK_MODE.get());
        DebugData.sendDebug("[CO_WORK]Disable");
        ChatTexts.send(maid, ChatTexts.CHAT_COWORK_DISABLE);
    }

    private boolean ownerStateConditions(@Nullable LivingEntity owner) {
        return owner != null && !owner.isSpectator() && !owner.isDeadOrDying();
    }
}
