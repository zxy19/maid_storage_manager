# RequestItemHandler Capability 迁移实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将 `RequestListItem` 的任务管理逻辑分离到 `IRequestTaskHandler` capability 接口中，使其他物品也能充当 request_list。

**Architecture:** 创建 `IRequestTaskHandler` capability 接口 + `DefaultRequestTaskHandler` 独立实现类。`RequestListItem` 和新的 `VirtualRequestListItem` 通过 `initCapabilities()` 返回该 handler。所有现有调用 `RequestListItem.xxx()` 的地方改为 `IRequestTaskHandler.of(stack).xxx(stack)`。`ItemStack.is(ItemRegistry.REQUEST_LIST_ITEM.get())` 检查改为 capability 查询。

**Tech Stack:** Java 21, NeoForge 1.21.1, Mojang mappings, Minecraft data components, Capability system

---

### Task 1: 创建 `RequestTask` record

**Files:**
- Create: `src/main/java/studio/fantasyit/maid_storage_manager/api/RequestTask.java`

- [ ] **Step 1: 创建 RequestTask record**

```java
package studio.fantasyit.maid_storage_manager.api;

import net.minecraft.world.item.ItemStack;

import java.util.List;

public record RequestTask(
        ItemStack item,
        int requested,
        int collected,
        int stored,
        boolean done,
        List<ItemStack> missing,
        String failAddition
) {}
```

- [ ] **Step 2: Build 验证编译通过**

Run: `gradlew build`

- [ ] **Step 3: Commit**

```bash
git add src/main/java/studio/fantasyit/maid_storage_manager/api/RequestTask.java
git commit -m "feat: add RequestTask record for capability API"
```

---

### Task 2: 创建 `IRequestTaskHandler` capability 接口

**Files:**
- Create: `src/main/java/studio/fantasyit/maid_storage_manager/api/IRequestTaskHandler.java`
- Modify: `src/main/java/studio/fantasyit/maid_storage_manager/registry/CapabilityRegistry.java`（新建）

- [ ] **Step 1: 创建 IRequestTaskHandler 接口**

`src/main/java/studio/fantasyit/maid_storage_manager/api/IRequestTaskHandler.java`:

```java
package studio.fantasyit.maid_storage_manager.api;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capability;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.items.data.RequestItemStackList;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;

import java.util.List;
import java.util.UUID;

public interface IRequestTaskHandler {

    static @Nullable IRequestTaskHandler of(ItemStack stack) {
        return stack.getCapability(CAPABILITY);
    }

    // --- 基础数据访问 ---
    RequestItemStackList.@NotNull Immutable getImmutableRequestData(ItemStack stack);
    @NotNull RequestItemStackList getMutableRequestData(ItemStack stack);

    // --- 匹配模式 ---
    ItemStackUtil.MATCH_TYPE getMatchType(ItemStack stack);
    ItemStackUtil.MATCH_TYPE getMatchType(ItemStack stack, boolean crafting);

    // --- 忽略/CD/重复 ---
    boolean isIgnored(ItemStack stack);
    void setIgnore(ItemStack stack);
    int getRepeatInterval(ItemStack stack);
    int getRepeatCd(ItemStack stack);
    boolean isCoolingDown(ItemStack stack);
    void tickCoolingDown(ItemStack stack);

    // --- 进度操作 ---
    void addItemStackCollected(ItemStack stack, ItemStack item, int count);
    void clearItemProcess(ItemStack stack);
    void clearAllNonSuccess(ItemStack stack);
    void markDone(ItemStack stack, ItemStack target);
    void markAllDone(ItemStack stack);
    ItemStack updateCollectedItem(ItemStack stack, ItemStack collected, int maxCollect, boolean isInCrafting);
    int updateStored(ItemStack stack, ItemStack toStore, boolean simulate, boolean isInCrafting);
    void updateCollectedNotStored(ItemStack stack, IItemHandler tmpStorage);
    void setFailAddition(ItemStack stack, ItemStack item, String failAddition);

    // --- 合成相关 ---
    void setMissingItem(ItemStack stack, ItemStack item, List<ItemStack> missing);

    // --- 状态查询 ---
    boolean isAllSuccess(ItemStack stack);
    boolean isAllStored(ItemStack stack);
    List<Pair<ItemStack, Integer>> getItemStacksNotDone(ItemStack stack);
    List<Pair<ItemStack, Integer>> getItemStacksNotDone(ItemStack stack, boolean includingNoRequest);

    // --- 存储目标 ---
    @Nullable UUID getStorageEntity(ItemStack stack);
    @Nullable Target getStorageBlock(ItemStack stack);
    boolean hasAnyStorage(ItemStack stack);

    // --- 工作UUID ---
    @NotNull UUID getWorkUUID(ItemStack stack);

    // --- 存量模式 ---
    boolean isStockMode(ItemStack stack);
    boolean hasCheckedStock(ItemStack stack);
    void setHasCheckedStock(ItemStack stack, boolean has);

    // --- 虚拟请求 ---
    boolean isVirtual(ItemStack stack);

    // --- 黑名单模式 ---
    boolean isBlackMode(ItemStack stack);
    boolean isBlackModeDone(ItemStack stack);

    // --- 虚拟数据 ---
    void setVirtualData(ItemStack stack, CompoundTag data);
    CompoundTag getVirtualData(ItemStack stack);

    // --- Capability 注册 ---
    Capability<IRequestTaskHandler> CAPABILITY = Capability.createVoid(
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(
                    studio.fantasyit.maid_storage_manager.MaidStorageManager.MODID,
                    "request_task_handler"
            ),
            IRequestTaskHandler.class
    );
}
```

- [ ] **Step 2: Build 验证**

Run: `gradlew build`

- [ ] **Step 3: Commit**

```bash
git add src/main/java/studio/fantasyit/maid_storage_manager/api/IRequestTaskHandler.java
git commit -m "feat: add IRequestTaskHandler capability interface"
```

---

### Task 3: 注册 capability

**Files:**
- Create: `src/main/java/studio/fantasyit/maid_storage_manager/registry/CapabilityRegistry.java`
- Modify: `src/main/java/studio/fantasyit/maid_storage_manager/MaidStorageManager.java`（在构造中调用注册）

- [ ] **Step 1: 创建 CapabilityRegistry**

（实际上 NeoForge 的 Capability 已通过 `Capability.createVoid` 在接口中注册，不需要额外的 Registry 类。此任务转为确认 MaidStorageManager 的主构造中模组事件总线已正确设置即可，无需新建文件。）

跳过此 Task，Capability 已在接口的静态字段中直接创建。

- [ ] **Step 1: Commit (no-op, capability is self-registering)**

```bash
git commit --allow-empty -m "chore: capability is self-registering via static field, no registry needed"
```

---

### Task 4: 创建 `DefaultRequestTaskHandler` 实现类

**Files:**
- Create: `src/main/java/studio/fantasyit/maid_storage_manager/items/handler/DefaultRequestTaskHandler.java`

将 `RequestListItem` 中所有 ~25 个静态方法的主体逻辑迁入此类，但不复制 UI 方法（`use()`、`interactLivingEntity()`、`useOn()`、`appendHoverText()`、`createMenu()`、`getDisplayName()`）。

关键变更：**删除所有 `if (!stack.is(ItemRegistry.REQUEST_LIST_ITEM.get()))` 守卫**，因为 capability 检查已由调用方完成。

- [ ] **Step 1: 创建 DefaultRequestTaskHandler**

`src/main/java/studio/fantasyit/maid_storage_manager/items/handler/DefaultRequestTaskHandler.java`:

```java
package studio.fantasyit.maid_storage_manager.items.handler;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.api.IRequestTaskHandler;
import studio.fantasyit.maid_storage_manager.items.data.RequestItemStackList;
import studio.fantasyit.maid_storage_manager.registry.DataComponentRegistry;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class DefaultRequestTaskHandler implements IRequestTaskHandler {

    public static final DefaultRequestTaskHandler INSTANCE = new DefaultRequestTaskHandler();

    private DefaultRequestTaskHandler() {}

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
        return Optional.ofNullable(
                stack.get(DataComponentRegistry.REQUEST_INTERVAL.get())
        ).orElse(0);
    }

    @Override
    public int getRepeatCd(ItemStack stack) {
        return Optional.ofNullable(
                stack.get(DataComponentRegistry.REQUEST_CD.get())
        ).orElse(0);
    }

    @Override
    public void addItemStackCollected(ItemStack stack, ItemStack item, int count) {
        RequestItemStackList request = getMutableRequestData(stack);
        List<RequestItemStackList.ListItem> items = request.getList();
        for (RequestItemStackList.ListItem listItem : items) {
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
        stack.set(DataComponentRegistry.REQUEST_ITEMS.get(), request.toImmutable());
        stack.set(DataComponentRegistry.REQUEST_IGNORE.get(), false);
        stack.set(DataComponentRegistry.REQUEST_FAIL_ADDITION.get(), "");
        stack.set(DataComponentRegistry.REQUEST_WORK_UUID.get(), UUID.randomUUID());
        stack.set(DataComponentRegistry.REQUEST_CD.get(), 0);
    }

    @Override
    public void clearAllNonSuccess(ItemStack stack) {
        RequestItemStackList request = getMutableRequestData(stack);
        List<RequestItemStackList.ListItem> list = request.getList();
        for (RequestItemStackList.ListItem tmp : list) {
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
        List<RequestItemStackList.ListItem> items = request.getList();
        for (RequestItemStackList.ListItem item : items) {
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
        List<RequestItemStackList.ListItem> list = request.getList();
        for (int i = 0; i < list.size(); i++) {
            RequestItemStackList.ListItem tmp = list.get(i);
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
                    list.set(i, tmp);
                    if (currentCollected >= tmp.requested && tmp.requested != -1) {
                        tmp.done = true;
                        list.set(i, tmp);
                    }
                }
                if (rest <= 0) break;
            }
        }
        stack.set(DataComponentRegistry.REQUEST_ITEMS, request.toImmutable());
        if (request.isBlackList()) {
            rest = 0;
        }
        return collected.copyWithCount(nonCalc + rest);
    }

    @Override
    public int updateStored(ItemStack stack, ItemStack toStore, boolean simulate, boolean isInCrafting) {
        RequestItemStackList request = getMutableRequestData(stack);
        int rest = toStore.getCount();
        List<RequestItemStackList.ListItem> list = request.getList();
        for (int i = 0; i < list.size(); i++) {
            RequestItemStackList.ListItem tmp = list.get(i);
            ItemStack target = tmp.item;
            int collected = tmp.collected;
            int stored = tmp.stored;
            if (stored >= collected) continue;
            if (ItemStackUtil.isSame(toStore, target, getMatchType(stack, isInCrafting))) {
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

    @Override
    public void updateCollectedNotStored(ItemStack stack, IItemHandler tmpStorage) {
        RequestItemStackList request = getMutableRequestData(stack);
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

    @Override
    public void setFailAddition(ItemStack stack, ItemStack item, String failAddition) {
        RequestItemStackList request = getMutableRequestData(stack);
        List<RequestItemStackList.ListItem> list = request.getList();
        for (int i = 0; i < list.size(); i++) {
            if (ItemStack.isSameItemSameComponents(list.get(i).item, item)) {
                list.get(i).failAddition = failAddition;
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
        List<RequestItemStackList.ListItem> list = request.getList();
        for (int i = 0; i < list.size(); i++) {
            RequestItemStackList.ListItem tmp = list.get(i);
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
        List<RequestItemStackList.ImmutableItem> list = request.list();
        for (RequestItemStackList.ImmutableItem tmp : list) {
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
        if (storageBlock != null && storageBlock.getType() != studio.fantasyit.maid_storage_manager.maid.memory.AbstractTargetMemory.TargetData.NO_TARGET) {
            return true;
        }
        return getStorageEntity(stack) != null;
    }

    @Override
    public boolean isStockMode(ItemStack stack) {
        RequestItemStackList.Immutable request = getImmutableRequestData(stack);
        return request.stockMode();
    }

    @Override
    public boolean hasCheckedStock(ItemStack stack) {
        RequestItemStackList.Immutable request = getImmutableRequestData(stack);
        return request.stockModeChecked();
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
        RequestItemStackList.Immutable request = getImmutableRequestData(stack);
        return request.blackList();
    }

    @Override
    public boolean isBlackModeDone(ItemStack stack) {
        RequestItemStackList.Immutable request = getImmutableRequestData(stack);
        return request.blacklistDone();
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
```

- [ ] **Step 2: Build 验证**

Run: `gradlew build`

- [ ] **Step 3: Commit**

```bash
git add src/main/java/studio/fantasyit/maid_storage_manager/items/handler/DefaultRequestTaskHandler.java
git commit -m "feat: add DefaultRequestTaskHandler implementing IRequestTaskHandler"
```

---

### Task 5: 创建 `VirtualRequestListItem`

**Files:**
- Create: `src/main/java/studio/fantasyit/maid_storage_manager/items/VirtualRequestListItem.java`
- Modify: `src/main/java/studio/fantasyit/maid_storage_manager/registry/ItemRegistry.java`

- [ ] **Step 1: 创建 VirtualRequestListItem**

`src/main/java/studio/fantasyit/maid_storage_manager/items/VirtualRequestListItem.java`:

```java
package studio.fantasyit.maid_storage_manager.items;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import oshi.util.tuples.Pair;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.api.IRequestTaskHandler;
import studio.fantasyit.maid_storage_manager.items.data.RequestItemStackList;
import studio.fantasyit.maid_storage_manager.items.handler.DefaultRequestTaskHandler;
import studio.fantasyit.maid_storage_manager.registry.DataComponentRegistry;

public class VirtualRequestListItem extends MaidInteractItem implements IRequestTaskHandler {

    public VirtualRequestListItem() {
        super(new Properties()
                .stacksTo(1)
                .component(DataComponentRegistry.REQUEST_ITEMS.get(), new RequestItemStackList().toImmutable())
                .component(DataComponentRegistry.REQUEST_VIRTUAL.get(), true)
        );
    }

    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new ICapabilityProvider() {
            @Override
            public <T> @Nullable T getCapability(net.neoforged.neoforge.capabilities.Capability<T> capability, @Nullable net.minecraft.core.Direction side) {
                if (capability == IRequestTaskHandler.CAPABILITY) {
                    return capability.cast(DefaultRequestTaskHandler.INSTANCE);
                }
                return null;
            }
        };
    }

    // 委托给 DefaultRequestTaskHandler
    @Override public @Nullable IRequestTaskHandler of(ItemStack stack) { return IRequestTaskHandler.of(stack); }
    @Override public RequestItemStackList.@javax.annotation.Nonnull Immutable getImmutableRequestData(ItemStack stack) { return DefaultRequestTaskHandler.INSTANCE.getImmutableRequestData(stack); }
    @Override public @javax.annotation.Nonnull RequestItemStackList getMutableRequestData(ItemStack stack) { return DefaultRequestTaskHandler.INSTANCE.getMutableRequestData(stack); }
    @Override public studio.fantasyit.maid_storage_manager.util.ItemStackUtil.MATCH_TYPE getMatchType(ItemStack stack) { return DefaultRequestTaskHandler.INSTANCE.getMatchType(stack); }
    @Override public studio.fantasyit.maid_storage_manager.util.ItemStackUtil.MATCH_TYPE getMatchType(ItemStack stack, boolean crafting) { return DefaultRequestTaskHandler.INSTANCE.getMatchType(stack, crafting); }
    @Override public boolean isIgnored(ItemStack stack) { return DefaultRequestTaskHandler.INSTANCE.isIgnored(stack); }
    @Override public void setIgnore(ItemStack stack) { DefaultRequestTaskHandler.INSTANCE.setIgnore(stack); }
    @Override public int getRepeatInterval(ItemStack stack) { return DefaultRequestTaskHandler.INSTANCE.getRepeatInterval(stack); }
    @Override public int getRepeatCd(ItemStack stack) { return DefaultRequestTaskHandler.INSTANCE.getRepeatCd(stack); }
    @Override public boolean isCoolingDown(ItemStack stack) { return DefaultRequestTaskHandler.INSTANCE.isCoolingDown(stack); }
    @Override public void tickCoolingDown(ItemStack stack) { DefaultRequestTaskHandler.INSTANCE.tickCoolingDown(stack); }
    @Override public void addItemStackCollected(ItemStack stack, ItemStack item, int count) { DefaultRequestTaskHandler.INSTANCE.addItemStackCollected(stack, item, count); }
    @Override public void clearItemProcess(ItemStack stack) { DefaultRequestTaskHandler.INSTANCE.clearItemProcess(stack); }
    @Override public void clearAllNonSuccess(ItemStack stack) { DefaultRequestTaskHandler.INSTANCE.clearAllNonSuccess(stack); }
    @Override public void markDone(ItemStack stack, ItemStack target) { DefaultRequestTaskHandler.INSTANCE.markDone(stack, target); }
    @Override public void markAllDone(ItemStack stack) { DefaultRequestTaskHandler.INSTANCE.markAllDone(stack); }
    @Override public ItemStack updateCollectedItem(ItemStack stack, ItemStack collected, int maxCollect, boolean isInCrafting) { return DefaultRequestTaskHandler.INSTANCE.updateCollectedItem(stack, collected, maxCollect, isInCrafting); }
    @Override public int updateStored(ItemStack stack, ItemStack toStore, boolean simulate, boolean isInCrafting) { return DefaultRequestTaskHandler.INSTANCE.updateStored(stack, toStore, simulate, isInCrafting); }
    @Override public void updateCollectedNotStored(ItemStack stack, net.neoforged.neoforge.items.IItemHandler tmpStorage) { DefaultRequestTaskHandler.INSTANCE.updateCollectedNotStored(stack, tmpStorage); }
    @Override public void setFailAddition(ItemStack stack, ItemStack item, String failAddition) { DefaultRequestTaskHandler.INSTANCE.setFailAddition(stack, item, failAddition); }
    @Override public void setMissingItem(ItemStack stack, ItemStack item, java.util.List<ItemStack> missing) { DefaultRequestTaskHandler.INSTANCE.setMissingItem(stack, item, missing); }
    @Override public boolean isAllSuccess(ItemStack stack) { return DefaultRequestTaskHandler.INSTANCE.isAllSuccess(stack); }
    @Override public boolean isAllStored(ItemStack stack) { return DefaultRequestTaskHandler.INSTANCE.isAllStored(stack); }
    @Override public java.util.List<Pair<ItemStack, Integer>> getItemStacksNotDone(ItemStack stack) { return DefaultRequestTaskHandler.INSTANCE.getItemStacksNotDone(stack); }
    @Override public java.util.List<Pair<ItemStack, Integer>> getItemStacksNotDone(ItemStack stack, boolean includingNoRequest) { return DefaultRequestTaskHandler.INSTANCE.getItemStacksNotDone(stack, includingNoRequest); }
    @Override public @Nullable java.util.UUID getStorageEntity(ItemStack stack) { return DefaultRequestTaskHandler.INSTANCE.getStorageEntity(stack); }
    @Override public @Nullable studio.fantasyit.maid_storage_manager.storage.Target getStorageBlock(ItemStack stack) { return DefaultRequestTaskHandler.INSTANCE.getStorageBlock(stack); }
    @Override public boolean hasAnyStorage(ItemStack stack) { return DefaultRequestTaskHandler.INSTANCE.hasAnyStorage(stack); }
    @Override public @javax.annotation.Nonnull java.util.UUID getWorkUUID(ItemStack stack) { return DefaultRequestTaskHandler.INSTANCE.getWorkUUID(stack); }
    @Override public boolean isStockMode(ItemStack stack) { return DefaultRequestTaskHandler.INSTANCE.isStockMode(stack); }
    @Override public boolean hasCheckedStock(ItemStack stack) { return DefaultRequestTaskHandler.INSTANCE.hasCheckedStock(stack); }
    @Override public void setHasCheckedStock(ItemStack stack, boolean has) { DefaultRequestTaskHandler.INSTANCE.setHasCheckedStock(stack, has); }
    @Override public boolean isVirtual(ItemStack stack) { return true; }
    @Override public boolean isBlackMode(ItemStack stack) { return DefaultRequestTaskHandler.INSTANCE.isBlackMode(stack); }
    @Override public boolean isBlackModeDone(ItemStack stack) { return DefaultRequestTaskHandler.INSTANCE.isBlackModeDone(stack); }
    @Override public void setVirtualData(ItemStack stack, CompoundTag data) { DefaultRequestTaskHandler.INSTANCE.setVirtualData(stack, data); }
    @Override public CompoundTag getVirtualData(ItemStack stack) { return DefaultRequestTaskHandler.INSTANCE.getVirtualData(stack); }
}
```

Wait - this delegation is way too verbose. Let me reconsider. Since `VirtualRequestListItem` uses the same data components and delegates to `DefaultRequestTaskHandler.INSTANCE`, and the capability is registered via `initCapabilities`, the Item itself doesn't need to implement the interface. The `initCapabilities` is sufficient.

Let me simplify: `VirtualRequestListItem` does NOT implement `IRequestTaskHandler` directly. It only provides the capability via `initCapabilities`.

Updated VirtualRequestListItem:

```java
package studio.fantasyit.maid_storage_manager.items;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.api.IRequestTaskHandler;
import studio.fantasyit.maid_storage_manager.items.data.RequestItemStackList;
import studio.fantasyit.maid_storage_manager.items.handler.DefaultRequestTaskHandler;
import studio.fantasyit.maid_storage_manager.registry.DataComponentRegistry;

public class VirtualRequestListItem extends MaidInteractItem {

    public VirtualRequestListItem() {
        super(new Properties()
                .stacksTo(1)
                .component(DataComponentRegistry.REQUEST_ITEMS.get(), new RequestItemStackList().toImmutable())
                .component(DataComponentRegistry.REQUEST_VIRTUAL.get(), true)
        );
    }

    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new ICapabilityProvider() {
            @Override
            public <T> @Nullable T getCapability(net.neoforged.neoforge.capabilities.Capability<T> capability, @Nullable net.minecraft.core.Direction side) {
                if (capability == IRequestTaskHandler.CAPABILITY) {
                    return capability.cast(DefaultRequestTaskHandler.INSTANCE);
                }
                return null;
            }
        };
    }
}
```

Much cleaner! The Item class stays small.

- [ ] **Step 2: 在 ItemRegistry 注册 virtual_request_list**

在 `ItemRegistry.java` 中添加注册行：

```java
// 在 REQUEST_LIST_ITEM 注册之后添加:
public static final DeferredHolder<Item, VirtualRequestListItem> VIRTUAL_REQUEST_LIST_ITEM = item("virtual_request_list", VirtualRequestListItem::new);
```

- [ ] **Step 3: Build 验证**

Run: `gradlew build`

- [ ] **Step 4: Commit**

```bash
git add src/main/java/studio/fantasyit/maid_storage_manager/items/VirtualRequestListItem.java src/main/java/studio/fantasyit/maid_storage_manager/registry/ItemRegistry.java
git commit -m "feat: add VirtualRequestListItem, always virtual, no GUI"
```

---

### Task 6: 重构 `RequestListItem` — 添加 capability，删除静态方法

**Files:**
- Modify: `src/main/java/studio/fantasyit/maid_storage_manager/items/RequestListItem.java`

操作步骤：
1. 添加 `implements IRequestTaskHandler` 到类声明
2. 添加 `initCapabilities()` 方法（返回 DefaultRequestTaskHandler.INSTANCE）
3. 修改类内部的静态方法调用（`use()`, `appendHoverText()`, `interactLivingEntity()`, `useOn()`, `createMenu()` 中调用自身静态方法的地方改为通过 capability）
4. 删除所有 public static 方法（约25个）
5. 保留 UI/交互方法（`use`, `interactLivingEntity`, `useOn`, `appendHoverText`, `createMenu`, `getDisplayName`）

- [ ] **Step 1: 修改 RequestListItem 类声明和添加 initCapabilities**

```java
// 修改前 (line 40):
public class RequestListItem extends MaidInteractItem implements MenuProvider {

// 修改后:
public class RequestListItem extends MaidInteractItem implements MenuProvider, IRequestTaskHandler {
```

添加 import:
```java
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import studio.fantasyit.maid_storage_manager.api.IRequestTaskHandler;
import studio.fantasyit.maid_storage_manager.items.handler.DefaultRequestTaskHandler;
```

在 `getDisplayName()` 之前添加 `initCapabilities`:
```java
    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new ICapabilityProvider() {
            @Override
            public <T> @Nullable T getCapability(net.neoforged.neoforge.capabilities.Capability<T> capability, @Nullable net.minecraft.core.Direction side) {
                if (capability == IRequestTaskHandler.CAPABILITY) {
                    return capability.cast(DefaultRequestTaskHandler.INSTANCE);
                }
                return null;
            }
        };
    }
```

- [ ] **Step 2: Update internal callers to use `IRequestTaskHandler.of()`**

在 `RequestListItem` 中搜索所有自身静态方法调用并改为通过 `IRequestTaskHandler.of(stack)` 调用。

`use()` 方法中的 `isVirtual(serverPlayer.getMainHandItem())` (line 176):
```java
// Before:
if (!isVirtual(serverPlayer.getMainHandItem()))

// After:
if (!IRequestTaskHandler.of(serverPlayer.getMainHandItem()).isVirtual(serverPlayer.getMainHandItem()))
```

`interactLivingEntity()` 中的 `hasAnyStorage(itemStack)` (line 198):
```java
// Before:
if (!hasAnyStorage(itemStack)) {

// After:
IRequestTaskHandler handler = IRequestTaskHandler.of(itemStack);
if (handler != null && !handler.hasAnyStorage(itemStack)) {
```

`appendHoverText()` 中的 `getImmutableRequestData(itemStack)` (line 267) -- keep calling through capability:
```java
// Before:
RequestItemStackList.Immutable request = getImmutableRequestData(itemStack);

// After:
IRequestTaskHandler handler = IRequestTaskHandler.of(itemStack);
RequestItemStackList.Immutable request = handler != null ? handler.getImmutableRequestData(itemStack) : null;
```

`createMenu()` 中的 `isVirtual()` (line 609):
```java
// Before:
if (isVirtual(p_39956_.getItemInHand(InteractionHand.MAIN_HAND)))

// After:
ItemStack mainHand = p_39956_.getItemInHand(InteractionHand.MAIN_HAND);
IRequestTaskHandler handler = IRequestTaskHandler.of(mainHand);
if (handler != null && handler.isVirtual(mainHand))
```

- [ ] **Step 3: 删除所有 public static 方法**

删除 `getMutableRequestData`, `getImmutableRequestData`, `getMatchType` (2个重载), `isIgnored`, `setIgnore`, `getRepeatInterval`, `getRepeatCd`, `addItemStackCollected`, `clearItemProcess`, `clearAllNonSuccess`, `isCoolingDown`, `tickCoolingDown`, `markDone`, `isAllStored`, `getItemStacksNotDone` (2个重载), `getStorageEntity`, `getStorageBlock`, `hasAnyStorage`, `updateCollectedItem`, `updateStored`, `updateCollectedNotStored`, `setFailAddition`, `markAllDone`, `getUUID`, `setMissingItem`, `isAllSuccess`, `isStockMode`, `hasCheckedStock`, `setHasCheckedStock`, `isVirtual`, `isBlackMode`, `isBlackModeDone`, `setVirtualData`, `getVirtualData`.

- [ ] **Step 4: Build 验证**

Run: `gradlew build`

期望：大量编译错误，因为其他文件仍在调用已删除的静态方法。这是预期的——后续任务会逐个修复。

- [ ] **Step 5: Commit**

```bash
git add src/main/java/studio/fantasyit/maid_storage_manager/items/RequestListItem.java
git commit -m "refactor: remove static methods from RequestListItem, add capability"
```

---

### Task 7: 迁移 `Conditions.java`

**Files:**
- Modify: `src/main/java/studio/fantasyit/maid_storage_manager/util/Conditions.java`

每个 `RequestListItem.xxx(maid.getMainHandItem())` 调用替换为能力查询模式。

- [ ] **Step 1: 修改 import**

删除: `import studio.fantasyit.maid_storage_manager.items.RequestListItem;`
添加: `import studio.fantasyit.maid_storage_manager.api.IRequestTaskHandler;`

- [ ] **Step 2: 迁移 takingRequestList() (lines 23-31)**

```java
public static boolean takingRequestList(EntityMaid maid) {
    if (MemoryUtil.getCrafting(maid).isSwappingHandWhenCrafting() && MemoryUtil.getCrafting(maid).hasPlan())
        return true;
    ItemStack stack = maid.getMainHandItem();
    IRequestTaskHandler handler = IRequestTaskHandler.of(stack);
    if (handler == null)
        return false;
    if (handler.isIgnored(stack) || handler.isCoolingDown(stack))
        return false;
    return true;
}
```

- [ ] **Step 3: 迁移 hasStorageBlock() (line 50-52)**

```java
public static boolean hasStorageBlock(EntityMaid maid) {
    IRequestTaskHandler handler = IRequestTaskHandler.of(maid.getMainHandItem());
    return handler != null && handler.getStorageBlock(maid.getMainHandItem()) != null;
}
```

- [ ] **Step 4: 迁移 listNotDone() (lines 57-61)**

```java
public static boolean listNotDone(EntityMaid maid) {
    ItemStack stack = maid.getMainHandItem();
    IRequestTaskHandler handler = IRequestTaskHandler.of(stack);
    if (handler == null) return true;
    if (handler.isBlackMode(stack))
        return !handler.isBlackModeDone(stack);
    return handler.getItemStacksNotDone(stack).size() != 0;
}
```

- [ ] **Step 5: 迁移 listAllStored() (lines 73-77)**

```java
public static boolean listAllStored(EntityMaid maid) {
    ItemStack stack = maid.getMainHandItem();
    IRequestTaskHandler handler = IRequestTaskHandler.of(stack);
    if (handler == null) return true;
    if (handler.isBlackMode(stack))
        return false;
    return handler.isAllStored(stack);
}
```

- [ ] **Step 6: 迁移 isNothingToPlace() 中的 isIgnored (line 123)**

```java
// Before:
return RequestListItem.isIgnored(slot);

// After:
IRequestTaskHandler slotHandler = IRequestTaskHandler.of(slot);
return slotHandler == null || slotHandler.isIgnored(slot);
```

- [ ] **Step 7: 迁移 shouldCheckStock() (lines 181-185)**

```java
public static boolean shouldCheckStock(EntityMaid maid) {
    ItemStack mainHandItem = maid.getMainHandItem();
    IRequestTaskHandler handler = IRequestTaskHandler.of(mainHandItem);
    if (handler == null) return false;
    if (handler.getStorageBlock(mainHandItem) == null) return false;
    if (!handler.isStockMode(mainHandItem)) return false;
    return !handler.hasCheckedStock(mainHandItem);
}
```

- [ ] **Step 8: Build 验证**

Run: `gradlew build`

- [ ] **Step 9: Commit**

```bash
git add src/main/java/studio/fantasyit/maid_storage_manager/util/Conditions.java
git commit -m "refactor: migrate Conditions.java to IRequestTaskHandler capability"
```

---

### Task 8: 迁移 `RequestItemUtil.java`

**Files:**
- Modify: `src/main/java/studio/fantasyit/maid_storage_manager/util/RequestItemUtil.java`

- [ ] **Step 1: 修改 import**

删除: `import studio.fantasyit.maid_storage_manager.items.RequestListItem;`
添加: `import studio.fantasyit.maid_storage_manager.api.IRequestTaskHandler;`

- [ ] **Step 2: 迁移 isRequestTarget() (lines 37-54)**

```java
public static boolean isRequestTarget(ServerLevel level, EntityMaid maid, Target target) {
    IRequestTaskHandler handler = IRequestTaskHandler.of(maid.getMainHandItem());
    Target storageBlock = handler != null ? handler.getStorageBlock(maid.getMainHandItem()) : null;
    if (storageBlock == null || target == null)
        return false;
    // ... rest unchanged
}
```

- [ ] **Step 3: 迁移 stopJobAndStoreOrThrowItem() (lines 56-105)**

将所有 `RequestListItem.xxx(reqList)` 替换为 `handler.xxx(reqList)`。

在方法开头获取 handler:
```java
ItemStack reqList = maid.getMainHandItem();
IRequestTaskHandler handler = IRequestTaskHandler.of(reqList);
```

然后将所有:
- `RequestListItem.getRepeatInterval(reqList)` → `handler.getRepeatInterval(reqList)`
- `RequestListItem.setIgnore(reqList)` → `handler.setIgnore(reqList)`
- `RequestListItem.isAllSuccess(reqList)` → `handler.isAllSuccess(reqList)`
- `RequestListItem.getVirtualData(reqList)` → `handler.getVirtualData(reqList)`
- `RequestListItem.clearAllNonSuccess(toItem)` → 通过 `IRequestTaskHandler.of(toItem).clearAllNonSuccess(toItem)`
- `RequestListItem.getUUID(toItem)` → `IRequestTaskHandler.of(toItem).getWorkUUID(toItem)`

在 `dispatchedTaskDone` 和 `dispatchFindTaskDone` 中也同样替换。

- [ ] **Step 4: 迁移 makeVirtualItemStack() (lines 158-196 和 198-203)**

将 `new ItemRegistry.REQUEST_LIST_ITEM.get().getDefaultInstance()` 改为:
```java
ItemStack itemStack = ItemRegistry.VIRTUAL_REQUEST_LIST_ITEM.get().getDefaultInstance().copy();
```

由于 `VirtualRequestListItem` 构造函数已设置 `REQUEST_VIRTUAL=true`，不需要再手动设置 `REQUEST_VIRTUAL` 和 `REQUEST_VIRTUAL_SOURCE` 之外的数据组件。
保留手动设置 `REQUEST_VIRTUAL_SOURCE`。

- [ ] **Step 5: 迁移 makeVirtualItemStack(ItemStack source, String source) (lines 198-203)**

因为 source ItemStack 不一定来自同一物品类型，用 capability 方式处理:
```java
public static ItemStack makeVirtualItemStack(ItemStack source, String virtual_source) {
    IRequestTaskHandler handler = IRequestTaskHandler.of(source);
    if (handler == null) return ItemStack.EMPTY;
    ItemStack newItem = source.copy();
    handler.setVirtualData(newItem, /* compound containing source info */ );
    newItem.set(DataComponentRegistry.REQUEST_VIRTUAL_SOURCE, virtual_source);
    newItem.set(DataComponentRegistry.REQUEST_VIRTUAL, true);
    return newItem;
}
```

Wait, `setVirtualData` only sets `REQUEST_VIRTUAL_DATA`. The source ItemStack might already have data there. Let me look at the original code:

```java
public static ItemStack makeVirtualItemStack(ItemStack source, String virtual_source) {
    ItemStack newItem = source.copy();
    newItem.set(DataComponentRegistry.REQUEST_VIRTUAL_SOURCE, virtual_source);
    newItem.set(DataComponentRegistry.REQUEST_VIRTUAL, true);
    return newItem;
}
```

This is simple - just copies the stack and sets source + virtual. No need for handler here since it's just data component manipulation that will work on any ItemStack with the right components. Keep as-is but remove `RequestListItem` dependency.

- [ ] **Step 6: 迁移 markVisForCurrentRequestList() (lines 212-220)**

```java
public static void markVisForCurrentRequestList(ServerLevel level, EntityMaid maid, AbstractTargetMemory target) {
    IRequestTaskHandler handler = IRequestTaskHandler.of(maid.getMainHandItem());
    Target storageBlock = handler != null ? handler.getStorageBlock(maid.getMainHandItem()) : null;
    // ... rest unchanged
}
```

- [ ] **Step 7: Build 验证**

Run: `gradlew build`

- [ ] **Step 8: Commit**

```bash
git add src/main/java/studio/fantasyit/maid_storage_manager/util/RequestItemUtil.java
git commit -m "refactor: migrate RequestItemUtil.java to IRequestTaskHandler capability"
```

---

### Task 9: 迁移 `CopyConfigRecipe.java` 和 `ListClearRecipe.java`

**Files:**
- Modify: `src/main/java/studio/fantasyit/maid_storage_manager/recipe/CopyConfigRecipe.java`
- Modify: `src/main/java/studio/fantasyit/maid_storage_manager/recipe/ListClearRecipe.java`

- [ ] **Step 1: 迁移 CopyConfigRecipe.applyCopy()**

```java
// Before (line 86):
if (!toCopy.is(ItemRegistry.REQUEST_LIST_ITEM.get()) || RequestListItem.isVirtual(toCopy) || RequestListItem.isVirtual(newStack)) {

// After:
IRequestTaskHandler toCopyHandler = IRequestTaskHandler.of(toCopy);
IRequestTaskHandler newStackHandler = IRequestTaskHandler.of(newStack);
if ((toCopyHandler == null && newStackHandler == null) || 
    (toCopyHandler != null && toCopyHandler.isVirtual(toCopy)) || 
    (newStackHandler != null && newStackHandler.isVirtual(newStack))) {
    return ItemStack.EMPTY;
}
```

Wait, the original condition is: if toCopy is NOT a request_list item OR is virtual, return empty. The new logic should be: if either doesn't have the capability OR is virtual, return empty.

Actually, let me think more carefully. The original logic means: the copy operation only works between two non-virtual request_list items. With capability, the semantics should be: both stacks need the capability, and neither is virtual.

```java
// Before (lines 85-91):
} else if (newStack.is(ItemRegistry.REQUEST_LIST_ITEM.get())) {
    if (!toCopy.is(ItemRegistry.REQUEST_LIST_ITEM.get()) || RequestListItem.isVirtual(toCopy) || RequestListItem.isVirtual(newStack)) {
        return ItemStack.EMPTY;
    }
    newStack = toCopy.copy();
    RequestListItem.clearItemProcess(newStack);
    return newStack;
}

// After:
IRequestTaskHandler toCopyHandler = IRequestTaskHandler.of(toCopy);
IRequestTaskHandler newStackHandler = IRequestTaskHandler.of(newStack);
if (toCopyHandler != null && newStackHandler != null) {
    if (toCopyHandler.isVirtual(toCopy) || newStackHandler.isVirtual(newStack)) {
        return ItemStack.EMPTY;
    }
    newStack = toCopy.copy();
    newStackHandler.clearItemProcess(newStack);
    return newStack;
}
```

Add import: `import studio.fantasyit.maid_storage_manager.api.IRequestTaskHandler;`
Remove import: `import studio.fantasyit.maid_storage_manager.items.RequestListItem;`

- [ ] **Step 2: 迁移 ListClearRecipe**

```java
// Before:
RequestListItem.clearItemProcess(tmp);

// After:
IRequestTaskHandler handler = IRequestTaskHandler.of(tmp);
if (handler != null) {
    handler.clearItemProcess(tmp);
}
```

- [ ] **Step 3: Build 验证**

Run: `gradlew build`

- [ ] **Step 4: Commit**

```bash
git add src/main/java/studio/fantasyit/maid_storage_manager/recipe/CopyConfigRecipe.java src/main/java/studio/fantasyit/maid_storage_manager/recipe/ListClearRecipe.java
git commit -m "refactor: migrate recipe classes to IRequestTaskHandler capability"
```

---

### Task 10: 迁移 `ItemSelectorMenu.java`

**Files:**
- Modify: `src/main/java/studio/fantasyit/maid_storage_manager/menu/request/ItemSelectorMenu.java`

- [ ] **Step 1: 修改 import**

删除: `import studio.fantasyit.maid_storage_manager.items.RequestListItem;`
添加: `import studio.fantasyit.maid_storage_manager.api.IRequestTaskHandler;`

- [ ] **Step 2: 迁移构造函数 (line 43)**

```java
// Before:
matching = RequestListItem.getMatchType(target);

// After:
IRequestTaskHandler handler = IRequestTaskHandler.of(target);
matching = handler != null ? handler.getMatchType(target) : ItemStackUtil.MATCH_TYPE.AUTO;
```

Since `target = player.getMainHandItem()` and the RequestListItem now has the capability, this should work.

- [ ] **Step 3: 迁移 clear() (line 67)**

```java
// Before:
RequestListItem.clearItemProcess(target);

// After:
IRequestTaskHandler handler = IRequestTaskHandler.of(target);
if (handler != null) {
    handler.clearItemProcess(target);
}
```

- [ ] **Step 4: 迁移 save() (line 74)**

```java
// Before:
RequestItemStackList data = RequestListItem.getMutableRequestData(target);

// After:
IRequestTaskHandler handler = IRequestTaskHandler.of(target);
RequestItemStackList data = handler != null ? handler.getMutableRequestData(target) : new RequestItemStackList();
```

- [ ] **Step 5: Build 验证**

Run: `gradlew build`

- [ ] **Step 6: Commit**

```bash
git add src/main/java/studio/fantasyit/maid_storage_manager/menu/request/ItemSelectorMenu.java
git commit -m "refactor: migrate ItemSelectorMenu to IRequestTaskHandler capability"
```

---

### Task 11: 迁移 Maid AI 行为类（批量 — 同模式重复）

**Files:**
- Modify: `src/main/java/studio/fantasyit/maid_storage_manager/maid/behavior/request/FindListItemBehavior.java`
- Modify: `src/main/java/studio/fantasyit/maid_storage_manager/maid/behavior/request/find/RequestFindBehavior.java`
- Modify: `src/main/java/studio/fantasyit/maid_storage_manager/maid/behavior/request/find/RequestFindMoveBehavior.java`
- Modify: `src/main/java/studio/fantasyit/maid_storage_manager/maid/behavior/request/ret/RequestRetBehavior.java`
- Modify: `src/main/java/studio/fantasyit/maid_storage_manager/maid/behavior/request/ret/RequestRetMoveBehavior.java`
- Modify: `src/main/java/studio/fantasyit/maid_storage_manager/maid/behavior/request/craft/CraftInitBehavior.java`
- Modify: `src/main/java/studio/fantasyit/maid_storage_manager/maid/behavior/request/craft/CraftExitBehavior.java`
- Modify: `src/main/java/studio/fantasyit/maid_storage_manager/maid/behavior/request/craft/work/RequestCraftWorkMoveBehavior.java`
- Modify: `src/main/java/studio/fantasyit/maid_storage_manager/maid/behavior/request/craft/ret/ReturnOnVehicleBehavior.java`
- Modify: `src/main/java/studio/fantasyit/maid_storage_manager/maid/behavior/request/craft/dispatched/DispatchedGatherBehavior.java`
- Modify: `src/main/java/studio/fantasyit/maid_storage_manager/maid/behavior/request/craft/dispatched/DispatchedGatherMoveBehavior.java`
- Modify: `src/main/java/studio/fantasyit/maid_storage_manager/maid/behavior/request/stock/StockCheckBehavior.java`
- Modify: `src/main/java/studio/fantasyit/maid_storage_manager/maid/behavior/request/stock/StockCheckMoveBehavior.java`

所有迁移遵循同一模式：

```java
// Before:
RequestListItem.xxxMethod(maid.getMainHandItem(), ...)

// After:
ItemStack stack = maid.getMainHandItem();
IRequestTaskHandler handler = IRequestTaskHandler.of(stack);
// For void methods:
if (handler != null) handler.xxxMethod(stack, ...);
// For value-returning methods:
ResultType result = handler != null ? handler.xxxMethod(stack, ...) : defaultValue;
```

在每个文件顶部：
- 删除 `import studio.fantasyit.maid_storage_manager.items.RequestListItem;`
- 添加 `import studio.fantasyit.maid_storage_manager.api.IRequestTaskHandler;`

- [ ] **Step 1-13: 逐个迁移每个文件**

具体替换列表（每对 `旧 → 新`）:

| 旧 | 新 |
|---|---|
| `RequestListItem.getUUID(stack)` | `handler.getWorkUUID(stack)` |
| `RequestListItem.tickCoolingDown(item)` | `handler.tickCoolingDown(item)` |
| `RequestListItem.isIgnored(item)` | `handler.isIgnored(item)` |
| `RequestListItem.isCoolingDown(item)` | `handler.isCoolingDown(item)` |
| `RequestListItem.getStorageBlock(stack)` | `handler.getStorageBlock(stack)` |
| `RequestListItem.getStorageEntity(stack)` | `handler.getStorageEntity(stack)` |
| `RequestListItem.getRepeatInterval(stack)` | `handler.getRepeatInterval(stack)` |
| `RequestListItem.updateCollectedItem(stack, ...)` | `handler.updateCollectedItem(stack, ...)` |
| `RequestListItem.getItemStacksNotDone(stack, ...)` | `handler.getItemStacksNotDone(stack, ...)` |
| `RequestListItem.getMatchType(stack)` | `handler.getMatchType(stack)` |
| `RequestListItem.isBlackMode(stack)` | `handler.isBlackMode(stack)` |
| `RequestListItem.updateStored(stack, ...)` | `handler.updateStored(stack, ...)` |
| `RequestListItem.isAllSuccess(stack)` | `handler.isAllSuccess(stack)` |
| `RequestListItem.updateCollectedNotStored(stack, ...)` | `handler.updateCollectedNotStored(stack, ...)` |
| `RequestListItem.markAllDone(stack)` | `handler.markAllDone(stack)` |
| `RequestListItem.setHasCheckedStock(stack, ...)` | `handler.setHasCheckedStock(stack, ...)` |

- [ ] **Step 14: Build 验证**

Run: `gradlew build`

- [ ] **Step 15: Commit**

```bash
git add src/main/java/studio/fantasyit/maid_storage_manager/maid/behavior/
git commit -m "refactor: migrate all maid AI behaviors to IRequestTaskHandler"
```

---

### Task 12: 迁移合成和调度相关类

**Files:**
- Modify: `src/main/java/studio/fantasyit/maid_storage_manager/craft/algo/MaidCraftPlanner.java`
- Modify: `src/main/java/studio/fantasyit/maid_storage_manager/craft/work/CraftLayerChain.java`
- Modify: `src/main/java/studio/fantasyit/maid_storage_manager/craft/work/ProgressData.java`
- Modify: `src/main/java/studio/fantasyit/maid_storage_manager/items/WorkCardItem.java`
- Modify: `src/main/java/studio/fantasyit/maid_storage_manager/ai/StorageFetchFunction.java`
- Modify: `src/main/java/studio/fantasyit/maid_storage_manager/event/BindingRender.java`
- Modify: `src/main/java/studio/fantasyit/maid_storage_manager/event/BindingRenderSyncSender.java`
- Modify: `src/main/java/studio/fantasyit/maid_storage_manager/maid/behavior/place/PlaceBehavior.java`
- Modify: `src/main/java/studio/fantasyit/maid_storage_manager/maid/behavior/place/PlaceMoveBehavior.java`
- Modify: `src/main/java/studio/fantasyit/maid_storage_manager/maid/behavior/place/ThrowToPlaceBehavior.java`
- Modify: `src/main/java/studio/fantasyit/maid_storage_manager/maid/memory/CraftMemory.java`
- Modify: `src/main/java/studio/fantasyit/maid_storage_manager/craft/algo/utils/RequestListSplitter.java`

- [ ] **Step 1-12: 迁移每个文件**

所有迁移遵循模式：`RequestListItem.xxx(stack, ...)` → `IRequestTaskHandler.of(stack).xxx(stack, ...)`（带 null check）。

`WorkCardItem.java` 中的 `RequestListItem.setVirtualData()` → `handler.setVirtualData()`
`WorkCardItem.java` 中的 `RequestListItem.getUUID()` → `handler.getWorkUUID()`
`StorageFetchFunction.java` 中的 `RequestListItem.getImmutableRequestData()` → `handler.getImmutableRequestData()`
`ProgressData.java` 中的 `RequestListItem.getImmutableRequestData()` → `handler.getImmutableRequestData()`
`CraftLayerChain.java` 中的各种调用 → `handler.xxx()`
`MaidCraftPlanner.java` 中的各种调用 → `handler.xxx()`
`BindingRender.java` 中的 `RequestListItem.getStorageBlock()` → `handler.getStorageBlock()`
`BindingRenderSyncSender.java` 中的 `RequestListItem.getStorageEntity()` → `handler.getStorageEntity()`
`PlaceBehavior.java`, `PlaceMoveBehavior.java`, `ThrowToPlaceBehavior.java` 中的 `RequestListItem.isIgnored()` → `handler.isIgnored()`

- [ ] **Step 11: Build 验证**

Run: `gradlew build`

现在整个项目应该可以编译通过了。

- [ ] **Step 12: Commit**

```bash
git add src/main/java/studio/fantasyit/maid_storage_manager/craft/ src/main/java/studio/fantasyit/maid_storage_manager/items/WorkCardItem.java src/main/java/studio/fantasyit/maid_storage_manager/ai/ src/main/java/studio/fantasyit/maid_storage_manager/event/ src/main/java/studio/fantasyit/maid_storage_manager/maid/behavior/place/
git commit -m "refactor: migrate crafting, AI, event, and place behavior classes to IRequestTaskHandler"
```

---

### Task 13: 迁移 `WrittenInvListItem.java` 和 `communicate/CommunicateUtil.java`

**Files:**
- Modify: `src/main/java/studio/fantasyit/maid_storage_manager/items/WrittenInvListItem.java`
- Modify: `src/main/java/studio/fantasyit/maid_storage_manager/communicate/CommunicateUtil.java`
- Modify: `src/main/java/studio/fantasyit/maid_storage_manager/integration/request/IngredientRequest.java`
- Modify: `src/main/java/studio/fantasyit/maid_storage_manager/integration/tour_guide/tours/RequestListTour.java`

- [ ] **Step 1: 迁移 WrittenInvListItem.java**

模式同样：`RequestListItem.xxx()` → `IRequestTaskHandler.of(stack).xxx(stack)`

- [ ] **Step 2: 迁移 CommunicateUtil.java**

创建虚拟请求时改用 `VIRTUAL_REQUEST_LIST_ITEM`。

- [ ] **Step 3: 迁移 IngredientRequest.java (JEI)**

创建虚拟请求时改用 `VIRTUAL_REQUEST_LIST_ITEM`。

- [ ] **Step 4: 迁移 RequestListTour.java**（目前是空桩，无需修改）

- [ ] **Step 5: Build 验证**

Run: `gradlew build`

- [ ] **Step 6: Commit**

```bash
git add src/main/java/studio/fantasyit/maid_storage_manager/items/WrittenInvListItem.java src/main/java/studio/fantasyit/maid_storage_manager/communicate/ src/main/java/studio/fantasyit/maid_storage_manager/integration/
git commit -m "refactor: migrate remaining callers to IRequestTaskHandler and VirtualRequestListItem"
```

---

### Task 14: 最终验证和清理

- [ ] **Step 1: Full build**

Run: `gradlew build`
期望：BUILD SUCCESSFUL，零编译错误。

- [ ] **Step 2: 检查是否还有残留的 `RequestListItem.` 直接调用**

Run: `rg "RequestListItem\." --include="*.java" src/`
期望：仅剩 `RequestListItem.java` 自身内部的调用，或 UI 方法的合法调用（`createMenu` 等）。

- [ ] **Step 3: 检查是否有残留的 `ItemRegistry.REQUEST_LIST_ITEM` 检查**

除了 `CopyConfigRecipe`、`ItemRegistry` 和 `CreativeTabRegistry` 之外，不应有其他地方硬检查 `is(REQUEST_LIST_ITEM)`。这些是 UI 注册用途，可以保留。

- [ ] **Step 4: Commit**

```bash
git commit --allow-empty -m "chore: final verification pass complete"
```
