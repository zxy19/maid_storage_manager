package studio.fantasyit.maid_storage_manager.maid.behavior.request.find;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.items.RequestListItem;
import studio.fantasyit.maid_storage_manager.maid.behavior.ScheduleBehavior;
import studio.fantasyit.maid_storage_manager.maid.memory.RequestProgressMemory;
import studio.fantasyit.maid_storage_manager.storage.MaidStorage;
import studio.fantasyit.maid_storage_manager.storage.base.IMaidStorage;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageContext;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageExtractableContext;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageInteractContext;
import studio.fantasyit.maid_storage_manager.util.*;

import java.util.*;

public class RequestFindBehavior extends Behavior<EntityMaid> {
    BehaviorBreath breath = new BehaviorBreath();
    IStorageContext context;

    public RequestFindBehavior() {
        super(Map.of(), 10000000);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, EntityMaid maid, long p_22547_) {
        if (!Conditions.takingRequestList(maid)) return false;
        if (MemoryUtil.getRequestProgress(maid).isReturning()) return false;
        if (!InvUtil.hasAnyFree(maid.getAvailableBackpackInv())) return false;
        return context != null && !context.isDone();
    }

    @Override
    protected boolean checkExtraStartConditions(@NotNull ServerLevel level, @NotNull EntityMaid maid) {
        if (MemoryUtil.getCurrentlyWorking(maid) != ScheduleBehavior.Schedule.REQUEST) return false;
        if (!MemoryUtil.getRequestProgress(maid).hasTarget()) return false;
        if (MemoryUtil.getRequestProgress(maid).isReturning()) return false;
        return Conditions.hasReachedValidTargetOrReset(maid);
    }


    @Override
    protected void start(@NotNull ServerLevel level, @NotNull EntityMaid maid, long gameTimeIn) {
        IMaidStorage storage = Objects.requireNonNull(MaidStorage.getInstance().getStorage(MemoryUtil.getRequestProgress(maid).getTargetType()));
        if (!MemoryUtil.getRequestProgress(maid).hasTarget()) return;
        ResourceLocation type = MemoryUtil.getRequestProgress(maid).getTargetType();
        BlockPos pos = MemoryUtil.getRequestProgress(maid).getTargetPos();

        context = storage.onStartCollect(level, maid, pos);
        if (context != null)
            context.start(maid, level, pos);
    }

    @Override
    protected void stop(ServerLevel level, EntityMaid maid, long p_22550_) {
        super.stop(level, maid, p_22550_);
        if (context != null) {
            context.finish();
            if (context.isDone()) {
                BlockPos targetPos = MemoryUtil.getRequestProgress(maid).getTargetPos();
                MemoryUtil.getRequestProgress(maid).addVisitedPos(targetPos);
                InvUtil.checkNearByContainers(level, targetPos, (pos) -> {
                    MemoryUtil.getRequestProgress(maid).addVisitedPos(pos);
                });
            }
        }
        MemoryUtil.getRequestProgress(maid).clearTarget();
        MemoryUtil.clearTarget(maid);
    }

    @Override
    protected void tick(ServerLevel level, EntityMaid maid, long p_22553_) {
        if (!breath.breathTick()) return;
        if (context instanceof IStorageInteractContext isic) {
            isic.tick(itemStack -> {
                int maxStore = InvUtil.maxCanPlace(maid.getAvailableBackpackInv(), itemStack);
                if (maxStore > 0) {
                    ItemStack copy = itemStack.copy();
                    ItemStack tmp = RequestListItem.updateCollectedItem(maid.getMainHandItem(), itemStack, maxStore);
                    copy.shrink(tmp.getCount());
                    InvUtil.tryPlace(maid.getAvailableBackpackInv(), copy);
                    return tmp;
                }
                return itemStack;
            });
        } else if (context instanceof IStorageExtractableContext isec) {
            List<Pair<ItemStack, Integer>> itemStacksNotDone = RequestListItem.getItemStacksNotDone(maid.getMainHandItem(), true);
            isec.extract(itemStacksNotDone.stream().map(Pair::getA).toList(),
                    RequestListItem.matchNbt(maid.getMainHandItem()),
                    itemStack -> {
                        int maxStore = InvUtil.maxCanPlace(maid.getAvailableBackpackInv(), itemStack);
                        if (maxStore > 0) {
                            ItemStack copy = itemStack.copy();
                            ItemStack tmp = RequestListItem.updateCollectedItem(maid.getMainHandItem(), itemStack, maxStore);
                            copy.shrink(tmp.getCount());
                            InvUtil.tryPlace(maid.getAvailableBackpackInv(), copy);
                        }
                        return itemStack;
                    });
        }
    }

}
