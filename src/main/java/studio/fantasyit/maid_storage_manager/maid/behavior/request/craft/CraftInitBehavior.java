package studio.fantasyit.maid_storage_manager.maid.behavior.request.craft;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import org.jetbrains.annotations.NotNull;
import studio.fantasyit.maid_storage_manager.craft.algo.MaidCraftPlanner;
import studio.fantasyit.maid_storage_manager.debug.DebugData;
import studio.fantasyit.maid_storage_manager.items.RequestListItem;
import studio.fantasyit.maid_storage_manager.maid.behavior.ScheduleBehavior;
import studio.fantasyit.maid_storage_manager.util.Conditions;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;

import java.util.Map;

public class CraftInitBehavior extends Behavior<EntityMaid> {
    public CraftInitBehavior() {
        super(Map.of());
    }

    MaidCraftPlanner planner;

    @Override
    protected boolean checkExtraStartConditions(ServerLevel p_22538_, EntityMaid p_22539_) {
        if (MemoryUtil.getCurrentlyWorking(p_22539_) != ScheduleBehavior.Schedule.REQUEST) return false;
        if (!Conditions.takingRequestList(p_22539_)) return false;
        if (MemoryUtil.getRequestProgress(p_22539_).isReturning()) return false;
        if (!MemoryUtil.getRequestProgress(p_22539_).isTryCrafting()) return false;
        //女仆当前没有生成合成任务，应该立刻计算所有合成
        return !MemoryUtil.getCrafting(p_22539_).hasTasks();
    }

    @Override
    protected boolean canStillUse(@NotNull ServerLevel level, @NotNull EntityMaid maid, long p_22547_) {
        if (MemoryUtil.getCurrentlyWorking(maid) != ScheduleBehavior.Schedule.REQUEST) return false;
        if (!Conditions.takingRequestList(maid)) return false;
        return !planner.done();
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long p_22542_) {
        planner = new MaidCraftPlanner(level, maid);
        MemoryUtil.getCrafting(maid).clearLayers();
        MemoryUtil.getCrafting(maid).resetVisitedPos();
        MemoryUtil.getCrafting(maid).startWorking(false);
    }

    @Override
    protected void tick(ServerLevel p_22551_, EntityMaid maid, long p_22553_) {
        planner.tick(p_22553_);
    }

    @Override
    protected void stop(ServerLevel p_22548_, EntityMaid maid, long p_22550_) {
        if (!planner.anySuccess()) {
            // 没有成功合成，就直接返回
            RequestListItem.markAllDone(maid.getMainHandItem());
            MemoryUtil.getRequestProgress(maid).setTryCrafting(false);
            MemoryUtil.getRequestProgress(maid).setReturn(true);
            DebugData.sendDebug("[REQUEST_CRAFT] Failed to find recipe for any items");
        }
        MemoryUtil.getCrafting(maid).resetAndMarkVisForRequest(p_22548_, maid);
        MemoryUtil.clearTarget(maid);
    }

    @Override
    protected boolean timedOut(long p_22537_) {
        return false;
    }
}
