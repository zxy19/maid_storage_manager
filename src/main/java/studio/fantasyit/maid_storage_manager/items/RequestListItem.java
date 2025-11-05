package studio.fantasyit.maid_storage_manager.items;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
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
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.items.data.RequestItemStackList;
import studio.fantasyit.maid_storage_manager.maid.memory.AbstractTargetMemory;
import studio.fantasyit.maid_storage_manager.menu.request.ItemSelectorMenu;
import studio.fantasyit.maid_storage_manager.registry.DataComponentRegistry;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;
import studio.fantasyit.maid_storage_manager.storage.MaidStorage;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class RequestListItem extends MaidInteractItem implements MenuProvider {

    public RequestListItem() {
        super(new Properties()
                .stacksTo(1)
                .component(DataComponentRegistry.REQUEST_ITEMS.get(), new RequestItemStackList().toImmutable())
        );
    }


    public static @NotNull RequestItemStackList getMutableRequestData(ItemStack target) {
        return getImmutableRequestData(target).toMutable();
    }

    public static @NotNull RequestItemStackList.Immutable getImmutableRequestData(ItemStack target) {
        if (!target.has(DataComponentRegistry.REQUEST_ITEMS))
            target.set(DataComponentRegistry.REQUEST_ITEMS.get(), new RequestItemStackList().toImmutable());
        return Objects.requireNonNull(target.get(DataComponentRegistry.REQUEST_ITEMS.get()));
    }

    public static ItemStackUtil.MATCH_TYPE getMatchType(ItemStack mainHandItem) {
        if (!mainHandItem.is(ItemRegistry.REQUEST_LIST_ITEM.get())) return ItemStackUtil.MATCH_TYPE.AUTO;
        return ItemStackUtil.MATCH_TYPE.values()[mainHandItem.getOrDefault(DataComponentRegistry.REQUEST_MATCHING.get(), 0)];
    }

    public static ItemStackUtil.MATCH_TYPE getMatchType(ItemStack mainHandItem, boolean crafting) {
        if (crafting) return ItemStackUtil.MATCH_TYPE.AUTO;
        return getMatchType(mainHandItem);
    }

    public static boolean isIgnored(ItemStack mainHandItem) {
        if (!mainHandItem.is(ItemRegistry.REQUEST_LIST_ITEM.get())) return false;
        return mainHandItem.getOrDefault(DataComponentRegistry.REQUEST_IGNORE.get(), false);
    }

    public static void setIgnore(ItemStack reqList) {
        if (!reqList.is(ItemRegistry.REQUEST_LIST_ITEM.get())) return;
        reqList.set(DataComponentRegistry.REQUEST_IGNORE.get(), true);
    }

    public static int getRepeatInterval(ItemStack mainHandItem) {
        if (!mainHandItem.is(ItemRegistry.REQUEST_LIST_ITEM.get())) return 0;
        return Optional.ofNullable(
                mainHandItem.get(DataComponentRegistry.REQUEST_INTERVAL.get())
        ).orElse(0);
    }

    public static int getRepeatCd(ItemStack mainHandItem) {
        if (!mainHandItem.is(ItemRegistry.REQUEST_LIST_ITEM.get())) return 0;
        return Optional.ofNullable(
                mainHandItem.get(DataComponentRegistry.REQUEST_CD.get())
        ).orElse(0);
    }

    public static void addItemStackCollected(ItemStack mainHandItem, ItemStack a, int count) {
        if (!mainHandItem.is(ItemRegistry.REQUEST_LIST_ITEM.get())) return;
        RequestItemStackList request = getMutableRequestData(mainHandItem);
        List<RequestItemStackList.ListItem> items = request.getList();
        for (RequestItemStackList.ListItem listItem : items) {
            ItemStack item = listItem.getItem();
            if (!ItemStackUtil.isSame(item, a, getMatchType(mainHandItem))) continue;

            listItem.collected += count;
            if (listItem.collected > listItem.requested && listItem.requested != -1) {
                listItem.done = true;
            }
        }
        mainHandItem.set(DataComponentRegistry.REQUEST_ITEMS, request.toImmutable());
    }

    public static void clearItemProcess(ItemStack target) {
        if (!target.is(ItemRegistry.REQUEST_LIST_ITEM.get())) return;
        RequestItemStackList request = getMutableRequestData(target);
        List<RequestItemStackList.ListItem> list = request.getList();
        for (RequestItemStackList.ListItem tmp : list) {
            tmp.collected = 0;
            tmp.stored = 0;
            tmp.done = false;
            tmp.failAddition = "";
            tmp.missing.clear();
        }
        request.blacklistDone = false;
        request.stockModeChecked = false;

        target.set(DataComponentRegistry.REQUEST_ITEMS.get(), request.toImmutable());
        target.set(DataComponentRegistry.REQUEST_IGNORE.get(), false);
        target.set(DataComponentRegistry.REQUEST_FAIL_ADDITION.get(), "");
        target.set(DataComponentRegistry.REQUEST_WORK_UUID.get(), UUID.randomUUID());
        target.set(DataComponentRegistry.REQUEST_CD.get(), 0);
    }

    public static void clearAllNonSuccess(ItemStack target) {
        if (!target.is(ItemRegistry.REQUEST_LIST_ITEM.get())) return;
        RequestItemStackList request = getMutableRequestData(target);
        List<RequestItemStackList.ListItem> list = request.getList();
        for (RequestItemStackList.ListItem tmp : list) {
            tmp.done = tmp.collected >= tmp.requested || tmp.requested == 0;
        }
        target.set(DataComponentRegistry.REQUEST_ITEMS.get(), request.toImmutable());
        target.set(DataComponentRegistry.REQUEST_FAIL_ADDITION.get(), "");
        target.set(DataComponentRegistry.REQUEST_CD.get(), 0);
        target.set(DataComponentRegistry.REQUEST_IGNORE.get(), false);
    }

    public static boolean isCoolingDown(ItemStack item) {
        if (!item.is(ItemRegistry.REQUEST_LIST_ITEM.get())) return false;
        Integer cd = item.get(DataComponentRegistry.REQUEST_CD.get());
        return cd != null && cd > 0;
    }

    public static void tickCoolingDown(ItemStack item) {
        if (!item.is(ItemRegistry.REQUEST_LIST_ITEM.get())) return;
        Integer cd = item.get(DataComponentRegistry.REQUEST_CD.get());
        if (cd != null && cd > 0) {
            cd--;
            item.set(DataComponentRegistry.REQUEST_CD.get(), cd);
            if (cd == 0) {
                clearItemProcess(item);
            }
        }
    }

    public static void markDone(ItemStack mainHandItem, ItemStack target) {
        if (!mainHandItem.is(ItemRegistry.REQUEST_LIST_ITEM.get())) return;
        RequestItemStackList request = getMutableRequestData(mainHandItem);
        List<RequestItemStackList.ListItem> items = request.getList();
        for (RequestItemStackList.ListItem item : items) {
            if (!ItemStack.isSameItemSameComponents(item.item, target)) continue;
            item.done = true;
        }
        mainHandItem.set(DataComponentRegistry.REQUEST_ITEMS.get(), request.toImmutable());
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, @NotNull Player player, @NotNull InteractionHand p_41434_) {
        if (player.isShiftKeyDown()) return InteractionResultHolder.pass(player.getItemInHand(p_41434_));
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            if (!isVirtual(serverPlayer.getMainHandItem()))
                serverPlayer.openMenu(this, (buffer) -> {
                });
            return InteractionResultHolder.consume(player.getItemInHand(p_41434_));
        }
        return InteractionResultHolder.pass(player.getItemInHand(p_41434_));
    }

    @Override
    public @NotNull InteractionResult interactLivingEntity(@NotNull ItemStack itemStack, Player player, LivingEntity entity, InteractionHand p_41401_) {
        if (!player.level().isClientSide && p_41401_ == InteractionHand.MAIN_HAND) {
            if (player.isShiftKeyDown()) {
                if (itemStack.has(DataComponentRegistry.REQUEST_STORAGE_ENTITY)) {
                    itemStack.remove(DataComponentRegistry.REQUEST_STORAGE_ENTITY);
                } else {
                    if (itemStack.has(DataComponentRegistry.REQUEST_STORAGE_BLOCK))
                        itemStack.remove(DataComponentRegistry.REQUEST_STORAGE_BLOCK);
                    itemStack.set(DataComponentRegistry.REQUEST_STORAGE_ENTITY, entity.getUUID());
                }
                return InteractionResult.SUCCESS;
            } else if (entity instanceof EntityMaid) {
                if (!hasAnyStorage(itemStack)) {
                    itemStack.remove(DataComponentRegistry.REQUEST_STORAGE_BLOCK);
                    itemStack.set(DataComponentRegistry.REQUEST_STORAGE_ENTITY, player.getUUID());
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
                if (item.has(DataComponentRegistry.REQUEST_STORAGE_ENTITY)) {
                    item.remove(DataComponentRegistry.REQUEST_STORAGE_ENTITY);
                }
                if (item.has(DataComponentRegistry.REQUEST_STORAGE_BLOCK)) {
                    Target storage = Objects.requireNonNull(item.get(DataComponentRegistry.REQUEST_STORAGE_BLOCK));
                    if (storage.getPos().equals(clickedPos) && storage.getSide().isPresent() && storage.getSide().get() == context.getClickedFace()) {
                        item.remove(DataComponentRegistry.REQUEST_STORAGE_BLOCK);
                        serverPlayer.sendSystemMessage(Component.translatable("interaction.clear_storage"));
                    } else {
                        if (storage.pos.equals(clickedPos)) {
                            storage.side = context.getClickedFace();
                        } else {
                            storage.pos = clickedPos;
                            storage.side = null;
                        }
                        serverPlayer.sendSystemMessage(Component.translatable("interaction.bind_storage", clickedPos.getX(), clickedPos.getY(), clickedPos.getZ()));
                        item.set(DataComponentRegistry.REQUEST_STORAGE_BLOCK, storage);
//                        TourGuideTrigger.trigger(serverPlayer, "request_list_bind");
                    }
                } else {
                    item.set(DataComponentRegistry.REQUEST_STORAGE_BLOCK, validTarget);
                    serverPlayer.sendSystemMessage(Component.translatable("interaction.bind_storage", clickedPos.getX(), clickedPos.getY(), clickedPos.getZ()));
                    //TODO bind Trigger
                }
            }
            return InteractionResult.CONSUME;
        } else {
            if (Objects.requireNonNull(context.getPlayer()).isShiftKeyDown()) return InteractionResult.CONSUME;
            return InteractionResult.PASS;
        }
    }

    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext p_339594_, List<Component> toolTip, TooltipFlag p_41424_) {
        super.appendHoverText(itemStack, p_339594_, toolTip, p_41424_);

        toolTip.add(Component.translatable("tooltip.maid_storage_manager.request_list.desc").withStyle(ChatFormatting.GRAY));

        if (Boolean.TRUE.equals(itemStack.get(DataComponentRegistry.REQUEST_VIRTUAL))) {
            toolTip.add(Component.translatable("tooltip.maid_storage_manager.request_list.virtual").withStyle(ChatFormatting.RED));
        }
        if (itemStack.has(DataComponentRegistry.REQUEST_STORAGE_ENTITY)) {
            String tuuid = itemStack.get(DataComponentRegistry.REQUEST_STORAGE_ENTITY).toString().substring(0, 8);
            toolTip.add(Component.translatable("tooltip.maid_storage_manager.request_list.entity", tuuid));
        } else if (itemStack.has(DataComponentRegistry.REQUEST_STORAGE_BLOCK)) {
            Target storage = itemStack.get(DataComponentRegistry.REQUEST_STORAGE_BLOCK);
            BlockPos storagePos = storage.getPos();
            toolTip.add(Component.translatable("tooltip.maid_storage_manager.request_list.storage", storagePos.getX(), storagePos.getY(), storagePos.getZ()));
        } else {
            toolTip.add(Component.translatable("tooltip.maid_storage_manager.request_list.no_storage"));
        }

        RequestItemStackList.Immutable request = getImmutableRequestData(itemStack);
        if (request != null) {
            List<RequestItemStackList.ImmutableItem> list = request.list();
            for (int i = 0; i < list.size(); i++) {
                RequestItemStackList.ImmutableItem tmp = list.get(i);
                ItemStack itemstack = tmp.item();
                if (itemstack.isEmpty()) continue;


                Component component = Component.translatable("gui.maid_storage_manager.written_inventory_list.request_item_info",
                        itemstack.getHoverName().getString(),
                        tmp.collected(),
                        String.valueOf(tmp.requested() == -1 ? "*" : tmp.requested()));

                if (tmp.done()) {
                    if (tmp.collected() >= tmp.requested() || tmp.requested() == -1) {
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

        if (itemStack.getOrDefault(DataComponentRegistry.REQUEST_INTERVAL, 0) > 0) {
            if (itemStack.getOrDefault(DataComponentRegistry.REQUEST_CD_UNIT, false))
                toolTip.add(Component.translatable("tooltip.maid_storage_manager.request_list.repeat_interval_second", itemStack.getOrDefault(DataComponentRegistry.REQUEST_INTERVAL, 0) / 20));
            else
                toolTip.add(Component.translatable("tooltip.maid_storage_manager.request_list.repeat_interval_tick", itemStack.getOrDefault(DataComponentRegistry.REQUEST_INTERVAL, 0)));
            if (itemStack.getOrDefault(DataComponentRegistry.REQUEST_CD, 0) > 0) {
                int cd = itemStack.getOrDefault(DataComponentRegistry.REQUEST_CD, 0);
                if (itemStack.getOrDefault(DataComponentRegistry.REQUEST_CD_UNIT, false))
                    cd /= 20;
                toolTip.add(Component.translatable("tooltip.maid_storage_manager.request_list.cooling_down", cd).withStyle(ChatFormatting.GREEN));
            }
        }
    }

    public static boolean isAllStored(ItemStack stack) {
        if (!stack.is(ItemRegistry.REQUEST_LIST_ITEM.get())) return false;
        RequestItemStackList.Immutable request = getImmutableRequestData(stack);
        if (request == null) return true;
        return request.list().stream().noneMatch(t -> {
            if (t.item().isEmpty()) return false;
            if (!t.done()) return false;
            return t.collected() > t.stored();
        });
    }

    public static List<Pair<ItemStack, Integer>> getItemStacksNotDone(ItemStack stack) {
        return getItemStacksNotDone(stack, true);
    }

    public static List<Pair<ItemStack, Integer>> getItemStacksNotDone(ItemStack stack, boolean includingNoRequest) {
        if (!stack.is(ItemRegistry.REQUEST_LIST_ITEM.get())) return List.of();
        RequestItemStackList.Immutable request = getImmutableRequestData(stack);
        if (request == null) return List.of();
        if (request.blackList()) return List.of();
        List<RequestItemStackList.ImmutableItem> list = request.list();
        return list.stream()
                .filter(t -> !t.done())
                .filter(t -> t.requested() != -1 || includingNoRequest).map(t -> {
                    int cnt = t.requested();
                    if (cnt != -1) cnt -= t.collected();
                    return new Pair<>(t.item(), cnt);
                }).filter(i -> !i.getA().isEmpty()).toList();
    }


    public static @Nullable UUID getStorageEntity(ItemStack stack) {
        if (!stack.is(ItemRegistry.REQUEST_LIST_ITEM.get())) return null;
        return stack.get(DataComponentRegistry.REQUEST_STORAGE_ENTITY);
    }

    public static @Nullable Target getStorageBlock(ItemStack stack) {
        if (!stack.is(ItemRegistry.REQUEST_LIST_ITEM.get())) return null;
        return stack.get(DataComponentRegistry.REQUEST_STORAGE_BLOCK);
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
        RequestItemStackList request = getMutableRequestData(stack);
        if (request == null) return ItemStack.EMPTY;
        //如果最大收集量要比物品栈数量小，那么有一部分不算入计算
        int nonCalc = Math.max(0, collected.getCount() - maxCollect);
        //从剩余的数量中进行计算
        int rest = collected.getCount() - nonCalc;
        int available = collected.getCount();

        List<RequestItemStackList.ListItem> list = request.getList();
        for (int i = 0; i < list.size(); i++) {
            RequestItemStackList.ListItem tmp = list.get(i);
            if (tmp.done) continue;
            //获取每一组被需求的物品
            ItemStack requested = tmp.getItem();
            if (ItemStackUtil.isSame(collected, requested, getMatchType(stack, isInCrafting))) {
                //如果黑名单，那么匹配的物品是不用收集的
                if (request.blackList) return collected;
                int requestedCount = tmp.requested;
                //如果指定了需要多少某种物品，那么最大值请求的数值
                int maxToStore = requestedCount;
                //如果没有指定的话，那么最大值就是无限拿
                if (maxToStore == -1) maxToStore = Integer.MAX_VALUE;
                //因为这里的最大值是请求的数量，需要减去已经拿走了的部分
                maxToStore -= tmp.collected;
                //最大值不能超过剩余的
                maxToStore = Math.min(maxToStore, rest);
                //确认本次是需要进行拿取的
                if (maxToStore > 0) {
                    rest -= maxToStore;
                    //更新需求表的已收集的值
                    int currentCollected = tmp.collected + maxToStore;
                    tmp.collected = currentCollected;
                    list.set(i, tmp);
                    //如果已经收集了全部，那么标记为完成
                    if (currentCollected >= requestedCount && requestedCount != -1) {
                        tmp.done = true;
                        list.set(i, tmp);
                    }
                }
                if (rest <= 0) break;
            }
        }
        stack.set(DataComponentRegistry.REQUEST_ITEMS, request.toImmutable());
        //黑名单情况，如果
        if (request.isBlackList()) {
            rest = 0;
        }
        return collected.copyWithCount(nonCalc + rest);
    }

    public static int updateStored(ItemStack stack, ItemStack toStore, boolean simulate, boolean isInCrafting) {
        if (!stack.is(ItemRegistry.REQUEST_LIST_ITEM.get())) return toStore.getCount();
        RequestItemStackList request = getMutableRequestData(stack);
        if (request == null) return toStore.getCount();

        //从剩余的数量中进行计算
        int rest = toStore.getCount();

        List<RequestItemStackList.ListItem> list = request.getList();
        for (int i = 0; i < list.size(); i++) {
            RequestItemStackList.ListItem tmp = list.get(i);

            ItemStack target = tmp.item;
            int collected = tmp.collected;
            int stored = tmp.stored;
            if (stored >= collected) continue;
            if (ItemStackUtil.isSame(toStore, target, getMatchType(stack, isInCrafting))) {
                //黑名单物品不进行存储
                if (request.blackList) return rest;
                int maxToStore = collected - stored;
                maxToStore = Math.min(maxToStore, rest);
                if (maxToStore > 0) {
                    rest -= maxToStore;
                    if (!simulate) tmp.stored = stored + maxToStore;
                    list.set(i, tmp);
                }
            }
            if (rest <= 0) break;
        }
        stack.set(DataComponentRegistry.REQUEST_ITEMS, request.toImmutable());
        if (request.blackList) {
            rest = 0;
        }
        return rest;
    }

    public static void updateCollectedNotStored(ItemStack stack, IItemHandler tmpStorage) {
        if (!stack.is(ItemRegistry.REQUEST_LIST_ITEM.get())) return;
        RequestItemStackList request = getMutableRequestData(stack);
        if (request == null) return;
        List<RequestItemStackList.ListItem> list = request.getList();
        for (RequestItemStackList.ListItem tmp : list) {
            ItemStack target = tmp.item;
            int requested = tmp.requested;
            int collected = tmp.collected;
            int stored = tmp.stored;
            if (stored >= collected) continue;
            int count = 0;
            for (int j = 0; j < tmpStorage.getSlots(); j++) {
                ItemStack itemStack = tmpStorage.getStackInSlot(j);
                if (ItemStackUtil.isSame(itemStack, target, getMatchType(stack))) {
                    count += itemStack.getCount();
                }
            }
            tmp.collected = (stored + Math.min(requested - stored, count));
        }
        stack.set(DataComponentRegistry.REQUEST_ITEMS, request.toImmutable());
    }

    public static void setFailAddition(ItemStack stack, ItemStack item, String failAddition) {
        if (!stack.is(ItemRegistry.REQUEST_LIST_ITEM.get())) return;
        RequestItemStackList request = getMutableRequestData(stack);
        if (request == null) return;
        List<RequestItemStackList.ListItem> list = request.getList();
        for (int i = 0; i < list.size(); i++) {
            RequestItemStackList.ListItem tmp = list.get(i);
            if (ItemStack.isSameItemSameComponents(tmp.item, item)) {
                tmp.failAddition = failAddition;
                list.set(i, tmp);
            }
        }
        stack.set(DataComponentRegistry.REQUEST_ITEMS, request.toImmutable());
    }

    public static void markAllDone(ItemStack stack) {
        if (!stack.is(ItemRegistry.REQUEST_LIST_ITEM.get())) return;
        RequestItemStackList request = getMutableRequestData(stack);
        if (request == null) return;
        List<RequestItemStackList.ListItem> list = request.getList();
        for (int i = 0; i < list.size(); i++) {
            list.get(i).done = true;
        }
        request.blacklistDone = true;
        stack.set(DataComponentRegistry.REQUEST_ITEMS, request.toImmutable());
    }

    public static @NotNull UUID getUUID(ItemStack stack) {
        if (!stack.is(ItemRegistry.REQUEST_LIST_ITEM.get())) return UUID.randomUUID();
        if (!stack.has(DataComponentRegistry.REQUEST_WORK_UUID))
            stack.set(DataComponentRegistry.REQUEST_WORK_UUID, UUID.randomUUID());
        return Objects.requireNonNull(stack.get(DataComponentRegistry.REQUEST_WORK_UUID));
    }

    public static void setMissingItem(ItemStack itemStack, ItemStack item, List<ItemStack> missing) {
        if (!itemStack.is(ItemRegistry.REQUEST_LIST_ITEM.get())) return;
        RequestItemStackList request = getMutableRequestData(itemStack);
        if (request == null) return;
        List<RequestItemStackList.ListItem> list = request.getList();
        for (int i = 0; i < list.size(); i++) {
            RequestItemStackList.ListItem tmp = list.get(i);
            if (ItemStack.isSameItemSameComponents(tmp.item, item)) {
                for (ItemStack ti : missing) {
                    if (ti.isEmpty()) continue;
                    if (ti.is(ItemRegistry.REQUEST_LIST_ITEM.get()))
                        continue;//TODO: 验证问题是否任然存在
                    int idx = -1;
                    for (int j = 0; j < tmp.missing.size(); j++) {
                        if (ItemStack.isSameItemSameComponents(tmp.missing.get(j), ti)) idx = j;
                    }
                    if (idx != -1) {
                        tmp.missing.get(idx).grow(ti.getCount());
                    } else if (tmp.missing.size() < 15)
                        tmp.missing.add(ti.copy());
                }
                break;
            }
        }
        itemStack.set(DataComponentRegistry.REQUEST_ITEMS, request.toImmutable());
    }

    public static boolean isAllSuccess(ItemStack stack) {
        if (!stack.is(ItemRegistry.REQUEST_LIST_ITEM.get())) return false;
        RequestItemStackList.Immutable request = getImmutableRequestData(stack);
        if (request == null) return false;
        if (request.blackList()) return request.blacklistDone();
        List<RequestItemStackList.ImmutableItem> list = request.list();
        for (RequestItemStackList.ImmutableItem tmp : list) {
            if (tmp.item().isEmpty()) continue;
            if (tmp.requested() == -1) continue;
            if (tmp.collected() < tmp.requested()) return false;
        }
        return true;
    }

    public static boolean isStockMode(ItemStack stack) {
        if (!stack.is(ItemRegistry.REQUEST_LIST_ITEM.get())) return false;
        RequestItemStackList.Immutable request = getImmutableRequestData(stack);
        if (request == null) return false;
        return request.stockMode();
    }

    public static boolean hasCheckedStock(ItemStack stack) {
        if (!stack.is(ItemRegistry.REQUEST_LIST_ITEM.get())) return false;
        RequestItemStackList.Immutable request = getImmutableRequestData(stack);
        if (request == null) return false;
        return request.stockModeChecked();
    }

    public static void setHasCheckedStock(ItemStack stack, boolean has) {
        if (!stack.is(ItemRegistry.REQUEST_LIST_ITEM.get())) return;
        RequestItemStackList request = getMutableRequestData(stack);
        if (request == null) return;
        request.stockModeChecked = has;
        stack.set(DataComponentRegistry.REQUEST_ITEMS, request.toImmutable());
    }

    public static boolean isVirtual(ItemStack stack) {
        if (!stack.is(ItemRegistry.REQUEST_LIST_ITEM.get())) return false;
        return stack.getOrDefault(DataComponentRegistry.REQUEST_VIRTUAL, false);
    }

    public static boolean isBlackMode(ItemStack stack) {
        if (!stack.is(ItemRegistry.REQUEST_LIST_ITEM.get())) return false;
        RequestItemStackList.Immutable request = getImmutableRequestData(stack);
        if (request == null) return false;
        return request.blackList();
    }

    public static boolean isBlackModeDone(ItemStack stack) {
        if (!stack.is(ItemRegistry.REQUEST_LIST_ITEM.get())) return false;
        RequestItemStackList.Immutable request = getImmutableRequestData(stack);
        if (request == null) return false;
        return request.blacklistDone();
    }

    public static void setVirtualData(ItemStack stack, CompoundTag data) {
        stack.set(DataComponentRegistry.REQUEST_VIRTUAL_DATA, data);
    }

    public static CompoundTag getVirtualData(ItemStack stack) {
        if (stack.has(DataComponentRegistry.REQUEST_VIRTUAL_DATA))
            return stack.get(DataComponentRegistry.REQUEST_VIRTUAL_DATA);
        return new CompoundTag();
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
