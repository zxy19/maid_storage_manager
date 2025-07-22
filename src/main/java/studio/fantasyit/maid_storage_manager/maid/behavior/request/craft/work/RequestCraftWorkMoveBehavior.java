package studio.fantasyit.maid_storage_manager.maid.behavior.request.craft.work;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.MaidPathFindingBFS;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import com.google.common.collect.ImmutableMap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.work.CraftLayer;
import studio.fantasyit.maid_storage_manager.craft.work.CraftLayerChain;
import studio.fantasyit.maid_storage_manager.debug.DebugData;
import studio.fantasyit.maid_storage_manager.items.RequestListItem;
import studio.fantasyit.maid_storage_manager.maid.ChatTexts;
import studio.fantasyit.maid_storage_manager.maid.behavior.ScheduleBehavior;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.util.Conditions;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;

import java.util.List;

/**
 * 合成工作1
 */
public class RequestCraftWorkMoveBehavior extends Behavior<EntityMaid> {
    public RequestCraftWorkMoveBehavior() {
        super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT, InitEntities.TARGET_POS.get(), MemoryStatus.VALUE_ABSENT));
    }

    CraftLayerChain plan;
    CraftLayer layer;
    Target target;

    @Override
    protected boolean checkExtraStartConditions(@NotNull ServerLevel worldIn, @NotNull EntityMaid owner) {
        if (MemoryUtil.getCurrentlyWorking(owner) != ScheduleBehavior.Schedule.REQUEST) return false;
        if (MemoryUtil.getRequestProgress(owner).isReturning()) return false;
        if (!Conditions.takingRequestList(owner)) return false;
        if (MemoryUtil.getCrafting(owner).isGatheringDispatched()) return false;
        if (!MemoryUtil.getCrafting(owner).hasPlan()) return false;
        if (!MemoryUtil.getCrafting(owner).plan().isCurrentWorking()) return false;
        return true;
    }


    @Override
    protected void start(ServerLevel level, EntityMaid maid, long p_22542_) {
        plan = MemoryUtil.getCrafting(maid).plan();
        layer = plan.getCurrentLayer();
        for (int i = 0; i < 3; i++) {
            if (targeting(level, maid))
                return;
        }
    }

    private boolean targeting(ServerLevel level, EntityMaid maid) {
        CraftGuideStepData step = layer.getStepData();
        if (step == null) {
            if (!layer.getItems().isEmpty())
                plan.setStatusMessage(maid,
                        Component.translatable(
                                ChatTexts.CHAT_CRAFTING_SUCCESS,
                                layer.getItems().get(0).getHoverName()
                        ).withStyle(ChatFormatting.GREEN)
                );
            DebugData.sendDebug("[REQUEST_CRAFT_WORK] Step Done. Set Success.");
            //根层
            for (int i = 0; i < layer.getItems().size(); i++) {
                ItemStack itemStack = layer.getItems().get(i);
                if (itemStack.isEmpty()) continue;
                RequestListItem.updateCollectedItem(maid.getMainHandItem(),
                        itemStack,
                        itemStack.getCount()
                );
            }
            MemoryUtil.getCrafting(maid).lastSuccess();
            plan.finishCurrentLayer(maid);
            plan.showCraftingProgress(maid);
            MemoryUtil.getCrafting(maid).resetAndMarkVis(level, maid);
            //立刻安排返回存储
            MemoryUtil.getRequestProgress(maid).setReturn();
            MemoryUtil.getRequestProgress(maid).clearTarget();
            MemoryUtil.clearTarget(maid);
            return true;
        }
        //如果当前layer被占用，那就不用开始走过去了
        if (plan.checkIsCurrentOccupied(level, maid)) {
            if (!plan.tryUseAnotherCraftGuide(level, maid)) {
                if (plan.tryReleaseAndStartNext()) {
                    MemoryUtil.getCrafting(maid).clearTarget();
                    MemoryUtil.clearTarget(maid);
                }
            }
            return true;
        }
        plan.setOccupied(level, maid);
        if (!plan.checkStepInputInbackpack(maid)) {
            return true;
        }
        Target storage = step.getStorage();
        DebugData.sendDebug(
                String.format("[REQUEST_CRAFT_WORK]Step %d [%d/%d], %s",
                        layer.getStep(),
                        layer.getDoneCount(),
                        layer.getCount(),
                        storage
                )
        );
        MaidPathFindingBFS pathFinding = new MaidPathFindingBFS(maid.getNavigation().getNodeEvaluator(), level, maid);
        BlockPos blockPos = step.actionType.pathFindingTargetProvider().find(maid, layer.getCraftData().get(), step, layer, pathFinding);
        if (blockPos != null) {
            MemoryUtil.setTarget(maid, blockPos, (float) Config.craftWorkSpeed);
            MemoryUtil.getCrafting(maid).setTarget(storage);
            MemoryUtil.setLookAt(maid, storage.getPos());
            MemoryUtil.getCrafting(maid).resetPathFindingFailCount();
        } else {
            MemoryUtil.getCrafting(maid).addPathFindingFailCount();
            if (MemoryUtil.getCrafting(maid).getPathFindingFailCount() > 200) {
                List<ItemStack> missing = layer.getCraftData().map(CraftGuideData::getOutput).orElse(List.of());
                DebugData.sendDebug("[REQUEST_CRAFT_WORK]Path finding fail.");
                plan.failCurrent(maid, missing, "tooltip.maid_storage_manager.request_list.fail_cannot_path_reach_crafting");
                MemoryUtil.getCrafting(maid).resetPathFindingFailCount();
            }
        }
        return true;
    }
}