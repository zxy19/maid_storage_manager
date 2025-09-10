package studio.fantasyit.maid_storage_manager.maid.behavior.request.ret;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.debug.DebugData;
import studio.fantasyit.maid_storage_manager.entity.VirtualItemEntity;
import studio.fantasyit.maid_storage_manager.items.RequestListItem;
import studio.fantasyit.maid_storage_manager.maid.ChatTexts;
import studio.fantasyit.maid_storage_manager.maid.behavior.ScheduleBehavior;
import studio.fantasyit.maid_storage_manager.maid.memory.RequestProgressMemory;
import studio.fantasyit.maid_storage_manager.storage.MaidStorage;
import studio.fantasyit.maid_storage_manager.storage.StorageVisitLock;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageContext;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageInsertableContext;
import studio.fantasyit.maid_storage_manager.util.*;

import java.util.Map;
import java.util.Objects;

public class RequestRetBehavior extends Behavior<EntityMaid> {
    private final BehaviorBreath breath = new BehaviorBreath();

    @Nullable IStorageContext context;
    int currentSlot = 0;
    private Target target;
    private Entity targetEntity;
    private boolean targetEntityReady = false;
    private VirtualItemEntity thrown;
    boolean inCrafting = false;
    private StorageVisitLock.LockContext lock = StorageVisitLock.DUMMY;

    public RequestRetBehavior() {
        super(Map.of());
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel p_22538_, EntityMaid maid) {
        if (MemoryUtil.getCurrentlyWorking(maid) != ScheduleBehavior.Schedule.REQUEST) return false;
        if (MemoryUtil.isWorking(maid)) return false;
        if (!MemoryUtil.getRequestProgress(maid).isReturning()) return false;
        return Conditions.hasReachedValidTargetOrReset(maid);
    }

    @Override
    protected boolean canStillUse(ServerLevel p_22545_, EntityMaid maid, long p_22547_) {
        if (thrown != null) return true;
        if (currentSlot >= maid.getAvailableInv(false).getSlots())
            return false;
        return (context != null && !context.isDone()) || targetEntity != null;
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long p_22542_) {
        MemoryUtil.setWorking(maid, true);
        lock = StorageVisitLock.DUMMY;
        context = null;
        targetEntity = null;
        thrown = null;
        RequestProgressMemory requestProgress = MemoryUtil.getRequestProgress(maid);
        if (requestProgress.isReturning()) {
            if (requestProgress.hasTarget()) {
                requestProgress.addTries();
                target = requestProgress.getTarget();
                context = Objects.requireNonNull(MaidStorage
                                .getInstance()
                                .getStorage(target.getType()))
                        .onStartPlace(level, maid, target);
                if (context != null)
                    context.start(maid, level, target);
                lock = StorageVisitLock.getWriteLock(target, maid);
            } else if (requestProgress.getTargetEntityUUID().isPresent()) {
                targetEntity = level.getEntity(requestProgress.getTargetEntityUUID().get());
                if (targetEntity instanceof EntityMaid m) {
                    targetEntityReady = false;
                    tryReadyMaid(m, maid);
                }
            }
        }
        currentSlot = 0;
        inCrafting = MemoryUtil.getCrafting(maid).hasPlan();
    }

    protected boolean tryReadyMaid(EntityMaid m, EntityMaid maid) {
        if (targetEntityReady) return true;
        if (MemoryUtil.isWorking(m) && !MemoryUtil.isParallelWorking(m)) return false;
        MemoryUtil.joinAndStartParallelWorking(m);
        m.getNavigation().stop();
        MemoryUtil.setTarget(m, m, (float) Config.collectSpeed);
        targetEntityReady = true;
        InvUtil.mergeSameStack(m.getAvailableInv(true));
        return true;
    }

    @Override
    protected void tick(ServerLevel p_22551_, EntityMaid maid, long p_22553_) {
        if (!breath.breathTick(maid)) return;
        super.tick(p_22551_, maid, p_22553_);
        if (context != null) this.tickStorageContext(maid);
        if (targetEntity != null) this.tickTargetEntity(maid);
    }

    private void tickTargetEntity(EntityMaid maid) {
        if (targetEntity instanceof EntityMaid m) {
            if (!tryReadyMaid(m, maid))
                return;
            else
                MemoryUtil.setLookAt(m, maid);
        }
        if (thrown != null) {
            if (targetEntity instanceof EntityMaid targetMaid) {
                DebugData.invChange(DebugData.InvChange.IN, targetMaid, thrown.getItem());
                InvUtil.pickUpVirtual(targetMaid, thrown);
                DebugData.invChange(DebugData.InvChange.CURRENT, targetMaid, ItemStack.EMPTY);
                if (!thrown.isAlive()) {
                    thrown = null;
                }
            } else
                thrown = null;
            breath.reset();
            return;
        }
        CombinedInvWrapper inv = maid.getAvailableInv(false);
        Vec3 targetDir = MathUtil.getFromToWithFriction(maid, targetEntity.getPosition(0));
        for (int i = 0; i < 5 && targetEntity != null && inv.getSlots() > currentSlot; i++) {
            @NotNull ItemStack item = inv.getStackInSlot(currentSlot++);
            int restCount = RequestListItem.updateStored(maid.getMainHandItem(), item, false, inCrafting);
            ItemStack toThrowStack = item.copy();
            toThrowStack.shrink(restCount);
            if (!toThrowStack.isEmpty()) {
                item.setCount(restCount);
                if (targetEntity instanceof EntityMaid) {
                    thrown = InvUtil.throwItemVirtual(maid, toThrowStack, targetDir);
                    DebugData.invChange(DebugData.InvChange.OUT, maid, toThrowStack);
                } else
                    InvUtil.throwItem(maid, toThrowStack, targetDir, true);
                inv.setStackInSlot(currentSlot - 1, item);
                break;
            }
        }
    }

    private void tickStorageContext(EntityMaid maid) {
        if (!lock.checkAndTryGrantLock()) return;
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
                    int i = RequestListItem.updateStored(maid.getMainHandItem(), stack, true, inCrafting);
                    int canStoreCount = stack.getCount() - i;
                    ItemStack notInserted = isic.insert(stack.copyWithCount(canStoreCount));
                    ItemStack toStoreItemStack = stack.copyWithCount(canStoreCount - notInserted.getCount());
                    RequestListItem.updateStored(maid.getMainHandItem(), toStoreItemStack, false, inCrafting);
                    availableInv.setStackInSlot(currentSlot, stack.copyWithCount(stack.getCount() - toStoreItemStack.getCount()));
                }
            currentSlot++;
        }
    }


    @Override
    protected void stop(@NotNull ServerLevel level, @NotNull EntityMaid maid, long p_22550_) {
        lock.release();
        MemoryUtil.setWorking(maid, false);
        super.stop(level, maid, p_22550_);
        if (context != null)
            context.finish();
        if (targetEntity instanceof EntityMaid m) {
            MemoryUtil.clearTarget(m);
            MemoryUtil.clearPickUpItemTemp(m);
            MemoryUtil.leaveParallelWorking(m);
        }
        //正在合成过程中，合成树还未结束，直接返回合成
        if (MemoryUtil.getCrafting(maid).hasPlan()) {
            MemoryUtil.getRequestProgress(maid).setReturn(false);
            MemoryUtil.getRequestProgress(maid).setTryCrafting(true);
            MemoryUtil.getRequestProgress(maid).clearTarget();
            MemoryUtil.getCrafting(maid).clearTarget();
            MemoryUtil.getCrafting(maid).plan().checkAndSwitchGroup(maid);
            MemoryUtil.clearTarget(maid);
            return;
        }
        if ((Conditions.listAllStored(maid) || Conditions.triesReach(maid)) && Conditions.listAllDone(maid)) {
            if (RequestListItem.isAllSuccess(maid.getMainHandItem()))
                ChatTexts.send(maid, ChatTexts.CHAT_REQUEST_SUCCESS);
            else
                ChatTexts.send(maid, ChatTexts.CHAT_REQUEST_FAIL);

            RequestItemUtil.stopJobAndStoreOrThrowItem(maid, context, targetEntity);

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
            RequestItemUtil.stopJobAndStoreOrThrowItem(maid, null, null);
        }
    }

    @Override
    protected boolean timedOut(long p_22537_) {
        return false;
    }
}