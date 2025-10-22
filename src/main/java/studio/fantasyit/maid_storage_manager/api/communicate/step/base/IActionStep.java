package studio.fantasyit.maid_storage_manager.api.communicate.step.base;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public interface IActionStep {
    default boolean isAvailable(Map<ResourceLocation, Boolean> checks, EntityMaid wisher, EntityMaid handler) {
        return true;
    }

    default boolean prepare(EntityMaid wisher, EntityMaid handler) {
        return true;
    }

    default boolean isPrepareDone(EntityMaid wisher, EntityMaid handler) {
        return true;
    }

    ActionResult start(EntityMaid wisher, EntityMaid handler);

    default ActionResult tick(EntityMaid wisher, EntityMaid handler) {
        return ActionResult.FAIL;
    }

    default void stop(EntityMaid wisher, EntityMaid handler) {
    }
}
