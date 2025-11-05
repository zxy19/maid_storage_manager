package studio.fantasyit.maid_storage_manager.communicate.wish;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.api.communicate.data.CommunicateWish;
import studio.fantasyit.maid_storage_manager.api.communicate.step.IActionStep;
import studio.fantasyit.maid_storage_manager.api.communicate.wish.IActionWish;
import studio.fantasyit.maid_storage_manager.communicate.data.SlotType;
import studio.fantasyit.maid_storage_manager.communicate.step.LimitedPlaceItemStep;
import studio.fantasyit.maid_storage_manager.maid.memory.ViewedInventoryMemory;
import studio.fantasyit.maid_storage_manager.maid.task.StorageManageTask;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;

import java.util.ArrayList;
import java.util.List;

public record PlaceItemWishWithLimitation(List<Pair<ItemStack, Integer>> marked,
                                          SlotType slot,
                                          ItemStackUtil.MATCH_TYPE match) implements IActionWish {
    public static final ResourceLocation TYPE = ResourceLocation.fromNamespaceAndPath("maid_storage_manager", "place_item");

    @Override
    public ResourceLocation getType() {
        return TYPE;
    }

    @Override
    public List<IActionStep> getSteps(EntityMaid handler, CommunicateWish wish) {
        if (!handler.getTask().getUid().equals(StorageManageTask.TASK_ID))
            return null;
        ViewedInventoryMemory vi = MemoryUtil.getViewedInventory(handler);
        List<ItemStack> toStore = new ArrayList<>();
        for (Pair<ItemStack, Integer> pair : marked) {
            if (pair.getB() != -1) {
                int itemCount = vi.getItemCount(pair.getA(), match);
                if (itemCount > pair.getB()) continue;
                //仓库最多允许存pair.getB()个物品，现有itemCount个。
                int tsc = Math.min(pair.getB() - itemCount, pair.getA().getCount());
                ItemStackUtil.addToList(toStore, pair.getA().copyWithCount(tsc), match);
            } else {
                ItemStackUtil.addToList(toStore, pair.getA(), match);
            }
        }
        return List.of(new LimitedPlaceItemStep(toStore, slot, match));
    }
}
