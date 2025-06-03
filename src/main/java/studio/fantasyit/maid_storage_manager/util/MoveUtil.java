package studio.fantasyit.maid_storage_manager.util;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.MaidPathFindingBFS;
import com.github.tartaricacid.touhoulittlemaid.inventory.handler.BaubleItemHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.advancement.AdvancementTypes;
import studio.fantasyit.maid_storage_manager.items.StorageDefineBauble;
import studio.fantasyit.maid_storage_manager.maid.memory.AbstractTargetMemory;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;
import studio.fantasyit.maid_storage_manager.storage.MaidStorage;
import studio.fantasyit.maid_storage_manager.storage.Target;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class MoveUtil {
    public static boolean isValidTarget(ServerLevel level, EntityMaid maid, Target target, boolean bypassNoAccess) {
        List<Target> rewrite = findTargetRewrite(level, maid, target, bypassNoAccess);
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

    public static @Nullable Target findTargetForPos(ServerLevel level, EntityMaid maid, BlockPos blockPos, AbstractTargetMemory memory) {
        return findTargetForPos(level, maid, blockPos, memory, false);
    }

    public static @Nullable Target findTargetForPos(ServerLevel level, EntityMaid maid, BlockPos blockPos, AbstractTargetMemory memory, boolean allowRequestOnly) {
        return PosUtil.findAroundUpAndDown(blockPos, (pos) -> {
            Target validTarget = MaidStorage.getInstance().isValidTarget(level, maid, pos);
            if (validTarget == null || !PosUtil.canTouch(level, blockPos, pos)) return null;
            List<Target> list = findTargetRewrite(level, maid, validTarget, allowRequestOnly);
            for (Target storage : list) {
                if (memory.isVisitedPos(storage))
                    continue;
                return storage;
            }
            return null;
        });
    }

    public static TagKey<Block> allowTag = TagKey.create(ForgeRegistries.BLOCKS.getRegistryKey(), new ResourceLocation(MaidStorageManager.MODID, "default_storage_blocks"));

    public static List<Target> findTargetRewrite(ServerLevel level, EntityMaid maid, Target target, boolean bypassNoAccess) {
        BaubleItemHandler maidBauble = maid.getMaidBauble();
        List<ItemStack> itemStack = new ArrayList<>();
        for (int i = 0; i < maidBauble.getSlots(); i++) {
            if (maidBauble.getStackInSlot(i).is(ItemRegistry.STORAGE_DEFINE_BAUBLE.get())) {
                itemStack.add(maidBauble.getStackInSlot(i));
            }
        }
        if (maid.getMainHandItem().is(ItemRegistry.REQUEST_LIST_ITEM.get())) {
            CompoundTag tag = maid.getMainHandItem().getOrCreateTag();
            ItemStack stack = ItemStack.of(tag.getCompound(StorageDefineBauble.TAG_STORAGE_DEFINE));
            if (!stack.isEmpty()) {
                itemStack.add(stack);
            }
        }
        List<Target> result = markedTargetOf(level, maid, target, bypassNoAccess);
        if (result != null) {
            if (Config.useAllStorageByDefault || level.getBlockState(target.getPos()).is(allowTag)) {
                result.add(target);
            }
        } else {
            result = new ArrayList<>();
        }
        if (itemStack.isEmpty()) return result;
        for (ItemStack stack : itemStack) {
            StorageDefineBauble.Mode mode = StorageDefineBauble.getMode(stack);
            List<Target> storages = StorageDefineBauble.getStorages(stack);
            List<Target> list = storages.stream().filter(storage -> storage.getPos().equals(target.getPos())).toList();
            if (mode == StorageDefineBauble.Mode.REPLACE) {
                result.clear();
                result.addAll(list);
            } else if (mode == StorageDefineBauble.Mode.APPEND) {
                result.addAll(list);
            } else if (mode == StorageDefineBauble.Mode.REMOVE) {
                result.removeAll(list);
            } else if (mode == StorageDefineBauble.Mode.REPLACE_SPEC) {
                if (!list.isEmpty()) {
                    result.clear();
                    result.addAll(list);
                }
            }
        }
        return result;
    }

    private static List<Target> markedTargetOf(ServerLevel level, EntityMaid maid, Target target, boolean bypassNoAccess) {
        List<Pair<Target, ItemStack>> marks = InvUtil.getMarksWithSameContainer(level, target);
        List<Target> list = new ArrayList<>();
        boolean hasAllowAccess = false;
        boolean hasNoAccess = false;
        for (Pair<Target, ItemStack> ti : marks) {
            if (ti.getB().is(ItemRegistry.ALLOW_ACCESS.get())) {
                list.add(ti.getA());
                hasAllowAccess = true;
            }
        }
        for (Pair<Target, ItemStack> ti : marks) {
            if (ti.getB().is(ItemRegistry.NO_ACCESS.get())) {
                hasNoAccess = true;
                if (!bypassNoAccess)
                    list = null;
            }
        }

        if (hasAllowAccess && hasNoAccess) {
            AdvancementTypes.triggerForMaid(maid, AdvancementTypes.LEFT_RIGHT_BRAINS_FIGHT);
        }

        return list;
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