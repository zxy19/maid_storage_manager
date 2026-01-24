package studio.fantasyit.maid_storage_manager.maid.behavior.request.craft.prefetch;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.craft.work.CraftLayer;
import studio.fantasyit.maid_storage_manager.craft.work.CraftLayerChain;
import studio.fantasyit.maid_storage_manager.craft.work.SolvedCraftLayer;
import studio.fantasyit.maid_storage_manager.maid.ChatTexts;
import studio.fantasyit.maid_storage_manager.maid.behavior.ScheduleBehavior;
import studio.fantasyit.maid_storage_manager.maid.behavior.base.AbstractGatherMoveBehavior;
import studio.fantasyit.maid_storage_manager.maid.memory.AbstractTargetMemory;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.util.Conditions;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;

import java.util.List;

/**
 * 预提取物品，替换清单函数
 */
public class RequestCraftPrefetchMoveBehavior extends AbstractGatherMoveBehavior {
    public RequestCraftPrefetchMoveBehavior() {
        super((float) Config.collectSpeed);
    }

    @Override
    protected boolean checkExtraStartConditions(@NotNull ServerLevel worldIn, @NotNull EntityMaid owner) {
        if (MemoryUtil.getCurrentlyWorking(owner) != ScheduleBehavior.Schedule.REQUEST) return false;
        if (MemoryUtil.getRequestProgress(owner).isReturning()) return false;
        if (!Conditions.takingRequestList(owner)) return false;
        if (!MemoryUtil.getCrafting(owner).hasPlan()) return false;
        if (MemoryUtil.getCrafting(owner).isGatheringDispatched()) return false;
        if (!MemoryUtil.getCrafting(owner).plan().isCurrentPrefetching()) return false;
        return true;
    }


    CraftLayerChain plan;
    CraftLayer layer;
    SolvedCraftLayer node;
    List<ItemStack> prefetchableTargetItem;

    @Override
    protected AbstractTargetMemory getMemory(EntityMaid maid) {
        return MemoryUtil.getCrafting(maid);
    }

    @Override
    protected boolean hasFinishedPre(ServerLevel level, EntityMaid maid) {
        plan = MemoryUtil.getCrafting(maid).plan();
        node = plan.getCurrentNode();
        layer = plan.getCurrentLayer();
        plan.ifChanged(() -> MemoryUtil.getCrafting(maid).resetAndMarkVis(level, maid));
        prefetchableTargetItem = layer.getToPrefetchItems(node.prefetchable());
        if (prefetchableTargetItem.isEmpty()) {
            plan.finishPrefetching(maid);
            return true;
        }
        return false;
    }

    @Override
    protected void findTarget(ServerLevel level, EntityMaid maid, Target target) {
        plan.setStatusMessage(maid, Component.translatable(ChatTexts.CHAT_CRAFT_GATHER));
    }

    @Override
    protected void noTarget(ServerLevel level, EntityMaid maid) {
        plan.finishGathering(maid);
    }

    @Override
    protected boolean isTargetItem(ServerLevel level, EntityMaid maid, List<ItemStack> targets, ItemStack itemStack) {
        return targets.stream().anyMatch(i2 -> ItemStackUtil.isSameInCrafting(i2, itemStack));
    }

    @Override
    protected @NotNull List<ItemStack> getPriorityItems(ServerLevel level, EntityMaid maid) {
        return prefetchableTargetItem;
    }
}
