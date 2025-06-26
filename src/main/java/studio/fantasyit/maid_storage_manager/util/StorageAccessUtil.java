package studio.fantasyit.maid_storage_manager.util;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.inventory.handler.BaubleItemHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.advancement.AdvancementTypes;
import studio.fantasyit.maid_storage_manager.items.FilterListItem;
import studio.fantasyit.maid_storage_manager.items.StorageDefineBauble;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;
import studio.fantasyit.maid_storage_manager.storage.Target;

import java.util.*;
import java.util.function.Consumer;

public class StorageAccessUtil {

    /**
     * 获取目标集合上的标记
     *
     * @param level
     * @param target
     * @param posSet
     * @return
     */
    public static List<Pair<Target, ItemStack>> getMarksForPosSet(Level level, Target target, List<BlockPos> posSet) {
        AABB aabb = AABB.ofSize(target.pos.getCenter(), 5, 5, 5);
        List<ItemFrame> frames = level.getEntities(
                EntityTypeTest.forClass(ItemFrame.class),
                aabb,
                itemFrame -> {
                    if (target.side != null && target.side != itemFrame.getDirection()) return false;
                    BlockPos relative = itemFrame.blockPosition().relative(itemFrame.getDirection(), -1);
                    return posSet.stream().anyMatch(t -> t.equals(relative));
                }
        );
        return frames
                .stream()
                .map(frame -> {
                    ItemStack t = frame.getItem();
                    if (t.is(ItemRegistry.FILTER_LIST.get()) || t.is(ItemRegistry.NO_ACCESS.get()) || t.is(ItemRegistry.ALLOW_ACCESS.get())) {
                        BlockPos relative = frame.blockPosition().relative(frame.getDirection(), -1);
                        return new Pair<>(target.sameType(relative, frame.getDirection()), t);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * 获取当前容器上标注的所有的标记
     *
     * @param level
     * @param target
     * @return
     */
    public static List<Pair<Target, ItemStack>> getMarksWithSameContainer(Level level, Target target) {
        List<BlockPos> samePos = new ArrayList<>(List.of(target.pos));
        checkNearByContainers(level, target.pos, samePos::add);
        return getMarksForPosSet(level, target, samePos);
    }


    /**
     * 获取被标记的目标
     *
     * @param level          level
     * @param maid           maid
     * @param target         目标位置
     * @param bypassNoAccess 忽略禁止访问
     * @return 被标记的目标
     */
    public static List<Target> markedTargetOf(ServerLevel level, EntityMaid maid, Target target, boolean bypassNoAccess) {
        List<Pair<Target, ItemStack>> marks = StorageAccessUtil.getMarksWithSameContainer(level, target);
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


    public static TagKey<Block> allowTag = TagKey.create(ForgeRegistries.BLOCKS.getRegistryKey(), new ResourceLocation(MaidStorageManager.MODID, "default_storage_blocks"));

    /**
     * 重写目标列表。对于特定的目标，根据允许访问和禁止访问，将其重写为新的列表。
     *
     * @param level          level
     * @param maid           maid
     * @param target         被重写目标
     * @param bypassNoAccess 忽略禁止访问
     * @return 新的目标列表
     */
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

    /**
     * 目标是否合法
     *
     * @param level          Level
     * @param maid           maid
     * @param target         目标
     * @param bypassNoAccess 是否无视noAccess的标记
     * @return 是否合法
     */
    public static boolean isValidTarget(ServerLevel level, EntityMaid maid, Target target, boolean bypassNoAccess) {
        List<Target> rewrite = findTargetRewrite(level, maid, target, bypassNoAccess);
        return rewrite.contains(target);
    }

    /**
     * 检查附近的方块是否属于同一个容器，如果是则调用consumer
     *
     * @param level    Level
     * @param pos      起始位置
     * @param consumer Consumer
     */
    public static void checkNearByContainers(Level level, BlockPos pos, Consumer<BlockPos> consumer) {
        BlockState blockState = level.getBlockState(pos);
        if (!blockState.is(allowTag)) {
            return;
        }
        BlockEntity blockEntity1 = level.getBlockEntity(pos);
        if (blockEntity1 == null) return;
        @NotNull LazyOptional<IItemHandler> optCap = blockEntity1.getCapability(ForgeCapabilities.ITEM_HANDLER);
        if (!optCap.isPresent()) return;
        IItemHandler inv = optCap.orElseThrow(RuntimeException::new);
        //确保清空第一个格子，再放入物品
        Queue<ItemStack> tmpExtracted = new LinkedList<>();
        while (inv.getStackInSlot(0).getCount() > 0 && !inv.extractItem(0, inv.getStackInSlot(0).getCount(), true).isEmpty())
            tmpExtracted.add(inv.extractItem(0, inv.getStackInSlot(0).getCount(), false));
        ItemStack markItem = Items.STICK.getDefaultInstance().copyWithCount(1);
        CompoundTag tag = markItem.getOrCreateTag();
        tag.putUUID("uuid", UUID.randomUUID());
        markItem.setTag(tag);
        inv.insertItem(0, markItem.copy(), false);
        PosUtil.findAroundUpAndDown(pos, blockPos -> {
            if (blockPos.equals(pos)) return null;
            BlockEntity blockEntity = level.getBlockEntity(blockPos);
            if (blockEntity != null) {
                blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(itemHandler -> {
                    for (int i = 0; i < itemHandler.getSlots(); i++) {
                        if (ItemStack.isSameItemSameTags(itemHandler.getStackInSlot(i), markItem)) {
                            consumer.accept(blockPos);
                        }
                    }
                });
            }
            return null;
        }, 1);

        //第一格有可能拿到的不是MarkItem？判断。如果存在问题，扫描容器查找MarkItem
        ItemStack itemStack = inv.extractItem(0, markItem.getCount(), true);
        if (itemStack.isEmpty() || !ItemStackUtil.isSame(itemStack, markItem, true)) {
            tmpExtracted.add(itemStack);
            InvUtil.tryExtract(inv, markItem, true);
        }else{
            inv.extractItem(0, markItem.getCount(), false);
        }
        while (!tmpExtracted.isEmpty()) {
            inv.insertItem(0, tmpExtracted.poll(), false);
        }
    }

    public static class Filter {
        public List<Pair<ItemStack, Boolean>> filtered;
        public boolean isBlackMode;

        public Filter(List<Pair<ItemStack, Boolean>> filtered, boolean isBlackMode) {
            this.filtered = filtered;
            this.isBlackMode = isBlackMode;
        }

        public boolean isAvailable(ItemStack itemStack) {
            for (Pair<ItemStack, Boolean> pair : filtered) {
                if (ItemStackUtil.isSame(pair.getA(), itemStack, pair.getB())) {
                    return !isBlackMode;
                }
            }
            return isBlackMode;
        }

        public boolean isWhitelist() {
            return !isBlackMode;
        }
    }

    /**
     * 获取指定位置的所有过滤器。
     *
     * @param level
     * @param target
     * @return A：是否黑名单过滤器。B：列表。元素A：物品，B：是否匹配NBT
     */
    public static Filter getFilterForTarget(Level level, Target target) {
        List<Pair<Target, ItemStack>> marksWithSameContainer = StorageAccessUtil.getMarksWithSameContainer(level, target);
        List<Pair<ItemStack, Boolean>> filtered;
        boolean isBlackMode;
        if (marksWithSameContainer.isEmpty()) {
            filtered = new ArrayList<>();
            isBlackMode = true;
        } else {
            List<CompoundTag> items = marksWithSameContainer
                    .stream()
                    .map(Pair::getB)
                    .filter(t -> t.is(ItemRegistry.FILTER_LIST.get()))
                    .map(ItemStack::getOrCreateTag).toList();
            isBlackMode = items.stream().allMatch(t -> t.getBoolean(FilterListItem.TAG_BLACK_MODE));
            filtered = new ArrayList<>();
            items
                    .stream()
                    .filter(t -> !t.getBoolean(FilterListItem.TAG_BLACK_MODE))
                    .forEach(t -> {
                        ListTag list = t.getList(FilterListItem.TAG_ITEMS, ListTag.TAG_COMPOUND);
                        for (int i = 0; i < list.size(); i++) {
                            CompoundTag tmp = list.getCompound(i);
                            ItemStack item = ItemStack.of(tmp.getCompound(FilterListItem.TAG_ITEMS_ITEM));
                            filtered.add(new Pair<>(item, t.getBoolean(FilterListItem.TAG_MATCH_TAG)));
                        }
                    });
            items
                    .stream()
                    .filter(t -> t.getBoolean(FilterListItem.TAG_BLACK_MODE))
                    .forEach(t -> {
                        ListTag list = t.getList(FilterListItem.TAG_ITEMS, ListTag.TAG_COMPOUND);
                        for (int i = 0; i < list.size(); i++) {
                            CompoundTag tmp = list.getCompound(i);
                            ItemStack item = ItemStack.of(tmp.getCompound(FilterListItem.TAG_ITEMS_ITEM));
                            if (isBlackMode)
                                filtered.add(new Pair<>(item, t.getBoolean(FilterListItem.TAG_MATCH_TAG)));
                            else {
                                //白名单模式下，黑名单列表的合并方式：移除撞车的
                                for (int j = 0; j < filtered.size(); j++) {
                                    Pair<ItemStack, Boolean> pair = filtered.get(j);
                                    if (ItemStackUtil.isSame(pair.getA(), item, tmp.getBoolean(FilterListItem.TAG_MATCH_TAG) && pair.getB())) {
                                        filtered.remove(pair);
                                        j--;
                                    }
                                }
                            }
                        }
                    });
        }
        return new Filter(filtered, isBlackMode);
    }
}
