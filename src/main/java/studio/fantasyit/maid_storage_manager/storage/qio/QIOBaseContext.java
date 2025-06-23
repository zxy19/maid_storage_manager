package studio.fantasyit.maid_storage_manager.storage.qio;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.tile.qio.TileEntityQIODashboard;
import net.minecraft.server.level.ServerLevel;
import studio.fantasyit.maid_storage_manager.integration.mekanism.MekanismIntegration;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.storage.base.AbstractFilterableBlockStorage;

public abstract class QIOBaseContext extends AbstractFilterableBlockStorage {
    QIOFrequency frequency;

    @Override
    public void start(EntityMaid maid, ServerLevel level, Target target) {
        super.start(maid, level, target);
        if (level.getBlockEntity(target.getPos()) instanceof TileEntityQIODashboard tile) {
            if (MekanismIntegration.isAccessibleByMaid(tile, maid))
                frequency = tile.getFrequency();
        }
    }
}
