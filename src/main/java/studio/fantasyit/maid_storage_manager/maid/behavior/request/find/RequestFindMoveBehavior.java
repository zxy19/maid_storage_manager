package studio.fantasyit.maid_storage_manager.maid.behavior.request.find;

import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidMoveToBlockTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
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
import studio.fantasyit.maid_storage_manager.maid.behavior.ScheduleBehavior;
import studio.fantasyit.maid_storage_manager.maid.memory.RequestProgressMemory;
import studio.fantasyit.maid_storage_manager.maid.memory.ViewedInventoryMemory;
import studio.fantasyit.maid_storage_manager.storage.MaidStorage;
import studio.fantasyit.maid_storage_manager.storage.Storage;
import studio.fantasyit.maid_storage_manager.util.Conditions;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;
import studio.fantasyit.maid_storage_manager.util.MoveUtil;
import studio.fantasyit.maid_storage_manager.util.PosUtil;

import java.util.List;
import java.util.Map;

/**
 * 手上持有物品清单，尝试前往附近所有的箱子
 */
public class RequestFindMoveBehavior extends MaidMoveToBlockTask {
    public RequestFindMoveBehavior() {
        super((float) Config.collectSpeed, 3);
        this.verticalSearchStart = 1;
    }

    Storage chestPos = null;

    @Override
    protected boolean checkExtraStartConditions(@NotNull ServerLevel worldIn, @NotNull EntityMaid owner) {
        if (MemoryUtil.getCurrentlyWorking(owner) != ScheduleBehavior.Schedule.REQUEST) return false;
        if (!Conditions.takingRequestList(owner)) return false;
        if (MemoryUtil.getRequestProgress(owner).isReturning()) return false;
        return Conditions.inventoryNotFull(owner) && !Conditions.listAllDone(owner);
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long p_22542_) {
        super.start(level, maid, p_22542_);
        if (!priorityTarget(level, maid))
            this.searchForDestination(level, maid);
        RequestProgressMemory requestProgress = MemoryUtil.getRequestProgress(maid);
        if (!maid.getBrain().hasMemoryValue(InitEntities.TARGET_POS.get())) {
            DebugData.getInstance().sendMessage("[REQUEST_FIND]No More Target");
            RequestListItem.markAllDone(maid.getMainHandItem());
        } else {
            if (chestPos != null) {
                requestProgress.setTarget(chestPos);
                maid.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(chestPos.getPos()));
            }
        }
    }


    private boolean priorityTarget(ServerLevel level, EntityMaid maid) {
        List<Pair<ItemStack, Integer>> notDone = RequestListItem.getItemStacksNotDone(maid.getMainHandItem(), true);
        Map<Storage, List<ViewedInventoryMemory.ItemCount>> viewed = MemoryUtil.getViewedInventory(maid).positionFlatten();
        for (Map.Entry<Storage, List<ViewedInventoryMemory.ItemCount>> blockPos : viewed.entrySet()) {
            if (MemoryUtil.getRequestProgress(maid).isVisitedPos(blockPos.getKey())) continue;
            if (blockPos
                    .getValue()
                    .stream()
                    .noneMatch(itemCount ->
                            notDone
                                    .stream()
                                    .anyMatch(i2 ->
                                            ItemStack.isSameItemSameTags(i2.getA(), itemCount.getItem())
                                    )
                    )
            ) {
                continue;
            }
            @Nullable BlockPos targetPos = MoveUtil.selectPosForTarget(level, maid, blockPos.getKey().getPos());
            if (targetPos != null) {
                @Nullable Storage storage = MaidStorage.getInstance().isValidTarget(level, maid, blockPos.getKey().getPos(), blockPos.getKey().side);
                if (storage != null) {
                    chestPos = storage;
                    MemoryUtil.setTarget(maid, targetPos, (float) Config.placeSpeed);
                    DebugData.getInstance().sendMessage("[REQUEST_FIND]Priority By Filter %s", storage);
                }
                return true;
            }
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
        Storage canTouchChest = MoveUtil.findTargetForPos(serverLevel, entityMaid, blockPos, requestProgress);
        if (canTouchChest != null) {
            chestPos = canTouchChest;
            DebugData.getInstance().sendMessage("[REQUEST_FIND]Target %s", canTouchChest);
        }
        return canTouchChest != null;
    }
}
