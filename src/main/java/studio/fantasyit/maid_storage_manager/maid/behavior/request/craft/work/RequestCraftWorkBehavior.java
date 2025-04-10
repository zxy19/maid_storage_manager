package studio.fantasyit.maid_storage_manager.maid.behavior.request.craft.work;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.craft.action.AbstractCraftActionContext;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftLayer;
import studio.fantasyit.maid_storage_manager.debug.DebugData;
import studio.fantasyit.maid_storage_manager.maid.ChatTexts;
import studio.fantasyit.maid_storage_manager.maid.behavior.ScheduleBehavior;
import studio.fantasyit.maid_storage_manager.util.BehaviorBreath;
import studio.fantasyit.maid_storage_manager.util.Conditions;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;

import java.util.List;
import java.util.Map;

public class RequestCraftWorkBehavior extends Behavior<EntityMaid> {
    BehaviorBreath breath = new BehaviorBreath();
    AbstractCraftActionContext context;
    CraftGuideStepData craftGuideStepData;
    private CraftLayer layer;
    private boolean done;
    private boolean fail;
    private int tryTick;

    public RequestCraftWorkBehavior() {
        super(Map.of());
    }

    @Override
    protected boolean canStillUse(ServerLevel level, EntityMaid maid, long p_22547_) {
        if (!Conditions.takingRequestList(maid)) return false;
        if (!MemoryUtil.getRequestProgress(maid).isTryCrafting()) return false;
        if (!MemoryUtil.getCrafting(maid).getCurrentLayer().hasCollectedAll()) return false;
        return !done;
    }

    @Override
    protected boolean checkExtraStartConditions(@NotNull ServerLevel level, @NotNull EntityMaid maid) {
        if (MemoryUtil.getCurrentlyWorking(maid) != ScheduleBehavior.Schedule.REQUEST) return false;
        if (!MemoryUtil.getRequestProgress(maid).isTryCrafting()) return false;
        if (!MemoryUtil.getCrafting(maid).hasStartWorking()) return false;
        if (!MemoryUtil.getCrafting(maid).hasTarget()) return false;
        if (!MemoryUtil.getCrafting(maid).hasCurrent()) return false;
        if (!MemoryUtil.getCrafting(maid).getCurrentLayer().hasCollectedAll()) return false;
        return Conditions.hasReachedValidTargetOrReset(maid);
    }


    @Override
    protected void start(@NotNull ServerLevel level, @NotNull EntityMaid maid, long gameTimeIn) {
        fail = false;
        if (!MemoryUtil.getCrafting(maid).hasTarget()) {
            fail = done = true;
            return;
        }
        layer = MemoryUtil.getCrafting(maid).getCurrentLayer();
        craftGuideStepData = layer.getStepData();
        ChatTexts.send(maid, ChatTexts.CHAT_CRAFT_WORK_PROGRESS,
                layer
                        .getCraftData()
                        .map(CraftGuideData::getAllOutputItems)
                        .map(l -> ChatTexts.fromComponent(l.get(0).getHoverName()))
                        .orElse(""),
                layer.getDoneCount().toString(),
                layer.getCount().toString()
        );
        if (craftGuideStepData == null) {
            MemoryUtil.getCrafting(maid).lastSuccess();
            done = true;
            return;
        }
        done = false;
        breath.reset();
        context = layer.startStep(maid);
        if (context == null) {
            fail = true;
            done = true;
            return;
        }
        AbstractCraftActionContext.Result start = context.start();
        if (start == AbstractCraftActionContext.Result.SUCCESS) {
            fail = false;
            done = true;
        } else if (start == AbstractCraftActionContext.Result.FAIL) {
            fail = true;
            done = true;
        }
    }

    @Override
    protected void tick(ServerLevel level, EntityMaid maid, long p_22553_) {
        tryTick++;
        if (allDone(maid)) {
            done = true;
        } else if (tryTick > Config.maxCraftTries) {
            fail = true;
            done = true;
            return;
        }
        if (!breath.breathTick()) return;
        AbstractCraftActionContext.Result tick = context.tick();
        switch (tick) {
            case SUCCESS -> {
                fail = false;
                done = true;
            }
            case FAIL -> {
                fail = true;
                done = true;
            }
            case CONTINUE -> tryTick = 0;
            default -> {
            }
        }
    }

    private boolean allDone(EntityMaid maid) {
        if (craftGuideStepData == null) return false;
        List<ItemStack> items = craftGuideStepData.getItems();
        for (int i = 0; i < items.size(); i++) {
            if (layer.getCurrentStepCount(i) < items.get(i).getCount()) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void stop(ServerLevel level, EntityMaid maid, long p_22550_) {
        super.stop(level, maid, p_22550_);
        if (context != null) {
            context.stop();
        }
        MemoryUtil.getCrafting(maid).clearTarget();
        MemoryUtil.clearTarget(maid);

        if (fail) {
            DebugData.getInstance().sendMessage("[REQUEST_CRAFT_WORK]crafting fail");
            MemoryUtil.getCrafting(maid).failCurrent(maid, craftGuideStepData.getItems());
        } else {
            DebugData.getInstance().sendMessage("[REQUEST_CRAFT_WORK]crafting done %s", layer.getStep());
            MemoryUtil.getCrafting(maid).getCurrentLayer().nextStep();
            if (MemoryUtil.getCrafting(maid).getCurrentLayer().isDone()) {
                DebugData.getInstance().sendMessage("[REQUEST_CRAFT_WORK]layer done");
                MemoryUtil.getCrafting(maid).nextLayer();
                MemoryUtil.getCrafting(maid).resetAndMarkVisForRequest(level, maid);
            }
        }
    }

    @Override
    protected boolean timedOut(long p_22537_) {
        return false;
    }
}
