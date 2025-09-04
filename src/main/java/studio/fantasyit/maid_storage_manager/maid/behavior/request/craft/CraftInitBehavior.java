package studio.fantasyit.maid_storage_manager.maid.behavior.request.craft;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import org.jetbrains.annotations.NotNull;
import studio.fantasyit.maid_storage_manager.attachment.CraftBlockOccupy;
import studio.fantasyit.maid_storage_manager.craft.algo.MaidCraftPlanner;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.debug.CraftingDebugContext;
import studio.fantasyit.maid_storage_manager.craft.debug.CraftingDebugManager;
import studio.fantasyit.maid_storage_manager.debug.DebugData;
import studio.fantasyit.maid_storage_manager.items.RequestListItem;
import studio.fantasyit.maid_storage_manager.maid.ChatTexts;
import studio.fantasyit.maid_storage_manager.maid.behavior.ScheduleBehavior;
import studio.fantasyit.maid_storage_manager.maid.data.StorageManagerConfigData;
import studio.fantasyit.maid_storage_manager.maid.memory.CraftMemory;
import studio.fantasyit.maid_storage_manager.registry.DataComponentRegistry;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;
import studio.fantasyit.maid_storage_manager.util.Conditions;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;

import java.util.Map;
import java.util.Optional;

public class CraftInitBehavior extends Behavior<EntityMaid> {
    public CraftInitBehavior() {
        super(Map.of());
    }

    MaidCraftPlanner planner;
    CraftingDebugContext debugContext;

    @Override
    protected boolean checkExtraStartConditions(ServerLevel p_22538_, EntityMaid p_22539_) {
        if (MemoryUtil.getCurrentlyWorking(p_22539_) != ScheduleBehavior.Schedule.REQUEST) return false;
        if (!Conditions.takingRequestList(p_22539_)) return false;
        if (MemoryUtil.getRequestProgress(p_22539_).isReturning()) return false;
        if (!MemoryUtil.getRequestProgress(p_22539_).isTryCrafting()) return false;
        if (MemoryUtil.getCrafting(p_22539_).isGoPlacingBeforeCraft()) return false;
        //女仆当前没有生成合成任务，应该立刻计算所有合成
        return !MemoryUtil.getCrafting(p_22539_).hasPlan();
    }

    @Override
    protected boolean canStillUse(@NotNull ServerLevel level, @NotNull EntityMaid maid, long p_22547_) {
        if (MemoryUtil.getCurrentlyWorking(maid) != ScheduleBehavior.Schedule.REQUEST) return false;
        if (!Conditions.takingRequestList(maid)) return false;
        return !planner.done();
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long p_22542_) {
        MemoryUtil.getCrafting(maid).clearPlan();
        MemoryUtil.getCrafting(maid).resetVisitedPos();
        if (StorageManagerConfigData.get(maid).useMemorizedCraftGuide()) {
            CraftMemory crafting = MemoryUtil.getCrafting(maid);
            MemoryUtil.getViewedInventory(maid).flatten().forEach(item -> {
                if (item.itemStack.is(ItemRegistry.CRAFT_GUIDE.get())) {
                    CraftGuideData craftGuideData = item.itemStack.get(DataComponentRegistry.CRAFT_GUIDE_DATA);
                    if (craftGuideData != null && craftGuideData.available()) {
                        crafting.addCraftGuide(craftGuideData);
                    }
                }
            });
        }
        planner = new MaidCraftPlanner(level, maid);
        CraftingDebugManager.getDebugContext(maid.getOwnerUUID())
                .ifPresentOrElse(c -> {
                    c.convey(planner);
                    debugContext = c;
                    debugContext.logNoLevel(CraftingDebugContext.TYPE.COMMON, "Starting craft calculator");
                }, () -> debugContext = null);
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
            ChatTexts.send(maid, ChatTexts.CHAT_CRAFT_CALCULATE_NO_RESULT);
            MemoryUtil.getCrafting(maid).calculatingProgress = 0;
            MemoryUtil.getCrafting(maid).calculatingTotal = 0;
        } else {
            MemoryUtil.getCrafting(maid).setPlan(planner.getPlan());
            MemoryUtil.getCrafting(maid).addIgnoreTargetFromRequest(maid, p_22548_);
            ChatTexts.remove(maid);
        }
        CraftBlockOccupy.get(p_22548_).removeAllOccupiesFor(maid);
        MemoryUtil.getCrafting(maid).resetAndMarkVis(p_22548_, maid);
        MemoryUtil.clearTarget(maid);
        if (debugContext != null) {
            Optional.ofNullable(maid.getOwner())
                    .ifPresent(o -> o.sendSystemMessage(Component.literal("Crafting debug done")));
            debugContext.stop();
        }
    }

    @Override
    protected boolean timedOut(long p_22537_) {
        return false;
    }
}