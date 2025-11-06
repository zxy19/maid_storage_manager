package studio.fantasyit.maid_storage_manager.maid.behavior.communicate;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import studio.fantasyit.maid_storage_manager.api.communicate.ICommunicatable;
import studio.fantasyit.maid_storage_manager.api.communicate.data.CommunicateRequest;
import studio.fantasyit.maid_storage_manager.api.communicate.step.ActionResult;
import studio.fantasyit.maid_storage_manager.api.communicate.step.IActionStep;
import studio.fantasyit.maid_storage_manager.communicate.CommunicateUtil;
import studio.fantasyit.maid_storage_manager.registry.MemoryModuleRegistry;

import java.util.Map;

public class MaidCommunicateWorkBehavior extends Behavior<EntityMaid> {
    private IActionStep step;
    private CommunicateRequest communicateRequest;
    private boolean isEnd;
    private boolean isSuccess;
    private boolean isKeepOn;

    public MaidCommunicateWorkBehavior() {
        super(Map.of(
                MemoryModuleRegistry.COMMUNICATE_REQUEST.get(), MemoryStatus.VALUE_PRESENT
        ));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        CommunicateRequest communicateRequest = CommunicateUtil.getCommunicateRequest(maid);
        if (communicateRequest == null || communicateRequest.isFinished() || !communicateRequest.isPrepared())
            return false;
        IActionStep currentStep = communicateRequest.getCurrentStep();
        if (currentStep == null)
            return false;
        return currentStep.isPrepareDone(communicateRequest.wisher(), maid);
    }


    @Override
    protected boolean canStillUse(ServerLevel p_22545_, EntityMaid p_22546_, long p_22547_) {
        if (communicateRequest == null || communicateRequest.isFinished() || !communicateRequest.isValid())
            return false;
        return !isEnd;
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long p_22542_) {
        if (!(maid.getTask() instanceof ICommunicatable communicatable))
            return;
        communicateRequest = communicatable.getCurrentCommunicateRequest(maid);
        if (communicateRequest == null || communicateRequest.isFinished())
            return;
        step = communicateRequest.getCurrentStep();
        ActionResult startResult = step.start(communicateRequest.wisher(), communicateRequest.handler());
        isEnd = startResult.isEnd();
        isSuccess = startResult.isSuccess();
        isKeepOn = startResult.isKeepon();
        communicateRequest.startWorking();
    }

    @Override
    protected void tick(ServerLevel level, EntityMaid maid, long p_22553_) {
        if (isEnd)
            return;
        ActionResult tick = step.tick(communicateRequest.wisher(), maid);
        isEnd = tick.isEnd();
        isSuccess = isSuccess && tick.isSuccess();
        isKeepOn = isKeepOn && tick.isKeepon();
    }

    @Override
    protected void stop(ServerLevel level, EntityMaid maid, long p_22550_) {
        step.stop(communicateRequest.wisher(), communicateRequest.handler());
        if (!isSuccess)
            communicateRequest.fail();
        if (isEnd && isKeepOn) {
            communicateRequest.nextStep();
            if (communicateRequest.isFinished())
                communicateRequest.stopAndClear();
        } else
            communicateRequest.stopAndClear();
    }

    @Override
    protected boolean timedOut(long p_22537_) {
        return false;
    }
}
