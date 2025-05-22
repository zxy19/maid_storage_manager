package studio.fantasyit.maid_storage_manager.storage.create;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
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

public class CreateStorage implements IMaidStorage {
    public static final ResourceLocation TYPE = new ResourceLocation(MaidStorageManager.MODID, "create");

    @Override
    public ResourceLocation getType() {
        return TYPE;
    }

    @Override
    public boolean isValidTarget(ServerLevel level, LivingEntity maid, BlockPos block, @Nullable Direction side) {
        return false;
    }

    @Override
    public @Nullable IStorageContext onStartCollect(ServerLevel level, EntityMaid maid, Target storage) {
        return null;
    }

    @Override
    public @Nullable IStorageContext onStartPlace(ServerLevel level, EntityMaid maid, Target storage) {
        return null;
    }

    @Override
    public @Nullable IStorageContext onStartView(ServerLevel level, EntityMaid maid, Target storage) {
        return null;
    }
}
