package studio.fantasyit.maid_storage_manager.storage.qio;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import mekanism.common.tile.qio.TileEntityQIODashboard;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.storage.base.IMaidStorage;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageContext;

public class QIOStorage implements IMaidStorage {
    public static final ResourceLocation TYPE = new ResourceLocation(MaidStorageManager.MODID, "qio");

    @Override
    public ResourceLocation getType() {
        return TYPE;
    }

    @Override
    public boolean isValidTarget(ServerLevel level, LivingEntity maid, BlockPos block, @Nullable Direction side) {
        return level.getBlockEntity(block) instanceof TileEntityQIODashboard;
    }

    @Override
    public @Nullable IStorageContext onStartCollect(ServerLevel level, EntityMaid maid, Target storage) {
        return new QIOCollectContext();
    }

    @Override
    public @Nullable IStorageContext onStartPlace(ServerLevel level, EntityMaid maid, Target storage) {
        return new QIOInsertContext();
    }

    @Override
    public @Nullable IStorageContext onStartView(ServerLevel level, EntityMaid maid, Target storage) {
        return new QIOViewContext();
    }
}
