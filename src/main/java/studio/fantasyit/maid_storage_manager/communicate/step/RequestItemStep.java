package studio.fantasyit.maid_storage_manager.communicate.step;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.api.communicate.step.ActionResult;
import studio.fantasyit.maid_storage_manager.api.communicate.step.IActionStep;
import studio.fantasyit.maid_storage_manager.util.Conditions;
import studio.fantasyit.maid_storage_manager.util.InvUtil;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;
import studio.fantasyit.maid_storage_manager.util.RequestItemUtil;

import java.util.List;

public class RequestItemStep implements IActionStep {
    final List<ItemStack> requested;
    final ItemStackUtil.MATCH_TYPE matchTag;
    boolean isDone;
    boolean isSuccess;

    public RequestItemStep(List<ItemStack> requested, ItemStackUtil.MATCH_TYPE matchTag) {
        this.requested = requested;
        this.matchTag = matchTag;
        isDone = false;
        isSuccess = false;
    }

    @Override
    public boolean prepare(EntityMaid wisher, EntityMaid handler) {
        if (!InvUtil.isEmpty(handler.getAvailableInv(true)))
            return false;
        ItemStack vi = RequestItemUtil.makeVirtualItemStack(requested, null, wisher, "COMMUNICATE");
        InvUtil.tryPlace(handler.getAvailableInv(true), vi);
        return true;
    }

    @Override
    public boolean isPrepareDone(EntityMaid wisher, EntityMaid handler) {
        if (isDone)
            return true;
        return !Conditions.takingRequestList(handler);
    }

    @Override
    public ActionResult start(EntityMaid wisher, EntityMaid handler) {
        return isSuccess ? ActionResult.SUCCESS : ActionResult.SOFT_FAIL;
    }

    public void onRequestDone(boolean success) {
        isDone = true;
        isSuccess = success;
    }
}
