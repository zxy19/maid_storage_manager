package studio.fantasyit.maid_storage_manager.maid.behavior.view;

import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidCheckRateTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import studio.fantasyit.maid_storage_manager.advancement.AdvancementTypes;
import studio.fantasyit.maid_storage_manager.maid.ChatTexts;
import studio.fantasyit.maid_storage_manager.maid.behavior.ScheduleBehavior;
import studio.fantasyit.maid_storage_manager.storage.MaidStorage;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.storage.base.IFilterable;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageContext;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageInteractContext;
import studio.fantasyit.maid_storage_manager.util.BehaviorBreath;
import studio.fantasyit.maid_storage_manager.util.Conditions;
import studio.fantasyit.maid_storage_manager.util.InvUtil;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ViewBehavior extends MaidCheckRateTask {
    private final BehaviorBreath breath = new BehaviorBreath();
    private IStorageContext context = null;
    Target target = null;
    List<ItemStack> mismatchFilter = new ArrayList<>();

    public ViewBehavior() {
        super(Map.of());
        this.setMaxCheckRate(5);
    }

    @Override
    protected boolean checkExtraStartConditions(@NotNull ServerLevel worldIn, @NotNull EntityMaid owner) {
        if (!super.checkExtraStartConditions(worldIn, owner)) return false;
        if (MemoryUtil.getCurrentlyWorking(owner) != ScheduleBehavior.Schedule.VIEW) return false;
        if (!MemoryUtil.getViewedInventory(owner).hasTarget()) return false;
        return Conditions.hasReachedValidTargetOrReset(owner);
    }

    @Override
    protected boolean canStillUse(ServerLevel p_22545_, EntityMaid maid, long p_22547_) {
        if (MemoryUtil.getCurrentlyWorking(maid) != ScheduleBehavior.Schedule.VIEW) return false;
        if (!Conditions.isNothingToPlace(maid)) return false;
        return context != null && !context.isDone();
    }

    @Override
    protected void start(@NotNull ServerLevel level, @NotNull EntityMaid maid, long p_22542_) {
        if (!MemoryUtil.getViewedInventory(maid).hasTarget()) return;
        target = MemoryUtil.getViewedInventory(maid).getTarget();
        MemoryUtil.getViewedInventory(maid).resetViewedInvForPos(target);
        InvUtil.checkNearByContainers(level, target.getPos(), pos -> {
            MemoryUtil.getViewedInventory(maid).resetViewedInvForPos(target.sameType(pos, null));
        });
        context = MaidStorage
                .getInstance()
                .getStorage(target.type)
                .onStartView(level, maid, target);
        if (context != null) {
            context.start(maid, level, target);
        }
        this.mismatchFilter.clear();
        AdvancementTypes.triggerForMaid(maid, AdvancementTypes.VIEW);
    }

    @Override
    protected void tick(ServerLevel p_22551_, EntityMaid maid, long p_22553_) {
        if (!breath.breathTick()) return;
        super.tick(p_22551_, maid, p_22553_);
        if (context instanceof IStorageInteractContext isic) {
            isic.tick(itemStack -> {
                if (isic instanceof IFilterable filter) {
                    if (!filter.isAvailable(itemStack)) {
                        mismatchFilter.add(itemStack);
                    }
                }
                MemoryUtil.getViewedInventory(maid).addItem(this.target, itemStack);
                return itemStack;
            });
        }
    }

    @Override
    protected void stop(ServerLevel level, EntityMaid maid, long p_22550_) {
        if (context != null) {
            MemoryUtil.getViewedInventory(maid).addVisitedPos(target);
            InvUtil.checkNearByContainers(level, target.getPos(), pos -> {
                MemoryUtil.getViewedInventory(maid).addVisitedPos(target.sameType(pos, null));
            });
            MemoryUtil.getViewedInventory(maid).clearTarget();
            context.finish();
        }
        MemoryUtil.clearTarget(maid);
        LinkedList<Target> markChanged = MemoryUtil.getViewedInventory(maid).getMarkChanged();
        if (!markChanged.isEmpty() && markChanged.peek().equals(target)) {
            markChanged.poll();
        }

        if (!mismatchFilter.isEmpty()) {
            ChatTexts.send(maid, ChatTexts.CHAT_RESORT);
            AdvancementTypes.triggerForMaid(maid, AdvancementTypes.RESORT);
            MemoryUtil.getResorting(maid).setNeedToResort(mismatchFilter);
            MemoryUtil.getResorting(maid).setTarget(target);
            MemoryUtil.getResorting(maid).addVisitedPos(target);
            InvUtil.checkNearByContainers(level, target.getPos(), pos -> {
                MemoryUtil.getResorting(maid).addVisitedPos(target.sameType(pos, null));
            });
        }
    }
    @Override
    protected boolean timedOut(long p_22537_) {
        return false;
    }
}
