package studio.fantasyit.maid_storage_manager.storage.base;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.storage.Target;

public interface IMaidStorage {
    ResourceLocation getType();

    boolean isValidTarget(ServerLevel level, LivingEntity maid, BlockPos block, @Nullable Direction side);

    @Nullable IStorageContext onStartCollect(ServerLevel level, EntityMaid maid, Target storage);

    @Nullable IStorageContext onStartPlace(ServerLevel level, EntityMaid maid, Target storage);

    @Nullable IStorageContext onStartView(ServerLevel level, EntityMaid maid, Target storage);

    default @Nullable IStorageContext onPreviewFilter(ServerLevel level, EntityMaid maid, Target storage) {
        return null;
    }
}