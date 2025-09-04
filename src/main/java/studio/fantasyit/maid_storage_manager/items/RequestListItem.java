package studio.fantasyit.maid_storage_manager.items;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.debug.DebugData;
import studio.fantasyit.maid_storage_manager.maid.memory.AbstractTargetMemory;
import studio.fantasyit.maid_storage_manager.menu.ItemSelectorMenu;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;
import studio.fantasyit.maid_storage_manager.storage.MaidStorage;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class RequestListItem extends MaidInteractItem implements MenuProvider {
    public static final String TAG_ITEMS_DONE = "done";
    public static final String TAG_ITEMS_STORED = "stored";
    public static final String TAG_STORAGE = "storage";

    public static final String TAG_STORAGE_ENTITY = "storage_entity";
    public static final String TAG_ITEMS = "items";
    public static final String TAG_ITEMS_ITEM = "item";
    public static final String TAG_BLACKMODE = "blackmode";
    public static final String TAG_ITEMS_REQUESTED = "requested";
    public static final String TAG_ITEMS_COLLECTED = "collected";
    public static final String TAG_ITEMS_MISSING = "missing";
    public static final String TAG_ITEMS_FAIL_ADDITION = "fail";
    public static final String TAG_MATCH_TAG = "match_tag";
    public static final String TAG_MATCH = "match";
    public static final String TAG_UUID = "uuid";
    public static final String TAG_IGNORE_TASK = "ignore_task";
    public static final String TAG_COOLING_DOWN = "cooling";
    public static final String TAG_REPEAT_INTERVAL = "interval";
    public static final String TAG_STOCK_MODE = "stock_mode";
    public static final String TAG_HAS_CHECK_STOCK = "has_checked_stock";
    public static final String TAG_UNIT_SECOND = "unit_second";
    private static final String TAG_BLACKMODE_DONE = "blackmode_done";
    public static final String TAG_VIRTUAL = "virtual";
    public static final String TAG_VIRTUAL_SOURCE = "virtual_source";
    public static final String TAG_VIRTUAL_DATA = "virtual_data";

    public RequestListItem() {
        super(new Properties().stacksTo(1));
    }

    public static ItemStackUtil.MATCH_TYPE getMatchType(ItemStack mainHandItem) {
        if (!mainHandItem.is(ItemRegistry.REQUEST_LIST_ITEM.get())) return ItemStackUtil.MATCH_TYPE.AUTO;
        if (!mainHandItem.hasTag()) return ItemStackUtil.MATCH_TYPE.AUTO;
        CompoundTag tag = Objects.requireNonNull(mainHandItem.getTag());
        if (tag.contains(TAG_MATCH_TAG)) {
            tag.putInt(TAG_MATCH, (tag.getBoolean(TAG_MATCH_TAG) ? ItemStackUtil.MATCH_TYPE.MATCHING : ItemStackUtil.MATCH_TYPE.NOT_MATCHING).ordinal());
            tag.remove(TAG_MATCH_TAG);
        }
        return ItemStackUtil.MATCH_TYPE.values()[tag.getInt(TAG_MATCH)];
    }

    public static ItemStackUtil.MATCH_TYPE getMatchType(ItemStack mainHandItem, boolean crafting) {
        if (crafting) return ItemStackUtil.MATCH_TYPE.AUTO;
        return getMatchType(mainHandItem);
    }

    public static boolean isIgnored(ItemStack mainHandItem) {
        if (!mainHandItem.is(ItemRegistry.REQUEST_LIST_ITEM.get())) return false;
        if (!mainHandItem.hasTag()) return false;
        CompoundTag tag = Objects.requireNonNull(mainHandItem.getTag());
        return tag.getBoolean(TAG_IGNORE_TASK);
    }

    public static int getRepeatInterval(ItemStack mainHandItem) {
        if (!mainHandItem.is(ItemRegistry.REQUEST_LIST_ITEM.get())) return 0;
        if (!mainHandItem.hasTag()) return 0;
        CompoundTag tag = Objects.requireNonNull(mainHandItem.getTag());
        return tag.getInt(TAG_REPEAT_INTERVAL);
    }

    public static void addItemStackCollected(ItemStack mainHandItem, ItemStack a, int count) {
        if (!mainHandItem.is(ItemRegistry.REQUEST_LIST_ITEM.get())) return;
        if (!mainHandItem.hasTag()) return;
        CompoundTag tag = Objects.requireNonNull(mainHandItem.getTag());
        ListTag items = tag.getList(TAG_ITEMS, ListTag.TAG_COMPOUND);
        for (int i = 0; i < items.size(); i++) {
            CompoundTag tmp = items.getCompound(i);
            ItemStack item = ItemStackUtil.parseStack(tmp.getCompound(TAG_ITEMS_ITEM));
            if (!ItemStackUtil.isSame(item, a, getMatchType(mainHandItem))) continue;

            int newCount = tag.getInt(TAG_ITEMS_COLLECTED) + count;
            tmp.putInt(TAG_ITEMS_COLLECTED, newCount);
            if (newCount > tmp.getInt(TAG_ITEMS_REQUESTED) && tmp.getInt(TAG_ITEMS_REQUESTED) != -1) {
                tmp.putBoolean(TAG_ITEMS_DONE, true);
            }
            items.set(i, tmp);
            break;
        }
        tag.put(TAG_ITEMS, items);
        mainHandItem.setTag(tag);
    }

    public static void clearItemProcess(ItemStack target) {
        if (!target.is(ItemRegistry.REQUEST_LIST_ITEM.get())) return;
        CompoundTag tag = target.getOrCreateTag();
        ListTag list = tag.getList(RequestListItem.TAG_ITEMS, ListTag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag tmp = list.getCompound(i);
            tmp.putInt(RequestListItem.TAG_ITEMS_COLLECTED, 0);
            tmp.putInt(RequestListItem.TAG_ITEMS_DONE, 0);
            tmp.putInt(RequestListItem.TAG_ITEMS_STORED, 0);
            tmp.remove(RequestListItem.TAG_ITEMS_MISSING);
            tmp.remove(RequestListItem.TAG_ITEMS_FAIL_ADDITION);
            list.set(i, tmp);
        }
        tag.remove(RequestListItem.TAG_ITEMS_FAIL_ADDITION);
        tag.putInt(RequestListItem.TAG_COOLING_DOWN, 0);
        tag.putBoolean(RequestListItem.TAG_IGNORE_TASK, false);
        tag.put(RequestListItem.TAG_ITEMS, list);
        tag.putUUID(RequestListItem.TAG_UUID, UUID.randomUUID());
        tag.putBoolean(RequestListItem.TAG_HAS_CHECK_STOCK, false);
        tag.putBoolean(RequestListItem.TAG_BLACKMODE_DONE, false);
        target.setTag(tag);
    }

    public static void clearAllNonSuccess(ItemStack target) {
        if (!target.is(ItemRegistry.REQUEST_LIST_ITEM.get())) return;
        CompoundTag tag = target.getOrCreateTag();
        ListTag list = tag.getList(RequestListItem.TAG_ITEMS, ListTag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag oDat = list.getCompound(i);
            oDat.putBoolean(RequestListItem.TAG_ITEMS_DONE,
                    oDat.getInt(RequestListItem.TAG_ITEMS_COLLECTED) >= oDat.getInt(RequestListItem.TAG_ITEMS_REQUESTED) || oDat.getInt(RequestListItem.TAG_ITEMS_REQUESTED) == -1
            );
            list.set(i, oDat);
        }
        tag.remove(RequestListItem.TAG_ITEMS_FAIL_ADDITION);
        tag.putInt(RequestListItem.TAG_COOLING_DOWN, 0);
        tag.putBoolean(RequestListItem.TAG_IGNORE_TASK, false);
        tag.put(RequestListItem.TAG_ITEMS, list);
        target.setTag(tag);
    }

    public static boolean isCoolingDown(ItemStack item) {
        if (!item.is(ItemRegistry.REQUEST_LIST_ITEM.get())) return false;
        if (!item.hasTag()) return false;
        CompoundTag tag = Objects.requireNonNull(item.getTag());
        return tag.getInt(TAG_COOLING_DOWN) > 0;
    }

    public static void tickCoolingDown(ItemStack item) {
        if (!item.is(ItemRegistry.REQUEST_LIST_ITEM.get())) return;
        if (!item.hasTag()) return;
        CompoundTag tag = Objects.requireNonNull(item.getTag());
        if (tag.getInt(TAG_COOLING_DOWN) > 0) {
            tag.putInt(TAG_COOLING_DOWN, tag.getInt(TAG_COOLING_DOWN) - 1);
            item.setTag(tag);
            if (tag.getInt(TAG_COOLING_DOWN) == 0) {
                DebugData.sendDebug("Cooling Done(clear_repeat)");
                clearItemProcess(item);
            }
        }
    }

    public static void markDone(ItemStack mainHandItem, ItemStack target) {
        if (!mainHandItem.is(ItemRegistry.REQUEST_LIST_ITEM.get())) return;
        if (!mainHandItem.hasTag()) return;
        CompoundTag tag = Objects.requireNonNull(mainHandItem.getTag());
        ListTag items = tag.getList(TAG_ITEMS, ListTag.TAG_COMPOUND);
        for (int i = 0; i < items.size(); i++) {
            CompoundTag tmp = items.getCompound(i);
            if (!ItemStack.isSameItemSameTags(ItemStackUtil.parseStack(tmp.getCompound(TAG_ITEMS_ITEM)), target))
                continue;
            tmp.putInt(TAG_ITEMS_DONE, 1);
            items.set(i, tmp);
        }
        tag.put(TAG_ITEMS, items);
        mainHandItem.setTag(tag);
    }

    public static void copyFailAdditionTo(ItemStack reqList, ItemStack mainHandItem, ItemStack targetItem) {
        if (!mainHandItem.is(ItemRegistry.REQUEST_LIST_ITEM.get()) || !reqList.is(ItemRegistry.REQUEST_LIST_ITEM.get()))
            return;
        if (!reqList.hasTag()) return;
        CompoundTag fromTag = Objects.requireNonNull(reqList.getTag());
        CompoundTag toTag = mainHandItem.getOrCreateTag();
        if (toTag.contains(TAG_ITEMS_FAIL_ADDITION))
            toTag.putString(TAG_ITEMS_FAIL_ADDITION, fromTag.getString(TAG_ITEMS_FAIL_ADDITION));
        ListTag list = fromTag.getList(TAG_ITEMS, ListTag.TAG_COMPOUND);
        ListTag toList = toTag.getList(TAG_ITEMS, ListTag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag tmp = list.getCompound(i);
            ItemStack item = ItemStackUtil.parseStack(tmp.getCompound(TAG_ITEMS_ITEM));
            if (item.isEmpty()) continue;
            if (!tmp.contains(TAG_ITEMS_FAIL_ADDITION) && !tmp.contains(TAG_ITEMS_MISSING)) continue;
            for (int j = 0; j < toList.size(); j++) {
                CompoundTag tmp2 = toList.getCompound(j);
                ItemStack toItem = ItemStackUtil.parseStack(tmp2.getCompound(TAG_ITEMS_ITEM));
                if (ItemStackUtil.isSame(item, toItem, true)) {
                    tmp2.putString(TAG_ITEMS_FAIL_ADDITION, tmp.getString(TAG_ITEMS_FAIL_ADDITION));
                    tmp2.put(TAG_ITEMS_MISSING, tmp.getList(TAG_ITEMS_MISSING, Tag.TAG_COMPOUND));
                    toList.set(j, tmp2);
                }
            }
        }
    }


    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, @NotNull Player player, @NotNull InteractionHand p_41434_) {
        if (player.isShiftKeyDown()) return InteractionResultHolder.pass(player.getItemInHand(p_41434_));
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            if (!isVirtual(serverPlayer.getMainHandItem()))
                NetworkHooks.openScreen(serverPlayer, this, (buffer) -> {
                });
            return InteractionResultHolder.consume(player.getItemInHand(p_41434_));
        }
        return InteractionResultHolder.pass(player.getItemInHand(p_41434_));
    }

    @Override
    public @NotNull InteractionResult interactLivingEntity(@NotNull ItemStack itemStack, Player player, LivingEntity entity, InteractionHand p_41401_) {
        if (!player.level().isClientSide && p_41401_ == InteractionHand.MAIN_HAND) {
            if (player.isShiftKeyDown()) {
                CompoundTag tag = itemStack.getOrCreateTag();
                if (tag.contains(TAG_STORAGE_ENTITY)) {
                    tag.remove(TAG_STORAGE_ENTITY);
                } else {
                    if (tag.contains(TAG_STORAGE))
                        tag.remove(TAG_STORAGE);
                    tag.putUUID(TAG_STORAGE_ENTITY, entity.getUUID());
                }
                player.getMainHandItem().setTag(tag);
                return InteractionResult.SUCCESS;
            } else if (entity instanceof EntityMaid) {
                if (!hasAnyStorage(itemStack)) {
                    CompoundTag tag = itemStack.getOrCreateTag();
                    if (tag.contains(TAG_STORAGE))
                        tag.remove(TAG_STORAGE);
                    tag.putUUID(TAG_STORAGE_ENTITY, player.getUUID());
                }
            }
        }
        return super.interactLivingEntity(itemStack, player, entity, p_41401_);
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
        if (!context.getLevel().isClientSide && context.getPlayer() instanceof ServerPlayer serverPlayer) {
            if (!serverPlayer.isShiftKeyDown()) return InteractionResult.PASS;
            BlockPos clickedPos = context.getClickedPos();
            Target validTarget = MaidStorage.getInstance().isValidTarget((ServerLevel) context.getLevel(), serverPlayer, clickedPos);
            if (validTarget != null) {
                ItemStack item = serverPlayer.getMainHandItem();
                CompoundTag tag = item.getOrCreateTag();
                if (tag.contains(TAG_STORAGE_ENTITY)) {
                    tag.remove(TAG_STORAGE_ENTITY);
                }
                if (tag.contains(TAG_STORAGE)) {
                    Target storage = Target.fromNbt(tag.getCompound(TAG_STORAGE));
                    if (storage.getPos().equals(clickedPos) && storage.getSide().isPresent() && storage.getSide().get() == context.getClickedFace()) {
                        tag.remove(TAG_STORAGE);
                        serverPlayer.sendSystemMessage(Component.translatable("interaction.clear_storage"));
                    } else {
                        if (storage.pos.equals(clickedPos)) {
                            storage.side = context.getClickedFace();
                        } else {
                            storage.pos = clickedPos;
                            storage.side = null;
                        }
                        serverPlayer.sendSystemMessage(Component.translatable("interaction.bind_storage", clickedPos.getX(), clickedPos.getY(), clickedPos.getZ()));
                        tag.put(TAG_STORAGE, storage.toNbt());
                    }
                } else {
                    tag.put(TAG_STORAGE, validTarget.toNbt());
                    serverPlayer.sendSystemMessage(Component.translatable("interaction.bind_storage", clickedPos.getX(), clickedPos.getY(), clickedPos.getZ()));
                }
                item.setTag(tag);
            }
            return InteractionResult.CONSUME;
        } else {
            if (Objects.requireNonNull(context.getPlayer()).isShiftKeyDown()) return InteractionResult.CONSUME;
            return InteractionResult.PASS;
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack itemStack, @Nullable Level p_41422_, @NotNull List<Component> toolTip, @NotNull TooltipFlag p_41424_) {
        super.appendHoverText(itemStack, p_41422_, toolTip, p_41424_);
        toolTip.add(Component.translatable("tooltip.maid_storage_manager.request_list.desc").withStyle(ChatFormatting.GRAY));
        if (!itemStack.hasTag()) {
            toolTip.add(Component.translatable("tooltip.maid_storage_manager.request_list.no_tag"));
            return;
        }
        CompoundTag tag = Objects.requireNonNull(itemStack.getTag());
        if (tag.contains(RequestListItem.TAG_VIRTUAL) && tag.getBoolean(RequestListItem.TAG_VIRTUAL)) {
            toolTip.add(Component.translatable("tooltip.maid_storage_manager.request_list.virtual").withStyle(ChatFormatting.RED));
        }
        if (tag.contains(RequestListItem.TAG_STORAGE_ENTITY)) {
            String tuuid = tag.getUUID(RequestListItem.TAG_STORAGE_ENTITY).toString().substring(0, 8);
            toolTip.add(Component.translatable("tooltip.maid_storage_manager.request_list.entity", tuuid));
        } else if (tag.contains(RequestListItem.TAG_STORAGE)) {
            Target storage = Target.fromNbt(tag.getCompound(RequestListItem.TAG_STORAGE));
            BlockPos storagePos = storage.getPos();
            toolTip.add(Component.translatable("tooltip.maid_storage_manager.request_list.storage", storagePos.getX(), storagePos.getY(), storagePos.getZ()));
        } else {
            toolTip.add(Component.translatable("tooltip.maid_storage_manager.request_list.no_storage"));
        }

        if (!tag.contains(RequestListItem.TAG_ITEMS)) {
            toolTip.add(Component.translatable("tooltip.maid_storage_manager.request_list.open_gui_to_config"));
        } else {
            ListTag list = tag.getList(RequestListItem.TAG_ITEMS, ListTag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag itemTag = list.getCompound(i);
                if (!itemTag.contains(RequestListItem.TAG_ITEMS_ITEM)) continue;

                ItemStack itemstack = ItemStackUtil.parseStack(itemTag.getCompound(RequestListItem.TAG_ITEMS_ITEM));
                if (itemstack.isEmpty()) continue;

                int collected = itemTag.getInt(RequestListItem.TAG_ITEMS_COLLECTED);
                int requested = itemTag.getInt(RequestListItem.TAG_ITEMS_REQUESTED);

                Component component = Component.translatable("gui.maid_storage_manager.written_inventory_list.request_item_info", itemstack.getHoverName().getString(), collected, String.valueOf(requested == -1 ? "*" : requested));

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

        if (tag.getInt(RequestListItem.TAG_REPEAT_INTERVAL) > 0) {
            if (tag.getBoolean(RequestListItem.TAG_UNIT_SECOND))
                toolTip.add(Component.translatable("tooltip.maid_storage_manager.request_list.repeat_interval_second", tag.getInt(RequestListItem.TAG_REPEAT_INTERVAL) / 20));
            else
                toolTip.add(Component.translatable("tooltip.maid_storage_manager.request_list.repeat_interval_tick", tag.getInt(RequestListItem.TAG_REPEAT_INTERVAL)));
            if (tag.getInt(RequestListItem.TAG_COOLING_DOWN) > 0) {
                int cd = tag.getInt(RequestListItem.TAG_COOLING_DOWN);
                if (tag.getBoolean(RequestListItem.TAG_UNIT_SECOND))
                    cd /= 20;
                toolTip.add(Component.translatable("tooltip.maid_storage_manager.request_list.cooling_down", cd).withStyle(ChatFormatting.GREEN));
            }
        }
    }

    public static boolean isAllStored(ItemStack stack) {
        if (!stack.is(ItemRegistry.REQUEST_LIST_ITEM.get())) return false;
        if (!stack.hasTag()) return false;
        CompoundTag tag = stack.getTag();
        ListTag list = Objects.requireNonNull(tag).getList(TAG_ITEMS, ListTag.TAG_COMPOUND);
        return list.stream().map(t -> (CompoundTag) t).noneMatch(t -> {
            if (ItemStackUtil.parseStack(t.getCompound(TAG_ITEMS_ITEM)).isEmpty()) return false;
            if (!t.getBoolean(TAG_ITEMS_DONE)) return false;
            return t.getInt(TAG_ITEMS_COLLECTED) > t.getInt(TAG_ITEMS_STORED);
        });
    }

    public static List<Pair<ItemStack, Integer>> getItemStacksNotDone(ItemStack stack) {
        return getItemStacksNotDone(stack, true);
    }

    public static List<Pair<ItemStack, Integer>> getItemStacksNotDone(ItemStack stack, boolean includingNoRequest) {
        if (!stack.is(ItemRegistry.REQUEST_LIST_ITEM.get())) return List.of();
        if (!stack.hasTag()) return List.of();

        CompoundTag tag = stack.getTag();
        if (tag.getBoolean(TAG_BLACKMODE)) return List.of();
        ListTag list = Objects.requireNonNull(tag).getList(TAG_ITEMS, ListTag.TAG_COMPOUND);
        return list.stream()
                .filter(t -> !((CompoundTag) t).getBoolean(TAG_ITEMS_DONE))
                .filter(t -> ((CompoundTag) t).getInt(TAG_ITEMS_REQUESTED) != -1 || includingNoRequest).map(t -> {
                    ItemStack item = ItemStackUtil.parseStack(((CompoundTag) t).getCompound(TAG_ITEMS_ITEM));
                    int cnt = ((CompoundTag) t).getInt(TAG_ITEMS_REQUESTED);
                    if (cnt != -1) cnt -= ((CompoundTag) t).getInt(TAG_ITEMS_COLLECTED);
                    return new Pair<>(item, cnt);
                }).filter(i -> !i.getA().isEmpty()).toList();
    }


    public static @Nullable UUID getStorageEntity(ItemStack stack) {
        if (!stack.is(ItemRegistry.REQUEST_LIST_ITEM.get())) return null;
        if (!stack.hasTag()) return null;
        CompoundTag tag = Objects.requireNonNull(stack.getTag());
        if (!tag.contains(TAG_STORAGE_ENTITY)) return null;
        return tag.getUUID(TAG_STORAGE_ENTITY);
    }

    public static @Nullable Target getStorageBlock(ItemStack stack) {
        if (!stack.is(ItemRegistry.REQUEST_LIST_ITEM.get())) return null;
        if (!stack.hasTag()) return null;
        CompoundTag tag = Objects.requireNonNull(stack.getTag());
        if (!tag.contains(TAG_STORAGE)) return null;
        return Target.fromNbt(tag.getCompound(TAG_STORAGE));
    }

    public static boolean hasAnyStorage(ItemStack stack) {
        Target storageBlock = getStorageBlock(stack);
        if (storageBlock != null && storageBlock.getType() != AbstractTargetMemory.TargetData.NO_TARGET) {
            return true;
        }
        return getStorageEntity(stack) != null;
    }

    /**
     * 更新收集到的物品，从收集到的中自动取出并自动标记需求的
     * 返回剩下的物品
     *
     * @param stack
     * @param collected
     */
    public static ItemStack updateCollectedItem(ItemStack stack, ItemStack collected, int maxCollect, boolean isInCrafting) {
        if (!stack.is(ItemRegistry.REQUEST_LIST_ITEM.get())) return ItemStack.EMPTY;
        if (!stack.hasTag()) return ItemStack.EMPTY;
        CompoundTag tag = Objects.requireNonNull(stack.getTag());
        //如果最大收集量要比物品栈数量小，那么有一部分不算入计算
        int nonCalc = Math.max(0, collected.getCount() - maxCollect);
        //从剩余的数量中进行计算
        int rest = collected.getCount() - nonCalc;
        int available = collected.getCount();

        ListTag list = tag.getList(TAG_ITEMS, ListTag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag tmp = list.getCompound(i);
            if (tmp.getBoolean(TAG_ITEMS_DONE)) continue;
            //获取每一组被需求的物品
            ItemStack requested = ItemStackUtil.parseStack(tmp.getCompound(TAG_ITEMS_ITEM));
            if (ItemStackUtil.isSame(collected, requested, getMatchType(stack, isInCrafting))) {
                //如果黑名单，那么匹配的物品是不用收集的
                if (tag.getBoolean(TAG_BLACKMODE)) return collected;
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
                if (rest <= 0) break;
            }
        }
        tag.put(TAG_ITEMS, list);
        stack.setTag(tag);
        //黑名单情况，如果
        if (tag.getBoolean(TAG_BLACKMODE)) {
            rest = 0;
        }
        return collected.copyWithCount(nonCalc + rest);
    }

    public static int updateStored(ItemStack stack, ItemStack toStore, boolean simulate, boolean isInCrafting) {
        if (!stack.is(ItemRegistry.REQUEST_LIST_ITEM.get())) return toStore.getCount();
        if (!stack.hasTag()) return toStore.getCount();

        //从剩余的数量中进行计算
        int rest = toStore.getCount();

        CompoundTag tag = Objects.requireNonNull(stack.getTag());
        ListTag list = tag.getList(TAG_ITEMS, ListTag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag tmp = list.getCompound(i);
            ItemStack target = ItemStackUtil.parseStack(tmp.getCompound(TAG_ITEMS_ITEM));
            int collected = tmp.getInt(TAG_ITEMS_COLLECTED);
            int stored = tmp.getInt(TAG_ITEMS_STORED);
            if (stored >= collected) continue;
            if (ItemStackUtil.isSame(toStore, target, getMatchType(stack, isInCrafting))) {
                //黑名单物品不进行存储
                if (tag.getBoolean(TAG_BLACKMODE)) return rest;
                int maxToStore = collected - stored;
                maxToStore = Math.min(maxToStore, rest);
                if (maxToStore > 0) {
                    rest -= maxToStore;
                    if (!simulate) tmp.putInt(TAG_ITEMS_STORED, stored + maxToStore);
                    list.set(i, tmp);
                }
            }
            if (rest <= 0) break;
        }
        tag.put(TAG_ITEMS, list);
        stack.setTag(tag);
        if (tag.getBoolean(TAG_BLACKMODE)) {
            rest = 0;
        }
        return rest;
    }

    public static void updateCollectedNotStored(ItemStack stack, IItemHandler tmpStorage) {
        if (!stack.is(ItemRegistry.REQUEST_LIST_ITEM.get())) return;
        if (!stack.hasTag()) return;
        CompoundTag tag = Objects.requireNonNull(stack.getTag());
        ListTag list = tag.getList(TAG_ITEMS, ListTag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag tmp = list.getCompound(i);
            ItemStack target = ItemStackUtil.parseStack(tmp.getCompound(TAG_ITEMS_ITEM));
            int requested = tmp.getInt(TAG_ITEMS_REQUESTED);
            int collected = tmp.getInt(TAG_ITEMS_COLLECTED);
            int stored = tmp.getInt(TAG_ITEMS_STORED);
            if (stored >= collected) continue;
            int count = 0;
            for (int j = 0; j < tmpStorage.getSlots(); j++) {
                ItemStack itemStack = tmpStorage.getStackInSlot(j);
                if (ItemStackUtil.isSame(itemStack, target, getMatchType(stack))) {
                    count += itemStack.getCount();
                }
            }
            tmp.putInt(TAG_ITEMS_COLLECTED, stored + Math.min(requested - stored, count));
        }
    }

    public static void setFailAddition(ItemStack stack, ItemStack item, String failAddition) {
        if (!stack.is(ItemRegistry.REQUEST_LIST_ITEM.get())) return;
        if (!stack.hasTag()) return;
        CompoundTag tag = Objects.requireNonNull(stack.getTag());
        ListTag list = tag.getList(TAG_ITEMS, ListTag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag tmp = list.getCompound(i);
            if (ItemStack.isSameItemSameTags(ItemStackUtil.parseStack(tmp.getCompound(TAG_ITEMS_ITEM)), item)) {
                tag.putString(TAG_ITEMS_FAIL_ADDITION, failAddition);
                list.set(i, tmp);
            }
        }
        tag.put(TAG_ITEMS, list);
    }

    public static void markAllDone(ItemStack stack) {
        if (!stack.is(ItemRegistry.REQUEST_LIST_ITEM.get())) return;
        if (!stack.hasTag()) return;
        CompoundTag tag = Objects.requireNonNull(stack.getTag());
        ListTag list = tag.getList(TAG_ITEMS, ListTag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag tmp = list.getCompound(i);
            tmp.putBoolean(TAG_ITEMS_DONE, true);
            list.set(i, tmp);
        }
        tag.putBoolean(TAG_BLACKMODE_DONE, true);
        tag.put(TAG_ITEMS, list);
    }

    public static @NotNull UUID getUUID(ItemStack stack) {
        if (!stack.is(ItemRegistry.REQUEST_LIST_ITEM.get())) return UUID.randomUUID();
        if (!stack.hasTag()) return UUID.randomUUID();
        CompoundTag tag = Objects.requireNonNull(stack.getTag());
        if (!tag.contains(TAG_UUID)) tag.putUUID(TAG_UUID, UUID.randomUUID());
        return tag.getUUID(TAG_UUID);
    }

    public static void setMissingItem(ItemStack requestListItem, ItemStack toSetFailItem, List<ItemStack> missing) {
        if (!requestListItem.is(ItemRegistry.REQUEST_LIST_ITEM.get())) return;
        if (!requestListItem.hasTag()) return;
        CompoundTag tag = Objects.requireNonNull(requestListItem.getTag());
        ListTag list = tag.getList(TAG_ITEMS, ListTag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag tmp = list.getCompound(i);
            if (ItemStack.isSameItemSameTags(ItemStackUtil.parseStack(tmp.getCompound(TAG_ITEMS_ITEM)), toSetFailItem)) {
                ListTag missingList = tmp.getList(TAG_ITEMS_MISSING, ListTag.TAG_COMPOUND);
                for (ItemStack ti : missing) {
                    if (ti.isEmpty()) continue;
                    if (ti.is(ItemRegistry.REQUEST_LIST_ITEM.get()))
                        continue;//TODO: 验证问题是否任然存在
                    int idx = -1;
                    for (int j = 0; j < missingList.size(); j++) {
                        if (ItemStack.isSameItemSameTags(ItemStackUtil.parseStack(missingList.getCompound(j)), ti))
                            idx = j;
                    }
                    if (idx != -1) {
                        ItemStack toBeSaved = ItemStackUtil.parseStack(missingList.getCompound(idx));
                        toBeSaved.grow(ti.getCount());
                        missingList.set(idx, ItemStackUtil.saveStack(toBeSaved));
                    } else if (missingList.size() < 15)
                        missingList.add(ItemStackUtil.saveStack(ti));
                }

                tmp.put(TAG_ITEMS_MISSING, missingList);
                list.set(i, tmp);
                break;
            }
        }
        tag.put(TAG_ITEMS, list);
        requestListItem.setTag(tag);
    }

    public static boolean isAllSuccess(ItemStack stack) {
        if (!stack.is(ItemRegistry.REQUEST_LIST_ITEM.get())) return false;
        if (!stack.hasTag()) return false;
        CompoundTag tag = Objects.requireNonNull(stack.getTag());
        if (tag.getBoolean(TAG_BLACKMODE)) return tag.getBoolean(TAG_BLACKMODE_DONE);
        ListTag list = tag.getList(TAG_ITEMS, ListTag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag tmp = list.getCompound(i);
            if (ItemStackUtil.parseStack(tmp.getCompound(TAG_ITEMS_ITEM)).isEmpty()) continue;
            if (tmp.getInt(TAG_ITEMS_REQUESTED) == -1) continue;
            if (tmp.getInt(TAG_ITEMS_COLLECTED) < tmp.getInt(TAG_ITEMS_REQUESTED)) return false;
        }
        return true;
    }

    public static boolean isStockMode(ItemStack stack) {
        if (!stack.is(ItemRegistry.REQUEST_LIST_ITEM.get())) return false;
        if (!stack.hasTag()) return false;
        CompoundTag tag = Objects.requireNonNull(stack.getTag());
        return tag.getBoolean(TAG_STOCK_MODE);
    }

    public static boolean hasCheckedStock(ItemStack stack) {
        if (!stack.is(ItemRegistry.REQUEST_LIST_ITEM.get())) return false;
        if (!stack.hasTag()) return false;
        CompoundTag tag = Objects.requireNonNull(stack.getTag());
        return tag.getBoolean(TAG_HAS_CHECK_STOCK);
    }

    public static void setHasCheckedStock(ItemStack stack, boolean has) {
        if (!stack.is(ItemRegistry.REQUEST_LIST_ITEM.get())) return;
        CompoundTag tag = stack.getOrCreateTag();
        tag.putBoolean(TAG_HAS_CHECK_STOCK, has);
    }

    public static boolean isVirtual(ItemStack stack) {
        if (!stack.is(ItemRegistry.REQUEST_LIST_ITEM.get())) return false;
        return stack.getOrCreateTag().getBoolean(TAG_VIRTUAL);
    }

    public static boolean isBlackMode(ItemStack stack) {
        if (!stack.is(ItemRegistry.REQUEST_LIST_ITEM.get())) return false;
        if (!stack.hasTag()) return false;
        CompoundTag tag = Objects.requireNonNull(stack.getTag());
        return tag.getBoolean(TAG_BLACKMODE);
    }

    public static boolean isBlackModeDone(ItemStack stack) {
        if (!stack.is(ItemRegistry.REQUEST_LIST_ITEM.get())) return false;
        if (!stack.hasTag()) return false;
        CompoundTag tag = Objects.requireNonNull(stack.getTag());
        return tag.getBoolean(TAG_BLACKMODE_DONE);
    }

    public static void setVirtualData(ItemStack stack, CompoundTag data) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.put(TAG_VIRTUAL_DATA, data);
        stack.setTag(tag);
    }

    public static CompoundTag getVirtualData(ItemStack stack) {
        if (!stack.is(ItemRegistry.REQUEST_LIST_ITEM.get())) return null;
        if (!stack.hasTag()) return null;
        CompoundTag tag = Objects.requireNonNull(stack.getTag());
        return tag.getCompound(TAG_VIRTUAL_DATA);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.literal("");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int p_39954_, Inventory p_39955_, Player p_39956_) {
        if (isVirtual(p_39956_.getItemInHand(InteractionHand.MAIN_HAND)))
            return null;
        return new ItemSelectorMenu(p_39954_, p_39956_);
    }

}
