package studio.fantasyit.maid_storage_manager.storage.ItemHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractChestBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import studio.fantasyit.maid_storage_manager.storage.base.IMultiBlockProcessor;

import java.util.function.Consumer;

public class ChestMultiBlockProcessor implements IMultiBlockProcessor {
    @Override
    public boolean isValid(Level level, BlockPos pos, BlockState blockState) {
        return blockState.getBlock() instanceof AbstractChestBlock<?>;
    }

    @Override
    public void process(Level level, BlockPos pos, Consumer<BlockPos> processor) {
        ChestType value = level.getBlockState(pos).getValue(ChestBlock.TYPE);
        if (value == ChestType.SINGLE)
            return;
        ChestType opposite = value.getOpposite();
        Direction dir = level.getBlockState(pos).getValue(ChestBlock.FACING);
        if(opposite == ChestType.RIGHT)
            dir = dir.getClockWise();
        else
            dir = dir.getCounterClockWise();
        if(level.getBlockState(pos.relative(dir)).getBlock() instanceof AbstractChestBlock<?>)
            processor.accept(pos.relative(dir));
    }
}
