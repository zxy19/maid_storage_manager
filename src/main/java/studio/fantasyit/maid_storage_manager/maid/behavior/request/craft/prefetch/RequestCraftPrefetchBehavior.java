package studio.fantasyit.maid_storage_manager.maid.behavior.request.craft.prefetch;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import studio.fantasyit.maid_storage_manager.craft.work.CraftLayer;
import studio.fantasyit.maid_storage_manager.craft.work.CraftLayerChain;
import studio.fantasyit.maid_storage_manager.craft.work.SolvedCraftLayer;
import studio.fantasyit.maid_storage_manager.maid.ChatTexts;
import studio.fantasyit.maid_storage_manager.maid.behavior.ScheduleBehavior;
import studio.fantasyit.maid_storage_manager.maid.behavior.base.AbstractGatherBehavior;
import studio.fantasyit.maid_storage_manager.maid.memory.AbstractTargetMemory;
import studio.fantasyit.maid_storage_manager.util.Conditions;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;

import java.util.List;

public class RequestCraftPrefetchBehavior extends AbstractGatherBehavior {
    CraftLayerChain plan;
    CraftLayer layer;
    List<ItemStack> toPrefetchItems;
    SolvedCraftLayer node;

    public RequestCraftPrefetchBehavior() {
        super();
    }

    @Override
    protected AbstractTargetMemory getMemory(EntityMaid maid) {
        return MemoryUtil.getCrafting(maid);
    }

    @Override
    protected void onStart(ServerLevel level, EntityMaid maid) {
        plan = MemoryUtil.getCrafting(maid).plan();
        layer = plan.getCurrentLayer();
        node = plan.getCurrentNode();
        plan.showCraftingProgress(maid);
        toPrefetchItems = layer.getToPrefetchItems(plan.getCurrentNode().prefetchable());
    }

    @Override
    protected void onStop(ServerLevel level, EntityMaid maid) {
        if (layer.getToPrefetchItems(node.prefetchable()).isEmpty()) {
            plan.finishPrefetching(maid);
        }
    }

    @Override
    protected int getMaxToGet(EntityMaid maid, ItemStack incomingItemstack) {
        int maxCount = 0;
        for (ItemStack itemStack : toPrefetchItems) {
            if (ItemStackUtil.isSame(itemStack, incomingItemstack, ItemStackUtil.MATCH_TYPE.AUTO))
                maxCount += itemStack.getCount();
        }
        return maxCount;
    }

    @Override
    protected ItemStack getToTakeItemStack(EntityMaid maid, ItemStack toTake, int maxStore) {
        return layer.memorizeItem(toTake, maxStore);
    }

    @Override
    protected void onTake(EntityMaid maid, ItemStack itemStack) {
        plan.setStatusMessage(maid,
                Component.translatable(
                        ChatTexts.CHAT_CRAFT_GATHER_ITEMS,
                        itemStack.getHoverName(),
                        String.valueOf(itemStack.getCount())
                )
        );
        ItemStackUtil.removeIsMatchInList(toPrefetchItems, itemStack, ItemStackUtil.MATCH_TYPE.AUTO);
    }

    @Override
    protected List<ItemStack> getToTakeItems(EntityMaid maid) {
        return layer.getUnCollectedItems();
    }


    @Override
    protected boolean checkExtraStartConditions(@NotNull ServerLevel level, @NotNull EntityMaid maid) {
        if (MemoryUtil.getCurrentlyWorking(maid) != ScheduleBehavior.Schedule.REQUEST) return false;
        if (MemoryUtil.isWorking(maid)) return false;
        if (!Conditions.takingRequestList(maid)) return false;
        if (!MemoryUtil.getRequestProgress(maid).isTryCrafting()) return false;
        if (MemoryUtil.getCrafting(maid).isGatheringDispatched()) return false;
        if (!MemoryUtil.getCrafting(maid).hasTarget()) return false;
        if (!MemoryUtil.getCrafting(maid).hasPlan()) return false;
        if (!MemoryUtil.getCrafting(maid).plan().isCurrentPrefetching()) return false;
        return Conditions.hasReachedValidTargetOrReset(maid);
    }


    @Override
    protected boolean canStillUse(ServerLevel level, EntityMaid maid, long p_22547_) {
        if (!Conditions.takingRequestList(maid)) return false;
        if (!MemoryUtil.getRequestProgress(maid).isTryCrafting()) return false;
        if (MemoryUtil.getCrafting(maid).hasPlan()) {
            if (toPrefetchItems.isEmpty())
                return false;
        }
        return super.canStillUse(level, maid, p_22547_);
    }


}
