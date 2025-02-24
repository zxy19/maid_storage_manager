package studio.fantasyit.maid_storage_manager.maid.behavior.request;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import studio.fantasyit.maid_storage_manager.util.Conditions;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;

import java.util.Map;

import static studio.fantasyit.maid_storage_manager.registry.MemoryModuleRegistry.RETURN_TO_SCHEDULE_AT;

public class ReturnWorkScheduleBehavior extends Behavior<EntityMaid> {
    public ReturnWorkScheduleBehavior() {
        super(Map.of(RETURN_TO_SCHEDULE_AT.get(), MemoryStatus.VALUE_PRESENT));
    }


    @Override
    protected void start(ServerLevel level, EntityMaid maid, long p_22542_) {
        Integer restartAt = MemoryUtil.getReturnToScheduleAt(maid);
        if (restartAt == null || restartAt < level.getServer().getTickCount()) {
            MemoryUtil.clearReturnWorkSchedule(maid);
        }
    }
}
