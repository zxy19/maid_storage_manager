# Event System Migration

## Removed Events

### `event/food/` 子包（完整移除）

| 文件 | 说明 |
|------|------|
| `event/food/ConvertFoodEatenEvent.java` | 监听 `MaidAfterEatEvent`，当食物有转化产物（`usingConvertsTo`）时自动放入女仆背包或掉落在地 |
| `event/food/RemainFoodEatenEvent.java` | 监听 `MaidAfterEatEvent`，当食物有容器（如碗、桶）时自动返还，通过 `MAID_EATEN_RETURN_CONTAINER_LIST` 配置匹配 |

> 这两类逻辑在 1.21.1 中已被移除或整合到别处。

### `api/event/` 移除的事件

| 文件 | 说明 | 替代方案 |
|------|------|----------|
| `AddTopInfoEvent.java` | The One Probe 集成事件，用于在 TOP 信息面板中添加自定义女仆信息 | TOP 可能不再兼容 1.21.1，改用 Jade |
| `ConvertMaidEvent.java` | 允许外部模组将任意 `Mob` 转换为 `IMaid` 实例以使用女仆渲染 | 如需要需自行实现 |
| `MaidAfterEatEvent.java` | 女仆吃完食物后触发，`event/food/` 子包的两个处理器订阅了此事件 | 随 food 子包一并移除 |
| `RegisterKubeJSEvent.java` | KubeJS 事件注册辅助，允许附属模组将自己的事件注册到 KubeJS `MaidEvents` 组 | KubeJS 兼容暂未移植 |

### `api/event/client/` 移除的事件

| 文件 | 说明 | 替代方案 |
|------|------|----------|
| `RenderMaidEvent.java` | 允许外部模组自定义女仆渲染（`IMaid` + `MaidModels.ModelData`），可取消默认渲染 | 1.21.1 中未提供等效替代 |

---

## New Events

### `event/` 新增

| 文件 | 说明 |
|------|------|
| `event/ClientTickEvent.java` | 客户端 tick 计数器 + 刷新率跟踪。`@EventBusSubscriber(value = Dist.CLIENT)` 订阅 `ClientTickEvent.Pre`，提供 `getTickCount()` 和 `getRefreshRate()` |

### `api/event/` 新增

无新增 API 事件。所有 1.21.1 中的 API 事件在 1.20.1 中均已存在。

---

## Changed Event Signatures

### 1. `event/CopyEntityIdEvent.java`

| 项目 | OLD (1.20.1) | NEW (1.21.1) |
|------|-------------|-------------|
| ResourceLocation 类名 | `net.minecraft.resources.ResourceLocation` | `net.minecraft.resources.Identifier` |
| Dist 检查方式 | `FMLEnvironment.dist == Dist.CLIENT` (字段) | `FMLEnvironment.getDist() == Dist.CLIENT` (方法) |
| isClientSide | `player.level.isClientSide` (字段) | `player.level.isClientSide()` (方法) |
| @OnlyIn 注解 | `@OnlyIn(Dist.CLIENT)` 在方法上 | 已移除，改用运行时 `FMLEnvironment.getDist()` 判断 |

### 2. `event/ClientExtensionsEvent.java`

| 项目 | OLD (1.20.1) | NEW (1.21.1) |
|------|-------------|-------------|
| @EventBusSubscriber | `bus = EventBusSubscriber.Bus.MOD` 显式指定 | 无需指定 bus（默认即 MOD bus） |
| 注册扩展数 | 7 个（Altar, GarageKit, Statue, Chair, EntityPlaceholder, GarageKit item, PicnicBasket） | 仅 1 个（Altar 的 BlockAltarExtensions） |
| 扩展来源 | 各 block/item 类的 `static final` 字段 | 独立 `BlockAltarExtensions` 类 |

> 大幅简化：GarageKit、Statue、Chair 等物品/方块的 client extensions 在 1.21.1 中不再需要单独注册。

### 3. `event/EntityDeathEvent.java`

| 项目 | OLD (1.20.1) | NEW (1.21.1) |
|------|-------------|-------------|
| KeepInventory 读取 | `newEntity.level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)` | `serverLevel.getGameRules().get(GameRules.KEEP_INVENTORY)` |
| GameRules 引用 | `GameRules.RULE_KEEPINVENTORY` (常量字段) | `GameRules.KEEP_INVENTORY` (Key 类型) |
| ServerLevel 检查 | 无 | `level instanceof ServerLevel serverLevel` 模式匹配 |

> NeoForge 26.1 使用新的 `GameRules.Key<T>` 泛型系统替代旧常量。

### 4. `event/EntityHurtEvent.java`

| 项目 | OLD (1.20.1) | NEW (1.21.1) |
|------|-------------|-------------|
| 同主人检查 | `tameable.getOwnerUUID() != null && tameable.getOwnerUUID().equals(thrower.getOwnerUUID())` | `tameable.isOwnedBy(owner)` (需先获取 `thrower.getOwner()`) |
| ResourceLocation | `net.minecraft.resources.ResourceLocation` | `net.minecraft.resources.Identifier` |

### 5. `event/EntityJoinWorldEvent.java`

| 项目 | OLD (1.20.1) | NEW (1.21.1) |
|------|-------------|-------------|
| MaidTemptGoal 构造 | 显式 cast `PathfinderMob pathfinderMob = (PathfinderMob) temptGoal.mob` | 直接使用 `temptGoal.mob`（`TemptGoal` 的 `mob` 字段现在是 `PathfinderMob` 类型） |

### 6. `event/MaidMealRegConfigEvent.java`

| 项目 | OLD (1.20.1) | NEW (1.21.1) |
|------|-------------|-------------|
| @EventBusSubscriber | `bus = EventBusSubscriber.Bus.MOD` 显式指定 | `@EventBusSubscriber()` 无 bus 参数 |

### 7. `event/MaidTrackEvent.java`

| 项目 | OLD (1.20.1) | NEW (1.21.1) |
|------|-------------|-------------|
| YSM 同步 | 额外同步 `SyncYsmMaidDataPackage`（`maid.isYsmModel()` 判断） | 已移除，仅同步饰品数据 |

---

## Event Handler Changes（`event/maid/` 子包）

### 8. `event/maid/SaddleMaidEvent.java`

| 项目 | OLD (1.20.1) | NEW (1.21.1) |
|------|-------------|-------------|
| Dist 检查 | `FMLLoader.getDist() == Dist.CLIENT` + `@OnlyIn(Dist.CLIENT)` | `FMLEnvironment.getDist() == Dist.CLIENT`，无 @OnlyIn |
| showTips() | 调用 `minecraft.getNarrator().sayNow(component)` | 无 narrator 调用 |

### 9. `event/maid/ApplyPotionEffectEvent.java`

| 项目 | OLD (1.20.1) | NEW (1.21.1) |
|------|-------------|-------------|
| 物品给予 | `ItemHandlerHelper.giveItemToPlayer(player, ...)` | `player.getInventory().placeItemBackInInventory(...)` |
| 牛奶清效果 | `maid.removeAllEffects()` | `maid.removeEffectsCuredBy(EffectCures.MILK)` |
| 声音引用 | `SoundEvents.GENERIC_DRINK` (直接字段) | `SoundEvents.GENERIC_DRINK.value()` (Holder 解包) |
| Random 访问 | `world.random` (字段) | `world.getRandom()` (方法) |

### 10. `event/maid/SwitchSittingEvent.java`

| 项目 | OLD (1.20.1) | NEW (1.21.1) |
|------|-------------|-------------|
| Home 检查 | `maid.hasRestriction()` | `maid.hasHome()` |
| 位置获取 | `maid.getRestrictCenter()` | `maid.getHomePosition()` |
| Random 访问 | `world.random` | `world.getRandom()` |

### 11. `event/maid/HandleBackpackEvent.java`

| 项目 | OLD (1.20.1) | NEW (1.21.1) |
|------|-------------|-------------|
| Cooldown | `player.getCooldowns().addCooldown(stack.getItem(), 20)` | `player.getCooldowns().addCooldown(stack, 20)` |
| 物品返还 | `ItemHandlerHelper.giveItemToPlayer(player, ...)` | `player.getInventory().placeItemBackInInventory(...)` |
| 声音引用 | `SoundEvents.HORSE_SADDLE` | `SoundEvents.HORSE_SADDLE.value()` |

### 12. `event/maid/UseNameTagEvent.java`（重大改造）

| 项目 | OLD (1.20.1) | NEW (1.21.1) |
|------|-------------|-------------|
| 架构 | 单类处理 client + server，通过 `@OnlyIn` 区分 | 仅处理 server，client 逻辑移除（改由客户端 open gui 包？） |
| @EventBusSubscriber | `@EventBusSubscriber`（无参数） | `@EventBusSubscriber(modid = ..., value = Dist.DEDICATED_SERVER)` |
| client handler | `onInteractClient` with `NameTagGui` 打开 GUI | 已移除 |

### 13. `event/maid/MaidAreaClickEvent.java`

| 项目 | OLD (1.20.1) | NEW (1.21.1) |
|------|-------------|-------------|
| isClientSide | `maid.level.isClientSide` (字段) | `maid.level.isClientSide()` (方法) |

### 14. `event/maid/MaidMountEvent.java`

| 项目 | OLD (1.20.1) | NEW (1.21.1) |
|------|-------------|-------------|
| Boat 类名 | `net.minecraft.world.entity.vehicle.Boat` | `net.minecraft.world.entity.vehicle.boat.Boat` |

---

## API Event Changes

### `api/event/client/MaidContainerGuiEvent.java`

| 项目 | OLD (1.20.1) | NEW (1.21.1) |
|------|-------------|-------------|
| GuiGraphics 类 | `net.minecraft.client.gui.GuiGraphics` | `net.minecraft.client.gui.GuiGraphicsExtractor` |

> Render 和 Tooltip 内部类中 `getGraphics()` 返回类型从 `GuiGraphics` 变为 `GuiGraphicsExtractor`。

### `api/event/client/DefaultGeckoAnimationEvent.java`

| 项目 | OLD (1.20.1) | NEW (1.21.1) |
|------|-------------|-------------|
| ResourceLocation | `net.minecraft.resources.ResourceLocation` | `net.minecraft.resources.Identifier` |
| addAnimation 方法 | 调用 `mergeAnimationFile(stream, animationFile)` 静态方法 | 改为 `GeckoContainerBuilder.getAnimationFile(stream).animations()` 内联实现 |
| 导入 | `static com.github.tartaricacid.touhoulittlemaid.client.resource.GeckoModelLoader.mergeAnimationFile` | 导入 `com.github.tartaricacid.touhoulittlemaid.client.resource.bedrock.GeckoContainerBuilder` |

---

## Event Bus Pattern Changes

### Forge (1.20.1) vs NeoForge (1.21.1)

| 特性 | OLD (1.20.1) | NEW (1.21.1) |
|------|-------------|-------------|
| 注解 | `@EventBusSubscriber` (net.neoforged.fml.common) | 相同 |
| bus 参数 | 部分类需显式指定 `bus = EventBusSubscriber.Bus.MOD` | 多数情况可省略，框架自动推断 |
| @Mod.EventBusSubscriber | 不使用 | 不使用 |
| 手动注册 IEventBus | `NeoForge.EVENT_BUS.addListener(...)` | 相同 |
| 主类构造参数 | `ModContainer modContainer, IEventBus modEventBus` | `IEventBus modEventBus, ModContainer modContainer`（顺序颠倒） |
| FMLEnvironment | `FMLEnvironment.production` (字段) | `FMLEnvironment.isProduction()` (方法) |
| FMLEnvironment | `FMLEnvironment.dist` (字段) | `FMLEnvironment.getDist()` (方法) |
| @OnlyIn | 普遍使用，如 `@OnlyIn(Dist.CLIENT)` | 大部分已移除，改用运行时判断或 `@EventBusSubscriber(value = Dist.XXX)` |

### 通用 Mojang Mapping 变化

以下变化影响所有事件类（非特定于某一文件）：

| 1.20.1 (MCP) | 1.21.1 (Mojang) |
|-------------|-----------------|
| `ResourceLocation` | `Identifier` |
| `FMLEnvironment.dist == Dist.CLIENT` | `FMLEnvironment.getDist() == Dist.CLIENT` |
| `FMLLoader.getDist()` | `FMLEnvironment.getDist()` |
| `world.random` (字段) | `world.getRandom()` (方法) |
| `level.isClientSide` (字段) | `level.isClientSide()` (方法) |
| `SoundEvents.GENERIC_DRINK` (字段) | `SoundEvents.GENERIC_DRINK.value()` (Holder 解包) |
| `GameRules.RULE_KEEPINVENTORY` (常量) | `GameRules.KEEP_INVENTORY` (Key<Boolean>) |
| `Boat` (旧包) | `boat.Boat` (子包重组织) |
| `ItemHandlerHelper.giveItemToPlayer()` | `player.getInventory().placeItemBackInInventory()` |
| `player.getCooldowns().addCooldown(item, ...)` | `player.getCooldowns().addCooldown(stack, ...)` |
| `removeAllEffects()` | `removeEffectsCuredBy(EffectCures.MILK)` |

---

## Summary

| 类别 | 数量 |
|------|------|
| 移除的 event/ 类 | 3（含 food/ 子包 2 个） |
| 移除的 api/event/ 类 | 5 |
| 新增的 event/ 类 | 1 |
| 新增的 api/event/ 类 | 0 |
| 有签名变化的事件类 | 14 |
