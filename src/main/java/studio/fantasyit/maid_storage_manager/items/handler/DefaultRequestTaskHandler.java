package studio.fantasyit.maid_storage_manager.items.handler;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.CombinedResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.api.IRequestTaskHandler;
import studio.fantasyit.maid_storage_manager.items.data.RequestItemStackList;
import studio.fantasyit.maid_storage_manager.maid.memory.AbstractTargetMemory;
import studio.fantasyit.maid_storage_manager.registry.DataComponentRegistry;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class DefaultRequestTaskHandler implements IRequestTaskHandler {

    public static final DefaultRequestTaskHandler INSTANCE = new DefaultRequestTaskHandler();

    private DefaultRequestTaskHandler() {
    }

    @Override
    public @NotNull RequestItemStackList getMutableRequestData(ItemStack stack) {
        return getImmutableRequestData(stack).toMutable();
    }

    @Override
    public RequestItemStackList.@NotNull Immutable getImmutableRequestData(ItemStack stack) {
        if (!stack.has(DataComponentRegistry.REQUEST_ITEMS))
            stack.set(DataComponentRegistry.REQUEST_ITEMS.get(), new RequestItemStackList().toImmutable());
        return Objects.requireNonNull(stack.get(DataComponentRegistry.REQUEST_ITEMS.get()));
    }

    @Override
    public ItemStackUtil.MATCH_TYPE getMatchType(ItemStack stack) {
        return ItemStackUtil.MATCH_TYPE.values()[stack.getOrDefault(DataComponentRegistry.REQUEST_MATCHING.get(), 0)];
    }

    @Override
    public ItemStackUtil.MATCH_TYPE getMatchType(ItemStack stack, boolean crafting) {
        if (crafting) return ItemStackUtil.MATCH_TYPE.AUTO;
        return getMatchType(stack);
    }

    @Override
    public boolean isIgnored(ItemStack stack) {
        return stack.getOrDefault(DataComponentRegistry.REQUEST_IGNORE.get(), false);
    }

    @Override
    public void setIgnore(ItemStack stack) {
        stack.set(DataComponentRegistry.REQUEST_IGNORE.get(), true);
    }

    @Override
    public int getRepeatInterval(ItemStack stack) {
        return Optional.ofNullable(stack.get(DataComponentRegistry.REQUEST_INTERVAL.get())).orElse(0);
    }

    @Override
    public int getRepeatCd(ItemStack stack) {
        return Optional.ofNullable(stack.get(DataComponentRegistry.REQUEST_CD.get())).orElse(0);
    }

    @Override
    public void addItemStackCollected(ItemStack stack, ItemStack item, int count) {
        RequestItemStackList request = getMutableRequestData(stack);
        for (RequestItemStackList.ListItem listItem : request.list) {
            if (!ItemStackUtil.isSame(listItem.getItem(), item, getMatchType(stack))) continue;
            listItem.collected += count;
            if (listItem.collected > listItem.requested && listItem.requested != -1) {
                listItem.done = true;
            }
        }
        stack.set(DataComponentRegistry.REQUEST_ITEMS, request.toImmutable());
    }

    @Override
    public void clearItemProcess(ItemStack stack) {
        RequestItemStackList request = getMutableRequestData(stack);
        for (RequestItemStackList.ListItem tmp : request.list) {
            tmp.collected = 0;
            tmp.stored = 0;
            tmp.done = false;
            tmp.failAddition = "";
            tmp.missing.clear();
        }
        request.blacklistDone = false;
        request.stockModeChecked = false;
        stack.set(DataComponentRegistry.REQUEST_ITEMS.get(), request.toImmutable());
        stack.set(DataComponentRegistry.REQUEST_IGNORE.get(), false);
        stack.set(DataComponentRegistry.REQUEST_FAIL_ADDITION.get(), "");
        stack.set(DataComponentRegistry.REQUEST_WORK_UUID.get(), UUID.randomUUID());
        stack.set(DataComponentRegistry.REQUEST_CD.get(), 0);
    }

    @Override
    public void clearAllNonSuccess(ItemStack stack) {
        RequestItemStackList request = getMutableRequestData(stack);
        for (RequestItemStackList.ListItem tmp : request.list) {
            tmp.done = tmp.collected >= tmp.requested || tmp.requested == 0;
        }
        stack.set(DataComponentRegistry.REQUEST_ITEMS.get(), request.toImmutable());
        stack.set(DataComponentRegistry.REQUEST_FAIL_ADDITION.get(), "");
        stack.set(DataComponentRegistry.REQUEST_CD.get(), 0);
        stack.set(DataComponentRegistry.REQUEST_IGNORE.get(), false);
    }

    @Override
    public boolean isCoolingDown(ItemStack stack) {
        Integer cd = stack.get(DataComponentRegistry.REQUEST_CD.get());
        return cd != null && cd > 0;
    }

    @Override
    public void tickCoolingDown(ItemStack stack) {
        Integer cd = stack.get(DataComponentRegistry.REQUEST_CD.get());
        if (cd != null && cd > 0) {
            cd--;
            stack.set(DataComponentRegistry.REQUEST_CD.get(), cd);
            if (cd == 0) {
                clearItemProcess(stack);
            }
        }
    }

    @Override
    public void markDone(ItemStack stack, ItemStack target) {
        RequestItemStackList request = getMutableRequestData(stack);
        for (RequestItemStackList.ListItem item : request.list) {
            if (!ItemStack.isSameItemSameComponents(item.item, target)) continue;
            item.done = true;
        }
        stack.set(DataComponentRegistry.REQUEST_ITEMS.get(), request.toImmutable());
    }

    @Override
    public ItemStack updateCollectedItem(ItemStack stack, ItemStack collected, int maxCollect, boolean isInCrafting) {
        RequestItemStackList request = getMutableRequestData(stack);
        int nonCalc = Math.max(0, collected.getCount() - maxCollect);
        int rest = collected.getCount() - nonCalc;
        for (int i = 0; i < request.list.size(); i++) {
            RequestItemStackList.ListItem tmp = request.list.get(i);
            if (tmp.done) continue;
            ItemStack requested = tmp.getItem();
            if (ItemStackUtil.isSame(collected, requested, getMatchType(stack, isInCrafting))) {
                if (request.blackList) return collected;
                int maxToStore = tmp.requested;
                if (maxToStore == -1) maxToStore = Integer.MAX_VALUE;
                maxToStore -= tmp.collected;
                maxToStore = Math.min(maxToStore, rest);
                if (maxToStore > 0) {
                    rest -= maxToStore;
                    int currentCollected = tmp.collected + maxToStore;
                    tmp.collected = currentCollected;
                    request.list.set(i, tmp);
                    if (currentCollected >= tmp.requested && tmp.requested != -1) {
                        tmp.done = true;
                        request.list.set(i, tmp);
                    }
                }
                if (rest <= 0) break;
            }
        }
        stack.set(DataComponentRegistry.REQUEST_ITEMS, request.toImmutable());
        if (request.isBlackList()) rest = 0;
        return collected.copyWithCount(nonCalc + rest);
    }

    @Override
    public int updateStored(ItemStack stack, ItemStack toStore, boolean simulate, boolean isInCrafting) {
        RequestItemStackList request = getMutableRequestData(stack);
        int rest = toStore.getCount();
        for (int i = 0; i < request.list.size(); i++) {
            RequestItemStackList.ListItem tmp = request.list.get(i);
            if (tmp.stored >= tmp.collected) continue;
            if (ItemStackUtil.isSame(toStore, tmp.item, getMatchType(stack, isInCrafting))) {
                if (request.blackList) return rest;
                int maxToStore = Math.min(tmp.collected - tmp.stored, rest);
                if (maxToStore > 0) {
                    rest -= maxToStore;
                    if (!simulate) tmp.stored += maxToStore;
                    request.list.set(i, tmp);
                }
            }
            if (rest <= 0) break;
        }
        stack.set(DataComponentRegistry.REQUEST_ITEMS, request.toImmutable());
        if (request.blackList) rest = 0;
        return rest;
    }

    @Override
    public void updateCollectedNotStored(ItemStack stack, CombinedResourceHandler<ItemResource> tmpStorage) {
        RequestItemStackList request = getMutableRequestData(stack);
        for (RequestItemStackList.ListItem tmp : request.list) {
            if (tmp.stored >= tmp.collected) continue;
            int count = 0;
            for (int j = 0; j < tmpStorage.size(); j++) {
                ItemStack itemStack = ItemUtil.getStack(tmpStorage, j);
                if (ItemStackUtil.isSame(itemStack, tmp.item, getMatchType(stack))) {
                    count += itemStack.getCount();
                }
            }
            tmp.collected = tmp.stored + Math.min(tmp.requested - tmp.stored, count);
        }
        stack.set(DataComponentRegistry.REQUEST_ITEMS, request.toImmutable());
    }

    @Override
    public void setFailAddition(ItemStack stack, ItemStack item, String failAddition) {
        RequestItemStackList request = getMutableRequestData(stack);
        for (int i = 0; i < request.list.size(); i++) {
            if (ItemStack.isSameItemSameComponents(request.list.get(i).item, item)) {
                request.list.get(i).failAddition = failAddition;
            }
        }
        stack.set(DataComponentRegistry.REQUEST_ITEMS, request.toImmutable());
    }

    @Override
    public void markAllDone(ItemStack stack) {
        RequestItemStackList request = getMutableRequestData(stack);
        for (int i = 0; i < request.list.size(); i++) {
            request.list.get(i).done = true;
        }
        request.blacklistDone = true;
        stack.set(DataComponentRegistry.REQUEST_ITEMS, request.toImmutable());
    }

    @Override
    public @NotNull UUID getWorkUUID(ItemStack stack) {
        if (!stack.has(DataComponentRegistry.REQUEST_WORK_UUID))
            stack.set(DataComponentRegistry.REQUEST_WORK_UUID, UUID.randomUUID());
        return Objects.requireNonNull(stack.get(DataComponentRegistry.REQUEST_WORK_UUID));
    }

    @Override
    public void setMissingItem(ItemStack stack, ItemStack item, List<ItemStack> missing) {
        RequestItemStackList request = getMutableRequestData(stack);
        for (int i = 0; i < request.list.size(); i++) {
            RequestItemStackList.ListItem tmp = request.list.get(i);
            if (ItemStack.isSameItemSameComponents(tmp.item, item)) {
                for (ItemStack ti : missing) {
                    if (ti.isEmpty()) continue;
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
        stack.set(DataComponentRegistry.REQUEST_ITEMS, request.toImmutable());
    }

    @Override
    public boolean isAllSuccess(ItemStack stack) {
        RequestItemStackList.Immutable request = getImmutableRequestData(stack);
        if (request.blackList()) return request.blacklistDone();
        for (RequestItemStackList.ImmutableItem tmp : request.list()) {
            if (tmp.item().isEmpty()) continue;
            if (tmp.requested() == -1) continue;
            if (tmp.collected() < tmp.requested()) return false;
        }
        return true;
    }

    @Override
    public boolean isAllStored(ItemStack stack) {
        RequestItemStackList.Immutable request = getImmutableRequestData(stack);
        return request.list().stream().noneMatch(t -> {
            if (t.item().isEmpty()) return false;
            if (!t.done()) return false;
            return t.collected() > t.stored();
        });
    }

    @Override
    public List<Pair<ItemStack, Integer>> getItemStacksNotDone(ItemStack stack) {
        return getItemStacksNotDone(stack, true);
    }

    @Override
    public List<Pair<ItemStack, Integer>> getItemStacksNotDone(ItemStack stack, boolean includingNoRequest) {
        RequestItemStackList.Immutable request = getImmutableRequestData(stack);
        if (request.blackList()) return List.of();
        return request.list().stream()
                .filter(t -> !t.done())
                .filter(t -> t.requested() != -1 || includingNoRequest)
                .map(t -> {
                    int cnt = t.requested();
                    if (cnt != -1) cnt -= t.collected();
                    return new Pair<>(t.item(), cnt);
                })
                .filter(i -> !i.getA().isEmpty())
                .toList();
    }

    @Override
    public @Nullable UUID getStorageEntity(ItemStack stack) {
        return stack.get(DataComponentRegistry.REQUEST_STORAGE_ENTITY);
    }

    @Override
    public @Nullable Target getStorageBlock(ItemStack stack) {
        return stack.get(DataComponentRegistry.REQUEST_STORAGE_BLOCK);
    }

    @Override
    public boolean hasAnyStorage(ItemStack stack) {
        Target storageBlock = getStorageBlock(stack);
        if (storageBlock != null && storageBlock.getType() != AbstractTargetMemory.TargetData.NO_TARGET) {
            return true;
        }
        return getStorageEntity(stack) != null;
    }

    @Override
    public boolean isStockMode(ItemStack stack) {
        return getImmutableRequestData(stack).stockMode();
    }

    @Override
    public boolean hasCheckedStock(ItemStack stack) {
        return getImmutableRequestData(stack).stockModeChecked();
    }

    @Override
    public void setHasCheckedStock(ItemStack stack, boolean has) {
        RequestItemStackList request = getMutableRequestData(stack);
        request.stockModeChecked = has;
        stack.set(DataComponentRegistry.REQUEST_ITEMS, request.toImmutable());
    }

    @Override
    public boolean isVirtual(ItemStack stack) {
        return stack.getOrDefault(DataComponentRegistry.REQUEST_VIRTUAL, false);
    }

    @Override
    public boolean isBlackMode(ItemStack stack) {
        return getImmutableRequestData(stack).blackList();
    }

    @Override
    public boolean isBlackModeDone(ItemStack stack) {
        return getImmutableRequestData(stack).blacklistDone();
    }

    @Override
    public void setVirtualData(ItemStack stack, CompoundTag data) {
        stack.set(DataComponentRegistry.REQUEST_VIRTUAL_DATA, data);
    }

    @Override
    public CompoundTag getVirtualData(ItemStack stack) {
        if (stack.has(DataComponentRegistry.REQUEST_VIRTUAL_DATA))
            return stack.get(DataComponentRegistry.REQUEST_VIRTUAL_DATA);
        return new CompoundTag();
    }
}
