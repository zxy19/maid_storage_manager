package studio.fantasyit.maid_storage_manager.communicate;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.inventory.handler.BaubleItemHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.api.communicate.ICommunicatable;
import studio.fantasyit.maid_storage_manager.items.RequestListItem;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;

import java.util.Optional;
import java.util.UUID;

public class CommunicateUtil {
    public static Optional<WrappedCommunicateContextGetter> getWillingCommunicatable(EntityMaid wisher, EntityMaid handler) {
        if (wisher.getTask() instanceof ICommunicatable ic && ic.willingCommunicate(wisher, null, handler))
            return Optional.of(new WrappedCommunicateContextGetter(ic, null));
        BaubleItemHandler maidBauble = wisher.getMaidBauble();
        for (int i = 0; i < maidBauble.getSlots(); i++) {
            if (maidBauble.getStackInSlot(i).getItem() instanceof ICommunicatable ic && ic.willingCommunicate(wisher, maidBauble.getStackInSlot(i), handler))
                return Optional.of(new WrappedCommunicateContextGetter(ic, maidBauble.getStackInSlot(i)));
        }
        return Optional.empty();
    }

    public static void communicateRequestDone(EntityMaid maid, ItemStack reqList) {
        CompoundTag data = RequestListItem.getVirtualData(reqList);
        if (data == null) return;
        UUID targetUUID = data.getUUID("target");
        if (maid.level() instanceof ServerLevel level && level.getEntity(targetUUID) instanceof EntityMaid em) {
            if (!RequestListItem.isAllSuccess(reqList)) {
                communicateSetFailCooldown(maid, level);
            }
            MemoryUtil.getCommunicate(maid).getAndRemoveDelayCompleteContext().ifPresent(t -> t.complete(maid, em));
        }
    }

    public static void communicateSetFailCooldown(EntityMaid maid, ServerLevel level) {
        MemoryUtil.getCommunicate(maid).startCooldown(maid.getUUID(), level, 1200);
    }
}
