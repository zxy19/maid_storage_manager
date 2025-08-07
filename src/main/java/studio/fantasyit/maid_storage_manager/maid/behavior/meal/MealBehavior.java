package studio.fantasyit.maid_storage_manager.maid.behavior.meal;

import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidWorkMealTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.NotNull;
import studio.fantasyit.maid_storage_manager.maid.behavior.ScheduleBehavior;
import studio.fantasyit.maid_storage_manager.storage.MaidStorage;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageContext;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageExtractableContext;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageInteractContext;
import studio.fantasyit.maid_storage_manager.util.*;

import java.util.List;
import java.util.function.Function;

public class MealBehavior extends MaidWorkMealTask {

    BehaviorBreath breath = new BehaviorBreath();
    private IStorageContext context = null;
    Target target = null;
    MutableBoolean hasTaken = new MutableBoolean(false);
    long endTimeStamp = -1;

    @Override
    protected boolean checkExtraStartConditions(@NotNull ServerLevel worldIn, @NotNull EntityMaid maid) {
        if (MemoryUtil.getCurrentlyWorking(maid) != ScheduleBehavior.Schedule.MEAL) return false;
        if (!MemoryUtil.getMeal(maid).hasTarget()) return false;
        if (Conditions.inventoryFull(maid)) return false;
        return Conditions.hasReachedValidTargetOrReset(maid);
    }

    @Override
    protected boolean canStillUse(ServerLevel p_22545_, EntityMaid maid, long p_22547_) {
        if (hasTaken.getValue())
            return !maid.getMainHandItem().isEmpty();
        return context != null && !context.isDone();
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long p_22542_) {
        if (!MemoryUtil.getMeal(maid).hasTarget()) return;
        MemoryUtil.setWorking(maid, true);
        target = MemoryUtil.getMeal(maid).getTarget();
        context = MaidStorage
                .getInstance()
                .getStorage(target.getType())
                .onStartCollect(level, maid, target);
        if (context != null) {
            context.start(maid, level, target);
        }
        hasTaken = new MutableBoolean(false);
        endTimeStamp = -1;
    }

    @Override
    protected void tick(ServerLevel level, EntityMaid maid, long p_22553_) {
        if (hasTaken.getValue()) {
            return;
        }

        if (!breath.breathTick(maid)) return;
        CombinedInvWrapper maidInv = maid.getAvailableInv(true);
        Function<ItemStack, ItemStack> taker = (ItemStack itemStack) -> {
            if (hasTaken.getValue())
                return itemStack;
            if (ItemStackUtil.isSame(itemStack, MemoryUtil.getMeal(maid).getCheckItem(), false)) {
                int maxStore = InvUtil.maxCanPlace(maidInv, itemStack);
                if (maxStore > 0) {
                    hasTaken.setTrue();
                    ItemStack copy = itemStack.copyWithCount(1);
                    ViewedInventoryUtil.ambitiousRemoveItemAndSync(maid, level, target, itemStack, 1);
                    InvUtil.tryPlace(maidInv, copy);
                    //放完了直接开吃。
                    super.start(level, maid, p_22553_);
                    MemoryUtil.getMeal(maid).setEating(true);
                    this.endTimeStamp = p_22553_ + 150L;
                    return itemStack.copyWithCount(itemStack.getCount() - 1);
                }
            }
            return itemStack;
        };
        if (context instanceof IStorageInteractContext isic) {
            isic.tick(taker);
        } else if (context instanceof IStorageExtractableContext isec) {
            if (isec.hasTask())
                isec.tick(taker);
            else {
                isec.setExtract(List.of(MemoryUtil.getMeal(maid).getCheckItem()), true);
            }
        }
    }

    @Override
    protected void stop(ServerLevel level, EntityMaid maid, long p_22550_) {
        if (context != null)
            context.finish();
        MemoryUtil.setWorking(maid, false);
        MemoryUtil.getMeal(maid).clearTarget();
        MemoryUtil.getMeal(maid).setCoolDown(40);
        MemoryUtil.getMeal(maid).setEating(false);
        MemoryUtil.clearTarget(maid);
    }

    @Override
    protected boolean timedOut(long p_22537_) {
        if (endTimeStamp == -1) return false;
        return p_22537_ > endTimeStamp;
    }
}
