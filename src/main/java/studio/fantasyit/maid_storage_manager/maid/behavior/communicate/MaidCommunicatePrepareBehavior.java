package studio.fantasyit.maid_storage_manager.maid.behavior.communicate;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import studio.fantasyit.maid_storage_manager.api.communicate.data.CommunicateRequest;
import studio.fantasyit.maid_storage_manager.api.communicate.step.IActionStep;
import studio.fantasyit.maid_storage_manager.communicate.CommunicateUtil;
import studio.fantasyit.maid_storage_manager.registry.MemoryModuleRegistry;

import java.util.Map;

public class MaidCommunicatePrepareBehavior extends Behavior<EntityMaid> {
    private IActionStep step;
    private boolean isEnd;
    private boolean isSuccess;

    public MaidCommunicatePrepareBehavior() {
        super(Map.of(
                MemoryModuleRegistry.COMMUNICATE_REQUEST.get(), MemoryStatus.VALUE_PRESENT
        ));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        CommunicateRequest communicateRequest = CommunicateUtil.getCommunicateRequest(maid);
        if (communicateRequest == null)
            return false;
        if (communicateRequest.isFinished())
            return false;
        IActionStep currentStep = communicateRequest.getCurrentStep();
        if (currentStep == null)
            return false;
        return currentStep.shouldRunPrepare(communicateRequest.wisher(), maid, communicateRequest.isPrepared());
    }


    @Override
    protected void start(ServerLevel level, EntityMaid maid, long p_22542_) {
        CommunicateRequest communicateRequest = CommunicateUtil.getCommunicateRequest(maid);
        if (communicateRequest == null)
            return;
        step = communicateRequest.getCurrentStep();
        assert step != null;
        if (step.prepare(communicateRequest.wisher(), maid)) {
            communicateRequest.prepare();
        }
    }
}
