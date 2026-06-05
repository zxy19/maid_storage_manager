# TODO/FIXME Inventory

> 核验日期: 2026-06-04
> 范围: `src/main/java` 中活跃 `.java` 与 `.java.disabled` 注释。
> 目标: NeoForge 26.1 + Touhou Little Maid 26.1。

---

## 概述

| 类型 | 数量 | 状态 |
|------|------|------|
| 活跃 FIXME issue | 3 组 / 6 条注释 | 其中 `SlotType` 影响功能，`VirtualDisplayEntity` 为已验证的残留注释，`CommonCraftScreen` 为显示问题 |
| 活跃 TODO issue | 4 条注释 | RequestList trigger、Create handler、ItemSelector trigger、CraftResultContext 验证 |
| disabled 文件中的 TODO | 2 条注释 | Create/Mekanism generator 内部待验证 |
| 旧文档中已失效条目 | 3 条 | VirtualDisplayEntityRender、RequestRetBehavior、SlotType.drawGold 的 FIXME 不再存在 |

---

## 活跃 FIXME

### FIXME-01: `SlotType` 的 ETA/背包 slot 写操作仍未完整迁移

| 属性 | 值 |
|------|-----|
| 文件 | `src/main/java/studio/fantasyit/maid_storage_manager/communicate/data/SlotType.java` |
| 行号 | 118, 121, 129, 205 |
| 优先级 | **CRITICAL** |
| 是否阻碍编译 | 否，相关逻辑仍被注释或退化处理 |
| 是否影响运行 | **是**，ETA slot 遍历和 `iterItemExceptSlotForMaid` 仍缺失 |

**当前状态:**

- `FLOWER` slot 已使用 `replaceSlot(CombinedResourceHandler<ItemResource>, int, ItemStack)`。
- `ETA` 的 sub-range 写操作仍没有恢复。
- `iterItemExceptSlotForMaid` 整段背包遍历写回逻辑仍注释。
- `replaceSlot()` 已用 `Transaction` 实现 extract + insert，但只覆盖完整 handler 的直接 slot 写入。

**建议修复方向:**

1. 明确 TLM 26.1 背包 slot 编号和 ETA slot 范围。
2. 避免构造不可写 sub-range wrapper，优先直接对完整 `CombinedResourceHandler` 的真实 slot index 调用 `replaceSlot()`。
3. 为 `iterItemExceptSlotForMaid` 恢复遍历逻辑，并逐个排除当前 slot 类型对应的真实 index。

---

### FIXME-02: `VirtualDisplayEntity` 中 `dropItem` 签名注释已过期

| 属性 | 值 |
|------|-----|
| 文件 | `src/main/java/studio/fantasyit/maid_storage_manager/entity/VirtualDisplayEntity.java` |
| 行号 | 31 |
| 优先级 | **LOW** |
| 是否阻碍编译 | 否 |
| 是否影响运行 | 未发现 |

**已验证:**

IDE 符号解析确认当前 MC 26.1 `ItemFrame.dropItem` 声明为:

```java
public void dropItem(ServerLevel level, @Nullable Entity causedBy)
```

当前代码 `this.dropItem(level, p_31776_.getEntity())` 与该签名匹配。`discard()` 也已替代旧 `kill()`。这条 FIXME 当前更像残留注释，后续改代码时可删除或改成更具体的说明。

---

### FIXME-03: `CommonCraftScreen` 多行错误文本 y 偏移未计算

| 属性 | 值 |
|------|-----|
| 文件 | `src/main/java/studio/fantasyit/maid_storage_manager/menu/craft/common/CommonCraftScreen.java` |
| 行号 | 566 |
| 优先级 | **LOW** |
| 是否阻碍编译 | 否 |
| 是否影响运行 | 是，长错误文案会多行重叠 |

**当前代码:**

```java
for (FormattedCharSequence line : font.split(..., 90)) {
    graphics.text(font, line, 0, 0, wordWrapColor);
    // FIXME: y offset needed for multiline
}
```

**建议修复方向:** 使用 `yOffset += font.lineHeight + 1/2` 或等价布局常量逐行递增。

---

## 活跃 TODO

### TODO-01: RequestListItem 绑定 Trigger

| 属性 | 值 |
|------|-----|
| 文件 | `src/main/java/studio/fantasyit/maid_storage_manager/items/RequestListItem.java` |
| 行号 | 111 |
| 优先级 | **LOW** |

`//TODO bind Trigger` 仍存在。旁边旧实现 `TourGuideTrigger.trigger(serverPlayer, "request_list_bind")` 也仍是注释状态。

---

### TODO-02: CreateStockManagerPacket handler 等待 Create

| 属性 | 值 |
|------|-----|
| 文件 | `src/main/java/studio/fantasyit/maid_storage_manager/network/Network.java` |
| 行号 | 296 |
| 优先级 | **MEDIUM** |

`CreateStockManagerPacket` 已注册，但 handler 体内只有 `// TODO wait create`，原 `StockManagerInteract` 调用仍注释。该 TODO 随 Create 集成恢复处理。

---

### TODO-03: ItemSelectorMenu 关闭 Trigger

| 属性 | 值 |
|------|-----|
| 文件 | `src/main/java/studio/fantasyit/maid_storage_manager/menu/request/ItemSelectorMenu.java` |
| 行号 | 340 |
| 优先级 | **LOW** |

`removed(Player)` 中仍有 `//TODO trigger`。需先确认目标是 Tour Guide、网络同步还是其他回调。

---

### TODO-04: CraftResultContext 验证 `setPlaceBefore()` 可行性

| 属性 | 值 |
|------|-----|
| 文件 | `src/main/java/studio/fantasyit/maid_storage_manager/craft/data/CraftResultContext.java` |
| 行号 | 47 |
| 优先级 | **LOW** |

当 `maxSlotConsume < currentMaxConsume` 且不是放置后的第一层时，代码仍注释掉 `layer.setPlaceBefore()`，并标注 `//TODO:验证可行性`。

---

## disabled 文件中的 TODO

| 文件 | 行号 | 内容 |
|------|------|------|
| `craft/generator/type/create/GeneratorCreate.java.disabled` | 174 | `//TODO 验证正确性` |
| `craft/generator/type/mekanism/GeneratorMekOsmiumComp.java.disabled` | 56 | `//? TODO: WHERE the 200 FROM?` |

---

## 旧文档中已失效的 FIXME

| 旧编号 | 当前核验结果 |
|--------|--------------|
| VirtualDisplayEntityRender item 渲染 | 已修复，文件活跃且未检出 FIXME |
| RequestRetBehavior `updateCollectedNotStored` API | 已修复，接口和调用均使用 `CombinedResourceHandler<ItemResource>` |
| SlotType `drawGold()` 的 `flush()/setColor()` | 当前源码无 FIXME 注释；`drawGold()` 仅调用 `icon.blit(...)`，金色叠加效果仍不存在但不属于活跃 TODO/FIXME 注释 |

---

## 修复优先级建议

| 排序 | 编号 | 简述 | 理由 |
|------|------|------|------|
| 1 | FIXME-01 | `SlotType` ETA/背包 slot 写操作 | 影响通信 slot 功能 |
| 2 | TODO-02 | CreateStockManagerPacket handler | 等 Create 集成恢复，当前 handler 空实现 |
| 3 | FIXME-03 | 多行文本 y 偏移 | UI 显示问题，低风险 |
| 4 | TODO-01/03/04 | Trigger 和合成策略验证 | 非阻塞 |
| 5 | FIXME-02 | 删除或改写残留注释 | 代码签名已验证正确 |

---

## 更新记录

| 日期 | 变更 |
|------|------|
| 2026-06-04 | 重新扫描 TODO/FIXME；修正行号；确认 `dropItem` 签名正确；新增 `Network.java` 的 Create TODO；移除旧失效 FIXME |
| 2026-06-02 | 旧版清单 |
