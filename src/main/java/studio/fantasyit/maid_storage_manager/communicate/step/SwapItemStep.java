package studio.fantasyit.maid_storage_manager.communicate.step;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.mutable.MutableInt;
import studio.fantasyit.maid_storage_manager.api.communicate.step.ActionResult;
import studio.fantasyit.maid_storage_manager.api.communicate.step.IMeetActionStep;
import studio.fantasyit.maid_storage_manager.communicate.data.SlotType;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;

import java.util.List;

public class SwapItemStep implements IMeetActionStep {
    final SlotType slot;
    final List<ItemStack> marked;
    final ItemStackUtil.MATCH_TYPE match;
    List<ItemStack> currentHas;
    int index = 0;

    public SwapItemStep(SlotType slot, List<ItemStack> marked, ItemStackUtil.MATCH_TYPE match) {
        this.slot = slot;
        this.marked = marked;
        this.match = match;
    }

    @Override
    public ActionResult start(EntityMaid wisher, EntityMaid handler) {
        currentHas = slot.getItemStacks(wisher);
        return ActionResult.CONTINUE;
    }

    @Override
    public ActionResult tick(EntityMaid wisher, EntityMaid handler) {
        if (index >= marked.size())
            return ActionResult.SUCCESS;
        ItemStack item = marked.get(index);
        int count = item.getCount();
        for (ItemStack current : currentHas) {
            if (ItemStackUtil.isSame(current, item, match)) {
                count -= current.getCount();
            }
        }
        if (count <= 0) {
            index++;
            return ActionResult.CONTINUE;
        }
        MutableInt finalCount = new MutableInt(count);
        slot.iterItemExceptSlotForMaid(wisher, itemStack -> {
            if (!ItemStackUtil.isSame(itemStack, item, match))
                return itemStack;
            int maxTransfer = Math.min(itemStack.getCount(), finalCount.getValue());
            ItemStack remainNotTransfer = slot.tryPlaceItemIn(itemStack.copyWithCount(maxTransfer), wisher);
            finalCount.subtract(maxTransfer - remainNotTransfer.getCount());
            return itemStack.copyWithCount(itemStack.getCount() - maxTransfer + remainNotTransfer.getCount());
        });
        index++;
        return ActionResult.CONTINUE;
    }
}
