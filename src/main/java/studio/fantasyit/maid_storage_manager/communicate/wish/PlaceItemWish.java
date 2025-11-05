package studio.fantasyit.maid_storage_manager.communicate.wish;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.api.communicate.data.CommunicateWish;
import studio.fantasyit.maid_storage_manager.api.communicate.step.IActionStep;
import studio.fantasyit.maid_storage_manager.api.communicate.wish.IActionWish;
import studio.fantasyit.maid_storage_manager.communicate.data.SlotType;
import studio.fantasyit.maid_storage_manager.communicate.step.LimitedPlaceItemStep;
import studio.fantasyit.maid_storage_manager.communicate.step.PlaceItemStep;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;

import java.util.List;

public record PlaceItemWish(List<ItemStack> marked, boolean whitelist, boolean count,
                            SlotType slot,
                            ItemStackUtil.MATCH_TYPE match) implements IActionWish {
    public static final ResourceLocation TYPE = ResourceLocation.fromNamespaceAndPath("maid_storage_manager", "place_item");

    @Override
    public ResourceLocation getType() {
        return TYPE;
    }

    @Override
    public List<IActionStep> getSteps(EntityMaid handler, CommunicateWish wish) {
        if (count)
            return List.of(new LimitedPlaceItemStep(marked, slot, match));
        return List.of(new PlaceItemStep(marked, whitelist, slot, match));
    }

}
