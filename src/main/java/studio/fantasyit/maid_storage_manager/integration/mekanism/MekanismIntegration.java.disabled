package studio.fantasyit.maid_storage_manager.integration.mekanism;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import mekanism.api.security.SecurityMode;
import mekanism.common.tile.base.TileEntityMekanism;

public class MekanismIntegration {
    public static boolean isAccessibleByMaid(TileEntityMekanism machine, EntityMaid maid) {
        if (machine.getSecurity().getMode() == SecurityMode.PUBLIC) return true;
        if (machine.getSecurity().getMode() == SecurityMode.TRUSTED)
            return machine.getOwnerUUID() != null && machine.getOwnerUUID().equals(maid.getOwnerUUID());
        return false;
    }
}
