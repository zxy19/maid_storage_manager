package studio.fantasyit.maid_storage_manager.maid.behavior.request.find;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.items.RequestListItem;
import studio.fantasyit.maid_storage_manager.maid.ChatTexts;
import studio.fantasyit.maid_storage_manager.maid.behavior.ScheduleBehavior;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;
import studio.fantasyit.maid_storage_manager.storage.MaidStorage;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.storage.base.*;
import studio.fantasyit.maid_storage_manager.util.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class RequestFindBehavior extends Behavior<EntityMaid> {
    BehaviorBreath breath = new BehaviorBreath();
    IStorageContext context;
    boolean canPick = false;
    Target target;
    ItemStack checkItem = null;


    public RequestFindBehavior() {
        super(Map.of());
    }

    @Override
    protected boolean canStillUse(ServerLevel level, EntityMaid maid, long p_22547_) {
        if (!Conditions.takingRequestList(maid)) return false;
        if (MemoryUtil.getRequestProgress(maid).isReturning()) return false;
        if (!InvUtil.hasAnyFree(maid.getAvailableInv(false))) return false;
        if (!canPick) return true;
        return context != null && !context.isDone();
    }

    @Override
    protected boolean checkExtraStartConditions(@NotNull ServerLevel level, @NotNull EntityMaid maid) {
        if (MemoryUtil.getCurrentlyWorking(maid) != ScheduleBehavior.Schedule.REQUEST) return false;
        if (!MemoryUtil.getRequestProgress(maid).hasTarget()) return false;
        if (MemoryUtil.getRequestProgress(maid).isReturning()) return false;
        if (MemoryUtil.getRequestProgress(maid).isTryCrafting()) return false;
        if (MemoryUtil.getRequestProgress(maid).isCheckingStock()) return false;
        return Conditions.hasReachedValidTargetOrReset(maid);
    }


    @Override
    protected void start(@NotNull ServerLevel level, @NotNull EntityMaid maid, long gameTimeIn) {
        @Nullable IMaidStorage storage = Objects.requireNonNull(MaidStorage.getInstance().getStorage(MemoryUtil.getRequestProgress(maid).getTarget().getType()));
        if (!MemoryUtil.getRequestProgress(maid).hasTarget()) return;
        target = MemoryUtil.getRequestProgress(maid).getTarget();
        checkItem = MemoryUtil.getRequestProgress(maid).getCheckItem();
        if (checkItem.isEmpty())
            checkItem = null;
        context = storage.onStartCollect(level, maid, target);
        if (context != null)
            context.start(maid, level, target);
        canPick = false;
    }


    @Override
    protected void tick(ServerLevel level, EntityMaid maid, long p_22553_) {
        if (canPick) tickPick(level, maid, p_22553_);
        else {
            tickGather(level, maid, p_22553_);
            if (context.isDone()) {
                context.reset();
                canPick = true;
                if (context instanceof IStorageInteractContext) {
                    MemoryUtil.getViewedInventory(maid).resetViewedInvForPos(target);
                    StorageAccessUtil.checkNearByContainers(level, target.getPos(), pos -> {
                        MemoryUtil.getViewedInventory(maid).resetViewedInvForPosAsRemoved(target.sameType(pos, null));
                    });
                }
            }
        }
    }

    private void tickGather(ServerLevel level, EntityMaid maid, long p22553) {
        if (!breath.breathTick(maid)) return;
        Function<ItemStack, ItemStack> takeItem = (itemStack) -> {
            if (checkItem != null && ItemStack.isSameItemSameTags(itemStack, checkItem))
                checkItem = null;

            int maxStore = InvUtil.maxCanPlace(maid.getAvailableInv(false), itemStack);
            if (maxStore > 0) {
                ItemStack copy = itemStack.copy();
                ItemStack tmp = RequestListItem.updateCollectedItem(maid.getMainHandItem(), itemStack, maxStore,false);
                copy.shrink(tmp.getCount());
                ViewedInventoryUtil.ambitiousRemoveItemAndSync(maid, level, target, itemStack, copy.getCount());
                InvUtil.tryPlace(maid.getAvailableInv(false), copy);
                return tmp;
            }
            return itemStack;
        };
        if (context instanceof IStorageInteractContext isic) {
            isic.tick(takeItem);
        } else if (context instanceof IStorageExtractableContext isec) {
            if (isec.hasTask())
                isec.tick(takeItem);
            else {
                List<Pair<ItemStack, Integer>> itemStacksNotDone = RequestListItem.getItemStacksNotDone(maid.getMainHandItem(), true);
                isec.setExtract(itemStacksNotDone.stream().map(c -> c.getA().copyWithCount(c.getB() == -1 ? Integer.MAX_VALUE : c.getB())).toList(), RequestListItem.getMatchType(maid.getMainHandItem()));
            }
        }
    }

    private void tickPick(ServerLevel level, EntityMaid maid, long p_22550_) {
        if (!breath.breathTick(maid)) return;
        if (context instanceof IStorageCraftDataProvider ispcp) {
            ispcp.getCraftGuideData().forEach(craftGuideData -> {
                MemoryUtil.getCrafting(maid).addCraftGuide(craftGuideData);
            });
        } else if (context instanceof IStorageInteractContext isic) {
            isic.tick(itemStack -> {
                MemoryUtil.getViewedInventory(maid).addItem(target, itemStack);
                if (itemStack.is(ItemRegistry.CRAFT_GUIDE.get())) {
                    MemoryUtil.getCrafting(maid).addCraftGuide(CraftGuideData.fromItemStack(itemStack));
                }
                return itemStack;
            });
        } else if (context instanceof IStorageExtractableContext isec) {
            if (isec.hasTask())
                isec.tick(itemStack -> {
                    if (itemStack.is(ItemRegistry.CRAFT_GUIDE.get())) {
                        MemoryUtil.getCrafting(maid).addCraftGuide(CraftGuideData.fromItemStack(itemStack));
                    }
                    return itemStack;
                });
            else
                isec.setExtract(List.of(ItemRegistry.CRAFT_GUIDE.get().getDefaultInstance()), ItemStackUtil.MATCH_TYPE.NOT_MATCHING);
        }
    }

    @Override
    protected void stop(ServerLevel level, EntityMaid maid, long p_22550_) {
        super.stop(level, maid, p_22550_);
        if (context != null) {
            context.finish();
            if (context.isDone()) {
                Target target = MemoryUtil.getRequestProgress(maid).getTarget();
                MemoryUtil.getRequestProgress(maid).addVisitedPos(target);
                StorageAccessUtil.checkNearByContainers(level, target.getPos(), (pos) -> {
                    MemoryUtil.getRequestProgress(maid).addVisitedPos(target.sameType(pos, null));
                });
            }
        }

        if (checkItem != null) {
            ChatTexts.send(maid, ChatTexts.CHAT_MISSING);
        }

        MemoryUtil.getRequestProgress(maid).clearTarget();
        MemoryUtil.clearTarget(maid);
    }

    @Override
    protected boolean timedOut(long p_22537_) {
        return false;
    }
}
