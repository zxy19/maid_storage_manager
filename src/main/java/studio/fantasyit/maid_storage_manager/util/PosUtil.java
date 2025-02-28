package studio.fantasyit.maid_storage_manager.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;

public class PosUtil {
    /**
     * 当前方块开始向上四格，左右各一格
     *
     * @param pos
     * @param consumer
     * @return
     */
    static public @Nullable <T> T findAroundFromStandPos(BlockPos pos, Function<BlockPos, @Nullable T> consumer) {
        return findAroundFromStandPos(pos, consumer, 4);
    }

    static public @Nullable <T> T findAroundFromStandPos(BlockPos pos, Function<BlockPos, @Nullable T> consumer, int depth) {
        T tmp;
        for (int i = 0; i < depth; i++) {
            if ((tmp = consumer.apply(pos)) != null) return tmp;
            if ((tmp = consumer.apply(pos.north())) != null) return tmp;
            if ((tmp = consumer.apply(pos.east())) != null) return tmp;
            if ((tmp = consumer.apply(pos.south())) != null) return tmp;
            if ((tmp = consumer.apply(pos.west())) != null) return tmp;
            pos = pos.above();
        }
        if ((tmp = consumer.apply(pos)) != null) return tmp;
        return null;
    }


    static public @Nullable <T> T findAroundBeside(BlockPos pos, Function<BlockPos, @Nullable T> consumer){
        T tmp = null;
        if((tmp = consumer.apply(pos)) != null) return tmp;
        if((tmp = consumer.apply(pos.above())) != null) return tmp;
        if((tmp = consumer.apply(pos.below())) != null) return tmp;
        if((tmp = consumer.apply(pos.north())) != null) return tmp;
        if((tmp = consumer.apply(pos.east())) != null) return tmp;
        if((tmp = consumer.apply(pos.south())) != null) return tmp;
        if((tmp = consumer.apply(pos.west())) != null) return tmp;
        return null;
    }
    static public @Nullable <T> T findAroundUpAndDown(BlockPos pos, Function<BlockPos, @Nullable T> consumer) {
        return findAroundUpAndDown(pos, consumer, 4);
    }

    static public @Nullable <T> T findAroundUpAndDown(BlockPos pos, Function<BlockPos, @Nullable T> consumer, int depth) {
        @Nullable T tmp = findAroundFromStandPos(pos, consumer, depth);
        if (tmp != null) return tmp;

        tmp = findAroundFromStandPos(pos.below(depth - 1), consumer, depth);
        if (tmp != null) return tmp;

        return null;
    }

    static public @NotNull BlockPos getEntityPos(Level level, @NotNull BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) return pos;
        return blockEntity.getBlockPos();
    }

    static public boolean isSafePos(Level level, BlockPos pos) {
        return level.getBlockState(pos).isAir()
                && level.getBlockState(pos.above()).isAir()
                && !level.getBlockState(pos.below()).isAir();
    }

    static public boolean hasSightLine(Level level, BlockPos pos1, BlockPos pos2) {
        BlockHitResult result = level.clip(new ClipContext(pos1.getCenter(),
                pos2.getCenter(),
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                null));
        if (result.getType() == HitResult.Type.BLOCK) {
            return result.getBlockPos().equals(pos2);
        }
        return false;
    }

    public static boolean isBetween(int x1, int x2, int x3) {
        return Math.min(x1, x2) <= x3 && x3 <= Math.max(x1, x2);
    }

    public static boolean isBetween(BlockPos pos1, BlockPos pos2, BlockPos pos3) {
        return isBetween(pos1.getX(), pos2.getX(), pos3.getX())
                && isBetween(pos1.getY(), pos2.getY(), pos3.getY())
                && isBetween(pos1.getZ(), pos2.getZ(), pos3.getZ());
    }

    public static boolean canTouch(ServerLevel serverLevel, BlockPos standPos, BlockPos touchPos) {
        if (standPos.equals(touchPos)) return true;
        int xOffset = touchPos.getX() - standPos.getX();
        int zOffset = touchPos.getZ() - standPos.getZ();
        int yOffset = touchPos.getY() - standPos.getY();
        Set<BlockPos> vis = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();
        queue.add(standPos);
        while (!queue.isEmpty()) {
            BlockPos poll = queue.poll();

            if (poll.distManhattan(standPos) > 5) continue;

            BlockPos xPos = poll.offset(xOffset, 0, 0);
            if (xPos.equals(touchPos)) return true;
            if (!vis.contains(xPos) && isBetween(standPos, touchPos, xPos) && serverLevel.getBlockState(xPos).isAir()) {
                vis.add(xPos);
                queue.add(xPos);
            }

            BlockPos yPos = poll.offset(0, yOffset, 0);
            if (yPos.equals(touchPos)) return true;
            if (!vis.contains(yPos) && isBetween(standPos, touchPos, yPos) && serverLevel.getBlockState(yPos).isAir()) {
                vis.add(yPos);
                queue.add(yPos);
            }

            BlockPos zPos = poll.offset(0, 0, zOffset);
            if (zPos.equals(touchPos)) return true;
            if (!vis.contains(zPos) && isBetween(standPos, touchPos, zPos) && serverLevel.getBlockState(zPos).isAir()) {
                vis.add(zPos);
                queue.add(zPos);
            }
        }
        return false;
    }
}
