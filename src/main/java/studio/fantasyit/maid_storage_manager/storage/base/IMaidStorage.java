package studio.fantasyit.maid_storage_manager.storage.base;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

public interface IMaidStorage {
    ResourceLocation getType();

    boolean isValidTarget(ServerLevel level, EntityMaid maid, BlockPos block);

    @Nullable IStorageContext onStartCollect(ServerLevel level, EntityMaid maid, BlockPos block);

    @Nullable IStorageContext onStartPlace(ServerLevel level, EntityMaid maid, BlockPos block);

    @Nullable IStorageContext onStartView(ServerLevel level, EntityMaid maid, BlockPos block);

    default @Nullable IStorageContext onPreviewFilter(ServerLevel level, EntityMaid maid, BlockPos block) {
        return null;
    }
}