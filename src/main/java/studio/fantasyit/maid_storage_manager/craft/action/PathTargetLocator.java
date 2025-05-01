package studio.fantasyit.maid_storage_manager.craft.action;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftLayer;
import studio.fantasyit.maid_storage_manager.util.MoveUtil;
import studio.fantasyit.maid_storage_manager.util.PosUtil;

import java.util.*;
import java.util.stream.Stream;

public class PathTargetLocator {
    public static BlockPos commonNearestAvailablePos(EntityMaid maid, CraftGuideData craftGuideData, CraftGuideStepData craftGuideStepData, CraftLayer layer) {
        return MoveUtil.selectPosForTarget((ServerLevel) maid.level(), maid, craftGuideStepData.getStorage().getPos());
    }

    public static BlockPos exactlySidedPos(EntityMaid maid, CraftGuideData craftGuideData, CraftGuideStepData craftGuideStepData, CraftLayer layer) {
        BlockPos pos = craftGuideStepData.getStorage().getPos();
        if (craftGuideStepData.getStorage().side != null)
            pos = pos.relative(craftGuideStepData.getStorage().side, 1);
        return pos;
    }

    public static BlockPos touchPos(EntityMaid maid, CraftGuideData craftGuideData, CraftGuideStepData craftGuideStepData, CraftLayer layer) {
        List<BlockPos> posList = new ArrayList<>();
        BlockPos pos = craftGuideStepData.getStorage().getPos();
        Direction side = craftGuideStepData.getStorage().side;
        if (side != null) {
            posList = List.of(pos.relative(side, 2));
        } else {
            posList = Arrays.stream(Direction.values()).map(d -> pos.relative(d, 2)).toList();
        }

        Stream<Pair<BlockPos, Integer>> allPos = posList.stream().map(finalPos ->
                        PosUtil.gatherAroundUpAndDown(pos, (pos1) -> {
                            if (!PosUtil.isSafePos(maid.level(), pos1)) return null;
                            Vec3 eyePos = pos1.getCenter().add(0, maid.getEyeHeight() - 0.5, 0);
                            for (Direction direction : Direction.values()) {
                                if (side != null && direction != side) continue;
                                for (float f = 0.5f; f > 0.0f; f -= 0.1f) {
                                    Vec3 offseted = pos.getCenter().relative(direction, f);
                                    if (maid.isWithinRestriction(pos1) && hasLineOfSight(maid, offseted, eyePos)) {
                                        Path path = maid.getNavigation().createPath(pos1, 0);
                                        if (path != null && path.canReach())
                                            return new Pair<>(pos1, path.getNodeCount());
                                    }
                                }
                            }
                            return null;
                        }))
                .filter(s -> !s.isEmpty())
                .map(s -> s.stream().min(Comparator.comparingInt(Pair::getB)))
                .map(Optional::get);
        return allPos.min(Comparator.comparingInt(Pair::getB)).map(Pair::getA).orElse(null);
    }

    private static boolean hasLineOfSight(EntityMaid maid, Vec3 startPos, Vec3 target) {
        if (startPos.distanceTo(target) > 128.0D) {
            return false;
        } else {
            return maid.level().clip(new ClipContext(target, startPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, maid)).getType() == HitResult.Type.MISS;
        }
    }

    public static BlockPos throwItemPos(EntityMaid maid, CraftGuideData craftGuideData, CraftGuideStepData craftGuideStepData, CraftLayer layer) {
        BlockPos currentTarget = craftGuideStepData.getStorage().getPos();
        if (craftGuideStepData.getStorage().side != null)
            currentTarget = currentTarget.relative(craftGuideStepData.getStorage().side);
        ServerLevel level = (ServerLevel) maid.level();
        for (int i = 0; i < 4; i++) {
            if (validPosForThrowItem(level, maid, currentTarget)) return currentTarget;
            if (level.getBlockState(currentTarget).isCollisionShapeFullBlock(level, currentTarget)) return null;
            if (validPosForThrowItem(level, maid, currentTarget.south())) return currentTarget.south();
            if (validPosForThrowItem(level, maid, currentTarget.east())) return currentTarget.east();
            if (validPosForThrowItem(level, maid, currentTarget.west())) return currentTarget.west();
            if (validPosForThrowItem(level, maid, currentTarget.north())) return currentTarget.north();
            currentTarget = currentTarget.above();
        }
        return null;
    }

    private static boolean validPosForThrowItem(ServerLevel level, EntityMaid maid, BlockPos pos) {
        if (PosUtil.isSafePos(level, pos) && maid.isWithinRestriction(pos)) {
            Path path = maid.getNavigation().createPath(pos, 0);
            if (path != null && path.canReach())
                return true;
        }
        return false;
    }

    public static BlockPos besidePosOrExactlyPos(EntityMaid maid, CraftGuideData craftGuideData, CraftGuideStepData craftGuideStepData, CraftLayer craftLayer) {
        BlockPos center = craftGuideStepData.getStorage().getPos();
        if (craftGuideStepData.getStorage().side != null)
            center = center.relative(craftGuideStepData.getStorage().side, 1);

        return PosUtil.findAround(center, (pos) -> {
            if (PosUtil.isSafePos(maid.level(), pos) && maid.isWithinRestriction(pos) && maid.canPathReach(pos)) {
                return pos;
            } else {
                return null;
            }
        });
    }
}
