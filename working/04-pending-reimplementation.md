# Pending Reimplementation

> 需要重新实现的核心功能。按优先级（P0-P3）和功能模块组织。
> 总计: 161 个 `.java.disabled` 文件 + 多个被注释的功能块等待恢复。

---

## P0 — 阻塞性，必须最先恢复

### 1. StorageManageTask (核心任务类)
- **文件**: `maid/task/StorageManageTask.java.disabled`
- **状态注释**: `MaidExtension.java:36` — 任务注册被注释
- **描述**: 女仆存储管理核心任务类，创建 AI 行为树，定义女仆在存储/物流/合成模式下的完整行为序列。
- **被级联禁用的子行为**:
  - `LogisticsOutputBehavior.java.disabled` — 物流输出行为（女仆向容器输出物品）
  - `LogisticsRecycleBehavior.java.disabled` — 物流回收行为（女仆从容器回收物品）
  - `WriteInventoryListBehavior.java.disabled` — 库存列表写入行为（更新存储列表）
- **阻塞原因**: TLM 26.1 中 Task 基类和 AI 行为树构造 API 全面重构
- **修复方案**: 
  1. 阅读 TLM 26.1 新的 Task 基类 API 和 `@LittleMaidExtension` 任务注册方式
  2. 重写 AI 行为树构建逻辑（旧用的是 Brain Schedule 系统，新的可能改为 Behavior Tree 或直接 Brain Activity）
  3. 依次恢复 3 个被级联禁用的行为类
  4. 恢复 `MaidExtension.java` 中 2 处 tips 条件检查
- **预计工作量**: 大 (5-8h)

### 2. StorageManagerMaidConfigGui (女仆配置 GUI)
- **文件**: `maid/config/StorageManagerMaidConfigGui.java.disabled`
- **描述**: 女仆配置 GUI 界面，允许玩家配置女仆的存储管理参数（内存助手模式、无排序放置、最大并行数等）
- **阻塞原因**: MC 26.1 Screen 渲染 API 从 `render(GuiGraphics, ...)` 迁移到 `extractRenderState()` + `submit(RenderState, PoseStack, SubmitNodeCollector, CameraRenderState)`
- **修复方案**:
  1. 参考 `migration/15-vanilla-screen-menu.md` 的 Screen 重写指南
  2. 参考已完成迁移的 screen 类（如 `FilterScreen.java`, `ItemSelectorScreen.java`）
  3. 重写所有渲染方法为 extractRenderState + submit 模式
  4. 适配 GuiGraphics → GuiGraphicsExtractor 变更
- **预计工作量**: 中 (3-5h)

### 3. ClientGuiRegistry (客户端 GUI 注册)
- **旧文件**: `registry/ClientGuiRegistry.java` (已删除)
- **描述**: 所有 Screen → MenuType 的客户端注册绑定。虽然 MenuType 注册（GuiRegistry.java:24-55）仍保留，但缺少此文件意味着所有合成界面和通信标记界面无法在客户端打开。
- **阻塞原因**: 此文件的实现方式在 MC 26.1 中需要使用新的 screen 注册 API
- **受影响需恢复的 Screen 类**:
  - `AltarCraftScreen.java` — 祭坛合成界面
  - `AnvilCraftScreen.java` — 铁砧合成界面
  - `BrewingCraftScreen.java` — 酿造合成界面
  - `CommonCraftScreen.java` — 通用合成界面
  - `CraftingTableCraftScreen.java` — 工作台合成界面
  - `FurnaceCraftScreen.java` — 熔炉合成界面
  - `SmithingCraftScreen.java` — 锻造合成界面
  - `StoneCutterCraftScreen.java` — 切石机合成界面
  - `CommunicateMarkScreen.java` — 通信标记界面
- **修复方案**:
  1. 研究 MC 26.1 中 `MenuScreens.register(MenuType, Screen.Constructor)` 的新位置
  2. 创建 `ClientGuiRegistry` 类，逐个注册 screen-menu 绑定
  3. 每个 screen 类本身已完成迁移（引用已验证），直接恢复注册即可
- **预计工作量**: 小 (1-2h)

### 4. Craft Types Recovery (合成类型恢复)
- **文件**: 8 个 `craft/type/*Type.java.disabled` + `CraftManager.java:75-82`
- **描述**: 合成类型注册和配置。这 8 个 type 类本身是轻量级的 Identifier 引用，主要用于 `isSpecialType(level, pos, direction)` 查询。需要恢复以启用配方类型隔离系统。
- **涉及的 type 类**:
  - `craft/type/CommonType.java.disabled` — `Identifier("common")`
  - `craft/type/CraftingType.java.disabled` — `Identifier("crafting")`
  - `craft/type/AltarType.java.disabled` — `Identifier("altar")`
  - `craft/type/FurnaceType.java.disabled` — `Identifier("furnace")`
  - `craft/type/BrewingType.java.disabled` — `Identifier("brewing")`
  - `craft/type/SmithingType.java.disabled` — `Identifier("smithing")`
  - `craft/type/AnvilType.java.disabled` — `Identifier("anvil")`
  - `craft/type/StoneCuttingType.java.disabled` — `Identifier("stone_cutting")`
- **阻塞原因**: `AltarType` 引用了 `AltarRecipeBuilder` 类（TLM 26.1 中 API 已变化）
- **修复方案**:
  1. 逐个检查每个 type 类的 `isSpecialType()` 实现，确认引用的 API 是否可用
  2. 恢复无外部依赖的 type 类（CraftingType, CommonType）
  3. 对于 AltarType，需适配 TLM 26.1 新的 `AltarRecipe` API
  4. 解除 `CraftManager.java:75-82` 中 8 行 `addCraftType` 注释
  5. 依次解除 6 个 `addAction` 注释（部分 Action 引用了禁用的 interface）
- **预计工作量**: 中 (3-5h)

---

## P1 — 重要功能，需要 API 适配

### 5. Item Storage System (IItemHandler 适配)
- **文件**: 5 个 `storage/ItemHandler/*.java.disabled`
  - `AbstractItemHandlerContext.java` — 物品处理器上下文基类
  - `ContextItemHandlerCollect.java` — 收集上下文
  - `ContextItemHandlerStore.java` — 存储上下文
  - `ContextItemHandlerView.java` — 查看上下文
  - `SimulateTargetInteractHelper.java` — 模拟目标交互
- **描述**: 基于旧 IItemHandler (Forge 风格) 的存储提供者层。IItemHandler → ResourceHandler<ItemResource> 完整迁移。
- **阻塞原因**: 
  - MC 26.1 中 `IItemHandler` 逐步废弃，被 `ResourceHandler` 替代
  - `ContainerOpenersCounter` API 变化
  - 需要 Transaction 模式的 extract/insert 替代 setStackInSlot
- **修复方案**:
  1. 将 `AbstractItemHandlerContext` 中所有 `IItemHandler` 引用改为 `ResourceHandler<ItemResource>`
  2. 使用 `Transaction` 模式替代直接写操作（参考 `SlotType.java:143-155` 的 `replaceSlot` 方法）
  3. 适配 `ContainerOpenersCounter`（当前已有 `ContainerOpenersCounterPatch` mixin 为其提供桥接）
  4. 参考 `storage/MaidStorage.java` 中已适配的 IItemHandler → ResourceHandler 迁代码
- **预计工作量**: 大 (6-10h)
- **依赖**: FIXME-01 (CombinedResourceHandler 写操作) 的部分知识

### 6. CraftManager 中的 Action 恢复
- **文件**: `CraftManager.java:165-224` — 6 个被注释的 addAction 调用
- **描述**: 恢复特定合成类型对应的 CraftAction 注册（AltarRecipeAction, VirtualAction, SmithingRecipeAction, AnvilRecipeAction, StoneCuttingRecipeAction）
- **阻塞原因**: 引用了被禁用的 type 类或被禁用的 interface
- **修复方案**:
  1. 先恢复 P0 的 Craft Types（依赖关系）
  2. 逐个检查每个 Action 类的依赖（AltarRecipeAction 依赖 TLM AltarRecipe API）
  3. 解除 `CraftManager.java:165-224` 中注释
  4. 解除 `CraftManager.java:230` 中 `GeneratorAltar` 注释
- **预计工作量**: 中 (2-4h)
- **依赖**: P0 第 4 项 (Craft Types Recovery)

### 7. CraftManager 兼容模组生成器恢复
- **文件**: `CraftManager.java:235-300` — AE2, RS, Create, Mekanism, Botania, Ars Nouveau 的生成器和类型注册
- **阻塞原因**: 所有兼容模组均未发布 MC 26.1 版本
- **修复方案**: 等待对应模组发布 MC 26.1 版本后逐个恢复：
  1. 恢复模组的集成类（`integration/` 下的 `.disabled` 文件）
  2. 解除 `CraftManager.java` 中对应注册块的注释
  3. 适配该模组的新 API
- **预计工作量**: 分散，每个模组 2-6h

---

## P2 — 中度优先级，渲染/事件适配

### 8. Entity Render 适配
- **文件**: 
  - `entity/VirtualDisplayEntityRender.java` — 虚拟展示框渲染（当前仅 FRAME 模式工作）
  - `entity/VirtualItemEntityRender.java.disabled` — 虚拟物品实体渲染
  - `event/BindingRender.java.disabled` — 绑定渲染
- **描述**: MC 26.1 SubmitNodeCollector 渲染管线适配。旧代码使用 `MultiBufferSource` + `render()` 模式。
- **阻塞原因**: 
  - EntityRenderer 从 `render(entity, yaw, partialTick, poseStack, bufferSource, light)` 迁移至 `submit(EntityRenderState, PoseStack, SubmitNodeCollector, CameraRenderState)`
  - 新增 `extractRenderState()` 方法
- **修复方案**:
  1. 研究 MC 26.1 中 `SubmitNodeCollector` 如何提交自定义 item 渲染
  2. 参考 `ItemFrameRenderer` 的 submit 实现
  3. 在 `VirtualDisplayEntityRender.submit()` 中实现 CORNER/LARGE/ICON 模式
  4. 恢复 `VirtualItemEntityRender` 和 `BindingRender`
- **预计工作量**: 大 (6-8h)
- **关联 FIXME**: FIXME-02 (渲染管线重构)

### 9. Network Packets 恢复
- **文件**: `Network.java` 中 5 个被注释的包注册
- **需要恢复的包**:
  | 包名 | 类型 | 位置 |
  |------|------|------|
  | `ItemSelectorSetItemPacket` | C2S | Network.java:96-114 |
  | `CraftGuideGuiPacket` | 双向 | Network.java:220-233 |
  | `CommunicateMarkGuiPacket` | 双向 | Network.java:328-337 |
  | `CraftGuideGeneratorUpdate` | 双向 | Network.java:338-347 |
  | `AIMatchLocalizedItemS2CPacket` | S2C | Network.java:357-364 |
- **修复方案**:
  1. 对于 `ItemSelectorSetItemPacket` — 检查 `ItemSelectorMenu.filteredItems.setItem` API 是否可用，恢复注册
  2. 对于 `CraftGuideGuiPacket` — 恢复 `ICraftGuiPacketReceiver` 接口，验证所有实现类
  3. 对于 `AIMatchLocalizedItemS2CPacket` — 恢复 AI 对话物品匹配同步，适配新 StreamCodec API
  4. 对于 `CommunicateMarkGuiPacket` — 验证 `CommunicateMarkGuiPacket.handle()` 实现
- **预计工作量**: 中 (3-5h)

### 10. Box Render 恢复
- **文件**: `util/BoxRenderUtil.java.disabled`
- **描述**: 用于调试和指示的盒子/立方体渲染工具
- **阻塞原因**: `RenderLevelStageEvent` API 变化（事件类型和注册方式改变）
- **修复方案**:
  1. 研究 MC 26.1 中 `RenderLevelStageEvent` 的新事件类型
  2. 适配原有的 `begin`, `vertex`, `endVertex` 等渲染调用为 SubmitNodeCollector 模式
  3. 或使用 MC 26.1 新增的 `LevelRenderer.renderDebug()` 钩子
- **预计工作量**: 小 (2-3h)

### 11. Client Events 恢复
- **文件** (全部 `.disabled`):
  - `event/InputEvent.java` — 客户端输入事件（滚轮切换 CraftGuide/LogisticsGuide 模式）
  - `event/TickClient.java` — 客户端 tick（调度渲染同步）
  - `event/PlayerInteractClient.java` — 客户端交互（工作卡绑定等）
  - `event/BindingRenderSyncSender.java` — 渲染同步发送器
- **阻塞原因**: 
  - `KeyMapping` 构造器从 `KeyMapping(name, keyCode, category)` 变为 `KeyMapping(name, Type, keyCode, category)`
  - `TickEvent.ClientTickEvent` 事件 API 变化
  - `PlayerInteractEvent` 事件 API 变化
- **修复方案**:
  1. 参考 `migration/15-vanilla-screen-menu.md` 中的输入/事件适配章节
  2. 参考 MC 26.1 中 `KeyMapping` 的新构造器签名
  3. 逐个恢复事件类并适配新 API
- **预计工作量**: 中 (3-5h)

### 12. Mixin 恢复
- **文件**:
  - `mixin/AltarRecipeMultiOutputMixin.java.disabled` — 扩展 TLM AltarRecipe 多输出支持
  - `mixin/LivingEntityBrainSerializeWrapper.java.disabled` — 女仆大脑序列化包装
- **需适配 API**:
  | Mixin | 旧 API | 新 API |
  |-------|--------|--------|
  | AltarRecipeMultiOutputMixin | `ShapelessRecipe(String, Category, ItemStack, List<ItemStack>)` | `ShapelessRecipe(CommonInfo, BookInfo, ItemStackTemplate, List<ItemStack>)` |
  | LivingEntityBrainSerializeWrapper | `Brain.serializeStart()` | `Brain.serializeStart(RegistryOps)` |
- **修复方案**:
  1. 使用 `idea_get_symbol_info` 查看 MC 26.1 中 `ShapelessRecipe` 和 `Brain` 的完整签名
  2. 更新 Mixin 目标方法签名
  3. 恢复文件后缀并加入 `mixins.json`
- **预计工作量**: 小 (1-2h)

### 13. 被注释的 Network Handler 恢复
- **位置**: `Network.java:82-94` — `ItemSelectorGuiPacket` handler 中的 `LogisticsGuideMenu` 分支
- **位置**: `Network.java:257-259` — `JEIRequestPacket` handler 中的 `IngredientRequest.onRequest()`
- **位置**: `Network.java:264-269` — `JEIRequestResultPacket` handler 中的 `InScreenTipData.show()`
- **位置**: `Network.java:285-286` — `MaidDataSyncToClientPacket` handler 中的 bauble 反序列化
- **位置**: `Network.java:297-306` — `CreateStockManagerPacket` handler 中的 StockManagerInteract
- **修复方案**:
  1. 依次恢复被依赖的类（LogisticsGuideMenu, IngredientRequest, InScreenTipData, StockManagerInteract）
  2. 验证 BaubleItemHandler.deserializeNBT 的新签名
  3. 解除各 handler 中的注释
- **预计工作量**: 分散，每个 0.5-2h
- **依赖**: 多个 P3 项（兼容模组集成）

---

## P3 — 低优先级，依赖外部模组更新

### 14. 兼容模组集成恢复

#### AE2 (9 文件)
- **等待**: AE2 MC 26.1 版本发布
- **文件**: 5 个 storage + 3 个 craft generator + 1 个 craft type
- **额外**: 解除 `CraftManager.java:235-247` 中 AE2 注册

#### RS (7 文件)
- **等待**: Refined Storage MC 26.1 版本发布
- **文件**: 5 个 storage + 1 个 craft action + 1 个 craft type
- **额外**: 解除 `CraftManager.java:248-260` 中 RS 注册

#### Create (23 文件)
- **等待**: Create MC 26.1 版本发布
- **文件**: 8 个 generator + 3 个 mixin + 5 个 storage + 4 个 integration + 3 个其他
- **额外**: 恢复 `CreateStockManagerPacket` handler、恢复 `StartUpEvent.java:28` CreateIntegration.init()
- **额外**: 解除 `CraftManager.java:264-273` 中 Create 注册

#### Mekanism (9 文件)
- **等待**: Mekanism MC 26.1 版本发布
- **文件**: 7 个 generator + 1 个 integration + 5 个 QIO storage
- **额外**: 解除 `CraftManager.java:274-282` 中 Mekanism 注册

#### JEI (10 文件)
- **等待**: JEI MC 26.1 版本发布
- **文件**: 4 个 integration + 2 个 mixin + 2 个 request + 3 个 menu handler
- **额外**: 恢复 `JeiGuiIconToggleButtonAccessor` 和 `JEIRecipeTransferHook` 到 mixins.json

#### EMI (8 文件)
- **等待**: EMI MC 26.1 版本发布
- **文件**: 3 个 integration + 1 个 mixin + 1 个 request + 3 个 menu handler
- **额外**: 恢复 `EMIRecipeTransferHook` 到 mixins.json

#### KubeJS (34 文件)
- **等待**: KubeJS MC 26.1 版本发布
- **修复方案**: 可能需要大幅重写（KJS API 版本跨越较大）
- **额外**: 解除 `CraftManager.java:55-56` 中 KJS 事件桥接

#### Ars Nouveau (4 文件)
- **等待**: Ars Nouveau MC 26.1 版本发布
- **额外**: 解除 `CraftManager.java:296-300` 中 Ars Nouveau 注册

#### Botania (6 个 generator)
- **等待**: Botania MC 26.1 版本发布
- **额外**: 解除 `CraftManager.java:288-295` 中 Botania 注册

#### TACZ (8 文件)
- **等待**: TACZ MC 26.1 版本发布
- **额外**: 解除 `CraftManager.java:261-263` 中 TACZ 注册

#### Sophisticated Storage (1 文件)
- **等待**: Sophisticated Storage MC 26.1 版本发布
- **额外**: 适配新的 MultiBlock API

#### Cloth Config (2 文件)
- **等待**: Cloth Config MC 26.1 版本发布
- **修复方案**: 适配新的包名和 ConfigBuilder API
- **额外**: 恢复 `StartUpEvent.java:29-32` 中 Cloth 事件注册

#### Jade (2 文件)
- **等待**: Jade MC 26.1 版本发布
- **修复方案**: 适配 Jade 新 API

#### The One Probe (注释内容)
- **位置**: `registry/CompatRegistry.java` — `InterModComms.sendTo("theoneprobe", ...)` 被注释
- **等待**: TOP MC 26.1 版本发布

### 15. Ingredient Request 系统 (2 文件)
- **文件**: `integration/request/IngredientRequest.java.disabled`, `IngredientRequestClient.java.disabled`
- **描述**: JEI/EMI 到请求系统的桥梁，支持从配方查看界面直接请求合成物品
- **等待**: JEI 或 EMI 发布 MC 26.1 版本
- **修复方案**: 适配 JEI/EMI 新 API 的 recipe transfer handler

### 16. Datagen 恢复 (2 文件)
- **文件**: 
  - `datagen/RecipeDataGen.java.disabled` — TLM 特殊配方格式的 datagen
  - `datagen/AdvancementDataGen.java.disabled` — 进度 datagen
- **阻塞原因**:
  - RecipeDataGen: TLM 的 `AltarRecipeBuilder` 不兼容 data component 配方格式
  - AdvancementDataGen: TLM `MaidEvent` trigger 类在 datagen 环境不可用
- **修复方案**:
  1. 等待 TLM 26.1 提供完整的 Builder API
  2. 或回退到使用原始 JSON 生成（更可靠但更冗长）
- **预计工作量**: 中 (3-5h)

### 17. HumanoidModelMixin 修复
- **文件**: `mixin/client/HumanoidModelMixin.java` (被 `/ FIXME` 注释)
- **描述**: 玩家骑乘女仆时的手臂角度设置
- **阻塞原因**: 具体问题待调查
- **预计工作量**: 小 (1h)

---

## 恢复顺序建议

```
Phase 1 (P0 — 基础骨架):
  ├── 1. Craft Types Recovery (解除 8 个 type 类)
  ├── 2. ClientGuiRegistry (screen 注册)
  ├── 3. CraftManager Action 恢复 (6 个 addAction)
  ├── 4. StorageManageTask (核心任务)
  └── 5. StorageManagerMaidConfigGui (配置 GUI)

Phase 2 (P1 — API 适配):
  ├── 6. Item Storage System (IItemHandler → ResourceHandler)
  ├── 7. Network Packets 恢复 (5 个包)
  └── 8. CraftManager 兼容模组 (依赖外部模组更新)

Phase 3 (P2 — 渲染/事件):
  ├── 9.  Entity Render 适配 (SubmitNodeCollector)
  ├── 10. Box Render 恢复
  ├── 11. Client Events 恢复
  ├── 12. Mixin 恢复 (2 个)
  └── 13. Network Handler 恢复

Phase 4 (P3 — 等待外部):
  ├── 14. 兼容模组集成 (逐个恢复，依赖各自更新)
  ├── 15. Ingredient Request 系统
  ├── 16. Datagen 恢复
  └── 17. HumanoidModelMixin 修复
```

---

## 每个恢复项的验证清单

恢复任意功能时，建议验证：

1. **编译**: `gradlew jar` 无错误
2. **启动**: 客户端/服务器启动无崩溃
3. **功能**: 对应功能在游戏中可操作
4. **级联**: 检查是否解锁了其他被禁用的文件
5. **反注册**: 恢复的功能是否需要在退出时清理

---

## 参考文档

| 文档 | 用途 |
|------|------|
| `working/01-disabled-files.md` | 完整禁用文件清单 |
| `working/02-todos-and-fixmes.md` | 当前活跃代码中的 TODO/FIXME |
| `working/03-deleted-functionality.md` | 已删除功能详情 |
| `migration/15-vanilla-screen-menu.md` | Screen/Menu 迁移指南 |
| `migration/13-vanilla-client-network.md` | 客户端/网络迁移指南 |
| `migration/04-client.md` | TLM Client 包迁移细节 |
| `migration/02-entity.md` | TLM Entity 迁移细节 |

---
*文档生成于 2026-06-01，基于 `master` 分支源码扫描*
