package studio.fantasyit.maid_storage_manager.maid.behavior.place;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.MaidPathFindingBFS;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.debug.DebugData;
import studio.fantasyit.maid_storage_manager.maid.ChatTexts;
import studio.fantasyit.maid_storage_manager.maid.behavior.MaidMoveToBlockTaskWithArrivalMap;
import studio.fantasyit.maid_storage_manager.maid.behavior.ScheduleBehavior;
import studio.fantasyit.maid_storage_manager.maid.data.StorageManagerConfigData;
import studio.fantasyit.maid_storage_manager.maid.memory.PlacingInventoryMemory;
import studio.fantasyit.maid_storage_manager.maid.memory.ViewedInventoryMemory;
import studio.fantasyit.maid_storage_manager.storage.MaidStorage;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.storage.base.IFilterable;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageContext;
import studio.fantasyit.maid_storage_manager.util.Conditions;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;
import studio.fantasyit.maid_storage_manager.util.MoveUtil;
import studio.fantasyit.maid_storage_manager.util.PosUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static studio.fantasyit.maid_storage_manager.maid.data.StorageManagerConfigData.SuppressStrategy;

public class PlaceMoveBehavior extends MaidMoveToBlockTaskWithArrivalMap {
    private Target chestPos;
    private PlacingInventoryMemory.Suppressed.Type suppressType;

    public PlaceMoveBehavior() {
        super((float) Config.placeSpeed, 3);
        this.verticalSearchStart = 1;
    }

    @Override
    protected boolean checkExtraStartConditions(@NotNull ServerLevel worldIn, @NotNull EntityMaid owner) {
        if (MemoryUtil.getCurrentlyWorking(owner) != ScheduleBehavior.Schedule.PLACE) return false;
        if (Conditions.isWaitingForReturn(owner)) return false;
        return !Conditions.isNothingToPlace(owner);
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long p_22542_) {
        super.start(level, maid, p_22542_);

        if (!this.priorityTarget(level, maid))
            this.searchForDestination(level, maid);

        if (!maid.getBrain().hasMemoryValue(InitEntities.TARGET_POS.get())) {
            if (!MemoryUtil.getPlacingInv(maid).isAnySuccess()) {
                MemoryUtil.getPlacingInv(maid).addFailCount();

                //快速放置-保留全部记录模式，没有目标几次后重置所有suppressed
                if (MemoryUtil.getPlacingInv(maid).getFailCount() >= 2 && MemoryUtil.getPlacingInv(maid).anySuppressed()) {
                    DebugData.sendDebug("[PLACE]Suppress clear L ALL");
                    MemoryUtil.getPlacingInv(maid).removeSuppressed();
                    MemoryUtil.getPlacingInv(maid).resetFailCount();
                }

                if (MemoryUtil.getPlacingInv(maid).getFailCount() >= 5)
                    ChatTexts.send(maid, ChatTexts.CHAT_CHEST_FULL);
            } else {
                MemoryUtil.getPlacingInv(maid).resetFailCount();
            }
            MemoryUtil.getPlacingInv(maid).resetVisitedPos();
            MemoryUtil.getPlacingInv(maid).clearTarget();
            MemoryUtil.getPlacingInv(maid).resetAnySuccess();
            MemoryUtil.clearTarget(maid);
            DebugData.sendDebug("[PLACE]Reset (Iter all)");
        } else {
            if (chestPos != null) {
                MemoryUtil.getPlacingInv(maid).setTarget(chestPos);
                MemoryUtil.getPlacingInv(maid).setTargetSuppressType(suppressType);
                maid.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(chestPos.pos));
            }
        }
    }

    private boolean priorityTarget(ServerLevel level, EntityMaid maid) {
        if (Conditions.noSortPlacement(maid)) return false;
        CombinedInvWrapper inv = maid.getAvailableInv(true);
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < inv.getSlots(); i++) {
            if (!inv.getStackInSlot(i).isEmpty())
                items.add(inv.getStackInSlot(i));
        }
        Target targetContent = null;
        List<ItemStack> targetContentList = new ArrayList<>();
        List<BlockPos> targetContentPos = null;
        Target targetFilter = null;
        List<ItemStack> targetFilterList = new ArrayList<>();
        List<BlockPos> targetFilterPos = null;

        MaidPathFindingBFS pathFinding = new MaidPathFindingBFS(maid.getNavigation().getNodeEvaluator(), level, maid);
        Map<Target, List<ViewedInventoryMemory.ItemCount>> blockPosListMap = MemoryUtil.getViewedInventory(maid).positionFlatten();
        for (Map.Entry<Target, List<ViewedInventoryMemory.ItemCount>> blockPos : blockPosListMap.entrySet()) {
            if (targetFilter != null) break;

            //过滤器判断
            Target validTarget = MaidStorage.getInstance().isValidTarget(level, maid, blockPos.getKey().getPos(), blockPos.getKey().side);
            if (validTarget != null) {
                if (!MoveUtil.isValidTarget(level, maid, validTarget, false)) continue;
            }

            if (MemoryUtil.getPlacingInv(maid).isVisitedPos(validTarget)) {
                continue;
            }

            if (MoveUtil.findTargetRewrite(level, maid, blockPos.getKey(), false).isEmpty())
                continue;

            List<BlockPos> possibleMove = MoveUtil.getAllAvailablePosForTarget(level, maid, blockPos.getKey().getPos(), pathFinding);
            if (possibleMove.isEmpty()) continue;

            if (validTarget != null) {
                @Nullable IStorageContext context = MaidStorage
                        .getInstance()
                        .getStorage(validTarget.getType())
                        .onPreviewFilter(level, maid, validTarget);
                if (context != null) context.start(maid, level, validTarget);
                if (context instanceof IFilterable ift) {
                    //请求返回箱子，不能存入其他物品
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


        PlacingInventoryMemory placingInv = MemoryUtil.getPlacingInv(maid);
        if (targetFilter != null) {
            placingInv.setArrangeItems(targetFilterList);
            chestPos = targetFilter;
            MemoryUtil.setTarget(maid, MoveUtil.getNearestFromTargetList(level, maid, targetFilterPos), (float) Config.placeSpeed);
            DebugData.sendDebug("[PLACE]Priority By Filter %s", targetFilter.toString());
            suppressType = PlacingInventoryMemory.Suppressed.Type.FILTER;
            return true;
        }
        if (StorageManagerConfigData.get(maid).suppressStrategy() == SuppressStrategy.AFTER_EACH
                && placingInv.anySuppressed(PlacingInventoryMemory.Suppressed.Type.FILTER)
        ) {
            placingInv.removeSuppressed(PlacingInventoryMemory.Suppressed.Type.FILTER);
            DebugData.sendDebug("[PLACE]Suppress clear L Filter");
            return priorityTarget(level, maid);
        }
        if (targetContent != null) {
            placingInv.setArrangeItems(targetContentList);
            chestPos = targetContent;
            MemoryUtil.setTarget(maid, MoveUtil.getNearestFromTargetList(level, maid, targetContentPos), (float) Config.placeSpeed);
            DebugData.sendDebug("[PLACE]Priority By Content %s", targetContent);
            suppressType = PlacingInventoryMemory.Suppressed.Type.MATCH;
            return true;
        }
        if ((StorageManagerConfigData.get(maid).suppressStrategy() == StorageManagerConfigData.SuppressStrategy.AFTER_EACH
                || StorageManagerConfigData.get(maid).suppressStrategy() == SuppressStrategy.AFTER_PRIORITY)
                && (placingInv.anySuppressed(PlacingInventoryMemory.Suppressed.Type.FILTER)
                || placingInv.anySuppressed(PlacingInventoryMemory.Suppressed.Type.MATCH))) {
            DebugData.sendDebug("[PLACE]Suppress clear L Match");
            MemoryUtil.getPlacingInv(maid).removeSuppressed(PlacingInventoryMemory.Suppressed.Type.FILTER);
            MemoryUtil.getPlacingInv(maid).removeSuppressed(PlacingInventoryMemory.Suppressed.Type.MATCH);
            return priorityTarget(level, maid);
        }
        return false;
    }

    @Override
    protected boolean shouldMoveTo(ServerLevel serverLevel, EntityMaid entityMaid, BlockPos blockPos) {
        if (!PosUtil.isSafePos(serverLevel, blockPos)) return false;

        //寻找当前格子能触碰的箱子
        Target canTouchChest = MoveUtil.findTargetForPos(serverLevel,
                entityMaid,
                blockPos,
                MemoryUtil.getPlacingInv(entityMaid));
        if (canTouchChest != null) {
            chestPos = canTouchChest;
            DebugData.sendDebug("[PLACE]Normal %s", canTouchChest);
            suppressType = PlacingInventoryMemory.Suppressed.Type.NORMAL;
            return true;
        }
        return false;
    }
}
