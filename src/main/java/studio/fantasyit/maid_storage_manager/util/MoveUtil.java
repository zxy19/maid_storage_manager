package studio.fantasyit.maid_storage_manager.util;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.inventory.handler.BaubleItemHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.items.StorageDefineBauble;
import studio.fantasyit.maid_storage_manager.maid.memory.AbstractTargetMemory;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;
import studio.fantasyit.maid_storage_manager.storage.MaidStorage;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.storage.base.IFilterable;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageContext;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MoveUtil {
    public static boolean isValidTarget(ServerLevel level, EntityMaid maid, Target target) {
        List<Target> rewrite = findTargetRewrite(level, maid, target);
        return rewrite.contains(target);
    }

    public static @Nullable BlockPos selectPosForTarget(ServerLevel level, EntityMaid maid, BlockPos target) {
        //寻找落脚点
        @NotNull List<Pair<BlockPos, Integer>> posList = PosUtil.gatherAroundUpAndDown(target,
                pos -> {
                    if (!PosUtil.isSafePos(level, pos)) return null;
                    if (maid.isWithinRestriction(pos) && PosUtil.canTouch(level, pos, target)) {
                        Path path = maid.getNavigation().createPath(pos, 0);
                        if (path != null && path.canReach())
                            return new Pair<>(pos, path.getNodeCount());
                        return null;
                    } else {
                        return null;
                    }
                });
        return posList.stream().min(Comparator.comparingInt(Pair::getB)).map(Pair::getA).orElse(null);
    }

    public static @Nullable Target findTargetForPos(ServerLevel level, EntityMaid maid, BlockPos blockPos, AbstractTargetMemory memory) {
        return findTargetForPos(level, maid, blockPos, memory, false);
    }

    public static @Nullable Target findTargetForPos(ServerLevel level, EntityMaid maid, BlockPos blockPos, AbstractTargetMemory memory, boolean allowRequestOnly) {
        return PosUtil.findAroundUpAndDown(blockPos, (pos) -> {
            Target validTarget = MaidStorage.getInstance().isValidTarget(level, maid, pos);
            if (validTarget == null || !PosUtil.canTouch(level, blockPos, pos)) return null;
            List<Target> list = findTargetRewrite(level, maid, validTarget);
            for (Target storage : list) {
                if (memory.isVisitedPos(storage))
                    continue;
                if (!allowRequestOnly) {
                    IStorageContext iStorageContext = MaidStorage
                            .getInstance()
                            .getStorage(storage.getType())
                            .onPreviewFilter(level, maid, storage);
                    if (iStorageContext instanceof IFilterable ift) {
                        iStorageContext.start(maid, level, storage);
                        if (ift.isRequestOnly())
                            continue;
                    }
                }
                return storage;
            }
            return null;
        });
    }

    public static TagKey<Block> allowTag = TagKey.create(ForgeRegistries.BLOCKS.getRegistryKey(), new ResourceLocation(MaidStorageManager.MODID, "default_storage_blocks"));

    public static List<Target> findTargetRewrite(ServerLevel level, EntityMaid maid, Target target) {
        BaubleItemHandler maidBauble = maid.getMaidBauble();
        List<ItemStack> itemStack = new ArrayList<>();
        for (int i = 0; i < maidBauble.getSlots(); i++) {
            if (maidBauble.getStackInSlot(i).is(ItemRegistry.STORAGE_DEFINE_BAUBLE.get())) {
                itemStack.add(maidBauble.getStackInSlot(i));
                break;
            }
        }
        if (maid.getMainHandItem().is(ItemRegistry.REQUEST_LIST_ITEM.get())) {
            CompoundTag tag = maid.getMainHandItem().getOrCreateTag();
            ItemStack stack = ItemStack.of(tag.getCompound(StorageDefineBauble.TAG_STORAGE_DEFINE));
            if (!stack.isEmpty()) {
                itemStack.add(stack);
            }
        }
        List<Target> result = new ArrayList<>();
        if (Config.useAllStorageByDefault || level.getBlockState(target.getPos()).is(allowTag)) {
            result.add(target);
        } else {
            result.addAll(markedTargetOf(level, target));
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

    private static List<Target> markedTargetOf(ServerLevel level, Target target) {
        List<Target> list = new ArrayList<>();
        AABB aabb = AABB.ofSize(target.pos.getCenter(), 5, 5, 5);
        List<ItemFrame> frames = level.getEntities(
                EntityTypeTest.forClass(ItemFrame.class),
                aabb,
                itemFrame -> true
        );
        for (ItemFrame frame : frames) {
            if (frame.getItem() != null && frame.getItem().is(ItemRegistry.ALLOW_ACCESS.get())) {
                BlockPos relative = frame.blockPosition().relative(frame.getDirection(), -1);
                list.add(target.sameType(relative, frame.getDirection()));
            }
        }
        return list;
    }
}
