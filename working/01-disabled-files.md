# Disabled Files Summary (Core Project)

> 核验日期: 2026-06-04
> 当前核心项目 `.java.disabled`: 3 个。
> 当前 integration 相关 `.java.disabled`: 98 个，详见 [05-integration-disabled.md](./05-integration-disabled.md)。
> 禁用方法: 文件重命名为 `.java.disabled`，不会被 javac 编译。

---

## 1. 当前核心禁用文件

### 1.1 Mixin (2 files)

waiting: Mixin 目标和构造器仍需按 MC 26.1/TLM 26.1 重新验证后再启用。

- `mixin/AltarRecipeMultiOutputMixin.java.disabled`
  - 作用: 扩展 TLM `AltarRecipe` 的多输出掉落。
  - 当前问题: 文件仍使用旧 `ShapelessRecipe(String, CraftingBookCategory, ItemStack, NonNullList<Ingredient>)` 构造器。
  - 已验证新 API: MC 26.1 `ShapelessRecipe` 构造器为 `ShapelessRecipe(Recipe.CommonInfo, CraftingRecipe.CraftingBookInfo, ItemStackTemplate, List<Ingredient>)`。
- `mixin/LivingEntityBrainSerializeWrapper.java.disabled`
  - 作用: 包装 `LivingEntity.addAdditionalSaveData` 中的 `Brain.serializeStart` 调用，为女仆大脑序列化补 `RegistryOps` 上下文。
  - 已验证现状: 当前 `Brain.serializeStart` 仍是 `serializeStart(DynamicOps<T>)`；禁用文件的修复方向是当传入 ops 不是 `RegistryOps` 时改用 `level().registryAccess().createSerializationContext(...)`。
  - 待验证: `@WrapOperation` 目标描述符和运行时行为是否仍适配当前 `LivingEntity` 字节码。

### 1.2 Datagen (1 file)

- `datagen/AdvancementDataGen.java.disabled`
  - 作用: 生成本模组 advancement。
  - 当前问题: 仍引用 TLM `MaidEventTrigger`，且未在 `DataGenGatherEvent` 注册。
  - 已验证现状: `RecipeDataGen` 已恢复并注册；advancement datagen 是当前唯一禁用的核心 datagen 文件。

---

## 2. 已不再禁用的核心文件

以下文件曾在旧文档中列为禁用或待恢复，但当前已是活跃 `.java`:

| 文件 | 当前状态 |
|------|----------|
| `communicate/wish/PlaceItemWishWithLimitation.java` | 已恢复，返回 `LimitedPlaceItemStep` |
| `datagen/RecipeDataGen.java` | 已恢复，并在 `DataGenGatherEvent:18` 注册 |
| `maid/MaidExtension.java` 中 `CONFIGURABLE_COMMUNICATE_MARK` bauble 绑定 | 已恢复，当前在 `MaidExtension:45` 调用 `manager.bind(...)` |

---

## 3. 已确认活跃/恢复区域

| 区域 | 当前状态 |
|------|----------|
| 基础 Craft Type | Common, Crafting, Altar, Furnace, Brewing, Smithing, Anvil, StoneCutting 均为活跃 `.java` |
| 基础 Craft Action | Altar/Virtual/Smithing/Anvil/StoneCutting 等注册已恢复 |
| StorageManageTask | `maid/task/StorageManageTask.java` 活跃 |
| Logistics 行为 | `LogisticsOutputBehavior`, `LogisticsRecycleBehavior`, `WriteInventoryListBehavior` 活跃 |
| ClientGuiRegistry | 活跃；仅 TACZ screen 注册仍注释 |
| ItemHandler 存储 | `storage/ItemHandler` 下 7 个 `.java` 活跃 |
| 客户端事件 | BindingRender, BindingRenderSyncSender, InputEvent, PlayerInteractClient, TickClient 活跃 |
| 渲染/实体 | BoxRenderUtil, VirtualItemEntityRender, VirtualDisplayEntityRender 活跃 |
| 网络包注册 | ItemSelectorSetItemPacket, CraftGuideGuiPacket, CommunicateMarkGuiPacket, CraftGuideGeneratorUpdate, AIMatchLocalizedItemS2CPacket 均已注册 |

---

## 4. 恢复优先级

1. **P1 - Mixin**: `AltarRecipeMultiOutputMixin`, `LivingEntityBrainSerializeWrapper`
2. **P2 - Advancement Datagen**: `AdvancementDataGen`
3. **P3 - 活跃代码中的 FIXME/TODO**: 见 [02-todos-and-fixmes.md](./02-todos-and-fixmes.md)

---

## 更新记录

| 日期 | 变更 |
|------|------|
| 2026-06-04 | 重新核验 `.java.disabled`；核心禁用从 5 个修正为 3 个；确认 `PlaceItemWishWithLimitation`、`RecipeDataGen`、通信标记 bauble 绑定已恢复 |
| 2026-06-02 | 初始迁移统计 |
