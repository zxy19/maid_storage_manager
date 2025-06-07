package studio.fantasyit.maid_storage_manager.maid.behavior.base;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.MaidPathFindingBFS;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.debug.DebugData;
import studio.fantasyit.maid_storage_manager.maid.memory.AbstractTargetMemory;
import studio.fantasyit.maid_storage_manager.maid.memory.ViewedInventoryMemory;
import studio.fantasyit.maid_storage_manager.storage.MaidStorage;
import studio.fantasyit.maid_storage_manager.storage.StoragePredictor;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.util.Conditions;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;
import studio.fantasyit.maid_storage_manager.util.MoveUtil;
import studio.fantasyit.maid_storage_manager.util.PosUtil;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 带过滤器的收集目标选择行为
 */
public abstract class AbstractGatherMoveBehavior extends MaidMoveToBlockTaskWithArrivalMap {
    public AbstractGatherMoveBehavior(float speed) {
        super(speed, 3);
        this.verticalSearchStart = 1;
        this.moveSpeed = speed;
    }

    private final float moveSpeed;

    Target chestPos = null;

    abstract protected AbstractTargetMemory getMemory(EntityMaid maid);

    abstract protected boolean hasFinishedPre(ServerLevel level, EntityMaid maid);

    abstract protected void findTarget(ServerLevel level, EntityMaid maid, Target target);

    abstract protected void noTarget(ServerLevel level, EntityMaid maid);

    protected abstract @NotNull List<ItemStack> getPriorityItems(ServerLevel level, EntityMaid maid);

    protected boolean isTargetItem(ServerLevel level, EntityMaid maid, List<ItemStack> targets, ItemStack itemStack) {
        return targets.stream().anyMatch(i2 -> ItemStack.isSameItem(i2, itemStack));
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long p_22542_) {
        super.start(level, maid, p_22542_);
        AbstractTargetMemory memory = getMemory(maid);
        if (hasFinishedPre(level, maid)) return;
        if (!this.priorityTarget(level, maid))
            if (Conditions.useScanTarget(maid))
                this.searchForDestination(level, maid);

        if (!maid.getBrain().hasMemoryValue(InitEntities.TARGET_POS.get())) {
            if (memory.confirmNoTarget()) {
                DebugData.sendDebug("[GATHERING] No More Target");
                noTarget(level, maid);
            }
        } else {
            memory.resetFailCount();
            if (chestPos != null) {
                memory.setTarget(chestPos);
                MemoryUtil.setLookAt(maid, chestPos.getPos());
                DebugData.sendDebug("[GATHERING] Target %s", chestPos);
            }
            findTarget(level, maid, chestPos);
        }
    }

    private boolean priorityTarget(ServerLevel level, EntityMaid maid) {
        if (!Conditions.usePriorityTarget(maid)) return false;
        AbstractTargetMemory memory = getMemory(maid);
        List<ItemStack> targets = getPriorityItems(level, maid);
        if (targets.isEmpty()) return false;
        Map<Target, List<ViewedInventoryMemory.ItemCount>> viewed = MemoryUtil.getViewedInventory(maid).positionFlatten();
        MaidPathFindingBFS pathFinding = new MaidPathFindingBFS(maid.getNavigation().getNodeEvaluator(), level, maid);
        for (Map.Entry<Target, List<ViewedInventoryMemory.ItemCount>> blockPos : viewed.entrySet()) {
            if (memory.isVisitedPos(blockPos.getKey())) continue;
            Optional<ViewedInventoryMemory.ItemCount> first = blockPos
                    .getValue()
                    .stream()
                    .filter(itemCount -> isTargetItem(level, maid, targets, itemCount.getItem()))
                    .findFirst();
            if (first.isEmpty()) {
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
            MemoryUtil.setTarget(maid, targetPos, this.moveSpeed);
            DebugData.sendDebug("[GATHERING]Priority By Content %s", storage);
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
        @Nullable Target canTouchChest = MoveUtil.findTargetForPos(serverLevel,
                entityMaid,
                blockPos,
                getMemory(entityMaid),
                false,
                StoragePredictor::isCollectable
        );
        if (canTouchChest != null) {
            chestPos = canTouchChest;
            DebugData.sendDebug("[GATHERING]Target %s", canTouchChest);
        }
        return canTouchChest != null;
    }
}
