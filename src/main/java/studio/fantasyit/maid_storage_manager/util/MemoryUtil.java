package studio.fantasyit.maid_storage_manager.util;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import studio.fantasyit.maid_storage_manager.maid.behavior.ScheduleBehavior;
import studio.fantasyit.maid_storage_manager.maid.memory.*;
import studio.fantasyit.maid_storage_manager.registry.MemoryModuleRegistry;
import studio.fantasyit.maid_storage_manager.storage.Target;

import java.util.UUID;

public class MemoryUtil {
    public static BlockPos getTargetPos(EntityMaid maid) {
        return maid
                .getBrain()
                .getMemory(InitEntities.TARGET_POS.get())
                .filter(t -> t instanceof BlockPosTracker)
                .map(t -> (BlockPosTracker) t)
                .map(BlockPosTracker::currentBlockPosition)
                .orElse(null);
    }

    public static RequestProgressMemory getRequestProgress(EntityMaid maid) {
        if (!maid.getBrain().hasMemoryValue(MemoryModuleRegistry.REQUEST_PROGRESS.get()))
            maid.getBrain().setMemory(MemoryModuleRegistry.REQUEST_PROGRESS.get(), new RequestProgressMemory());
        return maid.getBrain().getMemory(MemoryModuleRegistry.REQUEST_PROGRESS.get()).orElse(null);
    }

    public static ViewedInventoryMemory getViewedInventory(EntityMaid maid) {
        if (!maid.getBrain().hasMemoryValue(MemoryModuleRegistry.VIEWED_INVENTORY.get()))
            maid.getBrain().setMemory(MemoryModuleRegistry.VIEWED_INVENTORY.get(), new ViewedInventoryMemory());

        return maid.getBrain().getMemory(MemoryModuleRegistry.VIEWED_INVENTORY.get()).orElse(null);
    }

    public static void setReturnToScheduleAt(EntityMaid maid, int time) {
        maid.getBrain().setMemory(MemoryModuleRegistry.RETURN_TO_SCHEDULE_AT.get(), time);
    }

    public static Integer getReturnToScheduleAt(EntityMaid maid) {
        return maid.getBrain().getMemory(MemoryModuleRegistry.RETURN_TO_SCHEDULE_AT.get()).orElse(null);
    }

    public static void clearReturnWorkSchedule(EntityMaid maid) {
        maid.getBrain().eraseMemory(MemoryModuleRegistry.RETURN_TO_SCHEDULE_AT.get());
    }

    public static void clearTarget(EntityMaid maid) {
        maid.getBrain().eraseMemory(InitEntities.TARGET_POS.get());
        maid.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
    }

    public static PlacingInventoryMemory getPlacingInv(EntityMaid maid) {
        if (!maid.getBrain().hasMemoryValue(MemoryModuleRegistry.PLACING_INVENTORY.get()))
            maid.getBrain().setMemory(MemoryModuleRegistry.PLACING_INVENTORY.get(), new PlacingInventoryMemory());
        return maid.getBrain().getMemory(MemoryModuleRegistry.PLACING_INVENTORY.get()).orElse(null);
    }

    public static ResortingMemory getResorting(EntityMaid maid) {
        if (!maid.getBrain().hasMemoryValue(MemoryModuleRegistry.RESORTING.get()))
            maid.getBrain().setMemory(MemoryModuleRegistry.RESORTING.get(), new ResortingMemory());
        return maid.getBrain().getMemory(MemoryModuleRegistry.RESORTING.get()).orElse(null);
    }

    public static void setTarget(EntityMaid maid, BlockPos goal, float collectSpeed) {
        maid.getBrain().setMemory(InitEntities.TARGET_POS.get(), new BlockPosTracker(goal));
        BehaviorUtils.setWalkAndLookTargetMemories(maid, goal, collectSpeed, 0);
    }

    public static void setTarget(EntityMaid maid, Entity entity, float collectSpeed) {
        maid.getBrain().setMemory(InitEntities.TARGET_POS.get(), new EntityTracker(entity, true));
        BehaviorUtils.setWalkAndLookTargetMemories(maid, entity, collectSpeed, 0);
    }

    public static ScheduleBehavior.Schedule getCurrentlyWorking(EntityMaid maid) {
        return maid.getBrain().getMemory(MemoryModuleRegistry.CURRENTLY_WORKING.get()).orElse(ScheduleBehavior.Schedule.NO_SCHEDULE);
    }

    public static CraftMemory getCrafting(EntityMaid maid) {
        if (!maid.getBrain().hasMemoryValue(MemoryModuleRegistry.CRAFTING.get()))
            maid.getBrain().setMemory(MemoryModuleRegistry.CRAFTING.get(), new CraftMemory());
        return maid.getBrain().getMemory(MemoryModuleRegistry.CRAFTING.get()).orElse(null);
    }

    public static LogisticsMemory getLogistics(EntityMaid maid) {
        if (!maid.getBrain().hasMemoryValue(MemoryModuleRegistry.LOGISTICS.get()))
            maid.getBrain().setMemory(MemoryModuleRegistry.LOGISTICS.get(), new LogisticsMemory());
        return maid.getBrain().getMemory(MemoryModuleRegistry.LOGISTICS.get()).orElse(null);
    }

    public static MealMemory getMeal(EntityMaid maid) {
        if (!maid.getBrain().hasMemoryValue(MemoryModuleRegistry.MEAL.get()))
            maid.getBrain().setMemory(MemoryModuleRegistry.MEAL.get(), new MealMemory());
        return maid.getBrain().getMemory(MemoryModuleRegistry.MEAL.get()).orElse(null);
    }

    public static void setLookAt(EntityMaid maid, BlockPos pos) {
        maid.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(pos));
    }

    public static void setLookAt(EntityMaid maid, Entity target) {
        maid.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(target, true));
    }

    public static BlockPos getInteractPos(EntityMaid maid) {
        return maid.getBrain().getMemory(MemoryModuleRegistry.INTERACTION_RESULT.get()).orElse(null);
    }

    public static void setInteractPos(EntityMaid maid, BlockPos above) {
        maid.getBrain().setMemory(MemoryModuleRegistry.INTERACTION_RESULT.get(), above);
    }

    public static void clearInteractPos(EntityMaid maid) {
        maid.getBrain().eraseMemory(MemoryModuleRegistry.INTERACTION_RESULT.get());
    }

    public static Target getCoWorkTargetStorage(EntityMaid maid) {
        return maid.getBrain().getMemory(MemoryModuleRegistry.CO_WORK_TARGET_STORAGE.get()).orElse(null);
    }

    public static boolean isCoWorking(EntityMaid maid) {
        return maid.getBrain().hasMemoryValue(MemoryModuleRegistry.CO_WORK_MODE.get());
    }

    public static boolean isWorking(EntityMaid maid) {
        return maid.getBrain().hasMemoryValue(MemoryModuleRegistry.IS_WORKING.get());
    }

    public static void setWorking(EntityMaid maid, boolean working) {
        if (working) {
            maid.getBrain().setMemory(MemoryModuleRegistry.IS_WORKING.get(), true);
//            DebugData.sendDebug("->✔️Working Start");
        } else {
            maid.getBrain().eraseMemory(MemoryModuleRegistry.IS_WORKING.get());
//            DebugData.sendDebug("<-❌Working Stop");
        }
    }

    public static boolean canPickUpItemTemp(EntityMaid maid, UUID target) {
        if (maid.getBrain().hasMemoryValue(MemoryModuleRegistry.ENABLE_PICKUP_TEMP.get())) {
            return maid.getBrain().getMemory(MemoryModuleRegistry.ENABLE_PICKUP_TEMP.get()).map(uuid -> uuid.equals(target)).orElse(false);
        }
        return false;
    }

    public static void setPickUpItemTemp(EntityMaid maid, UUID target) {
        maid.getBrain().setMemory(MemoryModuleRegistry.ENABLE_PICKUP_TEMP.get(), target);
    }

    public static void clearPickUpItemTemp(EntityMaid maid) {
        maid.getBrain().eraseMemory(MemoryModuleRegistry.ENABLE_PICKUP_TEMP.get());
    }
}
