package studio.fantasyit.maid_storage_manager.items;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.menu.ItemSelectorMenu;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

public class RequestListItem extends Item implements MenuProvider {
    public static final String TAG_ITEMS_DONE = "done";
    public static final String TAG_ITEMS_STORED = "stored";
    public static final String TAG_STORAGE = "storage";
    public static final String TAG_ITEMS = "items";
    public static final String TAG_ITEMS_ITEM = "item";
    public static final String TAG_ITEMS_REQUESTED = "requested";
    public static final String TAG_ITEMS_COLLECTED = "collected";
    public static final String TAG_MATCH_TAG = "match_tag";
    public static final String TAG_UUID = "uuid";
    public static final String TAG_IGNORE_TASK = "ignore_task";

    public RequestListItem() {
        super(
                new Properties()
                        .stacksTo(1)
        );
    }

    public static boolean isIgnored(ItemStack mainHandItem) {
        if (!mainHandItem.is(ItemRegistry.REQUEST_LIST_ITEM.get()))
            return false;
        if (!mainHandItem.hasTag())
            return false;
        CompoundTag tag = Objects.requireNonNull(mainHandItem.getTag());
        return tag.getBoolean(TAG_IGNORE_TASK);
    }

    public static void addItemStackCollected(ItemStack mainHandItem, ItemStack a, int count) {
        if (!mainHandItem.is(ItemRegistry.REQUEST_LIST_ITEM.get()))
            return;
        if (!mainHandItem.hasTag())
            return;
        CompoundTag tag = Objects.requireNonNull(mainHandItem.getTag());
        ListTag items = tag.getList(TAG_ITEMS, ListTag.TAG_COMPOUND);
        for (int i = 0; i < items.size(); i++) {
            CompoundTag tmp = items.getCompound(i);
            ItemStack item = ItemStack.of(tmp.getCompound(TAG_ITEMS_ITEM));
            if (!ItemStack.isSameItem(item, a)) continue;
            if (!tag.getBoolean(TAG_MATCH_TAG) || ItemStack.isSameItemSameTags(item, a)) {
                int newCount = tag.getInt(TAG_ITEMS_COLLECTED) + count;
                tmp.putInt(TAG_ITEMS_COLLECTED, newCount);
                if (newCount > tmp.getInt(TAG_ITEMS_REQUESTED) && tmp.getInt(TAG_ITEMS_REQUESTED) != -1) {
                    tmp.putBoolean(TAG_ITEMS_DONE, true);
                }
                items.set(i, tmp);
                break;
            }
        }
        tag.put(TAG_ITEMS, items);
        mainHandItem.setTag(tag);
    }

    public static void clearItemProcess(ItemStack target) {
        if (!target.is(ItemRegistry.REQUEST_LIST_ITEM.get()))
            return;
        CompoundTag tag = target.getOrCreateTag();
        ListTag list = tag.getList(RequestListItem.TAG_ITEMS, ListTag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag tmp = list.getCompound(i);
            tmp.putInt(RequestListItem.TAG_ITEMS_COLLECTED, 0);
            tmp.putInt(RequestListItem.TAG_ITEMS_DONE, 0);
            tmp.putInt(RequestListItem.TAG_ITEMS_STORED, 0);
            list.set(i, tmp);
        }
        tag.putBoolean(RequestListItem.TAG_IGNORE_TASK, false);
        tag.put(RequestListItem.TAG_ITEMS, list);
        tag.putUUID(RequestListItem.TAG_UUID, UUID.randomUUID());
        target.setTag(tag);
    }

    public static boolean matchNbt(ItemStack mainHandItem) {
        if (!mainHandItem.is(ItemRegistry.REQUEST_LIST_ITEM.get()))
            return false;
        if (!mainHandItem.hasTag())
            return false;
        CompoundTag tag = Objects.requireNonNull(mainHandItem.getTag());
        return tag.getBoolean(RequestListItem.TAG_MATCH_TAG);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, @NotNull Player player, @NotNull InteractionHand p_41434_) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, this, (buffer) -> {
            });
            return InteractionResultHolder.consume(player.getItemInHand(p_41434_));
        } else {
            return InteractionResultHolder.pass(player.getItemInHand(p_41434_));
        }
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
        if (!context.getLevel().isClientSide && context.getPlayer() instanceof ServerPlayer serverPlayer) {
            if (!serverPlayer.isShiftKeyDown())
                return InteractionResult.PASS;
            BlockPos clickedPos = context.getClickedPos();
            BlockEntity be = context.getLevel().getBlockEntity(clickedPos);
            if (be != null && be.getCapability(ForgeCapabilities.ITEM_HANDLER).isPresent()) {
                ItemStack item = serverPlayer.getMainHandItem();
                CompoundTag tag = item.getOrCreateTag();
                if (tag.contains(TAG_STORAGE) && NbtUtils.readBlockPos(tag.getCompound(TAG_STORAGE)).equals(clickedPos)) {
                    tag.remove(TAG_STORAGE);
                    serverPlayer.sendSystemMessage(Component.translatable("interaction.clear_storage"));
                } else {
                    tag.put(TAG_STORAGE, NbtUtils.writeBlockPos(clickedPos));
                    serverPlayer.sendSystemMessage(Component.translatable("interaction.bind_storage",
                            clickedPos.getX(),
                            clickedPos.getY(),
                            clickedPos.getZ()));
                }
                item.setTag(tag);
            }
            return InteractionResult.CONSUME;
        } else {
            if (Objects.requireNonNull(context.getPlayer()).isShiftKeyDown())
                return InteractionResult.CONSUME;
            return InteractionResult.PASS;
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack itemStack,
                                @Nullable Level p_41422_,
                                @NotNull List<Component> toolTip,
                                @NotNull TooltipFlag p_41424_) {
        super.appendHoverText(itemStack, p_41422_, toolTip, p_41424_);
        toolTip.add(Component.translatable("tooltip.maid_storage_manager.request_list.desc").withStyle(ChatFormatting.GRAY));
        if (!itemStack.hasTag()) {
            toolTip.add(Component.translatable("tooltip.maid_storage_manager.request_list.no_tag"));
            return;
        }
        CompoundTag tag = Objects.requireNonNull(itemStack.getTag());
        if (!tag.contains(RequestListItem.TAG_STORAGE)) {
            toolTip.add(Component.translatable("tooltip.maid_storage_manager.request_list.no_storage"));
        } else {
            BlockPos storagePos = NbtUtils.readBlockPos(tag.getCompound(RequestListItem.TAG_STORAGE));
            toolTip.add(Component.translatable("tooltip.maid_storage_manager.request_list.storage", storagePos.getX(), storagePos.getY(), storagePos.getZ()));
        }

        if (!tag.contains(RequestListItem.TAG_ITEMS)) {
            toolTip.add(Component.translatable("tooltip.maid_storage_manager.request_list.open_gui_to_config"));
        } else {
            ListTag list = tag.getList(RequestListItem.TAG_ITEMS, ListTag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag itemTag = list.getCompound(i);
                if (!itemTag.contains(RequestListItem.TAG_ITEMS_ITEM)) continue;

                ItemStack itemstack = ItemStack.of(itemTag.getCompound(RequestListItem.TAG_ITEMS_ITEM));
                if (itemstack.isEmpty()) continue;

                int collected = itemTag.getInt(RequestListItem.TAG_ITEMS_COLLECTED);
                int requested = itemTag.getInt(RequestListItem.TAG_ITEMS_REQUESTED);

                Component component = Component.translatable("gui.maid_storage_manager.written_inventory_list.request_item_info",
                        itemstack.getHoverName().getString(),
                        collected,
                        String.valueOf(requested == -1 ? "*" : requested));

                if (itemTag.getBoolean(RequestListItem.TAG_ITEMS_DONE)) {
                    if (collected >= requested || requested == -1) {
                        component = component.copy().withStyle(ChatFormatting.GREEN);
                    } else {
                        component = component.copy().withStyle(ChatFormatting.RED);
                    }
                } else {
                    component = component.copy().withStyle(ChatFormatting.GRAY);
                }
                toolTip.add(component);
            }
        }
    }

    public static boolean isAllStored(ItemStack stack) {
        if (!stack.is(ItemRegistry.REQUEST_LIST_ITEM.get()))
            return false;
        if (!stack.hasTag())
            return false;
        CompoundTag tag = stack.getTag();
        ListTag list = Objects.requireNonNull(tag).getList(TAG_ITEMS, ListTag.TAG_COMPOUND);
        return list.stream()
                .map(t -> (CompoundTag) t)
                .noneMatch(t -> {
                    if (ItemStack.of(t.getCompound(TAG_ITEMS_ITEM)).isEmpty()) return false;
                    if (!t.getBoolean(TAG_ITEMS_DONE)) return false;
                    return t.getInt(TAG_ITEMS_COLLECTED) > t.getInt(TAG_ITEMS_STORED);
                });
    }

    public static List<Pair<ItemStack, Integer>> getItemStacksNotDone(ItemStack stack) {
        return getItemStacksNotDone(stack, true);
    }

    public static List<Pair<ItemStack, Integer>> getItemStacksNotDone(ItemStack stack, boolean includingNoRequest) {
        if (!stack.is(ItemRegistry.REQUEST_LIST_ITEM.get()))
            return List.of();
        if (!stack.hasTag())
            return List.of();
        CompoundTag tag = stack.getTag();
        ListTag list = Objects.requireNonNull(tag).getList(TAG_ITEMS, ListTag.TAG_COMPOUND);
        return list.stream()
                .filter(t -> !((CompoundTag) t).getBoolean(TAG_ITEMS_DONE))
                .filter(t -> ((CompoundTag) t).getInt(TAG_ITEMS_REQUESTED) != -1 || includingNoRequest)
                .map(t -> {
                    ItemStack item = ItemStack.of(((CompoundTag) t).getCompound(TAG_ITEMS_ITEM));
                    int cnt = ((CompoundTag) t).getInt(TAG_ITEMS_REQUESTED);
                    if (cnt != -1)
                        cnt -= ((CompoundTag) t).getInt(TAG_ITEMS_COLLECTED);
                    return new Pair<>(item, cnt);
                })
                .filter(i -> !i.getA().isEmpty())
                .toList();
    }

    public static @Nullable BlockPos getStorageBlock(ItemStack stack) {
        if (!stack.is(ItemRegistry.REQUEST_LIST_ITEM.get()))
            return null;
        if (!stack.hasTag())
            return null;
        CompoundTag tag = Objects.requireNonNull(stack.getTag());
        if (!tag.contains(TAG_STORAGE))
            return null;
        return NbtUtils.readBlockPos(tag.getCompound(TAG_STORAGE));
    }

    /**
     * 更新收集到的物品，从收集到的中自动取出并自动标记需求的
     * 返回剩下的物品
     *
     * @param stack
     * @param collected
     */
    public static ItemStack updateCollectedItem(ItemStack stack, ItemStack collected, int maxCollect) {
        if (!stack.is(ItemRegistry.REQUEST_LIST_ITEM.get()))
            return ItemStack.EMPTY;
        if (!stack.hasTag())
            return ItemStack.EMPTY;
        //如果最大收集量要比物品栈数量小，那么有一部分不算入计算
        int nonCalc = Math.max(0, collected.getCount() - maxCollect);

        //从剩余的数量中进行计算
        int rest = collected.getCount() - nonCalc;
        int available = collected.getCount();

        CompoundTag tag = Objects.requireNonNull(stack.getTag());
        ListTag list = tag.getList(TAG_ITEMS, ListTag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag tmp = list.getCompound(i);
            if (tmp.getBoolean(TAG_ITEMS_DONE))
                continue;
            //获取每一组被需求的物品
            ItemStack requested = ItemStack.of(tmp.getCompound(TAG_ITEMS_ITEM));
            if (ItemStack.isSameItem(collected, requested)) {
                if (!tag.getBoolean(TAG_MATCH_TAG) || ItemStack.isSameItemSameTags(collected, requested)) {
                    int requestedCount = tmp.getInt(TAG_ITEMS_REQUESTED);
                    //如果指定了需要多少某种物品，那么最大值请求的数值
                    int maxToStore = requestedCount;
                    //如果没有指定的话，那么最大值就是无限拿
                    if (maxToStore == -1) maxToStore = Integer.MAX_VALUE;
                    //因为这里的最大值是请求的数量，需要减去已经拿走了的部分
                    maxToStore -= tmp.getInt(TAG_ITEMS_COLLECTED);
                    //最大值不能超过剩余的
                    maxToStore = Math.min(maxToStore, rest);
                    //确认本次是需要进行拿取的
                    if (maxToStore > 0) {
                        rest -= maxToStore;
                        //更新需求表的已收集的值
                        int currentCollected = tmp.getInt(TAG_ITEMS_COLLECTED) + maxToStore;
                        tmp.putInt(TAG_ITEMS_COLLECTED, currentCollected);
                        list.set(i, tmp);
                        //如果已经收集了全部，那么标记为完成
                        if (currentCollected >= requestedCount && requestedCount != -1) {
                            tmp.putBoolean(TAG_ITEMS_DONE, true);
                            list.set(i, tmp);
                        }
                    }
                    if (rest <= 0)
                        break;
                }
            }
        }
        tag.put(TAG_ITEMS, list);
        stack.setTag(tag);
        return collected.copyWithCount(nonCalc + rest);
    }

    public static int updateStored(ItemStack stack, ItemStack toStore, boolean simulate) {
        if (!stack.is(ItemRegistry.REQUEST_LIST_ITEM.get()))
            return toStore.getCount();
        if (!stack.hasTag())
            return toStore.getCount();

        //从剩余的数量中进行计算
        int rest = toStore.getCount();

        CompoundTag tag = Objects.requireNonNull(stack.getTag());
        ListTag list = tag.getList(TAG_ITEMS, ListTag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag tmp = list.getCompound(i);
            ItemStack target = ItemStack.of(tmp.getCompound(TAG_ITEMS_ITEM));
            int collected = tmp.getInt(TAG_ITEMS_COLLECTED);
            int stored = tmp.getInt(TAG_ITEMS_STORED);
            if (stored >= collected)
                continue;
            if (ItemStack.isSameItem(toStore, target)) {
                if (!tag.getBoolean(TAG_MATCH_TAG) || ItemStack.isSameItemSameTags(toStore, target)) {
                    int maxToStore = collected - stored;
                    maxToStore = Math.min(maxToStore, rest);
                    if (maxToStore > 0) {
                        rest -= maxToStore;
                        if (!simulate)
                            tmp.putInt(TAG_ITEMS_STORED, stored + maxToStore);
                        list.set(i, tmp);
                    }
                }
            }
            if (rest <= 0)
                break;
        }
        tag.put(TAG_ITEMS, list);
        stack.setTag(tag);
        return rest;
    }

    public static void updateCollectedNotStored(ItemStack stack, IItemHandler tmpStorage) {
        if (!stack.is(ItemRegistry.REQUEST_LIST_ITEM.get()))
            return;
        if (!stack.hasTag())
            return;
        CompoundTag tag = Objects.requireNonNull(stack.getTag());
        ListTag list = tag.getList(TAG_ITEMS, ListTag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag tmp = list.getCompound(i);
            ItemStack target = ItemStack.of(tmp.getCompound(TAG_ITEMS_ITEM));
            int requested = tmp.getInt(TAG_ITEMS_REQUESTED);
            int collected = tmp.getInt(TAG_ITEMS_COLLECTED);
            int stored = tmp.getInt(TAG_ITEMS_STORED);
            if (stored >= collected)
                continue;
            int count = 0;
            for (int j = 0; j < tmpStorage.getSlots(); j++) {
                ItemStack itemStack = tmpStorage.getStackInSlot(j);
                if (ItemStack.isSameItem(itemStack, target)) {
                    if (!tag.getBoolean(TAG_MATCH_TAG) || ItemStack.isSameItemSameTags(itemStack, target))
                        count += itemStack.getCount();
                }
            }
            tmp.putInt(TAG_ITEMS_COLLECTED, stored + Math.min(requested - stored, count));
        }
    }

    public static void markAllDone(ItemStack stack) {
        if (!stack.is(ItemRegistry.REQUEST_LIST_ITEM.get()))
            return;
        if (!stack.hasTag())
            return;
        CompoundTag tag = Objects.requireNonNull(stack.getTag());
        ListTag list = tag.getList(TAG_ITEMS, ListTag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag tmp = list.getCompound(i);
            tmp.putBoolean(TAG_ITEMS_DONE, true);
            list.set(i, tmp);
        }
        tag.put(TAG_ITEMS, list);
    }

    public static @NotNull UUID getUUID(ItemStack stack) {
        if (!stack.is(ItemRegistry.REQUEST_LIST_ITEM.get()))
            return UUID.randomUUID();
        if (!stack.hasTag())
            return UUID.randomUUID();
        CompoundTag tag = Objects.requireNonNull(stack.getTag());
        if (!tag.contains(TAG_UUID))
            tag.putUUID(TAG_UUID, UUID.randomUUID());
        return tag.getUUID(TAG_UUID);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.literal("");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int p_39954_, Inventory p_39955_, Player p_39956_) {
        return new ItemSelectorMenu(p_39954_, p_39956_);
    }
}
