package studio.fantasyit.maid_storage_manager.maid.behavior.place;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import org.jetbrains.annotations.NotNull;
import studio.fantasyit.maid_storage_manager.debug.DebugData;
import studio.fantasyit.maid_storage_manager.items.RequestListItem;
import studio.fantasyit.maid_storage_manager.maid.behavior.ScheduleBehavior;
import studio.fantasyit.maid_storage_manager.maid.data.StorageManagerConfigData;
import studio.fantasyit.maid_storage_manager.registry.DataComponentRegistry;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;
import studio.fantasyit.maid_storage_manager.storage.MaidStorage;
import studio.fantasyit.maid_storage_manager.storage.StorageVisitLock;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.storage.base.IFilterable;
import studio.fantasyit.maid_storage_manager.storage.base.ISlotBasedStorage;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageContext;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageInsertableContext;
import studio.fantasyit.maid_storage_manager.util.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PlaceBehavior extends Behavior<EntityMaid> {
    BehaviorBreath breath = new BehaviorBreath();
    private IStorageContext context = null;
    Target target = null;
    int count = 0;
    private boolean changed;
    StorageVisitLock.LockContext lock = StorageVisitLock.DUMMY;

    public PlaceBehavior() {
        super(Map.of());
    }

    @Override
    protected boolean checkExtraStartConditions(@NotNull ServerLevel worldIn, @NotNull EntityMaid owner) {
        if (MemoryUtil.getCurrentlyWorking(owner) != ScheduleBehavior.Schedule.PLACE) return false;
        if (!super.checkExtraStartConditions(worldIn, owner)) return false;
        if (!MemoryUtil.getPlacingInv(owner).hasTarget()) return false;
        return Conditions.hasReachedValidTargetOrReset(owner);
    }

    @Override
    protected boolean canStillUse(ServerLevel p_22545_, EntityMaid maid, long p_22547_) {
        if (Conditions.isWaitingForReturn(maid)) return false;
        if (Conditions.isNothingToPlace(maid)) return false;
        if (count >= maid.getAvailableInv(false).getSlots()) return false;
        return context != null && !context.isDone();
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long p_22542_) {
        lock = StorageVisitLock.DUMMY;
        if (!MemoryUtil.getPlacingInv(maid).hasTarget()) return;
        MemoryUtil.setWorking(maid, true);
        target = MemoryUtil.getPlacingInv(maid).getTarget();
        context = MaidStorage
                .getInstance()
                .getStorage(target.getType())
                .onStartPlace(level, maid, target);
        if (context != null) {
            context.start(maid, level, target);
        }
        count = 0;
        changed = false;
        lock = StorageVisitLock.getWriteLock(target);
    }

    @Override
    protected void tick(ServerLevel p_22551_, EntityMaid maid, long p_22553_) {
        if (!lock.checkAndTryGrantLock()) return;
        if (!breath.breathTick(maid)) return;
        CombinedInvWrapper inv = maid.getAvailableInv(false);
        for (int _i = 0; _i < inv.getSlots() / 3; _i++) {
            if (count >= inv.getSlots()) {
                break;
            }
            @NotNull ItemStack item = inv.getStackInSlot(count);
            int oCount = item.getCount();
            boolean whitelist = false;
            if (context instanceof IFilterable iFilterable) {
                if (!iFilterable.isAvailable(item)) {
                    count++;
                    continue;
                }
                whitelist = iFilterable.isWhitelist();
            }
            if (context instanceof ISlotBasedStorage slotContext && exceedSlotLimit(slotContext, item, maid) && !whitelist) {
                count++;
                continue;
            }
            if (context instanceof IStorageInsertableContext isic) {
                List<ItemStack> arrangeItems = MemoryUtil.getPlacingInv(maid).getArrangeItems();
                if (arrangeItems.isEmpty() || arrangeItems.stream().anyMatch(i -> ItemStack.isSameItem(i, item))) {
                    if (item.is(ItemRegistry.REQUEST_LIST_ITEM.get())) {
                        if (RequestListItem.isIgnored(item)) {
                            item.remove(DataComponentRegistry.REQUEST_IGNORE);
                            ItemStack insert = isic.insert(item);
                            ViewedInventoryUtil.ambitiousAddItemAndSync(maid, p_22551_, target, item.copyWithCount(oCount - insert.getCount()));
                            inv.setStackInSlot(count, insert);
                        }
                    } else {
                        ItemStack insert = isic.insert(item);
                        ViewedInventoryUtil.ambitiousAddItemAndSync(maid, p_22551_, target, item.copyWithCount(oCount - insert.getCount()));
                        inv.setStackInSlot(count, insert);
                    }
                }
            }
            if (inv.getStackInSlot(count).getCount() != oCount) {
                changed = true;
            }
            count++;
        }
    }

    private boolean exceedSlotLimit(ISlotBasedStorage slotContext, @NotNull ItemStack item, EntityMaid maid) {
        List<ItemStack> existingItems = new ArrayList<>();
        boolean found = false;
        for (int i = 0; i < slotContext.getSlots(); i++) {
            ItemStack stack = slotContext.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            if (existingItems.stream().noneMatch(itemStack -> ItemStackUtil.isSame(itemStack, stack, false))) {
                existingItems.add(stack);
            }
            if (ItemStackUtil.isSame(stack, item, false)) {
                found = true;
            }
        }
        if (found) {
            return false;
        }
        int i = StorageManagerConfigData.get(maid).itemTypeLimit();
        if (i == -1 || existingItems.size() < i)
            return false;
        return true;
    }


    @Override
    protected void stop(ServerLevel level, EntityMaid maid, long p_22550_) {
        lock.release();
        MemoryUtil.setWorking(maid, false);
        if (context != null) {
            context.finish();
            MemoryUtil.getPlacingInv(maid).clearTarget();
            if (!changed) {
                MemoryUtil.getPlacingInv(maid).addVisitedPos(target);
            } else {
                MemoryUtil.getPlacingInv(maid).anySuccess();
            }
            MemoryUtil.getPlacingInv(maid).clearArrangeItems();
            StorageAccessUtil.checkNearByContainers(level, target.getPos(), pos -> {
                MemoryUtil.getPlacingInv(maid).addVisitedPos(target.sameType(pos, null));
            });
        }
        if (!changed) {
            if (maid.getOrCreateData(StorageManagerConfigData.KEY, StorageManagerConfigData.Data.getDefault()).suppressStrategy() != StorageManagerConfigData.SuppressStrategy.AFTER_ALL) {
                MemoryUtil.getPlacingInv(maid).addSuppressedPos(target);
                DebugData.sendDebug("[PLACE]Suppress set at %s", target);
            }
        }
        MemoryUtil.clearTarget(maid);
        MemoryUtil.getCrafting(maid).tryStartIfHasPlan();
        if (StorageManagerConfigData.get(maid).autoSorting())
            MemoryUtil.getSorting(maid).addNeedToSorting(target);
    }

    @Override
    protected boolean timedOut(long p_22537_) {
        return false;
    }
}
