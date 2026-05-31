# API Package Migration (NeoForge 21.1 / MC 1.21.1 → NeoForge 26.1)

## File Changes

| File Name | Old Status | New Status | Notes |
|---|---|---|---|
| `animation/IChairData.java` | ✓ | ✓ | Unchanged |
| `animation/ICustomAnimation.java` | ✓ | ⚠ | Removed `setGeckoRotationAngles` method and `AnimatedGeoModel` import |
| `animation/IEntityData.java` | ✓ | ✓ | Unchanged |
| `animation/IMagicCastingAnimationProvider.java` | ✓ | ⚠ | `IMaid` → `EntityMaid` parameter type |
| `animation/IMagicCastingState.java` | ✓ | ✓ | Unchanged |
| `animation/IMaidData.java` | ✓ | ✓ | Unchanged |
| `animation/IModelRenderer.java` | ✓ | ✓ | Unchanged |
| `animation/IWorldData.java` | ✓ | ✓ | Unchanged |
| `backpack/IBackpackData.java` | ✓ | ✗ | **Removed** — NBT serialization interface removed |
| `backpack/IMaidBackpack.java` | ✓ | ⚠ | Rendering refactored: `@OnlyIn` methods replaced by `getRenderData()` |
| `backpack/ITriggerSlotChange.java` | ✓ | ✓ | Unchanged |
| `backpack/MaidBackpackRenderData.java` | ✗ | ⚠ | **New** — abstract class for backpack client rendering |
| `bauble/IChestType.java` | ✓ | ✓ | Unchanged |
| `bauble/IMaidBauble.java` | ✓ | ✓ | Unchanged |
| `block/IBoardGameBlock.java` | ✓ | ✓ | Unchanged |
| `block/IBoardGameEntityBlock.java` | ✓ | ✓ | Unchanged |
| `block/IMaidEdibleBlock.java` | ✓ | ✓ | Unchanged |
| `block/IMultiBlock.java` | ✓ | ✓ | Unchanged |
| `client/decoder/GifDecoder.java` | ✓ | ✓ | Unchanged |
| `client/gui/ITooltipButton.java` | ✓ | ✓ | Unchanged |
| `client/render/MaidRenderState.java` | ✓ | ✓ | Unchanged |
| `client/sound/ICustomSoundBuffer.java` | ✓ | ✓ | Unchanged |
| `entity/IMaid.java` | ✓ | ✗ | **Removed** — Mob↔Maid conversion interface removed |
| `entity/IBroomControl.java` | ✓ | ✓ | Unchanged |
| `entity/ai/IExtraMaidBrain.java` | ✓ | ✓ | Unchanged |
| `entity/data/TaskDataKey.java` | ✓ | ⚠ | **Drastic simplification** — NBT methods removed, only `Identifier id()` remains |
| `entity/fishing/IFishingType.java` | ✓ | ✓ | Unchanged |
| `event/AddJadeInfoEvent.java` | ✓ | ✓ | Unchanged |
| `event/AddTopInfoEvent.java` | ✓ | ✗ | **Removed** — TheOneProbe integration event |
| `event/client/AddClothConfigEvent.java` | ✓ | ✓ | Unchanged |
| `event/client/DefaultGeckoAnimationEvent.java` | ✓ | ⚠ | `ResourceLocation`→`Identifier`, animation merge method changed |
| `event/client/MaidContainerGuiEvent.java` | ✓ | ⚠ | `GuiGraphics` → `GuiGraphicsExtractor` |
| `event/client/OpenPatchouliBookEvent.java` | ✓ | ✓ | Unchanged |
| `event/client/package-info.java` | ✓ | ✓ | Unchanged |
| `event/client/RenderMaidEvent.java` | ✓ | ✗ | **Removed** — depended on removed `IMaid` |
| `event/ConvertMaidEvent.java` | ✓ | ✗ | **Removed** — depended on removed `IMaid` |
| `event/InteractMaidEvent.java` | ✓ | ✓ | Unchanged |
| `event/MaidAfterEatEvent.java` | ✓ | ✗ | **Removed** |
| `event/MaidAndItemTransformEvent.java` | ✓ | ✓ | Unchanged |
| `event/MaidAttackEvent.java` | ✓ | ✓ | Unchanged |
| `event/MaidBackpackChangeEvent.java` | ✓ | ✓ | Unchanged |
| `event/MaidBaubleChangeEvent.java` | ✓ | ✓ | Unchanged |
| `event/MaidDamageEvent.java` | ✓ | ✓ | Unchanged |
| `event/MaidDeathEvent.java` | ✓ | ✓ | Unchanged |
| `event/MaidEquipEvent.java` | ✓ | ✓ | Unchanged |
| `event/MaidFavorabilityLevelChangeEvent.java` | ✓ | ✓ | Unchanged |
| `event/MaidFishedEvent.java` | ✓ | ✓ | Unchanged |
| `event/MaidHurtEvent.java` | ✓ | ✓ | Unchanged |
| `event/MaidHurtTarget.java` | ✓ | ✓ | Unchanged |
| `event/MaidPickupEvent.java` | ✓ | ⚠ | `AbstractArrow` import path changed (Minecraft 1.21.1 refactor) |
| `event/MaidPlaySoundEvent.java` | ✓ | ✓ | Unchanged |
| `event/MaidRequestItemEvent.java` | ✓ | ✓ | Unchanged |
| `event/MaidTamedEvent.java` | ✓ | ✓ | Unchanged |
| `event/MaidTaskEnableEvent.java` | ✓ | ✓ | Unchanged |
| `event/MaidTickEvent.java` | ✓ | ✓ | Unchanged |
| `event/MaidTombstoneEvent.java` | ✓ | ✓ | Unchanged |
| `event/MaidTypeNameEvent.java` | ✓ | ✓ | Unchanged |
| `event/MaidWirelessIOEvent.java` | ✓ | ⚠ | `IItemHandler` → `ResourceHandler<ItemResource>` (NeoForge transfer API) |
| `event/package-info.java` | ✓ | ✓ | Unchanged |
| `event/RegisterKubeJSEvent.java` | ✓ | ✗ | **Removed** — KubeJS event registration |
| `game/chess/Evaluate.java` | ✓ | ✓ | Unchanged |
| `game/chess/Position.java` | ✓ | ✓ | Unchanged |
| `game/chess/Search.java` | ✓ | ✓ | Unchanged |
| `game/chess/Util.java` | ✓ | ✓ | Unchanged |
| `game/gomoku/AIService.java` | ✓ | ✓ | Unchanged |
| `game/gomoku/GomokuCodec.java` | ✓ | ✗ | **Removed** — gomoku board codec utility |
| `game/gomoku/Point.java` | ✓ | ✓ | Unchanged |
| `game/gomoku/Statue.java` | ✓ | ✓ | Unchanged |
| `game/gomoku/ZhiZhangAIService.java` | ✓ | ✓ | Unchanged |
| `game/xqwlight/Position.java` | ✓ | ✓ | Unchanged |
| `game/xqwlight/Search.java` | ✓ | ✓ | Unchanged |
| `game/xqwlight/Util.java` | ✓ | ✓ | Unchanged |
| `ILittleMaid.java` | ✓ | ⚠ | Removed methods, `@OnlyIn` annotations, import changes (see below) |
| `LittleMaidExtension.java` | ✓ | ✓ | Unchanged |
| `mixin/IBlockBurningCacheMixin.java` | ✓ | ✓ | Unchanged |
| `mixin/INavigationMixin.java` | ✓ | ✓ | Unchanged |
| `mixin/IPlayerMixin.java` | ✓ | ✓ | Unchanged |
| `task/FunctionCallSwitchResult.java` | ✓ | ✓ | Unchanged |
| `task/IAttackTask.java` | ✓ | ⚠ | Multiple changes (see below) |
| `task/IFarmTask.java` | ✓ | ✓ | Unchanged |
| `task/IFeedTask.java` | ✓ | ✓ | Unchanged |
| `task/IMaidTask.java` | ✓ | ⚠ | Multiple changes (see below) |
| `task/IRangedAttackTask.java` | ✓ | ⚠ | `TARGET_CONDITIONS.test` signature changed |
| `task/ISpecialCropHandler.java` | ✓ | ⚠ | `ItemNameBlockItem` → `BlockItem` |
| `task/meal/IMaidMeal.java` | ✓ | ✓ | Unchanged |
| `task/meal/MaidMealType.java` | ✓ | ✓ | Unchanged |
| `task/package-info.java` | ✓ | ✓ | Unchanged |

## Key API Breaking Changes

### 1. `IMaid` interface removed (`entity/IMaid.java`)

The `IMaid` interface has been **completely removed**. It provided:
- Static `convert(Mob)` and `convertToMaid(Mob)` methods
- `asStrictMaid()` → `EntityMaid` conversion
- YSM model getter/setters
- `asEntity()`, task/animation query methods
- Equipment slot accessors, deprecated legacy methods

**Migration**: Addons previously using `IMaid` for rendering other mobs as maids must find alternative approaches. The `ConvertMaidEvent` and `RenderMaidEvent` classes that depend on it are also removed.

---

### 2. `ILittleMaid` main extension interface

#### Methods removed:
- `registerTaskData(TaskDataRegister register)` — Task data registration removed
- `registerAIFunctionCall(FunctionCallRegister register)` — Was deprecated since 1.5.1, now fully removed

#### `@OnlyIn` annotations removed:
All methods that were annotated `@OnlyIn(Dist.CLIENT)` no longer have this annotation:
- `addMaidTips(MaidTipsOverlay)`
- `addAdditionMaidLayer(EntityMaidRenderer, Context)`
- `addAdditionGeckoMaidLayer(GeckoEntityMaidRenderer, Context)`
- `addHardcodeAnimation(HardcodedAnimationManger)`
- `registerMagicCastingAnimation(MagicCastingAnimationManager)`

#### Type signature change:
```diff
- void addAdditionGeckoMaidLayer(GeckoEntityMaidRenderer<? extends Mob> renderer, ...)
+ void addAdditionGeckoMaidLayer(GeckoEntityMaidRenderer renderer, ...)
```
The generic type parameter `<? extends Mob>` was removed.

#### Import changes:
```diff
- import com.github.tartaricacid.touhoulittlemaid.client.renderer.entity.GeckoEntityMaidRenderer;
+ import com.github.tartaricacid.touhoulittlemaid.client.renderer.entity.gecko.GeckoEntityMaidRenderer;

- import com.github.tartaricacid.touhoulittlemaid.ai.service.function.FunctionCallRegister;
  (removed — deprecated method removed)

- import com.github.tartaricacid.touhoulittlemaid.entity.data.TaskDataRegister;
  (removed — TaskDataRegister no longer needed)

- import net.minecraft.world.entity.Mob;
  (removed — no longer needed for generic parameter)

- import net.neoforged.api.distmarker.Dist;
- import net.neoforged.api.distmarker.OnlyIn;
  (removed — @OnlyIn annotations removed)
```

---

### 3. `IMaidBackpack` — Client rendering refactored

Three `@OnlyIn(Dist.CLIENT)` client methods replaced by a single abstract method:

```diff
- @OnlyIn(Dist.CLIENT)
- public abstract void offsetBackpackItem(PoseStack poseStack);
- @Nullable @OnlyIn(Dist.CLIENT)
- public abstract EntityModel<EntityMaid> getBackpackModel(EntityModelSet modelSet);
- @Nullable @OnlyIn(Dist.CLIENT)
- public abstract ResourceLocation getBackpackTexture();
+ public abstract MaidBackpackRenderData getRenderData();
```

Methods removed (related to `IBackpackData`):
```diff
- public boolean hasBackpackData()
- @Nullable public IBackpackData getBackpackData(EntityMaid maid)
```

`ItemsUtil.dropEntityItems` call signature changed:
```diff
- ItemsUtil.dropEntityItems(maid, maid.getMaidInv(), BackpackLevel.EMPTY_CAPACITY)
+ ItemsUtil.dropEntityItems(maid, maid.getMaidInv(), BackpackLevel.EMPTY_CAPACITY, null)
```

#### Import changes:
```diff
- import net.minecraft.resources.ResourceLocation;
+ import net.minecraft.resources.Identifier;

- import net.neoforged.api.distmarker.Dist;
- import net.neoforged.api.distmarker.OnlyIn;
- import com.mojang.blaze3d.vertex.PoseStack;
- import net.minecraft.client.model.EntityModel;
- import net.minecraft.client.model.geom.EntityModelSet;
  (removed — rendering now via MaidBackpackRenderData)
```

---

### 4. `TaskDataKey<T>` — Drastic simplification

The entire NBT serialization surface was removed:

```diff
- public interface TaskDataKey<T> {
-     ResourceLocation getKey();
-     CompoundTag writeSaveData(T data);
-     T readSaveData(CompoundTag compound);
-     default CompoundTag writeSyncData(T data) { return this.writeSaveData(data); }
-     default T readSyncData(CompoundTag compound) { return this.readSaveData(compound); }
- }
+ public interface TaskDataKey<T> {
+     Identifier id();
+ }
```

**Migration**: Addons must no longer implement NBT serialization in TaskDataKey. Task data storage now uses NeoForge's DataAttachment system.

---

### 5. `Backpack` subsystem — `IBackpackData` removed, `MaidBackpackRenderData` added

`IBackpackData` removed (backpack data NBT interface: `load()`/`save()`/`serverTick()`/`getDataAccess()`).

New `MaidBackpackRenderData` abstract class added, consolidating backpack rendering:
```java
public abstract class MaidBackpackRenderData {
    public static final MaidBackpackRenderData EMPTY = ...;
    @Nullable public abstract EntityModel<EntityMaidRenderState> getBackpackModel();
    @Nullable public abstract Identifier getBackpackTexture();
    public abstract void offsetBackpackItem(PoseStack poseStack);
}
```

---

### 6. `IAttackTask` — Internal implementation changes

```diff
- ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(target.getType());
+ Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(target.getType());

- if (tamableAnimal.getOwnerUUID() != null)
+ if (tamableAnimal.getOwnerReference() != null)

- mobs.findClosest((e) -> maid.canAttack(e) && maid.isWithinRestriction(e.blockPosition()))
+ mobs.findClosest((e) -> maid.canAttack(e) && maid.isWithinHome(e.blockPosition()))
```

`AttackListData`/`InitTaskData.ATTACK_LIST` lookup logic commented out with a TODO (replaced by NeoForge DataAttachment):
```diff
- AttackListData attackListData = maid.getData(InitTaskData.ATTACK_LIST);
- if (attackListData != null && attackListData.attackGroups().containsKey(id)) {
-     monsterType = attackListData.attackGroups().get(id);
- } else {
-     monsterType = DefaultMonsterType.getMonsterType(target);
- }
+ // TODO 已经删除，需要使用 NeoForge 的 DataAttachment
+ monsterType = DefaultMonsterType.getMonsterType(target);
```

Removed imports:
```diff
- import com.github.tartaricacid.touhoulittlemaid.entity.data.inner.AttackListData;
- import com.github.tartaricacid.touhoulittlemaid.init.InitTaskData;
```

---

### 7. `IMaidTask` — Vanilla method rename changes

Minecraft 1.21.1 renamed several navigation/restriction methods:

```diff
- ResourceLocation getUid();
+ Identifier getUid();

- if (maid.hasRestriction())
+ if (maid.hasHome())

- return new AABB(maid.getRestrictCenter()).inflate(...)
+ return new AABB(maid.getHomePosition()).inflate(...)

- return maid.getRestrictRadius();
+ return maid.getHomeRadius();
```

---

### 8. `IRangedAttackTask` — `TARGET_CONDITIONS.test` signature change

Minecraft 1.21.1 changed `TargetingConditions.test` to require a `ServerLevel`:

```diff
  static boolean targetConditionsTest(EntityMaid maid, LivingEntity target, ModConfigSpec.IntValue configRange) {
      TARGET_CONDITIONS.range(configRange.get());
-     return TARGET_CONDITIONS.test(maid, target);
+     if (maid.level() instanceof ServerLevel serverLevel) {
+         return TARGET_CONDITIONS.test(serverLevel, maid, target);
+     }
+     return false;
  }
```

New import added:
```diff
+ import net.minecraft.server.level.ServerLevel;
```

---

### 9. `IMagicCastingAnimationProvider` — Parameter type change

```diff
- import com.github.tartaricacid.touhoulittlemaid.api.entity.IMaid;
- IMagicCastingState getMagicCastingState(IMaid maid);
- AnimationBuilder getAnimationBuilder(IMaid maid, IMagicCastingState state);
+ IMagicCastingState getMagicCastingState(EntityMaid maid);
+ AnimationBuilder getAnimationBuilder(EntityMaid maid, IMagicCastingState state);
```

Since `IMaid` was removed, this interface now uses `EntityMaid` directly.

---

### 10. `ICustomAnimation` — GeckoLib rendering method removed

The `setGeckoRotationAngles` method and related import were removed:

```diff
- import com.github.tartaricacid.touhoulittlemaid.geckolib3.geo.animated.AnimatedGeoModel;
- default void setGeckoRotationAngles(T entity, AnimatedGeoModel model, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) { }
```

Only the vanilla model `setRotationAngles` and `setupGeckoRotations` remain.

---

### 11. `ISpecialCropHandler` — Minecraft class rename

```diff
- import net.minecraft.world.item.ItemNameBlockItem;
- if (seed.getItem() instanceof ItemNameBlockItem blockNamedItem)
+ import net.minecraft.world.item.BlockItem;
+ if (seed.getItem() instanceof BlockItem blockNamedItem)
```

`ItemNameBlockItem` was renamed to `BlockItem` in Minecraft 1.21.1.

---

### 12. `MaidWirelessIOEvent` — Item transfer API migration

The Forge capability-based `IItemHandler` was replaced by NeoForge's transfer API:

```diff
- import net.neoforged.neoforge.items.IItemHandler;
- private final IItemHandler maidInv;
- private final IItemHandler chestInv;
- private final IItemHandler filterInv;
+ import net.neoforged.neoforge.transfer.ResourceHandler;
+ import net.neoforged.neoforge.transfer.item.ItemResource;
+ import org.jetbrains.annotations.NotNull;
+ private final ResourceHandler<@NotNull ItemResource> maidInv;
+ private final ResourceHandler<@NotNull ItemResource> chestInv;
+ private final ResourceHandler<@NotNull ItemResource> filterInv;
```

Getter return types changed accordingly:
```diff
- public IItemHandler getMaidInv()
+ public ResourceHandler<@NotNull ItemResource> getMaidInv()
```

---

### 13. `MaidContainerGuiEvent` — GUI class rename

```diff
- import net.minecraft.client.gui.GuiGraphics;
- private final GuiGraphics graphics;
- public GuiGraphics getGraphics()
+ import net.minecraft.client.gui.GuiGraphicsExtractor;
+ private final GuiGraphicsExtractor graphics;
+ public GuiGraphicsExtractor getGraphics()
```

---

### 14. `DefaultGeckoAnimationEvent` — Animation merge and identifier change

```diff
- import net.minecraft.resources.ResourceLocation;
- import static ...GeckoModelLoader.mergeAnimationFile;
- mergeAnimationFile(stream, animationFile);
+ import net.minecraft.resources.Identifier;
+ import ...GeckoContainerBuilder;
+ animationFile.animations().putAll(GeckoContainerBuilder.getAnimationFile(stream).animations());
```

---

### 15. `MaidPickupEvent` — Minecraft 1.21.1 package refactor

```diff
- import net.minecraft.world.entity.projectile.AbstractArrow;
+ import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
```

`AbstractArrow` was moved from `projectile` to `projectile.arrow` in MC 1.21.1.

---

### 16. Removed events

| Removed Class | Reason |
|---|---|
| `AddTopInfoEvent` | TheOneProbe integration removed |
| `ConvertMaidEvent` | Depended on removed `IMaid` |
| `MaidAfterEatEvent` | Removed |
| `RegisterKubeJSEvent` | KubeJS event registration removed |
| `RenderMaidEvent` | Depended on removed `IMaid` |

---

### 17. Removed game utility

| Removed Class | Reason |
|---|---|
| `GomokuCodec` | Gomoku board encoding/decoding utility removed |

---

## Import Changes

| Old Import | New Import | Rationale |
|---|---|---|
| `net.minecraft.resources.ResourceLocation` | `net.minecraft.resources.Identifier` | Minecraft 1.21.1 rename |
| `net.minecraft.world.entity.projectile.AbstractArrow` | `net.minecraft.world.entity.projectile.arrow.AbstractArrow` | MC 1.21.1 package refactor |
| `net.minecraft.world.item.ItemNameBlockItem` | `net.minecraft.world.item.BlockItem` | MC 1.21.1 rename |
| `net.neoforged.neoforge.items.IItemHandler` | `net.neoforged.neoforge.transfer.ResourceHandler<ItemResource>` | NeoForge transfer API |
| `net.minecraft.client.gui.GuiGraphics` | `net.minecraft.client.gui.GuiGraphicsExtractor` | Project refactor |
| `...renderer.entity.GeckoEntityMaidRenderer` | `...renderer.entity.gecko.GeckoEntityMaidRenderer` | Package relocation |
| `net.neoforged.api.distmarker.Dist` | (removed) | @OnlyIn annotations removed |
| `net.neoforged.api.distmarker.OnlyIn` | (removed) | @OnlyIn annotations removed |
| `net.minecraft.world.entity.Mob` | (removed) | Generic parameter removed |
| `com.mojang.blaze3d.vertex.PoseStack` | (removed from IMaidBackpack) | Rendering refactored |
| `net.minecraft.client.model.EntityModel` | (removed from IMaidBackpack) | Rendering refactored |
| `net.minecraft.client.model.geom.EntityModelSet` | (removed from IMaidBackpack) | Rendering refactored |
| `...entity.data.inner.AttackListData` | (removed) | Replaced by DataAttachment |
| `...init.InitTaskData` | (removed) | Replaced by DataAttachment |
| `...ai.service.function.FunctionCallRegister` | (removed) | registerAIFunctionCall removed |
| `...entity.data.TaskDataRegister` | (removed) | registerTaskData removed |
| `mcjty.theoneprobe.api.*` | (removed) | TheOneProbe integration removed |
| `dev.latvian.mods.kubejs.event.EventGroup` | (removed) | KubeJS event removed |
| `...api.entity.IMaid` | (removed) | IMaid interface removed |

## All Files Analyzed

| # | File | Status |
|---|---|---|
| 1 | `animation/IChairData.java` | ✓ No change |
| 2 | `animation/ICustomAnimation.java` | ⚠ Removed `setGeckoRotationAngles` |
| 3 | `animation/IEntityData.java` | ✓ No change |
| 4 | `animation/IMagicCastingAnimationProvider.java` | ⚠ `IMaid` → `EntityMaid` |
| 5 | `animation/IMagicCastingState.java` | ✓ No change |
| 6 | `animation/IMaidData.java` | ✓ No change |
| 7 | `animation/IModelRenderer.java` | ✓ No change |
| 8 | `animation/IWorldData.java` | ✓ No change |
| 9 | `backpack/IBackpackData.java` | ✗ Removed |
| 10 | `backpack/IMaidBackpack.java` | ⚠ Rendering refactored |
| 11 | `backpack/ITriggerSlotChange.java` | ✓ No change |
| 12 | `backpack/MaidBackpackRenderData.java` | ⚠ New file |
| 13 | `bauble/IChestType.java` | ✓ No change |
| 14 | `bauble/IMaidBauble.java` | ✓ No change |
| 15 | `block/IBoardGameBlock.java` | ✓ No change |
| 16 | `block/IBoardGameEntityBlock.java` | ✓ No change |
| 17 | `block/IMaidEdibleBlock.java` | ✓ No change |
| 18 | `block/IMultiBlock.java` | ✓ No change |
| 19 | `client/decoder/GifDecoder.java` | ✓ No change |
| 20 | `client/gui/ITooltipButton.java` | ✓ No change |
| 21 | `client/render/MaidRenderState.java` | ✓ No change |
| 22 | `client/sound/ICustomSoundBuffer.java` | ✓ No change |
| 23 | `entity/IMaid.java` | ✗ Removed |
| 24 | `entity/IBroomControl.java` | ✓ No change |
| 25 | `entity/ai/IExtraMaidBrain.java` | ✓ No change |
| 26 | `entity/data/TaskDataKey.java` | ⚠ NBT methods removed |
| 27 | `entity/fishing/IFishingType.java` | ✓ No change |
| 28 | `event/AddJadeInfoEvent.java` | ✓ No change |
| 29 | `event/AddTopInfoEvent.java` | ✗ Removed |
| 30 | `event/client/AddClothConfigEvent.java` | ✓ No change |
| 31 | `event/client/DefaultGeckoAnimationEvent.java` | ⚠ Animation merge + Identifier |
| 32 | `event/client/MaidContainerGuiEvent.java` | ⚠ GuiGraphics → GuiGraphicsExtractor |
| 33 | `event/client/OpenPatchouliBookEvent.java` | ✓ No change |
| 34 | `event/client/package-info.java` | ✓ No change |
| 35 | `event/client/RenderMaidEvent.java` | ✗ Removed |
| 36 | `event/ConvertMaidEvent.java` | ✗ Removed |
| 37 | `event/InteractMaidEvent.java` | ✓ No change |
| 38 | `event/MaidAfterEatEvent.java` | ✗ Removed |
| 39 | `event/MaidAndItemTransformEvent.java` | ✓ No change |
| 40 | `event/MaidAttackEvent.java` | ✓ No change |
| 41 | `event/MaidBackpackChangeEvent.java` | ✓ No change |
| 42 | `event/MaidBaubleChangeEvent.java` | ✓ No change |
| 43 | `event/MaidDamageEvent.java` | ✓ No change |
| 44 | `event/MaidDeathEvent.java` | ✓ No change |
| 45 | `event/MaidEquipEvent.java` | ✓ No change |
| 46 | `event/MaidFavorabilityLevelChangeEvent.java` | ✓ No change |
| 47 | `event/MaidFishedEvent.java` | ✓ No change |
| 48 | `event/MaidHurtEvent.java` | ✓ No change |
| 49 | `event/MaidHurtTarget.java` | ✓ No change |
| 50 | `event/MaidPickupEvent.java` | ⚠ AbstractArrow import path |
| 51 | `event/MaidPlaySoundEvent.java` | ✓ No change |
| 52 | `event/MaidRequestItemEvent.java` | ✓ No change |
| 53 | `event/MaidTamedEvent.java` | ✓ No change |
| 54 | `event/MaidTaskEnableEvent.java` | ✓ No change |
| 55 | `event/MaidTickEvent.java` | ✓ No change |
| 56 | `event/MaidTombstoneEvent.java` | ✓ No change |
| 57 | `event/MaidTypeNameEvent.java` | ✓ No change |
| 58 | `event/MaidWirelessIOEvent.java` | ⚠ IItemHandler → ResourceHandler |
| 59 | `event/package-info.java` | ✓ No change |
| 60 | `event/RegisterKubeJSEvent.java` | ✗ Removed |
| 61-64 | `game/chess/*.java` (4 files) | ✓ No change |
| 65 | `game/gomoku/AIService.java` | ✓ No change |
| 66 | `game/gomoku/GomokuCodec.java` | ✗ Removed |
| 67-69 | `game/gomoku/{Point,Statue,ZhiZhangAIService}.java` (3 files) | ✓ No change |
| 70-72 | `game/xqwlight/*.java` (3 files) | ✓ No change |
| 73 | `ILittleMaid.java` | ⚠ Methods removed, @OnlyIn removed, import changes |
| 74 | `LittleMaidExtension.java` | ✓ No change |
| 75-77 | `mixin/*.java` (3 files) | ✓ No change |
| 78 | `task/FunctionCallSwitchResult.java` | ✓ No change |
| 79 | `task/IAttackTask.java` | ⚠ Multiple changes |
| 80 | `task/IFarmTask.java` | ✓ No change |
| 81 | `task/IFeedTask.java` | ✓ No change |
| 82 | `task/IMaidTask.java` | ⚠ hasRestriction → hasHome etc. |
| 83 | `task/IRangedAttackTask.java` | ⚠ ServerLevel for test() |
| 84 | `task/ISpecialCropHandler.java` | ⚠ ItemNameBlockItem → BlockItem |
| 85 | `task/meal/IMaidMeal.java` | ✓ No change |
| 86 | `task/meal/MaidMealType.java` | ✓ No change |
| 87 | `task/package-info.java` | ✓ No change |

**Summary**: 59 ✓ unchanged · 17 ⚠ changed · 8 ✗ removed · 1 ⚠ new
