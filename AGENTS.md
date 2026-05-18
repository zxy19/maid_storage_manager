# AGENTS.md

## Project Overview
NeoForge 1.21.1 mod (`maid_storage_manager`) that adds storage/logistics/crafting AI jobs to Touhou Little Maid.

- **Language**: Java 21
- **Build**: Gradle, NeoForge MDK (moddev 2.0.89)
- **Package**: `studio.fantasyit.maid_storage_manager`
- **Main class**: `MaidStorageManager.java` (`@Mod`)
- **License**: MIT

## Tool Usage
**MANDATORY: All file operations (read, search, find symbols, build) MUST go through IDEA MCP tools.** Never use `Read`, `Glob`, `Grep`, `Bash` or any raw filesystem tool for source code access. Using raw tools will produce stale/incorrect results because they bypass the IDE's indexing and symbol resolution. This is non-negotiable.

## Build & Run
| Command | Purpose |
|---------|---------|
| `gradlew build` | Full build (jar) |
| `gradlew jar` | Build only jar |
| `gradlew clean` | Clean |
| Run config `Client` (in IDE) | Launch Minecraft client |
| Run config `Server` (in IDE) | Launch dedicated server |
| Run config `Data` (in IDE) | Run data generation |
| Run config `GameTestServer` | Run game tests |

All run configs use `--mixin.config maid_storage_manager.mixins.json`. Working directory is `run/`.

## Architecture
### Core Directories
- `maid/` — Maid AI: behaviors, tasks, memory modules, config
- `storage/` — Container access: ItemHandler, AE2, RS, Mekanism QIO, Create stock ticker
- `craft/` — Auto-crafting: algorithm (topology/DFS solvers), context, data, generators, work blocks
- `integration/` — Optional mod compat (JEI, EMI, KubeJS, Create, Mekanism, Jade, SOPH, TACZ)
- `items/` — Custom items (RequestList, StorageList, FilterItem, baubles, etc.)
- `menu/` — GUI screens (filter, request, craft, logistics, communicate)
- `network/` — Custom network packets (`Network.java` is the channel registry)
- `mixin/` — Mixin classes (mixins.json plugin: `IntegrationMixinControlPlugin`)
- `registry/` — Registration classes (items, gui, memory modules, recipes, etc.)
- `event/` — Forge event handlers

### Key Files
- `Config.java` — All mod config (NeoForge ModConfigSpec), loaded via Config static fields
- `Logger.java` — SLF4J wrapper; `Logger.debug()` is gated on `Config.enableDebug`
- `maid/MaidExtension.java` — Maid capability extension
- `storage/MaidStorage.java` — Central storage manager
- `craft/CraftManager.java` — Crafting orchestration
- Access Transformer: `src/main/resources/META-INF/accesstransformer.cfg`
- Mixin config: `src/main/resources/maid_storage_manager.mixins.json`

### Required Dependency
`touhou_little_maid` (local jar in `libs/`). Many optional mods are `compileOnly` at build time and `runtimeOnly` for dev testing.

## Code Style
- Uses official Mojang mappings (`mapping_channel=official`)
- No formal tests exist (`src/test/java/` is empty)
- Register things via `Registry` classes in `studio.fantasyit.maid_storage_manager.registry`
- Network packet classes use `C2S`/`S2C` suffix convention
- Item classes descend from `MaidInteractItem`
- Config is accessed via static fields (e.g. `Config.enableDebug`)
- Logging via `Logger.info/warn/error/debug` with format strings
