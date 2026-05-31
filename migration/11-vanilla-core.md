# Vanilla API Migration — Core Framework (NeoForge 21.1 / MC 1.21.1 → 1.21.1)

本文档记录 Minecraft/NeoForge 在 1.20.1 → 1.21.1 升级中涉及的核心框架变化：
`ResourceLocation` / `Identifier` 重命名、注册系统变更、Codec/MapCodec API、事件总线注释、配置系统等。

> **说明**：本项目的旧版（`D:\Minecraft\TouhouLittleMaid`）已经基于 NeoForge 21.1 / MC 1.21.1（而非 Forge），因此本文档比较的是 **NeoForge 21.1 / MC 1.21.1 → NeoForge 26.1** 的差异。大部分 `@Mod`、`IEventBus`、`DeferredRegister` 等模式在两个版本中保持一致。

---

## 1. ResourceLocation → Identifier

### 1.1 总结

MC 1.21.1 将 `ResourceLocation` 重命名为 `Identifier`。这是 Mojang 在从 Yarn 命名向 Mojang 官方命名迁移中的一部分。两个类的 API 完全兼容，仅有类名和方法名变化。

### 1.2 变更清单

| 旧 API (1.20.1) | 新 API (1.21.1) | 说明 |
|---|---|---|
| `net.minecraft.resources.ResourceLocation` | `net.minecraft.resources.Identifier` | 类名重命名 |
| `ResourceLocation.CODEC` | `Identifier.CODEC` | Codec 随类名变化 |
| `ResourceLocation.fromNamespaceAndPath(ns, path)` | `Identifier.fromNamespaceAndPath(ns, path)` | 工厂方法同名 |
| `ResourceKey.location()` | `ResourceKey.identifier()` | 返回类型仍是 Identifier/ResourceLocation |
| `dimension.location()` | `dimension.identifier()` | Dimension#location() 方法重命名 |
| `Level.NETHER.location()` | `Level.NETHER.identifier()` | 同上 |

### 1.3 影响范围

项目中 **332+ 处** `import net.minecraft.resources.ResourceLocation` 需改为 `import net.minecraft.resources.Identifier`，所有局部变量类型也同步修改。

迁移后的项目中 **不再有任何** 对 `ResourceLocation` 的引用。

---

## 2. Registry System

### 2.1 DeferredRegister 变化

| 事项 | 1.20.1 (NeoForge) | 1.21.1 (NeoForge) | 说明 |
|---|---|---|---|
| `DeferredRegister.create()` | `DeferredRegister.create(RegistryKey, modid)` | 不变 | 签名完全相同 |
| `DeferredRegister.createBlocks()` | `DeferredRegister.createBlocks(modid)` | 不变 | 简写不变 |
| `DeferredRegister.Blocks` | `DeferredRegister.Blocks` | 不变 | 类型别名不变 |
| `DeferredBlock<Block>` | `DeferredBlock<Block>` | 不变 | `DeferredBlock` 存在 |
| `DeferredItem<Item>` | `DeferredItem<Item>` | 不变 | `DeferredItem` 存在 |
| `DeferredHolder<T, V>` | `DeferredHolder<T, V>` | 不变 | `DeferredHolder` 存在 |
| `NeoForgeRegistries.Keys.ATTACHMENT_TYPES` | `NeoForgeRegistries.Keys.ATTACHMENT_TYPES` | 不变 | Keys 路径不变 |
| `NeoForgeRegistries.ENTITY_DATA_SERIALIZERS` | `NeoForgeRegistries.ENTITY_DATA_SERIALIZERS` | 不变 | 注册表 key 不变 |

### 2.2 注册表 Key 常量

`Registries` 类中的 key 常量本身没有变化：

```java
// 两个版本通用
Registries.ENTITY_TYPE
Registries.BLOCK_ENTITY_TYPE
Registries.BLOCK
Registries.ITEM
Registries.LOOT_CONDITION_TYPE
Registries.LOOT_FUNCTION_TYPE
Registries.RECIPE_TYPE
Registries.RECIPE_SERIALIZER
```

### 2.3 Loot 注册系统重大变化 — `LootItemConditionType` / `LootItemFunctionType` 移除

这是 **1.21.1 最大的注册系统变化之一**。`LootItemConditionType` 和 `LootItemFunctionType` 作为包装类型被移除，直接注册 `MapCodec`。

**旧 (1.20.1)：**
```java
// InitLootModifier.java
public static final DeferredRegister<LootItemConditionType> LOOT_CONDITION_TYPES =
        DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, MOD_ID);

public static final DeferredRegister<LootItemFunctionType<?>> LOOT_FUNCTION_TYPES =
        DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, MOD_ID);

public static final Supplier<LootItemConditionType> LOOT_TABLE_TYPE =
        LOOT_CONDITION_TYPES.register("loot_table_type",
                () -> new LootItemConditionType(LootTableTypeCondition.CODEC));

public static final Supplier<LootItemFunctionType<? extends LootItemConditionalFunction>> SET_INIT_MAID_OWNER_FUNCTION =
        LOOT_FUNCTION_TYPES.register("set_init_maid_owner",
                () -> new LootItemFunctionType<>(SetInitMaidOwnerFunction.CODEC));
```

**新 (1.21.1)：**
```java
// InitLootModifier.java
import com.mojang.serialization.MapCodec;

public static final DeferredRegister<MapCodec<? extends LootItemCondition>> LOOT_CONDITION_TYPES =
        DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, MOD_ID);

public static final DeferredRegister<MapCodec<? extends LootItemFunction>> LOOT_FUNCTION_TYPES =
        DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, MOD_ID);

public static final Supplier<MapCodec<? extends LootItemCondition>> LOOT_TABLE_TYPE =
        LOOT_CONDITION_TYPES.register("loot_table_type", () -> LootTableTypeCondition.CODEC);

public static final Supplier<MapCodec<? extends LootItemConditionalFunction>> SET_INIT_MAID_OWNER_FUNCTION =
        LOOT_FUNCTION_TYPES.register("set_init_maid_owner", () -> SetInitMaidOwnerFunction.CODEC);
```

**关键点**：
- `DeferredRegister<T>` 的类型参数从 `LootItemConditionType`/`LootItemFunctionType` 变为 `MapCodec<? extends ...>`
- 不再使用 `new LootItemConditionType(...)` / `new LootItemFunctionType(...)` 构造器
- 直接注册 `MapCodec` 实例
- 旧项目中 `SetTankCountFunction` 和 `RandomBoardStateFunction` 在迁移后被移除（不再需要）

### 2.4 IngredientType 移除

旧项目还有 `DeferredRegister<IngredientType<?>> INGREDIENT_TYPES`（使用 `NeoForgeRegistries.Keys.INGREDIENT_TYPES`），迁移后不再需要（`IngredientType` 注册被移除或内联）。

### 2.5 BlockEntityType 构造器变化

`BlockEntityType` 现在需要在构造时传入有效方块列表：

**旧 (1.20.1)：**
```java
public static Supplier<BlockEntityType<TileEntityAltar>> ALTAR_TE = TILE_ENTITIES.register("altar",
        () -> TileEntityAltar.TYPE);  // TYPE 是静态工厂
```

**新 (1.21.1)：**
```java
public static Supplier<BlockEntityType<TileEntityAltar>> ALTAR_TE = TILE_ENTITIES.register("altar",
        () -> new BlockEntityType<>(TileEntityAltar::new, ALTAR.get()));
```

需要手动调用 `new BlockEntityType<>(factory, validBlock)`。

### 2.6 注册表拆分为 InitBrains

旧项目中 `InitEntities` 包含了 MEMORY_MODULE_TYPES、SENSOR_TYPES、SCHEDULES、ACTIVITIES 等大脑/AI 相关的注册项。迁移后这些被提取到独立的 `InitBrains` 接口中。

### 2.7 Init 类的类类型变化

多个 Init 类从 `class` 改为 `interface`，以利用接口字段的 `public static final` 隐式修饰：

| 类 | 旧 | 新 |
|---|---|---|
| `InitEntities` | `public final class` | `public interface` |
| `InitDataAttachment` | `public class` | `public interface` |

`interface` 字段自动为 `public static final`，使得 `DeferredRegister` 注册声明更简洁。

---

## 3. getType() → codec() — LootItemCondition / LootItemFunction

### 3.1 总结

`LootItemCondition.getType()` → `LootItemCondition.codec()` 是配套 2.3 节变化的接口重构。

### 3.2 LootItemCondition 变化

**旧 (1.20.1)：**
```java
@Override
public LootItemConditionType getType() {
    return InitLootModifier.LOOT_TABLE_TYPE.get();
}
```

**新 (1.21.1)：**
```java
@Override
public MapCodec<? extends LootItemCondition> codec() {
    return CODEC;
}
```

### 3.3 LootItemConditionalFunction 变化

**旧 (1.20.1)：**
```java
@Override
public LootItemFunctionType<? extends LootItemConditionalFunction> getType() {
    return InitLootModifier.SET_INIT_MAID_OWNER_FUNCTION.get();
}
```

**新 (1.21.1)：**
```java
@Override
public MapCodec<? extends LootItemConditionalFunction> codec() {
    return CODEC;
}
```

### 3.4 RecipeSerializer 变化

`RecipeSerializer` 接口同样从 `getType()` 改为 `codec()`：

**旧 (1.20.1)：**
```java
// AltarRecipeSerializer.java
public MapCodec<AltarRecipe> codec() {
    return CODEC;
}
```

新项目中保持不变（该方法在 1.20.1 已经存在）。

---

## 4. Codec 与 MapCodec API

### 4.1 AttachmentType.Builder.serialize() — RecordCodecBuilder.create → RecordCodecBuilder.mapCodec

这是影响 `AttachmentType` 序列化的关键变化。

**旧 (1.20.1)：**
```java
// ChatTokensAttachment.java
public static final AttachmentType<ChatTokensAttachment> TYPE = AttachmentType.builder(...)
        .serialize(RecordCodecBuilder.create(ins -> ins.group(
                Codec.INT.fieldOf("num").forGetter(o -> o.num)
        ).apply(ins, ChatTokensAttachment::new)))
        .build();
```

**新 (1.21.1)：**
```java
// ChatTokensAttachment.java
public static final AttachmentType<ChatTokensAttachment> TYPE = AttachmentType
        .builder(() -> new ChatTokensAttachment(0))
        .serialize(RecordCodecBuilder.mapCodec(ins -> ins.group(
                Codec.INT.fieldOf("num").forGetter(o -> o.num)
        ).apply(ins, ChatTokensAttachment::new)))
        .build();
```

**差异**：
- `AttachmentType.Builder.serialize()` 的签名从接受 `Codec<A>` 变为接受 `MapCodec<A>`
- 因此必须将 `RecordCodecBuilder.create(...)` 改为 `RecordCodecBuilder.mapCodec(...)`
- `Codec<T>` → `MapCodec<T>` 的语义变化：MapCodec 用于需要字段名映射的序列化

**受影响文件**：
- `data/ChatTokensAttachment.java`
- `data/MaidNumAttachment.java`
- `data/PowerAttachment.java`

### 4.2 RecordCodecBuilder.create 的其他用途（不变）

`RecordCodecBuilder.create()` 在构造 `Codec<T>`（非 `MapCodec`）时保持不变。项目中大量 TTS/STT/LLM 配置类、advancement trigger、数据类的 `Codec<T>` 声明都继续使用 `RecordCodecBuilder.create()`。

```java
// 两个版本通用 - 用于生成 Codec<T>
public static final Codec<TrackInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        ...
).apply(instance, TrackInfo::new));
```

### 4.3 Block CODEC 模式

所有 Block 现在需要显式声明 `MapCodec` 字段并实现 `codec()` 方法：

```java
// 新 (1.21.1) - BlockGomoku.java
private static final MapCodec<BlockGomoku> CODEC = simpleCodec(BlockGomoku::new);

@Override
protected MapCodec<? extends BaseEntityBlock> codec() {
    return CODEC;
}
```

旧项目中部分 Block 在 inline 调用，新项目中统一将 `CODEC` 提取为独立的 `private static final` 字段。

### 4.4 MapCodec.unit 用于无状态类型

新增的 `MapCodec.unit()` 方法用于不需要配置参数的类型：

```java
// 新项目中的用法 (TileEntityItemStackChairRenderer.java)
public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(Unbaked::new);

@Override
public MapCodec<Unbaked> type() {
    return MAP_CODEC;
}
```

---

## 5. Mod Loading Framework

### 5.1 @Mod 注解 — 不变

```java
// 两个版本通用
@Mod(TouhouLittleMaid.MOD_ID)
public final class TouhouLittleMaid {
    public TouhouLittleMaid(IEventBus modEventBus, ModContainer modContainer) {
        ...
    }
}
```

导入路径 `net.neoforged.fml.common.Mod` 在两个版本中完全相同。

### 5.2 @EventBusSubscriber 变化

| 事项 | 旧 (1.20.1) | 新 (1.21.1) | 说明 |
|---|---|---|---|
| 导入路径 | `net.neoforged.fml.common.EventBusSubscriber` | 相同 | 不变 |
| MOD bus 显式声明 | `@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)` | `@EventBusSubscriber()` | MOD bus 现在是**默认值**，可省略 `bus` 参数 |
| GAME bus | `@EventBusSubscriber(bus = EventBusSubscriber.Bus.GAME, modid = ...)` | `@EventBusSubscriber(modid = ...)` | GAME bus 也被移除? 实际上项目中将 GAME bus 的事件也迁移为直接使用 @EventBusSubscriber(modid = ...) |

**具体变化示例**：

```java
// 旧 (1.20.1)
@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public final class CommonRegistry { ... }

// 新 (1.21.1)
@EventBusSubscriber()
public final class CommonRegistry { ... }
```

```java
// 旧 (1.20.1)
@EventBusSubscriber(modid = TouhouLittleMaid.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)

// 新 (1.21.1)
@EventBusSubscriber(modid = TouhouLittleMaid.MOD_ID)  // both sides, no Dist.CLIENT
```

client-only 事件现在通过 `@EventBusSubscriber(value = Dist.CLIENT)` 指定。

### 5.3 IEventBus — 不变

`IEventBus` 的使用模式保持不变：在 `@Mod` 构造函数中接收 `IEventBus modEventBus`，并传递给 `DeferredRegister.register(eventBus)`。

### 5.4 FMLEnvironment.production → FMLEnvironment.isProduction()

**旧 (1.20.1)：**
```java
public static boolean DEBUG = !FMLEnvironment.production;  // 字段访问
```

**新 (1.21.1)：**
```java
public static boolean DEBUG = !FMLEnvironment.isProduction();  // 方法调用
```

`production` 从 public 字段变为 private，通过 `isProduction()` getter 方法访问。

---

## 6. Loot 相关 API 变化

### 6.1 LootContext.getParamOrNull → getOptionalParameter

**旧 (1.20.1)：**
```java
Entity entity = context.getParamOrNull(LootContextParams.THIS_ENTITY);
```

**新 (1.21.1)：**
```java
Entity entity = context.getOptionalParameter(LootContextParams.THIS_ENTITY);
```

### 6.2 context.getResolver().get() 参数数变化

**旧 (1.20.1)：**
```java
context.getResolver().get(Registries.LOOT_TABLE, currentLootTable)
```

**新 (1.21.1)：**
```java
context.getResolver().get(currentLootTable)  // 1 参数
```

### 6.3 context.getQueriedLootTableId() 返回类型

返回类型从 `ResourceLocation` 变为 `Identifier`（同第 1 节）。

---

## 7. Item 构造器变化

所有 Item 子类现在需要一个 `Identifier` 参数并在 Properties 上调用 `.setId()`：

**旧 (1.20.1)：**
```java
public ItemCamera() {
    super(new Properties());
}
```

**新 (1.21.1)：**
```java
public ItemCamera(Identifier id) {
    super(new Properties().setId(ResourceKey.create(Registries.ITEM, id)));
}
```

这是 1.21.1 NeoForge 对注册集成的新要求。`ResourceKey.create(Registries.ITEM, id)` 将 Identifier 包装为 ResourceKey。

---

## 8. Config System — 不变

`ModConfig.Type.COMMON` / `ModConfig.Type.SERVER` 保持不变：

```java
// 两个版本通用
modContainer.registerConfig(ModConfig.Type.COMMON, GeneralConfig.getConfigSpec());
modContainer.registerConfig(ModConfig.Type.SERVER, ServerConfig.init());
```

---

## 9. 其他核心 API 变化

### 9.1 EntityAttributeCreationEvent 的移动

旧项目中 `EntityAttributeCreationEvent` 事件在 `InitEntities` 中处理：

```java
// 旧 (1.20.1) - InitEntities.java
@SubscribeEvent
public static void addEntityAttributeEvent(EntityAttributeCreationEvent event) {
    event.put(EntityMaid.TYPE, EntityMaid.createAttributes().build());
    event.put(EntityChair.TYPE, LivingEntity.createLivingAttributes().build());
    ...
}
```

新项目中此事件处理被移至 `InitAttribute` 类，与属性注册放在一起。

### 9.2 MemoryModuleType / SensorType / Schedule / Activity 拆分

如 2.6 节所述，AI 脑部系统注册从 `InitEntities` 拆分到独立的 `InitBrains` 接口。

### 9.3 不再需要的注册项

- `MAID_SCHEDULE_DATA_SERIALIZERS` — 旧项目中的 `MaidSchedule.DATA` 数据序列化器被移除
- `INGREDIENT_TYPES` — 不再需要注册 `IngredientType`

### 9.4 `builtInRegistryHolder().key().identifier()` 模式

新项目中使用 `builtInRegistryHolder().key().identifier()` 替代 `BuiltInRegistries.ENTITY_TYPE.getKey()`：

```java
// 新 (1.21.1) - ConditionalUse.java
Identifier registryName = itemInHand.getItem().builtInRegistryHolder().key().identifier();

// 对比旧 (1.20.1) - ConditionalUse.java
ResourceLocation registryName = BuiltInRegistries.ENTITY_TYPE.getKey(vehicle.getType());
```

`builtInRegistryHolder()` 返回 Holder 而非直接查 BuiltInRegistries，更类型安全。

---

## 10. 完整 Import 迁移对照表

| # | 旧 Import (1.20.1) | 新 Import (1.21.1) | 备注 |
|---|---|---|---|
| 1 | `net.minecraft.resources.ResourceLocation` | `net.minecraft.resources.Identifier` | 类重命名 |
| 2 | `net.minecraft.world.level.storage.loot.predicates.LootItemConditionType` | （移除） | 不再需要此类型 |
| 3 | `net.minecraft.world.level.storage.loot.functions.LootItemFunctionType` | （移除） | 不再需要此类型 |
| 4 | `com.mojang.serialization.MapCodec` | 不变 | 但使用频率大增 |
| 5 | `com.mojang.serialization.codecs.RecordCodecBuilder` | 不变 | serialize() 中用法改为 mapCodec |
| 6 | `net.neoforged.fml.common.Mod` | 不变 | - |
| 7 | `net.neoforged.fml.common.EventBusSubscriber` | 不变 | 但注解参数简化 |
| 8 | `net.neoforged.bus.api.IEventBus` | 不变 | - |
| 9 | `net.neoforged.bus.api.SubscribeEvent` | 不变 | - |
| 10 | `net.neoforged.fml.config.ModConfig` | 不变 | - |
| 11 | `net.neoforged.fml.ModContainer` | 不变 | - |
| 12 | `net.neoforged.fml.loading.FMLEnvironment` | 不变 | `production` 字段→方法 |
| 13 | `net.neoforged.neoforge.registries.DeferredRegister` | 不变 | - |
| 14 | `net.neoforged.neoforge.registries.DeferredBlock` | 不变 | - |
| 15 | `net.neoforged.neoforge.registries.DeferredItem` | 不变 | - |
| 16 | `net.neoforged.neoforge.registries.DeferredHolder` | 不变 | - |
| 17 | `net.neoforged.neoforge.registries.NeoForgeRegistries` | 不变 | - |
| 18 | `net.minecraft.core.registries.Registries` | 不变 | - |
| 19 | `net.minecraft.core.registries.BuiltInRegistries` | 不变 | - |
| 20 | `net.minecraft.resources.ResourceKey` | 不变 | `.location()` → `.identifier()` |
| 21 | `net.neoforged.neoforge.registries.RegisterEvent` | 不变 | - |
| 22 | `net.neoforged.neoforge.attachment.AttachmentType` | 不变 | serialize 签名变化 |
| 23 | `net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent` | 不变 | 处理器移到不同类 |
| 24 | `net.minecraft.world.level.storage.loot.predicates.LootItemCondition` | 不变 | `getType()` → `codec()` |
| 25 | `net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction` | 不变 | `getType()` → `codec()` |
| 26 | `net.neoforged.neoforge.registries.NeoForgeRegistries.Keys` | 不变 | - |
| 27 | `net.minecraft.world.level.storage.loot.functions.LootItemFunction` | 不变 | 仅在泛型参数中使用 |

---

## 11. 迁移检查清单

在迁移自定义代码时，请逐项检查：

- [ ] 所有 `ResourceLocation` → `Identifier`（import + 类型 + 工厂方法）
- [ ] 所有 `.location()` → `.identifier()`（ResourceKey、Dimension 等）
- [ ] `LootItemCondition.getType()` → `LootItemCondition.codec()`
- [ ] `LootItemConditionalFunction.getType()` → `LootItemConditionalFunction.codec()`
- [ ] `LootItemConditionType` / `LootItemFunctionType` 的 DeferredRegister 改为 `MapCodec<? extends ...>`
- [ ] 移除 `new LootItemConditionType(...)` / `new LootItemFunctionType(...)` 构造
- [ ] `AttachmentType.Builder.serialize()` 中的 `RecordCodecBuilder.create` → `RecordCodecBuilder.mapCodec`
- [ ] `FMLEnvironment.production` → `FMLEnvironment.isProduction()`
- [ ] `@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, ...)` → `@EventBusSubscriber(...)`
- [ ] `context.getParamOrNull(...)` → `context.getOptionalParameter(...)`
- [ ] `context.getResolver().get(Registries.X, key)` → `context.getResolver().get(key)`
- [ ] Block 子类添加 `MapCodec` 字段和 `codec()` 方法
- [ ] Item 子类构造函数接受 `Identifier` 并调用 `.setId()`
- [ ] `BlockEntityType` 构造传入有效方块
