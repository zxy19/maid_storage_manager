# Init, Network & Main Classes Migration

## 全局 API 变化

在对比具体类之前，有几个贯穿全局的 API 变更：

| 变更项 | 旧版 (OLD) | 新版 (NEW) |
|--------|-----------|-----------|
| 资源标识符 | `ResourceLocation` | `Identifier` |
| 从命名空间创建 | `ResourceLocation.fromNamespaceAndPath(ns, path)` | `Identifier.fromNamespaceAndPath(ns, path)` |
| 环境检测 | `FMLEnvironment.production` / `FMLEnvironment.dist` | `FMLEnvironment.isProduction()` / `FMLEnvironment.getDist()` |
| 附魔书创建 | `EnchantedBookItem.createForEnchantment()` | `EnchantmentHelper.createBook()` |
| 部分类声明 | `class` | `interface`（InitEntities, InitAttribute, InitBrains, InitRecipes, InitDataAttachment） |

---

## Registration System Changes

### DeferredRegister 创建方式统一

两个版本的注册系统都使用 NeoForge 的 `DeferredRegister`，但创建 API 有变化：

```
OLD: DeferredRegister.create(Registries.XXX, MOD_ID)
NEW: DeferredRegister.create(Registries.XXX, MOD_ID)  — 大部分不变

OLD 块: DeferredRegister.createBlocks(MOD_ID)
NEW 块: DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID)  — 同

OLD 物品: DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID)
NEW 物品: 同上，但物品构造必须显式设置 ResourceKey

OLD DataComponents: DeferredRegister.createDataComponents(MOD_ID)
NEW DataComponents: DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MOD_ID)
```

### 关键注册表变更

#### 1. `InitBrains.java` — **全新文件**

将大脑/行为相关注册从 `InitEntities` 中分离出来：

```java
// OLD: 在 InitEntities 中
MEMORY_MODULE_TYPES, SENSOR_TYPES, SCHEDULES, ACTIVITIES

// NEW: 在 InitBrains 中（独立文件）
MEMORY_MODULE_TYPES, SENSOR_TYPES, ENVIRONMENT_ATTRIBUTES, ACTIVITIES
```

**重大变化**: `SCHEDULES` 被移除，替换为 Minecraft 1.21.1 新的 `ENVIRONMENT_ATTRIBUTES` 系统：

```
OLD: Schedule + ScheduleBuilder          → 3 个 schedule (day_shift/night_shift/all_day)
NEW: EnvironmentAttribute<Activity>      → 3 个 environment_attribute (gameplay/maid_*_activity)
```

ENVIRONMENT_ATTRIBUTES 是 1.21.1 新增的注册表类型 `Registries.ENVIRONMENT_ATTRIBUTE`，用于控制生物在不同环境条件下的行为活动。

#### 2. `InitEntities.java` — 拆分

| 属性 | OLD | NEW |
|------|-----|-----|
| 类型 | `class` | `interface` |
| @EventBusSubscriber | `bus = EventBusSubscriber.Bus.MOD` | `modid = TouhouLittleMaid.MOD_ID` |
| 注册表 | ENTITY_TYPES, MEMORY_MODULE_TYPES, SENSOR_TYPES, SCHEDULES, DATA_SERIALIZERS, ACTIVITIES | ENTITY_TYPES, DATA_SERIALIZERS |
| 事件处理 | `addEntityAttributeEvent` + `addEntitySpawnPlacement` | 仅 `addEntitySpawnPlacement` |

- 大脑相关注册(MEMORY_MODULE_TYPES, SENSOR_TYPES, ACTIVITIES) 移至 `InitBrains`
- SCHEDULES 替换为 `InitBrains.ENVIRONMENT_ATTRIBUTES`
- `addEntityAttributeEvent` 移至 `InitAttribute`

#### 3. `InitAttribute.java` — 扩展

OLD 仅包含属性定义和 `newAttribute()` 工厂方法。NEW 新增：

- `@EventBusSubscriber(modid = ...)` 注解
- `addEntityAttributeEvent()` — 实体属性创建事件处理器（从 InitEntities 迁移过来）
- `createMaidAttributes()` 和 `createFairyAttributes()` — 属性构建器（从 InitEntities 迁移过来）

#### 4. `InitItems.java` — 物品注册 API 重大变更

**物品构造必须设置 `ResourceKey`**（1.21.1 新要求）：

```
OLD: () -> new BlockItem(InitBlocks.GOMOKU.get(), new Item.Properties())
NEW: id -> new BlockItem(InitBlocks.GOMOKU.get(), new Item.Properties()
     .setId(ResourceKey.create(Registries.ITEM, id)))
```

被移除的物品：
- `CRAFTING_TABLE_BACKPACK` / `ENDER_CHEST_BACKPACK` / `FURNACE_BACKPACK` / `TANK_BACKPACK` — 特殊背包
- `MONSTER_LIST` — 怪物列表工具
- `ENTITY_PLACEHOLDER` — 被注释掉
- `GOMOKU_BOARD_STATE` / `CCHESS_BOARD_STATE` / `WCHESS_BOARD_STATE` — 棋盘状态物品
- `MEMORIZABLE_GENSOKYO_LOCATION` — Patchouli 集成（被注释掉）

刷怪蛋 API 变更：
```
OLD: () -> new DeferredSpawnEggItem(() -> EntityMaid.TYPE, 0xffffff, 0xffffff, new Item.Properties())
NEW: id -> new SpawnEggItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))
     .component(DataComponents.ENTITY_DATA, TypedEntityData.of(EntityMaid.TYPE, new CompoundTag())))
```

使用了 Mojang 原版 `SpawnEggItem` 替代 NeoForge 的 `DeferredSpawnEggItem`。

Bauble 物品构造变更：
```
OLD: () -> new ItemDamageableBauble(6)
NEW: id -> new ItemDamageableBauble(id, 6)  — 需要传入 ResourceLocation id
```

#### 5. `InitRecipes.java` — 注册表结构变更

```
OLD: RECIPE_SERIALIZERS, RECIPE_TYPES, INGREDIENT_TYPES (NeoForge)
NEW: RECIPE_BOOK_CATEGORIES, RECIPE_SERIALIZERS, RECIPE_TYPES
```

- `INGREDIENT_TYPES` (`NeoForgeRegistries.Keys.INGREDIENT_TYPES`) 被移除
- 新增 `RECIPE_BOOK_CATEGORIES` (`Registries.RECIPE_BOOK_CATEGORY`) — 1.21.1 新增注册表

#### 6. `InitCapabilities.java` — 物品传输 API 迁移

NeoForge 物品传输 API 从 `IItemHandler` 迁移到 `ResourceHandler<ItemResource>`：

```
OLD: EntityCapability<IItemHandler, @Nullable Direction>
     EntityCapability.createSided(loc, IItemHandler.class)
     Capabilities.ItemHandler.ENTITY

NEW: EntityCapability<ResourceHandler<ItemResource>, @Nullable Direction>
     EntityCapability.createSided(loc, ResourceHandler.asClass())
     Capabilities.Item.ENTITY
```

#### 7. `InitDataAttachment.java` — 数据附着点扩展

OLD 仅含 3 个玩家相关的 AttachmentType：
- `MAID_NUM`, `POWER_NUM`, `CHAT_TOKENS`

NEW 新增 6 个女仆实体相关的 AttachmentType（从旧 TaskData 机制迁移）：
- `PROFILE` — 模型和声音包 ID
- `STATS` — 饥饿值、好感度、经验、雷击状态
- `TASK` — 工作模式相关
- `ANIMATION` — 动画状态
- `CONFIG` — 女仆行为配置
- `BACKPACK` — 背包类型
- `GAME` — 对弈记录

类型声明也从 `class` 改为 `interface`。

#### 8. `InitDataComponent.java` — 数据组件变更

```
OLD: FILTER_LIST_TAG  → DataComponentType<CompoundTag>
NEW: FILTER_LIST_TAG  → DataComponentType<List<ItemStack>>

OLD: 有 TANK_BACKPACK_TAG (CompoundTag)
NEW: 移除 TANK_BACKPACK_TAG

OLD: 有 BOARD_STATE_TAG (ItemBoardState.BoardStateInfo)
NEW: 移除 BOARD_STATE_TAG

OLD: 无 INIT_MAID_OWNER_TAG
NEW: 新增 INIT_MAID_OWNER_TAG (UUID)
```

#### 9. `InitLootModifier.java` — Loot 表类型变更

```
OLD: DeferredRegister<LootItemConditionType>         — 包装类型
     DeferredRegister<LootItemFunctionType<?>>
     注册: () -> new LootItemConditionType(codec)    — 需要 new 包装对象
     3 个 function: SET_TANK_COUNT, BOARD_STATE_RANDOMLY, SET_INIT_MAID_OWNER

NEW: DeferredRegister<MapCodec<? extends LootItemCondition>>  — 直接使用 MapCodec
     DeferredRegister<MapCodec<? extends LootItemFunction>>
     注册: () -> codec                                        — 直接传入 codec
     2 个: LOOT_TABLE_TYPE, SET_INIT_MAID_OWNER_FUNCTION
```

移除了 `SET_TANK_COUNT_FUNCTION` 和 `BOARD_STATE_RANDOMLY_FUNCTION`（功能合并或移除）。

#### 10. `InitDamage.java` — Registry API 变化

```
OLD: Registry<DamageType> damageTypes = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE);
     damageTypes.getHolderOrThrow(key)

NEW: var damageTypes = level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE);
     damageTypes.getOrThrow(key)
```

#### 11. `InitContainer.java` — 容器类型移除

移除了 4 个特殊背包容器（与物品移除对应）：
- `MAID_CRAFTING_TABLE_BACKPACK_CONTAINER`
- `MAID_ENDER_CHEST_CONTAINER`
- `MAID_FURNACE_CONTAINER`
- `MAID_TANK_CONTAINER`

#### 12. `InitCreativeTabs.java` — 创造模式标签页

- 移除了 Patchouli 集成 (`ItemModBook.forBook()`)
- 移除了特殊背包物品展示（与 InitItems 一致）
- 移除了棋盘状态物品展示
- `FMLEnvironment.dist` → `FMLEnvironment.getDist()`
- `EnchantedBookItem.createForEnchantment()` → `EnchantmentHelper.createBook()`

#### 13. `InitSounds.java` — 新增音效

新增一个音效条目：
- `GECKO_CUSTOM` — 用于 GeckoLib 模型的自定义音效

#### 14. `InitTaskData.java` — **被移除**

OLD 中有独立的 `InitTaskData` 类，用于注册 TaskData（`ATTACK_LIST`）。NEW 中此文件不存在，TaskData 概念被重新实现为 DataAttachment 系统（见 `InitDataAttachment` 的扩展）。

#### 15. `InitBlocks.java` — BlockEntity 构造方式变化

```
OLD: () -> TileEntityXxx.TYPE                          — 引用静态字段
NEW: () -> new BlockEntityType<>(TileEntityXxx::new, block.get())  — 内联构造
```

BlockEntity 类不再通过静态 `TYPE` 字段暴露类型，而是在注册点内联构造。

#### 16. 无变更的类

以下类在两个版本间完全一致（仅可能的导入路径变化无关）：
- `InitCommand.java`
- `InitPoi.java`
- `InitTrigger.java`

---

## Registry Subpackage Changes

### `CommonRegistry.java`

| 变更 | OLD | NEW |
|------|-----|-----|
| EventBusSubscriber | `@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)` | `@EventBusSubscriber()` |
| YSM 集成 | `event.enqueueWork(YsmCompat::init)` | 移除 |
| TaskData | `TaskDataRegister.init()` | 移除（合并到 DataAttachment） |

### `DatapackSyncEvent.java` — **全新文件**

NEW 新增此文件，用于在数据包同步时发送自定义配方数据：
```java
@EventBusSubscriber(modid = TouhouLittleMaid.MOD_ID)
public class DatapackSyncEvent {
    @SubscribeEvent
    public static void onDatapackSyncEvent(OnDatapackSyncEvent event) {
        event.sendRecipes(InitRecipes.ALTAR_RECIPE.get());
    }
}
```

---

## Network Stack Migration

### 关键发现：网络栈已预先现代化

**两个版本的 NetworkHandler.java 几乎完全相同**。两者都使用 NeoForge 1.21+ 的现代网络 API：
- `RegisterPayloadHandlersEvent` 事件
- `PayloadRegistrar` 注册器
- `CustomPacketPayload` 接口
- `STREAM_CODEC` 序列化

这意味着 OLD 项目实际上已经是一个较近的版本，网络栈的迁移在之前已经完成。

### 数据包差异

**OLD 独有的包**（YSM 集成）：
- `YsmMaidModelPackage` — 向服务端发送 YSM 模型数据
- `SyncYsmMaidDataPackage` — 向客户端同步 YSM 模型数据

**NEW 独有变量**：
- 无新增包类型

### `network/client/` 子包 — **全新架构**

NEW 新增了 `network/client/` 子包，包含 19 个客户端代理类（Proxy），用于处理客户端接收到的数据包。这些代理将客户端逻辑从消息类中分离出来：

| Proxy 类 | 对应消息包 |
|----------|-----------|
| `BeaconAbsorbPackageProxy` | `BeaconAbsorbPackage` |
| `CChessToClientPackageProxy` | `CChessToClientPackage` |
| `CheckSchedulePosPacketProxy` | `CheckSchedulePosPacket` |
| `CuriosS2CUpdatePacketProxy` | `CuriosS2CUpdatePacket` |
| `FoxScrollPackageProxy` | `FoxScrollPackage` |
| `GomokuClientPackageProxy` | `GomokuClientPackage` |
| `ItemBreakPackageProxy` | `ItemBreakPackage` |
| `MaidAnimationPackageProxy` | `MaidAnimationPackage` |
| `OpenBeaconGuiPackageProxy` | `OpenBeaconGuiPackage` |
| `OpenChairGuiPackageProxy` | `OpenChairGuiPackage` |
| `OpenPlayerInventoryPackageProxy` | `OpenPlayerInventoryPackage` |
| `OpenSwitcherGuiPackageProxy` | `OpenSwitcherGuiPackage` |
| `PlayMaidSoundAtPosPackageProxy` | `PlayMaidSoundAtPosPackage` |
| `PlayMaidSoundPackageProxy` | `PlayMaidSoundPackage` |
| `SendEffectPackageProxy` | `SendEffectPackage` |
| `SpawnParticlePackageProxy` | `SpawnParticlePackage` |
| `SyncBaublePackageProxy` | `SyncBaublePackage` |
| `SyncDataPackageProxy` | `SyncDataPackage` |
| `SyncMaidAreaPackageProxy` | `SyncMaidAreaPackage` |
| `WChessToClientPackageProxy` | `WChessToClientPackage` |

### 消息类细节变更（以 MaidModelPackage 为例）

```
OLD record: MaidModelPackage(int id, ResourceLocation modelId) implements CustomPacketPayload
NEW record: MaidModelPackage(int id, Identifier modelId) implements CustomPacketPayload

OLD handle(): 调用 maid.setIsYsmModel(false); — YSM 状态清除
NEW handle(): 无 YSM 相关调用

OLD: @NotNull 注解在 type() 方法上
NEW: 无 @NotNull 注解
```

所有消息类的 `ResourceLocation` 参数都已替换为 `Identifier`。

---

## Main Mod Class Changes

### `TouhouLittleMaid.java`

**几乎完全相同**，主要差异在 `initRegister()` 方法中注册顺序和内容的变化：

```
FMLEnvironment.production        → FMLEnvironment.isProduction()     （API 变化）

initRegister() 注册顺序和条目：

OLD:
  InitEntities.ENTITY_TYPES         → 第一个注册
  InitAttribute.ATTRIBUTES          → 第二个
  InitEntities.MEMORY_MODULE_TYPES  → 在 InitEntities 中
  InitEntities.SENSOR_TYPES
  InitEntities.SCHEDULES            → 在 InitEntities 中
  InitEntities.DATA_SERIALIZERS
  InitEntities.ACTIVITIES
  ...
  InitRecipes.INGREDIENT_TYPES      → 存在

NEW:
  InitAttribute.ATTRIBUTES          → 第一个注册
  InitEntities.ENTITY_TYPES         → 第二个
  InitEntities.DATA_SERIALIZERS     → ENTITY_TYPES 紧挨 DATA_SERIALIZERS
  InitBrains.MEMORY_MODULE_TYPES    → 移至 InitBrains
  InitBrains.SENSOR_TYPES
  InitBrains.ENVIRONMENT_ATTRIBUTES → 替换 SCHEDULES
  InitBrains.ACTIVITIES
  ...
  InitRecipes.RECIPE_BOOK_CATEGORIES → 替换 INGREDIENT_TYPES
```

### `TouhouLittleMaidClient.java`

**完全一致**，无任何实质性变化。

---

## Registry Inventory

### 完整注册表对比表

| DeferredRegister 字段 | 所属类 (OLD) | 所属类 (NEW) | 状态 |
|----------------------|-------------|-------------|------|
| `BLOCKS` | InitBlocks | InitBlocks | 不变 |
| `TILE_ENTITIES` | InitBlocks | InitBlocks | 不变（构造方式变化） |
| `ITEMS` | InitItems | InitItems | 不变（注册 API 变化） |
| `ENTITY_TYPES` | InitEntities | InitEntities | 不变 |
| `DATA_SERIALIZERS` | InitEntities | InitEntities | 不变 |
| `MEMORY_MODULE_TYPES` | InitEntities | **InitBrains** | 迁移 |
| `SENSOR_TYPES` | InitEntities | **InitBrains** | 迁移 |
| `SCHEDULES` | InitEntities | — | **移除** |
| `ENVIRONMENT_ATTRIBUTES` | — | **InitBrains** | **新增** |
| `ACTIVITIES` | InitEntities | **InitBrains** | 迁移 |
| `ATTACHMENT_TYPES` | InitDataAttachment | InitDataAttachment | 不变（条目扩展） |
| `DATA_COMPONENTS` | InitDataComponent | InitDataComponent | 不变（条目变化） |
| `SOUNDS` | InitSounds | InitSounds | 不变（条目变化） |
| `CONTAINER_TYPE` | InitContainer | InitContainer | 不变（条目减少） |
| `RECIPE_SERIALIZERS` | InitRecipes | InitRecipes | 不变 |
| `RECIPE_TYPES` | InitRecipes | InitRecipes | 不变 |
| `INGREDIENT_TYPES` | InitRecipes | — | **移除** |
| `RECIPE_BOOK_CATEGORIES` | — | InitRecipes | **新增** |
| `ARGUMENT_TYPE` | InitCommand | InitCommand | 不变 |
| `POI_TYPES` | InitPoi | InitPoi | 不变 |
| `TRIGGERS` | InitTrigger | InitTrigger | 不变 |
| `LOOT_CONDITION_TYPES` | InitLootModifier | InitLootModifier | 不变（类型变化） |
| `LOOT_FUNCTION_TYPES` | InitLootModifier | InitLootModifier | 不变（类型变化） |
| `TABS` | InitCreativeTabs | InitCreativeTabs | 不变（条目减少） |
| `ATTRIBUTES` | InitAttribute | InitAttribute | 不变 |
| `HAND_ITEM` | InitCapabilities | InitCapabilities | 不变（类型变化） |
| `ARMOR_ITEM` | InitCapabilities | InitCapabilities | 不变（类型变化） |

### 总结

- **移除**: `SCHEDULES`, `INGREDIENT_TYPES`
- **新增**: `ENVIRONMENT_ATTRIBUTES`, `RECIPE_BOOK_CATEGORIES`
- **迁移**: 4 个注册表从 InitEntities 移至 InitBrains
- **文件移除**: `InitTaskData.java`

---

## Packet/Payload Inventory

### 网络消息包对照表

| 消息类 | 方向 (OLD/NEW) | 状态 |
|--------|---------------|------|
| `MaidModelPackage` | ToServer / ToServer | 不变 |
| `ChairModelPackage` | ToServer / ToServer | 不变 |
| `OpenChairGuiPackage` | ToClient / ToClient | 不变 |
| `MaidConfigPackage` | ToServer / ToServer | 不变 |
| `MaidTaskPackage` | ToServer / ToServer | 不变 |
| `SendNameTagPackage` | ToServer / ToServer | 不变 |
| `ItemBreakPackage` | ToClient / ToClient | 不变 |
| `SpawnParticlePackage` | ToClient / ToClient | 不变 |
| `SyncDataPackage` | ToClient / ToClient | 不变 |
| `WirelessIOGuiPackage` | ToServer / ToServer | 不变 |
| `WirelessIOSlotConfigPackage` | ToServer / ToServer | 不变 |
| `OpenBeaconGuiPackage` | ToClient / ToClient | 不变 |
| `SetBeaconPotionPackage` | ToServer / ToServer | 不变 |
| `StorageAndTakePowerPackage` | ToServer / ToServer | 不变 |
| `SetBeaconOverflowPackage` | ToServer / ToServer | 不变 |
| `BeaconAbsorbPackage` | ToClient / ToClient | 不变 |
| `OpenSwitcherGuiPackage` | ToClient / ToClient | 不变 |
| `SaveSwitcherDataPackage` | ToServer / ToServer | 不变 |
| `ToggleTabPackage` | ToServer / ToServer | 不变 |
| `RequestEffectPackage` | ToServer / ToServer | 不变 |
| `SendEffectPackage` | ToClient / ToClient | 不变 |
| `PlayMaidSoundPackage` | ToClient / ToClient | 不变 |
| `PlayMaidSoundAtPosPackage` | ToClient / ToClient | 不变 |
| `SetMaidSoundIdPackage` | ToServer / ToServer | 不变 |
| `GomokuClientPackage` | ToClient / ToClient | 不变 |
| `GomokuServerPackage` | ToServer / ToServer | 不变 |
| `FoxScrollPackage` | ToClient / ToClient | 不变 |
| `SetScrollPackage` | ToServer / ToServer | 不变 |
| `CheckSchedulePosPacket` | ToClient / ToClient | 不变 |
| `SyncMaidAreaPackage` | ToClient / ToClient | 不变 |
| `ServantBellSetPackage` | ToServer / ToServer | 不变 |
| `SetAttackListPackage` | ToServer / ToServer | 不变 |
| `RefreshMaidBrainPackage` | ToServer / ToServer | 不变 |
| `MaidSubConfigPackage` | ToServer / ToServer | 不变 |
| `CChessToClientPackage` | ToClient / ToClient | 不变 |
| `CChessToServerPackage` | ToServer / ToServer | 不变 |
| `WChessToClientPackage` | ToClient / ToClient | 不变 |
| `WChessToServerPackage` | ToServer / ToServer | 不变 |
| `SendUserChatPackage` | ToServer / ToServer | 不变 |
| `TTSAudioToClientPackage` | ToClient / ToClient | 不变 |
| `TTSSystemAudioToClientPackage` | ToClient / ToClient | 不变 |
| `SaveMaidAIDataPackage` | ToServer / ToServer | 不变 |
| `ClearMaidAIDataPacket` | ToServer / ToServer | 不变 |
| `OpenMaidGuiPackage` | ToServer / ToServer | 不变 |
| `OpenPlayerInventoryPackage` | ToClient / ToClient | 不变 |
| `DismountPackage` | ToServer / ToServer | 不变 |
| `MaidAnimationPackage` | ToClient / ToClient | 不变 |
| `SyncBaublePackage` | ToClient / ToClient | 不变 |
| `CuriosS2CUpdatePacket` | ToClient / ToClient | 不变 |
| `OpenMaidAIChatPacket` | ToServer / ToServer | 不变 |
| `SyncMaidAIDataPacket` | ToClient / ToClient | 不变 |
| `OpenAIConfigPacket` | ToServer / ToServer | 不变 |
| `SyncAISitesPacket` | ToClient / ToClient | 不变 |
| `SaveLLMSitePacket` | ToServer / ToServer | 不变 |
| `SaveTTSSitePacket` | ToServer / ToServer | 不变 |
| `YsmMaidModelPackage` | ToServer / — | **移除** |
| `SyncYsmMaidDataPackage` | ToClient / — | **移除** |

**总数**: OLD 61 个消息类，NEW 59 个消息类（移除 2 个 YSM 相关包）。

### 客户端代理类（NEW 新增）

所有代理类位于 `network/client/` 包中，共 20 个代理类（含 `client/ai/` 空目录占位）：

| 代理类 | 用途 |
|--------|------|
| `BeaconAbsorbPackageProxy` | 信标吸收数据包客户端处理 |
| `CChessToClientPackageProxy` | 中国象棋客户端数据包处理 |
| `CheckSchedulePosPacketProxy` | 日程位置检查数据包处理 |
| `CuriosS2CUpdatePacketProxy` | Curios 饰品同步更新处理 |
| `FoxScrollPackageProxy` | 狐狸卷轴数据包处理 |
| `GomokuClientPackageProxy` | 五子棋客户端数据包处理 |
| `ItemBreakPackageProxy` | 物品损坏特效处理 |
| `MaidAnimationPackageProxy` | 女仆动画同步处理 |
| `OpenBeaconGuiPackageProxy` | 打开信标 GUI 处理 |
| `OpenChairGuiPackageProxy` | 打开座椅 GUI 处理 |
| `OpenPlayerInventoryPackageProxy` | 打开玩家背包处理 |
| `OpenSwitcherGuiPackageProxy` | 打开模型切换 GUI 处理 |
| `PlayMaidSoundAtPosPackageProxy` | 位置音效播放处理 |
| `PlayMaidSoundPackageProxy` | 女仆音效播放处理 |
| `SendEffectPackageProxy` | 效果发送处理 |
| `SpawnParticlePackageProxy` | 粒子生成处理 |
| `SyncBaublePackageProxy` | 饰品同步处理 |
| `SyncDataPackageProxy` | 数据同步处理（P 点、女仆数量） |
| `SyncMaidAreaPackageProxy` | 女仆工作区域同步处理 |
| `WChessToClientPackageProxy` | 国际象棋客户端数据包处理 |

---

> 迁移分析完成。共发现 **2 个新增类**（InitBrains, DatapackSyncEvent）、**1 个移除类**（InitTaskData）、**2 个移除的注册表**（SCHEDULES, INGREDIENT_TYPES）、**2 个新增的注册表**（ENVIRONMENT_ATTRIBUTES, RECIPE_BOOK_CATEGORIES）、**2 个移除的消息包**（YsmMaidModelPackage, SyncYsmMaidDataPackage）以及 **20 个新增的客户端代理类**。
