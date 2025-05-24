package studio.fantasyit.maid_storage_manager.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public class PosUtil {
    static public @Nullable <T> T findAround(BlockPos pos, Function<BlockPos, @Nullable T> consumer) {
        T tmp;
        if ((tmp = consumer.apply(pos)) != null) return tmp;
        if ((tmp = consumer.apply(pos.north())) != null) return tmp;
        if ((tmp = consumer.apply(pos.east())) != null) return tmp;
        if ((tmp = consumer.apply(pos.south())) != null) return tmp;
        if ((tmp = consumer.apply(pos.west())) != null) return tmp;
        return null;
    }

    static public @Nullable <T> T findAroundUpAndDown(BlockPos pos, Function<BlockPos, @Nullable T> consumer) {
        return findAroundUpAndDown(pos, consumer, 4);
    }

    static public @Nullable <T> T findAroundUpAndDown(BlockPos pos, Function<BlockPos, @Nullable T> consumer, int depth) {
        T tmp;
        for (int i = 0; i < depth * 2; i++) {
            if ((tmp = consumer.apply(pos)) != null) return tmp;
            if ((tmp = consumer.apply(pos.north())) != null) return tmp;
            if ((tmp = consumer.apply(pos.east())) != null) return tmp;
            if ((tmp = consumer.apply(pos.south())) != null) return tmp;
            if ((tmp = consumer.apply(pos.west())) != null) return tmp;
            if (i % 2 != 0)
                pos = pos.above(i + 1);
            else
                pos = pos.below(i + 1);
        }
        if ((tmp = consumer.apply(pos)) != null) return tmp;
        return null;
    }

    static public <T> @NotNull List<T> gatherAroundUpAndDown(BlockPos pos, Function<BlockPos, @Nullable T> consumer) {
        return gatherAroundUpAndDown(pos, consumer, 4);
    }

    static public <T> @NotNull List<T> gatherAroundUpAndDown(BlockPos pos, Function<BlockPos, @Nullable T> consumer, int depth) {
        T tmp;
        List<T> result = new ArrayList<>();
        for (int i = 0; i < depth * 2; i++) {
            if ((tmp = consumer.apply(pos)) != null) result.add(tmp);
            if ((tmp = consumer.apply(pos.north())) != null) result.add(tmp);
            if ((tmp = consumer.apply(pos.east())) != null) result.add(tmp);
            if ((tmp = consumer.apply(pos.south())) != null) result.add(tmp);
            if ((tmp = consumer.apply(pos.west())) != null) result.add(tmp);
            if (i % 2 != 0)
                pos = pos.above(i + 1);
            else
                pos = pos.below(i + 1);
        }
        if ((tmp = consumer.apply(pos)) != null) result.add(tmp);
        return result;
    }

    static public @NotNull BlockPos getEntityPos(Level level, @NotNull BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) return pos;
        return blockEntity.getBlockPos();
    }

    static protected boolean isEmptyBlockPos(Level level, BlockPos pos) {
        return level.getBlockState(pos).isAir() || level.getBlockState(pos).getCollisionShape(
                level,
                pos,
                CollisionContext.empty()
        ).isEmpty();
    }

    static public boolean isSafePos(Level level, BlockPos pos) {
        return isEmptyBlockPos(level, pos)
                && isEmptyBlockPos(level, pos.above())
                && !isEmptyBlockPos(level, pos.below());
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
        int xOffset = touchPos.getX() > standPos.getX() ? 1 : -1;
        int zOffset = touchPos.getZ() > standPos.getZ() ? 1 : -1;
        int yOffset = touchPos.getY() > standPos.getY() ? 1 : -1;
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
