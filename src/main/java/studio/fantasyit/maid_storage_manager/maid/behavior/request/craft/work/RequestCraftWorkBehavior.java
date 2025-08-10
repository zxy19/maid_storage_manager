package studio.fantasyit.maid_storage_manager.maid.behavior.request.craft.work;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.craft.context.AbstractCraftActionContext;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.work.CraftLayer;
import studio.fantasyit.maid_storage_manager.craft.work.CraftLayerChain;
import studio.fantasyit.maid_storage_manager.data.BoxTip;
import studio.fantasyit.maid_storage_manager.debug.DebugData;
import studio.fantasyit.maid_storage_manager.maid.ChatTexts;
import studio.fantasyit.maid_storage_manager.maid.behavior.ScheduleBehavior;
import studio.fantasyit.maid_storage_manager.network.ShowCommonPacket;
import studio.fantasyit.maid_storage_manager.util.BehaviorBreath;
import studio.fantasyit.maid_storage_manager.util.Conditions;
import studio.fantasyit.maid_storage_manager.util.InvUtil;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;

import java.util.Map;

public class RequestCraftWorkBehavior extends Behavior<EntityMaid> {
    BehaviorBreath breath = new BehaviorBreath();
    AbstractCraftActionContext context;
    CraftGuideStepData craftGuideStepData;
    private CraftLayer layer;
    private CraftLayerChain plan;
    private boolean done;
    private boolean fail;
    private boolean skipped;
    private boolean hasTrySkip;
    private boolean isOccupied;

    public RequestCraftWorkBehavior() {
        super(Map.of());
    }


    @Override
    protected boolean checkExtraStartConditions(@NotNull ServerLevel level, @NotNull EntityMaid maid) {
        if (MemoryUtil.getCurrentlyWorking(maid) != ScheduleBehavior.Schedule.REQUEST) return false;
        if (MemoryUtil.isWorking(maid)) return false;
        if (!MemoryUtil.getRequestProgress(maid).isTryCrafting()) return false;
        if (MemoryUtil.getCrafting(maid).isGatheringDispatched()) return false;
        if (!MemoryUtil.getCrafting(maid).hasTarget()) return false;
        if (!MemoryUtil.getCrafting(maid).hasPlan()) return false;
        if (!MemoryUtil.getCrafting(maid).plan().isCurrentWorking()) return false;
        CraftGuideStepData stepData = MemoryUtil.getCrafting(maid).plan().getCurrentLayer().getStepData();
        if (stepData == null) return false;
        return Conditions.hasReachedValidTargetOrReset(maid, stepData.actionType.pathCloseEnoughThreshold());
    }


    @Override
    protected void start(@NotNull ServerLevel level, @NotNull EntityMaid maid, long gameTimeIn) {
        fail = false;
        skipped = false;
        hasTrySkip = false;
        if (!MemoryUtil.getCrafting(maid).hasTarget()) {
            fail = done = true;
            return;
        }
        plan = MemoryUtil.getCrafting(maid).plan();
        layer = plan.getCurrentLayer();
        craftGuideStepData = layer.getStepData();
        plan.showCraftingProgress(maid);
        plan.setStatusMessage(maid,
                Component.translatable(
                        ChatTexts.CHAT_CRAFT_WORK_PROGRESS,
                        layer
                                .getCraftData()
                                .map(CraftGuideData::getAllOutputItems)
                                .map(l -> l.get(0).getHoverName())
                                .orElse(Component.empty()),
                        layer.getDoneCount().toString(),
                        layer.getCount().toString()
                )
        );
        if (craftGuideStepData == null) {
            MemoryUtil.getCrafting(maid).lastSuccess();
            done = true;
            return;
        }
        done = false;
        if (plan.checkIsCurrentOccupied(level, maid)) {
            tryRelease(maid);
            if (done) {
                return;
            }
            isOccupied = true;
        } else {
            isOccupied = false;
            plan.setOccupied(level, maid);
        }
        breath.reset();
        context = layer.startStep(maid);
        if (context == null) {
            fail = true;
            done = true;
            return;
        }
        context.loadEnv(layer.getEnv());
        AbstractCraftActionContext.Result start = context.start();
        if (start == AbstractCraftActionContext.Result.SUCCESS) {
            fail = false;
            done = true;
        } else if (start == AbstractCraftActionContext.Result.FAIL) {
            fail = true;
            done = true;
        }
        MemoryUtil.setWorking(maid, true);
        InvUtil.mergeSameStack(maid.getAvailableInv(false));
    }

    @Override
    protected boolean canStillUse(ServerLevel level, EntityMaid maid, long p_22547_) {
        if (!Conditions.takingRequestList(maid)) return false;
        if (!MemoryUtil.getRequestProgress(maid).isTryCrafting()) return false;
        if (!layer.hasCollectedAll()) return false;
        return !done;
    }

    @Override
    protected void tick(ServerLevel level, EntityMaid maid, long p_22553_) {
        if (isOccupied) {
            if (plan.checkIsCurrentOccupied(level, maid))
                return;
            isOccupied = false;
            plan.setOccupied(level, maid);
        }
        if (layer.addAndGetTryTick() > Config.maxCraftTries) {
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
            case CONTINUE -> {
                layer.setTryTick(0);
            }
            case NOT_DONE_INTERRUPTABLE -> {
                tryRelease(maid);
            }
            case CONTINUE_INTERRUPTABLE -> {
                layer.setTryTick(0);
                tryRelease(maid);
            }
            case NOT_DONE -> {
            }
        }
    }

    protected void tryRelease(EntityMaid maid) {
        if (!hasTrySkip && plan.tryReleaseAndStartNext()) {
            MemoryUtil.getCrafting(maid).clearTarget();
            MemoryUtil.clearTarget(maid);
            skipped = true;
            done = true;
        }
        hasTrySkip = true;
        if (plan.hasDispatchedWaitingCheck(maid)) {
            skipped = true;
            done = true;
        }
    }

    private static final float[] colors_r = new float[]{0.91f, 0.12f, 0.39f, 1};

    @Override
    protected void stop(ServerLevel level, EntityMaid maid, long p_22550_) {
        super.stop(level, maid, p_22550_);
        MemoryUtil.setWorking(maid, false);
        if (context != null) {
            context.stop();
            layer.setEnv(context.saveEnv(layer.getEnv()));
        }
        MemoryUtil.getCrafting(maid).clearTarget();
        MemoryUtil.clearTarget(maid);
        if (skipped) return;
        if (!MemoryUtil.getCrafting(maid).hasPlan()) return;
        if (fail) {
            DebugData.sendDebug("[REQUEST_CRAFT_WORK]crafting fail");
            plan.failCurrent(maid, craftGuideStepData.getItems(), "tooltip.maid_storage_manager.request_list.fail_crafting");
            PacketDistributor.sendToPlayersTrackingEntity(maid,
                    new ShowCommonPacket(new BoxTip(
                            craftGuideStepData.getStorage(),
                            Component.translatable("tip.maid_storage_manager.crafting_fail"),
                            600,
                            colors_r
                    )));
            if (!plan.isMaster() && maid.level() instanceof ServerLevel sl && sl.getEntity(plan.getMasterUUID()) instanceof EntityMaid toMaid && MemoryUtil.getCrafting(toMaid).hasPlan()) {
                CraftLayerChain plan1 = MemoryUtil.getCrafting(toMaid).plan();
                plan1.dispatchedFail("tooltip.maid_storage_manager.request_list.fail_crafting");
            }
        } else {
            DebugData.sendDebug("[REQUEST_CRAFT_WORK]crafting done %s", layer.getStep());
            layer.nextStep();
            if (layer.isDone()) {
                plan.removeOccupied(level, maid);
                DebugData.sendDebug("[REQUEST_CRAFT_WORK]layer done");
                plan.finishCurrentLayer(maid);
                MemoryUtil.getCrafting(maid).resetAndMarkVis(level, maid);
                plan.showCraftingProgress(maid);
            } else {
                plan.checkInputInbackpack(maid);
            }
            plan.handleStopAddingEvent(maid);
        }
    }

    @Override
    protected boolean timedOut(long p_22537_) {
        return false;
    }
}
