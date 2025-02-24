package studio.fantasyit.maid_storage_manager.util;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.maid.memory.ViewedInventoryMemory;
import studio.fantasyit.maid_storage_manager.registry.MemoryModuleRegistry;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class MemoryUtil {
    public static Set<BlockPos> getVisitedPos(Brain<EntityMaid> brain) {
        return brain.getMemory(MemoryModuleRegistry.MAID_VISITED_POS.get()).orElse(new HashSet<>());
    }

    public static void clearVisitedPos(Brain<EntityMaid> brain) {
        brain.eraseMemory(MemoryModuleRegistry.MAID_VISITED_POS.get());
    }

    public static @Nullable BlockPos getTargetingPos(Brain<EntityMaid> brain) {
        Optional<BlockPos> optionalPos = brain.getMemory(InitEntities.TARGET_POS.get()).map(PositionTracker::currentBlockPosition);
        return optionalPos.orElse(null);
    }

    public static void clearPosition(EntityMaid maid) {
        maid.getBrain().eraseMemory(InitEntities.TARGET_POS.get());
        maid.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
    }

    public static void setTargetingPos(EntityMaid maid, BlockPos pos) {
        maid.getBrain().setMemory(InitEntities.TARGET_POS.get(), new BlockPosTracker(pos));
    }

    //////////Terminal flags

    public static BlockPos getCurrentTerminalPos(EntityMaid maid) {
        return maid.getBrain().getMemory(MemoryModuleRegistry.CURRENT_TERMINAL_POS.get()).orElse(null);
    }


    public static void setCurrentTerminalPos(EntityMaid maid, BlockPos pos) {
        maid.getBrain().setMemory(MemoryModuleRegistry.CURRENT_TERMINAL_POS.get(), pos);
    }

    public static void clearCurrentTerminalPos(EntityMaid maid) {
        maid.getBrain().eraseMemory(MemoryModuleRegistry.CURRENT_TERMINAL_POS.get());
    }


    //////// Chest flags
    public static BlockPos getCurrentChestPos(EntityMaid maid) {
        return maid.getBrain().getMemory(MemoryModuleRegistry.CURRENT_CHEST_POS.get()).orElse(null);
    }

    public static void setCurrentChestPos(EntityMaid maid, BlockPos pos) {
        maid.getBrain().setMemory(MemoryModuleRegistry.CURRENT_CHEST_POS.get(), pos);
    }


    public static void clearCurrentChestPos(EntityMaid maid) {
        maid.getBrain().eraseMemory(MemoryModuleRegistry.CURRENT_CHEST_POS.get());
    }


    public static void returnToStorage(EntityMaid maid) {
        maid.getBrain().setMemory(MemoryModuleRegistry.RETURN_STORAGE.get(), true);
    }

    public static void arriveTarget(EntityMaid maid) {
        maid.getBrain().setMemory(MemoryModuleRegistry.ARRIVE_TARGET.get(), true);
    }

    public static void clearReturnToStorage(EntityMaid maid) {
        maid.getBrain().eraseMemory(MemoryModuleRegistry.RETURN_STORAGE.get());
    }

    public static void clearArriveTarget(EntityMaid maid) {
        maid.getBrain().eraseMemory(MemoryModuleRegistry.ARRIVE_TARGET.get());
    }

    public static Optional<UUID> getLastWorkUUID(EntityMaid maid) {
        return maid.getBrain().getMemory(MemoryModuleRegistry.LAST_TAKE_LIST.get());
    }

    public static void setLastWorkUUID(EntityMaid maid, UUID uuid) {
        maid.getBrain().setMemory(MemoryModuleRegistry.LAST_TAKE_LIST.get(), uuid);
    }


    public static void clearLastWorkUUID(EntityMaid maid) {
        maid.getBrain().eraseMemory(MemoryModuleRegistry.LAST_TAKE_LIST.get());
    }

    public static boolean isWorkingRequest(EntityMaid maid) {
        return maid.getBrain().getMemory(MemoryModuleRegistry.IS_WORKING_REQUEST.get()).orElse(false);
    }

    public static void setWorkingRequest(EntityMaid maid, boolean isWorkingRequest) {
        maid.getBrain().setMemory(MemoryModuleRegistry.IS_WORKING_REQUEST.get(), isWorkingRequest);
    }

    public static ViewedInventoryMemory getViewedInventory(EntityMaid maid) {
        if (!maid.getBrain().hasMemoryValue(MemoryModuleRegistry.VIEWED_INVENTORY.get()))
            maid.getBrain().setMemory(MemoryModuleRegistry.VIEWED_INVENTORY.get(), new ViewedInventoryMemory());

        return maid.getBrain().getMemory(MemoryModuleRegistry.VIEWED_INVENTORY.get()).orElse(null);
    }

    public static void finishChest(EntityMaid maid) {
        maid.getBrain().setMemory(MemoryModuleRegistry.FINISH_CHEST.get(), true);
    }

    public static void finishTerminal(EntityMaid maid) {
        maid.getBrain().setMemory(MemoryModuleRegistry.FINISH_TERMINAL.get(), true);
    }

    public static void clearFinish(EntityMaid maid) {
        maid.getBrain().eraseMemory(MemoryModuleRegistry.FINISH_CHEST.get());
        maid.getBrain().eraseMemory(MemoryModuleRegistry.FINISH_TERMINAL.get());
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
}
