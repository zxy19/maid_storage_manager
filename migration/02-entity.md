# Entity Package Migration

## Architecture Overview

### Major Architectural Change: EntityMaid → Manager Delegation Pattern

The most significant change in this migration is the complete refactoring of `EntityMaid` from a monolithic class (**~2828 lines**) into a lean delegation-based class (**~945 lines**) with 17+ specialized manager classes.

| Aspect | Old (NeoForge 21.1) | New (NeoForge 26.1) |
|--------|---------------------|------------------------|
| EntityMaid superclass | `TamableAnimal implements CrossbowAttackMob, IMaid` | `TamableAnimal implements CrossbowAttackMob, MaidAnimationManager.View, MaidConfigManager.View, ...` (17 View interfaces) |
| EntityMaid lines | ~2828 | ~945 |
| Data storage | `EntityDataAccessor` (30+ fields) + `CompoundTag` | `AttachmentType<T>` with record classes (7 records) |
| Serialization format | `CompoundTag` (NBT) | `ValueInput`/`ValueOutput` (NeoForge codec-based) |
| Brain creation | `makeBrain(Dynamic<?>)` / `brainProvider()` | `makeBrain(Brain.Packed)` / `BRAIN_PROVIDER` constant |
| Entity type ID | `ResourceLocation("maid")` via `.build("maid")` | `Identifier.fromNamespaceAndPath` via `ResourceKey.create` |
| Tick methods | `tick()`, `baseTick()`, `aiStep()`, `customServerAiStep()` | `tick()`, `baseTick()`, `aiStep()`, `customServerAiStep(ServerLevel level)` |
| YSM compat | Inline in EntityMaid (~100 lines) | Removed |

### Manager Delegation Pattern

Each manager class in the new code implements an inner `View` interface that EntityMaid implements. Managers access shared data through AttachmentType-based data records via the `EntityMaid.getData()` / `EntityMaid.setData()` bridge methods.

```
EntityMaid
├── MaidProfileManager ← ProfileData
├── MaidTaskManager ← TaskData
├── MaidStatsManager ← StatsData
├── MaidItemManager ← inventory internals
├── MaidParticleManager
├── MaidWorldInteractionManager
├── MaidTeleportManager
├── MaidAnimationManager ← AnimationData
├── MaidConfigManager ← ConfigData
├── MaidGameManager ← GameData
├── MaidBackpackManager ← BackpackData
├── MaidCombatManager
├── MaidDeathManager
├── MaidSoundManager
├── MaidClimbManager
├── MaidMiscManager
├── MaidKillRecordManager
├── MaidSwimManager
├── MaidNavigationManager
├── ChatBubbleManager
├── FavorabilityManager
└── MaidAIChatManager
```

---

## File Changes

### New Files (entity/passive/)

| File | Description |
|------|-------------|
| `MaidAnimationManager.java` | Animation states (begging, crossbow, swinging, aiming) using AnimationData |
| `MaidBackpackManager.java` | Backpack type and tick management |
| `MaidClimbManager.java` | Climbing logic (extracted from EntityMaid) |
| `MaidCombatManager.java` | Combat/shield/damage handling |
| `MaidDeathManager.java` | Death processing, tombstone, drop equipment |
| `MaidGameManager.java` | Game/board game data (renamed from MaidGameRecordManager) |
| `MaidItemManager.java` | Inventory management, item pickup, consume |
| `MaidMiscManager.java` | Misc: mobInteract, getTypeName, finalizeSpawn, thunderHit, leash |
| `MaidParticleManager.java` | Particle effects |
| `MaidProfileManager.java` | Model ID and sound pack ID management |
| `MaidSoundManager.java` | Sound event delegation |
| `MaidStatsManager.java` | Hunger/favorability/experience stats |
| `MaidTaskManager.java` | Task switching and home/restrict management |
| `MaidTeleportManager.java` | Portal and teleport handling |
| `MaidWorldInteractionManager.java` | Block interaction (destroy, place) |

### Removed Files (entity/passive/)

| File | Fate |
|------|------|
| `DefaultMaidSoundPack.java` | Merged into `ProfileData.getInitSoundPackId()` |
| `MaidGameRecordManager.java` | Renamed to `MaidGameManager` |

### entity/data/ Changes

| Old File | New File | Notes |
|----------|----------|-------|
| `MaidTaskDataMaps.java` | — | Removed; task data now stored via AttachmentType |
| `TaskDataRegister.java` | — | Removed; no longer needed with new data system |
| `inner/AttackListData.java` | — | Removed or moved elsewhere |
| — | `AnimationData.java` | Record: boolean flags (begging, chargingCrossbow, swingingArms, aiming) |
| — | `BackpackData.java` | Record: backpack type string |
| — | `ConfigData.java` | Record: boolean flags (pickup, homeMode, rideable, showBackpack, etc.) + pickupType + soundFreq |
| — | `GameData.java` | Record: winCounts map + gameStatue byte |
| — | `ProfileData.java` | Record: modelId + soundPackId with PECO randomization |
| — | `StatsData.java` | Record: hunger, favorability, experience, struckByLightning |
| — | `TaskData.java` | Record: taskId, schedule, restrictCenter, restrictRadius |

### entity/backpack/ Changes

| Old Files (Removed) | Reason |
|---------------------|--------|
| `CraftingTableBackpack.java` | Removed |
| `EnderChestBackpack.java` | Removed |
| `FurnaceBackpack.java` | Removed |
| `TankBackpack.java` | Removed |
| `data/FurnaceBackpackData.java` | Removed |
| `data/TankBackpackData.java` | Removed |

### entity/info/ Changes

| Old File | New File | Notes |
|----------|----------|-------|
| `models/ServerChairModels.java` | — | Removed; no longer tracked separately |
| — | `models/AbstractServerModels.java` | Generic base class for model info maps |
| `ServerCustomPackLoader.java` | `ServerCustomPackLoader.java` + `ServerCustomPackReader.java` + `ServerMaidModelPackLoader.java` | Split into 3 classes; old used `chatbubble` type adapter; new uses `IdentifierAdapter` |
| — | `ServerMaidModelPackLoader.java` | Package-private helper for loading maid model JSON |
| — | `ServerCustomPackReader.java` | Package-private helper for ZIP/directory pack scanning |

### entity/chatbubble/ Changes

| Old File | Fate |
|----------|------|
| `ChatBubbleManger.java` | Removed (was already `@Deprecated` typo class) |

---

## EntityMaid Changes: Method-by-Method Comparison

### Constructor & Initialization

| Feature | Old | New |
|---------|-----|-----|
| Inventory wrappers | Direct fields: `armorInvWrapper`, `handsInvWrapper`, `maidInv`, `maidBauble`, `hideInv`, `taskInv` | Delegated to `MaidItemManager` |
| Move control | `this.moveControl = new MaidMoveControl(this)` | Same |
| Navigation | `this.navigationManager = new MaidNavigationManager(this)` | Same |
| Swim manager | `this.swimManager = new MaidSwimManager(this)` | `this.swimManager = new MaidSwimManager(this)` (now declared as a manager field) |
| SchedulePos | `this.schedulePos = new SchedulePos(...)` | Not directly in EntityMaid (in TaskManager) |
| ItemCooldowns | `this.cooldowns = new ItemCooldowns()` | Not directly in EntityMaid |
| Backpack init | `backpack = BackpackManager.getEmptyBackpack()`, `backpackData = null` | Delegated to MaidBackpackManager |
| Task init | `task = TaskManager.getIdleTask()` | Delegated to MaidTaskManager |
| YSM model data | 4 synched data fields + 5 public fields | Removed |
| `canClimb` flag | Direct boolean field | Delegated to MaidClimbManager |
| `alreadyDropped` flag | Direct boolean field | In MaidDeathManager (or removed) |
| `backpackDelay` | Direct int field | In MaidBackpackManager |
| `playerHurtSoundCount`, `pickupSoundCount` | Direct int fields | In MaidSoundManager/MaidMiscManager |

### Synched Data (EntityDataAccessor)

**Old EntityMaid** registered 30+ `EntityDataAccessor` fields directly:
- `DATA_IS_YSM_MODEL`, `DATA_YSM_MODEL_ID`, `DATA_YSM_MODEL_TEXTURE`, `DATA_YSM_MODEL_NAME` (YSM compat — removed)
- `DATA_MODEL_ID`, `DATA_SOUND_PACK_ID` → Now in `ProfileData`
- `DATA_TASK` → Now in `TaskData`
- `DATA_BEGGING`, `DATA_IS_CHARGING_CROSSBOW`, `DATA_ARM_RISE`, `DATA_IS_AIMING` → Now in `AnimationData`
- `DATA_INVULNERABLE` → Remains directly on EntityMaid (`DATA_SYNC_INVULNERABLE`)
- `DATA_HUNGER`, `DATA_FAVORABILITY`, `DATA_EXPERIENCE`, `DATA_STRUCK_BY_LIGHTNING` → Now in `StatsData`
- `SCHEDULE_MODE`, `RESTRICT_CENTER`, `RESTRICT_RADIUS` → Now in `TaskData`
- `CHAT_BUBBLE` → Remains directly on EntityMaid
- `BACKPACK_TYPE`, `BACKPACK_ITEM_SHOW`, `BACKPACK_FLUID` → `BackpackData` + remaining direct fields
- `GAME_SKILL`, `GAME_STATUE` → Now in `GameData`
- `DATA_PICKUP`, `DATA_HOME_MODE`, `DATA_RIDEABLE`, `BACKPACK_SHOW`, `BACK_ITEM_SHOW`, `CHATBUBBLE_SHOW`, `SOUND_FREQ`, `PICKUP_TYPE`, `OPEN_DOOR`, `OPEN_FENCE_GATE`, `ACTIVE_CLIMBING` → Now in `ConfigData`
- `TASK_DATA_SYNC` → Removed (AttachmentType handles sync)

**New EntityMaid** registers only 3 synched data fields directly:
- `DATA_SYNC_INVULNERABLE`
- `BACKPACK_ITEM_SHOW`
- `CHAT_BUBBLE`

### Tick & Update Methods

| Old Method | New Equivalent |
|------------|---------------|
| `tick()` (YSMsync + backup inline) | Same, simpler (only MaidTickEvent + backup) |
| `baseTick()` (backpackDelay, playerHurtSound, climbFall, particles, health, sleep, syncData, gameRecord.tick) | `baseTick()` (delegates to: `backpackManager.tick()`, `soundManager.tick()`, `climbManager.tick()`, `particleManager.tick()`, `miscManager.tick()`, `gameManager.tick()`) |
| `aiStep()` (navigation, chatBubble, backpackData, favorability, schedulePos, cooldowns, shield) | `aiStep()` (delegates to: updateSwingTime, `navigationManager.tick()`, `chatBubbleManager.tick()`, `favorabilityManager.tick()`, `taskManager.tick()`, `combatManager.tick()`, `combatManager.aiStep()`) |
| `customServerAiStep()` (no param) | `customServerAiStep(ServerLevel level)` — signature change for 1.21.1 |

### Save/Load

| Aspect | Old | New |
|--------|-----|-----|
| Method signatures | `addAdditionalSaveData(CompoundTag)`, `readAdditionalSaveData(CompoundTag)` | `addAdditionalSaveData(ValueOutput)`, `readAdditionalSaveData(ValueInput)` |
| Model/sound data | Direct NBT writes | `statsManager.save(output)` → stores via AttachmentType |
| Inventory serialization | `maidInv.serializeNBT(registryAccess)` | `itemManager.save(output)` |
| YSM data | 7 NBT tags written/read | Removed |
| Backpack level migration | `BACKPACK_LEVEL_TAG` handling | Removed |
| Restrict center migration | `RESTRICT_CENTER_TAG` handling | Removed |
| Task data maps | `taskDataMaps.writeSaveData(compound)` / `readSaveData(compound)` | No longer needed |

### Combat & Damage

| Old Method | New Equivalent |
|------------|---------------|
| `isWithinMeleeAttackRange(LivingEntity)` | `combatManager.isWithinMeleeAttackRange(target)` |
| `doHurtTarget(Entity)` | `combatManager.doHurtTarget(level, target, super::doHurtTarget)` |
| `hurt(DamageSource, float)` (~120 lines) | `combatManager.hurtServer(level, source, amount, super::hurtServer)` |
| `actuallyHurt(DamageSource, float)` (~70 lines) | `combatManager.actuallyHurt(level, damageSrc, damageAmount)` — simplified, event wiring deferred |
| `performRangedAttack(LivingEntity, float)` | `combatManager.performRangedAttack(target, distanceFactor)` |
| Swipe attack `doSweepHurt()` | In MaidCombatManager |
| Shield blocking `canUseShield()`, `isBlocking()`, `blockUsingShield()`, `hurtCurrentlyUsedShield()` | In MaidCombatManager |
| `getProjectile(ItemStack)` | `combatManager.getProjectile(weaponStack)` |
| `changeDimension(DimensionTransition)` | `teleportManager.handlePortal()` |
| `thunderHit(ServerLevel, LightningBolt)` | `miscManager.thunderHit(world, lightning)` |

### Death & Drop

| Old Method | New Equivalent |
|------------|---------------|
| `die(DamageSource)` (bauble + event + clearFire + super + sendMaidPos) | `deathManager.die(cause, super::die)` |
| `dropEquipment()` (~50 lines, tombstone creation) | `deathManager.dropEquipment(level)` |
| `remove(RemovalReason)` | `deathManager.remove(reason)` + `super.remove(reason)` |
| `sendMaidPos()` (travel map death location) | Possibly removed or in MaidDeathManager |
| `alreadyDropped` flag | In MaidDeathManager |
| `destroyVanishingCursedItems()` | In MaidDeathManager |

### Inventory & Item Management

| Old Method | New Equivalent |
|------------|---------------|
| `pickupItem(ItemEntity, boolean)` | In MaidItemManager |
| `pickupXPOrb(ExperienceOrb)` | In MaidItemManager |
| `pickupPowerPoint(EntityPowerPoint)` | In MaidItemManager |
| `pickupArrow(AbstractArrow, boolean)` | In MaidItemManager |
| `pickupEntities()` logic in `pushEntities()` | `itemManager.pickupEntities()` in `pushEntities()` |
| `getAvailableInv(boolean)`, `getAllInv()`, `getAvailableBackpackInv()` | Delegated to MaidItemManager |
| `getMaidInv()`, `getMaidBauble()`, `getHandsInvWrapper()`, `getArmorInvWrapper()`, `getHideInv()`, `getTaskInv()` | Delegated to MaidItemManager |
| `completeUsingItem()` (swim reset + backCurrentHandItemStack) | Now: swim reset + `itemManager.backCurrentHandItemStack(this)` |
| `memoryHandItemStack(ItemStack)`, `backCurrentHandItemStack()` | In MaidItemManager |
| `updateUsingItem(ItemStack)` | `itemManager.updateUsingItem(usingItem)` |

### Task & Schedule

| Old Method | New Equivalent |
|------------|---------------|
| `getTask()` (parse + lookup from entityData) | `getTaskManager().getTask()` |
| `setTask(IMaidTask)` (comparison + set entityData + refreshBrain) | `getTaskManager().setTask(task)` |
| `getSchedule()`, `setSchedule(MaidSchedule)`, `getScheduleDetail()` | `getTaskManager().getSchedule()`, etc. |
| `isWithinRestriction()`, `isWithinRestriction(BlockPos)`, `restrictTo()`, `getRestrictCenter()`, `getRestrictRadius()`, `clearRestriction()`, `hasRestriction()` | `getTaskManager()` equivalents |
| `isWithinHome()`, `isWithinHome(BlockPos)`, `setHomeTo()`, `getHomePosition()`, `getHomeRadius()`, `hasHome()` | `getTaskManager()` equivalents (new naming: Home instead of Restriction) |
| `schedulePos` field | In MaidTaskManager/SchedulePos |
| `refreshBrain()` | Stays on EntityMaid (uses `Brain.Packed` instead of `Dynamic`) |

### Sounds

| Old Method | New Equivalent |
|------------|---------------|
| `playSound(SoundEvent, float, float)` (maid prefix routing + network packet) | `soundManager.playSound(soundEvent, volume, pitch)` — returns boolean for pass-through |
| `getAmbientSound()` | `soundManager.getAmbientSound()` |
| `getHurtSound(DamageSource)` (fire/player/default logic) | `soundManager.getHurtSound(damageSourceIn)` |
| `getDeathSound()` | `soundManager.getDeathSound()` |
| `tryPlayMaidPickupSound()` | In MaidItemManager or removed |
| `playerHurtSoundCount`, `pickupSoundCount` | In MaidSoundManager |

### Particle Effects

All previously inline particle methods (`spawnPortalParticle()`, `spawnRestoreHealthParticle()`, `spawnExplosionParticle()`, `spawnBubbleParticle()`, `spawnHeartParticle()`, `spawnRankUpParticle()`, `spawnSweepAttackParticle()`, `spawnItemParticles()`) are now in `MaidParticleManager`.

### Climbing

| Old Method | New Equivalent |
|------------|---------------|
| `onClimbable()` (~40 lines, path-based detection + ladder edge cases) | `climbManager.onClimbable(super::onClimbable)` |
| `handleOnClimbable(Vec3)` | `climbManager.handleOnClimbable(oriDelta)` |
| `handleRelativeFrictionAndCalculateMovement(Vec3, float)` | `climbManager.handleRelativeFrictionAndCalculateMovement(deltaMovement, friction)` |
| `travel(Vec3)` | `climbManager.travel(travelVector, super::travel)` |
| `canClimb` / `setCanClimb` / `isCanClimb` | In MaidClimbManager |

### Swimming

| Old Method | New Equivalent |
|------------|---------------|
| `getDefaultDimensions(Pose)` | Unchanged (still calls `swimManager.getSwimmingDimensions()`) |
| `updateSwimming()` | `swimManager.updateSwimming()` |
| `isPushedByFluid()` | Unchanged |
| `travel(Vec3)` | In MaidClimbManager |
| `completeUsingItem()` (swim reset) | `getSwimManager().resetEatBreatheItem()` |

### Interact & GUI

| Old Method | New Equivalent |
|------------|---------------|
| `mobInteract(Player, InteractionHand)` (~60 lines: fakePlayer check, event, interact, tameMaid) | `miscManager.mobInteract(playerIn, hand)` |
| `tameMaid(ItemStack, Player)` | In MaidMiscManager |
| `openMaidGui(Player)`, `openMaidGui(Player, int)`, `getGuiProvider(int)` | In MaidMiscManager |
| `getTypeName()` (event + YSM + model info) | `miscManager.getTypeName()` |
| `finalizeSpawn(ServerLevelAccessor, DifficultyInstance, MobSpawnType, SpawnGroupData)` | `miscManager.finalizeSpawn(...)` (param: `EntitySpawnReason` instead of `MobSpawnType`) |
| `setItemSlot(EquipmentSlot, ItemStack)` | Unchanged; still posts `MaidEquipEvent` directly |
| `onEquipItem(EquipmentSlot, ItemStack, ItemStack)` (netherite armor check + trigger) | `itemManager.onEquipItem(slot, oldItem, newItem)` |
| `eat(Level, ItemStack, FoodProperties)` | Not overridden/moved |

### Other Deletions & Changes

| Old Code | Fate |
|----------|------|
| `getNtrItem()`, `getTamedItem()`, `getTemptationItem()`, `getConfigIngredient()` | Moved elsewhere (possibly recipe/event layer) |
| `canDestroyBlock()`, `canPlaceBlock()`, `destroyBlock()`, `dropResourcesToMaidInv()`, `placeItemBlock()` | In MaidWorldInteractionManager |
| `getBoundingBoxForCulling()` (client, bedrock model) | Removed from server EntityMaid (client-side only) |
| `getLeashOffset()` (client, model-based) | `miscManager.getLegacyLeashOffset(modelId)` for server-side; client version separate |
| `isAiming()` / `setAiming()` | In MaidCombatManager or MaidAnimationManager |
| `hurtAndBreak(ItemStack, int)` | May be moved or removed |
| `getOwner()` (optimized MinecraftServer lookup) | Possibly removed |
| `teleportToOwner()`, `maybeTeleportTo()`, `teleportTooClosed()`, `canTeleportTo()` | In MaidTeleportManager |
| `getChatBubbleManager()` | Unchanged |
| `getKillRecordManager()` | Unchanged |
| `getFavorabilityManager()` | Unchanged |
| `getAiChatManager()` | Unchanged |
| `getNavigationManager()` | Unchanged |

---

## Data Storage Changes

### Before (1.20.1): EntityDataAccessor + CompoundTag

```java
// 30+ EntityDataAccessor fields defined directly in EntityMaid
private static final EntityDataAccessor<String> DATA_MODEL_ID = SynchedEntityData.defineId(...);
private static final EntityDataAccessor<Integer> DATA_HUNGER = SynchedEntityData.defineId(...);
// ... 30+ more

// Task data stored in MaidTaskDataMaps with CompoundTag serialization
private final MaidTaskDataMaps taskDataMaps = new MaidTaskDataMaps();
// Synced via separate TASK_DATA_SYNC EntityDataAccessor<CompoundTag>
```

### After (1.21.1): Neoforge AttachmentType + Records

```java
// Data defined as records with CODEC, STREAM_CODEC, and AttachmentType
public record ProfileData(String modelId, String soundPackId) {
    public static final AttachmentType<ProfileData> TYPE = AttachmentType
            .builder(() -> new ProfileData(DEFAULT_MODEL_ID, getInitSoundPackId()))
            .serialize(CODEC)
            .sync(STREAM_CODEC)
            .build();
}

// EntityMaid only defines 3 EntityDataAccessor fields directly
// All other data accessed via maid.getData(ATTACHMENT_TYPE) / maid.setData(ATTACHMENT_TYPE, value)
```

**Key benefits of new system:**
- Type-safe records with `with*()` builder pattern for immutable updates
- Automatic sync via AttachmentType's `.sync()` configuration
- Codec-based serialization replaces manual NBT reads/writes
- Reduced EntityDataAccessor count from 30+ to 3
- Built-in CODEC/STREAM_CODEC for networking

---

## Task System Changes

The task classes (`TaskAttack`, `TaskBowAttack`, `TaskCocoa`, etc.) in `entity/task/` appear structurally similar between versions. Key differences:

- **IMaidTask interface**: May have changed method signatures (e.g., `getTaskConfigGuiProvider` might now take different params)
- **TaskDataKey system** (old): Removed — task-specific data now stored differently
- **MaidTaskDataMaps** (old): Removed — no longer needed
- **TaskDataRegister** (old): Removed — registration pattern changed

---

## AI/Behavior Changes

### Brain System

| Aspect | Old | New |
|--------|-----|-----|
| Brain provider | `brainProvider()` returns `Brain.provider(memoryTypes, sensorTypes)` | `BRAIN_PROVIDER` constant from `MaidBrain` |
| Brain creation | `makeBrain(Dynamic<?>)` | `makeBrain(Brain.Packed)` |
| Brain refresh | `brain.copyWithoutBehaviors()` then re-register | `oldBrain.pack()` then `makeBrain()` |
| YSM compat in tick | `YsmCompat.isInstalled()` check in tick() | Removed |

### AI Subpackage Structure

The `entity/ai/brain/task/` directory has the **same files** in both versions (60+ task classes: `MaidArriveAtBlockTask`, `MaidAttackStrafingTask`, `MaidBegTask`, etc.). The sensor, ride, and other AI subpackages also appear structurally identical.

### AI Control, Edible, Fishing, Goal, Navigation, Path

All files in these subpackages appear to have same names/structure between versions. No new files added or removed. Implementation differences may exist at the method level but require per-file analysis.

---

## Import & Type Changes

### General API Changes (NeoForge 21.1 / MC 1.21.1 → 1.21.1)

| Old Import | New Import |
|------------|------------|
| `net.minecraft.resources.ResourceLocation` | `net.minecraft.resources.Identifier` (in some places) |
| `net.minecraft.nbt.CompoundTag` | `net.minecraft.world.level.storage.ValueInput/ValueOutput` (for entity save/load) |
| `com.mojang.serialization.Dynamic` | `net.minecraft.world.entity.ai.Brain.Packed` (for brain) |
| `net.minecraft.world.entity.MobSpawnType` | `net.minecraft.world.entity.EntitySpawnReason` (for finalizeSpawn) |
| `net.neoforged.network.PacketDistributor` (some patterns) | `net.neoforged.neoforge.network.PacketDistributor` |
| `net.neoforged.neoforge.items.IItemHandler` | `net.neoforged.neoforge.transfer.item.ItemUtil` (for some operations) |
| `net.neoforged.api.distmarker.Dist/OnlyIn` | Used differently or removed from EntityMaid |

### EntityMaid Imports

**Old imports (removed in new):**
- `MaidSchedule` (now referenced via TaskData)
- `ServerCustomPackLoader`, `CustomPackLoader`, `MaidModelInfo` — model/client imports removed from server entity
- `MaidBackpackHandler`, `MaidHandsInvWrapper`, `MaidInvWrapper`, `BaubleItemHandler`, `BaubleContainer` — inventory imports removed from EntityMaid
- `ItemFilm`, `CuriosCompat`, `YsmCompat`, `SyncYsmMaidDataPackage`, `YsmMaidClientTickEvent` — compat/ysm imports removed
- `TeleportHelper`, `ItemsUtil` — utility imports moved to managers
- `ArrowAccessor` (mixin accessor) — use removed
- `MaidHurtTarget`, `MaidDamageEvent`, `MaidDeathEvent`, `MaidTamedEvent`, `MaidPickupEvent`, `MaidPlaySoundEvent`, `InteractMaidEvent`, `MaidAfterEatEvent`, `MaidTombstoneEvent`, `MaidTypeNameEvent` — events now handled in managers
- Various block/particle/fluid imports — moved to relevant managers

**New imports (not in old):**
- `EntityDataSerializers.BOOLEAN` → only 1 (was 8+ booleans)
- `net.neoforged.neoforge.transfer.item.ItemUtil`
- Manager class imports (MaidConfigManager, etc.)
- `ValueInput`, `ValueOutput` for save/load
- `EntitySpawnReason` instead of `MobSpawnType`

### Backpack Type Changes

| Old | New | Status |
|-----|-----|--------|
| `BackpackManager` | `BackpackManager` | Kept |
| `BigBackpack` | `BigBackpack` | Kept |
| `EmptyBackpack` | `EmptyBackpack` | Kept |
| `MiddleBackpack` | `MiddleBackpack` | Kept |
| `SmallBackpack` | `SmallBackpack` | Kept |
| `CraftingTableBackpack` | — | **Removed** |
| `EnderChestBackpack` | — | **Removed** |
| `FurnaceBackpack` | — | **Removed** |
| `TankBackpack` | — | **Removed** |
| `data/FurnaceBackpackData` | — | **Removed** |
| `data/TankBackpackData` | — | **Removed** |

---

## Chat Bubble System

The deprecated `ChatBubbleManger` (typo) class in the old project was removed. Other chat bubble classes (`ChatBubbleManager`, `ChatBubbleDataCollection`, `ChatBubbleRegister`, `IChatBubbleData`, `RandomEmoji`, and all `implement/` classes) remain with same names.

---

## Summary of Change Counts

| Category | Count |
|----------|-------|
| New manager classes | 17 |
| New data record classes | 7 |
| Removed old data classes | 3 (MaidTaskDataMaps, TaskDataRegister, AttackListData) |
| Removed backpack variants | 4 (CraftingTable, EnderChest, Furnace, Tank) |
| Removed backpack data classes | 2 (FurnaceBackpackData, TankBackpackData) |
| Removed deprecated classes | 2 (ChatBubbleManger, DefaultMaidSoundPack) |
| Removed info classes | 1 (ServerChairModels) |
| New info classes | 2 (ServerCustomPackReader, ServerMaidModelPackLoader) + 1 abstract (AbstractServerModels) |
| EntityDataAccessor count change | 30+ → 3 |
| EntityMaid line count | ~2828 → ~945 (-66%) |
| Total new/removed/changed files | ~35+ |
