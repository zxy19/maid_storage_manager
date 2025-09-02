package studio.fantasyit.maid_storage_manager.integration.kubejs.binding;


import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import studio.fantasyit.maid_storage_manager.maid.behavior.ScheduleBehavior;
import studio.fantasyit.maid_storage_manager.maid.memory.*;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;

public class KJSMSMMemories {

    public BlockPos getTargetPos(EntityMaid maid) {
        return MemoryUtil.getTargetPos(maid);
    }

    public RequestProgressMemory getRequestProgress(EntityMaid maid) {
        return MemoryUtil.getRequestProgress(maid);
    }

    public ViewedInventoryMemory getViewedInventory(EntityMaid maid) {
        return MemoryUtil.getViewedInventory(maid);
    }

    public void setReturnToScheduleAt(EntityMaid maid, int time) {
        MemoryUtil.setReturnToScheduleAt(maid, time);
    }

    public Integer getReturnToScheduleAt(EntityMaid maid) {
        return MemoryUtil.getReturnToScheduleAt(maid);
    }

    public void clearReturnWorkSchedule(EntityMaid maid) {
        MemoryUtil.clearReturnWorkSchedule(maid);
    }

    public void clearTarget(EntityMaid maid) {
        MemoryUtil.clearTarget(maid);
    }

    public PlacingInventoryMemory getPlacingInv(EntityMaid maid) {
        return MemoryUtil.getPlacingInv(maid);
    }

    public ResortingMemory getResorting(EntityMaid maid) {
        return MemoryUtil.getResorting(maid);
    }

    public void setTarget(EntityMaid maid, BlockPos goal, float collectSpeed) {
        MemoryUtil.setTarget(maid, goal, collectSpeed);
    }

    public void setTarget(EntityMaid maid, Entity entity, float collectSpeed) {
        MemoryUtil.setTarget(maid, entity, collectSpeed);
    }

    public ScheduleBehavior.Schedule getCurrentlyWorking(EntityMaid maid) {
        return MemoryUtil.getCurrentlyWorking(maid);
    }

    public CraftMemory getCrafting(EntityMaid maid) {
        return MemoryUtil.getCrafting(maid);
    }

    public LogisticsMemory getLogistics(EntityMaid maid) {
        return MemoryUtil.getLogistics(maid);
    }

    public MealMemory getMeal(EntityMaid maid) {
        return MemoryUtil.getMeal(maid);
    }

    public boolean isCoWorking(EntityMaid maid) {
        return MemoryUtil.isCoWorking(maid);
    }

    public boolean isWorking(EntityMaid maid) {
        return MemoryUtil.isWorking(maid);
    }
    public void goRestrictCenterAndWait(EntityMaid maid, float speed) {
        MemoryUtil.goRestrictCenterAndWait(maid, speed);
    }
    public boolean isGoingCenter(EntityMaid maid) {
        return MemoryUtil.isGoingCenter(maid);
    }
    public void setGoingCenter(EntityMaid maid, boolean goingCenter) {
        MemoryUtil.setGoingCenter(maid, goingCenter);
    }
}
