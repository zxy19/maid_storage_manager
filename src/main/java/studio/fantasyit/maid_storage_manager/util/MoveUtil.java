package studio.fantasyit.maid_storage_manager.util;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.MaidPathFindingBFS;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.maid.memory.AbstractTargetMemory;
import studio.fantasyit.maid_storage_manager.storage.MaidStorage;
import studio.fantasyit.maid_storage_manager.storage.Target;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

public class MoveUtil {
    public static boolean isValidTarget(ServerLevel level, EntityMaid maid, Target target, boolean bypassNoAccess) {
        List<Target> rewrite = StorageAccessUtil.findTargetRewrite(level, maid, target, bypassNoAccess);
        return rewrite.contains(target);
    }

    public static List<BlockPos> getAllAvailablePosForTarget(ServerLevel level, EntityMaid maid, BlockPos target, MaidPathFindingBFS pathFinding) {
        Function<BlockPos, @Nullable BlockPos> predictor = (BlockPos pos) -> {
            if (!PosUtil.isSafePos(level, pos)) return null;
            if (maid.isWithinRestriction(pos) && PosUtil.canTouch(level, pos, target) && pathFinding.canPathReach(pos)) {
                return pos;
            } else {
                return null;
            }
        };
        if (maid.blockPosition().distManhattan(target) <= 2)
            if (predictor.apply(maid.blockPosition()) != null) return List.of(maid.blockPosition());
        return PosUtil.gatherAroundUpAndDown(target, predictor);
    }

    public static @Nullable BlockPos getNearestFromTargetList(ServerLevel level, EntityMaid maid, List<BlockPos> posListToEval) {
        if (posListToEval.contains(maid.blockPosition()))
            return maid.blockPosition();
        List<Pair<BlockPos, Integer>> posList = posListToEval
                .stream()
                .map(pos -> {
                    if (Config.fastPathSchedule) return new Pair<>(pos, (int) maid.distanceToSqr(pos.getCenter()));
                    Path path = maid.getNavigation().createPath(pos, 0);
                    if (path != null && path.canReach())
                        return new Pair<>(pos, path.getNodeCount());
                    return null;
                }).filter(Objects::nonNull).toList();

        return posList.stream().min(Comparator.comparingInt(Pair::getB)).map(Pair::getA).orElse(null);
    }

    public static @Nullable BlockPos selectPosForTarget(ServerLevel level, EntityMaid maid, BlockPos target) {
        MaidPathFindingBFS pathFinding = new MaidPathFindingBFS(maid.getNavigation().getNodeEvaluator(), level, maid);
        //寻找落脚点
        @NotNull List<BlockPos> posListToEval = getAllAvailablePosForTarget(level, maid, target, pathFinding);
        pathFinding.finish();
        return getNearestFromTargetList(level, maid, posListToEval);
    }


    public static @Nullable Target findTargetForPos(ServerLevel level, EntityMaid maid, BlockPos blockPos, AbstractTargetMemory memory, boolean allowRequestOnly, Predicate<Target> validator) {
        return PosUtil.findAroundUpAndDown(blockPos, (pos) -> {
            Target validTarget = MaidStorage.getInstance().isValidTarget(level, maid, pos);
            if (validTarget == null || !PosUtil.canTouch(level, blockPos, pos)) return null;
            List<Target> list = StorageAccessUtil.findTargetRewrite(level, maid, validTarget, allowRequestOnly);
            for (Target storage : list) {
                if (memory.isVisitedPos(storage))
                    continue;
                if (!validator.test(storage))
                    continue;
                return storage;
            }
            return null;
        });
    }

    public static boolean setMovementIfColliedTarget(ServerLevel level, EntityMaid maid, Target target) {
        if (target.side == null) return setMovementIfColliedTarget(level, maid, target.pos);
        else return setMovementIfColliedTarget(level, maid, target.pos.relative(target.side));
    }

    public static boolean setMovementIfColliedTarget(ServerLevel level, EntityMaid maid, BlockPos pos) {
        if (maid.getBoundingBox().intersects(new AABB(pos))) {
            if (maid.getDeltaMovement().length() > 0.1) return false;
            Vec3 dMove = maid.getPosition(0).subtract(pos.getCenter()).normalize().scale(0.4f);
            dMove = dMove.with(Direction.Axis.Y, 0);
            maid.setDeltaMovement(dMove);
            return false;
        }
        return true;
    }
}