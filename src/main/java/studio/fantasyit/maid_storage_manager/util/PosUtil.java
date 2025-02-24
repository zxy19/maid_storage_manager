package studio.fantasyit.maid_storage_manager.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class PosUtil {
    /**
     * 当前方块开始向上四格，左右各一格
     *
     * @param pos
     * @param consumer
     * @return
     */
    static public @Nullable BlockPos findAroundFromStandPos(BlockPos pos, Function<BlockPos, Boolean> consumer) {
        return findAroundFromStandPos(pos, consumer, 4);
    }

    static public @Nullable BlockPos findAroundFromStandPos(BlockPos pos, Function<BlockPos, Boolean> consumer, int depth) {
        for (int i = 0; i < depth; i++) {
            if (consumer.apply(pos)) return pos;
            if (consumer.apply(pos.north())) return pos.north();
            if (consumer.apply(pos.east())) return pos.east();
            if (consumer.apply(pos.south())) return pos.south();
            if (consumer.apply(pos.west())) return pos.west();
            pos = pos.above();
        }
        if (consumer.apply(pos)) return pos;
        return null;
    }

    static public @Nullable BlockPos findAroundUpAndDown(BlockPos pos, Function<BlockPos, Boolean> consumer) {
        return findAroundUpAndDown(pos, consumer, 4);
    }

    static public @Nullable BlockPos findAroundUpAndDown(BlockPos pos, Function<BlockPos, Boolean> consumer, int depth) {
        @Nullable BlockPos tmp = findAroundFromStandPos(pos, consumer, depth);
        if (tmp != null) return tmp;

        tmp = findAroundFromStandPos(pos.below(depth-1), consumer, depth);
        if (tmp != null) return tmp;

        return null;
    }

    static public @NotNull BlockPos getEntityPos(Level level, @NotNull BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) return pos;
        return blockEntity.getBlockPos();
    }
}
