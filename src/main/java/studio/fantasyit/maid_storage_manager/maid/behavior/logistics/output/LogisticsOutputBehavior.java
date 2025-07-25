package studio.fantasyit.maid_storage_manager.maid.behavior.logistics.output;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.craft.work.CraftLayer;
import studio.fantasyit.maid_storage_manager.maid.behavior.ScheduleBehavior;
import studio.fantasyit.maid_storage_manager.maid.memory.LogisticsMemory;
import studio.fantasyit.maid_storage_manager.storage.MaidStorage;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageContext;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageInsertableContext;
import studio.fantasyit.maid_storage_manager.util.BehaviorBreath;
import studio.fantasyit.maid_storage_manager.util.Conditions;
import studio.fantasyit.maid_storage_manager.util.InvUtil;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;

import java.util.Map;
import java.util.Objects;

public class LogisticsOutputBehavior extends Behavior<EntityMaid> {

    public LogisticsOutputBehavior() {
        super(Map.of());
    }

    private final BehaviorBreath breath = new BehaviorBreath();

    @Nullable IStorageContext context;
    int currentSlot = 0;
    private Target target;
    CraftLayer layer = null;

    @Override
    protected boolean canStillUse(ServerLevel p_22545_, EntityMaid maid, long p_22547_) {
        if (MemoryUtil.getCurrentlyWorking(maid) != ScheduleBehavior.Schedule.LOGISTICS) return false;
        if (!MemoryUtil.getLogistics(maid).shouldWork()) return false;
        if (MemoryUtil.getLogistics(maid).getStage() != LogisticsMemory.Stage.OUTPUT) return false;
        if (layer == null || layer.hasCollectedAll()) return false;
        if (currentSlot >= maid.getAvailableInv(false).getSlots())
            return false;
        return (context != null && !context.isDone());
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel p_22538_, EntityMaid maid) {
        if (MemoryUtil.getCurrentlyWorking(maid) != ScheduleBehavior.Schedule.LOGISTICS) return false;
        if (MemoryUtil.getLogistics(maid).getCurrentLogisticsGuideItem().isEmpty()) return false;
        if (MemoryUtil.getLogistics(maid).getStage() != LogisticsMemory.Stage.OUTPUT) return false;
        return Conditions.hasReachedValidTargetOrReset(maid);
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long p_22542_) {
        if (MemoryUtil.getLogistics(maid).hasTarget()) {
            target = MemoryUtil.getLogistics(maid).getTarget();
            context = Objects.requireNonNull(MaidStorage
                            .getInstance()
                            .getStorage(target.getType()))
                    .onStartPlace(level, maid, target);
            if (context != null)
                context.start(maid, level, target);

            layer = MemoryUtil.getLogistics(maid).getResultLayer();
        }

        currentSlot = 0;
    }

    @Override
    protected void tick(ServerLevel p_22551_, EntityMaid maid, long p_22553_) {
        if (!breath.breathTick(maid)) return;
        CombinedInvWrapper availableInv = maid.getAvailableInv(true);

        for (int i = 0; i < 5 && currentSlot < availableInv.getSlots(); i++)
            if (availableInv.getStackInSlot(currentSlot).isEmpty())
                currentSlot++;
        if (currentSlot < availableInv.getSlots()) {
            ItemStack stack = availableInv.getStackInSlot(currentSlot);
            if (!stack.isEmpty())
                if (context instanceof IStorageInsertableContext isic) {
                    int toInsert = layer.memorizeItemSimulate(stack);
                    ItemStack notInserted = isic.insert(stack.copyWithCount(toInsert));
                    layer.memorizeItem(stack.copyWithCount(toInsert - notInserted.getCount()), Integer.MAX_VALUE);
                    int restCount = stack.getCount() - toInsert + notInserted.getCount();
                    availableInv.setStackInSlot(currentSlot, stack.copyWithCount(restCount));
                }
            currentSlot++;
        }
        if (context != null)
            if (currentSlot >= availableInv.getSlots() || context.isDone()) {
                // 女仆还没有放置所有的物品，而且女仆只有这一个任务，那么继续等待直到物品放置完成
                if (!InvUtil.isEmpty(availableInv) && !MemoryUtil.getLogistics(maid).hasMultipleGuide(maid)) {
                    if (currentSlot >= availableInv.getSlots()) currentSlot = 0;
                    if (context.isDone()) context.reset();
                }
            }
    }

    @Override
    protected void stop(@NotNull ServerLevel level, @NotNull EntityMaid maid, long p_22550_) {
        super.stop(level, maid, p_22550_);
        if (context != null)
            context.finish();
        if (Conditions.isNothingToPlace(maid))
            MemoryUtil.getLogistics(maid).setStage(LogisticsMemory.Stage.FINISH);
        else
            MemoryUtil.getLogistics(maid).setStage(LogisticsMemory.Stage.RECYCLE);
        MemoryUtil.getLogistics(maid).clearTarget();
        MemoryUtil.clearTarget(maid);
    }

    @Override
    protected boolean timedOut(long p_22537_) {
        return false;
    }

}
