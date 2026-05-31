# Gradle 构建配置迁移 (NeoForge 21.1 / MC 1.21.1 → NeoForge 26.1)

本文档记录 Touhou Little Maid 在两个版本之间 **Gradle 构建系统** 的全部变化。

---

## 1. 全景总览

| 维度 | 旧项目 (NeoForge 21.1) | 新项目 (NeoForge 26.1) |
|------|------------------------|--------------------------|
| Minecraft / NeoForge | 1.21.1 / 21.1.219 | 26.1.2 / 26.1.2.68-beta |
| Gradle | 8.14.3-all (含源码) | 9.4.1-bin (无源码) |
| NeoGradle (moddev) | 2.0.95 | 2.0.141 |
| Shadow | 8.3.6 | 9.4.1 |
| Java | JDK 21 | **JDK 25** |
| Parchment 反混淆 | 2024.11.17 | **移除** |
| 本地 libs/ | 4 个 jar | **空（不存在）** |
| 依赖声明数量 | ~30 条 | ~12 条 |

---

## 2. build.gradle 逐项对比

### 2.1 插件版本

```diff
- id 'net.neoforged.moddev' version '2.0.95'
+ id 'net.neoforged.moddev' version '2.0.141'

- id 'com.gradleup.shadow' version '8.3.6'
+ id 'com.gradleup.shadow' version '9.4.1'
```

### 2.2 Java 工具链

```diff
- java.toolchain.languageVersion = JavaLanguageVersion.of(21)
+ java.toolchain.languageVersion = JavaLanguageVersion.of(25)
```

> **注意**: JDK 21 → JDK 25 跳了两代 LTS。CI 构建文档中提到实际 CI 仍使用 JDK 21，但 Gradle toolchain 配置要求 JDK 25。实际开发建议安装 JDK 25。

### 2.3 Parchment 映射移除

旧项目在 `neoForge {}` 块中有 Parchment 配置：

```groovy
// 旧项目有，新项目已删除
neoForge {
    parchment {
        mappingsVersion = project.parchment_mappings_version     // "2024.11.17"
        minecraftVersion = project.parchment_minecraft_version   // "1.21.1"
    }
}
```

新项目直接使用 Mojang 官方映射，不再使用 Parchment。这可能导致某些参数名从 `level` 变为 `p_xxxxx_`，影响可读性但不影响功能。

### 2.4 Data Run 类型变更

```diff
  data {
-     data()
+     clientData()
      gameDirectory = project.file('run/data')
      programArguments.addAll '--mod', project.mod_id, '--all',
```

> **关键变更**: `data()` 是服务端数据生成方式，`clientData()` 是客户端数据生成方式。NeoForge 26.1 的 moddev 插件要求使用 `clientData()` 因为数据生成器现在需要客户端资源上下文。

### 2.5 新增 terminal.jline 系统属性

```diff
  configureEach {
      jvmArguments = [...]
      systemProperty 'forge.logging.markers', 'REGISTRIES'
      logLevel = org.slf4j.event.Level.DEBUG
+     systemProperty 'terminal.jline', 'true'
  }
```

启用控制台彩色输出（JLine 终端）。

### 2.6 仓库变化

| 变化 | 详情 |
|------|------|
| ✅ 保留 | Modrinth, Shedaniel, bai.lol, TerraformersMC, Cursemaven, k-4u (TOP), Curios, latvian.dev, Architectury, JitPack, Createmod, GeckoLib, Fuzss |
| ➕ 新增 | **RyanHCode Maven** — 提供 `dev.ryanhcode.sable` 和 `sable-companion` 包 |
| ➖ 移除 | 无（仓库均保留） |

新增的 RyanHCode 仓库：
```groovy
maven {
    url = "https://maven.ryanhcode.dev/releases"
    name = "RyanHCode Maven"
    content {
        includeGroup "dev.ryanhcode.sable"
        includeGroup "dev.ryanhcode.sable-companion"
    }
}
```

### 2.7 依赖变化（完整对照表）

#### Jar-in-Jar (mcLib) 依赖 — 完全相同

| 依赖 | 旧版本 | 新版本 | 状态 |
|------|--------|--------|------|
| `org.gagravarr:vorbis-java-core` | 0.8 | 0.8 | 不变 |
| `io.github.jaredmdobson:concentus` | 1.0.2 | 1.0.2 | 不变 |
| `com.googlecode.soundlibs:mp3spi` | 1.9.5.4 | 1.9.5.4 | 不变 |
| `org.yaml:snakeyaml` | 2.4 | 2.4 | 不变 |

#### ShadowJar 重定向 — 完全相同

`shadowJar {}` 块的配置（relocate、exclude、mergeServiceFiles、minimize）在新旧项目中**完全一致**。

#### 模组依赖对照表

| 编译依赖 | 旧版本 | 新版本 | 旧 scope | 新 scope | 状态 |
|----------|--------|--------|----------|----------|------|
| JEI | `19.21.0.247-neoforge` | `29.6.2.31-neoforge` | implementation | implementation | 🔄 大版本更新 |
| Cloth Config (Shedaniel) | `15.0.140+neoforge` | `26.1.154+neoforge` | implementation | implementation | 🔄 |
| Jade | `15.10.2+neoforge` | `26.1.1+neoforge` | implementation | implementation | 🔄 |
| Curios | `curios-neoforge:9.2.0+1.21.1` | `curios:15.0.0-beta.2+26.1.2` | implementation | compileOnly | 🔄 从 implementation 降级为 compileOnly |
| Sodium | `mc1.21.1-0.6.13-neoforge` | `mc26.1.2-0.8.12-neoforge` | compileOnly | compileOnly | 🔄 |
| Iris | `1.8.8+1.21.1-neoforge` | `1.10.9+26.1-neoforge` | compileOnly | compileOnly | 🔄 |
| IPN (Inventory Profiles Next) | `neoforge-1.21.1-2.1.9` | `neoforge-26.1-2.3.2` | implementation | compileOnly | 🔄 scope 降级 |
| Aquaculture | `curse.maven:60028:6357106` | `maven.modrinth:2.9.1-neoforge` | implementation | compileOnly | 🔄 来源 + scope 变更 |
| Sophisticated Core | `1.21.1-1.3.50.1004` | `26.1.2-1.4.45.1849` | implementation | compileOnly | 🔄 scope 降级 |
| Sophisticated Backpacks | `1.21.1-3.24.15.1250` | `26.1.2-3.25.55.1739` | implementation | compileOnly | 🔄 scope 降级 |

#### 已移除的依赖（不再声明）

以下依赖在旧项目中存在，在新项目中**已被完全移除**：

| 被移除的依赖 | 旧版本 | 旧 scope | 原因推测 |
|-------------|--------|----------|---------|
| `maven.modrinth:architectury-api` | 13.0.8+neoforge | compileOnly | REI 前置，REI 已移除 |
| `maven.modrinth:rei` | 16.0.788+neoforge | compileOnly | REI 兼容已移除 |
| `maven.modrinth:emi` | 1.1.22+1.21.1+neoforge | compileOnly | EMI 兼容已移除 |
| `maven.modrinth:the-one-probe` | 1.21_neo-12.0.6 | compileOnly | TOP 兼容已移除 |
| `maven.modrinth:patchouli` | 1.21-88-neoforge | implementation | Patchouli 兼容简化 |
| `maven.modrinth:iron-chests` | 1.21-neoforge-16.0.7 | implementation | Iron Chests 兼容已移除 |
| `maven.modrinth:carry-on` | 2.2.2-neoforge | implementation | Carry On 兼容已移除 |
| `maven.modrinth:inventory-tweaks-refoxed` | 1.21.1-1.2.0 | implementation | InvTweaks 兼容简化 |
| `maven.modrinth:kotlin-for-forge` | 5.9.0 | runtimeOnly | IPN 前置（IPN scope 改为 compileOnly） |
| `maven.modrinth:libipn` | neoforge-1.21.1-6.5.0 | runtimeOnly | IPN scope 改为 compileOnly |
| `maven.modrinth:create` | 6.0.10+mc1.21.1 | runtimeOnly | Create 兼容已移除 |
| `net.createmod.ponder:ponder-neoforge` | 1.0.82+mc1.21.1 | implementation | Ponder 兼容已移除 |
| `dev.engine-room.flywheel:flywheel-neoforge` | 1.0.6 | runtimeOnly | Create 前置 |
| `maven.modrinth:kaleidoscope-cookery` | 1.2.0-neoforge+mc1.21.1 | implementation | Kaleidoscope 兼容简化 |
| `maven.modrinth:farmers-delight` | 1.21.1-1.2.9-neoforge | implementation | Farmer's Delight 兼容简化 |
| `maven.modrinth:immersive-melodies` | 0.6.2+1.21.1-neoforge | implementation | Immersive Melodies 已移除 |
| `maven.modrinth:simple-hats` | 1.21.1-Neo-0.4.0 | compileOnly | Simple Hats 已移除 |
| `maven.modrinth:owo-lib` | 0.12.15.5-beta.1+1.21 | compileOnly | Simple Hats 前置 |
| `maven.modrinth:accessories` | 1.1.0-beta.48+1.21.1 | compileOnly | Simple Hats 前置 |
| `maven.modrinth:just-more-cakes` | 1.19.2 | implementation | 已移除 |
| `maven.modrinth:forge-config-api-port` | v21.1.6-1.21.1-NeoForge | implementation | JMC 前置 |
| `maven.modrinth:common-network` | 1.0.21-1.21.1 | implementation | JMC 前置 |
| `libs:patpat` | 1.2.1+1.21.1 | compileOnly | PatPat 兼容简化 |
| `software.bernie.geckolib:geckolib-neoforge-1.21.1` | 4.7.5 | runtimeOnly | GeckoLib 改为独立安装 |
| `com.github.MUKSC:TACZ-1.21.1` | neoforge-1.1.6-hotfix-r5 | implementation | TACZ 已移除 |
| `libs:superbwarfare-1.21.1` | 0.8.7.1 | implementation | 枪械兼容已移除 |
| `libs:create-aeronautics-bundled-1.21.1` | 1.1.3 | runtimeOnly | Create 已移除 |
| `libs:sable-neoforge-1.21.1` | 1.1.3 | compileOnly + runtimeOnly | 已移除 |
| `dev.latvian.mods:rhino` | 2101.2.7-build.72 | compileOnly | KubeJS 前置 |
| `dev.latvian.mods:kubejs-neoforge` | 2101.7.2-build.336 | compileOnly | KubeJS 兼容简化 |

#### 新项目中已注释掉的依赖

```groovy
// runtimeOnly "maven.modrinth:kotlin-for-forge:6.2.0"
// runtimeOnly "maven.modrinth:libipn:neoforge-26.1.2-6.7.2"
```

### 2.8 源码 JAR 和 manifest

两项目的 `java { withSourcesJar() }` 和 `jar { manifest { ... } }` 内容相同（位置略有调整）。

### 2.9 重复的 JavaCompile 配置（⚠ 可能的 Bug）

新项目 `build.gradle` 末尾存在一处**重复**：

```groovy
// 第 325-328 行（第一次）
tasks.withType(JavaCompile).configureEach {
    options.compilerArgs += ['-Xlint:-removal']
    options.encoding = 'UTF-8'
}

// 第 344-346 行（第二次 — 缺少 compilerArgs）
tasks.withType(JavaCompile).configureEach {
    options.encoding = "UTF-8"
}
```

第二次调用只设置了 `encoding`，缺少 `-Xlint:-removal` 编译器参数，但因为被第一次调用覆盖，实际不影响。建议合并或删除重复块。

---

## 3. gradle.properties 对比

```diff
- org.gradle.jvmargs=-Xmx6G
+ org.gradle.jvmargs=-Xmx8G

- parchment_minecraft_version=1.21.1
- parchment_mappings_version=2024.11.17

- minecraft_version=1.21.1
+ minecraft_version=26.1.2

- minecraft_version_range=[1.21.1,1.21.2)
+ minecraft_version_range=[26.1.2]

- neo_version=21.1.219
+ neo_version=26.1.2.68-beta

- mod_version=1.5.2-neoforge+mc1.21.1
+ mod_version=2.0.0-neoforge+mc26.1.2
```

> **注意**: `minecraft_version` 从 `1.21.1` 变为 `26.1.2`。这是 NeoForge 26.1+ 的统一版本号方案 — 模组声明 `26.1.2` 同时代表 Minecraft 1.21.1 和 NeoForge 26.1.2。`minecraft_version_range` 也从开区间变为 `[26.1.2]` 精确匹配。

---

## 4. settings.gradle — 完全相同

两项目的 `settings.gradle` 内容完全一致：

```groovy
pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        maven { url = 'https://maven.neoforged.net/releases' }
    }
}

plugins {
    id 'org.gradle.toolchains.foojay-resolver-convention' version '0.8.0'
}
```

---

## 5. gradle-wrapper.properties 对比

```diff
- distributionUrl=https\://services.gradle.org/distributions/gradle-8.14.3-all.zip
+ distributionUrl=https\://services.gradle.org/distributions/gradle-9.4.1-bin.zip
```

| 项目 | 旧项目 | 新项目 |
|------|--------|--------|
| Gradle 版本 | 8.14.3 | 9.4.1 |
| 分发类型 | `-all` (含源码和文档) | `-bin` (仅运行时) |

`-all` → `-bin` 切换可节省下载体积，但开发时无法在 IDE 中查看 Gradle API 源码。

---

## 6. neoforge.mods.toml — 完全相同

两个项目的 `src/main/resources/META-INF/neoforge.mods.toml` 内容**完全一致**。所有版本号通过 `${...}` 变量在构建时替换。

```toml
modLoader = "javafml"
loaderVersion = "${loader_version_range}"
# ... 
[[dependencies."${mod_id}"]]
    modId = "neoforge"
    versionRange = "${neo_version_range}"
[[dependencies."${mod_id}"]]
    modId = "minecraft"
    versionRange = "${minecraft_version_range}"
[[dependencies."${mod_id}"]]
    modId = "kubejs"
    versionRange = "[2101.7.2-build.309,)"
```

---

## 7. mixins.json 对比

| 配置项 | 旧项目 | 新项目 | 说明 |
|--------|--------|--------|------|
| `plugin` | `...mixin.plugin.MixinPlugin` | **删除** | MixinPlugin 已移除（不再需要条件性 InvTweaks mixin） |
| `compatibilityLevel` | `JAVA_17` | `JAVA_25` | 适配 JDK 25 |
| `refmap` | `"touhou_little_maid.refmap.json"` | **删除** | NeoGradle 2.0 moddev 插件自动处理 refmap，不再需要显式声明 |

### 公共 Mixin 列表 — 完全相同

两个项目的 common mixins 列表完全一致（15 个 + 5 个 accessor）。

### 客户端 Mixin 列表

| Mixin | 旧项目 | 新项目 | 说明 |
|-------|--------|--------|------|
| `ClientPacketListenerMixin` | ✅ | ✅ | 不变 |
| `HumanoidModelMixin` | ✅ | 注释掉 | `// "client.HumanoidModelMixin"` — 暂停使用 |
| `ItemPropertiesMixin` | ✅ | 注释掉 | `// "client.ItemPropertiesMixin"` — 暂停使用 |
| `LanguageMixin` | ✅ | ✅ | 不变 |
| `LivingEntityRendererMixin` | ✅ | ✅ | 不变 |
| `InventoryScreenMixin` | — | ✅ | 新增（GeckoLib 1.21.1 兼容） |
| `LevelRendererMixin` | — | ✅ | 新增（GeckoLib 1.21.1 兼容） |
| `compat.InvTweaksMixin` | ✅ (via plugin) | — | 随 MixinPlugin 移除 |

---

## 8. libs/ 本地 JAR 目录

| 旧项目 libs/ | 新项目 libs/ |
|--------------|--------------|
| `patpat-1.2.1+1.21.1.jar` | — |
| `superbwarfare-1.21.1-0.8.7.1.jar` | — |
| `create-aeronautics-bundled-1.21.1-1.1.3.jar` | — |
| `sable-neoforge-1.21.1-1.1.3.jar` | — |

新项目的 `libs/` 目录为空（或不存在），所有依赖均通过 Maven 仓库远程获取，不再使用本地 JAR 方式管理。

---

## 9. 迁移检查清单

从旧项目迁移到新项目的构建配置时，请确认：

- [ ] JDK 25 已安装并设为 `JAVA_HOME`
- [ ] `gradle/wrapper/gradle-wrapper.properties` 指向 Gradle 9.4.1+
- [ ] `build.gradle` 中 moddev 插件版本 ≥ 2.0.141
- [ ] `build.gradle` 中 Shadow 插件版本 ≥ 9.4.1
- [ ] Java toolchain 设为 `JavaLanguageVersion.of(25)`
- [ ] Parchment 映射已移除（如需要可重新添加）
- [ ] `data` run 使用 `clientData()` 而非 `data()`
- [ ] `gradle.properties` 中 `minecraft_version` 使用 NeoForge 26.1 格式 (`26.1.2`)
- [ ] `gradle.properties` 中 `neo_version` 使用 `26.1.2.68-beta` 格式
- [ ] `gradle.properties` 中 NHRT 0X 调整为 8G+（JDK 25 需要更多内存）
- [ ] `mixins.json` 中 `compatibilityLevel` 设为 `JAVA_25`
- [ ] `mixins.json` 中移除 `plugin` 和 `refmap` 键
- [ ] 所有编译依赖版本更新为 NeoForge 26.1 对应版本
- [ ] 本地 `libs/` 中的 JAR 改为远程 Maven 仓库获取
- [ ] 不需要的兼容层依赖（REI, EMI, TOP, Patchouli 等）已从 `build.gradle` 移除

---

## 10. 与 AGENTS.md 的关系

本文档与 `AGENTS.md` 中的构建命令信息保持一致：

```bash
./gradlew build                    # 完整构建（含 shadowJar）
./gradlew runClient                # 启动客户端
./gradlew runServer                # 启动服务端
./gradlew runData                  # 运行数据生成器
./gradlew runGameTestServer        # 运行游戏测试
```

构建产物位置：`build/libs/touhoulittlemaid-<version>-all.jar`
