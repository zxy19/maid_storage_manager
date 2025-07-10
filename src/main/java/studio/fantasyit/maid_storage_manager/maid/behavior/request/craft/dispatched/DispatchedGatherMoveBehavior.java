package studio.fantasyit.maid_storage_manager.maid.behavior.request.craft.dispatched;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.items.RequestListItem;
import studio.fantasyit.maid_storage_manager.maid.behavior.ScheduleBehavior;
import studio.fantasyit.maid_storage_manager.util.Conditions;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;

import java.util.UUID;

public class DispatchedGatherMoveBehavior extends Behavior<EntityMaid> {
    public DispatchedGatherMoveBehavior() {
        super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT, InitEntities.TARGET_POS.get(), MemoryStatus.VALUE_ABSENT));
    }

    @Override
    protected boolean checkExtraStartConditions(@NotNull ServerLevel worldIn, @NotNull EntityMaid owner) {
        if (MemoryUtil.getCurrentlyWorking(owner) != ScheduleBehavior.Schedule.REQUEST) return false;
        if (MemoryUtil.getRequestProgress(owner).isReturning()) return false;
        if (!Conditions.takingRequestList(owner)) return false;
        if (!MemoryUtil.getCrafting(owner).isGatheringDispatched()) return false;
        return true;
    }


    @Override
    protected void start(ServerLevel level, EntityMaid maid, long p_22542_) {
        @Nullable UUID entityUU = RequestListItem.getStorageEntity(maid.getMainHandItem());
        if (entityUU == null)
            return;
        @Nullable Entity entity = level.getEntity(entityUU);
        if (entity == null)
            return;

        MemoryUtil.setTarget(maid, entity, (float) Config.collectSpeed);
    }
}