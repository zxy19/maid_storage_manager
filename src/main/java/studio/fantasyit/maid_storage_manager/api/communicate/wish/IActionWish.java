package studio.fantasyit.maid_storage_manager.api.communicate.wish;

import net.minecraft.resources.ResourceLocation;
import studio.fantasyit.maid_storage_manager.api.communicate.step.base.IActionStep;

import java.util.List;

public interface IActionWish {
    ResourceLocation getType();

    List<IActionStep> getSteps();
}
