# 1.7.0更新内容一览
该版本需要1.3.0及以上版本的车万女仆才能运行。
## 新特性

+ 添加了`物流指南`，用于在两个指定点之间搬运物品，或者循环执行某个合成配方
+ `请求列表`支持绑定任意实体作为返回位置，其将会被扔给目标实体。如果右键女仆的时候没有绑定，则会默认绑定主人。
+ 添加了`物品存放规则`设置，调整以更改物品存放速度和准确性之间的策略。
+ 添加了`允许访问`，可以快速标记一个任意容器为可访问的。
+ 支持了TLM 1.3.x 新特性AI函数调用，支持了仓库查询、请求物品和切换协同模式三个函数

## 优化

+ 重写帕秋莉手册（Yuia的恩情还不完）
+ 提高了女仆存物的速度
+ 现在`过滤器`，`禁止访问`，`允许访问`可以直接Shift右键贴到容器上，其会自动生成一个物品展示框
+ 重写了`禁止访问`，`允许访问`，`存储定义`相关逻辑，现在存储定义可以覆盖前二者的逻辑了
+ 优化了女仆存放物品的移动判断，解决了卡顿问题
+ 请求列表重复选项现在支持切换单位了
+ `存储更新旗`，`存储定义`的操作逻辑现在与其他标记物品一致了。
+ 添加了交互提示
+ `存储更新旗`现在可以主动标记`禁止访问`的存储并让女仆忘记这个存储

## 修复

+ 修复了部分场景下穿墙点击箱子的问题
+ 修复了AE提取物品可能不全的问题
+ 修复大量寻路性能问题
+ 修复了部分场景下无法正确识别悬挂的`禁止访问`，`允许访问`的问题


Version 1.7.0 Update Notes
This version requires Touhou Maid version 1.3.0 or higher to run.

New Features
Added Logistics Guide for transporting items between two specified points or cyclically executing crafting recipes.

Request List now supports binding any entity as a return location. Items will be thrown to the target entity. If no entity is bound when right-clicking the maid, it will default to the owner.

Added Item Storage Rules settings to adjust strategies for balancing storage speed and accuracy.

Added Allow Access to quickly mark any container as accessible.

Added support for TLM 1.3.x's new AI function call features, including warehouse queries, item requests, and switching Cowork Mode.

Optimizations
Rewrote the Patchouli Handbook (Thanks to Yuia's effort).

Improved the maid's item storage speed.

Filters, No Access, and Allow Access can now be Shift-right-clicked onto containers to automatically generate an item frame.

Overhauled No Access, Allow Access, and Storage Define logic. Storage definitions can now override the previous two mechanics.

Optimized the maid's item storage pathfinding to resolve lag issues.

Duplicate entries in the Request List now support unit switching.

Change Flag and Storage Define operations now align with other marker item behaviors.

Added interaction hints.

Change Flag can now actively mark No Access storages, forcing the maid to forget them.

Bug Fixes
Fixed issues with clicking on chests through walls in certain scenarios.

Fixed incomplete item extraction from Applied Energistics (AE) systems.

Resolved multiple pathfinding performance issues.

Fixed incorrect recognition of hanging No Access/Allow Access markers in specific situations.