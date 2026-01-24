package studio.fantasyit.maid_storage_manager.maid.behavior.base;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import studio.fantasyit.maid_storage_manager.debug.DebugData;
import studio.fantasyit.maid_storage_manager.maid.memory.AbstractTargetMemory;
import studio.fantasyit.maid_storage_manager.storage.MaidStorage;
import studio.fantasyit.maid_storage_manager.storage.StorageVisitLock;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.storage.base.IMaidStorage;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageContext;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageExtractableContext;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageInteractContext;
import studio.fantasyit.maid_storage_manager.util.*;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public abstract class AbstractGatherBehavior extends Behavior<EntityMaid> {
    BehaviorBreath breath = new BehaviorBreath();
    IStorageContext context;
    private Target target;
    boolean changed = false;
    private StorageVisitLock.LockContext lock;

    public AbstractGatherBehavior() {
        super(Map.of());
    }

    protected abstract AbstractTargetMemory getMemory(EntityMaid maid);

    protected abstract void onStart(ServerLevel level, EntityMaid maid);

    protected abstract void onStop(ServerLevel level, EntityMaid maid);

    protected abstract int getMaxToGet(EntityMaid maid, ItemStack incomingItemstack);

    protected abstract ItemStack getToTakeItemStack(EntityMaid maid, ItemStack toTake,int maxStore);

    protected abstract void onTake(EntityMaid maid, ItemStack itemStack);

    protected abstract List<ItemStack> getToTakeItems(EntityMaid maid);

    @Override
    protected boolean canStillUse(ServerLevel p_22545_, EntityMaid p_22546_, long p_22547_) {
        return context != null && !context.isDone();
    }

    @Override
    protected void start(@NotNull ServerLevel level, @NotNull EntityMaid maid, long gameTimeIn) {
        lock = StorageVisitLock.DUMMY;
        if (!getMemory(maid).hasTarget()) return;
        target = getMemory(maid).getTarget();
        if (target == null) return;

        IMaidStorage storage = MaidStorage.getInstance().getStorage(target.getType());
        if (storage == null)
            return;

        changed = false;
        context = storage.onStartCollect(level, maid, target);
        if (context != null)
            context.start(maid, level, target);

        onStart(level, maid);

        InvUtil.mergeSameStack(maid.getAvailableInv(true));
        lock = StorageVisitLock.getReadLock(target, maid);
        MemoryUtil.setWorking(maid, true);
    }

    @Override
    protected void tick(ServerLevel level, EntityMaid maid, long p_22553_) {
        if (!lock.checkAndTryGrantLock()) return;
        if (!breath.breathTick(maid)) return;
        Function<ItemStack, ItemStack> taker = (ItemStack itemStack) -> {
            int maxStore = Math.min(itemStack.getCount(), InvUtil.maxCanPlace(maid.getAvailableInv(false), itemStack));
            if (maxStore > 0) {
                maxStore = Math.min(maxStore, getMaxToGet(maid, itemStack));
            }
            if (maxStore > 0) {
                ItemStack copy = itemStack.copy();
                ItemStack toTake = getToTakeItemStack(maid, copy, maxStore);
                if (toTake.getCount() > 0) {
                    changed = true;
                    onTake(maid, toTake);
                }
                copy.shrink(toTake.getCount());
                ViewedInventoryUtil.ambitiousRemoveItemAndSync(maid, level, target, itemStack, toTake.getCount());
                InvUtil.tryPlace(maid.getAvailableInv(false), toTake);
                DebugData.invChange(DebugData.InvChange.IN, maid, toTake);
                return copy;
            }
            return itemStack;
        };
        if (context instanceof IStorageInteractContext isic) {
            isic.tick(taker);
        } else if (context instanceof IStorageExtractableContext isec) {
            if (isec.hasTask())
                isec.tick(taker);
            else
                isec.setExtract(getToTakeItems(maid), ItemStackUtil.MATCH_TYPE.AUTO);
        }
    }

    @Override
    protected void stop(ServerLevel level, EntityMaid maid, long p_22550_) {
        lock.release();
        MemoryUtil.setWorking(maid, false);
        if (context != null) {
            context.finish();
            if (context.isDone() && !changed) {
                Target target = getMemory(maid).getTarget();
                getMemory(maid).addVisitedPos(target);
                StorageAccessUtil.checkNearByContainers(level, target.getPos(), (pos) -> {
                    getMemory(maid).addVisitedPos(target.sameType(pos, null));
                });
            }
        }
        super.stop(level, maid, p_22550_);
        getMemory(maid).clearCheckItem();
        getMemory(maid).clearTarget();
        MemoryUtil.clearTarget(maid);
        onStop(level, maid);
    }

    @Override
    protected boolean timedOut(long p_22537_) {
        return false;
    }
}
