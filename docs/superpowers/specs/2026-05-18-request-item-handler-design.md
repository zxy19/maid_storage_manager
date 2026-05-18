# Design: RequestItemHandler Capability & request_list 功能分离

## 动机
当前 `RequestListItem` 是一个约 613 行的单体类，所有请求任务管理逻辑作为静态方法内嵌其中。每个方法都硬检查 `is(ItemRegistry.REQUEST_LIST_ITEM.get())`，导致没有其他物品可以充当"请求列表"。需要将任务管理功能与特定物品解耦，使得：
1. `request_list` 本身通过 capability 暴露功能
2. 其他物品也可以实现同一 capability 来充当请求列表
3. 创建专用虚拟请求物品 `virtual_request_list`

## 架构

```
┌──────────────────────────────────────────────┐
│  IRequestTaskHandler (Capability 接口)         │
│  ├── getTasks / getTasksNotDone               │
│  ├── addCollected / updateCollected           │
│  ├── updateStored / updateCollectedNotStored  │
│  ├── markDone / markAllDone / setMissingItem  │
│  ├── clearProgress / clearNonSuccess          │
│  ├── 配置: matchType, ignore, cd, repeat,      │
│  │         stockMode                           │
│  ├── 存储目标: storageBlock/Entity              │
│  ├── 状态查询: isAllSuccess/Stored             │
│  └── getWorkUUID                              │
├──────────────────────────────────────────────┤
│  RequestTask (新 record)                      │
│  ── API 用任务快照                             │
├──────────────────────────────────────────────┤
│  DefaultRequestTaskHandler (独立类)            │
│  ── 迁入所有现有静态方法逻辑                    │
│  ── 操作 REQUEST_ITEMS 等数据组件              │
├──────────────────┬───────────────────────────┤
│  RequestListItem │  VirtualRequestListItem   │
│  implements      │  implements               │
│  IRequestTaskH.. │  IRequestTaskHandler      │
│  → uses Default  │  → uses Default           │
│  (有 GUI/Menu)   │  (无 GUI, 始终 virtual)    │
└──────────────────┴───────────────────────────┘
```

## 新增文件

| 文件 | 说明 |
|------|------|
| `api/IRequestTaskHandler.java` | Capability 接口 |
| `api/RequestTask.java` | API 用任务快照 record |
| `items/handler/DefaultRequestTaskHandler.java` | 独立实现类 |
| `items/VirtualRequestListItem.java` | 虚拟请求专用物品 |

## 修改文件

| 文件 | 改动 |
|------|------|
| `RequestListItem.java` | 删除所有静态方法，改为返回 `DefaultRequestTaskHandler` 的 `LazyOptional`；保留 UI/交互逻辑 |
| `Conditions.java` | `takingRequestList()` 改为 capability 查询 |
| `RequestItemUtil.java` | `makeVirtualItemStack()` 改用 `VirtualRequestListItem` |
| Maid AI Behaviors (~6个类) | `RequestListItem.xxx()` → `handler.xxx()` |
| `ItemSelectorMenu` | `save()` 内读写改为 capability |
| `CopyConfigRecipe / ListClearRecipe` | 改为 capability 调用 |
| `ItemRegistry.java` | 注册 `virtual_request_list` |
| `CapabilityRegistry.java`（新建） | 注册 `IRequestTaskHandler` capability |
| AI/JEI/WorkCard/Communicate 虚拟创建处 | 改用 `VirtualRequestListItem` |

## 复用策略

`DefaultRequestTaskHandler` 是无状态独立类：
- 不依赖任何特定物品类型
- 仅通过 `ItemStack` 参数读写数据组件
- `RequestListItem` 和 `VirtualRequestListItem` 共用同一实现实例
- 其他物品只要使用相同数据组件即可直接复用

## VirtualRequestListItem 与 RequestListItem 的区别
- 不实现 `MenuProvider`（无 GUI）
- 无合成配方、无创造模式栏位
- 仅作内部程序化创建使用

## virtualSource 处理
`virtualSource` 保持现有数据组件方案。`IRequestTaskHandler` 接口提供 `getVirtualSource(stack)` 和 `getVirtualData(stack)` 方法。使用方创建虚拟 ItemStack 时直接设置数据组件。`VirtualRequestListItem` 不耦合任何 source 语义。
