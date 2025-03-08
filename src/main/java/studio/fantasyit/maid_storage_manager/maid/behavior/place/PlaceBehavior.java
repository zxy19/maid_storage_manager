package studio.fantasyit.maid_storage_manager.maid.behavior.place;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import org.jetbrains.annotations.NotNull;
import studio.fantasyit.maid_storage_manager.items.RequestListItem;
import studio.fantasyit.maid_storage_manager.maid.behavior.ScheduleBehavior;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;
import studio.fantasyit.maid_storage_manager.storage.MaidStorage;
import studio.fantasyit.maid_storage_manager.storage.Storage;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageContext;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageInsertableContext;
import studio.fantasyit.maid_storage_manager.util.BehaviorBreath;
import studio.fantasyit.maid_storage_manager.util.Conditions;
import studio.fantasyit.maid_storage_manager.util.InvUtil;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;

import java.util.List;
import java.util.Map;

public class PlaceBehavior extends Behavior<EntityMaid> {
    BehaviorBreath breath = new BehaviorBreath();
    private IStorageContext context = null;
    Storage target = null;
    int count = 0;
    private boolean changed;

    public PlaceBehavior() {
        super(Map.of(), 5000);
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
        if (!MemoryUtil.getPlacingInv(maid).hasTarget()) return;
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
    }

    @Override
    protected void tick(ServerLevel p_22551_, EntityMaid maid, long p_22553_) {
        super.tick(p_22551_, maid, p_22553_);
        if (!breath.breathTick()) return;
        CombinedInvWrapper inv = maid.getAvailableInv(false);
        if (count >= inv.getSlots()) {
            return;
        }
        @NotNull ItemStack item = inv.getStackInSlot(count);
        int oCount = item.getCount();
        if (context instanceof IStorageInsertableContext isic) {
            List<ItemStack> arrangeItems = MemoryUtil.getPlacingInv(maid).getArrangeItems();
            if (arrangeItems.isEmpty() || arrangeItems.stream().anyMatch(i -> ItemStack.isSameItem(i, item))) {
                if (item.is(ItemRegistry.REQUEST_LIST_ITEM.get())) {
                    if (RequestListItem.isIgnored(item)) {
                        CompoundTag tag = item.getOrCreateTag();
                        tag.putBoolean(RequestListItem.TAG_IGNORE_TASK, true);
                        item.setTag(tag);
                        ItemStack insert = isic.insert(item);
                        MemoryUtil.getViewedInventory(maid).addItem(target, insert.copyWithCount(oCount - insert.getCount()));
                        inv.setStackInSlot(count, insert);
                    }
                } else {
                    ItemStack insert = isic.insert(item);
                    MemoryUtil.getViewedInventory(maid).addItem(target, insert.copyWithCount(oCount - insert.getCount()));
                    inv.setStackInSlot(count, insert);
                }
            }
        }
        if (inv.getStackInSlot(count).getCount() != oCount) {
            changed = true;
        }
        count++;
    }


    @Override
    protected void stop(ServerLevel level, EntityMaid maid, long p_22550_) {
        if (context != null) {
            context.finish();
            MemoryUtil.getPlacingInv(maid).clearTarget();
            if (!changed) {
                MemoryUtil.getPlacingInv(maid).addVisitedPos(target);
            } else {
                MemoryUtil.getPlacingInv(maid).anySuccess();
            }
            MemoryUtil.getPlacingInv(maid).clearArrangeItems();
            InvUtil.checkNearByContainers(level, target.getPos(), pos -> {
                MemoryUtil.getPlacingInv(maid).addVisitedPos(target.sameType(pos, null));
            });
        }
        MemoryUtil.clearTarget(maid);
    }
}
