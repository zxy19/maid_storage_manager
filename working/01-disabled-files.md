# Disabled Files Summary (Core Project)

> 总计 5 个核心项目文件被禁用，等待后续恢复或重新实现。
> 禁用方法：文件重命名为 `.java.disabled`，不会被 javac 编译。
> 兼容模组相关 disabled 文件见 [05-integration-disabled.md](./05-integration-disabled.md)（108 个）。

---

## 1. Mixin — TLM API 适配（2 files）

waiting: ShapelessRecipe 构造器签名变化，Brain.serializeStart 签名变化

- `mixin/AltarRecipeMultiOutputMixin.java`
  - 扩展 TLM AltarRecipe 的多输出支持。MC 26.1 中 ShapelessRecipe 构造器从 `(String, Category, ItemStack, List)` 变为 `(CommonInfo, BookInfo, ItemStackTemplate, List<ItemStack>)`
- `mixin/LivingEntityBrainSerializeWrapper.java`
  - 女仆大脑序列化包装。MC 26.1 中 `Brain.serializeStart` 需要 `RegistryOps` 参数

---

## 2. 通信系统（1 file）

reason: 级联禁用

- `communicate/wish/PlaceItemWishWithLimitation.java`

---

## 3. Datagen（2 files）

reason: TLM datagen API 不兼容 MC 26.1

- `datagen/RecipeDataGen.java` — TLM AltarRecipeBuilder 不兼容 data component 配方
- `datagen/AdvancementDataGen.java` — TLM MaidEvent trigger 类在 datagen 环境不可用

---

## 4. 已恢复文件列表

以下核心项目文件已从 `.java.disabled` 恢复为 `.java`（共 18 个）：

### Phase 1（8 files）

| 分类 | 文件 |
|------|------|
| Craft Types | CommonType, CraftingType, AltarType, FurnaceType, BrewingType, SmithingType, AnvilType, StoneCuttingType |
| Maid 任务 | StorageManageTask |
| Maid 行为 | LogisticsOutputBehavior, LogisticsRecycleBehavior, WriteInventoryListBehavior |
| Maid 配置 | StorageManagerMaidConfigGui |
| Craft 生成器 | GeneratorAltar |
| 注册表 | ClientGuiRegistry |
| 存储 | ItemHandler 全部 5 个 |

### Phase 2（10 files）

| 分类 | 文件 |
|------|------|
| 客户端事件 | BindingRender, BindingRenderSyncSender, InputEvent, PlayerInteractClient, TickClient |
| 渲染/实体 | BoxRenderUtil, VirtualItemEntityRender, VirtualDisplayEntityRender (FIXME-02) |
| 网络包 | AIMatchLocalizedItemS2CPacket |

### 累计已恢复

| 类别 | 数量 |
|------|------|
| 核心项目 | 18 |
| Integration（详见 `05-integration-disabled.md`）| 19 |
| **总计** | **37** |

---

## 5. 恢复优先级

1. **P1 — Mixin**：AltarRecipeMultiOutputMixin, LivingEntityBrainSerializeWrapper
2. **P2 — 通信**：PlaceItemWishWithLimitation
3. **P3 — Datagen**：RecipeDataGen, AdvancementDataGen（可能需要等 TLM datagen API 更新）

---

*文档更新于 2026-06-02*
