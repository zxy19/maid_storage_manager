package studio.fantasyit.maid_storage_manager.mixin.tlm;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskNormalFarm;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import org.spongepowered.asm.mixin.Mixin;
import studio.fantasyit.maid_storage_manager.api.communicate.ICommunicatable;
import studio.fantasyit.maid_storage_manager.api.communicate.context.ICommunicateContext;
import studio.fantasyit.maid_storage_manager.api.communicate.context.SimpleStoreContext;
import studio.fantasyit.maid_storage_manager.util.InvUtil;

@Mixin(TaskNormalFarm.class)
public class MaidFarmTaskMixin implements ICommunicatable {
    @Override
    public boolean willingCommunicate(EntityMaid maid) {
        CombinedInvWrapper availableInv = maid.getAvailableInv(false);
        return InvUtil.freeSlots(availableInv) <= availableInv.getSlots() - 2;
    }

    @Override
    public ICommunicateContext startCommunicate(EntityMaid maid) {
        CombinedInvWrapper availableInv = maid.getAvailableInv(false);
        if (InvUtil.freeSlots(availableInv) <= availableInv.getSlots() - 2) {
            return new SimpleStoreContext();
        }
        return null;
    }
}
