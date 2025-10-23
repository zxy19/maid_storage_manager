package studio.fantasyit.maid_storage_manager.api.communicate.wish;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.api.communicate.step.RequestItemStep;
import studio.fantasyit.maid_storage_manager.api.communicate.step.SwapItemStep;
import studio.fantasyit.maid_storage_manager.api.communicate.step.base.IActionStep;
import studio.fantasyit.maid_storage_manager.communicate.SlotType;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;

import java.util.List;

public record RequestItemWish(List<ItemStack> marked, ItemStackUtil.MATCH_TYPE match,
                              SlotType slot) implements IActionWish {
    public static final ResourceLocation TYPE = new ResourceLocation("maid_storage_manager", "request_item");

    @Override
    public ResourceLocation getType() {
        return TYPE;
    }

    @Override
    public List<IActionStep> getSteps() {
        if (slot != SlotType.ALL)
            return List.of(
                    new RequestItemStep(marked, match),
                    new SwapItemStep(slot, marked, match)
            );
        return List.of(
                new RequestItemStep(marked, match)
        );
    }
}
