package studio.fantasyit.maid_storage_manager.maid.behavior.logistics.craft;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import org.jetbrains.annotations.NotNull;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.craft.context.AbstractCraftActionContext;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.work.CraftLayer;
import studio.fantasyit.maid_storage_manager.debug.DebugData;
import studio.fantasyit.maid_storage_manager.maid.ChatTexts;
import studio.fantasyit.maid_storage_manager.maid.behavior.ScheduleBehavior;
import studio.fantasyit.maid_storage_manager.maid.memory.LogisticsMemory;
import studio.fantasyit.maid_storage_manager.util.BehaviorBreath;
import studio.fantasyit.maid_storage_manager.util.Conditions;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;

import java.util.Map;

public class LogisticCraftWorkBehavior extends Behavior<EntityMaid> {
    BehaviorBreath breath = new BehaviorBreath();
    AbstractCraftActionContext context;
    CraftGuideStepData craftGuideStepData;
    private CraftLayer layer;
    private boolean done;
    private boolean fail;
    private int tryTick;

    public LogisticCraftWorkBehavior() {
        super(Map.of());
    }

    @Override
    protected boolean canStillUse(ServerLevel level, EntityMaid maid, long p_22547_) {
        if (MemoryUtil.getCurrentlyWorking(maid) != ScheduleBehavior.Schedule.LOGISTICS) return false;
        if (!MemoryUtil.getLogistics(maid).shouldWork()) return false;
        if (MemoryUtil.getLogistics(maid).getStage() != LogisticsMemory.Stage.CRAFT) return false;
        return !done;
    }

    @Override
    protected boolean checkExtraStartConditions(@NotNull ServerLevel level, @NotNull EntityMaid maid) {
        if (MemoryUtil.getCurrentlyWorking(maid) != ScheduleBehavior.Schedule.LOGISTICS) return false;
        if (!MemoryUtil.getLogistics(maid).shouldWork()) return false;
        if (MemoryUtil.getLogistics(maid).getStage() != LogisticsMemory.Stage.CRAFT) return false;
        if (MemoryUtil.getLogistics(maid).getCraftLayer() == null) return false;
        CraftGuideStepData stepData = MemoryUtil.getLogistics(maid).getCraftLayer().getStepData();
        if (stepData == null) return false;
        return Conditions.hasReachedValidTargetOrReset(maid, stepData.actionType.pathCloseEnoughThreshold());
    }


    @Override
    protected void start(@NotNull ServerLevel level, @NotNull EntityMaid maid, long gameTimeIn) {
        fail = false;
        tryTick = 0;
        if (!MemoryUtil.getLogistics(maid).hasTarget()) {
            fail = done = true;
            return;
        }
        layer = MemoryUtil.getLogistics(maid).getCraftLayer();
        craftGuideStepData = layer.getStepData();
        if (craftGuideStepData == null) {
            MemoryUtil.getLogistics(maid).setStage(LogisticsMemory.Stage.OUTPUT);
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

        if (layer.getCraftData().map(CraftGuideData::getAllOutputItems).map(l -> l.size() > 0).orElse(false))
            ChatTexts.progress(maid,
                    Component.translatable(ChatTexts.CHAT_CRAFT_WORK_PROGRESS,
                            layer
                                    .getCraftData()
                                    .map(CraftGuideData::getAllOutputItems)
                                    .map(l -> l.get(0).getHoverName())
                                    .orElse(Component.empty()),
                            String.valueOf((layer.getDoneCount() + 1)),
                            layer.getCount().toString()
                    ),
                    (double) (layer.getDoneCount()) / layer.getCount()
            );
    }

    @Override
    protected void tick(ServerLevel level, EntityMaid maid, long p_22553_) {
        tryTick++;
        if (tryTick > Config.maxCraftTries) {
            fail = true;
            done = true;
            return;
        }
        if (!breath.breathTick(maid)) return;
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

    @Override
    protected void stop(ServerLevel level, EntityMaid maid, long p_22550_) {
        super.stop(level, maid, p_22550_);
        if (context != null) {
            context.stop();
        }
        MemoryUtil.getLogistics(maid).clearTarget();
        MemoryUtil.clearTarget(maid);
        if (fail) {
            MemoryUtil.getLogistics(maid).setStage(LogisticsMemory.Stage.OUTPUT);
            ChatTexts.send(maid, ChatTexts.CHAT_CRAFTING_FAIL);
        } else {
            ChatTexts.progress(maid,
                    Component.translatable(ChatTexts.CHAT_CRAFT_WORK_PROGRESS,
                            layer
                                    .getCraftData()
                                    .map(CraftGuideData::getAllOutputItems)
                                    .map(l -> l.isEmpty() ? Component.empty() : l.get(0).getHoverName())
                                    .orElse(Component.empty()),
                            String.valueOf((layer.getDoneCount() + 1)),
                            layer.getCount().toString()
                    ),
                    (double) (layer.getDoneCount()) / layer.getCount()
            );
            DebugData.sendDebug("[REQUEST_CRAFT_WORK]crafting done %s", layer.getStep());
            layer.nextStep();
            if (layer.isDone()) {
                DebugData.sendDebug("[REQUEST_CRAFT_WORK]layer done");
                MemoryUtil.getLogistics(maid).setStage(LogisticsMemory.Stage.OUTPUT);
            }
        }
    }

    @Override
    protected boolean timedOut(long p_22537_) {
        return false;
    }
}
