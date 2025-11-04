package studio.fantasyit.maid_storage_manager.communicate.wish;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.api.communicate.data.CommunicateWish;
import studio.fantasyit.maid_storage_manager.api.communicate.step.IActionStep;
import studio.fantasyit.maid_storage_manager.api.communicate.wish.IActionWish;
import studio.fantasyit.maid_storage_manager.communicate.data.SlotType;
import studio.fantasyit.maid_storage_manager.communicate.step.RequestItemStep;
import studio.fantasyit.maid_storage_manager.communicate.step.SwapItemStep;
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
    public List<IActionStep> getSteps(EntityMaid handler, CommunicateWish wish) {
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
