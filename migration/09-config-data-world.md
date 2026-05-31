# Config, Data, World, Datagen & Loot Migration

## Configuration Changes

### Package: `src/main/java/com/github/tartaricacid/touhoulittlemaid/config/`

| File | OLD | NEW | Change |
|------|-----|-----|--------|
| `GeneralConfig.java` | 6 subconfigs | 5 subconfigs | `VanillaConfig.init()` removed |
| `ServerConfig.java` | 4 config values | 4 config values | Identical (no changes) |
| `subconfig/VanillaConfig.java` | Present | **REMOVED** | Entire file removed |

#### VanillaConfig.java — Removed
The `VanillaConfig` subconfig class managed 4 vanilla-replacement options:
- `REPLACE_SLIME_MODEL` — Replace vanilla slime model with yukkuri (default: true)
- `REPLACE_XP_TEXTURE` — Replace XP orb texture with point items (default: true)
- `REPLACE_TOTEM_TEXTURE` — Replace totem texture with life point (default: true)
- `REPLACE_XP_BOTTLE_TEXTURE` — Replace bottle o' enchanting texture (default: true)

**Migration**: These config options no longer exist. Any code referencing `VanillaConfig.REPLACE_*` or the translation key prefix `config.touhou_little_maid.vanilla` needs to be removed or re-routed. The visual replacement features must have been refactored into a different mechanism (likely removed or made unconditional/always-on).

#### GeneralConfig.java — VanillaConfig.init() Removed
```diff
- VanillaConfig.init(builder);  // REMOVED
```

**NeoForge config API**: The `ModConfigSpec.Builder` API is largely unchanged for 1.21.1. No structural NeoForge config system migration was needed for these config classes.

#### ServerConfig — Unchanged
The 4 `ServerConfig` values remain identical:
- `CLIENT_PACK_DOWNLOAD_URLS` — List\<String\>
- `MAID_AI_TIME_DEBUG` — boolean
- `MAID_BACKUP_INTERVAL_SECONDS` — int (default: 180)
- `MAID_BACKUP_MAX_COUNT` — int (default: 3)

---

## Data Attachment Changes

### Package: `src/main/java/com/github/tartaricacid/touhoulittlemaid/data/`

All 3 attachment files (`ChatTokensAttachment`, `MaidNumAttachment`, `PowerAttachment`) underwent the same **serialization API change**:

| Change | OLD | NEW |
|--------|-----|-----|
| Codec builder | `RecordCodecBuilder.create(...)` | `RecordCodecBuilder.mapCodec(...)` |
| Formatting | Single-line `.serialize(...)` | Multi-line `.serialize(...)` |

#### Example — ChatTokensAttachment.java
```diff
  public static final AttachmentType<ChatTokensAttachment> TYPE = AttachmentType
      .builder(() -> new ChatTokensAttachment(0))
-     .serialize(RecordCodecBuilder.create(ins -> ins.group(Codec.INT.fieldOf("num")
-             .forGetter(o -> o.num)).apply(ins, ChatTokensAttachment::new))).build();
+     .serialize(RecordCodecBuilder.mapCodec(ins -> ins.group(
+             Codec.INT.fieldOf("num").forGetter(o -> o.num)
+     ).apply(ins, ChatTokensAttachment::new))).build();
```

**Why**: NeoForge 26.1 `AttachmentType.Builder.serialize()` now takes `MapCodec<A>` instead of `Codec<A>`. This is a significant API change — all attachment serializers must use `RecordCodecBuilder.mapCodec()` or wrap with `Codec#fieldOf()` to produce a `MapCodec`.

Same change applies to `MaidNumAttachment.TYPE` and `PowerAttachment.TYPE`.

**No other logic changes** — field types, method signatures, and business logic are identical.

---

## World Data Changes

### Package: `src/main/java/com/github/tartaricacid/touhoulittlemaid/world/`

All 3 files have **major API migrations**. File count is identical (no additions/removals).

#### MaidInfo.java — Class → Record with Codec

| Aspect | OLD | NEW |
|--------|-----|-----|
| Type | Regular `class` with getters | Java `record` |
| Serialization | Manual NBT via `MaidWorldData.load/save` | Built-in `Codec<MaidInfo>` fields |
| New Codecs | N/A | `MAID_INFO_CODEC`, `TOMBS_STONE_CODEC` |

**OLD** (class with 6 getter methods like `getDimension()`, `getEntityId()`, etc.)
**NEW** (record with `Codec`-based serialization using `UUIDUtil.CODEC`, `ComponentSerialization.CODEC`, `BlockPos.CODEC`)

The `TOMBS_STONE_CODEC` differs from `MAID_INFO_CODEC` in its field name: `"MaidId"` vs `"TombstoneId"` for the entity identifier.

---

#### MaidWorldData.java — SavedData.Factory → SavedDataType

| Change | OLD (1.20.1) | NEW (1.21.1) |
|--------|-------------|-------------|
| Type identifier | `String IDENTIFIER = "touhou_little_maid_world_data"` | `Identifier IDENTIFIER = Identifier.fromNamespaceAndPath(...)` |
| Factory method | `SavedData.Factory<>()` with `load()` | `SavedDataType<>()` with `CODEC` |
| Serialization | Manual `save(CompoundTag)` / `load(CompoundTag)` | Codec-based `CODEC` field |
| `getOwnerId()` | `maid.getOwnerUUID()` (returns UUID) | `maid.getOwner()` returns `LivingEntity`, then `.getUUID()` |

**OLD Serialization Pattern**:
```java
public static SavedData.Factory<MaidWorldData> factory() {
    return new SavedData.Factory<>(MaidWorldData::new, MaidWorldData::load, DataFixTypes.ENTITY_CHUNK);
}
// Manual save(CompoundTag) / load(CompoundTag) with getAllKeys(), getCompound(), getList()
```

**NEW Serialization Pattern**:
```java
public static SavedDataType<MaidWorldData> factory() {
    return new SavedDataType<>(IDENTIFIER, MaidWorldData::new, CODEC, DataFixTypes.ENTITY_CHUNK);
}
// CODEC = RecordCodecBuilder with unboundedMap of UUID-to-List<MaidInfo>
```

**Other API changes**:
- `dimension.location()` → `dimension.identifier()` (returns `Identifier` not `String`)
- `context.getResolver().get(Registries.LOOT_TABLE, key)` → `context.getResolver().get(key)` (1 param)
- Field name `INITIAL_VALUE` removed from `SavedData.dirty` tracking — now explicit `dirty` in CODEC
- `getAllKeys()` → `keySet()` in CompoundTag API

**Critical**: The `MaidWorldData` no longer has manual `save/load` methods. All persistence is handled by the `CODEC` via `SavedDataType`. Any custom NBT field changes must go through the CODEC.

---

#### MaidBackupsManager.java — NBT → TagValueOutput

| Change | OLD | NEW |
|--------|-----|-----|
| Logger | `TouhouLittleMaid.LOGGER` | `LogUtils.getLogger()` (instance field) |
| Entity save | `maid.saveAsPassenger(CompoundTag)` | `maid.saveAsPassenger(TagValueOutput)` |
| Component serialization | `Component.Serializer.toJson/fromJson` | `ComponentSerialization.CODEC` via Codec |
| Owner reference | `maid.getOwnerUUID()` | `maid.getOwnerReference()` → `ownerReference.getUUID()` |
| Index data type | `CompoundTag` | `TagValueOutput` (wraps CompoundTag via Codec) |
| IndexData record | No CODEC | Added `IndexData.CODEC` with `ComponentSerialization.CODEC`, `BlockPos.CODEC` |
| `getAllKeys()` | Used | Changed to `keySet()` |
| `getCompound(s)` | Used | Changed to `getCompoundOrEmpty(s)` |
| Data folder | `dataFolder.toPath()` | `dataFolder` (already Path type) |
| Server access | `player.getServer()` | `player.level().getServer()` |

**IndexData Codec** (NEW):
```java
public static final Codec<IndexData> CODEC = RecordCodecBuilder.create(ins -> ins.group(
    ComponentSerialization.CODEC.fieldOf("Name").forGetter(IndexData::name),
    BlockPos.CODEC.fieldOf("Pos").forGetter(IndexData::pos),
    Codec.STRING.fieldOf("Dimension").forGetter(IndexData::dimension),
    Codec.LONG.fieldOf("Timestamp").forGetter(IndexData::timestamp)
).apply(ins, IndexData::new));
```

**BackupData record also changed**: `CompoundTag entityData` / `CompoundTag indexData` → `TagValueOutput entityDataOutput` / `TagValueOutput indexDataOutput`

---

## Datagen Changes

### Package: `src/main/java/com/github/tartaricacid/touhoulittlemaid/datagen/`

#### Files Summary

| File | OLD | NEW | Action |
|------|-----|-----|--------|
| `ItemModelGenerator.java` | Present | **REMOVED** | Item model generation removed |
| `tag/TagRecipeSerializer.java` | Present | **REMOVED** | Create compat tag removed |
| `DamageTypeProvider.java` | Absent | **NEW** | Damage type datagen |
| `TimelinesProvider.java` | Absent | **NEW** | Schedule timeline datagen |
| `tag/TagTimeline.java` | Absent | **NEW** | Timeline tag provider |

---

#### DataGenerator.java — Complete Restructure

**Event subscription**:
```diff
- @EventBusSubscriber(modid = ..., bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
+ @EventBusSubscriber(modid = ...)  // both sides, no Dist.CLIENT
```

**Event type**:
```diff
- public static void gatherData(GatherDataEvent event)
+ public static void gatherData(GatherDataEvent.Client event)  // Client-only sub-event
```

**Provider registration pattern changes**:
| Old Pattern | New Pattern |
|------------|-------------|
| `event.getVanillaPack(true)` then `vanillaPack.addProvider(...)` | `generator.addProvider(true, ...)` |
| `event.includeServer()` / `event.includeClient()` | Hardcoded `true` (all providers server+client now) |
| Separate `TagBlock` → `TagItem` chain via `contentsGetter()` | `event.createBlockAndItemTags((output,lookup) -> ..., (output,lookup,blockTags) -> ...)` |
| `vanillaPack.addProvider(packOutput -> new RecipeGenerator(...))` | `event.createProvider(RecipeGenerator.Runner::new)` |

**Tags reorganization**:
- `TagRecipeSerializer` (OLD) — removed entirely
- `TagEnchantment` (OLD, had `ExistingFileHelper` param) → (NEW, uses `datapackProvider.getRegistryProvider()`)
- `TagDamage` (OLD, had `ExistingFileHelper` param) → (NEW, no `ExistingFileHelper`, uses `datapackProvider.getRegistryProvider()`)
- `TagPaintingVariant` (OLD, had `ExistingFileHelper` param) → (NEW, no `ExistingFileHelper`, uses `datapackProvider.getRegistryProvider()`)
- `TagTimeline` (NEW) — tags the maid's schedule timeline as `TimelineTags.UNIVERSAL`

**Removed providers from DataGenerator**:
- `ItemModelGenerator` — Was registered as `includeClient()`, now removed
- `TagRecipeSerializer` — Was registered as `includeServer()`, now removed

**New providers added to DataGenerator**:
- `TagTimeline` — Registered with `generator.addProvider(true, ...)`

---

#### DamageTypeProvider.java — NEW

Registers two damage types for the mod:
- `DANMAKU` (`touhou_little_maid.danmaku`) — Damage from danmaku projectiles
- `DANMAKU_ENDER_KILLER` (`touhou_little_maid.danmaku_ender_killer`) — Danmaku effective against end entities

Both use `DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER` with 0.1f exhaustion and `DamageEffects.HURT`.

---

#### TimelinesProvider.java — NEW

Defines a `MAID_SCHEDULE` timeline with 3 tracks using the overworld clock (24000 tick period):
1. **Day Shift** — WORK(0-12000), IDLE(12000-16000), REST(16000-24000)
2. **Night Shift** — REST(0-8000), IDLE(8000-12000), WORK(12000-24000)
3. **All Day** — WORK(0-24000)

This replaces whatever schedule system was used pre-1.21.1 and leverages Minecraft's new `Timeline` datapack registry.

---

#### ItemModelGenerator — REMOVED

Generated handheld/tool models for items like `hakurei_gohei`, `sanae_gohei`, `extinguisher`, `camera`, `maid_beacon`, `snack_cabinet` (with separate "in_hand" perspective models) and basic item models for `owner_conversion_tool` and board state items.

**Migration**: Item models now likely generated via JSON assets directly or a different mechanism.

---

#### TagRecipeSerializer — REMOVED

This tag provider added the mod's altar recipe serializer to Create's `automation_ignore` recipe serializer tag.

**Migration**: This Create compatibility tag is no longer generated by datagen. If still needed, the tag must be provided through a different mechanism (resource JSON, datapack, or added back).

---

## Datapack Changes

### Package: `src/main/java/com/github/tartaricacid/touhoulittlemaid/datapack/`

| File | OLD | NEW | Change |
|------|-----|-----|--------|
| `BoardStateData.java` | Present | **REMOVED** | Board state data management |
| `pojo/BoardStateRecord.java` | Present | **REMOVED** | Board state record POJO |
| `resources/BoardStateDataReloadListener.java` | Present | **REMOVED** | Board state reload listener |
| `KaomojiData.java` | Present | Present | **Identical** |
| `resources/KaomojiDataReloadListener.java` | Present | Present | `ResourceLocation` → `Identifier` |
| `resources/SkillsDataReloadListener.java` | Present | Present | **Identical** |

---

#### BoardStateData System — REMOVED

The entire board state (棋谱/残局) data pipeline was removed:
- **BoardStateData.java**: Static data holder for chess/xiangqi/gomoku board state records
- **BoardStateRecord.java**: Record with `tags`, `display` (description + author), `data`, `weight` fields
- **BoardStateDataReloadListener.java**: Loaded board states from `data/board_states/chess.json`, `xiangqi.json`, `gomoku.json`

Also related: `RandomBoardStateFunction` (loot function) and board state items (`GOMOKU_BOARD_STATE`, `CCHESS_BOARD_STATE`, `WCHESS_BOARD_STATE`) — though items still exist in `ItemModelGenerator` (also removed).

The minigame board state feature appears to be deprecated or refactored out of the mod entirely.

---

#### KaomojiDataReloadListener — Import Migration Only
```diff
- import net.minecraft.resources.ResourceLocation;
+ import net.minecraft.resources.Identifier;
```
Yarn → Mojang mappings: `ResourceLocation` renamed to `Identifier`. The `FILE_PATH` constant and loading logic are identical.

---

#### SkillsDataReloadListener — Unchanged
Identical between OLD and NEW. No migration needed.

---

## Advancement Changes

### Package: `src/main/java/com/github/tartaricacid/touhoulittlemaid/advancements/`

File inventory is identical between OLD and NEW. All 7 files present with minimal changes.

| File | Change |
|------|--------|
| `altar/AltarCraftTrigger.java` | `ResourceLocation` → `Identifier` (import + parameter types) |
| `maid/MaidEventTrigger.java` | **Identical** |
| `maid/TriggerType.java` | **Identical** |
| `rewards/GivePatchouliBookConfigTrigger.java` | **Identical** |
| `rewards/GiveSmartSlabConfigTrigger.java` | **Identical** |
| `altar/package-info.java` | **Identical** |
| `maid/package-info.java` | **Identical** |
| `rewards/package-info.java` | **Identical** |

#### AltarCraftTrigger — `ResourceLocation` → `Identifier`
```diff
- public void trigger(ServerPlayer serverPlayer, ResourceLocation recipeId)
+ public void trigger(ServerPlayer serverPlayer, Identifier recipeId)

- ResourceLocation recipeId
+ Identifier recipeId

- ResourceLocation.CODEC
+ Identifier.CODEC
```
No logic changes — only the Mojang mappings rename.

---

## Loot System Changes

### Package: `src/main/java/com/github/tartaricacid/touhoulittlemaid/loot/`

| File | OLD | NEW | Change |
|------|-----|-----|--------|
| `RandomBoardStateFunction.java` | Present | **REMOVED** | Board state loot function |
| `SetTankCountFunction.java` | Present | **REMOVED** | Tank backpack loot function |
| `LootTableTypeCondition.java` | Present | Present | API migration |
| `SetInitMaidOwnerFunction.java` | Present | Present | API migration |
| `package-info.java` | Present | Present | Identical |

---

#### RandomBoardStateFunction — REMOVED
This loot function randomly selected a board state (chess/xiangqi/gomoku) matching specified tags and applied it to the board state item stack using `WeightedPicker.pickRandom()`.

**Dependency chain**: Depended on `BoardStateData`, `BoardStateRecord`, `InitLootModifier.BOARD_STATE_RANDOMLY`. The entire chain was removed.

---

#### SetTankCountFunction — REMOVED
This loot function pre-filled a tank backpack's fluid tank with a specified fluid and amount:
```java
public Builder(Fluid fluid, int bucketCount) {
    // filled with bucketCount * FluidType.BUCKET_VOLUME
}
```
Used `InitLootModifier.SET_TANK_COUNT_FUNCTION` and `InitDataComponent.TANK_BACKPACK_TAG`.

---

#### SetInitMaidOwnerFunction — Loot Codec API Migration

| Change | OLD | NEW |
|--------|-----|-----|
| Type ID | `ResourceLocation ID` | `Identifier ID` |
| Codec exposure | `getType()` returns `LootItemFunctionType` | `codec()` returns `MapCodec` |
| Param access | `context.getParamOrNull(...)` | `context.getOptionalParameter(...)` |
| `InitLootModifier` ref | Used in `getType()` | Removed (no explicit type register) |

```diff
- @Override
- public LootItemFunctionType<...> getType() {
-     return InitLootModifier.SET_INIT_MAID_OWNER_FUNCTION.get();
- }
+ @Override
+ public MapCodec<? extends LootItemConditionalFunction> codec() {
+     return CODEC;
+ }

- Entity entity = context.getParamOrNull(LootContextParams.THIS_ENTITY);
+ Entity entity = context.getOptionalParameter(LootContextParams.THIS_ENTITY);
```

The `codec()` method replaces `getType()` — a major 1.21.1 loot table API change. The `LootItemFunctionType` is now resolved from the codec rather than explicitly registered+referenced.

---

#### LootTableTypeCondition — Loot Condition API Migration

| Change | OLD | NEW |
|--------|-----|-----|
| Condition type | `getType()` returns `LootItemConditionType` | `codec()` returns `MapCodec` |
| `ResourceLocation` | Used for `lootTableType` field | Changed to `Identifier` |
| Registry lookup | `context.getResolver().get(Registries.LOOT_TABLE, key)` | `context.getResolver().get(key)` (1 param) |
| ResourceKey.method | `.location()` | `.identifier()` |

```diff
- @Override
- public LootItemConditionType getType() {
-     return InitLootModifier.LOOT_TABLE_TYPE.get();
- }
+ @Override
+ public MapCodec<? extends LootItemCondition> codec() {
+     return CODEC;
+ }

- context.getResolver().get(Registries.LOOT_TABLE, currentLootTable)
+ context.getResolver().get(currentLootTable)

- lootTableAdd.location()
+ lootTableAdd.identifier()
```

---

## File Inventory

### Removed Files (14 total)

| # | File | Reason |
|---|------|--------|
| 1 | `config/subconfig/VanillaConfig.java` | Vanilla replacement config deprecated |
| 2 | `datagen/ItemModelGenerator.java` | Item model gen refactored out |
| 3 | `datagen/tag/TagRecipeSerializer.java` | Create compat tag removed |
| 4 | `datapack/BoardStateData.java` | Board state feature removed |
| 5 | `datapack/pojo/BoardStateRecord.java` | Board state feature removed |
| 6 | `datapack/resources/BoardStateDataReloadListener.java` | Board state feature removed |
| 7 | `loot/RandomBoardStateFunction.java` | Board state loot function removed |
| 8 | `loot/SetTankCountFunction.java` | Tank backpack loot function removed |

### New Files (3 total)

| # | File | Purpose |
|---|------|---------|
| 1 | `datagen/DamageTypeProvider.java` | Danmaku damage type datagen |
| 2 | `datagen/TimelinesProvider.java` | Maid schedule timeline datagen |
| 3 | `datagen/tag/TagTimeline.java` | Timeline tag (UNIVERSAL) provider |

### API Migration Summary

| API Pattern | OLD (1.20.1) | NEW (1.21.1) |
|-------------|-------------|-------------|
| `RecordCodecBuilder.create()` | AttachmentType serialization | Change to `RecordCodecBuilder.mapCodec()` |
| `getType()` in loot items | `LootItemFunctionType` / `LootItemConditionType` | Change to `codec()` returning `MapCodec` |
| `SavedData` | `SavedData.Factory<>()` with manual load/save | `SavedDataType<>()` with CODEC |
| `ResourceLocation` | Import name | Renamed to `Identifier` |
| `.location()` on ResourceKey | Returns `ResourceLocation` | Renamed to `.identifier()` |
| `getParamOrNull()` | Loot context parameter | Renamed to `getOptionalParameter()` |
| `GatherDataEvent` | Full event | `GatherDataEvent.Client` sub-event |
| `getAllKeys()` on CompoundTag | Returns `Set<String>` | Renamed to `keySet()` |
| `getCompound(key)` | Returns CompoundTag | Changed to `getCompoundOrEmpty(key)` |
| `SaveAsPassenger` | Takes `CompoundTag` | Takes `TagValueOutput` |
| Server access | `player.getServer()` | `player.level().getServer()` |
| Data folder | `dataFolder.toPath()` | `dataFolder` (already Path) |

### Config Value Migration Checklist

- [ ] `VanillaConfig.REPLACE_SLIME_MODEL` — **REMOVED** — Remove all references
- [ ] `VanillaConfig.REPLACE_XP_TEXTURE` — **REMOVED** — Remove all references
- [ ] `VanillaConfig.REPLACE_TOTEM_TEXTURE` — **REMOVED** — Remove all references
- [ ] `VanillaConfig.REPLACE_XP_BOTTLE_TEXTURE` — **REMOVED** — Remove all references
- [ ] `ServerConfig.*` — All 4 values unchanged, no migration needed
- [ ] `InitLootModifier.BOARD_STATE_RANDOMLY` — **REMOVED** (from loot)
- [ ] `InitLootModifier.SET_TANK_COUNT_FUNCTION` — **REMOVED** (from loot)
- [ ] `InitDataComponent.TANK_BACKPACK_TAG` — Still referenced? (used in removed SetTankCountFunction)
