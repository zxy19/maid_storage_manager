# Client Package Migration

## Structural Overview

The `client/` package underwent the most extensive refactoring in the 1.21.1 migration. Nearly every subpackage was affected. The biggest changes are in `resource/` (complete restructure), `renderer/` (new state-based rendering), `animation/` (JS system removed), and `entity/` (rewritten for new render pipeline).

### High-Level Summary

| Area | Change Type | Detail |
|------|------------|--------|
| `resource/` | **Restructured** | Top-level files moved into `accessor/`, `bedrock/`, `loader/` subpackages |
| `animation/` | **Removed** | Custom JS animation system (`CustomJsAnimationManger`, `script/`) deleted |
| `entity/` | **Rewritten** | `GeckoMaidEntity` completely reworked for new render state system |
| `renderer/` | **Reorganized** | `backpack/`, `gecko/`, `state/` added; `item/`, `geckolayer/` removed; texture types replaced |
| `init/` | **Split** | Key mapping, reload listeners extracted to dedicated classes |
| `event/` | **Changed** | 2 removed, 3 added |
| `model/` | **Trimmed** | `AbstractModel`, `EntityPlaceholderModel` removed; bedrock models added |
| **New** | `particle/`, `proxy/`, `extensions/` | New packages for new functionality |

---

## Resource Loading Changes

This is the most significant structural change. The `resource/` package was completely reorganized from a flat structure to a well-layered architecture.

### Old Layout (top-level files)
```
resource/
├── BedrockModelLoader.java          → bedrock/InternalBedrockModelRegistry.java
├── CustomPackLoader.java            → loader/CustomPackLoader.java (orchestrator only)
├── GeckoModelLoader.java            → bedrock/CustomPackBedrockModelParser.java + GeckoContainerBuilder.java
├── LanguageLoader.java              → loader/LanguageLoader.java
├── LegacyPackRepositorySource.java  → **REMOVED**
├── listener/EmojiReloadListener.java → listener/EmojiReloadListener.java (kept)
└── models/ + pojo/                  → models/ + pojo/ (kept, enhanced)
```

### New Layout
```
resource/
├── accessor/                        ← NEW: unified resource access abstraction
│   ├── FileResourceAccessor.java
│   ├── ResourceAccessor.java         (interface)
│   └── ZipResourceAccessor.java
├── bedrock/                         ← NEW: centralized bedrock model management
│   ├── CustomPackBedrockModelParser.java
│   ├── GeckoContainerBuilder.java
│   ├── InternalBedrockModelManager.java
│   ├── InternalBedrockModelRegistry.java
│   ├── InternalBedrockModelSet.java
│   └── package-info.java
├── listener/                        ← Enhanced: added CustomPackReloadListener
│   ├── CustomPackReloadListener.java (NEW)
│   ├── EmojiReloadListener.java
│   └── package-info.java
├── loader/                          ← NEW: loader subsystem extracted from old CustomPackLoader
│   ├── ChairPackLoader.java          (chair-specific pack loading)
│   ├── CustomPackLoader.java         (thin orchestrator; old version was 700+ lines)
│   ├── CustomPackReader.java         (folder/zip traversal)
│   ├── CustomPackTextureLoader.java  (texture registration via ResourceAccessor)
│   ├── LanguageLoader.java           (moved from top-level)
│   ├── MaidPackLoader.java           (maid-specific pack loading)
│   └── PackLoaderHelper.java         (shared loading logic)
├── models/                          ← Enhanced
│   ├── AbstractClientModels.java     (NEW: base class)
│   ├── ChairModels.java
│   ├── DefaultPackConstant.java
│   ├── MaidModels.java
│   ├── PlayerMaidModels.java
│   └── SpecialMaidModelResolver.java (NEW)
└── pojo/                            ← Unchanged
```

### Key Resource Loading Changes

#### 1. ResourceAccessor Abstraction (NEW)
- **Old**: Two code paths — one for folders (`Path rootPath`) and one for zip files (`ZipFile zipFile`). Every method in `CustomPackLoader` was duplicated.
- **New**: `ResourceAccessor` interface with `FileResourceAccessor` and `ZipResourceAccessor` implementations. All loading logic is written once against the interface.
- **Migration note**: If you had custom pack loaders, refactor them to use `ResourceAccessor`.

#### 2. CustomPackLoader Shrunk Dramatically
- **Old**: 727-line monolithic class handling folder traversal, zip scanning, maid/chair model loading (both bedrock and gecko variants), texture registration, easter egg handling, and domain extraction.
- **New**: 107-line orchestrator that delegates to `CustomPackReader::readFolder`/`readZip`, which in turn dispatches to `MaidPackLoader`, `ChairPackLoader`, `LanguageLoader`, and `CustomSoundLoader`.
- **Old methods removed**: `readModelFromFolder`, `readModelFromZipFile`, all `loadMaidModelPack`/`loadChairModelPack`/`loadMaidModelElement`/`loadGeckoMaidModelElement` variants (8 methods), `putMaidEasterEggData`, `putMaidModelData`.

#### 3. BedrockModelLoader → InternalBedrockModelRegistry + Manager
- **Old**: `BedrockModelLoader` was a single class with static fields and an event handler. Models were registered via `BedrockEntityModelRegisterEvent`.
- **New**: Split into:
  - `InternalBedrockModelRegistry` — static registration of internal bedrock models (maid bed, chess pieces, backpacks, etc.)
  - `InternalBedrockModelManager` — runtime singleton that owns `InternalBedrockModelSet<SimpleBedrockModel>` and `InternalBedrockModelSet<SimpleBedrockEntityModel>`
  - `InternalBedrockModelSet` — self-registering reload listener pattern
- **Migration note**: Call `InternalBedrockModelManager.INSTANCE.getModel(location)` instead of `BedrockModelLoader.getModel(location)`.

#### 4. GeckoModelLoader Dissolved
- **Old**: `GeckoModelLoader` at top level handled geo model registration, animation file merging, and default animation loading.
- **New**: Functionality split into:
  - `CustomPackBedrockModelParser` — parses bedrock/gecko model files from `ResourceAccessor`
  - `GeckoContainerBuilder` — builds `GeckoContainer` objects for the animation system
  - Animation registration happens through `GeckoContainer` in the `onSetupAnimationController` pattern

#### 5. LanguageLoader Moved
- From `resource/LanguageLoader.java` → `resource/loader/LanguageLoader.java`
- API unchanged (still `LanguageLoader.readLanguageFile(path, domain)`)

#### 6. LegacyPackRepositorySource Removed
- The old `LegacyPackRepositorySource` that added a built-in resource pack for legacy resources was removed. It was tied to `AddPackFindersEvent` which is no longer subscribed in `ClientSetupEvent`.

#### 7. New Models and Base Classes
- `AbstractClientModels` — new abstract base class for `MaidModels`, `ChairModels`, and `PlayerMaidModels`
- `SpecialMaidModelResolver` — new helper for resolving special maid models

---

## Animation System Changes

### Custom JS Animation Removed
- **Removed files**: `CustomJsAnimationManger.java` and the entire `script/` subpackage:
  - `EntityChairWrapper.java`
  - `EntityMaidWrapper.java`
  - `GlWrapper.java`
  - `ModelRendererWrapper.java`
  - `WorldWrapper.java`
- **Impact**: Custom JavaScript animations (`.js` animation files in model packs) are no longer supported. All animations now use the GeckoLib/bedrock animation format and MoLang expressions.

### Gecko Animation Subpackage Enhanced

| File | Status | Notes |
|------|--------|-------|
| `gecko/AnimationManager.java` | **MODIFIED** | Updated for new render pipeline |
| `gecko/AnimationRegister.java` | MODIFIED | — |
| `gecko/AnimationState.java` | MODIFIED | — |
| `gecko/Priority.java` | KEPT | — |
| `gecko/condition/` | KEPT | Condition system preserved |
| `gecko/magic/` | KEPT | Magic casting animation preserved |
| `gecko/molang/` | KEPT | MoLang support preserved |
| `gecko/controller/` | **NEW** | Animation controller subsystem |
| `gecko/EntityTickStates.java` | **NEW** | Per-entity tick state tracking |
| `gecko/GeckoUpdateManager.java` | **NEW** | Centralized animation update management |

### Inner Animation System
All inner animations in `inner/` were preserved but updated:
- `InnerAnimation.java` — now uses `EntityRenderState` generics
- `MaidBaseAnimation`, `MaidTaskAnimation`, `MaidExtraAnimation`, etc. — updated render state types

### Hardcoded Animation
- `HardcodedAnimationManger.java` — still exists but refactored: no longer called by `GeckoMaidEntity.setCustomAnimations()` (the new entity model handles head rotation internally via `codeAnimation()`)

---

## GUI System Changes

### Removed
| File | Reason |
|------|--------|
| `gui/entity/cache/` (entire subpackage) | Cache icon system refactored; cache now managed via `CustomPackTextureLoader` |

### Preserved (modified)
- `gui/block/MaidBeaconGui.java`, `ModelSwitcherGui.java`, `ModelSwitcherModelGui.java`
- `gui/entity/ModelDownloadGui.java` + `detail/`, `maid/`, `model/` subpackages
- `gui/item/FoxScrollScreen.java`, `NameTagGui.java`, `PicnicBasketContainerScreen.java`, `ServantBellSetScreen.java`, `WirelessIOConfigSlotGui.java`, `WirelessIOContainerGui.java`
- `gui/mod/OptifineScreen.java`, `PatchouliWarningScreen.java`
- `gui/sound/MaidSoundPackGui.java`
- `gui/widget/ai/`, `gui/widget/button/`

---

## Rendering API Changes (Minecraft 1.21.1)

### Core API Migration

| Old API | New API |
|---------|---------|
| `PoseStack` / `MatrixStack` | `Matrix4f` (JOML), directly via `net.minecraft.client.renderer.MultiBufferSource` |
| `ResourceLocation` | `Identifier` (NeoForge rename; project uses `Identifier` throughout new code) |
| `VertexConsumer` with `PoseStack` | Direct `Matrix4f` + `Matrix3f` transforms |
| `RenderType` old constants | New `RenderType` hierarchy |
| `Minecraft.getInstance().getTextureManager()` direct | Uses `CustomPackTextureLoader` with `ResourceAccessor` |
| `RenderSystem` static calls | Modern equivalents or extracted |

### Entity Render State System (NEW)
Minecraft 1.21.1 introduces an `EntityRenderState` pattern where rendering data is extracted from entities into lightweight state objects. This affected the entire renderer chain:

New state classes:
- `renderer/entity/state/EntityMaidRenderState.java` — maid render state
- `renderer/entity/state/EntityChairRenderState.java` — chair render state
- `renderer/tileentity/state/` — block entity render states

### GeckoLib Render Data System (NEW)
- `geckolib3/geo/GeckoRenderData.java` — new base class
- `renderer/entity/gecko/GeckoMaidRenderData.java` — extends with climb rotation, overlay UV
- `geckolib3/geo/RenderContext.java` — new context object passed through rendering

### Renderer Package Changes

| Change | Detail |
|--------|--------|
| **NEW**: `renderer/backpack/` | `BigBackpackRenderData`, `MiddleBackpackRenderData`, `SmallBackpackRenderData` |
| **NEW**: `renderer/entity/gecko/` | Gecko-specific render data (`GeckoMaidRenderData`) |
| **NEW**: `renderer/entity/state/` | Entity render states |
| **NEW**: `renderer/tileentity/state/` | Block entity render states |
| **REMOVED**: `renderer/entity/geckolayer/` | Gecko layer rendering moved to new system |
| **REMOVED**: `renderer/entity/GeckoEntityChairRenderer.java` | Merged into `EntityChairRenderer` |
| **REMOVED**: `renderer/entity/GeckoEntityMaidRenderer.java` | Merged into `EntityMaidRenderer` |
| **REMOVED**: `renderer/entity/EntityMarisaYukkuriSlimeRender.java` | Removed |
| **REMOVED**: `renderer/entity/EntityYukkuriSlimeRender.java` | Removed |
| **REMOVED**: `renderer/entity/ReplaceExperienceOrbRenderer.java` | Removed |
| **REMOVED**: `renderer/item/ReplaceableBakedModel.java` | Removed entire `item/` subpackage |
| **REPLACED**: `renderer/texture/CacheIconTexture.java` | Now handled by `CustomPackTextureLoader` |
| **REPLACED**: `renderer/texture/FilePackTexture.java` | Now uses `FileResourceAccessor` |
| **REPLACED**: `renderer/texture/ZipPackTexture.java` | Now uses `ZipResourceAccessor` |
| **REMOVED**: `renderer/texture/GifTexture.java` | GIF support removed |
| **REMOVED**: `renderer/texture/SizeTexture.java` | Removed |
| **REMOVED**: `renderer/tileentity/PicnicBasketRender.java` | Replaced by `TileEntityItemStackPicnicBasketRenderer.java` |
| **REMOVED**: `renderer/tileentity/TileEntityEntityPlaceholderRenderer.java` | Entity placeholder removed |

---

## Init & Event Handler Changes

### Init Package

| File | Status | Notes |
|------|--------|-------|
| `ClientSetupEvent.java` | **MODIFIED** | Key mapping, pack finders, reload listeners extracted to separate classes; added IrisCompat |
| `InitClientTooltip.java` | MODIFIED | Updated for removed `ClientBoardStateTooltip` |
| `InitContainerGui.java` | MODIFIED | Updated for new GUI registration |
| `InitEntitiesRender.java` | MODIFIED | Updated for new renderer registration |
| `InitSpecialItemRender.java` | **REMOVED** | No longer needed |
| `ClientReloadListenerRegistry.java` | **NEW** | Extracted from `ClientSetupEvent` — handles all client reload listeners |
| `KeyMappingRegister.java` | **NEW** | Extracted from `ClientSetupEvent` — handles key mapping registration; adds `MAID_CATEGORY` |

### ClientSetupEvent Key Differences

```java
// OLD annotation:
@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT, modid = ...)

// NEW annotation (bus=MOD is default, removed):
@EventBusSubscriber(value = Dist.CLIENT, modid = ...)
```

**Removed event handlers** (moved to dedicated classes):
- `onRegisterKeyMappings(RegisterKeyMappingsEvent)` → `KeyMappingRegister`
- `onAddPackFinders(AddPackFindersEvent)` → **REMOVED** (LegacyPackRepositorySource deleted)
- `onRegisterClientReloadListeners(RegisterClientReloadListenersEvent)` → `ClientReloadListenerRegistry`

**New compat**: `IrisCompat` added alongside existing `OculusCompat`.

### Event Package Changes

| File | Status |
|------|--------|
| `ReloadResourceEvent.java` | **REMOVED** — resource reloading now handled by `CustomPackReloadListener` |
| `SpecialMaidRenderEvent.java` | **REMOVED** — special model rendering refactored |
| `ClientRecipeEvent.java` | **NEW** |
| `RegisterSpecialModelEvent.java` | **NEW** |
| `UseNameTagEvent.java` | **NEW** |

### Entity Package Changes

#### GeckoMaidEntity — Complete Rewrite
- **Old**: Type parameter `T extends Mob` (generic), manually managed `MaidState<T>` inner class, registered animation controllers imperatively in `registerControllers()`, used `setCustomAnimations()` for head rotation and hardcoded animation
- **New**: Type parameter `T extends EntityMaid` (specific), uses `GeckoMaidStateTracker<T>` for state tracking, controller setup delegated to `onSetupAnimationController()` via `GeckoContainer`, head rotation handled via `codeAnimation()`/`recoverLastCodedAnimation()`, async update support via `asyncUpdate()`, frame rate varies by render state

**Key new features**:
- `extractRenderData()` — populates `GeckoMaidRenderData` with overlay UV and climb rotation
- `determinImmutableContext()` — optimizes for non-entity render states
- `getFrameRateLimit()` — GUI preview uses screen refresh rate, statues at 30 FPS
- `preAnimationSetup()` — fires `MAID_INIT` MoLang event on first frame, executes `MAID_UPDATE` handler each tick
- `wrappedUpdateHandler` — MoLang event wrapper for per-tick update logic

#### GeckoMaidStateTracker (NEW)
Extends `EntityStateTracker<T>`, handles:
- Immersive melodies data updates per render tick
- Render tick data management

#### GeckoChairEntity — Simplified
- **Old**: Had animation controller registration with `DEFAULT_CHAIR_ANIMATION` fallback
- **New**: Delegate controller setup to `GeckoContainer.controllerFactory()`, removed manual animation handling

---

## New Packages

### `client/particle/ParticleSpawner.java`
MoLang-integrated particle spawning system:
- Parses particle IDs via `ParticleArgument`
- Supports absolute/relative positioning
- Single and multi-particle spawning with random distribution
- Particle type caching (60s TTL)
- Used from MoLang expressions via `evalSpawnParticle()`

### `client/proxy/ItemServantBellProxy.java`
Client-side proxy for opening the servant bell configuration screen from item use.

### `client/extensions/BlockAltarExtensions.java`
Implements `IClientBlockExtensions` for the Altar block:
- `addHitEffects()` — spawns particles matching the stored block state (not the altar itself)
- `addDestroyEffects()` — uses stored block state for destroy particles
- Allows the Altar to visually appear as whatever block it's storing

---

## Model Package Changes

| File | Status |
|------|--------|
| `AbstractModel.java` | **REMOVED** — base class no longer needed |
| `EntityPlaceholderModel.java` | **REMOVED** — entity placeholder feature removed |
| `bedrock/EntityMaidModel.java` | **NEW** — specialized bedrock model for maids |
| `bedrock/EntityChairModel.java` | **NEW** — specialized bedrock model for chairs |
| `bedrock/SimpleBedrockEntityModel.java` | **NEW** — generic entity bedrock model with render state |
| `bedrock/SimpleBedrockModel.java` | MODIFIED |
| All other models | MODIFIED — updated for new render API |

---

## Tooltip Changes

| File | Status |
|------|--------|
| `ClientBoardStateTooltip.java` | **REMOVED** |
| `ClientItemContainerTooltip.java` | KEPT (modified) |
| `ClientMaidTooltip.java` | KEPT (modified) |

---

## Sound Package

The `sound/` package was structurally preserved. However, the sound data classes may have been updated for the new audio pipeline. The `record/MicrophoneManager.java` was kept.

---

## File Inventory

| File Path | Status | Migration Notes |
|-----------|--------|-----------------|
| `animation/CustomJsAnimationManger.java` | **REMOVED** | JS animations no longer supported |
| `animation/script/` (5 files) | **REMOVED** | JS animation wrappers deleted |
| `animation/gecko/controller/` | **NEW** | Animation controller subsystem |
| `animation/gecko/EntityTickStates.java` | **NEW** | Per-entity tick state |
| `animation/gecko/GeckoUpdateManager.java` | **NEW** | Animation update manager |
| `animation/gecko/AnimationManager.java` | MODIFIED | Updated for render pipeline |
| `animation/gecko/AnimationRegister.java` | MODIFIED | Updated |
| `animation/gecko/AnimationState.java` | MODIFIED | Updated |
| `animation/HardcodedAnimationManger.java` | MODIFIED | Refactored for new entity model |
| `animation/inner/InnerAnimation.java` | MODIFIED | EntityRenderState generics added |
| `animation/inner/*Animation.java` (10 files) | MODIFIED | Updated state types |
| `animation/special/` | MODIFIED | Updated |
| `download/` (5 files) | KEPT | No structural changes |
| `entity/GeckoMaidEntity.java` | **REWRITTEN** | New render state model, async updates, GeckoContainer |
| `entity/GeckoMaidStateTracker.java` | **NEW** | Render tick tracking for maids |
| `entity/GeckoChairEntity.java` | MODIFIED | Simplified via GeckoContainer |
| `event/ReloadResourceEvent.java` | **REMOVED** | Replaced by CustomPackReloadListener |
| `event/SpecialMaidRenderEvent.java` | **REMOVED** | Refactored |
| `event/ClientRecipeEvent.java` | **NEW** | |
| `event/RegisterSpecialModelEvent.java` | **NEW** | |
| `event/UseNameTagEvent.java` | **NEW** | |
| `event/` (11 other files) | MODIFIED | Updated for API changes |
| `gui/entity/cache/` | **REMOVED** | Cache system refactored |
| `gui/` (all other files) | MODIFIED | Updated for 1.21.1 |
| `init/ClientSetupEvent.java` | MODIFIED | Extracted key mapping, pack finders, reload listeners |
| `init/ClientReloadListenerRegistry.java` | **NEW** | Centralized reload listener registration |
| `init/KeyMappingRegister.java` | **NEW** | Centralized key mapping registration |
| `init/InitSpecialItemRender.java` | **REMOVED** | No longer needed |
| `init/InitClientTooltip.java` | MODIFIED | Updated tooltip list |
| `init/InitContainerGui.java` | MODIFIED | Updated GUI registration |
| `init/InitEntitiesRender.java` | MODIFIED | Updated renderer registration |
| `input/` (2 files) | MODIFIED | Key mappings use `MAID_CATEGORY` |
| `model/AbstractModel.java` | **REMOVED** | |
| `model/EntityPlaceholderModel.java` | **REMOVED** | Feature removed |
| `model/bedrock/EntityMaidModel.java` | **NEW** | |
| `model/bedrock/EntityChairModel.java` | **NEW** | |
| `model/bedrock/SimpleBedrockEntityModel.java` | **NEW** | |
| `model/bedrock/SimpleBedrockModel.java` | MODIFIED | |
| `model/PlayerMaidModel.java` | MODIFIED | Updated |
| `model/*Model.java` (5 other files) | MODIFIED | Updated |
| `overlay/` (3 files) | MODIFIED | Updated |
| `particle/ParticleSpawner.java` | **NEW** | MoLang particle spawning |
| `proxy/ItemServantBellProxy.java` | **NEW** | Client-side proxy |
| `extensions/BlockAltarExtensions.java` | **NEW** | Altar block client extensions |
| `renderer/backpack/` (3 files) | **NEW** | Backpack render data |
| `renderer/entity/gecko/` | **NEW** | Gecko render data |
| `renderer/entity/state/` | **NEW** | Entity render states |
| `renderer/entity/geckolayer/` | **REMOVED** | Merged into new system |
| `renderer/entity/GeckoEntityChairRenderer.java` | **REMOVED** | Merged |
| `renderer/entity/GeckoEntityMaidRenderer.java` | **REMOVED** | Merged |
| `renderer/entity/EntityMarisaYukkuriSlimeRender.java` | **REMOVED** | |
| `renderer/entity/EntityYukkuriSlimeRender.java` | **REMOVED** | |
| `renderer/entity/ReplaceExperienceOrbRenderer.java` | **REMOVED** | |
| `renderer/item/ReplaceableBakedModel.java` | **REMOVED** | |
| `renderer/texture/CacheIconTexture.java` | **REMOVED** | Via CustomPackTextureLoader |
| `renderer/texture/FilePackTexture.java` | **REMOVED** | Via FileResourceAccessor |
| `renderer/texture/GifTexture.java` | **REMOVED** | GIF support removed |
| `renderer/texture/ZipPackTexture.java` | **REMOVED** | Via ZipResourceAccessor |
| `renderer/texture/SizeTexture.java` | **REMOVED** | |
| `renderer/texture/CustomPackTexture.java` | KEPT | Modified |
| `renderer/tileentity/PicnicBasketRender.java` | **REMOVED** | Replaced by ItemStack version |
| `renderer/tileentity/TileEntityEntityPlaceholderRenderer.java` | **REMOVED** | Feature removed |
| `renderer/tileentity/TileEntityItemStackPicnicBasketRenderer.java` | **NEW** | |
| `renderer/tileentity/state/` | **NEW** | BE render states |
| `resource/accessor/` (3 files) | **NEW** | Unified resource access |
| `resource/bedrock/CustomPackBedrockModelParser.java` | **NEW** | From old GeckoModelLoader |
| `resource/bedrock/GeckoContainerBuilder.java` | **NEW** | Gecko container construction |
| `resource/bedrock/InternalBedrockModelManager.java` | **NEW** | From old BedrockModelLoader |
| `resource/bedrock/InternalBedrockModelRegistry.java` | **NEW** | Bedrock model registration |
| `resource/bedrock/InternalBedrockModelSet.java` | **NEW** | Reload listener pattern |
| `resource/loader/ChairPackLoader.java` | **NEW** | Extracted from CustomPackLoader |
| `resource/loader/CustomPackLoader.java` | **REWRITTEN** | 727→107 lines, orchestrator only |
| `resource/loader/CustomPackReader.java` | **NEW** | Folder/zip traversal |
| `resource/loader/CustomPackTextureLoader.java` | **NEW** | Texture registration |
| `resource/loader/LanguageLoader.java` | **MOVED** | From resource/ top level |
| `resource/loader/MaidPackLoader.java` | **NEW** | Extracted from CustomPackLoader |
| `resource/loader/PackLoaderHelper.java` | **NEW** | Shared loading logic |
| `resource/listener/CustomPackReloadListener.java` | **NEW** | Replaces ReloadResourceEvent |
| `resource/listener/EmojiReloadListener.java` | MODIFIED | Updated |
| `resource/models/AbstractClientModels.java` | **NEW** | Base class for model managers |
| `resource/models/SpecialMaidModelResolver.java` | **NEW** | |
| `resource/models/*.java` (4 files) | MODIFIED | Updated |
| `resource/pojo/` (5 files) | MODIFIED | Updated for Identifier |
| `resource/BedrockModelLoader.java` | **REMOVED** | → bedrock/ subpackage |
| `resource/CustomPackLoader.java` | **MOVED** | → loader/ subpackage |
| `resource/GeckoModelLoader.java` | **REMOVED** | Split into bedrock/ classes |
| `resource/LanguageLoader.java` | **MOVED** | → loader/ subpackage |
| `resource/LegacyPackRepositorySource.java` | **REMOVED** | Legacy pack support removed |
| `sound/` (13 files) | MODIFIED | Updated for 1.21.1 |
| `tooltip/ClientBoardStateTooltip.java` | **REMOVED** | |
| `tooltip/ClientItemContainerTooltip.java` | MODIFIED | |
| `tooltip/ClientMaidTooltip.java` | MODIFIED | |
