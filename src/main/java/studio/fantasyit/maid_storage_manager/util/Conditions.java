package studio.fantasyit.maid_storage_manager.util;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitBrains;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.api.IRequestTaskHandler;
import studio.fantasyit.maid_storage_manager.maid.data.StorageManagerConfigData;
import studio.fantasyit.maid_storage_manager.registry.MemoryModuleRegistry;

import java.util.Optional;

public class Conditions {
    /**
     * 女仆是否手持请求列表或者正在处理请求列表
     */
    public static boolean takingRequestList(EntityMaid maid) {
        if (MemoryUtil.getCrafting(maid).isSwappingHandWhenCrafting() && MemoryUtil.getCrafting(maid).hasPlan())
            return true;
        ItemStack stack = maid.getMainHandItem();
        IRequestTaskHandler handler = IRequestTaskHandler.of(stack);
        if (handler == null)
            return false;
        if (handler.isIgnored(stack) || handler.isCoolingDown(stack))
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
        IRequestTaskHandler handler = IRequestTaskHandler.of(maid.getMainHandItem());
        return handler != null && handler.getStorageBlock(maid.getMainHandItem()) != null;
    }

    /**
     * 请求列表是否未完成
     */
    public static boolean listNotDone(EntityMaid maid) {
        ItemStack stack = maid.getMainHandItem();
        IRequestTaskHandler handler = IRequestTaskHandler.of(stack);
        if (handler == null) return true;
        if (handler.isBlackMode(stack))
            return !handler.isBlackModeDone(stack);
        return handler.getItemStacksNotDone(stack).size() != 0;
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
        ItemStack stack = maid.getMainHandItem();
        IRequestTaskHandler handler = IRequestTaskHandler.of(stack);
        if (handler == null) return true;
        if (handler.isBlackMode(stack))
            return false;
        return handler.isAllStored(stack);
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
        return brain.getMemory(InitBrains.TARGET_POS.get()).map(targetPos -> {
            Vec3 targetV3d = targetPos.currentPosition();
            boolean strictArrive = maid.distanceToSqr(targetV3d) < Math.pow(pathCloseEnoughThreshold, 2);
            boolean loosenArrive = maid.distanceToSqr(targetV3d) < Math.pow(pathCloseEnoughThreshold * 2, 2)
                    && maid.distanceToSqr(targetV3d.x(), maid.getY(), targetV3d.z()) < Math.pow(pathCloseEnoughThreshold, 2);
            if (!strictArrive && !loosenArrive) {
                Optional<WalkTarget> walkTarget = brain.getMemory(MemoryModuleType.WALK_TARGET);
                if (walkTarget.isEmpty() || !walkTarget.get().getTarget().currentPosition().equals(targetV3d)) {
                    brain.eraseMemory(InitBrains.TARGET_POS.get());
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
                    IRequestTaskHandler h = IRequestTaskHandler.of(slot);
                    return h == null || h.isIgnored(slot);
                }
        ).stream().allMatch(stack -> stack.isEmpty());
    }

    /**
     * 是否应该停止放置物品并前往拾取物品
     */
    public static boolean shouldStopAndPickUpItems(EntityMaid maid) {
        if (MemoryUtil.isWorking(maid))
            return false;
        ResourceHandler<ItemResource> inv = maid.getAvailableInv(false);
        if (InvUtil.freeSlots(inv) >= inv.size() * Config.pickupRequireWhenPlace) {
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
        return switch (StorageManagerConfigData.get(maid).memoryAssistant()) {
            case MEMORY_ONLY, MEMORY_FIRST -> true;
            case ALWAYS_SCAN -> false;
        };
    }

    /**
     * 是否应该使用扫描（非记忆匹配的）目标
     */
    public static boolean useScanTarget(EntityMaid maid) {
        return switch (StorageManagerConfigData.get(maid).memoryAssistant()) {
            case MEMORY_ONLY -> false;
            case ALWAYS_SCAN, MEMORY_FIRST -> true;
        };
    }

    /**
     * 存放是否不分类
     */
    public static boolean noSortPlacement(EntityMaid maid) {
        return StorageManagerConfigData.get(maid).noSortPlacement();
    }

    /**
     * 是否应该先进行存储检查
     */
    public static boolean shouldCheckStock(EntityMaid maid) {
        ItemStack mainHandItem = maid.getMainHandItem();
        IRequestTaskHandler handler = IRequestTaskHandler.of(mainHandItem);
        if (handler == null) return false;
        if (handler.getStorageBlock(mainHandItem) == null) return false;
        if (!handler.isStockMode(mainHandItem)) return false;
        return !handler.hasCheckedStock(mainHandItem);
    }
}