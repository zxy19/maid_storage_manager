package studio.fantasyit.maid_storage_manager.maid.behavior.communicate;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import studio.fantasyit.maid_storage_manager.api.communicate.context.ICommunicateContext;
import studio.fantasyit.maid_storage_manager.api.communicate.context.IDelayCompleteContext;
import studio.fantasyit.maid_storage_manager.api.communicate.context.IMultiTickContext;
import studio.fantasyit.maid_storage_manager.communicate.CommunicateUtil;
import studio.fantasyit.maid_storage_manager.communicate.WrappedCommunicateContextGetter;
import studio.fantasyit.maid_storage_manager.util.BehaviorBreath;
import studio.fantasyit.maid_storage_manager.util.Conditions;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;

import java.util.Map;

public class MaidCommunicateWorkBehavior extends Behavior<EntityMaid> {
    public MaidCommunicateWorkBehavior() {
        super(Map.of());
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        if (!MemoryUtil.getCommunicate(maid).hasTargetMaid()) return false;
        if (!Conditions.hasReachedValidTargetOrReset(MemoryUtil.getCommunicate(maid).getTargetMaid())) return false;
        return Conditions.hasReachedValidTargetOrReset(maid);
    }

    EntityMaid targetMaid = null;
    ICommunicateContext communicateContext = null;
    BehaviorBreath breath = new BehaviorBreath();
    boolean done = false;
    boolean isFinished = false;

    @Override
    protected boolean canStillUse(ServerLevel p_22545_, EntityMaid p_22546_, long p_22547_) {
        return targetMaid != null && targetMaid.isAlive() && communicateContext != null && !done;
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long p_22542_) {
        targetMaid = MemoryUtil.getCommunicate(maid).getTargetMaid();
        WrappedCommunicateContextGetter communicatable = CommunicateUtil.getWillingCommunicatable(targetMaid,maid).orElse(null);
        if (communicatable == null) {
            done = true;
            communicateContext = null;
            return;
        }
        done = false;
        isFinished = false;
        communicateContext = communicatable.get(targetMaid, maid);
        if (communicateContext == null)
            return;
        communicateContext.start(targetMaid, maid);
        if (!(communicateContext instanceof IMultiTickContext)) {
            done = true;
        }
    }

    @Override
    protected void tick(ServerLevel level, EntityMaid maid, long p_22553_) {
        if (!breath.breathTick(maid)) return;
        if (communicateContext == null) return;
        if (communicateContext instanceof IMultiTickContext mtc) {
            if (mtc.tick(targetMaid, maid)) {
                done = true;
            }
        }
    }

    @Override
    protected void stop(ServerLevel level, EntityMaid maid, long p_22550_) {
        if (targetMaid != null && communicateContext != null) {
            if (communicateContext instanceof IMultiTickContext mtc)
                mtc.stop(targetMaid, maid);
            if (communicateContext.isFinished(targetMaid, maid)) {
                MemoryUtil.getCommunicate(maid).startCooldown(targetMaid.getUUID(), level, 100);
                MemoryUtil.clearTarget(targetMaid);
            }
            if (communicateContext instanceof IDelayCompleteContext idcc)
                MemoryUtil.getCommunicate(maid).setDelayCompleteContext(idcc);
        }
        MemoryUtil.getCommunicate(maid).setTargetMaid(null);
    }

    @Override
    protected boolean timedOut(long p_22537_) {
        return false;
    }
}
