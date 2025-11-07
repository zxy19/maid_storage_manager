package studio.fantasyit.maid_storage_manager.storage.base;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Consumer;

public interface IMultiBlockProcessor {
    boolean isValid(Level level, BlockPos pos, BlockState blockState);
    void process(Level level, BlockPos pos, Consumer<BlockPos> processor);
}
