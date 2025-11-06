package studio.fantasyit.maid_storage_manager.api.communicate.data;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraftforge.common.MinecraftForge;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;
import studio.fantasyit.maid_storage_manager.api.communicate.step.IActionStep;
import studio.fantasyit.maid_storage_manager.api.event.CommunicateFinishEvent;
import studio.fantasyit.maid_storage_manager.communicate.CommunicateUtil;
import studio.fantasyit.maid_storage_manager.registry.MemoryModuleRegistry;

import java.util.UUID;

public record CommunicateRequest(
        CommunicatePlan plan,
        EntityMaid wisher,
        EntityMaid handler,
        UUID requestId,
        MutableInt currentStep,
        MutableBoolean prepared,
        MutableBoolean working,
        MutableInt tickToTimeOut,
        MutableBoolean failed
) {
    public static CommunicateRequest create(CommunicatePlan plan, EntityMaid wisher) {
        return new CommunicateRequest(plan,
                wisher,
                plan.handler(),
                UUID.randomUUID(),
                new MutableInt(0),
                new MutableBoolean(false),
                new MutableBoolean(false),
                new MutableInt(1200),
                new MutableBoolean(false)
        );
    }

    public boolean isFinished() {
        return currentStep.intValue() >= plan.steps().size();
    }

    public IActionStep getCurrentStep() {
        if (currentStep.intValue() >= plan.steps().size())
            return null;
        return plan.steps().get(currentStep.intValue());
    }

    public void nextStep() {
        currentStep.increment();
        prepared.setValue(false);
        working.setValue(false);
        resetTimeout();
    }

    public void prepare() {
        prepared.setValue(true);
        resetTimeout();
    }

    public void startWorking() {
        working.setValue(true);
        resetTimeout();
    }

    public boolean isPrepared() {
        return prepared.booleanValue();
    }

    public boolean isWorking() {
        return working.booleanValue();
    }

    public CommunicateHolder getHolder() {
        return new CommunicateHolder(requestId, handler);
    }

    public void stopAndClear() {
        wisher.getBrain().eraseMemory(MemoryModuleRegistry.COMMUNICATE_HOLDER.get());
        handler.getBrain().eraseMemory(MemoryModuleRegistry.COMMUNICATE_REQUEST.get());
        CommunicateUtil.setLastResult(wisher, requestId, !isFailed());
        MinecraftForge.EVENT_BUS.post(new CommunicateFinishEvent(this));
    }

    public boolean isValid() {
        if (wisher == null || handler == null || !wisher.isAlive() || !handler.isAlive())
            return false;
        if (!wisher.getBrain().hasMemoryValue(MemoryModuleRegistry.COMMUNICATE_HOLDER.get()))
            return false;
        if (!handler.getBrain().hasMemoryValue(MemoryModuleRegistry.COMMUNICATE_REQUEST.get()))
            return false;
        //noinspection OptionalGetWithoutIsPresent
        return wisher.getBrain().getMemory(MemoryModuleRegistry.COMMUNICATE_HOLDER.get()).get().requestId().equals(requestId) &&
                handler.getBrain().getMemory(MemoryModuleRegistry.COMMUNICATE_REQUEST.get()).get().requestId().equals(requestId);
    }

    public void resetTimeout() {
        tickToTimeOut.setValue(1200);
    }

    public void tick() {
        if (tickToTimeOut.intValue() > 0) {
            tickToTimeOut.decrement();
        } else {
            stopAndClear();
        }
    }

    public void fail() {
        failed.setValue(true);
    }

    public boolean isFailed() {
        return failed.booleanValue();
    }
}
