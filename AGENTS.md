# AGENTS.md

## Project Overview
NeoForge 26.1 mod (`maid_storage_manager`) that adds storage/logistics/crafting AI jobs to Touhou Little Maid.

- **Language**: Java 25
- **Build**: Gradle, NeoForge MDK (moddev 2.0.89)
- **Package**: `studio.fantasyit.maid_storage_manager`
- **Main class**: `MaidStorageManager.java` (`@Mod`)
- **License**: MIT
- 
## CRITICAL: Tool Usage Mandate

**THIS IS NON-NEGOTIABLE. VIOLATING THIS RULE MEANS CORRUPTED RESULTS AND WASTED TIME.**

When viewing **ANY file within this project** or **ANY class from external dependencies/libraries** (NeoForge, Minecraft, etc.), you **MUST ALWAYS** use the IDE-provided MCP tools:

- `idea_read_file` — for reading project files
- `idea_get_file_text_by_path` — for reading project files by path
- `idea_search_symbol` — for looking up classes/methods/fields by name
- `idea_get_symbol_info` — for inspecting a specific symbol's declaration

**YOU ARE ABSOLUTELY FORBIDDEN FROM USING**: `Read` tool (local filesystem read), `Glob`, `Grep`, `Bash` file operations, or any other non-IDE tool to **read, search, or inspect** project source files or dependency classes.

**When editing/writing code**, you may use any tool: `Edit`, `Write`, `idea_replace_text_in_file`, `idea_create_new_file` are all acceptable. There is no restriction on editing tools.

**THE ONLY EXCEPTION** is for non-Java files like `build.gradle`, `gradle.properties`, JSON resources, Gradle wrapper, `.gitignore`, and other config/asset files — these may be read with any tool.

**IF ANY IDE MCP TOOL FAILS OR RETURNS AN ERROR, OR IF THE TOOLS ARE NOT AVAILABLE IN YOUR ENVIRONMENT:**
1. **STOP IMMEDIATELY.** Do NOT proceed. Do NOT attempt to fall back to other tools.
2. Report the exact error clearly to the user and wait for instructions.
3. Under no circumstances attempt to read project Java files or dependency classes through alternative means.

This requirement exists because the IDE MCP tools understand Java semantics, resolve symbols correctly, navigate inheritance hierarchies, and decompile class files — capabilities that raw filesystem tools lack. Using raw tools on Java source will produce incomplete, misleading, or flat-out wrong results.

## CRITICAL: Subagent Usage

**THIS IS NON-NEGOTIABLE. USING THE WRONG SUBAGENT TYPE MEANS INCOMPLETE OR INCORRECT RESULTS.**

When dispatching subagents (via the `Task` tool):

- **ALWAYS use `subagent_type = "general"`. NEVER use `"explore"`.** The `explore` agent is inferior — it lacks the full toolset, cannot use IDEA MCP tools properly, and routinely produces incomplete, misleading results. Do not even consider it.

- **All subagents MUST use IDEA MCP tools** (`idea_read_file`, `idea_search_symbol`, `idea_get_symbol_info`, etc.) for reading or inspecting any Java source (project files or dependencies). Subagents are bound by the same Tool Usage Mandate above. When giving a subagent its task prompt, **you MUST explicitly instruct it** to use IDEA MCP tools for all file reads and symbol searches, and to never fall back to filesystem read tools.

- **To create a read-only subagent**, instruct it in the prompt: "You are a read-only agent. Do not create, edit, or write any files." The `general` agent will respect this. There is no need to use `explore` for read-only work — `general` with a read-only directive is always the correct choice.

- **Subagent prompts must contain**: (1) a clear instruction to use IDEA MCP tools exclusively for Java source, (2) the specific task and expected output format, (3) whether file editing is permitted or forbidden.

**YOU ARE NEVER PERMITTED TO USE `subagent_type = "explore"`. PRETEND IT DOES NOT EXIST.**

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
