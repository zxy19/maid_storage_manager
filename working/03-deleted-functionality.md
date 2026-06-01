# Deleted Functionality

> 记录迁移过程中被删除/移除/注释掉的功能、代码片段、资源文件。
> 总计: 134 个文件被禁用 (.java.disabled), 元数据: 3 个 active mixin, 40+ 处代码注释, 28 个 JSON 模型/标签删除, 10+ 个 access transformer 条目删除。
> 自上次更新以来: 27 个文件已恢复为 active `.java`

---

## 核心功能移除

### 女仆任务系统

#### CONFIGURABLE_COMMUNICATE_MARK Bauble
- **位置**: `MaidExtension.java:45`
- **状态**: 注释掉 `manager.bind(ItemRegistry.CONFIGURABLE_COMMUNICATE_MARK.get(), ...)`
- **影响**: 可配置通信标记饰品的女仆饰品绑定被禁用

> 注: `StorageManageTask` 及 3 个子行为类已恢复为 active（见恢复记录）。

---

### 合成系统 (CraftManager)

#### 基础 CraftType 和 Action 已全部恢复
- 8 个基础 Type (`CommonType`, `CraftingType`, `AltarType`, `FurnaceType`, `BrewingType`, `SmithingType`, `AnvilType`, `StoneCuttingType`) 已恢复
- 6 个 Action (`AltarRecipeAction`, `VirtualAction`×2, `SmithingRecipeAction`, `AnvilRecipeAction`, `StoneCuttingRecipeAction`) 已恢复
- `CraftManager.java:75-82` 和 `:165-224` 的注册均已解除注释

#### 仍在禁用的 Craft 类型/生成器
- **兼容模组 Type (3个)**: `AE2Type.java.disabled`, `RSType.java.disabled`, `TaczType.java.disabled`
- **兼容模组 Action (3个)**: `AeCraftingAction.java.disabled`, `RsCraftingAction.java.disabled`, `TaczRecipeAction.java.disabled`
- **兼容模组 Generator**: AE2 (3), RS (0), Create (9), Mekanism (8), Ars Nouveau (4), Botania (0 但注册注释), TACZ (1)
- **原因**: 依赖模组均未发布 MC 26.1 兼容版本

---

### 存储系统

#### ItemHandler 存储已恢复
- 5 个 `storage/ItemHandler/*.java` 文件已从 `.disabled` 恢复为 active
- `AbstractItemHandlerContext`, `ContextItemHandlerCollect`, `ContextItemHandlerStore`, `ContextItemHandlerView`, `SimulateTargetInteractHelper`

#### 仍在禁用的兼容模组存储
- **AE2 5 个** (等待 AE2 更新)
- **RS 5 个** (等待 RS 更新)
- **QIO 5 个** (等待 Mekanism 更新)
- **Create 5 个** (等待 Create 更新)

---

## 集成系统

### JEI 集成 (部分恢复)
- **已恢复 (7 文件)**:
  - `integration/jei/Plugin.java`
  - `integration/jei/GhostIngredientHandler.java`
  - `integration/jei/IFilterScreen.java`
  - `integration/jei/RequestRecipeHandler.java`
  - `menu/craft/base/handler/JEIRecipeHandler.java`
  - `menu/craft/common/JEICommonRecipeHandler.java`
  - `menu/craft/stone_cutter/JeiStoneCutterRecipeHandler.java`
- **仍禁用 (6 文件)**:
  - `integration/request/JEIClient.java.disabled`
  - `integration/request/JEIRequestDisplayError.java.disabled`
  - `mixin/JeiGuiIconToggleButtonAccessor.java.disabled`
  - `mixin/JEIRecipeTransferHook.java.disabled`
  - `api/mixin/IJEIButtonGetter.java.disabled`
  - `integration/request/IngredientRequest.java.disabled` 和 `IngredientRequestClient.java.disabled`
- **原因**: 核心 API 适配完成，mixin 和请求系统仍等待 JEI MC 26.1 版本发布

### EMI 集成 (8 个文件)
- **核心文件**: `Plugin.java.disabled`, `GhostIngredientHandler.java.disabled`, `RequestRecipeHandler.java.disabled`
- **Mixin**: `EMIRecipeTransferHook.java.disabled`
- **配方处理器**: `EmiRecipeHandler.java.disabled`, `EmiCommonRecipeHandler.java.disabled`, `EmiStoneCutterRecipeHandler.java.disabled`
- **请求系统**: `EMIClient.java.disabled`
- **原因**: EMI 暂无 MC 26.1 兼容版本

### Cloth Config 集成 (2 个文件)
- **文件**: `integration/cloth/AddClothEvent.java.disabled`, `integration/cloth/ClothEntry.java.disabled`
- **启动注册**: `StartUpEvent.java:28-32` — ClothEntry.registryConfigPage() + AddClothEvent 事件注册被注释
- **原因**: Cloth Config 包名/API 变化

### Create 交互 (StockManagerInteract)
- **位置**: `Network.java` + handler 内容注释
- **包**: `integration/create/StockManagerInteract.java.disabled`
- **功能**: 处理女仆与 Create StockKeeper 的库存管理/购物列表交互
- **影响**: `CreateStockManagerPacket` 的 OPEN_SCREEN 和 SHOP_LIST 分支不可用

### Ingredient Request 系统
- **文件**: `integration/request/IngredientRequest.java.disabled`, `IngredientRequestClient.java.disabled`
- **功能**: JEI/EMI 到请求系统的桥梁
- **影响**: `JEIRequestPacket` 服务器端处理被注释掉，导致从 JEI/EMI 界面请求物品的功能丧失

### KubeJS 集成 (34 个文件)
- **全部禁用**: `integration/kubejs/` 下所有文件
- **原因**: KubeJS 暂无 MC 26.1 兼容版本

### 其他兼容模组
- **The One Probe**: `integration/top/` 目录已完全移除，无任何遗留代码
- **Jade**: 2 个文件被禁用，等待 API 适配
- **Sophisticated Storage**: 1 个文件被禁用
- **TACZ**: 8 个文件全部禁用

---

## 网络数据包

### 被注释/禁用的网络包 (5 个)

| 数据包 | 位置 | 状态 |
|--------|------|------|
| `ItemSelectorSetItemPacket` | Network.java:96-114 | 注册注释掉 |
| `CraftGuideGuiPacket` | Network.java:200-213 | 双向注册注释掉 |
| `CommunicateMarkGuiPacket` | Network.java:308-317 | 双向注册注释掉 |
| `CraftGuideGeneratorUpdate` | Network.java:318-327 | 双向注册注释掉 |
| `AIMatchLocalizedItemS2CPacket` | Network.java:337-344 | 客户端注册注释掉 |

### Handler 内容被注释但注册保留的包 (4 个)

| 数据包 | 位置 | 被注释内容 |
|--------|------|-----------|
| `JEIRequestPacket` | Network.java:237-238 | `IngredientRequest.onRequest()` 调用 |
| `JEIRequestResultPacket` | Network.java:244-248 | `InScreenTipData.show()` 调用 |
| `MaidDataSyncToClientPacket` | Network.java:265-266 | bauble `deserializeNBT()` 调用 |
| `CreateStockManagerPacket` | Network.java:277-283 | `StockManagerInteract.handle()` 调用 |

---

## GUI/菜单注册删除

### ClientGuiRegistry — 已恢复
- **文件**: `registry/ClientGuiRegistry.java`（已从删除状态恢复并激活）
- **注册内容**: 所有 Screen → MenuType 的客户端注册绑定均已恢复
- **仍注释**: `TaczCraftScreen` 注册（等待 TACZ 恢复）

### StorageManagerMaidConfigGui — 已恢复
- **文件**: `maid/config/StorageManagerMaidConfigGui.java`（已从 `.disabled` 恢复为 active）
- **描述**: 女仆配置 GUI 界面

---

## 渲染系统删除

### RenderItemFrameEvent 旧逻辑 — 完全移除
- **旧文件**: `event/RenderItemFrameEvent.java` (不存在于当前代码库)
- **旧功能**: 使用 `RenderLevelStageEvent` 在 WORLD_AFTER_ENTITIES 阶段渲染虚拟物品展示框
- **原因**: MC 26.1 渲染管线使用 SubmitNodeCollector 替代 MultiBufferSource

### ItemStackLighting — 移除
- **文件**: `render/ItemStackLighting.java.disabled`
- **原因**: MC 26.1 渲染管线中光照计算方式变化

### BoxRenderUtil — 禁用
- **文件**: `util/BoxRenderUtil.java.disabled`
- **原因**: RenderLevelStageEvent API 变化

### VirtualDisplayEntityRender — 部分禁用
- **文件**: `entity/VirtualDisplayEntityRender.java` (编译通过但功能不完整)
- **禁用部分**: CORNER/LARGE/ICON 渲染模式仅抛注释，无实际渲染代码
- **原因**: EntityRenderer 从 `render()` 迁移至 `submit()`

### ModelBaked — 旧 BlockEntityWithoutLevelRenderer 逻辑移除
- **位置**: `event/ModelBaked.java`
- **旧功能**: 通过 `BEWLRProperties` 注册 BlockEntityWithoutLevelRenderer 实现动态物品渲染
- **当前状态**: 仅使用 `SimpleUnbakedStandaloneModel` 注册 3 个基础材质

---

## 事件系统删除

### 5 个客户端事件文件 — 全部禁用
| 文件 | 旧用途 | 原因 |
|------|--------|------|
| `BindingRender.java.disabled` | 女仆背包/库存绑定渲染 | MC 26.1 SubmitNodeCollector 管线 |
| `BindingRenderSyncSender.java.disabled` | 绑定渲染同步发送器 | `@OnlyIn` 移除 + 事件 API 变化 |
| `InputEvent.java.disabled` | 客户端输入事件处理 | KeyMapping 构造器变化 |
| `PlayerInteractClient.java.disabled` | 客户端交互事件 | 事件 API 变化 |
| `TickClient.java.disabled` | 客户端 tick 事件 | 事件 API 变化 |

---

## 注解删除

### @OnlyIn(Dist.CLIENT) — 11 处移除
MC 26.1 中 `@OnlyIn` 语义变化。以下 11 处在迁移中被移除（保留的 10 处已为方法级标注）：

| 移除位置 | 说明 |
|----------|------|
| `Registry.java` 类级别 | 注册类不再需要 client-only 条件 |
| `ClientRegistry.java` 类级别 | 同上 |
| `ClientEventRegistry.java` | 客户端事件注册 |
| `RenderItemFrameEvent.java` (已删除) | 渲染事件类 |
| `InputEvent.java` (已禁用) | 输入事件 |
| `TickClient.java` (已禁用) | 客户端 tick |
| `PlayerInteractClient.java` (已禁用) | 客户端交互 |
| `BindingRender.java` (已禁用) | 绑定渲染 |
| `BindingRenderSyncSender.java` (已禁用) | 渲染同步 |
| `ClientGuiRegistry.java` (已恢复) | 客户端 GUI 注册 |
| `BoxRenderUtil.java` (已禁用) | 渲染工具 |

- **注意**: 仍有 10 处 `@OnlyIn(Dist.CLIENT)` 保留在活跃代码中（`InventoryListDataClient`, `CraftingChatBubbleData`, `JoinLevelEvent`, `VirtualDisplayEntityRender`, `WrittenInvListItem`, `SlotType.drawGold()` 等）

### @Mod("ipn"/"mouse_tweaks") — 14 处移除
由于 IPN 和 MouseTweaks 两个模组在 MC 26.1 生态中不再以 `@Mod` 注解条件注入的方式工作：
- **IPN 相关**: `@Mod("inventoryprofilesnext")` 条件注入注解从 ~8 个地方移除
- **MouseTweaks 相关**: `@Mod("mouse_tweaks")` 条件注入注解从 ~6 个地方移除
- **原因**: 这些注入主要是为了在物品选择器中屏蔽 IPN 的快捷键干扰和 MouseTweaks 的右键行为

---

## 资源文件删除

### JSON 模型 (17 个 — 无变化)
（列表同上次，未恢复，使用 `SimpleUnbakedStandaloneModel` 替代）

### JSON 标签 (10 个 — 无变化)
（列表同上次，新标签通过 datagen 生成在 `src/generated/resources/`）

---

## Mixin 配置变更

### 当前活跃 Mixin (3 个)

```json
{
  "mixins": [
    "ContainerOpenersCounterPatch",
    "ItemFrameTickMixin"
  ],
  "client": [
    "ItemFrameRendererMixin"
  ]
}
```

### 从 mixins.json 移除的 Mixin (其余均保留 `.disabled` 文件)

**Create 相关 (3 个):** `CreateStockKeeperMenuMixin`, `CreateStockKeeperScreenMixin`, `CreateStockTickerBEMixin`

**JEI/EMI 相关 (3 个):** `JeiGuiIconToggleButtonAccessor`, `JEIRecipeTransferHook`, `EMIRecipeTransferHook`

**等待 TLM API 适配 (2 个):** `AltarRecipeMultiOutputMixin`, `LivingEntityBrainSerializeWrapper`

---

## Access Transformer 条目删除

当前 `accesstransformer.cfg` 保留 **19 个条目**（无变化）：
```
public-f Slot.x / Slot.y                   — GUI 槽位位置访问
public-f Player inventory                   — 物品栏访问
public-f Entity getX/Y/Z/getEyeHeight       — 实体位置/视线
public ItemFrameRenderer.itemRenderer       — 展示框渲染器
public AnvilMenu onTake                     — 铁砧取出
public PotionBrewing (3 个)                 — 酿造配方信息
public SmithingTransformRecipe (4 个)       — 锻造配方信息
public RenderSystem.shaderLightDirections   — 光照方向
public LivingEntity.updatingUsingItem       — 物品使用状态
public Entity.equipment                     — 实体装备访问
```

---

## 杂项删除

### TourGuideTrigger 调用 (1 处)
- **位置**: `items/RequestListItem.java:111`
- **内容**: `//TODO bind Trigger`
- **原因**: TourGuide 依赖被移除

### getMaidDebugTargets 条件化
- **位置**: `MaidExtension.java:106-113`
- **变更**: 添加 `if (!Config.aiFunctions) return List.of();` 守卫
- **原因**: 当 AI 功能被禁用时不注册调试目标

### LogisticsGuideMenu 界面引用
- **位置**: `Network.java:88-92`
- **内容**: `ItemSelectorGuiPacket` handler 中的 `LogisticsGuideMenu` 分支被注释
- **原因**: 级联禁用

### CraftGuideGuiPacket + ICraftGuiPacketReceiver
- **位置**: `Network.java:200-213`
- **内容**: 整个双向包注册被注释
- **原因**: 引用的 `ICraftGuiPacketReceiver` 接口被禁用

### BaubleItemHandler.deserializeNBT
- **位置**: `Network.java:265-266` — `MaidDataSyncToClientPacket` handler
- **内容**: `maid.getMaidBauble().deserializeNBT(sender.registryAccess(), msg.value)` 被注释
- **原因**: TLM 26.1 中 BaubleItemHandler.deserializeNBT 签名变化

### 兼容模组 InterModComms
- **状态**: `integration/top/` 目录已完全移除
- **内容**: 无 TheOneProbe 相关代码残留

---

## 自上次文档更新以来的恢复记录

| 分类 | 恢复文件数 | 文件 |
|------|-----------|------|
| Craft Types | 8 | CommonType, CraftingType, AltarType, FurnaceType, BrewingType, SmithingType, AnvilType, StoneCuttingType |
| JEI Integration | 7 | integration/jei/* (4) + menu/craft/ JEI handlers (3) |
| Maid 行为/任务 | 4 | StorageManageTask, LogisticsOutputBehavior, LogisticsRecycleBehavior, WriteInventoryListBehavior |
| ItemHandler 存储 | 5 | storage/ItemHandler/* (5) |
| 其他 | 3 | GeneratorAltar, StorageManagerMaidConfigGui, ClientGuiRegistry |

**总计恢复: 27 个文件**

---

*文档更新于 2026-06-01*
