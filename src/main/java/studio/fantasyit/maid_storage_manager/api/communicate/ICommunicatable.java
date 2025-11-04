package studio.fantasyit.maid_storage_manager.api.communicate;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.api.communicate.data.CommunicatePlan;
import studio.fantasyit.maid_storage_manager.api.communicate.data.CommunicateRequest;
import studio.fantasyit.maid_storage_manager.api.communicate.data.CommunicateWish;
import studio.fantasyit.maid_storage_manager.api.communicate.step.IActionStep;
import studio.fantasyit.maid_storage_manager.api.communicate.wish.IActionWish;
import studio.fantasyit.maid_storage_manager.registry.MemoryModuleRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public interface ICommunicatable {
    Set<ResourceLocation> getAcceptedWishTypes();

    default @Nullable CommunicatePlan acceptCommunicateWish(EntityMaid handler, CommunicateWish wish) {
        Set<ResourceLocation> acceptedWishTypes = getAcceptedWishTypes();
        List<IActionStep> steps = new ArrayList<>();
        for (IActionWish w : wish.wishes()) {
            if (!acceptedWishTypes.contains(w.getType()))
                return null;
            List<IActionStep> steps1 = w.getSteps(handler, wish);
            if (steps1 == null)
                return null;
            steps.addAll(steps1);
        }
        return new CommunicatePlan(steps, handler);
    }

    default boolean startCommunicate(EntityMaid handler, CommunicateRequest plan) {
        if (handler != plan.handler())
            return false;
        plan.wisher().getBrain().setMemory(
                MemoryModuleRegistry.COMMUNICATE_HOLDER.get(),
                plan.getHolder()
        );
        plan.handler().getBrain().setMemory(
                MemoryModuleRegistry.COMMUNICATE_REQUEST.get(),
                plan
        );
        return true;
    }

    CommunicateRequest getCurrentCommunicateRequest(EntityMaid handler);
}
