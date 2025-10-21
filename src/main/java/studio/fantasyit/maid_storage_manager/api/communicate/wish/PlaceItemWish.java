package studio.fantasyit.maid_storage_manager.api.communicate.wish;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.api.communicate.step.PlaceItemStep;
import studio.fantasyit.maid_storage_manager.api.communicate.step.base.IActionStep;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;

import java.util.List;

public record PlaceItemWish(List<ItemStack> marked, boolean whitelist,
                            ItemStackUtil.MATCH_TYPE match) implements IActionWish {
    public static final ResourceLocation TYPE = new ResourceLocation("maid_storage_manager:place_item");

    @Override
    public ResourceLocation getType() {
        return TYPE;
    }

    @Override
    public List<IActionStep> getSteps() {
        return List.of(new PlaceItemStep(marked, whitelist, match));
    }

}
