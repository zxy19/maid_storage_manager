package studio.fantasyit.maid_storage_manager.integration.sophisticated_storage;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.p3pp3rf1y.sophisticatedstorage.block.ChestBlock;
import net.p3pp3rf1y.sophisticatedstorage.block.ChestBlockEntity;
import studio.fantasyit.maid_storage_manager.storage.base.IMultiBlockProcessor;

import java.util.function.Consumer;

public class SophisticatedStorageMultiBlock implements IMultiBlockProcessor {
    @Override
    public boolean isValid(Level level, BlockPos pos, BlockState blockState) {
        return level.getBlockEntity(pos) instanceof ChestBlockEntity;
    }

    @Override
    public void process(Level level, BlockPos pos, Consumer<BlockPos> processor) {
        BlockEntity be = level.getBlockEntity(pos);
        BlockState state = level.getBlockState(pos);
        if (be instanceof ChestBlockEntity) {
            ChestType chestType = state.getValue(ChestBlock.TYPE);
            if (chestType != ChestType.SINGLE) {
                Direction facing = state.getValue(ChestBlock.FACING);
                BlockPos neighborPos = chestType == ChestType.RIGHT ? pos.relative(facing.getCounterClockWise()) : pos.relative(facing.getClockWise());
                if (level.getBlockEntity(neighborPos) instanceof ChestBlockEntity)
                    processor.accept(neighborPos);
            }
        }
    }
}
