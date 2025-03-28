package studio.fantasyit.maid_storage_manager.maid.behavior.resort;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import net.minecraftforge.items.wrapper.RangedWrapper;
import org.jetbrains.annotations.NotNull;
import studio.fantasyit.maid_storage_manager.maid.behavior.ScheduleBehavior;
import studio.fantasyit.maid_storage_manager.storage.MaidStorage;
import studio.fantasyit.maid_storage_manager.storage.Storage;
import studio.fantasyit.maid_storage_manager.storage.base.IFilterable;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageContext;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageExtractableContext;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageInteractContext;
import studio.fantasyit.maid_storage_manager.util.BehaviorBreath;
import studio.fantasyit.maid_storage_manager.util.Conditions;
import studio.fantasyit.maid_storage_manager.util.InvUtil;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;

import java.util.List;
import java.util.Map;

public class ResortBehavior extends Behavior<EntityMaid> {
    BehaviorBreath breath = new BehaviorBreath();
    private IStorageContext context = null;
    Storage target = null;
    int count = 0;

    public ResortBehavior() {
        super(Map.of());
    }

    @Override
    protected boolean checkExtraStartConditions(@NotNull ServerLevel worldIn, @NotNull EntityMaid maid) {
        if (MemoryUtil.getCurrentlyWorking(maid) != ScheduleBehavior.Schedule.RESORT) return false;
        if (!MemoryUtil.getResorting(maid).hasTarget()) return false;
        return Conditions.hasReachedValidTargetOrReset(maid);
    }

    @Override
    protected boolean canStillUse(ServerLevel p_22545_, EntityMaid maid, long p_22547_) {
        if (Conditions.isWaitingForReturn(maid)) return false;
        if (Conditions.inventoryFull(maid)) return false;
        return context != null && !context.isDone();
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long p_22542_) {
        if (!MemoryUtil.getResorting(maid).hasTarget()) return;
        target = MemoryUtil.getResorting(maid).getTarget();
        context = MaidStorage
                .getInstance()
                .getStorage(target.getType())
                .onStartCollect(level, maid, target);
        if (!(context instanceof IFilterable)) {
            context = null;
        }
        if (context != null) {
            context.start(maid, level, target);
        }
        count = 0;
    }

    @Override
    protected void tick(ServerLevel p_22551_, EntityMaid maid, long p_22553_) {
        super.tick(p_22551_, maid, p_22553_);
        if (!breath.breathTick()) return;
        CombinedInvWrapper maidInv = maid.getAvailableInv(false);
        if (context instanceof IStorageInteractContext isic) {
            isic.tick(itemStack -> {
                if (!((IFilterable) isic).isAvailable(itemStack)) {
                    int maxStore = InvUtil.maxCanPlace(maidInv, itemStack);
                    if (maxStore > 0) {
                        int store = Math.min(itemStack.getCount(), maxStore);
                        ItemStack copy = itemStack.copyWithCount(store);
                        InvUtil.tryPlace(maidInv, copy);
                        return itemStack.copyWithCount(itemStack.getCount() - store);
                    }
                }
                return itemStack;
            });
        } else if (context instanceof IStorageExtractableContext isec) {
            List<ItemStack> filterMismatch = MemoryUtil.getResorting(maid).getNeedToResort();
            isec.extract(filterMismatch, true, itemStack -> {
                if (!((IFilterable) isec).isAvailable(itemStack)) {
                    int maxStore = InvUtil.maxCanPlace(maidInv, itemStack);
                    if (maxStore > 0) {
                        int store = Math.min(itemStack.getCount(), maxStore);
                        ItemStack copy = itemStack.copyWithCount(store);
                        InvUtil.tryPlace(maidInv, copy);
                        return itemStack.copyWithCount(itemStack.getCount() - store);
                    }
                }
                return itemStack;
            });
        }
    }

    @Override
    protected void stop(ServerLevel level, EntityMaid maid, long p_22550_) {
        if (context != null) {
            if (context.isDone())
                MemoryUtil.getResorting(maid).clearTarget();

            MemoryUtil.getPlacingInv(maid).resetVisitedPos();
            MemoryUtil.getPlacingInv(maid).addVisitedPos(target);
            InvUtil.checkNearByContainers(level, target.getPos(), pos -> {
                MemoryUtil.getPlacingInv(maid).addVisitedPos(target.sameType(pos, null));
            });
            context.finish();
        }
        MemoryUtil.clearTarget(maid);
    }
    @Override
    protected boolean timedOut(long p_22537_) {
        return false;
    }
}