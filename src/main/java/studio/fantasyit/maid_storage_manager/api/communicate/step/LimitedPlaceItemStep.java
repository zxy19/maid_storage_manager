package studio.fantasyit.maid_storage_manager.api.communicate.step;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import studio.fantasyit.maid_storage_manager.api.communicate.step.base.ActionResult;
import studio.fantasyit.maid_storage_manager.api.communicate.step.base.IMeetActionStep;
import studio.fantasyit.maid_storage_manager.entity.VirtualItemEntity;
import studio.fantasyit.maid_storage_manager.util.BehaviorBreath;
import studio.fantasyit.maid_storage_manager.util.InvUtil;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;
import studio.fantasyit.maid_storage_manager.util.MathUtil;

import java.util.List;

public class LimitedPlaceItemStep implements IMeetActionStep {
    int index = 0;
    VirtualItemEntity item;
    final List<ItemStack> marked;
    final boolean whitelist;
    final ItemStackUtil.MATCH_TYPE matchTag;
    private CombinedInvWrapper inv;

    BehaviorBreath breath = new BehaviorBreath();

    public LimitedPlaceItemStep(List<ItemStack> marked, boolean whitelist, ItemStackUtil.MATCH_TYPE matchTag) {
        this.marked = marked;
        this.whitelist = whitelist;
        this.matchTag = matchTag;
    }

    @Override
    public ActionResult start(EntityMaid wisher, EntityMaid handler) {
        breath.reset();
        inv = wisher.getAvailableInv(true);
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
        for (int c = 0; c < 20; c++) {
            if (index >= inv.getSlots()) {
                return ActionResult.SUCCESS;
            }
            ItemStack stack = inv.getStackInSlot(index);
            //todo 限制数量
            if (marked.stream().anyMatch(itemStack -> ItemStackUtil.isSame(stack, itemStack, matchTag)) == whitelist) {
                continue;
            }
            item = InvUtil.throwItemVirtual(wisher, stack, MathUtil.getFromToWithFriction(wisher, handler.position()));
            return ActionResult.CONTINUE;
        }
        return ActionResult.CONTINUE;
    }

    @Override
    public void stop(EntityMaid wisher, EntityMaid handler) {
    }
}
