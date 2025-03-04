package studio.fantasyit.maid_storage_manager.storage.ae2;

import appeng.api.parts.IPart;
import appeng.blockentity.networking.CableBusBlockEntity;
import appeng.parts.reporting.AbstractTerminalPart;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.storage.Storage;
import studio.fantasyit.maid_storage_manager.storage.base.IMaidStorage;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageContext;

import java.util.Arrays;

public class Ae2Storage implements IMaidStorage {
    @Override
    public ResourceLocation getType() {
        return new ResourceLocation(MaidStorageManager.MODID, "ae2");
    }

    @Override
    public boolean isValidTarget(ServerLevel level, LivingEntity maid, BlockPos block, @Nullable Direction side) {
        BlockEntity blockEntity = level.getBlockEntity(block);

        if (blockEntity == null)
            return false;


        if (blockEntity instanceof CableBusBlockEntity cbb) {
            return Arrays.stream(Direction
                            .orderedByNearest(maid))
                    .anyMatch(direction -> {
                        IPart part = cbb.getCableBus().getPart(direction);
                        return part instanceof AbstractTerminalPart atp;
                    });
        }
        return false;
    }

    @Override
    public @Nullable IStorageContext onStartCollect(ServerLevel level, EntityMaid maid, Storage block) {
        return new Ae2CollectContext();
    }

    @Override
    public @Nullable IStorageContext onStartPlace(ServerLevel level, EntityMaid maid, Storage block) {
        return new Ae2PlacingContext();
    }

    @Override
    public @Nullable IStorageContext onStartView(ServerLevel level, EntityMaid maid, Storage block) {
        return new Ae2ViewContext();
    }
}
