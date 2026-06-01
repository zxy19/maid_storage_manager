# NeoForge Transfer API 迁移: `IItemHandler` → `ResourceHandler<ItemResource>`

本文档记录 NeoForge 26.1 中物品存储系统的核心 API 重写——从旧的 `IItemHandler` / `ItemStackHandler` 体系迁移到新的 Transfer API (`ResourceHandler<ItemResource>` / `ItemStacksResourceHandler`)。

这并非本模组特有变更，而是 **NeoForge 26.1 平台层级的破坏性 API 重写**。

---

## 1. 概述

NeoForge 21.1 时代存在两套物品存储 API 并行：
- **旧 API**: `IItemHandler`, `ItemStackHandler`, `SlotItemHandler`（`net.neoforged.neoforge.items` 包）
- **新 API**: `ResourceHandler<ItemResource>`, `ItemStacksResourceHandler`, `ResourceHandlerSlot`（`net.neoforged.neoforge.transfer` 包）

旧项目主要使用旧 API（`ItemStackHandler` 作为 inventory handler 父类），部分地方混合使用新 API。新项目**完全移除旧 API**，所有物品存储全部迁移到 Transfer API。

---

## 2. 包路径迁移

| 旧包路径 | 新包路径 |
|----------|----------|
| `net.neoforged.neoforge.items.IItemHandler` | `net.neoforged.neoforge.transfer.ResourceHandler` |
| `net.neoforged.neoforge.items.IItemHandlerModifiable` | `ResourceHandler` 统一处理读写（无独立接口） |
| `net.neoforged.neoforge.items.ItemStackHandler` | `net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler` |
| `net.neoforged.neoforge.items.SlotItemHandler` | `net.neoforged.neoforge.transfer.item.ResourceHandlerSlot` |
| `net.neoforged.neoforge.items.ItemHandlerHelper` | `net.neoforged.neoforge.transfer.item.ItemResourceUtil` |
| `net.neoforged.neoforge.items.wrapper.CombinedInvWrapper` | `net.neoforged.neoforge.transfer.CombinedResourceHandler` |
| `net.neoforged.neoforge.items.wrapper.RangedWrapper` | `net.neoforged.neoforge.transfer.RangedResourceHandler` |
| `net.neoforged.neoforge.items.wrapper.EntityHandsInvWrapper` | **已移除** — 直接使用 `ResourceHandler` |
| `net.neoforged.neoforge.items.wrapper.EntityArmorInvWrapper` | **已移除** — 直接使用 `ResourceHandler` |
| `net.neoforged.neoforge.capabilities.Capabilities.ItemHandler` | `net.neoforged.neoforge.capabilities.Capabilities.Item` |

---

## 3. 类层次结构对比

### 3.1 ItemStackHandler → ItemStacksResourceHandler

```diff
-import net.neoforged.neoforge.items.ItemStackHandler;
+import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
+import net.neoforged.neoforge.transfer.item.ItemResource;

-public class MaidBackpackHandler extends ItemStackHandler {
+public class MaidBackpackHandler extends ItemStacksResourceHandler {
```

### 3.2 SlotItemHandler → ResourceHandlerSlot

```diff
-import net.neoforged.neoforge.items.SlotItemHandler;
+import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;

-public class BackpackSlot extends SlotItemHandler implements ITriggerSlotChange {
+public class BackpackSlot extends ResourceHandlerSlot implements ITriggerSlotChange {
```

### 3.3 CombinedInvWrapper → CombinedResourceHandler

```diff
-import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
+import net.neoforged.neoforge.transfer.CombinedResourceHandler;
+import net.neoforged.neoforge.transfer.item.ItemResource;

-public class MaidInvWrapper extends CombinedInvWrapper {
+public class MaidInvWrapper extends CombinedResourceHandler<@NotNull ItemResource> {
```

### 3.4 RangedWrapper → RangedResourceHandler

```diff
-RangedWrapper rangedWrapper = new RangedWrapper(maidInv, 0, maxContainerIndex);
+var combinedInvWrapper = RangedResourceHandler.of(maidInv, 0, maxContainerIndex);
```

### 3.5 EntityHandsInvWrapper / EntityArmorInvWrapper → 移除

旧项目中的 `MaidHandsInvWrapper`（继承 `EntityHandsInvWrapper`）在新项目中**完全移除**。新项目手部和护甲物品栏直接使用 `ResourceHandler<ItemResource>`，不再需要专门的 wrapper 类。

```diff
-// OLD: 旧的 MaidHandsInvWrapper.java（已删除）
-public class MaidHandsInvWrapper extends EntityHandsInvWrapper {
-    public MaidHandsInvWrapper(LivingEntity entity) {
-        super(entity);
-    }
-    @Override
-    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
-        return EntityMaid.canInsertItem(stack);
-    }
-}

+// NEW: EntityMaid 直接暴露 ResourceHandler<ItemResource>（见 MaidItemManager）
```

---

## 4. 方法级 API 对照表

### 4.1 ItemStackHandler 方法

| 旧方法 (`ItemStackHandler`) | 新方法 (`ItemStacksResourceHandler`) | 说明 |
|---------------------------|--------------------------------------|------|
| `getSlots()` | `size()` | 获取槽位数量 |
| `getStackInSlot(slot)` | `ItemUtil.getStack(this, slot)` | 获取 ItemStack（改为静态工具方法） |
| `setStackInSlot(slot, stack)` | `ItemUtil.set(this, slot, resource, amount)` | 设置物品 |
| `isItemValid(slot, ItemStack)` | `isValid(slot, ItemResource)` | 验证物品（参数从 ItemStack 变为 ItemResource） |
| `getSlotLimit(slot)` | `getCapacity(index, ItemResource)` | 获取单格上限（增加 ItemResource 参数） |
| `insertItem(slot, ItemStack, simulate)` | `insert(slot, ItemResource, amount, parent)` | 插入（增加 TransactionContext，返回值从 ItemStack 变为 int） |
| `extractItem(slot, amount, simulate)` | `extract(slot, ItemResource, amount, parent)` | 提取（增加 ItemResource 和 TransactionContext） |
| `onContentsChanged(slot)` | `onContentsChanged(slot, ItemStack previousStack)` | 内容变更回调（新增 previousStack 参数） |
| `onLoad()` | **已移除** | 反序列化回调不再存在 |
| `stacks` (protected field) | `stacks` (protected field) | 仍然存在，类型仍是 `NonNullList<ItemStack>` |

### 4.2 insertItem 签名变化（关键）

```diff
// OLD — ItemStackHandler
-@Nonnull public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate)

// NEW — ItemStacksResourceHandler
+public int insert(int slot, @Nonnull ItemResource resource, int amount, @Nonnull TransactionContext parent)
```

**关键差异**:
1. 参数从 `ItemStack` 变为 `ItemResource` + `int amount`
2. 旧 `simulate` boolean → 新 `parent` TransactionContext（嵌套事务支持）
3. 返回值从 `ItemStack`（剩余物品） → `int`（实际插入数量）

### 4.3 isItemValid → isValid

```diff
// OLD
-public boolean isItemValid(int slot, @Nonnull ItemStack stack)
+public boolean isValid(int slot, @Nonnull ItemResource resource)
```

新旧差异：参数 `ItemStack → ItemResource`，新建一个 `ItemResource` 用 `ItemResource.of(ItemStack)`。

### 4.4 getStackInSlot → ItemUtil.getStack

```diff
// OLD
-ItemStack stack = handler.getStackInSlot(slot);

// NEW
+import net.neoforged.neoforge.transfer.item.ItemUtil;
+ItemStack stack = ItemUtil.getStack(handler, slot);
```

`getStackInSlot()` 不再直接存在于 handler 上，改为通过 `ItemUtil` 静态方法访问。

### 4.5 getSlotLimit → getCapacity

```diff
// OLD AltarItemHandler
-public int getSlotLimit(int slot) { return 1; }

// NEW AltarItemHandler
+protected int getCapacity(int index, @NotNull ItemResource resource) { return 1; }
```

### 4.6 ItemHandlerHelper → ItemResourceUtil

```diff
// OLD
-import net.neoforged.neoforge.items.ItemHandlerHelper;
-ItemHandlerHelper.insertItemStacked(handler, itemstack, simulate);

// NEW
+import net.neoforged.neoforge.transfer.item.ItemResourceUtil;
+// 注意：insertItemStacked 在新 API 中的等价方法签名变化
+// 实际使用中可通过 ResourceHandler.insert() 配合 TransactionContext 实现
```

---

## 5. Slot 变化详解

### 5.1 构造器变化

```diff
// OLD — SlotItemHandler 使用 IItemHandler
-new SlotItemHandler(IItemHandler handler, int index, int x, int y)

// NEW — ResourceHandlerSlot 使用 ResourceHandler<ItemResource> + IndexModifier
+new ResourceHandlerSlot(ResourceHandler<ItemResource> handler, IndexModifier<ItemResource> indexModifier, int index, int x, int y)
```

`IndexModifier<ItemResource>` 是新引入的索引修饰器，用于将 slot 索引映射到实际的 handler 索引。创建方式：

```java
// 通过 Lambda 直接创建（最简单）
var indexModifier = ItemsUtil.createIndexModifier(capability);
// 等效于
var indexModifier = (idx) -> idx;

// 通过方法引用（用于 BackpackSlot）
new ResourceHandlerSlot(maidInv, maidInv::set, index, x, y)
```

### 5.2 getNoItemIcon() 返回值变化

```diff
// OLD — 返回 Pair<ResourceLocation, ResourceLocation>
-@OnlyIn(Dist.CLIENT)
-public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
-    return Pair.of(BLOCK_ATLAS, EMPTY_MAINHAND_SLOT);
-}

// NEW — 返回单个 Identifier
+@Override
+public Identifier getNoItemIcon() {
+    return EMPTY_MAINHAND_SLOT;
+}
```

不需要指定 atlas，Minecraft 26.1 自动处理。

---

## 6. EntityMaid 库存方法变化

### 6.1 返回类型全部更新

| 旧方法（EntityMaid） | 旧返回类型 | 新方法（MaidItemManager） | 新返回类型 |
|---------------------|-----------|-------------------------|-----------|
| `getMaidInv()` | `ItemStackHandler` | `getMaidInv()` | `ItemStacksResourceHandler` |
| `getAvailableInv(boolean)` | `CombinedInvWrapper` | `getAvailableInv(boolean)` | `CombinedResourceHandler<ItemResource>` |
| `getAllInv()` | `CombinedInvWrapper` | `getAllInv()` | `CombinedResourceHandler<ItemResource>` |
| `getHandsInvWrapper()` | `EntityHandsInvWrapper` | `getHandsInvWrapper()` | `ResourceHandler<ItemResource>` |
| `getArmorInvWrapper()` | `EntityArmorInvWrapper` | `getHandsInvWrapper()` | `ResourceHandler<ItemResource>` |
| `getMaidBauble()` | `BaubleItemHandler` | `getMaidBauble()` | `BaubleItemHandler`（父类变了） |

### 6.2 方法从 EntityMaid 迁移到 MaidItemManager

旧项目中 `getAvailableInv()`、`getAllInv()` 等直接在 `EntityMaid` 类中定义。新项目中这些方法移到了 `MaidItemManager` 组件中。

```diff
// OLD
-EntityMaid maid = ...;
-maid.getAvailableInv(false);

// NEW
+MaidItemManager itemManager = maid.getItemManager();
+itemManager.getAvailableInv(false);
```

### 6.3 对调用者的影响

所有使用 `getAvailableInv()` 的地方，返回类型从 `CombinedInvWrapper` 变为 `CombinedResourceHandler<ItemResource>`，变量声明也需要更新：

```diff
// OLD
-IItemHandler inv = maid.getAvailableInv(true);
-CombinedInvWrapper inv = maid.getAvailableInv(false);
-ItemStack stack = inv.getStackInSlot(i);

// NEW
+var inv = maid.getItemManager().getAvailableInv(true);
+CombinedResourceHandler<ItemResource> inv = maid.getItemManager().getAvailableInv(false);
+ItemStack stack = ItemUtil.getStack(inv, i);
```

---

## 7. 背包型物品处理器 (Backpack Handler) 具体变更

### 7.1 MaidBackpackHandler

```diff
-import net.neoforged.neoforge.items.ItemStackHandler;
+import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
+import net.neoforged.neoforge.transfer.item.ItemResource;

-public class MaidBackpackHandler extends ItemStackHandler {
+public class MaidBackpackHandler extends ItemStacksResourceHandler {
     public static final int BACKPACK_ITEM_SLOT = 5;
     private final EntityMaid maid;

     public MaidBackpackHandler(int size, EntityMaid maid) {
         super(size);
         this.maid = maid;
     }

     @Override
-    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
+    public boolean isValid(int slot, @Nonnull ItemResource resource) {
-        return EntityMaid.canInsertItem(stack);
+        return MaidItemManager.canInsertItem(resource.toStack());
     }

     @Override
-    protected void onContentsChanged(int slot) {
+    protected void onContentsChanged(int slot, @Nonnull ItemStack previousStack) {
         if (slot == BACKPACK_ITEM_SLOT) {
-            maid.setBackpackShowItem(this.getStackInSlot(slot));
+            maid.setBackpackShowItem(ItemUtil.getStack(this, slot));
         }
     }
 }
```

### 7.2 AltarItemHandler

```diff
-import net.neoforged.neoforge.items.ItemStackHandler;
+import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
+import net.neoforged.neoforge.transfer.item.ItemResource;

-public class AltarItemHandler extends ItemStackHandler {
+public class AltarItemHandler extends ItemStacksResourceHandler {
+    public AltarItemHandler() {
+        super(1);
+    }

     @Override
-    public int getSlotLimit(int slot) {
+    protected int getCapacity(int index, @NotNull ItemResource resource) {
         return 1;
     }
 }
```

注意 `ItemStacksResourceHandler` 需要调用 `super(size)` 来指定大小，而旧 `ItemStackHandler` 默认大小为 1。

---

## 8. Capability 注册变化

```diff
-import net.neoforged.neoforge.items.IItemHandler;
+import net.neoforged.neoforge.transfer.ResourceHandler;
+import net.neoforged.neoforge.transfer.item.ItemResource;

-public static final EntityCapability<IItemHandler, @Nullable Direction> HAND_ITEM =
-    EntityCapability.createSided(getResourceLocation("hand_item"), IItemHandler.class);
+public static final EntityCapability<ResourceHandler<ItemResource>, @Nullable Direction> HAND_ITEM =
+    EntityCapability.createSided(getResourceLocation("hand_item"), ResourceHandler.asClass());

-public static final EntityCapability<IItemHandler, @Nullable Direction> ARMOR_ITEM =
-    EntityCapability.createSided(getResourceLocation("armor_item"), IItemHandler.class);
+public static final EntityCapability<ResourceHandler<ItemResource>, @Nullable Direction> ARMOR_ITEM =
+    EntityCapability.createSided(getResourceLocation("armor_item"), ResourceHandler.asClass());

// Forge兼容cap也变了:
-event.registerEntity(Capabilities.ItemHandler.ENTITY, InitEntities.MAID.get(), ...)
+event.registerEntity(Capabilities.Item.ENTITY, InitEntities.MAID.get(), ...)
```

---

## 9. 本模组中受影响的文件清单

### 需要修改父类的文件

| 文件 | 旧父类 | 新父类 |
|------|--------|--------|
| `MaidBackpackHandler.java` | `ItemStackHandler` | `ItemStacksResourceHandler` |
| `BaubleItemHandler.java` | `ItemStackHandler` | `ItemStacksResourceHandler` |
| `AltarItemHandler.java` | `ItemStackHandler` | `ItemStacksResourceHandler` |
| `MaidInvWrapper.java` | `CombinedInvWrapper` | `CombinedResourceHandler<ItemResource>` |
| `BackpackSlot` (inner in MaidMainContainer) | `SlotItemHandler` | `ResourceHandlerSlot` |

### 需要修改返回类型的文件

| 文件 | 旧返回类型 | 新返回类型 |
|------|-----------|-----------|
| `EntityMaid.java` / `MaidItemManager.java` | `CombinedInvWrapper` | `CombinedResourceHandler<ItemResource>` |
| `EntityMaid.java` / `MaidItemManager.java` | `ItemStackHandler` | `ItemStacksResourceHandler` |

### 已移除的文件

| 文件 | 原因 |
|------|------|
| `MaidHandsInvWrapper.java` | `EntityHandsInvWrapper` 在新平台已移除 |

### 所有调用点（需更新 IItemHandler → ResourceHandler）

以下文件包含对 `getAvailableInv()` / `getAllInv()` 的调用，返回类型由 `IItemHandler`/`CombinedInvWrapper` 变为 `CombinedResourceHandler<ItemResource>`：

- `entity/ai/brain/task/MaidFarmMoveTask.java`
- `entity/ai/brain/task/MaidFarmPlantTask.java`
- `entity/ai/brain/task/MaidTorchPlaceTask.java`
- `entity/ai/brain/task/MaidTorchMoveTask.java`
- `entity/ai/brain/task/MaidStealEdibleMoveBlockTask.java`
- `entity/ai/brain/task/MaidStealEdibleUseTask.java`
- `entity/ai/brain/task/MaidFeedOwnerTask.java`
- `entity/ai/brain/task/MaidFeedAnimalTask.java`
- `entity/ai/brain/task/MaidMilkTask.java`
- `entity/ai/brain/task/MaidCollectHoneyTask.java`
- `entity/ai/brain/task/MaidHomeMealTask.java`
- `entity/task/TaskTorch.java`
- `entity/task/TaskHoney.java`
- `entity/task/TaskMilk.java`
- `entity/task/TaskBowAttack.java`
- `entity/task/TaskCrossBowAttack.java`
- `entity/task/TaskFeedAnimal.java`
- `entity/task/crop/NetherWartCropHandler.java`
- `item/bauble/WirelessIOBauble.java`
- `item/ItemWirelessIO.java`
- `item/ItemPicnicBasket.java`
- `util/ItemsUtil.java`
- `client/tooltip/ClientItemContainerTooltip.java`

---

## 10. 迁移检查清单

将旧项目中的物品存储代码迁移到新 API：

- [ ] `extends ItemStackHandler` → `extends ItemStacksResourceHandler`
- [ ] `extends IItemHandler` → `implements ResourceHandler<ItemResource>` 或 extends 实现类
- [ ] `new SlotItemHandler(...)` → `new ResourceHandlerSlot(...)` 需加 `IndexModifier`
- [ ] `getStackInSlot(slot)` → `ItemUtil.getStack(this, slot)`
- [ ] `getSlots()` → `size()`
- [ ] `isItemValid(slot, stack)` → `isValid(slot, resource)`
- [ ] `getSlotLimit(slot)` → `getCapacity(index, resource)`
- [ ] `insertItem(slot, stack, simulate)` → `insert(slot, resource, amount, parent)`
- [ ] `onContentsChanged(slot)` → `onContentsChanged(slot, previousStack)`
- [ ] `onLoad()` 的实现迁移到构造器或手动调用
- [ ] `IItemHandler` 变量声明 → `ResourceHandler<ItemResource>` 或 `var`
- [ ] `CombinedInvWrapper` → `CombinedResourceHandler<ItemResource>`
- [ ] `RangedWrapper` → `RangedResourceHandler.of(...)`
- [ ] `EntityCapability<IItemHandler, ...>` → `EntityCapability<ResourceHandler<ItemResource>, ...>`
- [ ] `Capabilities.ItemHandler.ENTITY` → `Capabilities.Item.ENTITY`
- [ ] `EntityHandsInvWrapper` 和 `EntityArmorInvWrapper` → `ResourceHandler<ItemResource>`
- [ ] `getNoItemIcon()` 返回从 `Pair<RL,RL>` → 单个 `Identifier`

---

## 11. 与已有迁移文档的关系

- **15-vanilla-screen-menu.md** 中已经记录了 `SlotItemHandler → ResourceHandlerSlot` 的 Container/Slot 层变化
- **02-entity.md** 记录了 EntityMaid 拆分到 Manager 类中（`getAvailableInv()` 迁移到 `MaidItemManager`）
- 本文档聚焦于底层存储 API (`IItemHandler` → `ResourceHandler`) 的完整迁移，是以上两者的补充
