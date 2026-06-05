# Deleted Functionality (Core Project)

> 核验日期: 2026-06-04
> 记录核心项目迁移中仍被移除、注释或退化的功能。
> 当前活跃 Mixin: 5 个；Access Transformer 条目: 20 个。
> 兼容模组相关禁用见 [05-integration-disabled.md](./05-integration-disabled.md)。

---

## 核心功能状态

### 女仆任务系统

#### CONFIGURABLE_COMMUNICATE_MARK Bauble - 已恢复

- **位置**: `maid/MaidExtension.java:45`
- **当前状态**: `manager.bind(ItemRegistry.CONFIGURABLE_COMMUNICATE_MARK.get(), ...)` 已恢复。
- **旧文档状态**: 曾记录为注释掉，现已失效。

#### StorageManageTask - 已恢复

- `StorageManageTask` 及 logistics 子行为均为活跃 `.java`。
- `MaidExtension.addMaidTask()` 当前注册 `new StorageManageTask()`。

---

## 合成系统

### 基础 CraftType 和 Action - 已恢复

- 8 个基础 Type 已恢复: CommonType, CraftingType, AltarType, FurnaceType, BrewingType, SmithingType, AnvilType, StoneCuttingType。
- 基础 Action 和虚拟 Action 注册已恢复。
- `CraftManager.fireInternal()` 当前注册基础 Type、Action 和基础 generator。

### 兼容模组 Craft 注册

- AE2 craft type/action 和 AE2 generator 已恢复。
- RS、TACZ、Create、Mekanism、Botania、Ars Nouveau 注册块仍在 `CraftManager.java` 中注释，详见 `05-integration-disabled.md`。
- Botania generator 文件虽然是 `.java`，但文件内容整体注释，实际仍不可用。

---

## 存储系统

### ItemHandler 存储 - 已恢复

`storage/ItemHandler` 当前有 7 个活跃 `.java`:

- `AbstractItemHandlerContext.java`
- `ChestMultiBlockProcessor.java`
- `ContextItemHandlerCollect.java`
- `ContextItemHandlerStore.java`
- `ContextItemHandlerView.java`
- `ItemHandlerStorage.java`
- `SimulateTargetInteractHelper.java`

### 兼容模组存储

- AE2 storage 已恢复并在 `MaidStorage` 中按配置注册。
- RS、Create、QIO storage 注册仍整块注释。
- Sophisticated Storage 多方块处理器已恢复，`MaidStorage` 当前按 `Integrations.sophisticatedStorage()` 注册 `SophisticatedStorageMultiBlock`。
- Create multi-block vault 处理器仍注释。

---

## 渲染系统

### ItemStackLighting - 已删除

- **文件**: `render/ItemStackLighting.java` 未在当前项目中找到。

### RenderItemFrameEvent - 已恢复

- **文件**: `event/RenderItemFrameEvent.java`
- **当前状态**: 活跃，已迁移到 NeoForge `RenderItemInFrameEvent`，并通过 `SubmitNodeCollector` 渲染 map-like item。

### BoxRenderUtil / VirtualItemEntityRender / VirtualDisplayEntityRender - 已恢复

- `util/BoxRenderUtil.java` 活跃。
- `entity/VirtualItemEntityRender.java` 活跃。
- `entity/VirtualDisplayEntityRender.java` 活跃，旧 FIXME 已移除。

### ModelBaked - 简化保留

- **文件**: `event/ModelBaked.java`
- **当前状态**: 仅注册 `craft_guide_blank` 的 `SimpleUnbakedStandaloneModel`。
- **旧功能**: BEWLR 动态物品渲染注册逻辑已移除。

---

## 事件系统

5 个客户端事件文件均已恢复并适配当前事件 API:

| 文件 | 当前状态 |
|------|----------|
| `BindingRender.java` | 活跃 |
| `BindingRenderSyncSender.java` | 活跃 |
| `InputEvent.java` | 活跃 |
| `PlayerInteractClient.java` | 活跃 |
| `TickClient.java` | 活跃 |

---

## 注解和旧兼容钩子

### `@OnlyIn` - 当前源码中无残留

IDE 搜索确认 `src/main/java` 中未找到 `@OnlyIn`。

### IPN / MouseTweaks 条件注入 - 当前源码中无残留

IDE 搜索确认 `inventoryprofilesnext` 和 `mouse_tweaks` 旧注解/条件代码均未找到。`build.gradle` 中仍保留 IPN/MouseTweaks compileOnly 依赖，但源码层旧钩子已清理。

### Tour Guide 注册 - 仍基本停用

- `integration/tour_guide/TourGuideRegister.java` 和 `ScreenPredicator.java` 文件内容整体注释。
- `InventoryListTour.java` 内容整体注释。
- `RequestListTour.java` 和 `CraftGuideTour.java` 当前只是空类。
- `RequestListItem` 与 `InventoryListScreen` 中仍有 TourGuideTrigger 相关注释。

---

## 网络数据包

### 已恢复注册的包

| 数据包 | 当前状态 |
|--------|----------|
| `ItemSelectorSetItemPacket` | 已注册并调用 `filteredItems.setItem(...)`、`save()`、`broadcastChanges()` |
| `CraftGuideGuiPacket` | 已双向注册，handler 调用 `ICraftGuiPacketReceiver.handleGuiPacket(...)` |
| `CommunicateMarkGuiPacket` | 已双向注册，handler 调用 `CommunicateMarkGuiPacket.handle(...)` |
| `CraftGuideGeneratorUpdate` | 已双向注册，handler 调用 `CraftGuideGeneratorUpdate.handle(...)` |
| `IngredientRequestC2SPacket` / `IngredientRequestResultS2CPacket` | 已注册，用于 ingredient request |
| `AIMatchLocalizedItemS2CPacket` | 已注册 |

### 仍退化或等待集成的 handler

| 数据包 | 位置 | 当前状态 |
|--------|------|----------|
| `CreateStockManagerPacket` | `Network.java:291-308` | 包已注册，但 handler 只有 `// TODO wait create`，原 `StockManagerInteract` 逻辑注释 |

### 已替换的旧 bauble 同步逻辑

- 旧文档中的 `MaidDataSyncToClientPacket` 当前未找到。
- 当前为 `MaidBaubleSyncPacket`，handler 使用 `Transaction` 对 `BaubleItemHandler` extract/insert 同步。

---

## GUI/菜单

### ClientGuiRegistry - 已恢复

- `registry/ClientGuiRegistry.java` 活跃。
- 所有核心 Screen -> MenuType 绑定已恢复。
- `TaczCraftScreen` 注册仍注释: `ClientGuiRegistry.java:39`。

### LogisticsGuideMenu 分支 - 已恢复

- `Network.java:90-92` 当前已处理 `sender.containerMenu instanceof LogisticsGuideMenu`。

---

## 资源文件

- `src/main/resources/assets/maid_storage_manager/models/**` 当前未找到静态 JSON model。
- `src/generated/resources/assets/maid_storage_manager/models/item/` 当前有 16 个生成 item model JSON。
- `src/generated/resources/data/maid_storage_manager/tags/` 当前有 10 个生成 tag JSON。
- 配方 JSON 由 `RecipeDataGen` 生成到 `src/generated/resources`。

---

## Mixin 配置

当前 `maid_storage_manager.mixins.json` 活跃 Mixin (5 个):

```json
{
  "mixins": [
    "ContainerOpenersCounterPatch",
    "ItemFrameTickMixin",
    "JEIRecipeTransferHook"
  ],
  "client": [
    "ItemFrameRendererMixin",
    "ItemFrameRenderStateMixin"
  ]
}
```

禁用的核心 Mixin (2 个):

- `AltarRecipeMultiOutputMixin.java.disabled`
- `LivingEntityBrainSerializeWrapper.java.disabled`

兼容模组 disabled Mixin:

- `EMIRecipeTransferHook.java.disabled`
- `CreateStockTickerBEMixin.java.disabled`
- `CreateStockKeeperMenuMixin.java.disabled`
- `CreateStockKeeperScreenMixin.java.disabled`

---

## Access Transformer

当前 `accesstransformer.cfg` 保留 20 个条目。

---

## 更新记录

| 日期 | 变更 |
|------|------|
| 2026-06-04 | 重新核验删除/注释状态；修正活跃 mixin 数、AT 条目数、网络包恢复状态、bauble 绑定、RenderItemFrameEvent、资源生成状态 |
| 2026-06-02 | 旧版迁移记录 |
