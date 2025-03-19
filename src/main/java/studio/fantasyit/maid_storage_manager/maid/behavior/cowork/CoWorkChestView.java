package studio.fantasyit.maid_storage_manager.maid.behavior.cowork;

import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidCheckRateTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.maid.behavior.ScheduleBehavior;
import studio.fantasyit.maid_storage_manager.registry.MemoryModuleRegistry;
import studio.fantasyit.maid_storage_manager.storage.MaidStorage;
import studio.fantasyit.maid_storage_manager.storage.Storage;
import studio.fantasyit.maid_storage_manager.storage.base.IFilterable;
import studio.fantasyit.maid_storage_manager.storage.base.IMaidStorage;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageContext;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageInteractContext;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;
import studio.fantasyit.maid_storage_manager.util.MoveUtil;

import java.util.List;
import java.util.Map;

public class CoWorkChestView extends MaidCheckRateTask {
    public CoWorkChestView() {
        super(Map.of(MemoryModuleRegistry.CO_WORK_TARGET_STORAGE.get(), MemoryStatus.VALUE_PRESENT));
        this.setMaxCheckRate(20);
    }

    Storage target;
    IStorageContext context;
    int currentHoldingContainerId = -1;

    @Override
    protected boolean checkExtraStartConditions(ServerLevel worldIn, EntityMaid maid) {
        if (!super.checkExtraStartConditions(worldIn, maid)) return false;
        return (MemoryUtil.getCurrentlyWorking(maid) == ScheduleBehavior.Schedule.CO_WORK);
    }

    @Override
    protected boolean canStillUse(ServerLevel p_22545_, EntityMaid p_22546_, long p_22547_) {
        return context != null && !context.isDone();
    }

    @Override
    protected void start(@NotNull ServerLevel level,
                         @NotNull EntityMaid maid,
                         long p_22542_) {
        Storage interactedTarget = MemoryUtil.getCoWorkTargetStorage(maid);
        if (interactedTarget == null) {
            maid.getBrain().eraseMemory(MemoryModuleRegistry.CO_WORK_TARGET_STORAGE.get());
            context = null;
            return;
        }
        List<Storage> possibleTargets = MoveUtil.findTargetRewrite(level, maid, interactedTarget.withoutSide());
        if (possibleTargets.contains(interactedTarget))
            target = interactedTarget;
        else if (possibleTargets.size() > 0)
            target = possibleTargets.get(0);
        else {
            context = null;
            return;
        }
        target = MemoryUtil.getViewedInventory(maid).ambitiousPos(level, target);
        MemoryUtil.getViewedInventory(maid).resetViewedInvForPos(target);
        @Nullable IMaidStorage storageType = MaidStorage.getInstance().getStorage(target.getType());
        if (storageType != null)
            context = storageType.onStartView(level, maid, target);
        else context = null;
        if (context == null) {
            maid.getBrain().eraseMemory(MemoryModuleRegistry.CO_WORK_TARGET_STORAGE.get());
            context = null;
            return;
        }
        currentHoldingContainerId = -1;
        if (maid.getOwner() instanceof ServerPlayer sp) {
            if (sp.hasContainerOpen()) {
                currentHoldingContainerId = sp.containerMenu.containerId;
            }
        }
        context.start(maid, level, target);
        if (context instanceof IFilterable ift) {
            if (ift.isRequestOnly()) {
                context.finish();
                maid.getBrain().eraseMemory(MemoryModuleRegistry.CO_WORK_TARGET_STORAGE.get());
                context = null;
                return;
            }
        }
        MemoryUtil.setLookAt(maid, target.pos);
    }

    @Override
    protected void tick(ServerLevel p_22551_, EntityMaid maid, long p_22553_) {
        MemoryUtil.setLookAt(maid, target.pos);
        if (context instanceof IStorageInteractContext context) {
            context.tick(itemStack -> {
                MemoryUtil.getViewedInventory(maid).addItem(target, itemStack);
                return itemStack;
            });
        }

        //玩家没有关掉箱子，那女仆应当持续扫描
        if (context.isDone()) {
            if (maid.getOwner() instanceof ServerPlayer sp) {
                if (sp.hasContainerOpen() && sp.containerMenu.containerId == currentHoldingContainerId) {
                    context.reset();
                    MemoryUtil.getViewedInventory(maid).resetViewedInvForPos(target);
                }
            }
        }
    }

    @Override
    protected void stop(ServerLevel p_22548_, EntityMaid maid, long p_22550_) {
        if (MemoryUtil.getCoWorkTargetStorage(maid) != null)
            setNextCheckTickCount(5);
        if (context != null) {
            context.finish();
            context = null;
        }
        if (maid.getOwner() instanceof ServerPlayer sp) {
            if (sp.hasContainerOpen()) {
                return;
            }
        }
        maid.getBrain().eraseMemory(MemoryModuleRegistry.CO_WORK_TARGET_STORAGE.get());
    }

    @Override
    protected boolean timedOut(long p_22537_) {
        return false;
    }
}
