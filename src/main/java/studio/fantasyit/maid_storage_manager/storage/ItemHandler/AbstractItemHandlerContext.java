package studio.fantasyit.maid_storage_manager.storage.ItemHandler;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.storage.base.AbstractFilterableBlockStorage;
import studio.fantasyit.maid_storage_manager.storage.base.ISlotBasedStorage;
import studio.fantasyit.maid_storage_manager.storage.base.ISortSlotContext;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;

import java.util.Stack;

public class AbstractItemHandlerContext extends AbstractFilterableBlockStorage implements ISortSlotContext, ISlotBasedStorage {

    boolean isSortingSlots = false;
    int sortingSlot = -1;
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
        sortingSlot = helper.itemHandler.getSlots() - 1;
    }

    @Override
    public void tickSorting() {
        if (helper.itemHandler == null || !isSortingSlots || sortingSlot <= 0) {
            isSortingSlots = false;
            return;
        }
        int cstT = Math.max(3000 / helper.itemHandler.getSlots(), 1);
        while (sortingSlot >= 0 && cstT > 0) {
            if (helper.itemHandler.getStackInSlot(sortingSlot).isEmpty()) {
                sortingSlot--;
                continue;
            }
            cstT--;
            int cur = sortingSlot - 1;
            int targetP = -1;
            int lastSuccess = -1;
            //第一步，向前寻找从当前位置开始连续的物品序列
            while (cur >= 0 && isSame(cur, sortingSlot))
                cur--;
            //如果第一步直接找到了容器开头，那么说明当前物品是容器唯一物品，可以直接结束
            if (cur < 0) {
                sortingSlot = -1;
                isSortingSlots = false;
                continue;
            }
            //第二步，寻找可能可以拼接在其后的物品序列位置。
            while (cur >= 0) {
                if (isSame(cur, sortingSlot)) {
                    //如果当前位置的下一个位置没用判断成功
                    if (lastSuccess != cur + 1)
                        targetP = cur;
                    lastSuccess = cur;
                }
                cur--;
            }
            //找到了可以插入的位置？
            if (targetP != -1) {
                int targetIndex = targetP + 1;
                swap(targetIndex, sortingSlot);
                sortingSlot = helper.itemHandler.getSlots() - 1;
            } else {
                //否则，当前位置正常，向前进行交换
                sortingSlot--;
            }
        }
    }

    private void swap(int slot1, int slot2) {
        if (slot2 == slot1 || helper.itemHandler == null) return;
        Stack<ItemStack> extracted = new Stack<>();
        while (helper.itemHandler.getStackInSlot(slot1).getCount() > 0) {
            ItemStack itemStack = helper.itemHandler.extractItem(slot1, helper.itemHandler.getStackInSlot(slot1).getCount(), false);
            if (itemStack.isEmpty()) break;
            extracted.push(itemStack);
        }
        while (helper.itemHandler.getStackInSlot(slot2).getCount() > 0) {
            ItemStack itemStack = helper.itemHandler.extractItem(slot2, helper.itemHandler.getStackInSlot(slot2).getCount(), true);
            if (itemStack.isEmpty()) break;
            @NotNull ItemStack rest = helper.itemHandler.insertItem(slot1, itemStack, true);
            ItemStack toInsert = itemStack.copy();
            toInsert.shrink(rest.getCount());
            if (toInsert.isEmpty()) break;
            helper.itemHandler.insertItem(slot1, helper.itemHandler.extractItem(slot2, toInsert.getCount(), false), false);
        }
        while (!extracted.isEmpty()) {
            ItemStack tmp = extracted.pop();
            int count1 = tmp.getCount();
            ItemStack itemStack = helper.itemHandler.insertItem(slot2, tmp, false);
            if (itemStack.getCount() == count1) {
                break;
            }
        }
        while (!extracted.isEmpty()) {
            helper.opener.drop(extracted.pop(), true);
        }
    }

    private boolean isSame(int slot1, int slot2) {
        if (helper.itemHandler == null) return false;
        return ItemStackUtil.isSame(helper.itemHandler.getStackInSlot(slot1), helper.itemHandler.getStackInSlot(slot2), false);
    }

    @Override
    public boolean isDoneSorting() {
        return !isSortingSlots;
    }

    @Override
    public int getSlots() {
        if (helper.itemHandler == null)
            return 0;
        return helper.itemHandler.getSlots();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        if (helper.itemHandler == null)
            return ItemStack.EMPTY;
        return helper.itemHandler.getStackInSlot(slot);
    }

    @Override
    public void finish() {
        helper.stop();
    }
}
