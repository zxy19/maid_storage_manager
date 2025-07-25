package studio.fantasyit.maid_storage_manager.maid.behavior;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.neoforged.neoforge.network.PacketDistributor;
import studio.fantasyit.maid_storage_manager.debug.DebugData;
import studio.fantasyit.maid_storage_manager.maid.memory.LogisticsMemory;
import studio.fantasyit.maid_storage_manager.network.MaidDataSyncToClientPacket;
import studio.fantasyit.maid_storage_manager.registry.MemoryModuleRegistry;
import studio.fantasyit.maid_storage_manager.util.Conditions;
import studio.fantasyit.maid_storage_manager.util.InvUtil;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;

import java.util.Map;

public class ScheduleBehavior extends Behavior<EntityMaid> {
    public enum Schedule {
        NO_SCHEDULE,
        PLACE,
        VIEW,
        REQUEST,
        CO_WORK,
        LOGISTICS,
        RESORT,
        MEAL
    }

    public ScheduleBehavior() {
        super(Map.of());
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long p_22542_) {
        memoryTick(maid);
        Schedule last = MemoryUtil.getCurrentlyWorking(maid);
        Schedule next = last;

        Integer restartAt = MemoryUtil.getReturnToScheduleAt(maid);
        if (restartAt != null) {
            if (restartAt < level.getServer().getTickCount())
                MemoryUtil.clearReturnWorkSchedule(maid);
            next = Schedule.NO_SCHEDULE;
        } else if (MemoryUtil.getViewedInventory(maid).isViewing())
            //正在查看，必须保证完成查看！
            next = Schedule.VIEW;
        else if (MemoryUtil.getCrafting(maid).isGoPlacingBeforeCraft() && !MemoryUtil.getRequestProgress(maid).isReturning()) {
            //之前在执行请求且上次任务完成，而且不在返回存储物品（也就意味着上次任务或者失败或者完全存储完成了），则应该先存储背包的所有东西
            //如果背包清空，则继续执行任务
            if (Conditions.isNothingToPlace(maid)) {
                MemoryUtil.getCrafting(maid).setGoPlacingBeforeCraft(false);
                next = Schedule.NO_SCHEDULE;
            } else
                next = Schedule.PLACE;
        } else if (Conditions.takingRequestList(maid) && (last == Schedule.REQUEST || InvUtil.hasAnyFree(maid.getAvailableInv(false)))) {
            //如果拿着列表且背包有空就可以开始处理请求，否则还得继续放东西
            next = Schedule.REQUEST;
        } else if (MemoryUtil.getLogistics(maid).shouldWork()) {
            if (MemoryUtil.getLogistics(maid).getStage() == LogisticsMemory.Stage.FINISH && !Conditions.isNothingToPlace(maid))
                next = Schedule.PLACE;
            else
                next = Schedule.LOGISTICS;
        } else if (MemoryUtil.getMeal(maid).isEating()) {
            next = Schedule.MEAL;
        } else if (!Conditions.isNothingToPlace(maid))
            //没捡满的话优先捡东西
            if (maid.getBrain().hasMemoryValue(InitEntities.VISIBLE_PICKUP_ENTITIES.get())
                    && Conditions.shouldStopAndPickUpItems(maid))
                next = Schedule.NO_SCHEDULE;
            else
                next = Schedule.PLACE;
        else if (MemoryUtil.getResorting(maid).hasTarget())
            next = Schedule.RESORT;
        else if (MemoryUtil.getMeal(maid).hasTarget())
            next = Schedule.MEAL;
        else if (!MemoryUtil.getViewedInventory(maid).getMarkChanged().isEmpty())
            next = Schedule.VIEW;
        else if (MemoryUtil.isCoWorking(maid))
            next = Schedule.CO_WORK;
        else if (maid.getBrain().hasMemoryValue(InitEntities.VISIBLE_PICKUP_ENTITIES.get()))
            next = Schedule.NO_SCHEDULE;
        else
            next = Schedule.VIEW;

        if (last != next) {
            maid.getBrain().setMemory(MemoryModuleRegistry.CURRENTLY_WORKING.get(), next);
            MemoryUtil.clearTarget(maid);
            DebugData.sendDebug("Schedule Change %s -> %s", last.toString(), next.toString());

            CompoundTag nbt = new CompoundTag();
            nbt.putInt("id", next.ordinal());
            PacketDistributor.sendToPlayersTrackingEntity(
                    maid,
                    new MaidDataSyncToClientPacket(
                            MaidDataSyncToClientPacket.Type.WORKING,
                            maid.getId(),
                            nbt
                    )
            );
        }
    }

    protected void memoryTick(EntityMaid maid) {
        MemoryUtil.getMeal(maid).tick();
    }
}
