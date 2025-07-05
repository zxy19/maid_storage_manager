package studio.fantasyit.maid_storage_manager.maid.behavior.logistics.recycle;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
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
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;

import java.util.Map;
import java.util.Objects;

public class LogisticsRecycleBehavior extends Behavior<EntityMaid> {

    public LogisticsRecycleBehavior() {
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
        if (MemoryUtil.getLogistics(maid).getStage() != LogisticsMemory.Stage.RECYCLE) return false;
        if (layer == null) return false;
        if (currentSlot >= maid.getAvailableInv(false).getSlots())
            return false;
        return (context != null && !context.isDone());
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel p_22538_, EntityMaid maid) {
        if (MemoryUtil.getCurrentlyWorking(maid) != ScheduleBehavior.Schedule.LOGISTICS) return false;
        if (MemoryUtil.getLogistics(maid).getCurrentLogisticsGuideItem().isEmpty()) return false;
        if (MemoryUtil.getLogistics(maid).getStage() != LogisticsMemory.Stage.RECYCLE) return false;
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

            layer = MemoryUtil.getLogistics(maid).getCraftLayer();
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
            if (!stack.isEmpty() && inInput(stack))
                if (context instanceof IStorageInsertableContext isic) {
                    ItemStack notInserted = isic.insert(stack);
                    availableInv.setStackInSlot(currentSlot, notInserted);
                }
            currentSlot++;
        }


    }

    protected boolean inInput(ItemStack stack) {
        if (layer != null) {
            return layer.getItems().stream().anyMatch(itemStack -> ItemStackUtil.isSame(itemStack, stack, false));
        }
        return false;
    }

    @Override
    protected void stop(@NotNull ServerLevel level, @NotNull EntityMaid maid, long p_22550_) {
        super.stop(level, maid, p_22550_);
        if (context != null)
            context.finish();

        MemoryUtil.getLogistics(maid).setStage(LogisticsMemory.Stage.FINISH);
        MemoryUtil.getLogistics(maid).clearTarget();
        MemoryUtil.clearTarget(maid);
    }

    @Override
    protected boolean timedOut(long p_22537_) {
        return false;
    }
}
