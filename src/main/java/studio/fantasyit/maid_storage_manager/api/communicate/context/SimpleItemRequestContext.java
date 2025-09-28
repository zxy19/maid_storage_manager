package studio.fantasyit.maid_storage_manager.api.communicate.context;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.items.RequestListItem;
import studio.fantasyit.maid_storage_manager.util.InvUtil;
import studio.fantasyit.maid_storage_manager.util.RequestItemUtil;

import java.util.List;

public class SimpleItemRequestContext implements ICommunicateContext {
    private final List<ItemStack> toRequestItems;
    private boolean isFinished;

    public SimpleItemRequestContext(List<ItemStack> toRequestItems) {
        this.toRequestItems = toRequestItems;
        isFinished = true;
    }

    @Override
    public void start(EntityMaid maid, EntityMaid toCommunicate) {
        List<ItemStack> toRequestItems = this.toRequestItems;
        if (toRequestItems.size() > 10)
            toRequestItems = toRequestItems.subList(0, 10);
        else
            isFinished = false;
        ItemStack vreq = RequestItemUtil.makeVirtualItemStack(toRequestItems, null, maid, "COMMUNICATE");
        CompoundTag tag = new CompoundTag();
        tag.putUUID("target", maid.getUUID());
        RequestListItem.setVirtualData(vreq, tag);
        InvUtil.tryPlace(toCommunicate.getAvailableInv(true), vreq);
    }

    @Override
    public boolean isFinished(EntityMaid maid, EntityMaid toCommunicate) {
        return isFinished;
    }
}
