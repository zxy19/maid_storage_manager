package studio.fantasyit.maid_storage_manager.api.communicate.data;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;
import studio.fantasyit.maid_storage_manager.api.communicate.step.base.IActionStep;

import java.util.UUID;

public record CommunicateRequest(
        CommunicatePlan plan,
        EntityMaid wisher,
        EntityMaid handler,
        UUID requestId,
        MutableInt currentStep,
        MutableBoolean prepared
) {
    public static CommunicateRequest create(CommunicatePlan plan, EntityMaid wisher) {
        return new CommunicateRequest(plan, wisher, plan.handler(), UUID.randomUUID(), new MutableInt(0), new MutableBoolean(false));
    }

    public boolean isFinished() {
        return currentStep.intValue() >= plan.steps().size();
    }

    public IActionStep getCurrentStep() {
        return plan.steps().get(currentStep.intValue());
    }

    public void nextStep() {
        currentStep.increment();
        prepared.setValue(false);
    }

    public void prepare() {
        prepared.setValue(true);
    }

    public boolean isPrepared() {
        return prepared.booleanValue();
    }

    public CommunicateHolder getHolder() {
        return new CommunicateHolder(requestId, handler);
    }
}
