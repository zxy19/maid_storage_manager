# Touhou Little Maid 迁移文档

**版本跨度**: NeoForge 21.1 (MC 1.21.1, Java 21) → NeoForge 26.1 (MC 26.1, Java 25)

本目录包含从旧项目 (`D:\Minecraft\TouhouLittleMaid`) 到新项目 (`D:\Minecraft\_proj\TouhouLittleMaid-26.1`) 的完整 API 迁移文档。

---

## 版本命名重要说明

**Minecraft 在 NeoForge 26.1 引入了全新的版本号体系。**

旧的 `minecraft_version` 使用 `1.x.y` 格式（如 `1.21.1`）。自 NeoForge 26.1 起，Minecraft 与 NeoForge 统一版本号：**`minecraft_version=26.1.2` 是 Minecraft 的实际版本号**，而非映射关系。

| 旧版本命名 | 新版本命名 | 说明 |
|-----------|-----------|------|
| `1.21.1` | `26.1` | 主版本号 |
| `1.21.2` | `26.1.2` | 次版本号 |
| `NeoForge 21.1.x` | `NeoForge 26.1.x` | 平台版本 |

这意味着：
- **NeoForge 26.1 之于 21.1**，等同于过去「Minecraft 1.22 之于 1.21」的大版本迁移——有大量 API 破坏性变更
- `minecraft_version_range=[26.1.2]` 精确匹配单一版本，替代了旧的 `[1.21.1,1.21.2)` 开区间
- 本迁移文档中，所有「MC 1.21.1」的引用指旧版本，所有「MC 26.1」指新版本——**这是两个不同的 Minecraft 主版本**

---

## 文档索引

| 序号 | 文档 | 包范围 | 变更数 | 关键变更 |
|------|------|--------|--------|----------|
| 01 | [api/](./01-api.md) | `api/` 全部子包 | ~26 | 接口签名变更、移除 `IBackpackData`/`IMaid`/`ConvertMaidEvent` 等、新增 `LittleMaidExtension` |
| 02 | [entity/](./02-entity.md) | `entity/` 全部子包 | ~35+ | **重大重构**: EntityMaid → 15+ Maid*Manager 类拆分、数据存储 EntityDataAccessor → AttachmentType |
| 03 | [init/network/](./03-init-network.md) | `init/` + `network/` + 主类 | ~20+ | 网络协议重写 (→ CustomPacketPayload+STREAM_CODEC)、注册 API 变化、新增 InitBrains |
| 04 | [client/](./04-client.md) | `client/` 全部子包 | ~60+ | resource/ 包完全重组、JS 动画系统移除、渲染管线重写 (RendererState)、新增 particle/proxy/extensions |
| 05 | [compat/](./05-compat.md) | `compat/` 全部模块 | ~19 | 移除 emi/rei/top/ysm/tacz 兼容、新增 iris/extracontainer、多个兼容大幅简化 |
| 06 | [items/blocks/](./06-items-blocks-inventory.md) | `item/` + `block/` + `tileentity/` + `inventory/` + `crafting/` | ~57 | Item.Properties API 变化、移除 BoardState/Placeholder/TankBackpack 物品、容器重构 |
| 07 | [events/](./07-events.md) | `event/` + `api/event/` | ~22 | 移除 8 个事件（含 food/ 子包）、事件总线模式变化、Mojang 映射迁移 |
| 08 | [geckolib/](./08-geckolib-molang.md) | `geckolib3/` + `molang/` | ~36 | **重大重构**: Controller 架构重写、渲染管线预计算变换、MoLang Double→Float、新增声音系统 |
| 09 | [config/data/](./09-config-data-world.md) | `config/` + `data/` + `world/` + `datagen/` + `datapack/` + `advancements/` + `loot/` | ~11 | DataComponent API 变化、数据附件重构、移除 BoardState/ItemModel 数据生成器 |
| 10 | [utilities/](./10-utilities.md) | `command/` + `debug/` + `util/` + `mixin/` + `ai/` | ~6 | 权限 API 重命名、MixinPlugin 移除、新增 4 个 mixin、AI function-call 弃用代码移除 |

---

## 变色等级总结

### 架构级变更 (理解整个项目结构)

1. **EntityMaid 拆分** (02) — 单体类拆分为 15+ Manager 组件
2. **网络协议重写** (03) — Forge 网络 → CustomPacketPayload + STREAM_CODEC
3. **GeckoLib 渲染重写** (08) — Controller 单体→接口, 渲染 PosesStack→预计算矩阵
4. **Client resource/ 重组** (04) — 资源加载器完全重新组织
5. **MoLang 类型变更** (08) — Double → Float, 存储层重构

### 需要关注的移除内容

| 移除内容 | 所在文档 | 影响 |
|----------|----------|------|
| `api/backpack/IBackpackData.java` | 01 | 背包数据接口移除，需适配新 AttachmentType |
| `api/entity/IMaid.java` | 01 | 女仆接口移除，功能合并入其他位置 |
| `api/event/ConvertMaidEvent` | 01 | 女仆转换事件移除 |
| `api/event/MaidAfterEatEvent` | 01 | 食后事件移除 |
| `api/event/RegisterKubeJSEvent` | 01 | KubeJS 注册事件移除 |
| `compat/emi/`, `compat/rei/` | 05 | EMI/REI 配方查看兼容移除 |
| `compat/top/` | 05 | The One Probe 兼容移除 |
| `compat/ysm/` | 05 | YSM 模型兼容移除 |
| `event/food/` | 07 | 食物事件子包移除 |
| JS 动画系统 | 04 | CustomJsAnimationManger/script/ 移除 |
| `network/message/Ysm*Package` | 03 | YSM 网络包移除 |
| `item/ItemBoardState` 等 | 06 | 棋盘状态/占位符等物品移除 |
| `loot/RandomBoardStateFunction` | 09 | 棋盘随机战利品函数移除 |
| `datapack/BoardState*` | 09 | 棋盘状态数据包内容移除 |
| `mixin/plugin/MixinPlugin` | 10 | Mixin 插件移除 |

---

## 文件统计

| 文档 | 大小 |
|------|------|
| 01-api.md | 23.7 KB |
| 02-entity.md | 24.6 KB |
| 03-init-network.md | 21.2 KB |
| 04-client.md | 24.7 KB |
| 05-compat.md | 11.8 KB |
| 06-items-blocks-inventory.md | 26.5 KB |
| 07-events.md | 10.7 KB |
| 08-geckolib-molang.md | 25.0 KB |
| 09-config-data-world.md | 20.4 KB |
| 10-utilities.md | 10.7 KB |
| **总计** | **~199 KB** |

---

## 原版 Minecraft/NeoForge API 变化文档

以下文档专注于描述 **Minecraft 1.21.1 和 NeoForge 框架本身** 的 API 变化，而非本模组特有代码的变化。

| 序号 | 文档 | 覆盖范围 | 变更数 | 关键变更 |
|------|------|----------|--------|----------|
| 11 | [vanilla-core/](./11-vanilla-core.md) | 注册表、Identifier、Codec、框架基础 | ~27 | ResourceLocation→Identifier、RecordCodecBuilder mapCodec、Loot注册重构、@EventBusSubscriber简化 |
| 12 | [vanilla-items-entities/](./12-vanilla-items-entities.md) | 物品、方块、实体、数据、AI、配方 | ~65+ | Item.setId()、EntityDataAccessor→AttachmentType、MobSpawnType→EntitySpawnReason、Brain API重写、CompoundTag→ValueInput/ValueOutput |
| 13 | [vanilla-client-network/](./13-vanilla-client-network.md) | 网络、渲染、GUI、文本组件、输入 | 10 节 | CustomPacketPayload+StreamCodec网络重写、EntityRenderState系统、Component/MutableComponent变化、ClickEvent/HoverEvent签名变更、KeyMapping重构 |
| 14 | [gradle-config/](./14-gradle-config.md) | 构建系统完整配置 | 9 节 | JDK 21→25、Gradle 8→9.4.1、moddev 2.0.95→2.0.141、Shadow 8→9.4.1、Parchment移除、依赖大幅精简、mixins.json 重构 |
| 15 | [vanilla-screen-menu/](./15-vanilla-screen-menu.md) | Screen、Container、Slot、MenuType | ~70+ | render→extractRenderState渲染架构重写、GuiGraphics→GuiGraphicsExtractor、SlotItemHandler→ResourceHandlerSlot、stillValid重命名、openMenu参数变更 |

### 原版 API 核心迁移主题

| 主题 | 影响范围 | 文档参考 |
|------|----------|----------|
| `ResourceLocation` → `Identifier` | 全项目 | 11 |
| `RecordCodecBuilder.create()` → `.mapCodec()` | AttachmentType, Loot | 11, 09 |
| Loot: `getType()` → `codec()` 返回 MapCodec | 战利品条件/函数 | 11, 09 |
| `EntityDataAccessor` → `AttachmentType<T>` | 实体数据存储 | 12, 02 |
| `MobSpawnType` → `EntitySpawnReason` | 实体生成 | 12 |
| Brain/Schedule API 重构 | 实体 AI | 12 |
| `CompoundTag` → `ValueInput`/`ValueOutput` | 序列化 | 12 |
| `FriendlyByteBuf` → `RegistryFriendlyByteBuf` | 网络 | 13, 03 |
| 网络包: `SimpleChannel` → `CustomPacketPayload` + `StreamCodec` | 网络 | 13, 03 |
| `PoseStack` 操作 → 预计算矩阵 | 渲染 | 13, 08 |
| `EntityRenderState` 系统 | 实体/方块渲染器 | 13, 04 |
| `MutableComponent` 移除, `Component` 统一 | 文本 | 13 |
| `ClickEvent`/`HoverEvent` 构造器重写 | GUI/文本 | 13 |
| `@EventBusSubscriber` bus 参数移除 | 事件 | 11, 13 |
| `KeyMapping` 构造器重写 | 按键绑定 | 13 |
| `TooltipFlag` → `TooltipDisplay` | 物品提示 | 13, 06 |
| Item 需要 `.setId()` | 物品注册 | 12 |
| 方块/方块实体需要 `.setId()` | 方块注册 | 12 |
| `FMLEnvironment.production` → `.isProduction()` | 调试判断 | 11 |

---

## 文件统计

| 类型 | 文档数 | 总大小 |
|------|--------|--------|
| 模组包级迁移 | 10 份 | ~199 KB |
| 原版 API 迁移 | 4 份 | ~108 KB |
| 构建配置迁移 | 1 份 | ~12 KB |
| **总计** | **15 份** | **~319 KB** |

---

## 生成信息

- **生成时间**: 2026-05-31
- **方法**: 14 个并行 subagent + 直接分析，通过 IDEA MCP 读取并比较两个项目的源码和配置文件
- **旧项目根**: `D:\Minecraft\TouhouLittleMaid` (NeoForge 21.1 / MC 1.21.1)
- **新项目根**: `D:\Minecraft\_proj\TouhouLittleMaid-26.1` (NeoForge 26.1 / MC 26.1)
