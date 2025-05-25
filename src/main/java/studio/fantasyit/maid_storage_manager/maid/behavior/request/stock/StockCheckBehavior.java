package studio.fantasyit.maid_storage_manager.maid.behavior.request.stock;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.items.RequestListItem;
import studio.fantasyit.maid_storage_manager.maid.behavior.ScheduleBehavior;
import studio.fantasyit.maid_storage_manager.storage.MaidStorage;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageContext;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageInteractContext;
import studio.fantasyit.maid_storage_manager.util.BehaviorBreath;
import studio.fantasyit.maid_storage_manager.util.Conditions;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;

import java.util.Map;

public class StockCheckBehavior extends Behavior<EntityMaid> {

    private final BehaviorBreath breath = new BehaviorBreath();
    IStorageContext context;
    private Target target;

    public StockCheckBehavior() {
        super(Map.of());
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel p_22538_, EntityMaid maid) {
        if (MemoryUtil.getCurrentlyWorking(maid) != ScheduleBehavior.Schedule.REQUEST) return false;
        if (!MemoryUtil.getRequestProgress(maid).isCheckingStock()) return false;
        return Conditions.hasReachedValidTargetOrReset(maid);
    }

    @Override
    protected boolean canStillUse(ServerLevel p_22545_, EntityMaid maid, long p_22547_) {
        if (MemoryUtil.getCurrentlyWorking(maid) != ScheduleBehavior.Schedule.REQUEST) return false;
        return context != null && !context.isDone();
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long p_22542_) {
        if (!MemoryUtil.getRequestProgress(maid).hasTarget()) return;
        target = MemoryUtil.getRequestProgress(maid).getTarget();
        context = MaidStorage
                .getInstance()
                .getStorage(target.type)
                .onStartView(level, maid, target);
        if (context != null) {
            context.start(maid, level, target);
        }
    }

    @Override
    protected void tick(ServerLevel p_22551_, EntityMaid maid, long p_22553_) {
        if (!breath.breathTick(maid)) return;
        super.tick(p_22551_, maid, p_22553_);
        if (context instanceof IStorageInteractContext isic) {
            isic.tick(itemStack -> {
                ItemStack rest = RequestListItem.updateCollectedItem(maid.getMainHandItem(), itemStack.copy(), itemStack.getCount());
                if (rest.getCount() != itemStack.getCount()) {
                    RequestListItem.updateStored(maid.getMainHandItem(),
                            itemStack.copyWithCount(itemStack.getCount() - rest.getCount()),
                            false);
                }
                return itemStack;
            });
        }
    }

    @Override
    protected void stop(ServerLevel level, EntityMaid maid, long p_22550_) {
        super.stop(level, maid, p_22550_);
        if (context != null) {
            context.finish();
        }

        MemoryUtil.getRequestProgress(maid).addVisitedPos(target);
        MemoryUtil.getRequestProgress(maid).setCheckingStock(false);
        MemoryUtil.getViewedInventory(maid).clearTarget();
        MemoryUtil.clearTarget(maid);
        RequestListItem.setHasCheckedStock(maid.getMainHandItem(), true);
    }
}
