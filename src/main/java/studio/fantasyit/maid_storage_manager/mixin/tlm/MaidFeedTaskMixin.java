package studio.fantasyit.maid_storage_manager.mixin.tlm;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskFeedAnimal;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import studio.fantasyit.maid_storage_manager.api.communicate.ICommunicatable;
import studio.fantasyit.maid_storage_manager.api.communicate.context.ICommunicateContext;
import studio.fantasyit.maid_storage_manager.api.communicate.context.SimpleItemRequestContext;
import studio.fantasyit.maid_storage_manager.util.InvUtil;

import java.util.List;

@Mixin(TaskFeedAnimal.class)
public class MaidFeedTaskMixin implements ICommunicatable {
    @Override
    public boolean willingCommunicate(EntityMaid maid) {
        return InvUtil.getTargetIndex(maid, Items.CARROT.getDefaultInstance(), false) == -1;
    }

    @Override
    public ICommunicateContext startCommunicate(EntityMaid maid) {
        return new SimpleItemRequestContext(List.of(Items.CARROT.getDefaultInstance().copyWithCount(64)));
    }
}
