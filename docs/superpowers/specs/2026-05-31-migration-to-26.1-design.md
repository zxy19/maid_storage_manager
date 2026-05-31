# Migration Design: maid_storage_manager 1.21.1 → 26.1

## Scope
将 maid_storage_manager 从 NeoForge 21.1 (MC 1.21.1) 迁移到 NeoForge 26.1 (MC 26.1)。

## Prerequisites
- 构建 TLM 26.1 jar (`D:\Minecraft\_proj\TouhouLittleMaid-26.1`)
- 替换 `libs/` 中的 TLM 依赖
- 更新其他可选依赖版本 (JEI, Jade, Cloth Config, GeckoLib 等)

## Key API Changes

### TLM API
- `TaskDataKey<T>` 接口简化, `writeSaveData`/`readSaveData` 删除, 改用 `AttachmentType<T>`
- `TaskDataRegister` 类删除, 数据注册通过 `DeferredRegister<AttachmentType<?>>`
- `ILittleMaid.registerTaskData()` 方法移除
- `EntityMaid.getOrCreateData(KEY,default)` / `setAndSyncData(KEY,data)` → `getData(T)` / `setData(T, value)`
- `EntityMaid.hasHome()` → `getData(TASK).restrictRadius() > 0`
- `isMaidInSittingPose()` 删除
- `BaubleItemHandler.serializeNBT()` 删除
- `LLMCallback` 构造函数变化
- `IChatBubbleRenderer.render()` 参数 `PoseStack` → `EntityGraphics`

### Vanilla/NeoForge API
- Item 构造函数需 `Identifier id` + `Properties.setId()`
- `@OnlyIn(Dist.CLIENT)` 移除
- `appendHoverText()` 签名变化 (`Consumer<Component>`, `TooltipDisplay`)
- `RecordCodecBuilder.create()` **保持可用**（已确认）
- `SoundEvents` 类型变化 (需 `.value()` 解包)
- `FriendlyByteBuf.writeResourceLocation/readResourceLocation` 方法变化

### Rendering
- `CustomItemRenderer` 从 `BlockEntityWithoutLevelRenderer` 迁移到 `SpecialModelRenderer<ItemStackTemplate>` 模式
- 参考: `ether_craft` 的 `AnswerItemOverlaySMR.java`

## Migration Phases

### Phase 0: 前置
1. 构建 TLM 26.1 jar
2. 替换依赖
3. 验证基础编译

### Phase 1: 数据系统 (最关键)
1. `StorageManagerConfigData.java` — 从 `TaskDataKey` 改用 `AttachmentType`
2. `DataAttachmentRegistry.java` — 注册新 AttachmentType
3. `MaidExtension.java` — 删除 `registerTaskData`
4. 所有 `getOrCreateData`/`setAndSyncData` → 新 API (~15 处)

### Phase 2: Item 注册 (~15 文件)
1. `MaidInteractItem.java` — 加 `Identifier id` 构造参数
2. `HangUpItem.java` — 同上
3. `ItemRegistry.java` — `DeferredRegister.Items` + 方法引用
4. 所有 14 个 Item 类加构造参数

### Phase 3: EntityMaid API (~8 文件)
1. `hasHome()`/`getHomePosition()`/`getHomeRadius()` 适配
2. `isMaidInSittingPose()` 替代
3. `getExperience()`/`setExperience()` 适配
4. `BaubleItemHandler.serializeNBT()` 替代
5. `LLMCallback` 构造适配

### Phase 4: 渲染 (~5 文件)
1. `CustomItemRenderer.java` → `SpecialModelRenderer` 模式
2. `CustomCommonGraphics`/`ICustomGraphics` 兼容性检查
3. `CraftingChatBubbleData.java` 移除 `@OnlyIn`
4. `IEntityGraphicsBufferSourceGetter` mixin 验证

### Phase 5: @OnlyIn 移除 (11 文件, 13 处)
纯注解删除，无逻辑变化

### Phase 6: appendHoverText 校验 (~10 文件)
验证 Consumer pattern 和 TooltipDisplay 使用

### Phase 7: 杂项
1. SoundEvents `.value()`
2. EventBusSubscriber 注解修正
3. FriendlyByteBuf 方法名适配
4. ItemStack 序列化验证

### Phase 8: 构建验证
编译通过 → 依赖更新 → AT/mixin 检查 → IDE 运行
