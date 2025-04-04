package studio.fantasyit.maid_storage_manager.maid.behavior.request.ret;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.items.RequestListItem;
import studio.fantasyit.maid_storage_manager.maid.ChatTexts;
import studio.fantasyit.maid_storage_manager.maid.behavior.ScheduleBehavior;
import studio.fantasyit.maid_storage_manager.maid.memory.RequestProgressMemory;
import studio.fantasyit.maid_storage_manager.storage.MaidStorage;
import studio.fantasyit.maid_storage_manager.storage.Storage;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageContext;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageInsertableContext;
import studio.fantasyit.maid_storage_manager.util.*;

import java.util.Map;
import java.util.Objects;

public class RequestRetBehavior extends Behavior<EntityMaid> {
    private final BehaviorBreath breath = new BehaviorBreath();

    @Nullable IStorageContext context;
    int currentSlot = 0;
    private Storage target;

    public RequestRetBehavior() {
        super(Map.of());
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel p_22538_, EntityMaid maid) {
        if (MemoryUtil.getCurrentlyWorking(maid) != ScheduleBehavior.Schedule.REQUEST) return false;
        if (!MemoryUtil.getRequestProgress(maid).isReturning()) return false;
        return Conditions.hasReachedValidTargetOrReset(maid);
    }

    @Override
    protected boolean canStillUse(ServerLevel p_22545_, EntityMaid maid, long p_22547_) {
        if (currentSlot >= maid.getAvailableInv(false).getSlots())
            return false;
        return context != null && !context.isDone();
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long p_22542_) {
        RequestProgressMemory requestProgress = MemoryUtil.getRequestProgress(maid);
        if (requestProgress.isReturning() && requestProgress.hasTarget()) {
            requestProgress.addTries();
            target = requestProgress.getTarget();
            context = Objects.requireNonNull(MaidStorage
                            .getInstance()
                            .getStorage(target.getType()))
                    .onStartPlace(level, maid, target);
            if (context != null)
                context.start(maid, level, target);
        }
        currentSlot = 0;
    }

    @Override
    protected void tick(ServerLevel p_22551_, EntityMaid maid, long p_22553_) {
        if (!breath.breathTick()) return;
        super.tick(p_22551_, maid, p_22553_);
        CombinedInvWrapper availableInv = maid.getAvailableInv(true);

        for (int i = 0; i < 5 && currentSlot < availableInv.getSlots(); i++)
            if (availableInv.getStackInSlot(currentSlot).isEmpty())
                currentSlot++;
        if (availableInv.getStackInSlot(currentSlot) == maid.getMainHandItem())
            currentSlot++;
        if (currentSlot < availableInv.getSlots()) {
            ItemStack stack = availableInv.getStackInSlot(currentSlot);
            if (!stack.isEmpty())
                if (context instanceof IStorageInsertableContext isic) {
                    int i = RequestListItem.updateStored(maid.getMainHandItem(), stack, true);
                    int canStoreCount = stack.getCount() - i;
                    ItemStack notInserted = isic.insert(stack.copyWithCount(canStoreCount));
                    ItemStack toStoreItemStack = stack.copyWithCount(canStoreCount - notInserted.getCount());
                    RequestListItem.updateStored(maid.getMainHandItem(), toStoreItemStack, false);
                    availableInv.setStackInSlot(currentSlot, stack.copyWithCount(stack.getCount() - toStoreItemStack.getCount()));
                }
            currentSlot++;
        }
    }

    @Override
    protected void stop(@NotNull ServerLevel level, @NotNull EntityMaid maid, long p_22550_) {
        super.stop(level, maid, p_22550_);
        if (context != null)
            context.finish();
        if (MemoryUtil.getCrafting(maid).hasTasks()) {
            MemoryUtil.getRequestProgress(maid).setReturn(false);
            MemoryUtil.getRequestProgress(maid).setTryCrafting(true);
            MemoryUtil.getRequestProgress(maid).clearTarget();
            MemoryUtil.getCrafting(maid).clearTarget();
            MemoryUtil.clearTarget(maid);
            return;
        }
        if ((Conditions.listAllStored(maid) || Conditions.triesReach(maid)) && Conditions.listAllDone(maid)) {
            if (RequestListItem.isAllSuccess(maid.getMainHandItem()))
                ChatTexts.send(maid, ChatTexts.CHAT_REQUEST_SUCCESS);
            else
                ChatTexts.send(maid, ChatTexts.CHAT_REQUEST_FAIL);

            RequestItemUtil.stopJobAndStoreOrThrowItem(maid, context);
            if (target != null) {
                MemoryUtil.setInteractPos(maid, target.getPos().above());
            }
        }
        RequestListItem.updateCollectedNotStored(maid.getMainHandItem(), maid.getAvailableInv(false));
        MemoryUtil.getRequestProgress(maid).setReturn(false);
        MemoryUtil.getRequestProgress(maid).clearTarget();
        MemoryUtil.getCrafting(maid).clearTarget();
        MemoryUtil.clearTarget(maid);

        //莫名其妙没空了（被扔垃圾了），那就先扔掉清单好勒
        if (!InvUtil.hasAnyFree(maid.getAvailableInv(false))) {
            RequestItemUtil.stopJobAndStoreOrThrowItem(maid, null);
        }
    }
    @Override
    protected boolean timedOut(long p_22537_) {
        return false;
    }
}