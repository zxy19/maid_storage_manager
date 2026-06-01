package studio.fantasyit.maid_storage_manager.storage.ItemHandler;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.storage.base.AbstractFilterableBlockStorage;
import studio.fantasyit.maid_storage_manager.storage.base.ISlotBasedStorage;
import studio.fantasyit.maid_storage_manager.storage.base.ISortSlotContext;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;

import java.util.Stack;

public class AbstractItemHandlerContext extends AbstractFilterableBlockStorage implements ISortSlotContext, ISlotBasedStorage {

    boolean isSortingSlots = false;
    int sortingSlot = -1;
    int maxSortingChances = 0;
    protected SimulateTargetInteractHelper helper;

    @Override
    public void start(EntityMaid maid, ServerLevel level, Target target) {
        super.start(maid, level, target);
        helper = new SimulateTargetInteractHelper(maid, target.getPos(), target.getSide().orElse(null), level);
        helper.open();
    }

    @Override
    public void startSorting() {
        if (helper.itemHandler == null)
            return;
        isSortingSlots = true;
        sortingSlot = helper.itemHandler.size() - 1;
        maxSortingChances = helper.itemHandler.size() * helper.itemHandler.size() / 2;
    }

    @Override
    public void tickSorting() {
        if (helper.itemHandler == null || !isSortingSlots || sortingSlot <= 0 || maxSortingChances <= 0) {
            isSortingSlots = false;
            return;
        }
        int cstT = Math.max(3000 / helper.itemHandler.size(), 1);
        while (sortingSlot >= 0 && cstT > 0 && maxSortingChances > 0) {
            if (ItemUtil.getStack(helper.itemHandler, sortingSlot).isEmpty()) {
                sortingSlot--;
                continue;
            }
            cstT--;
            int cur = sortingSlot - 1;
            int targetP = -1;
            int lastSuccess = -1;
            while (cur >= 0 && isSame(cur, sortingSlot))
                cur--;
            if (cur < 0) {
                sortingSlot = -1;
                isSortingSlots = false;
                continue;
            }
            while (cur >= 0) {
                if (isSame(cur, sortingSlot)) {
                    if (lastSuccess != cur + 1)
                        targetP = cur;
                    lastSuccess = cur;
                }
                cur--;
            }
            if (targetP != -1) {
                int targetIndex = targetP + 1;
                swap(targetIndex, sortingSlot);
                maxSortingChances--;
                sortingSlot = helper.itemHandler.size() - 1;
            } else {
                sortingSlot--;
            }
        }
    }

    private void swap(int slot1, int slot2) {
        if (slot2 == slot1 || helper.itemHandler == null) return;
        Stack<ItemStack> extracted = new Stack<>();
        ItemStack stackInSlot1 = ItemUtil.getStack(helper.itemHandler, slot1);
        int t0 = stackInSlot1.getCount();
        while (ItemUtil.getStack(helper.itemHandler, slot1).getCount() > 0 && t0 > 0) {
            int toExtract = ItemUtil.getStack(helper.itemHandler, slot1).getCount();
            try (var tx = Transaction.open(null)) {
                int extractedCount = helper.itemHandler.extract(slot1, ItemResource.of(stackInSlot1), toExtract, tx);
                tx.commit();
                t0 -= extractedCount;
                if (extractedCount == 0) break;
                extracted.push(stackInSlot1.copyWithCount(extractedCount));
            }
        }
        ItemStack stackInSlot2 = ItemUtil.getStack(helper.itemHandler, slot2);
        int t1 = stackInSlot2.getCount();
        while (ItemUtil.getStack(helper.itemHandler, slot2).getCount() > 0 && t1 > 0) {
            ItemStack currentStack2 = ItemUtil.getStack(helper.itemHandler, slot2);
            if (currentStack2.isEmpty()) break;
            try (var tx = Transaction.open(null)) {
                int inserted = helper.itemHandler.insert(slot1, ItemResource.of(currentStack2), currentStack2.getCount(), tx);
                if (inserted > 0) {
                    int extractedCount = helper.itemHandler.extract(slot2, ItemResource.of(currentStack2), inserted, tx);
                    t1 -= extractedCount;
                    tx.commit();
                }
            }
        }
        while (!extracted.isEmpty()) {
            ItemStack tmp = extracted.pop();
            int count1 = tmp.getCount();
            try (var tx = Transaction.open(null)) {
                int inserted = helper.itemHandler.insert(slot2, ItemResource.of(tmp), count1, tx);
                tx.commit();
                if (inserted == 0) {
                    extracted.push(tmp);
                    break;
                }
            }
        }
        while (!extracted.isEmpty()) {
            helper.opener.drop(extracted.pop(), true);
        }
    }

    private boolean isSame(int slot1, int slot2) {
        if (helper.itemHandler == null) return false;
        return ItemStackUtil.isSame(ItemUtil.getStack(helper.itemHandler, slot1), ItemUtil.getStack(helper.itemHandler, slot2), false);
    }

    @Override
    public boolean isDoneSorting() {
        return !isSortingSlots || maxSortingChances == 0;
    }

    @Override
    public int getSlots() {
        if (helper.itemHandler == null)
            return 0;
        return helper.itemHandler.size();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        if (helper.itemHandler == null)
            return ItemStack.EMPTY;
        return ItemUtil.getStack(helper.itemHandler, slot);
    }

    @Override
    public void finish() {
        helper.stop();
    }
}
