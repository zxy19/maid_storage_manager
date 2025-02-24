package studio.fantasyit.maid_storage_manager.util;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.phys.Vec3;
import studio.fantasyit.maid_storage_manager.items.RequestListItem;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;
import studio.fantasyit.maid_storage_manager.registry.MemoryModuleRegistry;

import java.util.Optional;

public class Conditions {
    public static boolean takingRequestList(EntityMaid maid) {
        return maid.getMainHandItem().is(ItemRegistry.REQUEST_LIST_ITEM.get());
    }

    public static boolean inventoryNotFull(EntityMaid maid) {
        return InvUtil.hasAnyFree(maid.getAvailableBackpackInv());
    }

    public static boolean inventoryFull(EntityMaid maid) {
        return !inventoryNotFull(maid);
    }

    public static boolean hasStorageBlock(EntityMaid maid) {
        return RequestListItem.getStorageBlock(maid.getMainHandItem()) != null;
    }

    public static boolean listNotDone(EntityMaid maid) {
        return RequestListItem.getItemStacksNotDone(maid.getMainHandItem()).size() != 0;
    }

    public static boolean listAllDone(EntityMaid maid) {
        return !listNotDone(maid);
    }

    public static boolean listAllStored(EntityMaid maid) {
        return RequestListItem.isAllStored(maid.getMainHandItem());
    }

    public static boolean isTryToReturnStorage(EntityMaid maid) {
        return maid.getBrain().hasMemoryValue(MemoryModuleRegistry.RETURN_STORAGE.get());
    }

    public static boolean alreadyArriveTarget(EntityMaid maid) {
        return maid.getBrain().hasMemoryValue(MemoryModuleRegistry.ARRIVE_TARGET.get());
    }

    public static boolean hasReachedValidTargetOrReset(EntityMaid maid) {
        Brain<EntityMaid> brain = maid.getBrain();
        return brain.getMemory(InitEntities.TARGET_POS.get()).map(targetPos -> {
            Vec3 targetV3d = targetPos.currentPosition();
            if (maid.distanceToSqr(targetV3d) > Math.pow(2, 2)) {
                Optional<WalkTarget> walkTarget = brain.getMemory(MemoryModuleType.WALK_TARGET);
                if (walkTarget.isEmpty() || !walkTarget.get().getTarget().currentPosition().equals(targetV3d)) {
                    brain.eraseMemory(InitEntities.TARGET_POS.get());
                }
                return false;
            }
            return true;
        }).orElse(false);
    }

    public static boolean hasCurrentChestPos(EntityMaid maid) {
        return MemoryUtil.getCurrentChestPos(maid) != null;
    }

    public static boolean hasCurrentTerminalPos(EntityMaid maid) {
        return MemoryUtil.getCurrentTerminalPos(maid) != null;
    }

    public static boolean finishAll(EntityMaid maid) {
        return maid.getBrain().hasMemoryValue(MemoryModuleRegistry.FINISH_CHEST.get())
                && maid.getBrain().hasMemoryValue(MemoryModuleRegistry.FINISH_TERMINAL.get());
    }
    public static boolean isWaitingForReturn(EntityMaid maid) {
        return maid.getBrain().hasMemoryValue(MemoryModuleRegistry.RETURN_TO_SCHEDULE_AT.get());
    }
}
