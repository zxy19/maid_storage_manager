package studio.fantasyit.maid_storage_manager.communicate.step;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.mutable.MutableInt;
import studio.fantasyit.maid_storage_manager.api.communicate.step.ActionResult;
import studio.fantasyit.maid_storage_manager.api.communicate.step.IMeetActionStep;
import studio.fantasyit.maid_storage_manager.communicate.data.SlotType;
import studio.fantasyit.maid_storage_manager.entity.VirtualItemEntity;
import studio.fantasyit.maid_storage_manager.util.BehaviorBreath;
import studio.fantasyit.maid_storage_manager.util.InvUtil;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;
import studio.fantasyit.maid_storage_manager.util.MathUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class LimitedPlaceItemStep implements IMeetActionStep {
    int index = 0;
    List<MutableInt> requiredCounts;
    HashMap<Integer, MutableInt> indexOfMarkToSlot;
    VirtualItemEntity item;
    private final List<ItemStack> marked;
    private final ItemStackUtil.MATCH_TYPE matchTag;
    private final SlotType slot;

    BehaviorBreath breath = new BehaviorBreath();

    public LimitedPlaceItemStep(List<ItemStack> marked, SlotType slot, ItemStackUtil.MATCH_TYPE matchTag) {
        this.marked = marked;
        this.matchTag = matchTag;
        this.slot = slot;
        requiredCounts = marked.stream().map(ItemStack::getCount).map(MutableInt::new).toList();
        indexOfMarkToSlot = new HashMap<>();
    }

    @Override
    public ActionResult start(EntityMaid wisher, EntityMaid handler) {
        breath.reset();
        item = null;
        return ActionResult.CONTINUE;
    }

    @Override
    public ActionResult tick(EntityMaid wisher, EntityMaid handler) {
        if (!breath.breathTick(wisher)) {
            return ActionResult.CONTINUE;
        }
        if (item != null) {
            InvUtil.pickUpVirtual(handler, item);
            if (item.isAlive()) {
                InvUtil.pickUpVirtual(wisher, item);
                return ActionResult.FAIL;
            }
            item = null;
            return ActionResult.CONTINUE;
        }

        Optional<Integer> i1 = slot.processSlotItemsAndGetIsFinished(wisher, index, (stack, idx) -> {
            if (!indexOfMarkToSlot.containsKey(idx))
                indexOfMarkToSlot.put(idx, new MutableInt(0));
            MutableInt _i = indexOfMarkToSlot.get(idx);
            for (; _i.getValue() < marked.size(); _i.increment()) {
                int i = _i.getValue();
                if (requiredCounts.get(i).intValue() <= 0)
                    continue;
                ItemStack itemStack = marked.get(i);
                if (ItemStackUtil.isSame(stack, itemStack, matchTag)) {
                    int realTake = Math.min(stack.getCount(), requiredCounts.get(i).intValue());
                    item = InvUtil.throwItemVirtual(wisher, stack.copyWithCount(realTake), MathUtil.getFromToWithFriction(wisher, handler.position()));
                    requiredCounts.get(i).subtract(realTake);
                    stack.shrink(realTake);
                    if (realTake != 0) {
                        return stack;
                    }
                }
            }
            return stack;
        });
        if (i1.isPresent()) {
            index = i1.get();
            return ActionResult.CONTINUE;
        }
        if(item != null)
            return ActionResult.CONTINUE;
        return ActionResult.SUCCESS;
    }
}
