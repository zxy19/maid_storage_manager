package studio.fantasyit.maid_storage_manager.maid.behavior.request.find;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.MaidPathFindingBFS;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.debug.DebugData;
import studio.fantasyit.maid_storage_manager.items.RequestListItem;
import studio.fantasyit.maid_storage_manager.maid.behavior.MaidMoveToBlockTaskWithArrivalMap;
import studio.fantasyit.maid_storage_manager.maid.behavior.ScheduleBehavior;
import studio.fantasyit.maid_storage_manager.maid.memory.RequestProgressMemory;
import studio.fantasyit.maid_storage_manager.maid.memory.ViewedInventoryMemory;
import studio.fantasyit.maid_storage_manager.storage.MaidStorage;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.util.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 手上持有物品清单，尝试前往附近所有的箱子
 */
public class RequestFindMoveBehavior extends MaidMoveToBlockTaskWithArrivalMap {
    public RequestFindMoveBehavior() {
        super((float) Config.collectSpeed, 3);
        this.verticalSearchStart = 1;
    }

    ItemStack checkItem = null;
    Target chestPos = null;
    Target returnPos = null;

    @Override
    protected boolean checkExtraStartConditions(@NotNull ServerLevel worldIn, @NotNull EntityMaid owner) {
        if (MemoryUtil.getCurrentlyWorking(owner) != ScheduleBehavior.Schedule.REQUEST) return false;
        if (!Conditions.takingRequestList(owner)) return false;
        if (MemoryUtil.getRequestProgress(owner).isReturning()) return false;
        if (MemoryUtil.getRequestProgress(owner).isTryCrafting()) return false;
        if (MemoryUtil.getRequestProgress(owner).isCheckingStock()) return false;
        if (Conditions.shouldCheckStock(owner)) return false;
        return Conditions.inventoryNotFull(owner) && !Conditions.listAllDone(owner);
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long p_22542_) {
        super.start(level, maid, p_22542_);
        MemoryUtil.getRequestProgress(maid).clearCheckItem();
        checkItem = null;
        if (!priorityTarget(level, maid))
            if (Conditions.useScanTarget(maid) || RequestListItem.isBlackMode(maid.getMainHandItem()))
                this.searchForDestination(level, maid);
        RequestProgressMemory requestProgress = MemoryUtil.getRequestProgress(maid);
        if (!maid.getBrain().hasMemoryValue(InitEntities.TARGET_POS.get())) {
            if (MemoryUtil.getRequestProgress(maid).confirmNoTarget()) {
                DebugData.sendDebug("[REQUEST_FIND]No More Target");
                MemoryUtil.getRequestProgress(maid).setTryCrafting(true);
                //立刻安排返回存储
                MemoryUtil.getRequestProgress(maid).setReturn();
                MemoryUtil.getRequestProgress(maid).clearTarget();
                MemoryUtil.clearTarget(maid);
            }
        } else {
            MemoryUtil.getRequestProgress(maid).resetFailCount();
            if (chestPos != null) {
                requestProgress.setTarget(chestPos);
                maid.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(chestPos.getPos()));
            }
            if (checkItem != null) {
                requestProgress.setCheckItem(checkItem);
            }
        }
    }


    private boolean priorityTarget(ServerLevel level, EntityMaid maid) {
        if (!Conditions.usePriorityTarget(maid)) return false;
        if (RequestListItem.isBlackMode(maid.getMainHandItem())) return false;
        List<Pair<ItemStack, Integer>> notDone = RequestListItem.getItemStacksNotDone(maid.getMainHandItem(), true);
        boolean matchTag = RequestListItem.matchNbt(maid.getMainHandItem());
        Map<Target, List<ViewedInventoryMemory.ItemCount>> viewed = MemoryUtil.getViewedInventory(maid).positionFlatten();
        MaidPathFindingBFS pathFinding = new MaidPathFindingBFS(maid.getNavigation().getNodeEvaluator(), level, maid);
        for (Map.Entry<Target, List<ViewedInventoryMemory.ItemCount>> blockPos : viewed.entrySet()) {
            if (MemoryUtil.getRequestProgress(maid).isVisitedPos(blockPos.getKey())) continue;
            Optional<ViewedInventoryMemory.ItemCount> targetItem = blockPos
                    .getValue()
                    .stream()
                    .filter(itemCount ->
                            notDone
                                    .stream()
                                    .anyMatch(i2 ->
                                            ItemStackUtil.isSame(i2.getA(), itemCount.getItem(), matchTag)
                                    )
                    )
                    .findFirst();

            @Nullable Target storage = MaidStorage.getInstance().isValidTarget(level, maid, blockPos.getKey().getPos(), blockPos.getKey().side);
            if (storage == null) continue;
            boolean craftGuideProvider = MaidStorage.getInstance().isCraftGuideProvider(storage, blockPos.getValue());
            if (targetItem.isEmpty() && !craftGuideProvider) continue;

            if (!MoveUtil.isValidTarget(level, maid, storage, false)) continue;

            List<BlockPos> possiblePos = MoveUtil.getAllAvailablePosForTarget(level, maid, blockPos.getKey().getPos(), pathFinding);
            if (possiblePos.isEmpty()) continue;

            @Nullable BlockPos targetPos = MoveUtil.getNearestFromTargetList(level, maid, possiblePos);
            if (targetPos == null) continue;

            chestPos = storage;
            MemoryUtil.setTarget(maid, targetPos, (float) Config.placeSpeed);
            DebugData.sendDebug("[REQUEST_FIND]Priority By Filter %s", storage);
            targetItem.ifPresent(itemCount -> this.checkItem = itemCount.getFirst());
            return true;
        }
        return false;
    }

    @Override
    protected boolean shouldMoveTo(@NotNull ServerLevel serverLevel,
                                   EntityMaid entityMaid,
                                   @NotNull BlockPos blockPos) {
        if (!PosUtil.isSafePos(serverLevel, blockPos)) return false;
        RequestProgressMemory requestProgress = MemoryUtil.getRequestProgress(entityMaid);
        //寻找当前格子能触碰的箱子
        Target canTouchChest = MoveUtil.findTargetForPos(serverLevel, entityMaid, blockPos, requestProgress);
        if (canTouchChest != null) {
            chestPos = canTouchChest;
            DebugData.sendDebug("[REQUEST_FIND]Target %s", canTouchChest);
        }
        return canTouchChest != null;
    }
}
