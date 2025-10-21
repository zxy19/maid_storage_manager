package studio.fantasyit.maid_storage_manager.api.communicate.data;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import org.apache.commons.lang3.mutable.MutableInt;
import studio.fantasyit.maid_storage_manager.api.communicate.step.base.IActionStep;

import java.util.UUID;

public record CommunicateRequest(
        CommunicatePlan plan,
        EntityMaid wisher,
        EntityMaid handler,
        UUID requestId,
        MutableInt currentStep
) {
    public boolean isFinished() {
        return currentStep.intValue() >= plan.steps().size();
    }

    public IActionStep getCurrentStep() {
        return plan.steps().get(currentStep.intValue());
    }

    public void nextStep() {
        currentStep.increment();
    }
}
