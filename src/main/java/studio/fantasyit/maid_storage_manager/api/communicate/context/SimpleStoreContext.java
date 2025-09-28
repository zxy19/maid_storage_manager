package studio.fantasyit.maid_storage_manager.api.communicate.context;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import studio.fantasyit.maid_storage_manager.entity.VirtualItemEntity;
import studio.fantasyit.maid_storage_manager.util.InvUtil;
import studio.fantasyit.maid_storage_manager.util.MathUtil;

import java.util.function.Function;

public class SimpleStoreContext implements IMultiTickContext {
    int idx = 0;
    private VirtualItemEntity toPickItem;

    public boolean tickStoreItem(EntityMaid maid, Function<ItemStack, ItemStack> store) {
        CombinedInvWrapper availableInv = maid.getAvailableInv(false);
        if (idx >= availableInv.getSlots())
            return true;
        ItemStack stackInSlot = availableInv.getStackInSlot(idx);
        if (stackInSlot.isEmpty()) {
            idx++;
            return false;
        }
        ItemStack itemStack = availableInv.extractItem(idx, stackInSlot.getCount(), false);
        itemStack = store.apply(itemStack);
        availableInv.insertItem(idx, itemStack, false);
        if (itemStack.isEmpty())
            idx++;
        return false;
    }

    @Override
    public boolean isFinished(EntityMaid maid, EntityMaid toCommunicate) {
        return idx >= maid.getAvailableInv(false).getSlots();
    }

    @Override
    public void start(EntityMaid maid, EntityMaid toCommunicate) {
        idx = 0;
    }

    @Override
    public boolean tick(EntityMaid maid, EntityMaid toCommunicate) {
        if (toPickItem != null) {
            InvUtil.pickUpVirtual(toCommunicate, toPickItem);
            toPickItem = null;
            return false;
        } else if (!InvUtil.hasAnyFree(toCommunicate.getAvailableInv(false)))
            return true;
        else return tickStoreItem(maid, t -> this.store(t, maid, toCommunicate));
    }

    private ItemStack store(ItemStack itemStack, EntityMaid maid, EntityMaid toCommunicate) {
        int i = InvUtil.maxCanPlace(toCommunicate.getAvailableInv(false), itemStack);
        ItemStack toThrow = itemStack.copyWithCount(Math.min(itemStack.getCount(), i));
        toPickItem = InvUtil.throwItemVirtual(toCommunicate, toThrow, MathUtil.getFromToWithFriction(maid, toCommunicate.position()));
        return itemStack.copyWithCount(itemStack.getCount() - toThrow.getCount());
    }

    @Override
    public void stop(EntityMaid maid, EntityMaid toCommunicate) {
    }
}
