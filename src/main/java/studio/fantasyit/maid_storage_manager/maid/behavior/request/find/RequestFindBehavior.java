package studio.fantasyit.maid_storage_manager.maid.behavior.request.find;

import com.github.tartaricacid.touhoulittlemaid.entity.chatbubble.ChatBubbleManger;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.craft.CraftGuideData;
import studio.fantasyit.maid_storage_manager.items.RequestListItem;
import studio.fantasyit.maid_storage_manager.maid.ChatTexts;
import studio.fantasyit.maid_storage_manager.maid.behavior.ScheduleBehavior;
import studio.fantasyit.maid_storage_manager.maid.memory.ViewedInventoryMemory;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;
import studio.fantasyit.maid_storage_manager.storage.MaidStorage;
import studio.fantasyit.maid_storage_manager.storage.Storage;
import studio.fantasyit.maid_storage_manager.storage.base.IMaidStorage;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageContext;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageExtractableContext;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageInteractContext;
import studio.fantasyit.maid_storage_manager.util.BehaviorBreath;
import studio.fantasyit.maid_storage_manager.util.Conditions;
import studio.fantasyit.maid_storage_manager.util.InvUtil;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class RequestFindBehavior extends Behavior<EntityMaid> {
    BehaviorBreath breath = new BehaviorBreath();
    IStorageContext context;
    boolean canPick = false;
    Storage target;
    ItemStack checkItem = null;


    public RequestFindBehavior() {
        super(Map.of(), 10000000);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, EntityMaid maid, long p_22547_) {
        if (!Conditions.takingRequestList(maid)) return false;
        if (MemoryUtil.getRequestProgress(maid).isReturning()) return false;
        if (!InvUtil.hasAnyFree(maid.getMaidInv())) return false;
        if (!canPick) return true;
        return context != null && !context.isDone();
    }

    @Override
    protected boolean checkExtraStartConditions(@NotNull ServerLevel level, @NotNull EntityMaid maid) {
        if (MemoryUtil.getCurrentlyWorking(maid) != ScheduleBehavior.Schedule.REQUEST) return false;
        if (!MemoryUtil.getRequestProgress(maid).hasTarget()) return false;
        if (MemoryUtil.getRequestProgress(maid).isReturning()) return false;
        if (MemoryUtil.getRequestProgress(maid).isTryCrafting()) return false;
        return Conditions.hasReachedValidTargetOrReset(maid);
    }


    @Override
    protected void start(@NotNull ServerLevel level, @NotNull EntityMaid maid, long gameTimeIn) {
        IMaidStorage storage = Objects.requireNonNull(MaidStorage.getInstance().getStorage(MemoryUtil.getRequestProgress(maid).getTarget().getType()));
        if (!MemoryUtil.getRequestProgress(maid).hasTarget()) return;
        target = MemoryUtil.getRequestProgress(maid).getTarget();
        checkItem = MemoryUtil.getRequestProgress(maid).getCheckItem();
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
                }
            }
        }
    }

    private void tickGather(ServerLevel level, EntityMaid maid, long p22553) {
        if (!breath.breathTick()) return;
        Function<ItemStack, ItemStack> takeItem = (itemStack) -> {
            if (checkItem != null && ItemStack.isSameItemSameTags(itemStack, checkItem))
                checkItem = null;

            int maxStore = InvUtil.maxCanPlace(maid.getAvailableInv(false), itemStack);
            if (maxStore > 0) {
                ItemStack copy = itemStack.copy();
                ItemStack tmp = RequestListItem.updateCollectedItem(maid.getMainHandItem(), itemStack, maxStore);
                copy.shrink(tmp.getCount());
                MemoryUtil.getViewedInventory(maid).ambitiousRemoveItem(level, target, itemStack, copy.getCount());
                InvUtil.tryPlace(maid.getAvailableInv(false), copy);
                return tmp;
            }
            return itemStack;
        };
        if (context instanceof IStorageInteractContext isic) {
            isic.tick(takeItem);
        } else if (context instanceof IStorageExtractableContext isec) {
            List<Pair<ItemStack, Integer>> itemStacksNotDone = RequestListItem.getItemStacksNotDone(maid.getMainHandItem(), true);
            isec.extract(itemStacksNotDone.stream().map(Pair::getA).toList(),
                    RequestListItem.matchNbt(maid.getMainHandItem()),
                    takeItem);
        }
    }

    private void tickPick(ServerLevel level, EntityMaid maid, long p_22550_) {
        if (!breath.breathTick()) return;
        if (context instanceof IStorageInteractContext isic) {
            isic.tick(itemStack -> {
                MemoryUtil.getViewedInventory(maid).addItem(target, itemStack);
                if (itemStack.is(ItemRegistry.CRAFT_GUIDE.get())) {
                    MemoryUtil.getCrafting(maid).addCraftGuide(CraftGuideData.fromItemStack(itemStack));
                }
                return itemStack;
            });
        } else if (context instanceof IStorageExtractableContext isec) {
            isec.extract(List.of(ItemRegistry.CRAFT_GUIDE.get().getDefaultInstance()),
                    false,
                    itemStack -> {
                        if (itemStack.is(ItemRegistry.CRAFT_GUIDE.get())) {
                            MemoryUtil.getCrafting(maid).addCraftGuide(CraftGuideData.fromItemStack(itemStack));
                        }
                        return itemStack;
                    });
        }
    }

    @Override
    protected void stop(ServerLevel level, EntityMaid maid, long p_22550_) {
        super.stop(level, maid, p_22550_);
        if (context != null) {
            context.finish();
            if (context.isDone()) {
                Storage target = MemoryUtil.getRequestProgress(maid).getTarget();
                MemoryUtil.getRequestProgress(maid).addVisitedPos(target);
                InvUtil.checkNearByContainers(level, target.getPos(), (pos) -> {
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
}
