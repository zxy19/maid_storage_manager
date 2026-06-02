# Deleted Functionality (Core Project)

> 记录核心项目迁移过程中被删除/移除/注释掉的功能。
> 兼容模组相关删除/禁用见 [05-integration-disabled.md](./05-integration-disabled.md)。
> 活跃 Mixin: 3 个 | Access Transformer 条目: 19 个

---

## 核心功能移除

### 女仆任务系统

#### CONFIGURABLE_COMMUNICATE_MARK Bauble
- **位置**: `MaidExtension.java:45`
- **状态**: 注释掉 `manager.bind(ItemRegistry.CONFIGURABLE_COMMUNICATE_MARK.get(), ...)`
- **影响**: 可配置通信标记饰品的女仆饰品绑定被禁用

> 注: `StorageManageTask` 及 3 个子行为类、`StorageManagerMaidConfigGui` 均已恢复。

---

### 合成系统 (CraftManager)

#### 基础 CraftType 和 Action — 已全部恢复
- 8 个基础 Type (CommonType, CraftingType, AltarType, FurnaceType, BrewingType, SmithingType, AnvilType, StoneCuttingType) 已恢复
- 6 个 Action (AltarRecipeAction, VirtualAction×2, SmithingRecipeAction, AnvilRecipeAction, StoneCuttingRecipeAction) 已恢复
- `CraftManager.java:75-82` 和 `:165-224` 的注册均已解除注释

#### 兼容模组 Craft 注册
- **状态**: 见 `05-integration-disabled.md` §3
- AE2 已恢复，其余 (RS, TACZ, Create, Mekanism, Botania, Ars Nouveau) 等待模组更新

---

### 存储系统

#### ItemHandler 存储 — 已恢复
- 5 个 `storage/ItemHandler/*.java` 文件已全部恢复

#### 兼容模组存储
- **状态**: 见 `05-integration-disabled.md` (RS 5, QIO 5, Create 6, AE2 5 已恢复)

---

## 渲染系统

### ItemStackLighting — 已删除
- **文件**: `render/ItemStackLighting.java`（已从项目中完全移除）

### BoxRenderUtil — 已恢复
- **文件**: `util/BoxRenderUtil.java`
- **状态**: RECOVERED — 适配 SubmitNodeCollector 渲染管线

### VirtualDisplayEntityRender — FIXME-02 已修复
- **文件**: `entity/VirtualDisplayEntityRender.java`
- **状态**: RESOLVED — submit() 完整实现 CORNER/LARGE/ICON 渲染

### VirtualItemEntityRender — 已恢复
- **文件**: `entity/VirtualItemEntityRender.java`
- **状态**: RECOVERED — 适配 MC 26.1 EntityRenderer 管线

### ModelBaked — 旧 BEWLR 逻辑移除
- **位置**: `event/ModelBaked.java`
- **旧功能**: 通过 `BEWLRProperties` 注册 BlockEntityWithoutLevelRenderer 实现动态物品渲染
- **当前状态**: 仅使用 `SimpleUnbakedStandaloneModel` 注册 3 个基础材质

---

## 事件系统

### 5 个客户端事件文件 — 已全部恢复

| 文件 | 状态 | 适配内容 |
|------|------|----------|
| `BindingRender.java` | RECOVERED | SubmitCustomGeometryEvent + SubmitNodeCollector |
| `BindingRenderSyncSender.java` | RECOVERED | PlayerTickEvent.Post + PacketDistributor |
| `InputEvent.java` | RECOVERED | KeyMapping 新构造器 (name, Type, keyCode, category) |
| `PlayerInteractClient.java` | RECOVERED | PlayerInteractEvent.EntityInteract |
| `TickClient.java` | RECOVERED | ClientTickEvent.Post |

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
| `InputEvent.java` (已恢复) | 输入事件 |
| `TickClient.java` (已恢复) | 客户端 tick |
| `PlayerInteractClient.java` (已恢复) | 客户端交互 |
| `BindingRender.java` (已恢复) | 绑定渲染 |
| `BindingRenderSyncSender.java` (已恢复) | 渲染同步 |
| `ClientGuiRegistry.java` (已恢复) | 客户端 GUI 注册 |
| `BoxRenderUtil.java` (已恢复) | 渲染工具 |

### @Mod("ipn"/"mouse_tweaks") — 14 处移除

IPN 和 MouseTweaks 在 MC 26.1 生态中不再以 `@Mod` 注解条件注入方式工作：
- IPN 相关: `@Mod("inventoryprofilesnext")` ~8 处移除 (物品选择器快捷键屏蔽)
- MouseTweaks 相关: `@Mod("mouse_tweaks")` ~6 处移除 (右键行为)

---

## 网络数据包

### 被注释的网络包注册 (5 个)

| 数据包 | 位置 | 状态 |
|--------|------|------|
| `ItemSelectorSetItemPacket` | Network.java:96-114 | 注册注释掉 |
| `CraftGuideGuiPacket` | Network.java:200-213 | 双向注册注释掉 |
| `CommunicateMarkGuiPacket` | Network.java:308-317 | 双向注册注释掉 |
| `CraftGuideGeneratorUpdate` | Network.java:318-327 | 双向注册注释掉 |

> 注: `AIMatchLocalizedItemS2CPacket` 已恢复。

### Handler 内容被注释但注册保留的包 (3 个)

| 数据包 | 位置 | 被注释内容 |
|--------|------|-----------|
| `MaidDataSyncToClientPacket` | Network.java:265-266 | bauble `deserializeNBT()` 调用 |
| `CreateStockManagerPacket` | Network.java:277-283 | `StockManagerInteract.handle()` (级联禁用) |
| `JEIRequestPacket` + `JEIRequestResultPacket` | :237-248 | 见 integration 文档 |

---

## GUI/菜单

### ClientGuiRegistry — 已恢复
- `registry/ClientGuiRegistry.java` 已重新创建并激活
- 所有 Screen → MenuType 绑定已恢复（除 TaczCraftScreen）

### LogisticsGuideMenu 界面引用
- **位置**: `Network.java:88-92`
- **内容**: `ItemSelectorGuiPacket` handler 中的 `LogisticsGuideMenu` 分支被注释
- **原因**: 级联禁用

---

## 资源文件

- JSON 模型: 17 个已删除（使用 `SimpleUnbakedStandaloneModel` 替代）
- JSON 标签: 10 个已删除（新标签通过 datagen 生成）

---

## Mixin 配置

当前 `mixins.json` 活跃 Mixin (3 个):
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

禁用的核心 Mixin (2 个，等待 TLM API 适配):
- `AltarRecipeMultiOutputMixin`
- `LivingEntityBrainSerializeWrapper`

兼容模组 Mixin 见 `05-integration-disabled.md`。

---

## Access Transformer

当前 `accesstransformer.cfg` 保留 19 个条目（无变化）。

---

## 杂项

### TourGuideTrigger 调用
- **位置**: `items/RequestListItem.java:111`
- **内容**: `//TODO bind Trigger`

### getMaidDebugTargets 条件化
- **位置**: `MaidExtension.java:106-113`
- **变更**: 添加 `if (!Config.aiFunctions) return List.of();` 守卫

### BaubleItemHandler.deserializeNBT
- **位置**: `Network.java:265-266`
- **内容**: `maid.getMaidBauble().deserializeNBT(sender.registryAccess(), msg.value)` 被注释
- **原因**: TLM 26.1 BaubleItemHandler.deserializeNBT 签名变化

---

## 恢复记录

| 日期 | 类别 | 数量 | 文件 |
|------|------|------|------|
| Phase 1 | Craft Types | 8 | CommonType, CraftingType, AltarType, FurnaceType, BrewingType, SmithingType, AnvilType, StoneCuttingType |
| Phase 1 | Maid 任务/行为 | 4 | StorageManageTask, LogisticsOutputBehavior, LogisticsRecycleBehavior, WriteInventoryListBehavior |
| Phase 1 | 存储/配置/GUI | 8 | ItemHandler×5, StorageManagerMaidConfigGui, ClientGuiRegistry, GeneratorAltar |
| Phase 2 | 客户端事件 | 5 | BindingRender, BindingRenderSyncSender, InputEvent, PlayerInteractClient, TickClient |
| Phase 2 | 渲染/实体 | 3 | BoxRenderUtil, VirtualItemEntityRender, VirtualDisplayEntityRender (FIXME-02) |
| Phase 2 | 网络包 | 1 | AIMatchLocalizedItemS2CPacket |

**核心项目累计恢复: 29 个文件**（含 JEI 核心 7 和 AE2 10 则为 46，integration 19 个见对应文档）

---

*文档更新于 2026-06-02*
