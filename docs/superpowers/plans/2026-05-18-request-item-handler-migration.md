# RequestItemHandler Capability 迁移实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将 `RequestListItem` 的任务管理逻辑分离到 `IRequestTaskHandler` capability 接口中，使其他物品也能充当 request_list。

**Architecture:** 基于 NeoForge 1.21.1 的 `ItemCapability<T, C>` 事件驱动能力系统。创建 `IRequestTaskHandler` 接口 + `DefaultRequestTaskHandler` 实现类。通过 `RegisterCapabilitiesEvent` 注册物品的能力提供者 (lambda: `(stack, ctx) -> DefaultRequestTaskHandler.INSTANCE`)。所有现有 `RequestListItem.xxx()` 调用改为 `IRequestTaskHandler.of(stack).xxx(stack)`。

**Tech Stack:** Java 21, NeoForge 1.21.1, Mojang mappings, `ItemCapability`, `RegisterCapabilitiesEvent`

**NeoForge 1.21.1 能力系统关键 API:**
- `ItemCapability<IRequestTaskHandler, Void>` — 能力类型（通过 `ItemCapability.createVoid()` 创建）
- `ICapabilityProvider<ItemStack, Void, IRequestTaskHandler>` — 函数式接口 `(ItemStack, Void) -> @Nullable IRequestTaskHandler`
- `RegisterCapabilitiesEvent.registerItem(capability, provider, items...)` — 注册提供者
- `stack.getCapability(ItemCapability)` — 查询能力，返回 `@Nullable T`

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

- [ ] **Step 2: Build 验证**

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

- [ ] **Step 1: 创建 IRequestTaskHandler 接口**

```java
package studio.fantasyit.maid_storage_manager.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.items.data.RequestItemStackList;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;

import java.util.List;
import java.util.UUID;

public interface IRequestTaskHandler {

    ItemCapability<IRequestTaskHandler, @Nullable Void> CAPABILITY =
            ItemCapability.createVoid(
                    ResourceLocation.fromNamespaceAndPath(MaidStorageManager.MODID, "request_task_handler"),
                    IRequestTaskHandler.class
            );

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
}
```

- [ ] **Step 2: Build 验证**

Run: `gradlew build`

- [ ] **Step 3: Commit**

```bash
git add src/main/java/studio/fantasyit/maid_storage_manager/api/IRequestTaskHandler.java
git commit -m "feat: add IRequestTaskHandler capability interface with ItemCapability"
```

---

### Task 3: 注册能力提供者

**Files:**
- Modify: `src/main/java/studio/fantasyit/maid_storage_manager/MaidStorageManager.java`

在 mod constructor 中注册 `RegisterCapabilitiesEvent` 监听器，为 `REQUEST_LIST_ITEM` 和 `VIRTUAL_REQUEST_LIST_ITEM` 注册能力提供者。

- [ ] **Step 1: 添加能力注册**

在 `MaidStorageManager.java` 构造函数末尾添加：

```java
modEventBus.addListener(RegisterCapabilitiesEvent.class, event -> {
    event.registerItem(
            IRequestTaskHandler.CAPABILITY,
            (stack, ctx) -> DefaultRequestTaskHandler.INSTANCE,
            ItemRegistry.REQUEST_LIST_ITEM.get(),
            ItemRegistry.VIRTUAL_REQUEST_LIST_ITEM.get()
    );
});
```

需要添加 imports:
```java
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import studio.fantasyit.maid_storage_manager.api.IRequestTaskHandler;
import studio.fantasyit.maid_storage_manager.items.handler.DefaultRequestTaskHandler;
```

- [ ] **Step 2: Build 验证** (预计失败，因为 DefaultRequestTaskHandler 还未创建)

Run: `gradlew build`

- [ ] **Step 3: Commit**

```bash
git add src/main/java/studio/fantasyit/maid_storage_manager/MaidStorageManager.java
git commit -m "feat: register IRequestTaskHandler capability for request_list items"
```

---

### Task 4: 创建 `DefaultRequestTaskHandler` 实现类

**Files:**
- Create: `src/main/java/studio/fantasyit/maid_storage_manager/items/handler/DefaultRequestTaskHandler.java`

将 `RequestListItem` 中所有 ~25 个静态方法的主体逻辑迁入此类。删除所有 `if (!stack.is(ItemRegistry.REQUEST_LIST_ITEM.get()))` 守卫（能力检查已由调用方完成）。

> 完整代码见 plan 附录 A。此处不再重复列出全部 25 个方法，实际迁移时从 `RequestListItem.java` 的现有方法体中逐行复制，仅移除 `is(REQUEST_LIST_ITEM)` 守卫。

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
import studio.fantasyit.maid_storage_manager.maid.memory.AbstractTargetMemory;
import studio.fantasyit.maid_storage_manager.registry.DataComponentRegistry;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;

import java.util.ArrayList;
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
    public void updateCollectedNotStored(ItemStack stack, IItemHandler tmpStorage) {
        RequestItemStackList request = getMutableRequestData(stack);
        for (RequestItemStackList.ListItem tmp : request.list) {
            if (tmp.stored >= tmp.collected) continue;
            int count = 0;
            for (int j = 0; j < tmpStorage.getSlots(); j++) {
                ItemStack itemStack = tmpStorage.getStackInSlot(j);
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

物品本身无需实现 `IRequestTaskHandler`，能力提供者已在 Task 3 中通过事件注册。

- [ ] **Step 1: 创建 VirtualRequestListItem**

```java
package studio.fantasyit.maid_storage_manager.items;

import studio.fantasyit.maid_storage_manager.items.data.RequestItemStackList;
import studio.fantasyit.maid_storage_manager.registry.DataComponentRegistry;

public class VirtualRequestListItem extends MaidInteractItem {

    public VirtualRequestListItem() {
        super(new Properties()
                .stacksTo(1)
                .component(DataComponentRegistry.REQUEST_ITEMS.get(), new RequestItemStackList().toImmutable())
                .component(DataComponentRegistry.REQUEST_VIRTUAL.get(), true)
        );
    }
}
```

- [ ] **Step 2: 注册到 ItemRegistry**

在 `ItemRegistry.java` 中添加（紧接 `REQUEST_LIST_ITEM` 后）:

```java
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

### Task 6: 重构 `RequestListItem` — 删除静态方法

**Files:**
- Modify: `src/main/java/studio/fantasyit/maid_storage_manager/items/RequestListItem.java`

操作：
1. 删除所有 public static 方法（保留 UI 方法）
2. 修改内部调用（`use()`、`appendHoverText()`、`interactLivingEntity()`、`useOn()`、`createMenu()` 中调用自身静态方法处改为通过 `IRequestTaskHandler.of()`）

**注意：`RequestListItem` 不实现 `IRequestTaskHandler`。能力通过 `RegisterCapabilitiesEvent` 提供。**

- [ ] **Step 1: 修改内部调用**

在 `RequestListItem` 的 `use()` (line 176): 
```java
// 改前:
if (!isVirtual(serverPlayer.getMainHandItem()))

// 改后:
IRequestTaskHandler handler = IRequestTaskHandler.of(serverPlayer.getMainHandItem());
if (handler == null || !handler.isVirtual(serverPlayer.getMainHandItem()))
```

在 `appendHoverText()` (line 267):
```java
// 改前:
RequestItemStackList.Immutable request = getImmutableRequestData(itemStack);

// 改后:
IRequestTaskHandler handler = IRequestTaskHandler.of(itemStack);
RequestItemStackList.Immutable request = handler != null ? handler.getImmutableRequestData(itemStack) : null;
```

在 `interactLivingEntity()` (line 198):
```java
// 改前:
if (!hasAnyStorage(itemStack)) {

// 改后:
IRequestTaskHandler handler = IRequestTaskHandler.of(itemStack);
if (handler != null && !handler.hasAnyStorage(itemStack)) {
```

在 `createMenu()` (line 609):
```java
// 改前:
if (isVirtual(p_39956_.getItemInHand(InteractionHand.MAIN_HAND)))

// 改后:
ItemStack mainHand = p_39956_.getItemInHand(InteractionHand.MAIN_HAND);
IRequestTaskHandler handler = IRequestTaskHandler.of(mainHand);
if (handler != null && handler.isVirtual(mainHand))
```

添加 import: `import studio.fantasyit.maid_storage_manager.api.IRequestTaskHandler;`

- [ ] **Step 2: 删除所有 public static 方法**

删除: `getMutableRequestData`, `getImmutableRequestData`, `getMatchType`（2个）, `isIgnored`, `setIgnore`, `getRepeatInterval`, `getRepeatCd`, `addItemStackCollected`, `clearItemProcess`, `clearAllNonSuccess`, `isCoolingDown`, `tickCoolingDown`, `markDone`, `isAllStored`, `getItemStacksNotDone`（2个）, `getStorageEntity`, `getStorageBlock`, `hasAnyStorage`, `updateCollectedItem`, `updateStored`, `updateCollectedNotStored`, `setFailAddition`, `markAllDone`, `getUUID`, `setMissingItem`, `isAllSuccess`, `isStockMode`, `hasCheckedStock`, `setHasCheckedStock`, `isVirtual`, `isBlackMode`, `isBlackModeDone`, `setVirtualData`, `getVirtualData`.

- [ ] **Step 3: Build 验证** (预计大量编译错误，后续任务修复)

Run: `gradlew build`

- [ ] **Step 4: Commit**

```bash
git add src/main/java/studio/fantasyit/maid_storage_manager/items/RequestListItem.java
git commit -m "refactor: remove static methods from RequestListItem, delegating to capability"
```

---

### Task 7: 迁移 `Conditions.java`

**Files:**
- Modify: `src/main/java/studio/fantasyit/maid_storage_manager/util/Conditions.java`

替换 import: `RequestListItem` → `IRequestTaskHandler`。所有 `RequestListItem.xxx(stack)` → `handler.xxx(stack)`。

- [ ] **Step 1: 修改 import**

删除: `import studio.fantasyit.maid_storage_manager.items.RequestListItem;`
删除: `import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;`
添加: `import studio.fantasyit.maid_storage_manager.api.IRequestTaskHandler;`

- [ ] **Step 2: takingRequestList (lines 23-31)**

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

- [ ] **Step 3: hasStorageBlock (lines 50-52)**

```java
public static boolean hasStorageBlock(EntityMaid maid) {
    IRequestTaskHandler handler = IRequestTaskHandler.of(maid.getMainHandItem());
    return handler != null && handler.getStorageBlock(maid.getMainHandItem()) != null;
}
```

- [ ] **Step 4: listNotDone (lines 57-61)**

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

- [ ] **Step 5: listAllStored (lines 73-77)**

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

- [ ] **Step 6: isNothingToPlace (line 123)**

```java
// 改前:
return RequestListItem.isIgnored(slot);

// 改后:
IRequestTaskHandler slotHandler = IRequestTaskHandler.of(slot);
return slotHandler == null || slotHandler.isIgnored(slot);
```

- [ ] **Step 7: shouldCheckStock (lines 181-185)**

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
git commit -m "refactor: migrate Conditions to IRequestTaskHandler capability"
```

---

### Task 8: 迁移 `RequestItemUtil.java`

**Files:**
- Modify: `src/main/java/studio/fantasyit/maid_storage_manager/util/RequestItemUtil.java`

- [ ] **Step 1: 修改 import**

删除: `import studio.fantasyit.maid_storage_manager.items.RequestListItem;`
添加: `import studio.fantasyit.maid_storage_manager.api.IRequestTaskHandler;`

- [ ] **Step 2: isRequestTarget (line 37-38)**

```java
// 改前:
Target storageBlock = RequestListItem.getStorageBlock(maid.getMainHandItem());

// 改后:
IRequestTaskHandler handler = IRequestTaskHandler.of(maid.getMainHandItem());
Target storageBlock = handler != null ? handler.getStorageBlock(maid.getMainHandItem()) : null;
```

- [ ] **Step 3: stopJobAndStoreOrThrowItem (lines 56-105)**

在方法开头获取 handler: `IRequestTaskHandler handler = IRequestTaskHandler.of(reqList);`

替换所有 `RequestListItem.xxx(reqList)` → `handler != null ? handler.xxx(reqList)`:
- `RequestListItem.getRepeatInterval(reqList)` → `handler.getRepeatInterval(reqList)`
- `RequestListItem.setIgnore(reqList)` → `handler.setIgnore(reqList)`
- `RequestListItem.isAllSuccess(reqList)` → `handler.isAllSuccess(reqList)`
- `RequestListItem.getVirtualData(reqList)` → `handler.getVirtualData(reqList)`

对于 `clearAllNonSuccess(toItem)` 和 `getUUID(toItem)`:
```java
IRequestTaskHandler toHandler = IRequestTaskHandler.of(toItem);
if (toHandler != null) toHandler.clearAllNonSuccess(toItem);
```

- [ ] **Step 4: makeVirtualItemStack (lines 158-196)**

改用 `VIRTUAL_REQUEST_LIST_ITEM`:
```java
ItemStack itemStack = ItemRegistry.VIRTUAL_REQUEST_LIST_ITEM.get().getDefaultInstance().copy();
```

- [ ] **Step 5: dispatchedTaskDone / dispatchFindTaskDone**

同样替换 `RequestListItem.xxx()` → `handler.xxx()`.

- [ ] **Step 6: markVisForCurrentRequestList (line 213)**

```java
IRequestTaskHandler handler = IRequestTaskHandler.of(maid.getMainHandItem());
Target storageBlock = handler != null ? handler.getStorageBlock(maid.getMainHandItem()) : null;
```

- [ ] **Step 7: Build 验证**

Run: `gradlew build`

- [ ] **Step 8: Commit**

```bash
git add src/main/java/studio/fantasyit/maid_storage_manager/util/RequestItemUtil.java
git commit -m "refactor: migrate RequestItemUtil to IRequestTaskHandler"
```

---

### Task 9: 迁移配方类

**Files:**
- Modify: `src/main/java/studio/fantasyit/maid_storage_manager/recipe/CopyConfigRecipe.java`
- Modify: `src/main/java/studio/fantasyit/maid_storage_manager/recipe/ListClearRecipe.java`

- [ ] **Step 1: CopyConfigRecipe.applyCopy (lines 85-91)**

```java
// 改前:
if (!toCopy.is(ItemRegistry.REQUEST_LIST_ITEM.get()) || RequestListItem.isVirtual(toCopy) || RequestListItem.isVirtual(newStack)) {
    return ItemStack.EMPTY;
}
newStack = toCopy.copy();
RequestListItem.clearItemProcess(newStack);

// 改后:
IRequestTaskHandler toCopyHandler = IRequestTaskHandler.of(toCopy);
IRequestTaskHandler newStackHandler = IRequestTaskHandler.of(newStack);
if (toCopyHandler == null || newStackHandler == null ||
    toCopyHandler.isVirtual(toCopy) || newStackHandler.isVirtual(newStack)) {
    return ItemStack.EMPTY;
}
newStack = toCopy.copy();
newStackHandler.clearItemProcess(newStack);
```

- [ ] **Step 2: ListClearRecipe**

```java
// 改前:
RequestListItem.clearItemProcess(tmp);

// 改后:
IRequestTaskHandler handler = IRequestTaskHandler.of(tmp);
if (handler != null) handler.clearItemProcess(tmp);
```

- [ ] **Step 3: Build 验证, Commit**

```bash
git add src/main/java/studio/fantasyit/maid_storage_manager/recipe/CopyConfigRecipe.java src/main/java/studio/fantasyit/maid_storage_manager/recipe/ListClearRecipe.java
git commit -m "refactor: migrate recipe classes to IRequestTaskHandler"
```

---

### Task 10: 迁移 `ItemSelectorMenu.java`

**Files:**
- Modify: `src/main/java/studio/fantasyit/maid_storage_manager/menu/request/ItemSelectorMenu.java`

- [ ] **Step 1: 修改 import**

删除: `import studio.fantasyit.maid_storage_manager.items.RequestListItem;`
添加: `import studio.fantasyit.maid_storage_manager.api.IRequestTaskHandler;`

- [ ] **Step 2: 构造函数 (line 43)**

```java
// 改前:
matching = RequestListItem.getMatchType(target);

// 改后:
IRequestTaskHandler handler = IRequestTaskHandler.of(target);
matching = handler != null ? handler.getMatchType(target) : ItemStackUtil.MATCH_TYPE.AUTO;
```

- [ ] **Step 3: clear() (line 67)**

```java
// 改前:
RequestListItem.clearItemProcess(target);

// 改后:
IRequestTaskHandler handler = IRequestTaskHandler.of(target);
if (handler != null) handler.clearItemProcess(target);
```

- [ ] **Step 4: save() (line 74)**

```java
// 改前:
RequestItemStackList data = RequestListItem.getMutableRequestData(target);

// 改后:
IRequestTaskHandler handler = IRequestTaskHandler.of(target);
RequestItemStackList data = handler != null ? handler.getMutableRequestData(target) : new RequestItemStackList();
```

- [ ] **Step 5: Build 验证, Commit**

```bash
git add src/main/java/studio/fantasyit/maid_storage_manager/menu/request/ItemSelectorMenu.java
git commit -m "refactor: migrate ItemSelectorMenu to IRequestTaskHandler"
```

---

### Task 11: 迁移 Maid AI 行为类（13 文件批量替换）

**修改文件 (统一模式: `RequestListItem.xxx(stack)` → `handler.xxx(stack)`):**
- `maid/behavior/request/FindListItemBehavior.java`
- `maid/behavior/request/find/RequestFindBehavior.java`
- `maid/behavior/request/find/RequestFindMoveBehavior.java`
- `maid/behavior/request/ret/RequestRetBehavior.java`
- `maid/behavior/request/ret/RequestRetMoveBehavior.java`
- `maid/behavior/request/craft/CraftInitBehavior.java`
- `maid/behavior/request/craft/CraftExitBehavior.java`
- `maid/behavior/request/craft/work/RequestCraftWorkMoveBehavior.java`
- `maid/behavior/request/craft/ret/ReturnOnVehicleBehavior.java`
- `maid/behavior/request/craft/dispatched/DispatchedGatherBehavior.java`
- `maid/behavior/request/craft/dispatched/DispatchedGatherMoveBehavior.java`
- `maid/behavior/request/stock/StockCheckBehavior.java`
- `maid/behavior/request/stock/StockCheckMoveBehavior.java`

**每个文件的变更:**
- 删除 `import studio.fantasyit.maid_storage_manager.items.RequestListItem;`
- 添加 `import studio.fantasyit.maid_storage_manager.api.IRequestTaskHandler;`
- 在调用的方法作用域开头提取 `IRequestTaskHandler handler = IRequestTaskHandler.of(stack);`
- 替换: `RequestListItem.xxx(stack, args)` → `handler.xxx(stack, args)`

**具体替换映射表:**

| 旧方法 | 新方法 |
|--------|--------|
| `RequestListItem.getUUID(stack)` | `handler.getWorkUUID(stack)` |
| `RequestListItem.tickCoolingDown(stack)` | `handler.tickCoolingDown(stack)` |
| `RequestListItem.isIgnored(stack)` | `handler.isIgnored(stack)` |
| `RequestListItem.isCoolingDown(stack)` | `handler.isCoolingDown(stack)` |
| `RequestListItem.getStorageBlock(stack)` | `handler.getStorageBlock(stack)` |
| `RequestListItem.getStorageEntity(stack)` | `handler.getStorageEntity(stack)` |
| `RequestListItem.getRepeatInterval(stack)` | `handler.getRepeatInterval(stack)` |
| `RequestListItem.updateCollectedItem(stack, a, b, c)` | `handler.updateCollectedItem(stack, a, b, c)` |
| `RequestListItem.getItemStacksNotDone(stack)` | `handler.getItemStacksNotDone(stack)` |
| `RequestListItem.getItemStacksNotDone(stack, flag)` | `handler.getItemStacksNotDone(stack, flag)` |
| `RequestListItem.getMatchType(stack)` | `handler.getMatchType(stack)` |
| `RequestListItem.isBlackMode(stack)` | `handler.isBlackMode(stack)` |
| `RequestListItem.updateStored(stack, a, b, c)` | `handler.updateStored(stack, a, b, c)` |
| `RequestListItem.isAllSuccess(stack)` | `handler.isAllSuccess(stack)` |
| `RequestListItem.updateCollectedNotStored(stack, a)` | `handler.updateCollectedNotStored(stack, a)` |
| `RequestListItem.markAllDone(stack)` | `handler.markAllDone(stack)` |
| `RequestListItem.setHasCheckedStock(stack, a)` | `handler.setHasCheckedStock(stack, a)` |
| `RequestListItem.isBlackModeDone(stack)` | `handler.isBlackModeDone(stack)` |

- [ ] **Step 1: 逐文件修改**

- [ ] **Step 2: Build 验证, Commit**

```bash
git add src/main/java/studio/fantasyit/maid_storage_manager/maid/behavior/
git commit -m "refactor: migrate all maid AI behaviors to IRequestTaskHandler"
```

---

### Task 12: 迁移合成/事件/放置/内存类

**Files:**
- Modify: `maid/memory/CraftMemory.java`
- Modify: `craft/algo/MaidCraftPlanner.java`
- Modify: `craft/work/CraftLayerChain.java`
- Modify: `craft/work/ProgressData.java`
- Modify: `craft/algo/utils/RequestListSplitter.java`
- Modify: `items/WorkCardItem.java`
- Modify: `ai/StorageFetchFunction.java`
- Modify: `event/BindingRender.java`
- Modify: `event/BindingRenderSyncSender.java`
- Modify: `maid/behavior/place/PlaceBehavior.java`
- Modify: `maid/behavior/place/PlaceMoveBehavior.java`
- Modify: `maid/behavior/place/ThrowToPlaceBehavior.java`

统一模式：`RequestListItem.xxx()` → `IRequestTaskHandler.of(stack).xxx(stack)`

- [ ] **Step 1: 逐个修改**

- [ ] **Step 2: Build 验证, Commit**

```bash
git add src/main/java/studio/fantasyit/maid_storage_manager/craft/ src/main/java/studio/fantasyit/maid_storage_manager/items/WorkCardItem.java src/main/java/studio/fantasyit/maid_storage_manager/ai/ src/main/java/studio/fantasyit/maid_storage_manager/event/ src/main/java/studio/fantasyit/maid_storage_manager/maid/memory/ src/main/java/studio/fantasyit/maid_storage_manager/maid/behavior/place/
git commit -m "refactor: migrate crafting, AI, event, place to IRequestTaskHandler"
```

---

### Task 13: 迁移剩余调用者

**Files:**
- Modify: `items/WrittenInvListItem.java`
- Modify: `communicate/CommunicateUtil.java`
- Modify: `integration/request/IngredientRequest.java`

- [ ] **Step 1: 迁移 WrittenInvListItem** — `RequestListItem.xxx()` → `IRequestTaskHandler.of(stack).xxx(stack)`

- [ ] **Step 2: 迁移 CommunicateUtil** — 创建虚拟请求改用 `VIRTUAL_REQUEST_LIST_ITEM`

- [ ] **Step 3: 迁移 IngredientRequest (JEI)** — 创建虚拟请求改用 `VIRTUAL_REQUEST_LIST_ITEM`

- [ ] **Step 4: Build 验证, Commit**

```bash
git add src/main/java/studio/fantasyit/maid_storage_manager/items/WrittenInvListItem.java src/main/java/studio/fantasyit/maid_storage_manager/communicate/ src/main/java/studio/fantasyit/maid_storage_manager/integration/
git commit -m "refactor: migrate remaining callers to IRequestTaskHandler and VirtualRequestListItem"
```

---

### Task 14: 最终验证

- [ ] **Step 1: Full build**

Run: `gradlew build`
期望: BUILD SUCCESSFUL

- [ ] **Step 2: 残留检查**

```bash
rg "RequestListItem\." --include="*.java" src/main/java/
```
期望: 仅剩 `RequestListItem` UI 方法的合法调用，无残留静态方法调用。

- [ ] **Step 3: 最终 commit**

```bash
git add . && git commit -m "chore: final cleanup after IRequestTaskHandler migration" || echo "nothing to commit"
```
