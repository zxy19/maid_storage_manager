package studio.fantasyit.maid_storage_manager.craft.action;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.MaidPathFindingBFS;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftLayer;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.util.MoveUtil;
import studio.fantasyit.maid_storage_manager.util.PosUtil;

import java.util.*;
import java.util.stream.Stream;

public class PathTargetLocator {
    public static BlockPos commonNearestAvailablePos(EntityMaid maid, CraftGuideData craftGuideData, CraftGuideStepData craftGuideStepData, CraftLayer layer, MaidPathFindingBFS pathFinding) {
        return MoveUtil.selectPosForTarget((ServerLevel) maid.level(), maid, craftGuideStepData.getStorage().getPos());
    }

    public static BlockPos exactlySidedPos(EntityMaid maid, CraftGuideData craftGuideData, CraftGuideStepData craftGuideStepData, CraftLayer layer, MaidPathFindingBFS pathFinding) {
        BlockPos pos = craftGuideStepData.getStorage().getPos();
        if (craftGuideStepData.getStorage().side != null)
            pos = pos.relative(craftGuideStepData.getStorage().side, 1);
        return pos;
    }

    public static BlockPos touchPos(EntityMaid maid, CraftGuideData craftGuideData, CraftGuideStepData craftGuideStepData, CraftLayer layer, MaidPathFindingBFS pathFinding) {
        List<BlockPos> posList = new ArrayList<>();
        Target target = craftGuideStepData.getStorage();
        BlockPos pos = target.getPos();
        Direction side = target.side;
        if (side != null) {
            posList = List.of(pos.relative(side, 2));
        } else {
            posList = Arrays.stream(Direction.values()).map(d -> pos.relative(d, 2)).toList();
        }
        List<List<BlockPos>> allPosTmp = posList.stream().map(finalPos ->
                        PosUtil.gatherAroundUpAndDown(finalPos, (pos1) -> {
                            if (PosUtil.isBetween(pos, finalPos, pos1)) return null;
                            if (!PosUtil.isSafePos(maid.level(), pos1)) return null;
                            if (maid.isWithinRestriction(pos1) && pathFinding.canPathReach(pos1) && canTouchBlock(maid, pos1, target)) {
                                return pos1;
                            }
                            return null;
                        }))
                .filter(s -> !s.isEmpty())
                .toList();
        Stream<Pair<BlockPos, Integer>> allPos = allPosTmp
                .stream()
                .map(s -> s.stream().map(pos1 -> {
                    Path path = maid.getNavigation().createPath(pos1, 0);
                    if (path != null && path.canReach())
                        return new Pair<>(pos1, path.getNodeCount());
                    return null;
                }).filter(Objects::nonNull).toList())
                .filter(s -> !s.isEmpty())
                .map(s -> s.stream().min(Comparator.comparingInt(Pair::getB)))
                .map(Optional::get);
        return allPos.min(Comparator.comparingInt(Pair::getB)).map(Pair::getA).orElse(null);
    }

    private static boolean canTouchBlock(EntityMaid maid, BlockPos standPos, Target target) {
        Vec3 eyePos = standPos.getCenter().add(0, maid.getEyeHeight() - 0.5, 0);
        for (Direction direction : Direction.values()) {
            if (target.side != null && direction != target.side) continue;
            for (float f = 0.5f; f > 0.0f; f -= 0.1f) {
                Vec3 offseted = target.pos.getCenter().relative(direction, f);
                BlockHitResult clip = maid.level().clip(new ClipContext(eyePos, offseted, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, maid));
                if ((clip.getType() == HitResult.Type.BLOCK && clip.getBlockPos().equals(target.pos)) || clip.getType() == HitResult.Type.MISS)
                    if (target.side == null || target.side == clip.getDirection())
                        return true;
            }
        }
        return false;
    }

    public static BlockPos throwItemPos(EntityMaid maid, CraftGuideData craftGuideData, CraftGuideStepData craftGuideStepData, CraftLayer layer, MaidPathFindingBFS pathFinding) {
        BlockPos currentTarget = craftGuideStepData.getStorage().getPos();
        if (craftGuideStepData.getStorage().side != null)
            currentTarget = currentTarget.relative(craftGuideStepData.getStorage().side);
        ServerLevel level = (ServerLevel) maid.level();
        for (int i = 0; i < 4; i++) {
            if (validPosForThrowItem(level, maid, currentTarget, pathFinding)) return currentTarget;
            if (level.getBlockState(currentTarget).isCollisionShapeFullBlock(level, currentTarget)) return null;
            if (validPosForThrowItem(level, maid, currentTarget.south(), pathFinding)) return currentTarget.south();
            if (validPosForThrowItem(level, maid, currentTarget.east(), pathFinding)) return currentTarget.east();
            if (validPosForThrowItem(level, maid, currentTarget.west(), pathFinding)) return currentTarget.west();
            if (validPosForThrowItem(level, maid, currentTarget.north(), pathFinding)) return currentTarget.north();
            currentTarget = currentTarget.above();
        }
        return null;
    }

    private static boolean validPosForThrowItem(ServerLevel level, EntityMaid maid, BlockPos pos, MaidPathFindingBFS pathFinding) {
        if (PosUtil.isSafePos(level, pos) && maid.isWithinRestriction(pos)) {
            return pathFinding.canPathReach(pos);
        }
        return false;
    }

    public static BlockPos besidePosOrExactlyPos(EntityMaid maid, CraftGuideData craftGuideData, CraftGuideStepData craftGuideStepData, CraftLayer craftLayer, MaidPathFindingBFS pathFinding) {
        BlockPos center = craftGuideStepData.getStorage().getPos();
        if (craftGuideStepData.getStorage().side != null)
            center = center.relative(craftGuideStepData.getStorage().side, 1);

        return PosUtil.findAround(center, (pos) -> {
            if (PosUtil.isSafePos(maid.level(), pos) && maid.isWithinRestriction(pos) && pathFinding.canPathReach(pos)) {
                return pos;
            } else {
                return null;
            }
        });
    }

    public static BlockPos nearByNoLimitation(EntityMaid maid, CraftGuideData craftGuideData, CraftGuideStepData craftGuideStepData, CraftLayer craftLayer, MaidPathFindingBFS pathFinding) {
        ServerLevel level = (ServerLevel) maid.level();
        BlockPos target1 = craftGuideStepData.getStorage().getPos();
        if (craftGuideStepData.getStorage().side != null)
            target1 = target1.relative(craftGuideStepData.getStorage().side, 1);
        BlockPos target = target1;
        //寻找落脚点
        @NotNull List<BlockPos> posListToEval = PosUtil.gatherAroundUpAndDown(target,
                pos -> {
                    if (!PosUtil.isSafePos(level, pos)) return null;
                    if (maid.isWithinRestriction(pos) && PosUtil.canTouch(level, pos, target) && pathFinding.canPathReach(pos)) {
                        return pos;
                    } else {
                        return null;
                    }
                });
        ;
        pathFinding.finish();
        return MoveUtil.getNearestFromTargetList(level, maid, posListToEval);
    }
}
