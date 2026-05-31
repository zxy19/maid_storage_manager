# Items, Blocks, Inventory & Crafting Migration

## Overview

This document covers the migration of items, blocks, block entities, inventory/container systems, and crafting recipes from the old Forge-based project to the new NeoForge 26.1-based project.

---

## Item Changes

### Added Items (NEW only)

| Item | Class | Description |
|------|-------|-------------|
| ItemScarecrow | `item/ItemScarecrow.java` | BlockItem for the new Scarecrow double-block entity |
| ItemSnackCabinet | `item/ItemSnackCabinet.java` | BlockItem for the new SnackCabinet block |

### Removed Items (OLD only)

| Item | Class | Reason |
|------|-------|--------|
| ItemBoardState | `item/ItemBoardState.java` | Board game state storage (Gomoku/CChess/WChess boards). Consolidated into the block entities directly in new version. |
| ItemEntityPlaceholder | `item/ItemEntityPlaceholder.java` | Creative-mode altar recipe preview item. Removed in new version. |
| ItemTankBackpack | `item/ItemTankBackpack.java` | Tank backpack item extending `ItemMaidBackpack`. Consolidated into the backpack data system. |
| ItemMonsterList | `item/ItemMonsterList.java` | Already `@Deprecated(since = "1.1.13")` in old. Fully removed. |
| ItemChair | `item/ItemChair.java` (old only - NEW merged as ItemChair) | Chair block item - still exists but with new constructor pattern |
| ItemChairShow | `item/ItemChairShow.java` (old only) | Chair showcase item - still exists but with new constructor pattern |

### Item Status Table (Complete)

| Item | OLD | NEW | Changed? | Notes |
|------|-----|-----|----------|-------|
| AbstractStoreMaidItem | ✅ | ✅ | Major | Constructor unchanged (takes Properties), but internal methods use ValueInput/ValueOutput |
| BackpackLevel | ✅ | ✅ | Minor | Enum migrated |
| ItemAdvancementIcon | ✅ | ✅ | Constructor | Now takes `Identifier id` |
| ItemBoardState | ✅ | ❌ | **REMOVED** | Board state storage consolidated |
| ItemBroom | ✅ | ✅ | Constructor | Now takes `Identifier id` |
| ItemCamera | ✅ | ✅ | Major API | `use()` returns `InteractionResult`; `appendHoverText()` uses `Consumer`; NBT uses `ValueInput`/`ValueOutput` |
| ItemChair | ✅ | ✅ | Constructor | Now takes `Identifier id` |
| ItemChairShow | ✅ | ✅ | Constructor | Now takes `Identifier id` |
| ItemChisel | ✅ | ✅ | Constructor | Now takes `Identifier id` |
| ItemDamageableBauble | ✅ | ✅ | Constructor | Now takes `Identifier id, int durability`; `.setNoRepair()` removed |
| ItemEntityIdCopy | ✅ | ✅ | Constructor | Now takes `Identifier id` |
| ItemEntityPlaceholder | ✅ | ❌ | **REMOVED** | Altar recipe preview removed |
| ItemExtinguisher | ✅ | ✅ | Constructor/Sig | Now takes `Identifier id`; `use()` returns `InteractionResult` |
| ItemFairySpawnEgg | ✅ | ✅ | Constructor | Now takes `Identifier id` |
| ItemFavorabilityTool | ✅ | ✅ | Constructor | Now takes `Identifier id` |
| ItemFilm | ✅ | ✅ | Constructor | Now takes `Identifier id` |
| ItemFoxScroll | ✅ | ✅ | Constructor | Now takes `Identifier id` |
| ItemGarageKit | ✅ | ✅ | Constructor | Now takes `Identifier id` |
| ItemHakureiGohei | ✅ | ✅ | Constructor | Now takes `Identifier id` |
| ItemKappaCompass | ✅ | ✅ | Constructor | Now takes `Identifier id` |
| ItemMaidBackpack | ✅ | ✅ | Constructor | Now takes `Identifier id` |
| ItemMaidBeacon | ✅ | ✅ | Constructor | Now takes `Identifier id` |
| ItemMaidBed | ✅ | ✅ | Constructor | Now takes `Identifier id` |
| ItemModelSwitcher | ✅ | ✅ | Constructor | Now takes `Identifier id` |
| ItemMonsterList | ✅ | ❌ | **REMOVED** | Already deprecated in old |
| ItemNormalBauble | ✅ | ✅ | Constructor | Now takes `Identifier id` |
| ItemPhoto | ✅ | ✅ | Constructor | Now takes `Identifier id` |
| ItemPicnicBasket | ✅ | ✅ | Major API | Now takes `Identifier id, Block block`; uses `ItemStacksResourceHandler` instead of `ItemStackHandler`; `.overrideDescription()` |
| ItemPowerPoint | ✅ | ✅ | Constructor | Now takes `Identifier id` |
| ItemScarecrow | ❌ | ✅ | **NEW** | BlockItem for new Scarecrow |
| ItemServantBell | ✅ | ✅ | Constructor | Now takes `Identifier id` |
| ItemSmartSlab | ✅ | ✅ | Constructor | Now takes `Identifier id` |
| ItemSnackCabinet | ❌ | ✅ | **NEW** | BlockItem for new SnackCabinet |
| ItemSubstituteJizo | ✅ | ✅ | Constructor | Now takes `Identifier id` |
| ItemTankBackpack | ✅ | ❌ | **REMOVED** | Consolidated into backpack data |
| ItemTrumpet | ✅ | ✅ | Constructor | Now takes `Identifier id` |
| ItemWirelessIO | ✅ | ✅ | Constructor | Now takes `Identifier id` |

### Universal Item Constructor Change

**OLD pattern:**
```java
public ItemCamera() {
    super((new Properties()).stacksTo(1).durability(50));
}
```

**NEW pattern:**
```java
public ItemCamera(Identifier id) {
    super((new Properties())
            .stacksTo(1)
            .durability(50)
            .setId(ResourceKey.create(Registries.ITEM, id)));
}
```

All item constructors now require an `Identifier id` parameter and call `.setId(ResourceKey.create(Registries.ITEM, id))` on the Properties builder. This is a NeoForge 26.1 requirement for proper registry integration.

### Key API Changes (Items)

| API | OLD (Forge) | NEW (NeoForge 26.1) |
|-----|-------------|------------------------|
| `use()` return type | `InteractionResultHolder<ItemStack>` | `InteractionResult` |
| `appendHoverText()` | `List<Component> tooltip` param | `TooltipDisplay display, Consumer<Component> tooltip` params |
| Entity save | `maid.saveWithoutId(CompoundTag tag)` | `maid.saveWithoutId(TagValueOutput)` + `output.buildResult()` |
| Entity load | `maid.load(CompoundTag)` / `maid.readAdditionalSaveData(CompoundTag)` | `maid.load(ValueInput)` / `maid.readAdditionalSaveData(ValueInput)` |
| UUID read from NBT | `tag.getUUID(key)` | `tag.read(UUIDUtil.CODEC.fieldOf(key))` |
| Give item to player | `ItemHandlerHelper.giveItemToPlayer(player, stack)` | `player.getInventory().placeItemBackInInventory(stack)` |
| `sidedSuccess()` | `InteractionResultHolder.sidedSuccess()` | `InteractionResult.SUCCESS` (no sided variant needed) |
| `level.isClientSide` (field) | `worldIn.isClientSide` | `worldIn.isClientSide()` (method) |
| `maid.moveTo()` | Moves entity to pos | `maid.snapTo()` (new 1.21.1 method) |

---

## Bauble Changes

### Bauble Files (Both versions identical set)

All 12 bauble implementations exist in both old and new:
- `BaubleManager.java` — REGISTRY-SIDE: Unchanged (uses same `DeferredHolder<Item, Item>` API)
- `DrownProtectBauble.java`
- `ExplosionProtectBauble.java`
- `ExtraLifeBauble.java`
- `FallProtectBauble.java`
- `FireProtectBauble.java`
- `ItemMagnetBauble.java`
- `MagicProtectBauble.java`
- `MuteBauble.java`
- `NimbleFabricBauble.java`
- `ProjectileProtectBauble.java`
- `UndyingTotemBauble.java`
- `WirelessIOBauble.java`

The `BaubleManager` class is identical between old and new — no migration needed.

---

## Block & BlockEntity Changes

### Block Status Table

| Block | OLD | NEW | Superclass Change? | Notes |
|-------|-----|-----|-------------------|-------|
| BlockAltar | ✅ | ✅ | No (Block + EntityBlock) | Major internal API changes (see below) |
| BlockBookshelf | ✅ | ✅ | No (BaseEntityBlock) | API migration |
| BlockCChess | ✅ | ✅ | No (BaseEntityBlock) | API migration |
| BlockComputer | ✅ | ✅ | No (BaseEntityBlock) | API migration |
| BlockGarageKit | ✅ | ✅ | No (Block + EntityBlock) | Removed IClientBlockExtensions; new `getDrops()` pattern |
| BlockGomoku | ✅ | ✅ | No (BaseEntityBlock) | API migration |
| BlockJoy | ✅ | ✅ | No (BaseEntityBlock) | API migration |
| BlockKeyboard | ✅ | ✅ | No (BaseEntityBlock) | API migration |
| BlockMaidBeacon | ✅ | ✅ | No (BaseEntityBlock) | API migration |
| BlockMaidBed | ✅ | ✅ | No (BedBlock) | API migration |
| BlockModelSwitcher | ✅ | ✅ | No (BaseEntityBlock) | API migration |
| BlockPicnicMat | ✅ | ✅ | No (BaseEntityBlock) | API migration |
| BlockScarecrow | ❌ | ✅ | HorizontalDirectionalBlock | **NEW** double-block entity |
| BlockShrine | ✅ | ✅ | No (BaseEntityBlock) | API migration |
| BlockSnackCabinet | ❌ | ✅ | BaseEntityBlock | **NEW** with `MapCodec` |
| BlockStatue | ✅ | ✅ | No (BaseEntityBlock) | API migration |
| BlockWChess | ✅ | ✅ | No (BaseEntityBlock) | API migration |

### Universal Block Constructor Change

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

### Key Block API Changes

| API | OLD | NEW |
|-----|-----|-----|
| `useItemOn()` return | `ItemInteractionResult` | `InteractionResult` |
| `onBlockExploded()` level param | `Level world` | `ServerLevel world` |
| `getCloneItemStack()` sig | `(BlockState, HitResult, LevelReader, BlockPos, Player)` | `(LevelReader, BlockPos, BlockState, boolean includeData, Player)` |
| `playerWillDestroy()` | return `BlockState` | return `BlockState` (unchanged) |
| Drop handling (GarageKit) | `onRemove()` override | `getDrops(LootParams.Builder)` |
| Client extensions | `IClientBlockExtensions` static field | Removed entirely (Altar, GarageKit) |
| `codec()` method | Not required | Required: `protected MapCodec<?> codec()` for all blocks |
| Recipe access | `world.getRecipeManager().getRecipeFor()` | `serverLevel.recipeAccess().getRecipeFor()` |
| Item handler access | `handler.getStackInSlot(0)` | `ItemUtil.getStack(handler, 0)` |
| Item extract | `handler.extractItem(0, 1, false)` | `ItemsUtil.extractItem(handler, 0, 1, false, null)` |
| Item set | `handler.setStackInSlot(0, stack)` | `ItemsUtil.setStackInSlot(handler, 0, stack)` |
| Item empty | `handler.setStackInSlot(0, ItemStack.EMPTY)` | `handler.set(0, ItemResource.EMPTY, 0)` |

### New Blocks: Details

#### BlockScarecrow (`block/BlockScarecrow.java`)
- Extends `HorizontalDirectionalBlock` (not BaseEntityBlock — has no TE)
- Uses `DoubleBlockHalf` for upper/lower block
- Has `MapCodec<BlockScarecrow> CODEC` and `codec()` override
- `updateShape()` handles neighbor changes for double-block integrity
- `playerWillDestroy()` prevents double drops in creative mode
- Associated item: `ItemScarecrow`

#### BlockSnackCabinet (`block/BlockSnackCabinet.java`)
- Extends `BaseEntityBlock` + `EntityBlock`
- Has `MapCodec<BlockSnackCabinet> CODEC` and `codec()` override
- Has `FACING` (horizontal direction) and `TYPE` (0=none, 1=full, 2=half) block states
- `updateShape()` checks for half/full blocks above
- `useItemOn()` passes through if holding BlockItem and clicking top face
- Has `hasAnalogOutputSignal()` and `getAnalogOutputSignal()` for comparator support
- Has `tick()` that calls `recheckOpen()` on TE
- Associated TE: `TileEntitySnackCabinet`
- Associated item: `ItemSnackCabinet`

### MultiBlock System (unchanged)

Both versions have:
- `multiblock/MultiBlockAltar.java`
- `multiblock/MultiBlockManager.java`

### Properties (unchanged)

Both versions have:
- `properties/GomokuPart.java`
- `properties/PicnicMatPart.java`

---

## Tile Entity (BlockEntity) Changes

### TE Status Table

| TileEntity | OLD | NEW | Notes |
|------------|-----|-----|-------|
| TileEntityAltar | ✅ | ✅ | Major internal API changes |
| TileEntityBookshelf | ✅ | ✅ | NBT migration |
| TileEntityCChess | ✅ | ✅ | NBT migration |
| TileEntityComputer | ✅ | ✅ | NBT migration |
| TileEntityGarageKit | ✅ | ✅ | Major NBT migration |
| TileEntityGomoku | ✅ | ✅ | NBT migration |
| TileEntityJoy | ✅ | ✅ | NBT migration |
| TileEntityKeyboard | ✅ | ✅ | NBT migration |
| TileEntityMaidBeacon | ✅ | ✅ | NBT migration |
| TileEntityMaidBed | ✅ | ✅ | NBT migration |
| TileEntityModelSwitcher | ✅ | ✅ | NBT migration |
| TileEntityPicnicMat | ✅ | ✅ | NBT migration |
| TileEntityShrine | ✅ | ✅ | NBT migration |
| TileEntitySnackCabinet | ❌ | ✅ | **NEW** - for SnackCabinet block |
| TileEntityStatue | ✅ | ✅ | NBT migration |
| TileEntityWChess | ✅ | ✅ | NBT migration |

### Universal BlockEntity Changes

**Constructor — OLD:**
```java
public static final BlockEntityType<TileEntityAltar> TYPE = BlockEntityType.Builder.of(
    TileEntityAltar::new, InitBlocks.ALTAR.get()
).build(null);

public TileEntityAltar(BlockPos blockPos, BlockState blockState) {
    super(TYPE, blockPos, blockState);
}
```

**Constructor — NEW:**
```java
// TYPE is registered as DeferredRegister in InitBlocks:
// public static final DeferredRegister<BlockEntityType<?>> TILE_ENTITIES = ...
// public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityAltar>> ALTAR_TE = ...

public TileEntityAltar(BlockPos blockPos, BlockState blockState) {
    super(InitBlocks.ALTAR_TE.get(), blockPos, blockState);
}
```

### NBT Save/Load Migration

**OLD (`CompoundTag`-based):**
```java
@Override
public void saveAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
    getPersistentData().putBoolean(IS_RENDER, isRender);
    getPersistentData().putInt(STORAGE_STATE_ID, Block.getId(storageState));
    getPersistentData().put(STORAGE_ITEM, handler.serializeNBT(pRegistries));
    super.saveAdditional(pTag, pRegistries);
}

@Override
public void loadAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
    super.loadAdditional(pTag, pRegistries);
    isRender = getPersistentData().getBoolean(IS_RENDER);
    storageState = Block.stateById(getPersistentData().getInt(STORAGE_STATE_ID));
    handler.deserializeNBT(pRegistries, getPersistentData().getCompound(STORAGE_ITEM));
}
```

**NEW (`ValueInput`/`ValueOutput`-based):**
```java
@Override
public void saveAdditional(ValueOutput output) {
    output.putBoolean(IS_RENDER, isRender);
    output.putInt(STORAGE_STATE_ID, Block.getId(storageState));
    output.putChild(STORAGE_ITEM, handler);  // ItemStacksResourceHandler auto-serializes
    output.store(DIRECTION, Direction.CODEC, direction);  // No built-in putString
    output.putChild(STORAGE_BLOCK_LIST, blockPosList);  // PosListData serializes via Codec
    super.saveAdditional(output);
}

@Override
public void loadAdditional(ValueInput input) {
    super.loadAdditional(input);
    isRender = input.getBooleanOr(IS_RENDER, false);
    storageState = Block.stateById(input.getIntOr(STORAGE_STATE_ID, Block.getId(Blocks.AIR.defaultBlockState())));
    input.readChild(STORAGE_ITEM, handler);  // Auto-deserialize
    direction = input.read(DIRECTION, Direction.CODEC).orElse(Direction.SOUTH);
    input.readChild(STORAGE_BLOCK_LIST, blockPosList);
}
```

### Key TE API Changes

| API | OLD | NEW |
|-----|-----|-----|
| Save NBT | `saveAdditional(CompoundTag, HolderLookup.Provider)` | `saveAdditional(ValueOutput)` |
| Load NBT | `loadAdditional(CompoundTag, HolderLookup.Provider)` | `loadAdditional(ValueInput)` |
| Persistent data | `getPersistentData()` | No longer available — directly use output/input |
| Item handler type | `ItemStackHandler` | `ItemStacksResourceHandler` |
| Handler serialize | `handler.serializeNBT(reg)` / NBTTag | `output.putChild(key, handler)` |
| Handler deserialize | `handler.deserializeNBT(reg, NBTTag)` | `input.readChild(key, handler)` |
| Direction serialize | `direction.getSerializedName()` (String) | `output.store(key, Direction.CODEC, direction)` |
| Direction deserialize | `Direction.byName(string)` | `input.read(key, Direction.CODEC)` |
| Block removal | Override `onRemove()` in Block | Override `preRemoveSideEffects()` in BE |
| `getWorldPosition()` | `this.worldPosition` | `this.worldPosition` (unchanged field) |

### New TileEntitySnackCabinet

Stores snack cabinet inventory using `ItemStacksResourceHandler`. Follows the new `ValueInput`/`ValueOutput` NBT pattern. Includes `recheckOpen()` for container open/close sound handling.

---

## Inventory / Container Changes

### File Changes Summary

| File | OLD | NEW | Notes |
|------|-----|-----|-------|
| `handler/MaidHandsInvWrapper.java` | ✅ | ❌ | **REMOVED** — unused wrapper |
| `tooltip/BoardStateTooltip.java` | ✅ | ❌ | **REMOVED** — board game tooltip rendering |
| `tooltip/YsmMaidInfo.java` | ✅ | ❌ | **REMOVED** — YSM rendering consolidated |
| `handler/BaubleItemHandler.java` | ✅ | ✅ | Changed: `ItemStackHandler` → `ItemStacksResourceHandler` |
| `handler/MaidBackpackHandler.java` | ✅ | ✅ | Changed: `ItemStackHandler` → `ItemStacksResourceHandler` |
| `handler/AltarItemHandler.java` | ✅ | ✅ | Changed: `ItemStackHandler` → `ItemStacksResourceHandler` |
| `handler/MaidInvWrapper.java` | ✅ | ✅ | Changed: wraps new transfer API |
| `tooltip/ItemContainerTooltip.java` | ✅ | ✅ | Changed: uses `ItemStacksResourceHandler` |
| `tooltip/ItemMaidTooltip.java` | ✅ | ✅ | Changed: removed `YsmMaidInfo` constructor param |
| `container/MaidMainContainer.java` | ✅ | ✅ | Changed: `SlotItemHandler` → `ResourceHandlerSlot` |
| `container/AbstractMaidContainer.java` | ✅ | ✅ | API migration |
| `chest/*` | ✅ | ✅ | 4 files in both, API migration |

### Handler System Migration

The biggest inventory change is the move from Forge's `ItemStackHandler`/`IItemHandler` to NeoForge's transfer API:

| Concept | OLD (Forge Capability) | NEW (NeoForge Transfer API) |
|---------|------------------------|---------------------------|
| Handler base class | `ItemStackHandler` | `ItemStacksResourceHandler` |
| Capability type | `IItemHandler` | `ResourceHandler<ItemResource>` |
| Get stack | `handler.getStackInSlot(slot)` | `ItemUtil.getStack(handler, slot)` |
| Set stack | `handler.setStackInSlot(slot, stack)` | `handler.set(slot, ItemResource.of(stack), count)` |
| Extract item | `handler.extractItem(slot, amount, simulate)` returns `ItemStack` | `handler.extract(slot, resource, amount, tx)` returns `int` |
| Insert item | `handler.insertItem(slot, stack, simulate)` returns `ItemStack` | `handler.insert(slot, resource, amount, tx)` returns `int` |
| Slots count | `handler.getSlots()` | `handler.size()` |
| Validity check | `handler.isItemValid(slot, stack)` | `handler.isValid(slot, ItemResource)` |
| Content changed | `onContentsChanged(int slot)` | `onContentsChanged(int slot, ItemStack previousStack)` |
| Serialize | `handler.serializeNBT(registries)` | Auto via `ValueOutput.putChild()` |

### Container System Migration

**OLD:**
```java
IItemHandler handler = maid.getCapability(InitCapabilities.HAND_ITEM, Direction.DOWN);
addSlot(new SlotItemHandler(handler, 0, 87, 77) {
    @Override
    @OnlyIn(Dist.CLIENT)
    public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
        return Pair.of(BLOCK_ATLAS, EMPTY_MAINHAND_SLOT);
    }
});
```

**NEW:**
```java
ResourceHandler<ItemResource> capability = maid.getCapability(InitCapabilities.HAND_ITEM, Direction.DOWN);
var indexModifier = ItemsUtil.createIndexModifier(capability);
addSlot(new ResourceHandlerSlot(capability, indexModifier, 0, 87, 77) {
    @Override
    public Identifier getNoItemIcon() {
        return EMPTY_MAINHAND_SLOT;
    }
});
```

### Container API Changes

| API | OLD | NEW |
|-----|-----|-----|
| Slot class | `SlotItemHandler` | `ResourceHandlerSlot` |
| Slot construction | `SlotItemHandler(handler, index, x, y)` | `ResourceHandlerSlot(handler, indexModifier, index, x, y)` |
| Empty icon return | `Pair<ResourceLocation, ResourceLocation>` | `Identifier` |
| Empty icon texture | `"item/empty_slot_sword"` | `"container/slot/sword"` |
| Index mapping | Direct | `ItemsUtil.createIndexModifier(capability)` |
| `BackpackSlot.create()` | Direct new (old) | Static factory `BackpackSlot.create()` (new) |
| `BLOCK_ATLAS` | Used in `Pair.of(BLOCK_ATLAS, ...)` | Not needed (return single `Identifier`) |

---

## Crafting Recipe Changes

### File Changes

| File | OLD | NEW | Notes |
|------|-----|-----|-------|
| `AltarRecipe.java` | ✅ | ✅ | Completely rewritten |
| `AltarRecipeSerializer.java` | ✅ | ✅ | Completely rewritten |
| `FallbackIngredient.java` | ✅ | ❌ | **REMOVED** |

### AltarRecipe: Complete Rewrite

**OLD:** `extends ShapelessRecipe`, used `NonNullList<Ingredient>`, `ResourceLocation entityType`
**NEW:** `implements Recipe<CraftingInput>`, uses `List<Ingredient>`, `Identifier entityType`

| Aspect | OLD | NEW |
|--------|-----|-----|
| Superclass | `extends ShapelessRecipe` | `implements Recipe<CraftingInput>` |
| Result type | `ItemStack result` | `ItemStackTemplate result` |
| Entity type | `ResourceLocation entityType` | `Identifier entityType` |
| Group | `String group` constructor param | `group()` returns `""` (not needed) |
| Category | `CraftingBookCategory category` | Removed from recipe, uses `RecipeBookCategory` |
| Ingredients | `NonNullList<Ingredient>` | `List<Ingredient>` |
| `matches()` | Inherited from ShapelessRecipe | Custom implementation with `RecipeMatcher.findMatches()` |
| `assemble()` | Inherited | New: `result.create()` |
| `placementInfo()` | N/A (inherited) | Must implement: `PlacementInfo.NOT_PLACEABLE` |
| `showNotification()` | N/A | Must implement: returns `false` |
| `isSpecial()` | N/A | Must implement: returns `true` |
| `recipeBookCategory()` | N/A | Must implement: returns `ALTAR_RECIPE_CATEGORY` |
| Entity spawn | `MobSpawnType.EVENT / SPAWN_EGG` | `EntitySpawnReason.EVENT / SPAWN_ITEM_USE` |
| Mob finalize | `mob.finalizeSpawn()` | `EventHooks.finalizeMobSpawn()` |
| Maid NBT load | `CompoundTag` → `maid.readAdditionalSaveData()` | `TagValueInput` → `maid.readAdditionalSaveData(input)` |
| `EntityType.create()` | `EntityType.create(CompoundTag, Level)` | `EntityType.create(ValueInput, Level, EntitySpawnReason)` |

### AltarRecipeSerializer: Complete Rewrite

**OLD:** `implements RecipeSerializer<AltarRecipe>` with `codec()` and `streamCodec()` methods
**NEW:** Class with static `CODEC`, `STREAM_CODEC`, and `SERIALIZER` fields — no interface implementation

| Aspect | OLD | NEW |
|--------|-----|-----|
| Class declaration | `implements RecipeSerializer<AltarRecipe>` | Plain class, no interface |
| Serializer instance | `this` (implements interface) | `new RecipeSerializer<>(CODEC, STREAM_CODEC)` |
| Group field | ✅ (optional `String`) | ❌ Removed |
| Category field | ✅ `CraftingBookCategory` | ❌ Removed |
| Result codec | `ItemStack.STRICT_CODEC` | `ItemStackTemplate.CODEC` |
| Ingredients limit | Manual validation (max 6) | `Ingredient.CODEC.sizeLimitedListOf(MAX_INGREDIENTS)` |
| `ResourceLocation` codec | `ResourceLocation.CODEC` | `Identifier.CODEC` |
| Max ingredients | Inline check in `checkIngredients()` | `MAX_INGREDIENTS = 6` constant |

### Registry Names

| Registry Key | OLD | NEW |
|-------------|-----|-----|
| RecipeType | `InitRecipes.ALTAR_CRAFTING` | `InitRecipes.ALTAR_RECIPE` |
| RecipeSerializer | `InitRecipes.ALTAR_RECIPE_SERIALIZER` | `InitRecipes.ALTAR_RECIPE_SERIALIZER` (unchanged name) |
| Category | N/A | `InitRecipes.ALTAR_RECIPE_CATEGORY` (new) |

---

## Component / DataComponent Changes (Minecraft 1.21.1)

### ItemStack NBT → DataComponent

In Minecraft 1.21.1, item NBT data is replaced by the **DataComponent** system. Instead of `stack.getTag()` / `stack.setTag()`, components are accessed by type:

| OLD (NBT) | NEW (DataComponent) |
|-----------|---------------------|
| `stack.getOrCreateTag().put(key, value)` | `stack.set(COMPONENT_TYPE, value)` |
| `stack.getTag().get(key)` | `stack.get(COMPONENT_TYPE)` |
| `CustomData.of(compoundTag)` | Same: `CustomData.of(compoundTag)` — still used for custom NBT |
| `item.getDescriptionId()` | `.overrideDescription("translation.key")` in Properties |

### Custom Data Components (InitDataComponent)

The mod defines its own data components for storing mod-specific data:
- `MAID_INFO` — stores maid entity NBT as `CustomData`
- `RECIPES_ID_TAG` — altar recipe identifier
- `BOARD_STATE_TAG` — board game state (OLD only, used by removed ItemBoardState)
- `TANK_BACKPACK_TAG` — tank backpack fluid data (OLD only)
- `MODEL_ID_TAG_NAME` — maid model ID
- `ENTITY_ID_TAG_NAME` — entity type ID

### `appendHoverText()` Signature

**OLD:**
```java
public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag)
```

**NEW:**
```java
public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag)
```

Key difference: `tooltip` is now a `Consumer<Component>` that you call `tooltip.accept(component)` on, instead of adding to a `List<Component>`.

### `getTooltipImage()` — Unchanged

Both old and new use same `Optional<TooltipComponent> getTooltipImage(ItemStack stack)` signature.

### AbstractStoreMaidItem Changes

In the new version, `AbstractStoreMaidItem.getTooltipImage()` no longer includes `YsmMaidInfo`:
- **OLD:** Creates `ItemMaidTooltip(modelId, customName, ysmMaidInfo)` with YSM rendering data
- **NEW:** Creates `ItemMaidTooltip(modelId, customName)` without YSM data

---

## Summary of Migration Patterns

### Pattern 1: `.setId()` on all registrable objects
All Items, Blocks, and other registry objects now require `.setId(ResourceKey.create(Registry, identifier))` in their Properties builder.

### Pattern 2: `InteractionResult` vs `InteractionResultHolder`
`Item.use()` now returns `InteractionResult` (not `InteractionResultHolder<ItemStack>`). Success cases return `InteractionResult.SUCCESS`.

### Pattern 3: `ValueInput`/`ValueOutput` instead of `CompoundTag` for serialization
Entity save/load and BlockEntity save/load use the new `ValueInput`/`ValueOutput` system with Codecs instead of raw CompoundTag manipulation.

### Pattern 4: `ItemStacksResourceHandler` replaces `ItemStackHandler`
All inventory handlers now extend NeoForge's transfer API base class with `ItemUtil` static helpers.

### Pattern 5: `Recipe<>` interface replaces `extends ShapelessRecipe`
Custom recipes must directly implement `Recipe<CraftingInput>` with all required methods.

### Pattern 6: `EntitySpawnReason` replaces `MobSpawnType`
All entity spawning uses the new enum.

### Pattern 7: `Identifier` replaces `ResourceLocation`
`ResourceLocation` → `Identifier` throughout (NeoForge/Mojang renaming).
