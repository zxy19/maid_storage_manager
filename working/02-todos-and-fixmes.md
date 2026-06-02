# TODO/FIXME Inventory

> 自动扫描生成，手工更新 | Updated: 2026-06-02
> 项目: maid_storage_manager | 目标: NeoForge 26.1 + Touhou Little Maid 26.1

---

## 概述

| 类型 | 数量 | 状态 |
|------|------|------|
| FIXME | 4 | 影响功能运行，需优先修复 |
| TODO | 3 | 功能未完成，可延后 |
| 迁移文档中记录的 TODO | 1 | 第三方兼容缺失 |

> 兼容模组 disabled 文件中的 TODO 已移至 [05-integration-disabled.md](./05-integration-disabled.md)
> FIXME-02 已修复（渲染管线全面适配），FIXME-03 已修复（updateCollectedNotStored API）

---

## FIXME（紧急 - 阻碍编译或运行时崩溃）

---

### FIXME-01: CombinedResourceHandler 不支持 setStackInSlot

| 属性 | 值 |
|------|-----|
| 文件 | `src/main/java/studio/fantasyit/maid_storage_manager/communicate/data/SlotType.java` |
| 行号 | 120, 123, 131, 207 |
| 优先级 | **CRITICAL** |
| 是否阻碍编译 | 否（相关代码已注释） |
| 是否影响运行 | **是** — ETA 背包槽位、iterItemExceptSlotForMaid 功能缺失 |

**问题描述：**

TLM 26.1 将女仆背包 API 从 `IItemHandler`（Forge 风格）迁移至 NeoForge 26.1 的 `CombinedResourceHandler<ItemResource>`。后者不支持 `setStackInSlot(slot, stack)` 写操作，需要使用 `Transaction` 模式的 `extract()`/`insert()`。

**涉及位置：**

1. **行 120 (ETA case 的 process 方法)：**
   - `// FIXME: TLM 26.1 - CombinedResourceHandler doesn't support setStackInSlot.`

2. **行 123 (ETA case 的 RangedResourceHandler 组合)：**
   - `// FIXME: TLM 26.1 - Cannot compose RangedResourceHandlers into CombinedResourceHandler safely for write ops.`

3. **行 131 (iterItemExceptSlotForMaid 方法)：**
   - `// FIXME: TLM 26.1 - getAvailableInv returns CombinedResourceHandler which has no setStackInSlot.`

4. **行 207 (ETA case 的 doPlace 方法)：**
   - `// FIXME: TLM 26.1 - Cannot create CombinedResourceHandler from RangedResourceHandler sub-ranges for write ops.`

**涉及 API 变化：**

```
TLM 26.1 之前: IItemHandler handler = maid.getAvailableInv(false);
               handler.setStackInSlot(slot, stack);      // 直接写

TLM 26.1 之后: CombinedResourceHandler<ItemResource> handler = maid.getAvailableInv(false);
               handler.extract(slot, old, count, tx);     // Transaction 写
               handler.insert(slot, resource, count, tx);  // Transaction 写
               // 不再有 setStackInSlot
```

**已有适配：** `replaceSlot()` 方法（行 143-155）已用 `Transaction` 实现 extract+insert 模式。但该工具方法假定传入完整的 `CombinedResourceHandler`，无法处理 sub-range 场景。

**建议修复方案：**

1. 对于 `ETA` 的 `process` 方法：确认背包槽索引范围（例如索引 8..N），直接用 `replaceSlot(handler, i, ...)` 遍历
2. 对于 `iterItemExceptSlotForMaid`：解除注释，将原本的 `IItemHandler` 操作替换为 `replaceSlot()` 调用
3. 对于 sub-range 场景：考虑使用 `CombinedResourceHandler.composeOf()` 或创建自定义 `CombinedResourceHandler` 子类包装 sub-range

**影响范围：**
- `SlotType.ETA` 的所有槽位遍历和修改操作
- 通信背包的"除指定槽位外遍历"逻辑
- 未修复会导致：ETA 背包槽处理失败、物品通信系统的部分功能不工作

---

### ~~FIXME-02: MC 26.1 渲染管线重构 - VirtualDisplayEntity 的 item 渲染~~ ✅ 已修复

| 属性 | 值 |
|------|-----|
| 文件 | `src/main/java/studio/fantasyit/maid_storage_manager/entity/VirtualDisplayEntityRender.java` |
| 行号 | ~~26, 34~~ |
| 优先级 | ~~HIGH~~ |
| 状态 | **RESOLVED** |

**修复方式：** `submit()` 方法已完整实现 CORNER、LARGE 和 ICON 三种渲染模式。使用 `SubmitNodeCollector` + `ItemStack.submit()` 提交自定义 item 渲染，支持 `RenderItemInFrameEvent` 事件钩子。所有 FIXME 注释已移除。

---

### ~~FIXME-03: updateCollectedNotStored API 不兼容~~ ✅ 已修复

| 属性 | 值 |
|------|-----|
| 文件 | `src/main/java/studio/fantasyit/maid_storage_manager/maid/behavior/request/ret/RequestRetBehavior.java` |
| 行号 | ~~234-235~~ |
| 优先级 | ~~HIGH~~ |
| 状态 | **RESOLVED** |

**修复方式：** `updateCollectedNotStored` 方法签名已适配，现在直接接受 `CombinedResourceHandler`，调用已解除注释。

---

### FIXME-04: GuiGraphicsExtractor.flush() 和 setColor() 已移除

| 属性 | 值 |
|------|-----|
| 文件 | `src/main/java/studio/fantasyit/maid_storage_manager/communicate/data/SlotType.java` |
| 行号 | 231 |
| 优先级 | **MEDIUM** |
| 是否阻碍编译 | 否（相关代码已注释） |
| 是否影响运行 | **是** — 金色 slot 图标效果缺失 |

**问题描述：**

`drawGold()` 方法用于在 GUI 中渲染带金色叠加效果的 slot 图标。原有实现使用：
1. `graphics.flush()` — 刷新渲染批次
2. `graphics.setColor(1.69f, 1.69f, 0.04f, 1.0f)` — 设置金色 tint
3. 多次 `icon.blit()` 调用实现叠加闪烁效果

MC 26.1 / TLM 26.1 移除了 `GuiGraphicsExtractor.flush()` 和 `setColor()` 方法。

**当前状态：** 仅保留基础的 `icon.blit(graphics, x, y)` 调用，无金色效果。FIXME 注释行 231：
```
// FIXME: TLM 26.1 - GuiGraphicsExtractor.flush() and setColor() removed.
```

**涉及 API 变化：**

```
TLM 26.1 之前: graphics.setColor(r, g, b, a);  // 笔刷颜色
               graphics.flush();                // 刷新批次

TLM 26.1:      已移除
```

**建议修复方案：**

1. 研究 MC 26.1 Blaze3D 的渲染状态/RenderPipeline API
2. 使用 `RenderSystem.setShaderColor()` 或 NeoForge 等价方法来设置颜色叠加
3. 如果不支持 flush 语义，使用单次带 tint 的 `blit()` 调用替代多次叠加

**影响范围：**
- `CommunicateScreen` 中 slot 图标渲染
- 仅视觉效果，不影响功能
- 当前表现为纯白色 slot 图标（无金色高亮）

---

### FIXME-05: kill() 已重命名, dropItem() 签名变化

| 属性 | 值 |
|------|-----|
| 文件 | `src/main/java/studio/fantasyit/maid_storage_manager/entity/VirtualDisplayEntity.java` |
| 行号 | 31 |
| 优先级 | **MEDIUM** |
| 是否阻碍编译 | 否（已用 discard() 替代） |
| 是否影响运行 | **可能** — dropItem 签名需验证 |

**问题描述：**

`VirtualDisplayEntity.hurtServer()` 方法处理实体被攻击时的行为：
1. `kill()` → 已改为 `discard()`（MC 26.1 重命名）
2. `dropItem(level, p_31776_.getEntity())` — FIXME 注释提示签名在 MC 26.1 中改变

**当前代码：**

```java
// FIXME: MC 26.1 - kill() renamed, dropItem() signature changed
this.discard();
this.markHurt();
this.dropItem(level, p_31776_.getEntity());
```

**涉及 API 变化：**

```
MC 26.0 及之前: kill()
MC 26.1:       discard()  (已适配)

MC 26.0:       dropItem() / dropItem(ItemStack)  (无参数)
MC 26.1:       dropItem(ServerLevel, Entity) 或 dropItem(ServerLevel, DamageSource)?
```

**建议修复方案：**

1. 使用 `idea_get_symbol_info` 检查 `ItemFrame.dropItem` 在 MC 26.1 中的正确签名
2. 确认是否改为 `dropItem(ServerLevel)` 或其他签名
3. 调整参数传递

**影响范围：**
- 虚拟展示框被破坏时的物品掉落行为
- 如果签名不对，编译期就会失败（目前能编译，可能是签名恰好兼容或未被严格检查）

---

### FIXME-06: 多行文本 y 偏移未计算

| 属性 | 值 |
|------|-----|
| 文件 | `src/main/java/studio/fantasyit/maid_storage_manager/menu/craft/common/CommonCraftScreen.java` |
| 行号 | 561 |
| 优先级 | **LOW** |
| 是否阻碍编译 | 否 |
| 是否影响运行 | **是** — 生成器错误信息多行堆叠显示 |

**问题描述：**

`renderGeneratorDecorations()` 方法渲染合成指引界面中的生成器错误信息。当错误文本超过 90 像素宽度时，由 `font.split()` 自动换行为多行。但循环中没有增加 y 偏移，导致所有行在 `(0, 0)` 位置堆叠渲染。

**当前代码：**

```java
for (FormattedCharSequence line : font.split(..., 90)) {
    graphics.text(font, line, 0, 0, wordWrapColor);
    // FIXME: y offset needed for multiline
}
```

**建议修复方案：**

```java
int yOffset = 0;
for (FormattedCharSequence line : font.split(..., 90)) {
    graphics.text(font, line, 0, yOffset, wordWrapColor);
    yOffset += font.lineHeight + 2;  // 或 font.lineHeight + 1
}
```

**影响范围：**
- 合成指引 GUI 中的生成器错误信息显示
- 仅影响超过一行的长错误文本
- 单行文本无影响

---

## TODO（功能不完整）

---

### TODO-01: 请求列表绑定 Trigger

| 属性 | 值 |
|------|-----|
| 文件 | `src/main/java/studio/fantasyit/maid_storage_manager/items/RequestListItem.java` |
| 行号 | 111 |
| 优先级 | **LOW** |
| 状态 | 未实现 |

**问题描述：**

当玩家用 RequestListItem 绑定存储位置时，应触发进度/成就触发器。行 111 有已注释的参考实现：

```java
//TODO bind Trigger
```

**建议修复方案：**

1. 确认 `TourGuideTrigger` 类是否存在或已被替代
2. 如果是第三方 API 不可用，考虑使用原版 `CriteriaTriggers` 或自定义 advancement trigger
3. 该行解除注释并确保依赖可用

**影响范围：**
- 仅影响教程/进度系统
- 不影响核心功能

---

### TODO-02: 物品选择器菜单 Trigger

| 属性 | 值 |
|------|-----|
| 文件 | `src/main/java/studio/fantasyit/maid_storage_manager/menu/request/ItemSelectorMenu.java` |
| 行号 | 340 |
| 优先级 | **LOW** |
| 状态 | 未实现 |

**问题描述：**

`ItemSelectorMenu.removed(Player)` 方法在菜单关闭时调用。注释 `//TODO trigger` 表明应在此处触发某事件或回调。

`ItemSelectorMenu` 是物品选择器 GUI 的容器，`removed()` 在 GUI 关闭时被调用。

**建议修复方案：**

1. 确认触发对象和 trigger 类型（可能是 `TourGuideTrigger`、网络同步、或状态保存）
2. 参考 `ItemSelectorMenu` 中其他方法（如 `save()`）的实现模式

**影响范围：**
- 物品选择器 GUI 关闭时的回调
- 不影响核心功能

---

### TODO-03: 合成结果上下文 - 验证 setPlaceBefore 可行性

| 属性 | 值 |
|------|-----|
| 文件 | `src/main/java/studio/fantasyit/maid_storage_manager/craft/data/CraftResultContext.java` |
| 行号 | 47 |
| 优先级 | **LOW** |
| 状态 | 实验性代码，已注释 |

**问题描述：**

在 `calculate()` 方法中，当检测到合成消耗超过背包容量时（`maxSlotConsume < currentMaxConsume`），代码尝试通过 `layer.setPlaceBefore()` 在合成前先放置物品腾出空间。该逻辑行 48 被注释掉，注释标记为 `//TODO:验证可行性`。

这种"合成间隙放置物品"的策略可能打乱合成执行顺序或导致资源竞争。

**建议修复方案：**

1. 编写测试用例模拟"背包容量不足"的合成场景
2. 验证 `setPlaceBefore()` 与合成算法（拓扑排序/DFS求解器）的兼容性
3. 验证放置操作不会导致后续合成的物品被意外存储

**影响范围：**
- 合成算法的高级优化策略
- 未启用时：大合成在背包空间不足时会失败而非自动清理空间

---

## 已禁用文件中的 TODO

> 兼容模组 disabled 文件中的 TODO（Create GeneratorCreate、Mekanism GeneratorMekOsmiumComp）已移至 [05-integration-disabled.md](./05-integration-disabled.md)。

---

## 迁移文档中记录的 TODO

| 位置 | 内容 | 状态 |
|------|------|------|
| `mixin/client/HumanoidModelMixin.java` | 被禁用 (`// FIXME`) | 玩家骑乘女仆时的手臂角度设置 |

---

## 修复优先级建议

| 排序 | 编号 | 简述 | 理由 |
|------|------|------|------|
| 1 | FIXME-01 | CombinedResourceHandler 写操作 | 导致 ETA 背包槽功能完全不可用 |
| 2 | FIXME-05 | dropItem 签名 | 实体掉落行为可能异常 |
| 3 | FIXME-04 | GuiGraphicsExtractor 渲染 | 仅视觉，不影响功能 |
| 4 | FIXME-06 | 多行文本 y 偏移 | 仅影响长错误信息显示 |
| 5 | TODO-01~03 | 触发器/验证 | 非阻塞，可延后 |

---

## 通用修复模式参考

### Transaction 模式（替代 setStackInSlot）

```java
// 已有工具方法（SlotType.java:143-155）
private static void replaceSlot(CombinedResourceHandler<ItemResource> handler, int index, ItemStack newStack) {
    try (Transaction tx = Transaction.open(null)) {
        ItemResource oldResource = handler.getResource(index);
        int oldAmount = (int) handler.getAmountAsLong(index);
        if (oldAmount > 0 && !oldResource.isEmpty()) {
            handler.extract(index, oldResource, oldAmount, tx);
        }
        if (!newStack.isEmpty()) {
            handler.insert(index, ItemResource.of(newStack), newStack.getCount(), tx);
        }
        tx.commit();
    }
}
```

### CombinedResourceHandler 适配 IItemHandler

当上游代码仍需 `IItemHandler` 接口时，可通过 `NeoForge.getAdapter()` 桥接：

```java
IItemHandler legacy = NeoForge.getAdapter(IItemHandler.class, handler);
// 注意：需要验证 CombinedResourceHandler 是否注册了 IItemHandler 适配器
```

---

*文档更新于 2026-06-02*

## 更新记录

| 日期 | 变更 |
|------|------|
| 2026-06-02 | FIXME-02 已修复；兼容模组 TODO 移至 integration 文档 |
| 2026-06-01 | FIXME-03 已修复；FIXME-05/06 行号修正；移除 TOP 兼容 TODO |
