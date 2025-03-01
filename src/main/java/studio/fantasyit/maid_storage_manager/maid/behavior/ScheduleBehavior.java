package studio.fantasyit.maid_storage_manager.maid.behavior;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import studio.fantasyit.maid_storage_manager.debug.DebugData;
import studio.fantasyit.maid_storage_manager.registry.MemoryModuleRegistry;
import studio.fantasyit.maid_storage_manager.util.Conditions;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;

import java.util.Map;

public class ScheduleBehavior extends Behavior<EntityMaid> {
    public enum Schedule {
        NO_SCHEDULE,
        PLACE,
        VIEW,
        REQUEST,
        RESORT
    }

    public ScheduleBehavior() {
        super(Map.of());
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long p_22542_) {
        Schedule last = MemoryUtil.getCurrentlyWorking(maid);
        Schedule next = last;

        Integer restartAt = MemoryUtil.getReturnToScheduleAt(maid);
        if (restartAt != null) {
            if (restartAt < level.getServer().getTickCount())
                MemoryUtil.clearReturnWorkSchedule(maid);
            next = Schedule.NO_SCHEDULE;
            //如果拿着列表且背包有空就可以开始处理请求，否则还得继续放东西
        } else if (Conditions.takingRequestList(maid) && (last == Schedule.REQUEST || Conditions.inventoryNotFull(maid)))
            next = Schedule.REQUEST;
        else if (!Conditions.isNothingToPlace(maid))
            next = Schedule.PLACE;
        else if (MemoryUtil.getResorting(maid).hasTarget())
            next = Schedule.RESORT;
        else if (maid.getBrain().hasMemoryValue(InitEntities.VISIBLE_PICKUP_ENTITIES.get()))
            next = Schedule.NO_SCHEDULE;
        else
            next = Schedule.VIEW;

        if (last != next) {
            maid.getBrain().setMemory(MemoryModuleRegistry.CURRENTLY_WORKING.get(), next);
            MemoryUtil.clearTarget(maid);
            DebugData.getInstance().sendMessage("Schedule Change %s -> %s", last.toString(), next.toString());
        }
    }
}
