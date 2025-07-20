package studio.fantasyit.maid_storage_manager.maid.behavior.request.craft.ret;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.craft.work.CraftLayer;
import studio.fantasyit.maid_storage_manager.craft.work.CraftLayerChain;
import studio.fantasyit.maid_storage_manager.debug.DebugData;
import studio.fantasyit.maid_storage_manager.items.RequestListItem;
import studio.fantasyit.maid_storage_manager.maid.ChatTexts;
import studio.fantasyit.maid_storage_manager.maid.behavior.ScheduleBehavior;
import studio.fantasyit.maid_storage_manager.maid.memory.CraftMemory;
import studio.fantasyit.maid_storage_manager.util.Conditions;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;

import java.util.Map;

public class ReturnOnVehicleBehavior extends Behavior<EntityMaid> {
    public ReturnOnVehicleBehavior() {
        super(Map.of());
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel p_22538_, EntityMaid maid) {
        if (MemoryUtil.getCurrentlyWorking(maid) != ScheduleBehavior.Schedule.REQUEST) return false;
        if (MemoryUtil.getRequestProgress(maid).isReturning()) return false;
        if (!Conditions.takingRequestList(maid)) return false;
        if (MemoryUtil.getCrafting(maid).isGatheringDispatched()) return false;
        if (!MemoryUtil.getCrafting(maid).hasPlan()) return false;
        if (!MemoryUtil.getCrafting(maid).plan().hasCurrent()) return false;
        if (MemoryUtil.getCrafting(maid).plan().getCurrentLayer().getCraftData().isPresent()) return false;
        return true;
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long p_22542_) {
        CraftMemory crafting = MemoryUtil.getCrafting(maid);
        CraftLayerChain plan = crafting.plan();
        CraftLayer layer = plan.getCurrentLayer();

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
        plan.finishCurrentLayer();
        plan.showCraftingProgress(maid);
        MemoryUtil.getCrafting(maid).resetAndMarkVis(level, maid);
        //立刻安排返回存储
        MemoryUtil.getRequestProgress(maid).setReturn();
        MemoryUtil.getRequestProgress(maid).clearTarget();
        MemoryUtil.clearTarget(maid);
    }
}
