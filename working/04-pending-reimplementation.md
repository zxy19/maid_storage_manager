# Pending Reimplementation (Core Project)

> 核验日期: 2026-06-04
> 本文只记录核心项目仍需恢复或重新实现的内容。
> 兼容模组集成恢复见 [05-integration-disabled.md](./05-integration-disabled.md)。

---

## 已完成/不再待恢复

| 项目 | 当前状态 |
|------|----------|
| Craft Types Recovery | 8 个基础 Type 和基础 Action 已注册 |
| ClientGuiRegistry | 已恢复，TACZ screen 除外 |
| StorageManageTask | 已恢复并在 `MaidExtension` 注册 |
| StorageManagerMaidConfigGui | 已恢复 |
| ItemHandler Storage System | 已恢复，当前 7 个 ItemHandler `.java` 活跃 |
| Client Events | 5 个客户端事件文件已恢复 |
| Entity/Box Render | VirtualItemEntityRender、VirtualDisplayEntityRender、BoxRenderUtil 已恢复 |
| AIMatchLocalizedItemS2CPacket | 已恢复注册 |
| `PlaceItemWishWithLimitation` | 已恢复 |
| `RecipeDataGen` | 已恢复并在 `DataGenGatherEvent` 注册 |
| 网络包注册 | ItemSelectorSetItemPacket、CraftGuideGuiPacket、CommunicateMarkGuiPacket、CraftGuideGeneratorUpdate 均已恢复 |
| CONFIGURABLE_COMMUNICATE_MARK Bauble | 已在 `MaidExtension` 恢复绑定 |
| LogisticsGuideMenu 网络分支 | 已恢复 |

---

## P1 - 核心 Mixin 恢复

### Mixin 适配 (2 files)

| Mixin | 当前问题 | 已验证 API/状态 |
|-------|----------|------------------|
| `AltarRecipeMultiOutputMixin.java.disabled` | 旧 `ShapelessRecipe` 构造器不兼容 | 当前构造器为 `(Recipe.CommonInfo, CraftingRecipe.CraftingBookInfo, ItemStackTemplate, List<Ingredient>)` |
| `LivingEntityBrainSerializeWrapper.java.disabled` | 需要重新验证 `LivingEntity.addAdditionalSaveData` 注入点和 `RegistryOps` 包装逻辑 | `Brain.serializeStart` 当前仍是 `DynamicOps<T>` 参数；禁用文件目标是补 serialization context |

**修复步骤:**

1. 用 IDE 符号/反编译确认目标类当前字段和方法描述符。
2. 更新 mixin 构造器和 `@WrapOperation` target。
3. 恢复 `.java` 后缀并加入 `maid_storage_manager.mixins.json`。
4. 运行客户端或最小化 mixin 加载验证。

---

## P2 - Datagen 恢复

### AdvancementDataGen (1 file)

- **文件**: `datagen/AdvancementDataGen.java.disabled`
- **当前状态**: 禁用。
- **阻塞点**: 引用 TLM `MaidEventTrigger`，需要确认当前 TLM 26.1 datagen 环境是否可用。
- **当前 datagen 状态**: `RecipeDataGen`、Tag/Model datagen 已注册；advancement provider 未注册。

**修复方案:**

1. 用 IDE 解析 `MaidEventTrigger` 当前 API。
2. 如果 TLM trigger 仍不能在 datagen classpath 使用，改成原始 JSON/provider 生成策略。
3. 恢复后在 `DataGenGatherEvent` 注册 advancement provider。

---

## P3 - 活跃代码中的核心 TODO/FIXME

这些不是 `.java.disabled`，但仍代表核心功能或行为缺口。详见 [02-todos-and-fixmes.md](./02-todos-and-fixmes.md)。

| 项目 | 文件 | 说明 |
|------|------|------|
| SlotType ETA/背包 slot 写操作 | `communicate/data/SlotType.java` | 需要完成 `CombinedResourceHandler` transaction 迁移 |
| RequestList / ItemSelector Trigger | `RequestListItem.java`, `ItemSelectorMenu.java` | Tour Guide 或替代 trigger 待确认 |
| CraftResultContext `setPlaceBefore()` | `craft/data/CraftResultContext.java` | 需要验证合成顺序与背包容量策略 |
| CommonCraftScreen 多行 y offset | `menu/craft/common/CommonCraftScreen.java` | UI 显示问题 |
| VirtualDisplayEntity FIXME 注释 | `entity/VirtualDisplayEntity.java` | `dropItem` 签名已验证正确，后续可清理注释 |

---

## P4 - 已移除或空壳的辅助集成

### Tour Guide

- `TourGuideRegister.java`、`ScreenPredicator.java`、`InventoryListTour.java` 内容整体注释。
- `RequestListTour.java`、`CraftGuideTour.java` 当前为空类。
- 相关 trigger 调用仍注释或 TODO。

### HumanoidModelMixin

- 当前项目中未找到 `mixin/client/HumanoidModelMixin.java` 或 `.java.disabled`。
- 旧文档中的“玩家骑乘女仆时手臂角度设置”条目无法在当前源码树定位，已从核心待恢复清单移除。

---

## 恢复顺序建议

```text
Phase 1:
  - AltarRecipeMultiOutputMixin
  - LivingEntityBrainSerializeWrapper

Phase 2:
  - AdvancementDataGen

Phase 3:
  - SlotType ETA/背包 slot transaction 迁移
  - Tour Guide / trigger 体系确认
  - CraftResultContext setPlaceBefore 策略验证
  - CommonCraftScreen 多行显示修复
```

---

## 参考文档

| 文档 | 用途 |
|------|------|
| `working/01-disabled-files.md` | 核心项目禁用文件清单 |
| `working/02-todos-and-fixmes.md` | 当前活跃代码中的 TODO/FIXME |
| `working/03-deleted-functionality.md` | 核心项目已删除或注释功能 |
| `working/05-integration-disabled.md` | 兼容模组集成状态 |
| `migration/` | API 迁移参考 |

---

## 更新记录

| 日期 | 变更 |
|------|------|
| 2026-06-04 | 移除已恢复的通信、RecipeDataGen、网络包、bauble 绑定；新增当前剩余核心待恢复项 |
| 2026-06-02 | 旧版核心待恢复清单 |
