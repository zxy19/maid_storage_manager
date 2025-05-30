package studio.fantasyit.maid_storage_manager.maid.behavior.request.craft.gather;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.MaidPathFindingBFS;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.debug.DebugData;
import studio.fantasyit.maid_storage_manager.maid.ChatTexts;
import studio.fantasyit.maid_storage_manager.maid.behavior.MaidMoveToBlockTaskWithArrivalMap;
import studio.fantasyit.maid_storage_manager.maid.behavior.ScheduleBehavior;
import studio.fantasyit.maid_storage_manager.maid.memory.ViewedInventoryMemory;
import studio.fantasyit.maid_storage_manager.storage.MaidStorage;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.util.Conditions;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;
import studio.fantasyit.maid_storage_manager.util.MoveUtil;
import studio.fantasyit.maid_storage_manager.util.PosUtil;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 手上持有物品清单，尝试前往附近所有的箱子
 */
public class RequestCraftGatherMoveBehavior extends MaidMoveToBlockTaskWithArrivalMap {
    public RequestCraftGatherMoveBehavior() {
        super((float) Config.collectSpeed, 3);
        this.verticalSearchStart = 1;
    }

    Target chestPos = null;

    @Override
    protected boolean checkExtraStartConditions(@NotNull ServerLevel worldIn, @NotNull EntityMaid owner) {
        if (MemoryUtil.getCurrentlyWorking(owner) != ScheduleBehavior.Schedule.REQUEST) return false;
        if (MemoryUtil.getRequestProgress(owner).isReturning()) return false;
        if (!Conditions.takingRequestList(owner)) return false;
        if (MemoryUtil.getCrafting(owner).hasStartWorking()) return false;
        if (!MemoryUtil.getCrafting(owner).hasCurrent()) return false;
        return true;
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long p_22542_) {
        super.start(level, maid, p_22542_);
        if (MemoryUtil.getCrafting(maid).getCurrentLayer().hasCollectedAll()) {
            MemoryUtil.getCrafting(maid).finishGathering(maid);
            return;
        }
        if (!this.priorityTarget(level, maid))
            if (Conditions.useScanTarget(maid))
                this.searchForDestination(level, maid);
        if (!maid.getBrain().hasMemoryValue(InitEntities.TARGET_POS.get())) {
            if (MemoryUtil.getCrafting(maid).confirmNoTarget()) {
                DebugData.sendDebug("[REQUEST_CRAFT_GATHER] No More Target");
                MemoryUtil.getCrafting(maid).finishGathering(maid);
            }
        } else {
            MemoryUtil.getCrafting(maid).resetFailCount();
            if (chestPos != null) {
                MemoryUtil.getCrafting(maid).setTarget(chestPos);
                MemoryUtil.setLookAt(maid, chestPos.getPos());
                DebugData.sendDebug("[REQUEST_CRAFT_GATHER] Target %s", chestPos);
            }
            ChatTexts.send(maid, ChatTexts.CHAT_CRAFT_GATHER);
        }
    }

    private boolean priorityTarget(ServerLevel level, EntityMaid maid) {
        if (!Conditions.usePriorityTarget(maid)) return false;
        List<ItemStack> targets = Objects.requireNonNull(MemoryUtil.getCrafting(maid).getCurrentLayer()).getUnCollectedItems();
        if (targets.isEmpty()) return false;
        Map<Target, List<ViewedInventoryMemory.ItemCount>> viewed = MemoryUtil.getViewedInventory(maid).positionFlatten();
        MaidPathFindingBFS pathFinding = new MaidPathFindingBFS(maid.getNavigation().getNodeEvaluator(), level, maid);
        for (Map.Entry<Target, List<ViewedInventoryMemory.ItemCount>> blockPos : viewed.entrySet()) {
            if (MemoryUtil.getCrafting(maid).isVisitedPos(blockPos.getKey())) continue;
            if (blockPos
                    .getValue()
                    .stream()
                    .noneMatch(itemCount ->
                            targets
                                    .stream()
                                    .anyMatch(i2 ->
                                            ItemStack.isSameItem(i2, itemCount.getItem())
                                    )
                    )
            ) {
                continue;
            }
            @Nullable Target storage = MaidStorage.getInstance().isValidTarget(level,
                    maid,
                    blockPos.getKey().getPos(),
                    blockPos.getKey().side);
            if (storage == null) continue;
            if (!MoveUtil.isValidTarget(level, maid, storage, false)) continue;

            List<BlockPos> possiblePos = MoveUtil.getAllAvailablePosForTarget(level, maid, blockPos.getKey().getPos(), pathFinding);
            if (possiblePos.isEmpty()) continue;

            @Nullable BlockPos targetPos = MoveUtil.getNearestFromTargetList(level, maid, possiblePos);
            if (targetPos == null) continue;

            chestPos = storage;
            MemoryUtil.setTarget(maid, targetPos, (float) Config.placeSpeed);
            DebugData.sendDebug("[REQUEST_CRAFT_GATHER]Priority By Content %s", storage);
            return true;
        }
        return false;
    }

    @Override
    protected boolean shouldMoveTo(@NotNull ServerLevel serverLevel,
                                   EntityMaid entityMaid,
                                   @NotNull BlockPos blockPos) {
        if (!PosUtil.isSafePos(serverLevel, blockPos)) return false;
        //寻找当前格子能触碰的箱子
        @Nullable Target canTouchChest = MoveUtil.findTargetForPos(serverLevel, entityMaid, blockPos, MemoryUtil.getCrafting(entityMaid));
        if (canTouchChest != null) {
            chestPos = canTouchChest;
            DebugData.sendDebug("[REQUEST_CRAFT_GATHER]Target %s", canTouchChest);
        }
        return canTouchChest != null;
    }
}
