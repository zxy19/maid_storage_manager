# Pending Reimplementation (Core Project)

> 需要重新实现的核心项目功能，按优先级（P0-P3）组织。
> 兼容模组集成恢复见 [05-integration-disabled.md](./05-integration-disabled.md)。

---

## ✅ 已完成

### 1. Craft Types Recovery — ✅ 完成
- 8 个基础 Type 类: CommonType, CraftingType, AltarType, FurnaceType, BrewingType, SmithingType, AnvilType, StoneCuttingType
- GeneratorAltar 已恢复
- CraftManager.java 中 addCraftType (8) 和 addAction (6) 注册已解除注释

### 2. ClientGuiRegistry — ✅ 完成
- `registry/ClientGuiRegistry.java` 已重新创建并激活

### 3. StorageManageTask — ✅ 完成
- `maid/task/StorageManageTask.java` + 3 个子行为类已恢复

### 4. StorageManagerMaidConfigGui — ✅ 完成
- `maid/config/StorageManagerMaidConfigGui.java` 已恢复

### 5. ItemHandler Storage System — ✅ 完成
- 5 个 `storage/ItemHandler/*.java` 文件已全部恢复

### 6. Client Events — ✅ 完成
- 5 个客户端事件文件全部恢复并适配 MC 26.1: BindingRender, BindingRenderSyncSender, InputEvent, PlayerInteractClient, TickClient

### 7. Entity/Box Render — ✅ 完成
- VirtualItemEntityRender, VirtualDisplayEntityRender (FIXME-02 已解决), BoxRenderUtil 全部恢复

### 8. AIMatchLocalizedItemS2CPacket — ✅ 完成

---

## P1 — Mixin 恢复

### Mixin 适配 (2 files)

| Mixin | 问题 | 新 API |
|-------|------|--------|
| `AltarRecipeMultiOutputMixin` | ShapelessRecipe 构造器 | `(CommonInfo, BookInfo, ItemStackTemplate, List)` |
| `LivingEntityBrainSerializeWrapper` | Brain 序列化 | `serializeStart(RegistryOps)` |

**修复方案:**
1. 查看 MC 26.1 中 `ShapelessRecipe` 和 `Brain` 的完整签名
2. 更新 Mixin 目标方法签名
3. 恢复文件后缀并加入 `mixins.json`

**预计工作量**: 小 (1-2h)

---

## P2 — 网络数据包与通信

### Network Packets 恢复 (5 个被注释的包注册)

| 包名 | 类型 | 位置 |
|------|------|------|
| `ItemSelectorSetItemPacket` | C2S | Network.java:96-114 |
| `CraftGuideGuiPacket` | 双向 | Network.java:200-213 |
| `CommunicateMarkGuiPacket` | 双向 | Network.java:308-317 |
| `CraftGuideGeneratorUpdate` | 双向 | Network.java:318-327 |

**恢复方式:**
1. `ItemSelectorSetItemPacket` — 检查 `ItemSelectorMenu.filteredItems.setItem` API
2. `CraftGuideGuiPacket` — 恢复 `ICraftGuiPacketReceiver` 接口
3. `CommunicateMarkGuiPacket` — 恢复通信标记数据同步
4. `CraftGuideGeneratorUpdate` — 恢复生成器配置同步

**预计工作量**: 中 (3-5h)

### Network Handler 恢复 (被注释内容)

| Handler | 位置 | 被注释内容 |
|---------|------|-----------|
| `MaidDataSyncToClientPacket` | :265-266 | bauble `deserializeNBT()` 调用 |
| `CreateStockManagerPacket` | :277-283 | 级联 Create 禁用 |

---

## P3 — 通信与 Datagen

### Communicate 恢复 (1 file)
- **文件**: `communicate/wish/PlaceItemWishWithLimitation.java`
- **描述**: 通信意愿系统中的受限物品放置逻辑
- **阻塞原因**: 级联禁用
- **预计工作量**: 小 (1h)

### Datagen 恢复 (2 files)
- **文件**: 
  - `datagen/RecipeDataGen.java` — TLM 特殊配方格式 datagen
  - `datagen/AdvancementDataGen.java` — 进度 datagen
- **阻塞原因**:
  - RecipeDataGen: TLM AltarRecipeBuilder 不兼容 data component 配方
  - AdvancementDataGen: TLM MaidEvent trigger 类在 datagen 环境不可用
- **修复方案**: 等待 TLM 26.1 提供完整的 Builder API，或回退到原始 JSON 生成
- **预计工作量**: 中 (3-5h)

### HumanoidModelMixin 修复
- **文件**: `mixin/client/HumanoidModelMixin.java` (被 `// FIXME` 注释)
- **描述**: 玩家骑乘女仆时的手臂角度设置
- **预计工作量**: 小 (1h)

### CONFIGURABLE_COMMUNICATE_MARK Bauble
- **位置**: `MaidExtension.java:45`
- **状态**: 注册被注释
- **描述**: 可配置通信标记饰品绑定

---

## 恢复顺序建议（核心项目）

```
Phase 1 (P1 — Mixin):
  └── Mixin 恢复 (2 files, 1-2h)

Phase 2 (P2 — 网络):
  ├── Network Packets 恢复 (4 个包, 3-5h)
  └── Network Handler 恢复 (2 个, 2-3h)

Phase 3 (P3 — 其他):
  ├── Communicate 恢复 (1 file, 1h)
  ├── Datagen 恢复 (2 files, 3-5h)
  ├── HumanoidModelMixin 修复 (1h)
  └── CONFIGURABLE_COMMUNICATE_MARK Bauble
```

---

## 参考文档

| 文档 | 用途 |
|------|------|
| `working/01-disabled-files.md` | 核心项目禁用文件清单 |
| `working/02-todos-and-fixmes.md` | 当前活跃代码中的 TODO/FIXME |
| `working/03-deleted-functionality.md` | 核心项目已删除功能 |
| `working/05-integration-disabled.md` | 兼容模组集成状态 |
| `migration/` | API 迁移参考 |

---

## 更新记录

| 日期 | 变更 |
|------|------|
| 2026-06-02 | 提取兼容模组内容至 05-integration-disabled.md；仅保留核心项目项 |
| 2026-06-01 | Client Events/Entity Render 等 10 项确认完成 |

---

*文档更新于 2026-06-02*
