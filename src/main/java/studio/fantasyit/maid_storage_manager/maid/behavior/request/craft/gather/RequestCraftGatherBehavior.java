package studio.fantasyit.maid_storage_manager.maid.behavior.request.craft.gather;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import studio.fantasyit.maid_storage_manager.craft.work.CraftLayer;
import studio.fantasyit.maid_storage_manager.craft.work.CraftLayerChain;
import studio.fantasyit.maid_storage_manager.debug.DebugData;
import studio.fantasyit.maid_storage_manager.maid.ChatTexts;
import studio.fantasyit.maid_storage_manager.maid.behavior.ScheduleBehavior;
import studio.fantasyit.maid_storage_manager.storage.MaidStorage;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.storage.base.IMaidStorage;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageContext;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageExtractableContext;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageInteractContext;
import studio.fantasyit.maid_storage_manager.util.*;

import java.util.Map;
import java.util.function.Function;

public class RequestCraftGatherBehavior extends Behavior<EntityMaid> {
    BehaviorBreath breath = new BehaviorBreath();
    IStorageContext context;
    private Target target;
    boolean changed = false;
    CraftLayerChain plan;
    CraftLayer layer;

    public RequestCraftGatherBehavior() {
        super(Map.of());
    }


    @Override
    protected boolean checkExtraStartConditions(@NotNull ServerLevel level, @NotNull EntityMaid maid) {
        if (MemoryUtil.getCurrentlyWorking(maid) != ScheduleBehavior.Schedule.REQUEST) return false;
        if (!Conditions.takingRequestList(maid)) return false;
        if (!MemoryUtil.getRequestProgress(maid).isTryCrafting()) return false;
        if (MemoryUtil.getCrafting(maid).isGatheringDispatched()) return false;
        if (!MemoryUtil.getCrafting(maid).hasTarget()) return false;
        if (!MemoryUtil.getCrafting(maid).hasPlan()) return false;
        if (!MemoryUtil.getCrafting(maid).plan().isCurrentGathering()) return false;
        return Conditions.hasReachedValidTargetOrReset(maid);
    }


    @Override
    protected void start(@NotNull ServerLevel level, @NotNull EntityMaid maid, long gameTimeIn) {
        plan = MemoryUtil.getCrafting(maid).plan();
        layer = plan.getCurrentLayer();
        if (!MemoryUtil.getCrafting(maid).hasTarget()) return;
        target = MemoryUtil.getCrafting(maid).getTarget();
        IMaidStorage storage = MaidStorage.getInstance().getStorage(target.getType());
        if (storage == null)
            return;

        changed = false;
        context = storage.onStartCollect(level, maid, target);
        if (context != null)
            context.start(maid, level, target);
        plan.showCraftingProgress(maid);
        InvUtil.mergeSameStack(maid.getAvailableInv(true));
    }

    @Override
    protected boolean canStillUse(ServerLevel level, EntityMaid maid, long p_22547_) {
        if (!Conditions.takingRequestList(maid)) return false;
        if (!MemoryUtil.getRequestProgress(maid).isTryCrafting()) return false;
        if (MemoryUtil.getCrafting(maid).hasPlan()) {
            if (layer.hasCollectedAll())
                return false;
        }
        return context != null && !context.isDone();
    }


    @Override
    protected void tick(ServerLevel level, EntityMaid maid, long p_22553_) {
        if (!breath.breathTick(maid)) return;
        Function<ItemStack, ItemStack> taker = (ItemStack itemStack) -> {
            int maxStore = InvUtil.maxCanPlace(maid.getAvailableInv(false), itemStack);
            if (maxStore > 0) {
                ItemStack copy = itemStack.copy();
                ItemStack toTake = layer.memorizeItem(itemStack, maxStore);
                if (toTake.getCount() > 0) {
                    changed = true;
                    plan.setStatusMessage(maid,
                            Component.translatable(
                                    ChatTexts.CHAT_CRAFT_GATHER_ITEMS,
                                    itemStack.getHoverName(),
                                    String.valueOf(toTake.getCount())
                            )
                    );
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
                isec.setExtract(layer.getUnCollectedItems(), true);
        }
    }

    @Override
    protected void stop(ServerLevel level, EntityMaid maid, long p_22550_) {
        super.stop(level, maid, p_22550_);
        if (context != null) {
            context.finish();
            if (context.isDone() && !changed) {
                Target target = MemoryUtil.getCrafting(maid).getTarget();
                MemoryUtil.getCrafting(maid).addVisitedPos(target);
                StorageAccessUtil.checkNearByContainers(level, target.getPos(), (pos) -> {
                    MemoryUtil.getCrafting(maid).addVisitedPos(target.sameType(pos, null));
                });
            }
        }
        MemoryUtil.getCrafting(maid).clearCheckItem();
        MemoryUtil.getCrafting(maid).clearTarget();
        MemoryUtil.clearTarget(maid);

        if (layer.hasCollectedAll()) {
            plan.finishGathering(maid);
        }
    }

    @Override
    protected boolean timedOut(long p_22537_) {
        return false;
    }
}
