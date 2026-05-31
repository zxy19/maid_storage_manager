# Vanilla API Migration — Items, Blocks, Entities (1.20.1 → 1.21.1)

## 1. Item System

### 1.1 Item.Properties — `setId()` Requirement

**OLD (NeoForge 21.1):**
```java
public ItemCamera() {
    super((new Properties()).stacksTo(1).durability(50));
}
```

**NEW (NeoForge 26.1):**
```java
public ItemCamera(Identifier id) {
    super((new Properties())
            .stacksTo(1)
            .durability(50)
            .setId(ResourceKey.create(Registries.ITEM, id)));
}
```

**Key changes:**
- All item constructors now receive `Identifier id` parameter
- Must call `.setId(ResourceKey.create(Registries.ITEM, id))` on `Item.Properties`
- `.stacksTo()`, `.durability()`, `.fireResistant()`, `.food()`, `.rarity()` remain unchanged

**Affected files:** All items in `item/` package, including `ItemCamera`, `ItemMaidBackpack`, `ItemChair`, `ItemHakureiGohei`, `ItemBroom`, `ItemFoxScroll`, `ItemTrumpet`, etc.
(Complete list in `migration/06-items-blocks-inventory.md`)

### 1.2 Item Registration — `DeferredRegister.Items`

**OLD:**
```java
public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, MOD_ID);
// Registration: () -> new ItemCamera()
```

**NEW:**
```java
public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
// Registration: ItemCamera::new (method reference, receives Identifier)
```

**Key change:** Uses `DeferredRegister.Items` (specialized) instead of `DeferredRegister<Item>`. Return type is `DeferredItem<Item>`.

**Source file:** `init/InitItems.java` (both projects)

### 1.3 `use()` Return Type Change

**OLD:**
```java
public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn)
```

**NEW:**
```java
public InteractionResult use(Level worldIn, Player playerIn, InteractionHand handIn)
```

Return `InteractionResult.SUCCESS` instead of `InteractionResultHolder.sidedSuccess(stack)`.

**Affected files:** `item/ItemCamera.java`, `item/ItemExtinguisher.java`, `item/ItemBroom.java`, etc.

### 1.4 `appendHoverText()` Signature Change

**OLD:**
```java
public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn)
```

**NEW:**
```java
public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag)
```

- Tooltip is added via `tooltip.accept(component)` (Consumer pattern) instead of `tooltip.add(component)`
- `Level` param replaced by `TooltipContext`
- New `TooltipDisplay` param from `net.minecraft.world.item.component.TooltipDisplay`

**Affected files:** All items with tooltips — `item/ItemCamera.java`, `item/ItemFoxScroll.java`, `item/ItemTrumpet.java`, etc.

### 1.5 DataComponents (replaces ItemStack NBT)

**1.21.1 introduces `DataComponents`** — a new system replacing direct NBT on ItemStacks:

| Old Pattern | New Pattern |
|---|---|
| `stack.getOrCreateTag()` | `stack.get(DataComponents.CUSTOM_DATA)` → CustomData |
| `stack.getOrCreateTag().putString(key, val)` | Custom data via `ItemStack.update()` |
| `stack.getOrCreateTag().getUUID(key)` | `tag.read(UUIDUtil.CODEC.fieldOf(key))` |

**Key new classes imported:**
```java
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.ItemAttributeModifiers;
```

**Spawn egg example (OLD → NEW):**
```java
// OLD:
new DeferredSpawnEggItem(() -> EntityMaid.TYPE, 0xffffff, 0xffffff, new Item.Properties())

// NEW:
new SpawnEggItem(new Item.Properties()
    .setId(ResourceKey.create(Registries.ITEM, id))
    .component(DataComponents.ENTITY_DATA, TypedEntityData.of(EntityMaid.TYPE, new CompoundTag())))
```

- `DeferredSpawnEggItem` (Forge) → `SpawnEggItem` (vanilla) with `.component(DataComponents.ENTITY_DATA, ...)`

**Files using DataComponents:** `init/InitItems.java`, `item/ItemPicnicBasket.java`, `item/ItemCamera.java`, `item/ItemFilm.java`, `item/ItemGarageKit.java`, `block/BlockGarageKit.java`, `entity/passive/MaidCombatManager.java`, etc.

### 1.6 ItemStack Serialization

**OLD:**
```java
ItemStack.STRICT_CODEC  // in AltarRecipeSerializer
ItemStack.STREAM_CODEC.decode(buf)
```

**NEW:**
```java
ItemStackTemplate.CODEC  // replaced STRICT_CODEC
ItemStackTemplate.STREAM_CODEC.decode(buf)
```

### 1.7 Entity → Item Stack NBT Changes

**OLD:**
```java
maid.saveWithoutId(CompoundTag tag)
maid.load(CompoundTag tag)
maid.readAdditionalSaveData(CompoundTag tag)
```

**NEW:**
```java
maid.saveWithoutId(TagValueOutput output) + output.buildResult()
maid.load(ValueInput input)
maid.readAdditionalSaveData(ValueInput input)
```

**Import changes:**
```java
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.nbt.TagValueOutput;
```

**Affected files:** `item/ItemCamera.java`, `item/ItemGarageKit.java`, `item/ItemSmartSlab.java`, `item/ItemChair.java`, `item/ItemPhoto.java`, `entity/item/AbstractEntityFromItem.java`

---

## 2. Block System

### 2.1 Block.Properties — `.setId()` Requirement

**OLD:**
```java
public BlockAltar() {
    super(BlockBehaviour.Properties.of().sound(SoundType.STONE).strength(2, 2).noOcclusion());
}
```

**NEW:**
```java
public BlockAltar(Identifier id) {
    super(BlockBehaviour.Properties.of()
            .setId(ResourceKey.create(Registries.BLOCK, id))
            .sound(SoundType.STONE)
            .strength(2, 2)
            .noOcclusion());
}
```

Same pattern as items: `.setId(ResourceKey.create(Registries.BLOCK, id))` is now required.

**Source file:** `init/InitBlocks.java` — unchanged between old/new (constructor patterns identical)

### 2.2 Block Registration

**OLD:**
```java
public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, MOD_ID);
// DeferredBlock<Block>
```

**NEW:**
```java
public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);
// DeferredBlock<Block>
```

Same `DeferredRegister.Blocks` / `DeferredRegister.createBlocks()` pattern as items.

### 2.3 `ClientboundBlockEntityDataPacket`

The class name has changed package in 1.21.1:
- Both versions use `import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;`
- This is unchanged in this project.

---

## 3. Entity System

### 3.1 EntityType Registration — `.build()` API

**OLD (NeoForge 21.1):**
```java
public static final EntityType<EntityMaid> TYPE = EntityType.Builder
    .<EntityMaid>of(EntityMaid::new, MobCategory.CREATURE)
    .sized(0.6f, 1.5f)
    .clientTrackingRange(10)
    .build("maid");  // takes String mod ID prefix
```

**NEW (NeoForge 26.1):**
```java
public static final Identifier ENTITY_ID = Identifier.fromNamespaceAndPath(TouhouLittleMaid.MOD_ID, "maid");
public static final ResourceKey<EntityType<?>> ENTITY_KEY = ResourceKey.create(Registries.ENTITY_TYPE, ENTITY_ID);
public static final EntityType<EntityMaid> TYPE = EntityType.Builder
    .<EntityMaid>of(EntityMaid::new, MobCategory.CREATURE)
    .sized(0.6f, 1.5f)
    .clientTrackingRange(10)
    .build(ENTITY_KEY);  // takes ResourceKey instead of String
```

**Key difference:** `.build()` now takes a `ResourceKey<EntityType<?>>` instead of a String. A separate `Identifier` and `ResourceKey` must be defined first.

**Affected files:** `entity/passive/EntityMaid.java`, `entity/item/EntityChair.java`, `entity/monster/EntityFairy.java`, etc.

### 3.2 EntityDataAccessor → AttachmentType (Major Migration)

This is **the biggest single API change** in the entity system. `EntityDataAccessor` fields (which used `SynchedEntityData`) have been replaced with NeoForge's `AttachmentType<T>` system.

**OLD (EntityDataAccessor):**
```java
private static final EntityDataAccessor<String> DATA_MODEL_ID = SynchedEntityData.defineId(EntityMaid.class, EntityDataSerializers.STRING);
private static final EntityDataAccessor<Integer> DATA_HUNGER = SynchedEntityData.defineId(EntityMaid.class, EntityDataSerializers.INT);
// ... 30+ EntityDataAccessor fields

// Usage:
this.entityData.set(DATA_MODEL_ID, "newValue");
String value = this.entityData.get(DATA_MODEL_ID);
```

**NEW (AttachmentType with Record):**
```java
// Data defined as a record class with its own AttachmentType
public record ProfileData(String modelId, String soundPackId) {
    private static final MapCodec<ProfileData> CODEC = RecordCodecBuilder.mapCodec(ins -> ins.group(
            Codec.STRING.fieldOf("model_id").forGetter(ProfileData::modelId),
            Codec.STRING.fieldOf("sound_pack_id").forGetter(ProfileData::soundPackId)
    ).apply(ins, ProfileData::new));

    private static final StreamCodec<RegistryFriendlyByteBuf, ProfileData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ProfileData::modelId,
            ByteBufCodecs.STRING_UTF8, ProfileData::soundPackId,
            ProfileData::new
    );

    public static final AttachmentType<ProfileData> TYPE = AttachmentType
            .builder(() -> new ProfileData(DEFAULT_MODEL_ID, getInitSoundPackId()))
            .serialize(CODEC)
            .sync(STREAM_CODEC)
            .build();
}

// Usage:
maid.setData(InitDataAttachment.PROFILE, current.withModelId("newModelId"));
ProfileData data = maid.getData(InitDataAttachment.PROFILE);
```

**Key components of an AttachmentType:**
1. **Builder** — `AttachmentType.builder(defaultSupplier)`
2. **serialize(CODEC)** — for save/load (persistence)
3. **sync(STREAM_CODEC)** — for network synchronization
4. **build()** — returns the AttachmentType

**AttachmentType registration:**
```java
public interface InitDataAttachment {
    DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, MOD_ID);

    Supplier<AttachmentType<ProfileData>> PROFILE = ATTACHMENT_TYPES.register("profile", () -> ProfileData.TYPE);
    Supplier<AttachmentType<StatsData>> STATS = ATTACHMENT_TYPES.register("stats", () -> StatsData.TYPE);
    Supplier<AttachmentType<TaskData>> TASK = ATTACHMENT_TYPES.register("task", () -> TaskData.TYPE);
    Supplier<AttachmentType<AnimationData>> ANIMATION = ATTACHMENT_TYPES.register("animation", () -> AnimationData.TYPE);
    Supplier<AttachmentType<ConfigData>> CONFIG = ATTACHMENT_TYPES.register("config", () -> ConfigData.TYPE);
    Supplier<AttachmentType<BackpackData>> BACKPACK = ATTACHMENT_TYPES.register("backpack", () -> BackpackData.TYPE);
    Supplier<AttachmentType<GameData>> GAME = ATTACHMENT_TYPES.register("game", () -> GameData.TYPE);
}
```

**Data record classes (all new in 1.21.1):**

| Record | Fields | File |
|---|---|---|
| `ProfileData` | `modelId: String`, `soundPackId: String` | `entity/data/ProfileData.java` |
| `StatsData` | `hunger: int`, `favorability: int`, `experience: int`, `struckByLightning: boolean` | `entity/data/StatsData.java` |
| `TaskData` | `taskId: String`, `schedule: ...`, `restrictCenter: BlockPos`, `restrictRadius: int` | `entity/data/TaskData.java` |
| `AnimationData` | `begging: boolean`, `chargingCrossbow: boolean`, `swingingArms: boolean`, `aiming: boolean` | `entity/data/AnimationData.java` |
| `ConfigData` | `pickup: boolean`, `homeMode: boolean`, `rideable: boolean`, `showBackpack: boolean`, ... | `entity/data/ConfigData.java` |
| `BackpackData` | `backpackType: String` | `entity/data/BackpackData.java` |
| `GameData` | `winCounts: Map`, `gameStatue: byte` | `entity/data/GameData.java` |

**Result:** EntityMaid went from 30+ `EntityDataAccessor` fields to just 3 remaining on the entity:
- `DATA_SYNC_INVULNERABLE`
- `BACKPACK_ITEM_SHOW`
- `CHAT_BUBBLE`

**Source files:** `init/InitDataAttachment.java`, `entity/data/*.java`

### 3.3 Player Data Attachments

Old project directly accessed `MaidNumAttachment` on players. New project also uses `AttachmentType` for player data:

```java
Supplier<AttachmentType<MaidNumAttachment>> MAID_NUM = ATTACHMENT_TYPES.register("maid_num", () -> MaidNumAttachment.TYPE);
Supplier<AttachmentType<PowerAttachment>> POWER_NUM = ATTACHMENT_TYPES.register("power", () -> PowerAttachment.TYPE);
Supplier<AttachmentType<ChatTokensAttachment>> CHAT_TOKENS = ATTACHMENT_TYPES.register("chat_tokens", () -> ChatTokensAttachment.TYPE);
```

**Source file:** `init/InitDataAttachment.java`

### 3.4 MobSpawnType → EntitySpawnReason

**OLD:**
```java
import net.minecraft.world.entity.MobSpawnType;

public SpawnGroupData finalizeSpawn(ServerLevelAccessor worldIn, DifficultyInstance difficultyIn,
                                     MobSpawnType reason, @Nullable SpawnGroupData spawnDataIn)
```

**NEW:**
```java
import net.minecraft.world.entity.EntitySpawnReason;

public SpawnGroupData finalizeSpawn(ServerLevelAccessor worldIn, DifficultyInstance difficultyIn,
                                     EntitySpawnReason reason, @Nullable SpawnGroupData spawnDataIn)
```

**Enum values mapping:**
| Old (`MobSpawnType`) | New (`EntitySpawnReason`) |
|---|---|
| `MobSpawnType.STRUCTURE` | `EntitySpawnReason.STRUCTURE` |
| `MobSpawnType.SPAWN_EGG` | `EntitySpawnReason.SPAWN_EGG` |
| `MobSpawnType.EVENT` | `EntitySpawnReason.EVENT` |
| — | `EntitySpawnReason.SPAWN_ITEM_USE` (new) |

**Affected files:** `entity/passive/EntityMaid.java`, `crafting/AltarRecipe.java`, `item/ItemBroom.java`, `item/ItemChair.java`, `item/ItemPhoto.java`, `item/ItemSmartSlab.java`, `item/ItemCamera.java`, `block/BlockGarageKit.java`, `entity/monster/EntityFairy.java`

### 3.5 SpawnPlacementTypes

**OLD:**
```java
SpawnPlacementTypes.ON_GROUND
Heightmap.Types.MOTION_BLOCKING_NO_LEAVES  // note: Heightmap (no dot)
```

**NEW:**
```java
SpawnPlacementTypes.ON_GROUND
Heightmap.Types.MOTION_BLOCKING_NO_LEAVES  // note: Heightmap (with dot), same
```

`SpawnPlacementTypes` replaced the old `SpawnPlacements.Type`. The old project already used `SpawnPlacementTypes` so this is unchanged.

**Event method signature unchanged:**
```java
event.register(entityType, placementType, heightmap, predicate, operation);
```

**Source file:** `init/InitEntities.java`

### 3.6 `EntityAttributeCreationEvent.bus`

**OLD:**
```java
@SubscribeEvent
public static void addEntityAttributeEvent(EntityAttributeCreationEvent event) {
    event.put(EntityMaid.TYPE, EntityMaid.createAttributes().build());
}
// EntityAttributeCreationEvent called directly via EVENT_BUS
```

**NEW:**
In the new project, this is set up in `init/InitAttribute.java` instead, using `@EventBusSubscriber(modid = ...)`:
```java
@SubscribeEvent
static void addEntityAttributeEvent(EntityAttributeCreationEvent event) {
    event.put(EntityMaid.TYPE, InitAttribute.createMaidAttributes().build());
}
```

**Source files:** `init/InitAttribute.java` (new), `init/InitEntities.java` (old had it inline)

### 3.7 Entity Attribute Registration

**OLD:**
```java
DeferredRegister.create(BuiltInRegistries.ATTRIBUTE, MOD_ID)
```

**NEW:**
```java
DeferredRegister.create(BuiltInRegistries.ATTRIBUTE, MOD_ID)
```

Same registration pattern. Both use `new RangedAttribute(key, defaultValue, min, max).setSyncable(true)`.

**Source files:** `init/InitAttribute.java` (both projects — identical)

### 3.8 `isClientSide` — Field → Method

**OLD:**
```java
if (!worldIn.isClientSide) { }  // boolean field
```

**NEW:**
```java
if (!worldIn.isClientSide()) { }  // method call
```

`Level.isClientSide` changed from a boolean field to a method `isClientSide()`.

**Affects:** Almost every file that checks client/server side.

---

## 4. Mob AI — Brain, Activity, Goal System

### 4.1 Brain Provider Pattern

**OLD (NeoForge 21.1):**
```java
// brainProvider returns a Provider (Memory+Sensor types only)
@Override
protected Brain.Provider<EntityMaid> brainProvider() {
    return Brain.provider(MaidBrain.getMemoryTypes(), MaidBrain.getSensorTypes());
}

// makeBrain takes Dynamic<?> (NBT serialization)
@Override
protected Brain<?> makeBrain(Dynamic<?> dynamicIn) {
    Brain<EntityMaid> brain = this.brainProvider().makeBrain(dynamicIn);
    MaidBrain.registerBrainGoals(brain, this);
    return brain;
}
```

**NEW (NeoForge 26.1):**
```java
// BRAIN_PROVIDER is a static constant that includes Activities
public static final Brain.Provider<EntityMaid> BRAIN_PROVIDER = Brain.provider(
        MaidBrain.getMemoryTypes(),
        MaidBrain.getSensorTypes(),
        MaidBrain::getActivities
);

// makeBrain takes Brain.Packed (codec-based serialization)
@Override
protected Brain<? extends LivingEntity> makeBrain(Brain.Packed packedBrain) {
    Brain<EntityMaid> brain = BRAIN_PROVIDER.makeBrain(this, packedBrain);
    MaidBrain.registerBrainGoals(brain, this);
    return brain;
}
```

**Key differences:**
1. `Brain.Provider` now includes Activities (3-arg provider)
2. `makeBrain()` takes `Brain.Packed` instead of `Dynamic<?>`
3. `BRAIN_PROVIDER` is a static constant instead of a method

**Source files:** `entity/passive/EntityMaid.java` (both), `entity/ai/brain/MaidBrain.java` (both)

### 4.2 Activity Registration — `ActivityData.create()`

**OLD:**
```java
// Activities were registered via brain.addActivity()
brain.addActivity(Activity.CORE, ImmutableList.copyOf(behaviors));
brain.addActivity(Activity.IDLE, ImmutableList.copyOf(behaviors));
brain.addActivity(Activity.REST, ImmutableList.copyOf(behaviors));

// Active activity management
brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
brain.setDefaultActivity(Activity.IDLE);
brain.setActiveActivityIfPossible(Activity.IDLE);
```

**NEW:**
```java
// Activities are returned as a list of ActivityData objects
public static List<ActivityData<EntityMaid>> getActivities(EntityMaid maid) {
    return List.of(
            initCoreActivity(),    // → ActivityData.create(Activity.CORE, ...)
            initPanicActivity(),   // → ActivityData.create(Activity.PANIC, ...)
            initRideIdleActivity(), // → ActivityData.create(InitBrains.RIDE_IDLE.get(), ...)
            initRideWorkActivity(maid),
            initRideRestActivity(),
            initIdleActivity(),    // → ActivityData.create(Activity.IDLE, ...)
            initWorkActivity(maid), // → ActivityData.create(Activity.WORK, ...)
            initRestActivity()     // → ActivityData.create(Activity.REST, ...)
    );
}
```

**Key differences:**
1. `brain.addActivity()` → `ActivityData.create(activity, behaviors)`
2. Activities are now returned from the `getActivities` function, not registered via `addActivity`
3. No more `setCoreActivities()`, `setDefaultActivity()`, `setActiveActivityIfPossible()` — these are handled by the `Brain.Provider`
4. Custom activities use `ActivityData.create(customActivity, behaviors)` — same as vanilla

### 4.3 Schedule → EnvironmentAttribute

**OLD:**
```java
// Schedules registered as DeferredRegister<Schedule> in Registries.SCHEDULE
public static final DeferredRegister<Schedule> SCHEDULES = DeferredRegister.create(Registries.SCHEDULE, MOD_ID);

// Schedule set via brain.setSchedule()
brain.setSchedule(InitEntities.MAID_DAY_SHIFT_SCHEDULES.get());

// Defined with ScheduleBuilder
new ScheduleBuilder(new Schedule())
    .changeActivityAt(0, Activity.WORK)
    .changeActivityAt(12000, Activity.IDLE)
    .build();
```

**NEW:**
```java
// Schedules are now EnvironmentAttribute<Activity> in Registries.ENVIRONMENT_ATTRIBUTE
public static final DeferredRegister<EnvironmentAttribute<?>> ENVIRONMENT_ATTRIBUTES = DeferredRegister.create(Registries.ENVIRONMENT_ATTRIBUTE, MOD_ID);

// Defined with EnvironmentAttribute.builder()
EnvironmentAttribute
    .builder(AttributeTypes.ACTIVITY)
    .defaultValue(Activity.IDLE)
    .build();

// Schedule set via brain.setSchedule(maid.getSchedule().getEnvironmentAttribute())
```

**Key differences:**
1. `Schedule` → `EnvironmentAttribute<Activity>`
2. `Registries.SCHEDULE` → `Registries.ENVIRONMENT_ATTRIBUTE`
3. `ScheduleBuilder` → `EnvironmentAttribute.builder(AttributeTypes.ACTIVITY)`
4. `brain.setSchedule(schedule)` → `brain.setSchedule(environmentAttribute)`

**Source files:** `init/InitBrains.java` (new), `init/InitEntities.java` (old had Schedules)

### 4.4 Custom Activities Registration

**OLD:**
```java
public static Supplier<Activity> RIDE_IDLE = ACTIVITIES.register("ride_idle", () -> new Activity("tlm_ride_idle"));
// Registered in InitEntities: Registries.ACTIVITY
```

**NEW:**
```java
Supplier<Activity> RIDE_IDLE = ACTIVITIES.register("ride_idle", () -> new Activity("tlm_ride_idle"));
// Registered in InitBrains: Registries.ACTIVITY (same registry)
```

Activity registration itself is identical — `DeferredRegister.create(Registries.ACTIVITY, MOD_ID)`.

### 4.5 MemoryModuleType & SensorType Registration

Both old and new use the same pattern:
```java
DeferredRegister.create(Registries.MEMORY_MODULE_TYPE, MOD_ID);
DeferredRegister.create(Registries.SENSOR_TYPE, MOD_ID);
```

**Source files:** `init/InitBrains.java` (new), `init/InitEntities.java` (old)

### 4.6 `customServerAiStep()` Signature

**OLD:**
```java
protected void customServerAiStep()  // no parameter
```

**NEW:**
```java
protected void customServerAiStep(ServerLevel level)  // receives ServerLevel
```

**Source file:** `entity/passive/EntityMaid.java`

---

## 5. BlockEntity System

### 5.1 BlockEntityType Registration

**OLD:**
```java
// TYPE defined in BlockEntity class
public static final BlockEntityType<TileEntityStatue> TYPE = BlockEntityType.Builder
    .of(TileEntityStatue::new, InitBlocks.STATUE.get())
    .build(null);

// Registered in InitBlocks:
public static Supplier<BlockEntityType<TileEntityStatue>> STATUE_TE = 
    TILE_ENTITIES.register("statue", () -> TileEntityStatue.TYPE);
```

**NEW:**
```java
// TYPE defined in BlockEntity class — SAME PATTERN
public static final BlockEntityType<TileEntityStatue> TYPE = BlockEntityType.Builder
    .of(TileEntityStatue::new, InitBlocks.STATUE.get())
    .build(null);

// Registered in InitBlocks — SAME PATTERN
public static Supplier<BlockEntityType<TileEntityStatue>> STATUE_TE = 
    TILE_ENTITIES.register("statue", () -> TileEntityStatue.TYPE);
```

BlockEntityType registration is unchanged. Both use `BlockEntityType.Builder.of().build(null)`.

### 5.2 BlockEntity — Save/Load (CompoundTag → ValueInput/ValueOutput)

**OLD:**
```java
protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) { }
public void load(CompoundTag tag, HolderLookup.Provider registries) { }
```

**NEW:**
```java
protected void saveAdditional(ValueOutput output, HolderLookup.Provider registries) { }
public void load(ValueInput input, HolderLookup.Provider registries) { }
protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) { }  // OLD, still compiled but deprecated
public void loadAdditional(ValueInput input, HolderLookup.Provider registries) { }
```

**Imports:**
```java
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
```

**Source files:** `tileentity/TileEntityStatue.java`, and all BlockEntity subclasses

### 5.3 BlockEntityType Registration — `DeferredRegister`

```java
public static final DeferredRegister<BlockEntityType<?>> TILE_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MOD_ID);
```

Unchanged from old to new.

---

## 6. Recipe System

### 6.1 RecipeSerializer — MapCodec Pattern

**OLD:**
```java
// AltarRecipeSerializer implements RecipeSerializer<AltarRecipe>
public class AltarRecipeSerializer implements RecipeSerializer<AltarRecipe> {
    public static final MapCodec<AltarRecipe> CODEC = RecordCodecBuilder.mapCodec(/*...*/);

    @Override
    public MapCodec<AltarRecipe> codec() { return CODEC; }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, AltarRecipe> streamCodec() {
        return StreamCodec.of(this::toNetwork, this::fromNetwork);
    }
}

// Registration:
new AltarRecipeSerializer()  // instantiated, implements RecipeSerializer
```

**NEW:**
```java
// AltarRecipeSerializer is a utility class (NOT implementing RecipeSerializer)
public class AltarRecipeSerializer {
    public static final MapCodec<AltarRecipe> CODEC = RecordCodecBuilder.mapCodec(/*...*/);
    public static final StreamCodec<RegistryFriendlyByteBuf, AltarRecipe> STREAM_CODEC = /*...*/;

    public static final RecipeSerializer<AltarRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);
}

// Registration:
AltarRecipeSerializer.SERIALIZER  // static field, uses RecipeSerializer(codec, streamCodec) constructor
```

**Key differences:**
1. No longer `implements RecipeSerializer` — uses `new RecipeSerializer<>(codec, streamCodec)` (built-in constructor)
2. `StreamCodec` object is `STREAM_CODEC` (constant) instead of method references
3. Altar recipe lost `group`, `category` fields in the new version (simplified)

**Source files:** `crafting/AltarRecipeSerializer.java` (both projects)

### 6.2 Recipe Registration

**OLD:**
```java
public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = 
    DeferredRegister.create(Registries.RECIPE_SERIALIZER, MOD_ID);
public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = 
    DeferredRegister.create(Registries.RECIPE_TYPE, MOD_ID);

public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<AltarRecipe>> ALTAR_RECIPE_SERIALIZER = 
    RECIPE_SERIALIZERS.register("altar_recipe_serializers", AltarRecipeSerializer::new);
```

**NEW:**
```java
public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = 
    DeferredRegister.create(Registries.RECIPE_SERIALIZER, MOD_ID);
public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = 
    DeferredRegister.create(Registries.RECIPE_TYPE, MOD_ID);
public static final DeferredRegister<RecipeBookCategory> RECIPE_BOOK_CATEGORIES = 
    DeferredRegister.create(Registries.RECIPE_BOOK_CATEGORY, MOD_ID);  // NEW

public static final Supplier<RecipeSerializer<AltarRecipe>> ALTAR_RECIPE_SERIALIZER = 
    RECIPE_SERIALIZERS.register("altar", () -> AltarRecipeSerializer.SERIALIZER);
```

**Key differences:**
1. New registrable: `RecipeBookCategory` in `Registries.RECIPE_BOOK_CATEGORY`
2. Uses `AltarRecipeSerializer.SERIALIZER` (static constant) instead of `AltarRecipeSerializer::new` (instantiation)

### 6.3 AltarRecipe — Removed `group` and `category`

**OLD constructor:**
```java
public AltarRecipe(String group, CraftingBookCategory category, NonNullList<Ingredient> ingredients, 
                    float power, ItemStack result, ResourceLocation entityType, String langKey)
```

**NEW constructor:**
```java
public AltarRecipe(NonNullList<Ingredient> ingredients, float power, 
                    ItemStackTemplate result, Identifier entityType, String langKey)
```

**Key changes:**
- `String group` removed
- `CraftingBookCategory category` removed
- `ItemStack` result → `ItemStackTemplate` result
- `ResourceLocation entityType` → `Identifier entityType`

**Source files:** `crafting/AltarRecipe.java` (both projects)

---

## 7. Loot System

### 7.1 LootItemConditionType → MapCodec Direct Registration

**OLD:**
```java
// Registered as LootItemConditionType (wrapper type)
public static final DeferredRegister<LootItemConditionType> LOOT_CONDITION_TYPES = 
    DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, MOD_ID);

public static final Supplier<LootItemConditionType> LOOT_TABLE_TYPE = 
    LOOT_CONDITION_TYPES.register("loot_table_type", 
        () -> new LootItemConditionType(LootTableTypeCondition.CODEC));
```

**NEW:**
```java
// Registered directly as MapCodec (no wrapper type)
public static final DeferredRegister<MapCodec<? extends LootItemCondition>> LOOT_CONDITION_TYPES = 
    DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, MOD_ID);

public static final Supplier<MapCodec<? extends LootItemCondition>> LOOT_TABLE_TYPE = 
    LOOT_CONDITION_TYPES.register("loot_table_type", 
        () -> LootTableTypeCondition.CODEC);
```

**Key change:** `LootItemConditionType` wrapper is gone. Register `MapCodec` directly. This applies to both conditions and functions.

### 7.2 LootItemFunctionType → MapCodec Direct Registration

**OLD:**
```java
public static final DeferredRegister<LootItemFunctionType<?>> LOOT_FUNCTION_TYPES = 
    DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, MOD_ID);

public static final Supplier<LootItemFunctionType<? extends LootItemConditionalFunction>> SET_INIT_MAID_OWNER_FUNCTION = 
    LOOT_FUNCTION_TYPES.register("set_init_maid_owner", 
        () -> new LootItemFunctionType<>(SetInitMaidOwnerFunction.CODEC));
```

**NEW:**
```java
public static final DeferredRegister<MapCodec<? extends LootItemFunction>> LOOT_FUNCTION_TYPES = 
    DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, MOD_ID);

public static final Supplier<MapCodec<? extends LootItemConditionalFunction>> SET_INIT_MAID_OWNER_FUNCTION = 
    LOOT_FUNCTION_TYPES.register("set_init_maid_owner", 
        () -> SetInitMaidOwnerFunction.CODEC);
```

**Key difference:**
1. `DeferredRegister<LootItemFunctionType<?>>` → `DeferredRegister<MapCodec<? extends LootItemFunction>>`
2. `new LootItemFunctionType<>(codec)` → just `codec`
3. No wrapper types needed

**Source files:** `init/InitLootModifier.java` (both projects)

### 7.3 Loot Parameter Names

**OLD:**
```java
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
LootContextParams.THIS_ENTITY
```

**NEW:**
```java
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
LootContextParams.THIS_ENTITY  // same
```

Loot context parameter names are unchanged.

**Source files:** `entity/item/EntityBox.java`, `block/BlockShrine.java`, etc.

---

## 8. Damage System

### 8.1 DamageSource — `HolderLookup` API Changes

**OLD:**
```java
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.core.Registry;

Registry<DamageType> damageTypes = thrower.level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE);
return new DamageSource(damageTypes.getHolderOrThrow(DANMAKU), danmaku, thrower);
```

**NEW:**
```java
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;

var damageTypes = thrower.level().registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE);
return new DamageSource(damageTypes.getOrThrow(DANMAKU), danmaku, thrower);
```

**Key changes:**
1. `registryAccess().registryOrThrow()` → `registryAccess().lookupOrThrow()`
2. `getHolderOrThrow(key)` → `getOrThrow(key)`
3. `level` (field) → `level()` (method)
4. No more `Registry<DamageType>` import — uses `var` or `HolderLookup.RegistryLookup<DamageType>`

**Source file:** `init/InitDamage.java` (both projects)

### 8.2 DamageType — ResourceKey Pattern

**OLD:**
```java
public static final ResourceKey<DamageType> DANMAKU = ResourceKey.create(Registries.DAMAGE_TYPE, 
    ResourceLocation.fromNamespaceAndPath(TouhouLittleMaid.MOD_ID, "danmaku"));
```

**NEW:**
```java
public static final ResourceKey<DamageType> DANMAKU = ResourceKey.create(Registries.DAMAGE_TYPE, 
    Identifier.fromNamespaceAndPath(TouhouLittleMaid.MOD_ID, "danmaku"));
// ResourceLocation → Identifier (same API, just different class name)
```

### 8.3 DamageType Registration

Damage types are registered via data generation (JSON files in `data/touhou_little_maid/damage_type/`). This is unchanged from old to new.

---

## 9. Complete Import Migration Table

### Common Import Changes

| Old Import | New Import | Notes |
|---|---|---|
| `net.minecraft.resources.ResourceLocation` | `net.minecraft.resources.Identifier` | Class renamed; same methods |
| `net.minecraft.world.entity.MobSpawnType` | `net.minecraft.world.entity.EntitySpawnReason` | Enum renamed |
| `net.minecraft.world.level.Level.isClientSide` (field) | `net.minecraft.world.level.Level.isClientSide()` (method) | Field → method |
| `net.minecraft.world.level.registryAccess()` (method) | `net.minecraft.world.level.registryAccess()` (method) | Same; but returns are now HolderLookup |
| `net.minecraft.core.Registry` | Not needed | Replaced by `HolderLookup` from `registryAccess()` |
| `net.minecraft.nbt.CompoundTag` | `net.minecraft.world.level.storage.ValueInput` / `ValueOutput` | For entity/block entity save/load |

### Item-Related Imports

| Old | New | Notes |
|---|---|---|
| `net.minecraft.world.InteractionResultHolder` | `net.minecraft.world.InteractionResult` | `use()` return type |
| NBT-based custom data | `net.minecraft.core.component.DataComponents` | New component system |
| NBT-based custom data | `net.minecraft.world.item.component.CustomData` | Wraps CompoundTag |
| — | `net.minecraft.world.item.component.TooltipDisplay` | New `appendHoverText` param |
| — | `net.minecraft.world.item.component.TypedEntityData` | For spawn egg entity data |
| — | `net.minecraft.world.item.component.ItemContainerContents` | Container item data |
| — | `net.minecraft.world.item.component.ItemAttributeModifiers` | Attribute modifiers |
| `net.neoforged.neoforge.common.DeferredSpawnEggItem` | `net.minecraft.world.item.SpawnEggItem` | Vanilla spawn egg |
| `net.minecraft.world.item.Items` | Same | Unchanged |
| `net.minecraft.world.item.Item.Properties()` | `net.minecraft.world.item.Item.Properties()` + `.setId()` | Must call setId |
| `net.minecraft.world.item.TooltipFlag` | Same | Unchanged |
| `net.neoforged.neoforge.items.ItemStackHandler` | `net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler` | ItemStack capacity now needs slots count |

### Entity-Related Imports

| Old | New | Notes |
|---|---|---|
| `net.minecraft.network.syncher.EntityDataAccessor` | `net.neoforged.neoforge.attachment.AttachmentType` | Major migration |
| `net.minecraft.network.syncher.EntityDataSerializers` | `com.mojang.serialization.Codec` + `net.minecraft.network.codec.ByteBufCodecs` | For new data system |
| `net.minecraft.network.syncher.SynchedEntityData` | `net.neoforged.neoforge.attachment.AttachmentType` + `RecordCodecBuilder` | No longer direct |
| `net.minecraft.world.entity.schedule.Schedule` | `net.minecraft.world.attribute.EnvironmentAttribute` | Schedule system |
| `net.minecraft.world.entity.schedule.ScheduleBuilder` | `net.minecraft.world.attribute.EnvironmentAttribute.builder()` | Builder |
| `com.mojang.serialization.Dynamic<?>` | `net.minecraft.world.entity.ai.Brain.Packed` | Brain serialization |
| `net.minecraft.world.entity.EntityType.Builder.build(String)` | `.build(ResourceKey)` | Entity registration |
| `net.neoforged.neoforge.registries.NeoForgeRegistries` | Same | AttachmentType, DataSerializer |

### Recipe-Related Imports

| Old | New | Notes |
|---|---|---|
| `net.minecraft.world.item.crafting.CraftingBookCategory` | Not needed (removed from recipe) | Simplified |
| `net.minecraft.world.item.ItemStack.STRICT_CODEC` | `net.minecraft.world.item.ItemStackTemplate.CODEC` | Item stack codec |
| `net.minecraft.world.item.crafting.RecipeSerializer` | Same | Constructor pattern changed |
| `net.minecraft.world.item.crafting.RecipeBookCategory` | Same | Now a DeferredRegister entry |
| `net.minecraft.world.item.crafting.RecipeType` | Same | Unchanged |

### Loot-Related Imports

| Old | New | Notes |
|---|---|---|
| `net.minecraft.world.level.storage.loot.functions.LootItemFunctionType` | `com.mojang.serialization.MapCodec` | Direct registration |
| `net.minecraft.world.level.storage.loot.predicates.LootItemConditionType` | `com.mojang.serialization.MapCodec` | Direct registration |
| `net.minecraft.world.level.storage.loot.LootParams` | Same | Unchanged |
| `net.minecraft.world.level.storage.loot.parameters.LootContextParams` | Same | Unchanged |

### Damage-Related Imports

| Old | New | Notes |
|---|---|---|
| `net.minecraft.core.Registry` | `net.minecraft.core.HolderLookup` | Via `lookupOrThrow()` |
| `registryAccess().registryOrThrow()` | `registryAccess().lookupOrThrow()` | Method name change |
| `damageTypes.getHolderOrThrow()` | `damageTypes.getOrThrow()` | Method name change |

---

## 10. Other Notable API Changes

### 10.1 `Level` — Field Access to Method Access

| Old | New |
|---|---|
| `level.isClientSide` | `level.isClientSide()` |
| `level.registryAccess()` | `level.registryAccess()` (same) |
| `maid.level` | `maid.level()` |
| `player.level` | `player.level()` |

### 10.2 `ItemHandlerHelper` → `Inventory` Method

**OLD:**
```java
ItemHandlerHelper.giveItemToPlayer(player, stack)
```

**NEW:**
```java
player.getInventory().placeItemBackInInventory(stack)
```

### 10.3 `maid.moveTo()` → `maid.snapTo()`

`Entity.moveTo()` was renamed to `Entity.snapTo()` in 1.21.1.

### 10.4 `Identifier` vs `ResourceLocation`

`ResourceLocation` class was renamed to `Identifier` in 1.21.1. Same methods:
- `Identifier.fromNamespaceAndPath(namespace, path)` — same as old `new ResourceLocation(namespace, path)` or `ResourceLocation.fromNamespaceAndPath()`
- Both implement the same interface

The old project already used `ResourceLocation.fromNamespaceAndPath()` which maps to `Identifier.fromNamespaceAndPath()`.

---

## 11. Summary of Affected Files

### Init/Registration Files
- `init/InitItems.java` — DeferredRegister.Items, component-based spawn eggs
- `init/InitBlocks.java` — DeferredRegister.Blocks
- `init/InitEntities.java` — EntityType.build(ResourceKey), removed Brain/Schedule registries
- `init/InitBrains.java` (NEW) — Memory/Sensor/Activity/EnvironmentAttribute registries
- `init/InitDataAttachment.java` (NEW) — AttachmentType registries (replaces EntityDataAccessor)
- `init/InitRecipes.java` — RecipeBookCategory, new RecipeSerializer constructor
- `init/InitDamage.java` — `lookupOrThrow()` / `getOrThrow()` API
- `init/InitLootModifier.java` — MapCodec direct registration
- `init/InitAttribute.java` — Largely unchanged

### Entity Files
- `entity/passive/EntityMaid.java` — Major refactor (Brain.Packed, AttachmentType, EntitySpawnReason, isClientSide())
- `entity/ai/brain/MaidBrain.java` — ActivityData.create() instead of brain.addActivity()
- `entity/data/*.java` (NEW) — 7 record classes with AttachmentType definitions
- `entity/item/*.java` — ValueInput/ValueOutput save/load, EntitySpawnReason

### Item Files
- `item/*.java` — Identifier constructor, .setId(), InteractionResult, appendHoverText Consumer

### Block/BE Files
- `block/*.java` — Identifier constructor, .setId()
- `tileentity/*.java` — ValueInput/ValueOutput save/load

### Crafting/Loot Files
- `crafting/AltarRecipeSerializer.java` — new RecipeSerializer(codec, streamCodec) constructor
- `crafting/AltarRecipe.java` — Removed group/category, ItemStackTemplate
- `loot/*.java` — MapCodec-based loot type
