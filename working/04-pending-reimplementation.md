# Pending Reimplementation

> 需要重新实现的核心功能。按优先级（P0-P3）和功能模块组织。
> 总计: 134 个 `.java.disabled` 文件 + 多个被注释的功能块等待恢复。
> 自上次更新以来: 27 个文件已完成恢复。

---

## ✅ 已完成（Phase 1 & 2 核心项）

### 1. Craft Types Recovery — ✅ 完成
- **8 个基础 Type 类已恢复**: CommonType, CraftingType, AltarType, FurnaceType, BrewingType, SmithingType, AnvilType, StoneCuttingType
- **CraftManager.java:75-82** 中 addCraftType 注册已解除注释
- **CraftManager.java:165-224** 中 6 个 addAction 注册已解除注释
- **GeneratorAltar** 已恢复
- 剩余 3 个兼容模组 Type (AE2Type, RSType, TaczType) 仍等待各自模组

### 2. ClientGuiRegistry — ✅ 完成
- `registry/ClientGuiRegistry.java` 已重新创建并激活
- 所有 Screen → MenuType 绑定已恢复（除 TaczCraftScreen 仍注释）

### 3. StorageManageTask (核心任务) — ✅ 完成
- `maid/task/StorageManageTask.java` 已恢复
- `MaidExtension.java:36` 中任务注册已解除注释
- 3 个子行为类均已恢复: LogisticsOutputBehavior, LogisticsRecycleBehavior, WriteInventoryListBehavior
- `MaidExtension.java:92,97` 两处 tips 条件检查已恢复

### 4. StorageManagerMaidConfigGui — ✅ 完成
- `maid/config/StorageManagerMaidConfigGui.java` 已从 `.disabled` 恢复为 active

### 5. ItemHandler Storage System — ✅ 完成
- 5 个 `storage/ItemHandler/*.java` 文件已全部恢复

### 6. JEI 核心集成 — ✅ 部分完成
- 4 个 `integration/jei/` 文件已恢复: Plugin, GhostIngredientHandler, IFilterScreen, RequestRecipeHandler
- 3 个 JEI 配方处理器已恢复: JEIRecipeHandler, JEICommonRecipeHandler, JeiStoneCutterRecipeHandler
- **仍待 JEI MC 26.1 版本**: mixin (2), request 系统 (4)

---

## P1 — 重要功能，中等优先级

### 7. CraftManager 兼容模组生成器恢复
- **文件**: `CraftManager.java:235-300` — AE2, RS, Create, Mekanism, Botania, Ars Nouveau 的生成器和类型注册
- **阻塞原因**: 所有兼容模组均未发布 MC 26.1 版本
- **修复方案**: 等待对应模组发布 MC 26.1 版本后逐个恢复：
  1. 恢复模组的集成类（`integration/` 下的 `.disabled` 文件）
  2. 解除 `CraftManager.java` 中对应注册块的注释
  3. 适配该模组的新 API
- **预计工作量**: 分散，每个模组 2-6h

### 8. Network Packets 恢复
- **文件**: `Network.java` 中 5 个被注释的包注册
- **需要恢复的包**:

| 包名 | 类型 | 位置 |
|------|------|------|
| `ItemSelectorSetItemPacket` | C2S | Network.java:96-114 |
| `CraftGuideGuiPacket` | 双向 | Network.java:200-213 |
| `CommunicateMarkGuiPacket` | 双向 | Network.java:308-317 |
| `CraftGuideGeneratorUpdate` | 双向 | Network.java:318-327 |
| `AIMatchLocalizedItemS2CPacket` | S2C | Network.java:337-344 |

- **修复方案**:
  1. 对于 `ItemSelectorSetItemPacket` — 检查 `ItemSelectorMenu.filteredItems.setItem` API 是否可用
  2. 对于 `CraftGuideGuiPacket` — 恢复 `ICraftGuiPacketReceiver` 接口
  3. 对于 `AIMatchLocalizedItemS2CPacket` — 恢复 AI 对话物品匹配同步，适配新 StreamCodec API
- **预计工作量**: 中 (3-5h)

### 9. 被注释的 Network Handler 恢复
- **位置**: `Network.java` 中 4 个 handler 内容被注释但注册保留的包
- **被注释内容**:
  - `JEIRequestPacket` handler — `IngredientRequest.onRequest()` 调用
  - `JEIRequestResultPacket` handler — `InScreenTipData.show()` 调用
  - `MaidDataSyncToClientPacket` handler — bauble `deserializeNBT()` 调用
  - `CreateStockManagerPacket` handler — `StockManagerInteract.handle()` 调用
- **修复方案**: 依次恢复被依赖的类，验证新 API 签名
- **预计工作量**: 分散，每个 0.5-2h
- **依赖**: 多个 P3 项（兼容模组集成）

---

## P2 — 渲染/事件适配

### 10. Entity Render 适配
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

### 11. Box Render 恢复
- **文件**: `util/BoxRenderUtil.java.disabled`
- **描述**: 用于调试和指示的盒子/立方体渲染工具
- **阻塞原因**: `RenderLevelStageEvent` API 变化
- **修复方案**:
  1. 研究 MC 26.1 中 `RenderLevelStageEvent` 的新事件类型
  2. 适配原有的渲染调用为 SubmitNodeCollector 模式
  3. 或使用 MC 26.1 新增的 `LevelRenderer.renderDebug()` 钩子
- **预计工作量**: 小 (2-3h)

### 12. Client Events 恢复
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

### 13. Mixin 恢复
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

---

## P3 — 低优先级，依赖外部模组更新

### 14. 兼容模组集成恢复

#### AE2 (9 文件)
- **等待**: AE2 MC 26.1 版本发布
- **文件**: 5 个 storage + 3 个 craft generator + 1 个 craft type + 1 个 craft action
- **额外**: 解除 `CraftManager.java:235-247` 中 AE2 注册

#### RS (7 文件)
- **等待**: Refined Storage MC 26.1 版本发布
- **文件**: 5 个 storage + 1 个 craft action + 1 个 craft type
- **额外**: 解除 `CraftManager.java:248-260` 中 RS 注册

#### Create (23 文件)
- **等待**: Create MC 26.1 版本发布
- **文件**: 9 个 generator + 3 个 mixin + 5 个 storage + 5 个 integration
- **额外**: 恢复 `CreateStockManagerPacket` handler、恢复 `StartUpEvent.java:28` CreateIntegration.init()
- **额外**: 解除 `CraftManager.java:264-273` 中 Create 注册

#### Mekanism (14 文件)
- **等待**: Mekanism MC 26.1 版本发布
- **文件**: 8 个 generator + 1 个 integration + 5 个 QIO storage
- **额外**: 解除 `CraftManager.java:274-282` 中 Mekanism 注册

#### JEI (6 文件 仍待恢复)
- **等待**: JEI MC 26.1 版本发布
- **文件**: 2 个 mixin + 2 个 request + 1 个 API + IngredientRequest (2)
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
- **额外**: 解除 `CraftManager.java:261-263` 中 TACZ 注册、`ClientGuiRegistry` 中 TaczCraftScreen

#### Sophisticated Storage (1 文件)
- **等待**: Sophisticated Storage MC 26.1 版本发布

#### Cloth Config (2 文件)
- **等待**: Cloth Config MC 26.1 版本发布
- **修复方案**: 适配新的包名和 ConfigBuilder API
- **额外**: 恢复 `StartUpEvent.java:29-32` 中 Cloth 事件注册

#### Jade (2 文件)
- **等待**: Jade MC 26.1 版本发布
- **修复方案**: 适配 Jade 新 API

#### The One Probe
- **状态**: `integration/top/` 目录已完全移除，无遗留代码
- **等待**: TOP MC 26.1 版本发布后重新创建集成

### 15. Ingredient Request 系统 (2 文件)
- **文件**: `integration/request/IngredientRequest.java.disabled`, `IngredientRequestClient.java.disabled`
- **描述**: JEI/EMI 到请求系统的桥梁
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
  2. 或回退到使用原始 JSON 生成
- **预计工作量**: 中 (3-5h)

### 17. HumanoidModelMixin 修复
- **文件**: `mixin/client/HumanoidModelMixin.java` (被 `/ FIXME` 注释)
- **描述**: 玩家骑乘女仆时的手臂角度设置
- **阻塞原因**: 具体问题待调查
- **预计工作量**: 小 (1h)

### 18. CONFIGURABLE_COMMUNICATE_MARK Bauble
- **位置**: `MaidExtension.java:45`
- **状态**: 注册被注释掉
- **描述**: 可配置通信标记饰品绑定

---

## 恢复顺序建议

```
Phase 1 (P1 — 重要功能):
  ├── 7.  Network Packets 恢复 (5 个包)
  ├── 8.  Network Handler 恢复 (4 个 handler)
  └── 9.  Mixin 恢复 (2 个 Mixin)

Phase 2 (P2 — 渲染/事件):
  ├── 10. Entity Render 适配 (SubmitNodeCollector)
  ├── 11. Box Render 恢复
  ├── 12. Client Events 恢复
  └── 13. CraftManager 兼容模组生成器 (等待外部)

Phase 3 (P3 — 等待外部):
  ├── 14. 兼容模组集成 (逐个恢复，依赖各自更新)
  ├── 15. Ingredient Request 系统
  ├── 16. Datagen 恢复
  ├── 17. HumanoidModelMixin 修复
  └── 18. CONFIGURABLE_COMMUNICATE_MARK Bauble
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

## 更新记录

| 日期 | 变更 |
|------|------|
| 2026-06-01 | 移除 6 项已完成任务（Craft Types, ClientGuiRegistry, StorageManageTask, MaidConfigGui, ItemHandler Storage, JEI 核心）；重排优先级；新增 CONFIGURABLE_COMMUNICATE_MARK 恢复项；更新兼容模组文件计数 |

---

*文档更新于 2026-06-01*
