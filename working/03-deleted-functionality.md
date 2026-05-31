# Deleted Functionality

> 记录迁移过程中被删除/移除/注释掉的功能、代码片段、资源文件。
> 总计: 161+ 文件被禁用 (.java.disabled), 6 个 mixin 从配置移除, 40+ 处代码注释, 28 个 JSON 模型/标签删除, 10+ 个 access transformer 条目删除。

---

## 核心功能移除

### 女仆任务系统

#### StorageManageTask (核心任务)
- **位置**: `maid/task/StorageManageTask.java.disabled` + `MaidExtension.java:36`
- **状态**: 注释掉 `manager.add(new StorageManageTask());`
- **影响**: 整个女仆存储管理任务树不可用。作为核心任务类，其禁用导致了 3 个子行为类的级联禁用。
- **被级联禁用的子行为**:
  - `maid/behavior/logistics/output/LogisticsOutputBehavior.java.disabled` — 物流输出行为
  - `maid/behavior/logistics/recycle/LogisticsRecycleBehavior.java.disabled` — 物流回收行为
  - `maid/behavior/view/WriteInventoryListBehavior.java.disabled` — 库存列表写入行为
- **受影响的下游引用**: 
  - `MaidExtension.java:92,97` — 两处 maid tips 中的 `maid.getTask().getUid().equals(StorageManageTask.TASK_ID)` 检查被注释

#### CONFIGURABLE_COMMUNICATE_MARK Bauble
- **位置**: `MaidExtension.java:45`
- **状态**: 注释掉 `manager.bind(ItemRegistry.CONFIGURABLE_COMMUNICATE_MARK.get(), ...)`
- **影响**: 可配置通信标记饰品的女仆饰品绑定被禁用

---

### 合成系统 (CraftManager)

#### 8 个 CraftType 注册被注释
- **位置**: `CraftManager.java:75-82`
- **被移除的类型**:
  | 类型 | 类 | 说明 |
  |------|-----|------|
  | `CommonType` | `craft/type/CommonType.java.disabled` | 通用合成 |
  | `CraftingType` | `craft/type/CraftingType.java.disabled` | 工作台合成 |
  | `AltarType` | `craft/type/AltarType.java.disabled` | TLM 祭坛合成 |
  | `FurnaceType` | `craft/type/FurnaceType.java.disabled` | 熔炉 |
  | `BrewingType` | `craft/type/BrewingType.java.disabled` | 酿造台 |
  | `SmithingType` | `craft/type/SmithingType.java.disabled` | 锻造台 |
  | `AnvilType` | `craft/type/AnvilType.java.disabled` | 铁砧 |
  | `StoneCuttingType` | `craft/type/StoneCuttingType.java.disabled` | 切石机 |
- **原因**: 这些 type 类引用了 AltarRecipeBuilder 等 TLM 26.1 中 API 已变化的类，同时 CraftManager 类型注册链需要它们全部可用。
- **影响**: 没有类型注册，配方隔离系统（每种合成台独立筛选配方）失效，回退到通用模式。

#### 6 个 CraftAction 注册被注释
- **位置**: `CraftManager.java:165-224`
- **被移除的 Action**:
  | Action | 目标类型 | 执行器 | 影响 |
  |--------|----------|--------|------|
  | `AltarRecipeAction` | AltarType | AltarRecipeAction | TLM 祭坛自定义合成 |
  | `VirtualAction` | FurnaceType | — | 熔炉需要等待结果 |
  | `VirtualAction` | BrewingType | — | 酿造需要等待结果 |
  | `SmithingRecipeAction` | SmithingType | SmithingRecipeAction | 锻造台自定义槽位放置 |
  | `AnvilRecipeAction` | AnvilType | AnvilRecipeAction | 铁砧需要"取出产物" |
  | `StoneCuttingRecipeAction` | StoneCuttingType | StoneCuttingRecipeAction | 切石机选择配方 |
- **原因**: 这些 Action 依赖被禁用的 type 类或引用了已禁用的 integration 代码。

#### 1 个 AutoCraftGuideGenerator 被注释
- **位置**: `CraftManager.java:230`
- `GeneratorAltar` (TLM 祭坛配方生成器) — 依赖 TLM AltarRecipe API 变化

#### 兼容模组合成生成器全注释
- **位置**: `CraftManager.java:235-300`
- AE2: 3 个 generator (Inscriber, Charger, ItemTransform) + 1 type + 1 action
- RS: 1 type + 1 action
- Create: 8 个 generator (Press, Compact, Mix, Milling, Crushing, FanRecipes, Use, Deployer)
- Mekanism: 7 个 generator (Enrichment, Infusion, Crushing, Sawing, OsmiumComp, Combine, Smelter)
- Botania: 6 个 generator (RunicAltar, Apothecary, ManaInfuse, MythicalFlower, Elven, Daisy)
- Ars Nouveau: 3 个 generator (Imbuement, Apparatus, Enchanting)
- **原因**: 这些依赖模组均未发布 MC 26.1 兼容版本

---

### 存储系统

#### 10 个存储 Provider 被禁用
- **ItemHandler 5 个**: `storage/ItemHandler/AbstractItemHandlerContext.java`, `ContextItemHandlerCollect.java`, `ContextItemHandlerStore.java`, `ContextItemHandlerView.java`, `SimulateTargetInteractHelper.java`
  - **原因**: IItemHandler → ResourceHandler 迁移 + ContainerOpenersCounter API 变化
- **AE2 5 个** (等待 AE2 更新)
- **RS 5 个** (等待 RS 更新)
- **QIO 5 个** (等待 Mekanism 更新)
- **Create 5 个** (等待 Create 更新)

---

## 集成系统

### JEI 集成 (10 个文件)
- **核心文件** (全部 `.disabled`): `Plugin.java`, `GhostIngredientHandler.java`, `IFilterScreen.java`, `RequestRecipeHandler.java`
- **Mixin** (全部从 `mixins.json` 移除但文件保留 `.disabled`): `JeiGuiIconToggleButtonAccessor.java`, `JEIRecipeTransferHook.java`
- **配方处理器**: `JEIRecipeHandler.java`, `JEICommonRecipeHandler.java`, `JeiStoneCutterRecipeHandler.java`
- **请求系统**: `JEIClient.java`, `JEIRequestDisplayError.java`
- **原因**: JEI 暂无 MC 26.1 兼容版本

### EMI 集成 (8 个文件)
- **核心文件**: `Plugin.java`, `GhostIngredientHandler.java`, `RequestRecipeHandler.java`
- **Mixin** (从 `mixins.json` 移除但文件保留): `EMIRecipeTransferHook.java`
- **配方处理器**: `EmiRecipeHandler.java`, `EmiCommonRecipeHandler.java`, `EmiStoneCutterRecipeHandler.java`
- **请求系统**: `EMIClient.java`
- **原因**: EMI 暂无 MC 26.1 兼容版本

### Cloth Config 集成 (2 个文件)
- **文件**: `integration/cloth/AddClothEvent.java.disabled`, `integration/cloth/ClothEntry.java.disabled`
- **启动注册**: `StartUpEvent.java:28-32` — ClothEntry.registryConfigPage() + AddClothEvent 事件注册被注释
- **原因**: Cloth Config 包名/API 变化

### Create 交互 (StockManagerInteract)
- **位置**: `Network.java:40` (import 注释) + `Network.java:301` (调用注释)
- **包**: `integration/create/StockManagerInteract.java.disabled`
- **功能**: 处理女仆与 Create StockKeeper 的库存管理/购物列表交互
- **影响**: `CreateStockManagerPacket` 的 `OPEN_SCREEN` 和 `SHOP_LIST` 分支不可用

### Ingredient Request 系统
- **位置**: `Network.java:41` (import 注释) + `Network.java:258` (调用注释)
- **文件**: `integration/request/IngredientRequest.java.disabled`, `IngredientRequestClient.java.disabled`
- **功能**: JEI/EMI 到请求系统的桥梁
- **影响**: `JEIRequestPacket` 服务器端处理被注释掉，导致从 JEI/EMI 界面请求物品的功能丧失

### KubeJS 集成 (34 个文件)
- **全部禁用**: `integration/kubejs/` 下所有文件
- **原因**: KubeJS 暂无 MC 26.1 兼容版本
- **影响**: 脚本扩展、事件、包装器等全部不可用

### 其他兼容模组
- **The One Probe**: `integration/top/` 下 `InterModComms.sendTo("theoneprobe", ...)` 被注释
- **Jade**: 2 个文件被禁用，等待 API 适配
- **Sophisticated Storage**: 1 个文件被禁用
- **TACZ**: 8 个文件全部禁用

---

## 网络数据包

### 被注释/禁用的网络包 (8 个)

| 数据包 | 位置 | 状态 |
|--------|------|------|
| `ItemSelectorSetItemPacket` | Network.java:96-114 | 注册注释掉，send 方法空实现 |
| `CraftGuideGuiPacket` | Network.java:220-233 | 双向注册注释掉 |
| `CommunicateMarkGuiPacket` | Network.java:328-337 | 双向注册注释掉 |
| `CraftGuideGeneratorUpdate` | Network.java:338-347 | 双向注册注释掉 |
| `AIMatchLocalizedItemS2CPacket` | Network.java:357-364 | 客户端注册注释掉 |
| `JEIRequestPacket` | Network.java:252-261 | 注册保留但 handler 内容注释 |
| `JEIRequestResultPacket` | Network.java:262-271 | 注册保留但 handler 内容注释 (InScreenTipData 禁用) |
| `CreateStockManagerPacket` | Network.java:292-308 | 注册保留但 handler 内容注释 |

### 被注释的通信菜单处理
- **位置**: Network.java:90-92
- `ItemSelectorGuiPacket` handler 中 `LogisticsGuideMenu` 分支被注释掉
- **原因**: LogisticsGuideMenu 界面被禁用

---

## GUI/菜单注册删除

### ClientGuiRegistry — 完全移除
- **旧文件**: `registry/ClientGuiRegistry.java` (不存在于当前代码库)
- **描述**: 包含所有 Screen → MenuType 的客户端注册绑定
- **注册内容**: 曾包含 AltarCraftScreen, AnvilCraftScreen, BrewingCraftScreen, CommonCraftScreen, CraftingTableCraftScreen, FurnaceCraftScreen, SmithingCraftScreen, StoneCutterCraftScreen, CommunicateMarkScreen 等 screen 到对应 menu 的绑定
- **原因**: MC 26.1 中 Screen/Menu 注册体系变化，screen 绑定方式改变
- **影响**: 虽然 MenuType 注册 (GuiRegistry) 仍保留，但缺少 ClientGuiRegistry 的 screen 绑定意味着所有合成界面和通信标记界面无法在客户端打开

### StorageManagerMaidConfigGui — 禁用
- **文件**: `maid/config/StorageManagerMaidConfigGui.java.disabled`
- **描述**: 女仆配置 GUI 界面
- **原因**: MC 26.1 screen 渲染 API 从 `render()` 迁移到 `extractRenderState()` + `submit()` 模式

---

## 渲染系统删除

### RenderItemFrameEvent 旧逻辑 — 完全移除
- **旧文件**: `event/RenderItemFrameEvent.java` (不存在于当前代码库)
- **旧功能**: 使用 `RenderLevelStageEvent` 在 WORLD_AFTER_ENTITIES 阶段渲染虚拟物品展示框
- **原因**: MC 26.1 渲染管线使用 SubmitNodeCollector 替代 MultiBufferSource，旧事件注册方式不可用

### ItemStackLighting — 移除
- **文件**: `render/ItemStackLighting.java.disabled`
- **描述**: 物品光照渲染工具类
- **原因**: MC 26.1 渲染管线中光照计算方式变化

### BoxRenderUtil — 禁用
- **文件**: `util/BoxRenderUtil.java.disabled`
- **原因**: RenderLevelStageEvent API 变化

### VirtualDisplayEntityRender — 部分禁用
- **文件**: `entity/VirtualDisplayEntityRender.java` (编译通过但功能不完整)
- **禁用部分**: CORNER/LARGE/ICON 渲染模式仅抛注释，无实际渲染代码
- **原因**: EntityRenderer 从 `render(entity, yaw, partialTick, poseStack, bufferSource, light)` 迁移至 `submit(EntityRenderState, PoseStack, SubmitNodeCollector, CameraRenderState)`

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
| `ClientGuiRegistry.java` (已删除) | 客户端 GUI 注册 |
| `BoxRenderUtil.java` (已禁用) | 渲染工具 |
- **注意**: 仍有 10 处 `@OnlyIn(Dist.CLIENT)` 保留在活跃代码中（类: `InventoryListDataClient`, `CraftingChatBubbleData`, `JoinLevelEvent`, `VirtualDisplayEntityRender`, `WrittenInvListItem`, `SlotType.drawGold()`, `ScreenEvent` 等）

### @Mod("ipn"/"mouse_tweaks") — 14 处移除
由于 IPN (Inventory Profiles Next) 和 MouseTweaks 两个模组在 MC 26.1 生态中不再以 `@Mod` 注解条件注入的方式工作：
- **IPN 相关**: `@Mod("inventoryprofilesnext")` 条件注入注解从 ~8 个地方移除
- **MouseTweaks 相关**: `@Mod("mouse_tweaks")` 条件注入注解从 ~6 个地方移除
- **原因**: 这些注入主要是为了在物品选择器中屏蔽 IPN 的快捷键干扰和 MouseTweaks 的右键行为。替代方案待确定（如果这两个模组有 MC 26.1 版本则需要重新集成）

---

## 资源文件删除

### JSON 模型 (17 个)
从 `assets/maid_storage_manager/models/` 目录删除的旧 Item 模型 JSON 文件：
| 文件名 | 说明 |
|--------|------|
| `item/no_access.json` | 无权限图标 |
| `item/request_list.json` | 请求列表物品模型 |
| `item/storage_list.json` | 存储列表物品模型 |
| `item/craft_guide.json` | 合成指引物品模型 |
| `item/craft_guide_crafting_table.json` | 工作台合成指引模型 |
| `item/craft_guide_altar.json` | 祭坛合成指引模型 |
| `item/craft_guide_furnace.json` | 熔炉合成指引模型 |
| `item/craft_guide_brewing.json` | 酿造合成指引模型 |
| `item/craft_guide_smithing.json` | 锻造合成指引模型 |
| `item/craft_guide_anvil.json` | 铁砧合成指引模型 |
| `item/craft_guide_stone_cutter.json` | 切石机合成指引模型 |
| `item/filter_list.json` | 过滤列表物品模型 |
| `item/storage_define_bauble.json` | 存储定义饰品模型 |
| `item/logistics_guide.json` | 物流指引物品模型 |
| `item/portable_craft_calculator_bauble.json` | 便携合成计算器饰品模型 |
| `item/communicate_mark.json` | 通信标记物品模型 |
| `item/communicate_mark_config.json` | 通信标记配置物品模型 |
- **原因**: 迁移到 NeoForge 26.1 后, Item 模型改用 `SimpleUnbakedStandaloneModel` + `ModelBaked` 事件注册，不再需要 JSON 模型文件。仅保留 `models/item/` 空目录结构。

### JSON 标签 (10 个)
从 `data/maid_storage_manager/tags/` 删除的旧标签文件：

**block 标签 (8 个):**
| 文件 | 说明 |
|------|------|
| `tags/block/crafting_table.json` | 旧工作台标签 |
| `tags/block/furnace.json` | 旧熔炉标签 |
| `tags/block/smithing_table.json` | 旧锻造台标签 |
| `tags/block/anvil.json` | 旧铁砧标签 |
| `tags/block/brewing_stand.json` | 旧酿造台标签 |
| `tags/block/stone_cutter.json` | 旧切石机标签 |
| `tags/block/default_storage.json` | 旧默认存储标签 |
| `tags/block/create_package_container.json` | 旧 Create 打包容器标签 |

**item 标签 (1 个):**
| 文件 | 说明 |
|------|------|
| `tags/item/maid_interact.json` | 旧女仆交互物品标签 |

**类型标签 (1 个):**
| 文件 | 说明 |
|------|------|
| `tags/type/sorting_target.json` | 旧排序目标类型标签 |

- **当前状态**: 新标签文件位于 `src/generated/resources/data/maid_storage_manager/tags/`，使用 26.1 中所有生成 (block 标签带 `working_block/` 前缀)
- **注意**: 旧 JSON 资源文件未保留 `.disabled` 后缀，已直接从文件系统中删除

---

## Mixin 配置变更

### 从 mixins.json 移除的 6 个 Mixin

**Create 相关 (3 个):**
| Mixin | 状态 |
|-------|------|
| `CreateStockKeeperMenuMixin` | 文件保留为 `.disabled` |
| `CreateStockKeeperScreenMixin` | 文件保留为 `.disabled` |
| `CreateStockTickerBEMixin` | 文件保留为 `.disabled` |

**JEI/EMI 相关 (3 个):**
| Mixin | 状态 |
|-------|------|
| `JeiGuiIconToggleButtonAccessor` | 文件保留为 `.disabled` |
| `JEIRecipeTransferHook` | 文件保留为 `.disabled` |
| `EMIRecipeTransferHook` | 文件保留为 `.disabled` |

**当前 mixins.json 仅剩 4 个活跃 Mixin:**
```json
"mixins": [
    "ContainerOpenersCounterPatch",
    "ItemFrameTickMixin"
],
"client": [
    "EntityGraphicsBufferSourceGetter",
    "ItemFrameRendererMixin"
]
```

### 仍保留文件但不在 mixins.json 中的 Mixin (2 个)
这些 Mixin 类文件有 `.disabled` 后缀，等待 API 适配后重新加入 mixins.json：
| Mixin | 阻塞原因 |
|-------|----------|
| `AltarRecipeMultiOutputMixin.java.disabled` | ShapelessRecipe 构造器从 (String, Category, ItemStack, List) 变为 (CommonInfo, BookInfo, ItemStackTemplate, List) |
| `LivingEntityBrainSerializeWrapper.java.disabled` | Brain.serializeStart 需要 RegistryOps 参数 |

---

## Access Transformer 条目删除

当前 `accesstransformer.cfg` 保留 **19 个条目**，以下条目在迁移中被删除（总计 ~15 个条目）：

### 被删除的集成相关 AT 条目
| 条目 | 关联功能 | 删除原因 |
|------|----------|----------|
| `public net.minecraft.world.inventory.FurnaceMenu *` (若干字段) | FurnaceCraftScreen | 禁用 |
| `public net.minecraft.world.inventory.BrewingStandMenu *` (若干字段) | BrewingCraftScreen | 禁用 |
| `public net.minecraft.world.inventory.StonecutterMenu *` (若干字段) | StoneCutterCraftScreen | 禁用 |
| `public net.minecraft.world.inventory.SmithingMenu *` (若干字段) | SmithingCraftScreen | 禁用 |
| `public net.minecraft.world.inventory.AnvilMenu *` (额外字段) | AnvilCraftScreen | 禁用 |
| `public com.tom.storagemod.*` (若干) | Sophisticated Storage 集成 | 集成禁用 |

### 被删除的渲染相关 AT 条目
| 条目 | 关联功能 | 删除原因 |
|------|----------|----------|
| `public net.minecraft.client.renderer.entity.ItemRenderer *` (若干) | ItemStackLighting | 渲染变更 |
| RenderSystem 相关额外条目 | BoxRenderUtil | 渲染变更 |

### 被删除的事件/网络 AT 条目
| 条目 | 关联功能 | 删除原因 |
|------|----------|----------|
| `public net.neoforged.neoforge.event.level.LevelEvent *` | RenderItemFrameEvent | 事件移除 |
| 网络通道相关条目 | 旧网络包 | 网络 API 重构 |

### 保留的 19 个 AT 条目
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
- **位置**: `items/RequestListItem.java:106`
- **内容**: `// TourGuideTrigger.trigger(serverPlayer, "request_list_bind");`
- **原因**: TourGuide 依赖被移除 (`23e8140` commit)

### getMaidDebugTargets 条件化
- **位置**: `MaidExtension.java:106-113`
- **变更**: 添加 `if (!Config.aiFunctions) return List.of();` 守卫
- **原因**: 当 AI 功能被禁用时不注册调试目标

### LogisticsGuideMenu 界面引用
- **位置**: `Network.java:90-92`
- **内容**: `ItemSelectorGuiPacket` handler 中的 `LogisticsGuideMenu` 分支被注释
- **原因**: 级联禁用

### CraftGuideGuiPacket + ICraftGuiPacketReceiver
- **位置**: `Network.java:220-233`
- **内容**: 整个双向包注册被注释
- **原因**: 引用的 `ICraftGuiPacketReceiver` 接口被禁用，所有合成 screen 被禁用

### BaubleItemHandler.deserializeNBT
- **位置**: `Network.java:285-286` — `MaidDataSyncToClientPacket` handler 中 bauble 反序列化
- **内容**: `maid.getMaidBauble().deserializeNBT(sender.registryAccess(), msg.value)` 被注释
- **原因**: TLM 26.1 中 BaubleItemHandler.deserializeNBT 签名变化

### 兼容模组 InterModComms
- **位置**: `registry/CompatRegistry.java`
- **内容**: `InterModComms.sendTo("theoneprobe", ...)` 被注释
- **原因**: The One Probe MC 26.1 兼容缺失

---
*文档生成于 2026-06-01，基于 `master` 分支源码扫描*
