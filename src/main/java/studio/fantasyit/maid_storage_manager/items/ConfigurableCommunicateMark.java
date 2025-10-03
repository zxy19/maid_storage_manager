package studio.fantasyit.maid_storage_manager.items;

import com.github.tartaricacid.touhoulittlemaid.api.bauble.IMaidBauble;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.api.communicate.ICommunicatable;
import studio.fantasyit.maid_storage_manager.api.communicate.context.ICommunicateContext;

public class ConfigurableCommunicateMark implements ICommunicatable, IMaidBauble {
    @Override
    public boolean willingCommunicate(EntityMaid maid) {
        return false;
    }

    @Override
    public @Nullable ICommunicateContext startCommunicate(EntityMaid maid) {
        return null;
    }
}
