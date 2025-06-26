package studio.fantasyit.maid_storage_manager.integration.kubejs.helper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import studio.fantasyit.maid_storage_manager.storage.Target;

import java.util.Optional;

public class TargetOperator {
    public static TargetOperator INSTANCE = new TargetOperator();
    public Target makeTargetNoSide(ResourceLocation id, BlockPos pos) {
        return new Target(id, pos, Optional.empty());
    }

    public Target makeTarget(ResourceLocation id, BlockPos pos, Direction side) {
        return new Target(id, pos, Optional.of(side));
    }

    public Target makeTargetVirtualNoSide(BlockPos clickedPos) {
        return Target.virtual(clickedPos, null);
    }

    public Target makeTargetVirtual(ResourceLocation id, BlockPos clickedPos, Direction side) {
        return Target.virtual(clickedPos, side);
    }
}
