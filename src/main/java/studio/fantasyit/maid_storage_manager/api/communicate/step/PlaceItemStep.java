package studio.fantasyit.maid_storage_manager.api.communicate.step;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.api.communicate.step.base.ActionResult;
import studio.fantasyit.maid_storage_manager.api.communicate.step.base.IMeetActionStep;
import studio.fantasyit.maid_storage_manager.communicate.SlotType;
import studio.fantasyit.maid_storage_manager.entity.VirtualItemEntity;
import studio.fantasyit.maid_storage_manager.util.BehaviorBreath;
import studio.fantasyit.maid_storage_manager.util.InvUtil;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;
import studio.fantasyit.maid_storage_manager.util.MathUtil;

import java.util.List;
import java.util.Optional;

public class PlaceItemStep implements IMeetActionStep {
    int index = 0;
    VirtualItemEntity item;
    final List<ItemStack> marked;
    final boolean whitelist;
    final ItemStackUtil.MATCH_TYPE matchTag;
    private final SlotType slot;

    BehaviorBreath breath = new BehaviorBreath();

    public PlaceItemStep(List<ItemStack> marked, boolean whitelist, SlotType slot, ItemStackUtil.MATCH_TYPE matchTag) {
        this.marked = marked;
        this.whitelist = whitelist;
        this.matchTag = matchTag;
        this.slot = slot;
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
        Optional<Integer> i = slot.processSlotItemsAndGetIsFinished(wisher, index, (stack, idx) -> {
            if (marked.stream().anyMatch(itemStack -> ItemStackUtil.isSame(stack, itemStack, matchTag)) == whitelist) {
                return stack;
            }
            item = InvUtil.throwItemVirtual(wisher, stack, MathUtil.getFromToWithFriction(wisher, handler.position()));
            return ItemStack.EMPTY;
        });
        if (i.isPresent()) {
            index = i.get();
            return ActionResult.CONTINUE;
        }
        return ActionResult.SUCCESS;
    }
}
