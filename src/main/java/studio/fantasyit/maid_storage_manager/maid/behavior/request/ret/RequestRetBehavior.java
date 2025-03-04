package studio.fantasyit.maid_storage_manager.maid.behavior.request.ret;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.wrapper.RangedWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.items.RequestListItem;
import studio.fantasyit.maid_storage_manager.maid.behavior.ScheduleBehavior;
import studio.fantasyit.maid_storage_manager.maid.memory.RequestProgressMemory;
import studio.fantasyit.maid_storage_manager.storage.MaidStorage;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageContext;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageInsertableContext;
import studio.fantasyit.maid_storage_manager.util.*;

import java.util.Map;
import java.util.Objects;

public class RequestRetBehavior extends Behavior<EntityMaid> {
    private final BehaviorBreath breath = new BehaviorBreath();

    @Nullable IStorageContext context;
    int currentSlot = 0;

    public RequestRetBehavior() {
        super(Map.of(), 10000000);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel p_22538_, EntityMaid maid) {
        if (MemoryUtil.getCurrentlyWorking(maid) != ScheduleBehavior.Schedule.REQUEST) return false;
        if (!MemoryUtil.getRequestProgress(maid).isReturning()) return false;
        return Conditions.hasReachedValidTargetOrReset(maid);
    }

    @Override
    protected boolean canStillUse(ServerLevel p_22545_, EntityMaid maid, long p_22547_) {
        if (currentSlot >= maid.getAvailableBackpackInv().getSlots())
            return false;
        return context != null && !context.isDone();
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long p_22542_) {
        RequestProgressMemory requestProgress = MemoryUtil.getRequestProgress(maid);
        if (requestProgress.isReturning() && requestProgress.hasTarget()) {
            BlockPos targetPos = requestProgress.getTargetPos();
            ResourceLocation type = requestProgress.getTargetType();
            context = Objects.requireNonNull(MaidStorage.getInstance().getStorage(type)).onStartPlace(level, maid, targetPos);
            if (context != null)
                context.start(maid, level, targetPos);

        }
        currentSlot = 0;
    }

    @Override
    protected void tick(ServerLevel p_22551_, EntityMaid maid, long p_22553_) {
        if (!breath.breathTick()) return;
        super.tick(p_22551_, maid, p_22553_);
        RangedWrapper availableBackpackInv = maid.getAvailableBackpackInv();
        if (currentSlot < availableBackpackInv.getSlots()) {
            ItemStack stack = availableBackpackInv.getStackInSlot(currentSlot);
            if (!stack.isEmpty())
                if (context instanceof IStorageInsertableContext isic) {
                    int i = RequestListItem.updateStored(maid.getMainHandItem(), stack, true);
                    int canStoreCount = stack.getCount() - i;
                    ItemStack notInserted = isic.insert(stack.copyWithCount(canStoreCount));
                    ItemStack toStoreItemStack = stack.copyWithCount(canStoreCount - notInserted.getCount());
                    RequestListItem.updateStored(maid.getMainHandItem(), toStoreItemStack, false);
                    availableBackpackInv.setStackInSlot(currentSlot, stack.copyWithCount(stack.getCount() - toStoreItemStack.getCount()));
                }
            currentSlot++;
        }
    }

    @Override
    protected void stop(@NotNull ServerLevel level, @NotNull EntityMaid maid, long p_22550_) {
        super.stop(level, maid, p_22550_);
        if (Conditions.listAllStored(maid) && Conditions.listAllDone(maid)) {
            RequestItemUtil.stopJobAndStoreOrThrowItem(maid, context);
        }
        if (context != null)
            context.finish();
        RequestListItem.updateCollectedNotStored(maid.getMainHandItem(), maid.getAvailableBackpackInv());
        MemoryUtil.getRequestProgress(maid).setReturn(false);
        MemoryUtil.getRequestProgress(maid).clearTarget();
        MemoryUtil.clearTarget(maid);

        //莫名其妙没空了（被扔垃圾了），那就先扔掉清单好勒
        if (!InvUtil.hasAnyFree(maid.getAvailableBackpackInv())) {
            RequestItemUtil.stopJobAndStoreOrThrowItem(maid, null);
        }
    }
}