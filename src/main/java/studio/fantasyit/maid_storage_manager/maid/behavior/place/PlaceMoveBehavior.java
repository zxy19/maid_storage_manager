package studio.fantasyit.maid_storage_manager.maid.behavior.place;

import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidMoveToBlockTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.debug.DebugData;
import studio.fantasyit.maid_storage_manager.maid.behavior.ScheduleBehavior;
import studio.fantasyit.maid_storage_manager.maid.memory.PlacingInventoryMemory;
import studio.fantasyit.maid_storage_manager.maid.memory.ViewedInventoryMemory;
import studio.fantasyit.maid_storage_manager.storage.MaidStorage;
import studio.fantasyit.maid_storage_manager.storage.Storage;
import studio.fantasyit.maid_storage_manager.storage.base.IFilterable;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageContext;
import studio.fantasyit.maid_storage_manager.util.Conditions;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;
import studio.fantasyit.maid_storage_manager.util.MoveUtil;
import studio.fantasyit.maid_storage_manager.util.PosUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PlaceMoveBehavior extends MaidMoveToBlockTask {
    private Storage chestPos;

    public PlaceMoveBehavior() {
        super((float) Config.placeSpeed, 3);
        this.verticalSearchStart = 1;
    }

    @Override
    protected boolean checkExtraStartConditions(@NotNull ServerLevel worldIn, @NotNull EntityMaid owner) {
        if (MemoryUtil.getCurrentlyWorking(owner) != ScheduleBehavior.Schedule.PLACE) return false;
        if (Conditions.isWaitingForReturn(owner)) return false;
        if (Conditions.takingRequestList(owner)) return false;
        return !Conditions.isNothingToPlace(owner);
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long p_22542_) {
        super.start(level, maid, p_22542_);

        if (!this.priorityTarget(level, maid))
            this.searchForDestination(level, maid);

        if (!maid.getBrain().hasMemoryValue(InitEntities.TARGET_POS.get())) {
            MemoryUtil.getPlacingInv(maid).resetVisitedPos();
            MemoryUtil.getPlacingInv(maid).clearTarget();
            MemoryUtil.clearTarget(maid);
            DebugData.getInstance().sendMessage("[PLACE]Reset (Iter all)");
        } else {
            if (chestPos != null) {
                MemoryUtil.getPlacingInv(maid).setTarget(chestPos);
                maid.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(chestPos.pos));
            }
        }
    }

    private boolean priorityTarget(ServerLevel level, EntityMaid maid) {
        CombinedInvWrapper inv = maid.getAvailableInv(true);
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < inv.getSlots(); i++) {
            if (!inv.getStackInSlot(i).isEmpty())
                items.add(inv.getStackInSlot(i));
        }
        Storage targetContent = null;
        List<ItemStack> targetContentList = new ArrayList<>();
        BlockPos targetContentPos = null;
        Storage targetFilter = null;
        List<ItemStack> targetFilterList = new ArrayList<>();
        BlockPos targetFilterPos = null;

        Map<Storage, List<ViewedInventoryMemory.ItemCount>> blockPosListMap = MemoryUtil.getViewedInventory(maid).positionFlatten();
        for (Map.Entry<Storage, List<ViewedInventoryMemory.ItemCount>> blockPos : blockPosListMap.entrySet()) {
            if (targetFilter != null) break;

            @Nullable BlockPos possibleMove = MoveUtil.selectPosForTarget(level, maid, blockPos.getKey().getPos());
            if (possibleMove == null) continue;

            //过滤器判断
            Storage validTarget = MaidStorage.getInstance().isValidTarget(level, maid, blockPos.getKey().getPos(), blockPos.getKey().side);
            if (validTarget != null) {
                if (MemoryUtil.getPlacingInv(maid).isVisitedPos(validTarget))
                    continue;
                @Nullable IStorageContext context = MaidStorage
                        .getInstance()
                        .getStorage(validTarget.getType())
                        .onPreviewFilter(level, maid, validTarget);
                if (context != null) context.start(maid, level, validTarget);
                if (context instanceof IFilterable ift) {
                    //请求返回箱子，不能存入其他物品
                    if (ift.isRequestOnly()) continue;
                    if (ift.isWhitelist()) {
                        boolean found = false;
                        for (ItemStack itemStack : items) {
                            if (ift.isAvailable(itemStack)) {
                                found = true;
                                targetFilterList.add(itemStack);
                            }
                        }
                        if (found) {
                            targetFilter = validTarget;
                            targetFilterPos = possibleMove;
                        }
                    }
                }
            }

            //内容物判断
            if (targetContent != null) continue;
            boolean foundTarget = false;
            for (ViewedInventoryMemory.ItemCount itemCount : blockPos.getValue()) {
                boolean found = false;
                for (ItemStack itemStack : items) {
                    if (!itemCount.getFirst().isEmpty() && ItemStack.isSameItem(itemStack, itemCount.getFirst())) {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    foundTarget = true;
                    targetContentList.add(itemCount.getFirst());
                    break;
                }
            }
            if (foundTarget) {
                targetContentPos = possibleMove;
                targetContent = validTarget;
            }
        }

        if (targetFilter != null) {
            PlacingInventoryMemory placingInv = MemoryUtil.getPlacingInv(maid);
            placingInv.setArrangeItems(targetFilterList);
            chestPos = targetFilter;
            MemoryUtil.setTarget(maid, targetFilterPos, (float) Config.placeSpeed);
            DebugData.getInstance().sendMessage("[PLACE]Priority By Filter %s", targetFilter.toString());
            return true;
        } else if (targetContent != null) {
            PlacingInventoryMemory placingInv = MemoryUtil.getPlacingInv(maid);
            placingInv.setArrangeItems(targetContentList);
            chestPos = targetContent;
            MemoryUtil.setTarget(maid, targetContentPos, (float) Config.placeSpeed);
            DebugData.getInstance().sendMessage("[PLACE]Priority By Content %s", targetContent);
            return true;
        }
        return false;
    }

    @Override
    protected boolean shouldMoveTo(ServerLevel serverLevel, EntityMaid entityMaid, BlockPos blockPos) {
        if (!PosUtil.isSafePos(serverLevel, blockPos)) return false;

        //寻找当前格子能触碰的箱子
        Storage canTouchChest = MoveUtil.findTargetForPos(serverLevel,
                entityMaid,
                blockPos,
                MemoryUtil.getPlacingInv(entityMaid));
        if (canTouchChest != null) {
            chestPos = canTouchChest;
            DebugData.getInstance().sendMessage("[PLACE]Normal %s", canTouchChest);
            return true;
        }
        return false;
    }
}
