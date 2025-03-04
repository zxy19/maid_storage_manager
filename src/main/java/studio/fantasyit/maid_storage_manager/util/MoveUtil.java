package studio.fantasyit.maid_storage_manager.util;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.inventory.handler.BaubleItemHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.items.StorageDefineBauble;
import studio.fantasyit.maid_storage_manager.maid.memory.AbstractTargetMemory;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;
import studio.fantasyit.maid_storage_manager.storage.MaidStorage;
import studio.fantasyit.maid_storage_manager.storage.Storage;
import studio.fantasyit.maid_storage_manager.storage.base.IFilterable;
import studio.fantasyit.maid_storage_manager.storage.base.IStorageContext;

import java.util.ArrayList;
import java.util.List;

public class MoveUtil {
    public static @Nullable BlockPos selectPosForTarget(ServerLevel level, EntityMaid maid, BlockPos target) {
        //寻找落脚点
        return PosUtil.findAroundUpAndDown(target,
                pos -> {
                    if (!PosUtil.isSafePos(level, pos)) return null;

                    if (maid.isWithinRestriction(pos) && maid.canPathReach(pos) && PosUtil.canTouch(level, pos, target)) {
                        return pos;
                    } else {
                        return null;
                    }
                });
    }

    public static @Nullable Storage findTargetForPos(ServerLevel level, EntityMaid maid, BlockPos blockPos, AbstractTargetMemory memory) {
        return findTargetForPos(level, maid, blockPos, memory, false);
    }

    public static @Nullable Storage findTargetForPos(ServerLevel level, EntityMaid maid, BlockPos blockPos, AbstractTargetMemory memory, boolean allowRequestOnly) {
        return PosUtil.findAroundUpAndDown(blockPos, (pos) -> {
            Storage validTarget = MaidStorage.getInstance().isValidTarget(level, maid, pos);
            if (validTarget == null || !PosUtil.canTouch(level, blockPos, pos)) return null;
            List<Storage> list = findTargetRewriteByBauble(level, maid, validTarget);
            for (Storage storage : list) {
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

    public static List<Storage> findTargetRewriteByBauble(ServerLevel level, EntityMaid maid, Storage target) {
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
        if (itemStack.isEmpty()) return List.of(target);
        List<Storage> result = new ArrayList<>();
        result.add(target);
        for (ItemStack stack : itemStack) {
            StorageDefineBauble.Mode mode = StorageDefineBauble.getMode(stack);
            List<Storage> storages = StorageDefineBauble.getStorages(stack);
            List<Storage> list = storages.stream().filter(storage -> storage.getPos().equals(target.getPos())).toList();
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
}
