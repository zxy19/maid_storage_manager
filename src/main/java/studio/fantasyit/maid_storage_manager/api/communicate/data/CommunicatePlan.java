package studio.fantasyit.maid_storage_manager.api.communicate.data;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.resources.ResourceLocation;
import studio.fantasyit.maid_storage_manager.api.communicate.step.IActionStep;

import java.util.HashMap;
import java.util.List;

public record CommunicatePlan(
        List<IActionStep> steps,
        EntityMaid handler
) {
    public boolean isAvailable(EntityMaid wisher) {
        HashMap<ResourceLocation, Boolean> checks = new HashMap<>();
        for (IActionStep step : steps) {
            if (!step.isAvailable(checks, wisher, handler))
                return false;
        }
        return true;
    }
}