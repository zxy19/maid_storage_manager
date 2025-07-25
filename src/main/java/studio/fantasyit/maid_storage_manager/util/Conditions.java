package studio.fantasyit.maid_storage_manager.util;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.items.RequestListItem;
import studio.fantasyit.maid_storage_manager.maid.data.StorageManagerConfigData;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;
import studio.fantasyit.maid_storage_manager.registry.MemoryModuleRegistry;

import java.util.Optional;

public class Conditions {
    /**
     * 女仆是否手持请求列表或者正在处理请求列表
     */
    public static boolean takingRequestList(EntityMaid maid) {
        if (MemoryUtil.getCrafting(maid).isSwappingHandWhenCrafting() && MemoryUtil.getCrafting(maid).hasPlan())
            return true;
        if (!maid.getMainHandItem().is(ItemRegistry.REQUEST_LIST_ITEM.get()))
            return false;
        if (RequestListItem.isIgnored(maid.getMainHandItem()) || RequestListItem.isCoolingDown(maid.getMainHandItem()))
            return false;
        return true;
    }

    /**
     * 女仆是否背包未满
     */
    public static boolean inventoryNotFull(EntityMaid maid) {
        return InvUtil.hasAnyFree(maid.getAvailableInv(false));
    }

    /**
     * 女仆是否背包已满
     */
    public static boolean inventoryFull(EntityMaid maid) {
        return !inventoryNotFull(maid);
    }

    /**
     * 当前请求列表是否有绑定存储方块
     */
    public static boolean hasStorageBlock(EntityMaid maid) {
        return RequestListItem.getStorageBlock(maid.getMainHandItem()) != null;
    }

    /**
     * 请求列表是否未完成
     */
    public static boolean listNotDone(EntityMaid maid) {
        if (RequestListItem.isBlackMode(maid.getMainHandItem()))
            return !RequestListItem.isBlackModeDone(maid.getMainHandItem());
        return RequestListItem.getItemStacksNotDone(maid.getMainHandItem()).size() != 0;
    }

    /**
     * 请求列表是否完成
     */
    public static boolean listAllDone(EntityMaid maid) {
        return !listNotDone(maid);
    }

    /**
     * 请求列表是否存储完毕
     */
    public static boolean listAllStored(EntityMaid maid) {
        if (RequestListItem.isBlackMode(maid.getMainHandItem()))
            return false;
        return RequestListItem.isAllStored(maid.getMainHandItem());
    }

    /**
     * 是否已经到达目标，如果不合法则重置目标
     */
    public static boolean hasReachedValidTargetOrReset(EntityMaid maid) {
        return hasReachedValidTargetOrReset(maid, 2);
    }

    /**
     * 是否已经到达目标，如果不合法则重置目标
     */
    public static boolean hasReachedValidTargetOrReset(EntityMaid maid, double pathCloseEnoughThreshold) {
        Brain<EntityMaid> brain = maid.getBrain();
        return brain.getMemory(InitEntities.TARGET_POS.get()).map(targetPos -> {
            Vec3 targetV3d = targetPos.currentPosition();
            boolean strictArrive = maid.distanceToSqr(targetV3d) < Math.pow(pathCloseEnoughThreshold, 2);
            boolean loosenArrive = maid.distanceToSqr(targetV3d) < Math.pow(pathCloseEnoughThreshold * 2, 2)
                    && maid.distanceToSqr(targetV3d.x(), maid.getY(), targetV3d.z()) < Math.pow(pathCloseEnoughThreshold, 2);
            if (!strictArrive && !loosenArrive) {
                Optional<WalkTarget> walkTarget = brain.getMemory(MemoryModuleType.WALK_TARGET);
                if (walkTarget.isEmpty() || !walkTarget.get().getTarget().currentPosition().equals(targetV3d)) {
                    brain.eraseMemory(InitEntities.TARGET_POS.get());
                }
                return false;
            }
            return true;
        }).orElse(false);
    }

    /**
     * 是否正在闲置等待（一般是因为未绑定的请求列表结束）
     */
    public static boolean isWaitingForReturn(EntityMaid maid) {
        return maid.getBrain().hasMemoryValue(MemoryModuleRegistry.RETURN_TO_SCHEDULE_AT.get());
    }

    /**
     * 是否没有需要放置的物品
     */
    public static boolean isNothingToPlace(EntityMaid maid) {
        return InvUtil.forSlotMatches(
                maid.getAvailableInv(false),
                //请求列表如果不处在忽略工作状态，则说明可以进行
                slot -> {
                    if (slot.is(ItemRegistry.REQUEST_LIST_ITEM.get())) {
                        return RequestListItem.isIgnored(slot);
                    }
                    return true;
                }
        ).stream().allMatch(stack -> stack.isEmpty());
    }

    /**
     * 是否应该停止放置物品并前往拾取物品
     */
    public static boolean shouldStopAndPickUpItems(EntityMaid maid) {
        if (MemoryUtil.isWorking(maid))
            return false;
        CombinedInvWrapper inv = maid.getAvailableInv(false);
        if (InvUtil.freeSlots(inv) >= inv.getSlots() * Config.pickupRequireWhenPlace) {
            return true;
        }
        return false;
    }

    /**
     * 请求存放次数到达最大次数
     */
    public static boolean triesReach(EntityMaid maid) {
        return MemoryUtil.getRequestProgress(maid).getTries() > Config.maxStoreTries;
    }

    /**
     * 是否应该使用优先（记忆匹配的）目标
     */
    public static boolean usePriorityTarget(EntityMaid maid) {
        return switch (maid.getOrCreateData(StorageManagerConfigData.KEY, StorageManagerConfigData.Data.getDefault()).memoryAssistant()) {
            case MEMORY_ONLY, MEMORY_FIRST -> true;
            case ALWAYS_SCAN -> false;
        };
    }

    /**
     * 是否应该使用扫描（非记忆匹配的）目标
     */
    public static boolean useScanTarget(EntityMaid maid) {
        return switch (maid.getOrCreateData(StorageManagerConfigData.KEY, StorageManagerConfigData.Data.getDefault()).memoryAssistant()) {
            case MEMORY_ONLY -> false;
            case ALWAYS_SCAN, MEMORY_FIRST -> true;
        };
    }

    /**
     * 存放是否不分类
     */
    public static boolean noSortPlacement(EntityMaid maid) {
        return maid.getOrCreateData(StorageManagerConfigData.KEY, StorageManagerConfigData.Data.getDefault()).noSortPlacement();
    }

    /**
     * 是否应该先进行存储检查
     */
    public static boolean shouldCheckStock(EntityMaid maid) {
        ItemStack mainHandItem = maid.getMainHandItem();
        if (RequestListItem.getStorageBlock(mainHandItem) == null) return false;
        if (!RequestListItem.isStockMode(mainHandItem)) return false;
        return !RequestListItem.hasCheckedStock(mainHandItem);
    }
}