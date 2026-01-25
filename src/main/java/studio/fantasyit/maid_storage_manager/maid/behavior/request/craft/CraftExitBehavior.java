package studio.fantasyit.maid_storage_manager.maid.behavior.request.craft;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import studio.fantasyit.maid_storage_manager.craft.debug.ProgressDebugContext;
import studio.fantasyit.maid_storage_manager.debug.DebugData;
import studio.fantasyit.maid_storage_manager.items.RequestListItem;
import studio.fantasyit.maid_storage_manager.maid.ChatTexts;
import studio.fantasyit.maid_storage_manager.maid.behavior.ScheduleBehavior;
import studio.fantasyit.maid_storage_manager.util.Conditions;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;

import java.util.Map;

public class CraftExitBehavior extends Behavior<EntityMaid> {
    public CraftExitBehavior() {
        super(Map.of());
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel p_22538_, EntityMaid p_22539_) {
        if (MemoryUtil.getCurrentlyWorking(p_22539_) != ScheduleBehavior.Schedule.REQUEST) return false;
        if (!Conditions.takingRequestList(p_22539_)) return false;
        if (!MemoryUtil.getRequestProgress(p_22539_).isTryCrafting()) return false;
        if (MemoryUtil.getRequestProgress(p_22539_).isReturning()) return false;
        if (!MemoryUtil.getCrafting(p_22539_).hasPlan()) return false;
        return MemoryUtil.getCrafting(p_22539_).plan().isDone();
    }


    @Override
    protected void start(ServerLevel level, EntityMaid maid, long p_22542_) {
        if (MemoryUtil.getCrafting(maid).plan().getIsStoppingAdding()) {
            MemoryUtil.getCrafting(maid).plan().handleStopAddingEvent(maid);
            return;
        }
        DebugData.sendDebug(maid, ProgressDebugContext.TYPE.STATUS, "[REQUEST_CRAFT]Exit");
        RequestListItem.markAllDone(maid.getMainHandItem());
        MemoryUtil.getCrafting(maid).clearCraftGuides();
        MemoryUtil.getCrafting(maid).clearPlan();
        ChatTexts.removeSecondary(maid);
        MemoryUtil.getRequestProgress(maid).setTryCrafting(false);
        MemoryUtil.getRequestProgress(maid).setReturn(true);
    }
}
