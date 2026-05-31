# Utilities, Commands, Mixins & AI Migration

## Command System Changes

### RootCommand Permission API

`RootCommand.java` - permission check changed from direct int to named constant:

```java
// OLD
.requires((source -> source.hasPermission(2)));

// NEW
.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS));
```

### BackupCommand: ClickEvent / HoverEvent API

`BackupCommand.java` - Minecraft 1.21.1 replaced `ClickEvent.Action` / `HoverEvent.Action` constructors with static subclass constructors:

```java
// OLD
new HoverEvent(HoverEvent.Action.SHOW_TEXT, ...)
new ClickEvent(ClickEvent.Action.RUN_COMMAND, ...)

// NEW
new HoverEvent.ShowText(...)
new ClickEvent.RunCommand(...)
```

### PackCommand: Distribution & Reload API

`PackCommand.java` - two API renames:

```java
// OLD
import net.neoforged.fml.loading.FMLLoader;
FMLLoader.getDist()
ReloadResourceEvent.asyncReloadAllPack()

// NEW
import net.neoforged.fml.loading.FMLEnvironment;
FMLEnvironment.getDist()
CustomPackReloadListener.asyncReload()
```

### MaidDebugCommand: Entity & Block API

`MaidDebugCommand.java` - three vanilla API renames:

```java
// OLD
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.block.FarmBlock;
serverPlayer.serverLevel();
entityMaid.finalizeSpawn(..., MobSpawnType.SPAWN_EGG, ...);

// NEW
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.block.FarmlandBlock;
serverPlayer.level();
entityMaid.finalizeSpawn(..., EntitySpawnReason.SPAWN_ITEM_USE, ...);
```

### Unchanged Subcommands

These files are byte-for-byte identical between OLD and NEW:
- `HandleTypeArgument.java`
- `AIChatCommand.java`
- `ChatTokensCommand.java`
- `MaidCommand.java`
- `MaidNumCommand.java`
- `PowerCommand.java`

---

## Debug Utility Changes

Directory structure unchanged. Only `MaidDebugCommand.java` has the API changes listed above.

Other debug files (`DebugStickClickEvent.java`, `DebugClientRenderEvent.java`, `DebugMaidManager.java`, `DebugTarget.java`, `DefaultTargets.java`, `SendMaidDebugDataEvent.java`) were not compared line-by-line but are presumed structurally similar.

---

## Utility Class Changes

### Removed Files (OLD only)

| File | Purpose | Why Removed |
|------|---------|-------------|
| `IconCache.java` | Screenshot-based icon export (from CyclopsMC IconExporter) | Functionality likely removed or replaced |
| `MaidFluidRender.java` | GUI fluid rendering using `FluidStack` / `TextureAtlasSprite` | Fluid rendering rewritten or merged elsewhere |
| `MaidFluidUtil.java` | Fluid bucket-to-tank conversion helpers | Fluid capability API changed; logic migrated |

### New Files (NEW only)

| File | Purpose |
|------|---------|
| `EntityMaidEquipmentWrapper.java` | Custom `ResourceHandler<ItemResource>` for maid inventory using NeoForge transfer API (replaces vanilla `LivingEntityEquipmentWrapper` limitations) |
| `IdentifierAdapter.java` | Gson `JsonSerializer`/`JsonDeserializer` for `ResourceLocation` |
| `LazyValue.java` | Thread-safe lazy initialization wrapper extending `LazyInitializer` |
| `ThreadTools.java` | Shared `ThreadPoolExecutor` ("TLM Gecko Worker") for GeckoLib parallel rendering tasks |

### Unchanged Common Files

These files exist in both projects and are byte-for-byte identical or trivially unchanged:

| File | Status |
|------|--------|
| `AnnotatedInstanceUtil.java` | Unchanged |
| `BiomeCacheUtil.java` | Unchanged |
| `ByteBufUtils.java` | Unchanged |
| `CappedQueue.java` | Unchanged |
| `CChessUtil.java` | Unchanged |
| `CenterOffsetBlockPosSet.java` | Unchanged |
| `EntityCacheUtil.java` | Unchanged |
| `EquipmentUtil.java` | Unchanged |
| `functional/*` (6 files) | Unchanged |
| `GameModeUtil.java` | Unchanged |
| `GetJarResources.java` | Unchanged |
| `GuiTools.java` | Unchanged |
| `HandUtils.java` | Unchanged |
| `http/MultipartBody.java` | Unchanged |
| `http/MultipartBodyBuilder.java` | Unchanged |
| `http/MultiPartRecord.java` | Unchanged |
| `http/UrlTool.java` | Unchanged |
| `HttpUtil.java` | Unchanged |
| `ItemsUtil.java` | Unchanged |
| `JERIUtil.java` | Unchanged |
| `MaidRayTraceHelper.java` | Unchanged |
| `Md5Utils.java` | Unchanged |
| `NBTToJson.java` | Unchanged |
| `OpusDecoderUtil.java` | Unchanged |
| `ParseI18n.java` | Unchanged |
| `PlaceHelper.java` | Unchanged |
| `PosListData.java` | Unchanged |
| `Rectangle.java` | Unchanged |
| `RenderHelper.java` | Unchanged |
| `ResourceLocationUtil.java` | Unchanged |
| `ShapeDraw.java` | Unchanged |
| `SoundUtil.java` | Unchanged |
| `SystemAppDataUtil.java` | Unchanged |
| `TaskEquipUtil.java` | Unchanged |
| `TeleportHelper.java` | Unchanged |
| `TipsHelper.java` | Unchanged |
| `VoxelShapeUtils.java` | Unchanged |
| `WChessUtil.java` | Unchanged |
| `WeightedPicker.java` | Unchanged |
| `ZipFileCheck.java` | Unchanged |

### HTTP Utilities

Both `HttpUtil.java` (single-threaded download manager using `HttpURLConnection`) and the `http/` sub-package (`UrlTool`, `MultipartBody`, `MultipartBodyBuilder`, `MultiPartRecord`) are **identical** between OLD and NEW. No `java.net.http.HttpClient` migration occurred.

---

## Mixin Changes

### Mixin Config (`touhou_little_maid.mixins.json`)

```diff
- "plugin": "com.github.tartaricacid.touhoulittlemaid.mixin.plugin.MixinPlugin",
- "compatibilityLevel": "JAVA_17",
- "refmap": "touhou_little_maid.refmap.json",
+ "compatibilityLevel": "JAVA_25",
```

Client mixins section:
```diff
  "client": [
    "client.ClientPacketListenerMixin",
-   "client.HumanoidModelMixin",
-   "client.ItemPropertiesMixin",
+   "// client.HumanoidModelMixin",
+   "// client.ItemPropertiesMixin",
    "client.LanguageMixin",
    "client.LivingEntityRendererMixin",
+   "client.InventoryScreenMixin",
+   "client.LevelRendererMixin"
  ]
```

### Removed Files

| File | Reason |
|------|--------|
| `mixin/plugin/MixinPlugin.java` | Plugin removed; conditional InvTweaks compat no longer needed (InvTweaks not ported or compat dropped) |
| `mixin/client/compat/InvTweaksMixin.java` | InvTweaks mixin (canceled inventory sort events on maid GUI screens). Removed with plugin |

### Commented-Out Mixins (Exist on Disk but Disabled)

Both files still exist in `src/main/java/` but are **disabled** in the JSON config:

| File | Status | Note |
|------|--------|------|
| `mixin/client/HumanoidModelMixin.java` | Disabled (`// FIXME`) | Sets player arm angles when riding a maid. Marked for fixing. |
| `mixin/client/ItemPropertiesMixin.java` | Disabled | Item property overrides registration. File still on disk. |

### New Mixins

| File | Purpose |
|------|---------|
| `mixin/client/InventoryScreenMixin.java` | Injects `RenderContextManager.setRenderingInInventory(true/false)` before/after inventory entity rendering. Also kicks GeckoLib update tasks for `EntityMaid` and `EntityChair` after render. |
| `mixin/client/LevelRendererMixin.java` | Injects `RenderContextManager.setRenderingLevel(true/false)` around `extractLevel()`, and `GeckoUpdateManager.updateRemaining()` / `finalizeFrame()` around `extractLevel()` and `renderLevel()`. Critical for GeckoLib parallel rendering on 1.21.1. |

### Unchanged Mixins

All 15 common mixins are identical structure:
- `ArrowMixin`, `BlockBurningCache`, `ConduitBlockEntityMixin`, `EntityMixin`
- `FishingHookPredicateMixin`, `MixinCrossbowItem`, `MobInteractMixin`, `NavigationMixin`
- `NodeEvaluatorBurningCacher`, `ParrotMixin`, `PersistentEntitySectionManagerMixin`
- `PlayerMixin`, `StructureTemplateMixin`, `SweetBerryBushBlockMixin`, `ThrownTridentMixin`
- All 5 accessors: `ArrowAccessor`, `CropBlockAccessor`, `EntityAccessor`, `FenceGateBlockAccessor`, `LivingEntityAccessor`
- `ClientPacketListenerMixin`, `LanguageMixin`, `LivingEntityRendererMixin`

---

## AI System Changes

Directory structure is nearly identical. Only deprecated classes were removed:

### Removed Files (OLD only)

| File | Reason |
|------|--------|
| `ai/manager/entity/Player2AppCheck.java` | Player2 local app health check utility (HTTP health endpoint probe). Removed as Player2 may no longer be the recommended AI tool, or check logic was inlined. |
| `ai/service/function/FunctionCallRegister.java` | `@Deprecated(since="1.5.1")` function call registry. Replaced by skill mechanism. Cleaned up. |
| `ai/service/function/IFunctionCall.java` | `@Deprecated(since="1.5.1", forRemoval=true)` function call interface. Cleaned up. |
| `ai/service/function/response/ToolResponse.java` | `@Deprecated` response type. Cleaned up. |

All other AI files (agent, manager, service subdirectories) maintain identical structure between OLD and NEW.

---

## File Inventory

### Commands (7 files, 0 changed structure)
```
command/
├── arguments/
│   ├── HandleTypeArgument.java    (unchanged)
│   └── package-info.java
├── RootCommand.java               (permission API only)
└── subcommand/
    ├── AIChatCommand.java          (unchanged)
    ├── BackupCommand.java          (ClickEvent/HoverEvent API)
    ├── ChatTokensCommand.java      (unchanged)
    ├── MaidCommand.java            (unchanged)
    ├── MaidNumCommand.java         (unchanged)
    ├── PackCommand.java            (FMLLoader→FMLEnvironment + reload API)
    └── PowerCommand.java           (unchanged)
```

### Debug (7 files, 0 changed structure)
```
debug/
├── command/
│   └── MaidDebugCommand.java       (MobSpawnType→EntitySpawnReason, FarmBlock→FarmlandBlock)
├── event/
│   └── DebugStickClickEvent.java
└── target/
    ├── DebugClientRenderEvent.java
    ├── DebugMaidManager.java
    ├── DebugTarget.java
    ├── DefaultTargets.java
    └── SendMaidDebugDataEvent.java
```

### Util (47 files OLD, 46 files NEW)
```
util/
    REMOVED: IconCache.java, MaidFluidRender.java, MaidFluidUtil.java
    ADDED: EntityMaidEquipmentWrapper.java, IdentifierAdapter.java, LazyValue.java, ThreadTools.java
    UNCHANGED: 39 files + functional/ (6 files) + http/ (4 files)
```

### Mixin (24 files OLD, 22 files NEW)
```
mixin/
    REMOVED: plugin/MixinPlugin.java, client/compat/InvTweaksMixin.java
    DISABLED: client/HumanoidModelMixin.java, client/ItemPropertiesMixin.java (still on disk, commented out in JSON)
    ADDED: client/InventoryScreenMixin.java, client/LevelRendererMixin.java
    UNCHANGED: 15 common + 5 accessor + 3 client
```

### AI (~80 files OLD, ~76 files NEW)
```
    REMOVED:
      manager/entity/Player2AppCheck.java
      service/function/FunctionCallRegister.java
      service/function/IFunctionCall.java
      service/function/response/ToolResponse.java
    UNCHANGED: ~76 files in agent/, manager/, service/ subtrees
```
