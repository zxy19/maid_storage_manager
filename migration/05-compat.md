# Compatibility Package Migration

> **迁移范围**: `src/main/java/com/github/tartaricacid/touhoulittlemaid/compat/`  
> **旧项目**: NeoForge 21.1 / MC 1.21.1 → **新项目**: NeoForge 26.1

---

## Module Inventory

本表列出两个项目中所有 compat 模块及其迁移状态。

| 模块 | 旧项目状态 | 新项目状态 | 变化说明 |
|------|-----------|-----------|---------|
| `aquaculture/` | AquacultureCompat.java + client/ + entity/ | 同（结构未变） | 无显著变化 |
| `carryon/` | RenderFixer.java | RenderFixer.java | 无显著变化 |
| `cloth/` | ClothConfigCompat + GlobalAIIntegration + MenuIntegration | 同（3 文件） | 无显著变化 |
| `curios/` | CuriosCompat + CuriosEvent + client/ + menu/ | 同（结构未变） | 无显著变化 |
| `embeddium/` | EmbeddiumCompat + **EmbeddiumGeoRenderer** | 仅 EmbeddiumCompat | **移除 GeoRenderer**，渲染方法已 stub 化 |
| `emi/` | MaidEmiPlugin + altar/ + transfer/ | **不存在** | ⚠️ 已移除 |
| `extracontainer/` | **不存在** | BackpackProvider + ExtraContainerManager + ContainerRef + MaidContainerCache + MaidInventoryRef + curios/ | ✅ **新增**模块（背包抽象框架） |
| `farmersdelight/` | FarmersDelightCompat + **FarmersDelightEdible** | 仅 FarmersDelightCompat | 移除 Edible 接口 |
| `gun/` | common/ + swarfare/ + **tacz/** | common/ + swarfare/ | 移除 tacz 子模块 |
| `immersivemelodies/` | client/ + server/ | client/ + server/ | 无显著变化 |
| `invtweaks/` | InvTweaksCompat.java | InvTweaksCompat.java | 无显著变化 |
| `ipn/` | SortButtonScreen.java | SortButtonScreen.java | 无显著变化 |
| `iris/` | **不存在** | IrisCompat.java | ✅ **新增**模块 |
| `ironchest/` | IronChestType.java | IronChestType.java | 无显著变化 |
| `jade/` | JadePlugin + provider/ | JadePlugin + provider/ | API 调用无变化 |
| `jei/` | MaidPlugin + altar/ + package-info | **MaidJeiPlugin** + altar/ | 重命名类，简化注册逻辑 |
| `jmc/` | JmcCompat + **JmcEdible** | 仅 JmcCompat | 移除 Edible 接口 |
| `kaleidoscope/` | KaleidoscopeCompat + **crop/** + **edible/** | 仅 KaleidoscopeCompat | 大幅简化 |
| `kubejs/` | ModKubeJSCompat + ModKubeJSPlugin + event/ + recipe/ + register/ | 仅 ModKubeJSCompat（stub） | **完全 stub 化** |
| `oculus/` | OculusCompat.java | OculusCompat.java | 无显著变化 |
| `patchouli/` | PatchouliCompat + **AltarRecipeComponent** + **MultiblockRegistry** + **OpenDefaultBook** | 仅 PatchouliCompat | 大幅简化 |
| `patpat/` | PatPatCompat + **PatPatRenderer** | 仅 PatPatCompat | 移除 Renderer |
| `ponder/` | PonderCompat + **MaidPonderPlugin** + **MaidPonderScenes** | 仅 PonderCompat | 大幅简化 |
| `rei/` | MaidREIClientPlugin + altar/ + transfer/ | **不存在** | ⚠️ 已移除 |
| `sbackpack/` | SBackpackCompat + SBackpackCompatInner + BackpackRightClickMaidEvent + curios/ | 同（4 文件） | 无显著变化 |
| `simplehats/` | SimpleHatsCompat + **SimpleHatsRenderer** | 仅 SimpleHatsCompat | 移除 Renderer |
| `sodium/` | SodiumCompat + **SodiumGeoRenderer** | 仅 SodiumCompat | **移除 GeoRenderer**，渲染方法已 stub 化 |
| `tbackpack/` | **不存在** | TBackpackCompat.java | ✅ **新增**模块（Traveler's Backpack） |
| `top/` | TheOneProbeInfo + provider/ | **不存在** | ⚠️ 已移除 |
| `ysm/` | YsmCompat + event/ | **不存在** | ⚠️ 已移除 |

---

## Removed Compat Modules

以下 5 个兼容模块在 NeoForge 迁移中被移除：

| 移除模块 | 旧模块 ID | 原功能 | 移除原因/备注 |
|---------|----------|--------|-------------|
| `emi/` | `emi` | EMI 配方查看器集成（祭坛配方、背包合成传输、物品隐藏） | 需等待 EMI 发布 NeoForge 26.1 兼容版本 |
| `rei/` | `roughlyenoughitems` | REI 配方查看器集成（功能同 EMI） | 同上，需等待 REI 适配 |
| `top/` | `theoneprobe` | TheOneProbe HUD 信息显示（女仆实体信息、神社灯方块覆盖） | CompatRegistry 中留了 TODO 注释 `// TODO: TheOneProbe 兼容已移除，后续迁移`；旧代码通过 `InterModComms.sendTo("theoneprobe", ...)` 注册 |
| `ysm/` | `yes_steve_model` | Yes Steve Model 兼容（版本检查 >=2.3.3，YSM 模型信息读取） | YSM 尚未发布 NeoForge 26.1 版本 |
| `(tacz)` | `tacz` | gun/tacz 子模块（Timeless and Classics Zero 枪械模组） | gun 模块中 tacz 子目录移除，仅保留 common/ 和 swarfare/ |

**注意事项**：
- EMI 和 REI 同时移除，意味着目前项目**没有任何 JEI 以外的配方查看器兼容**。
- TheOneProbe 移除时，`CompatRegistry.java` 中保留了 `TOP` 常量定义和 TODO 注释，说明后续会重新添加。
- YSM 移除意味着 `YsmMaidInfo` 工具类和 `EntityMaid` 中的 `IS_YSM_MODEL_TAG` 等 NBT 字段可能也需要处理（但该兼容属于纯消费方，不影响核心逻辑）。

---

## New/Changed Compat Modules

### 新增模块

| 模块 | 用途 | 关键类 |
|------|-----|--------|
| `extracontainer/` | 背包抽象框架，统一管理多种背包模组的容器引用 | `BackpackProvider`（接口）、`ExtraContainerManager`（注册与查询）、`ContainerRef`（容器引用）、`MaidContainerCache`（缓存）、`MaidInventoryRef`（女仆物品栏引用）、`curios/` 子包（Curios 装备事件处理） |
| `iris/` | Iris 着色器兼容（检测 Iris 是否安装、是否在渲染阴影通道） | `IrisCompat` — 使用 `IrisApi.getInstance().isRenderingShadowPass()`；内含已注释掉的 PBRLoader（待后续启用） |
| `tbackpack/` | Traveler's Backpack 兼容 | `TBackpackCompat` — 目前为 stub，`isBackpack()` 返回 false，`init()` 为空 |

### 显著变化的模块

#### 1. embeddium/ & sodium/ — 渲染优化移除

**旧版**: 包含 `EmbeddiumGeoRenderer` 和 `SodiumGeoRenderer`（各 230 行），通过直接写入 `VertexBufferWriter` 实现性能优化。使用方式：
```java
// 旧版 EmbeddiumCompat.embeddiumRenderCubesOfBone()
if (EmbeddiumCompat.isEmbeddiumInstalled()) {
    return EmbeddiumGeoRenderer.renderCubesOfBone(bone, poseStack, buffer, ...);
}
return false;
```

**新版**: 两个 GeoRenderer 类均已删除，`embeddiumRenderCubesOfBone()` / `sodiumRenderCubesOfBone()` 方法直接返回 `false`，不再提供加速渲染路径。这意味着当前版本中 embeddium/sodium 兼容仅做安装检测，不再使用其 API 进行渲染优化。

#### 2. jei/ — JEI 插件重构

| 变更点 | 旧版 (MaidPlugin) | 新版 (MaidJeiPlugin) |
|--------|-------------------|---------------------|
| 类名 | `MaidPlugin` | `MaidJeiPlugin` |
| 包注释 | `package-info.java`（`@ParametersAreNonnullByDefault`） | 已移除 |
| 资源类型 | `ResourceLocation` | `Identifier`（Yarn → Mojmap） |
| 祭坛配方来源 | `AltarRecipeMaker.getInstance().getAltarRecipes()` | `ClientRecipeEvent.ALTAR_RECIPES`（新数据流） |
| 祭坛催化剂 | 仅 `HAKUREI_GOHEI` | `HAKUREI_GOHEI` + `SANAE_GOHEI` |
| 物品子类型 | `EntityPlaceholderSubtype` 注册 | 已移除 |
| 配方传输处理器 | 合成台 + 熔炉背包传输 | 已移除 |
| GUI 点击区域 | 合成台/熔炉背包点击区域 | 已移除 |
| 物品信息 | `addIngredientInfo` (旧 ItemStack API) | `addIngredientInfo` (新 API，`InitItems.GARAGE_KIT.toStack()`) |
| 祭坛类别注册 | `addRecipeCatalyst` | `addCraftingStation`（API 变化） |

整体上，新版 JEI 插件更简洁：移除背包传输处理器、点击区域等 UI 交互逻辑，祭坛配方改用客户端事件机制获取数据。

#### 3. kubejs/ — 完全 stub 化

**旧版**: `ModKubeJSCompat` 的 `maidTipsOverlayInit()`、`maidBaubleInit()`、`maidTaskInit()` 方法会调用 `MaidRegisterJS.TIPS.register()` / `BAUBLE.register()` / `TASK.register()`，支持通过 KubeJS 注册自定义提示、饰品和任务。另含 `ModKubeJSPlugin`、`event/`、`recipe/`、`register/` 等子包。

**新版**: `ModKubeJSCompat` 精简为 stub：所有注册方法体为空，仅保留 `ENABLE` 标志位。Plugin、event、recipe、register 子包全部移除。

#### 4. 简化的模块

多个模块在新版中被大幅简化，移除辅助类/子包：

| 模块 | 移除的内容 |
|------|-----------|
| `patchouli/` | `AltarRecipeComponent`（祭坛配方组件）、`MultiblockRegistry`（多方块注册）、`OpenDefaultBook`（默认书本打开） |
| `patpat/` | `PatPatRenderer`（拍头渲染器） |
| `ponder/` | `MaidPonderPlugin`、`MaidPonderScenes` |
| `simplehats/` | `SimpleHatsRenderer` |
| `kaleidoscope/` | `crop/`、`edible/` 子包 |
| `farmersdelight/` | `FarmersDelightEdible` |
| `jmc/` | `JmcEdible` |
| `gun/` | `tacz/` 子包 |

---

## Key Integration Pattern Changes

### CompatRegistry.java 变更

```diff
- @EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
+ @EventBusSubscriber()

- import com.github.tartaricacid.touhoulittlemaid.compat.top.TheOneProbeInfo;
+ import com.github.tartaricacid.touhoulittlemaid.compat.tbackpack.TBackpackCompat;

+ public static final String TBACKPACK = "travelersbackpack";

- event.enqueueWork(() -> checkModLoad(TOP, () -> InterModComms.sendTo(TOP, ...)));
+ // TODO: TheOneProbe 兼容已移除，后续迁移

+ event.enqueueWork(() -> checkModLoad(TBACKPACK, TBackpackCompat::init));
```

**关键变化**：
1. `@EventBusSubscriber` 注解参数简化（`Bus.MOD` 是新默认值）
2. 移除 `InterModComms` 导入和使用（TOP 注册方式变更）
3. 新增 `TBACKPACK` 常量和注册
4. TOP 的 `sendTo` 调用被注释掉并标记 TODO

### 集成注册方式概览

新旧项目均继承同一套兼容集成模式：

| 集成方式 | 适用模块 | API 来源 |
|---------|---------|---------|
| `InterModEnqueueEvent` + `checkModLoad()` | curios, patchouli, sbackpack, tbackpack, immersivemelodies | NeoForge IMC |
| `@JeiPlugin` / `@WailaPlugin` 等注解 | jei, jade, emi(已移除), rei(已移除) | 各自 mod API |
| ModList 检测 + 条件逻辑 | embeddium, sodium, iris, oculus, kubejs | NeoForge ModList |
| `NeoForge.EVENT_BUS.register()` | extracontainer | NeoForge Event Bus |

### API 命名空间变化

从 Forge 到 NeoForge，多个 API 类发生命名变化：
- `net.minecraft.resources.ResourceLocation` → `net.minecraft.resources.Identifier` (Mojang mappings)
- `mezz.jei.api.registration.IRecipeTransferRegistration` / `ISubtypeRegistration` 等旧 API 被移除（JEI 版本升级）
- `snownee.jade.api.IWaila*` 接口保持兼容（Jade 跨版本 API 稳定）

### extracontainer 新架构

新增的 `extracontainer` 模块定义了一个**背包抽象层**：

```
BackpackProvider (接口)
  ├── isBackpack(ItemStack) → boolean
  └── createSlotRef(slotType, slotIndex) → ContainerRef

ExtraContainerManager (管理器)
  ├── register(BackpackProvider)    → 注册新的背包类型
  ├── isAnyBackpack(ItemStack)      → 查询是否为背包
  └── tryCreateSlotRef(...)         → 尝试创建容器引用

ContainerRef / MaidContainerCache / MaidInventoryRef  → 容器访问基础设施
curios/ 子包 → Curios 装备事件处理 (Equip, Pickup, Request)
```

该框架允许多种背包模组通过实现 `BackpackProvider` 接口来与女仆的物品栏系统集成，替代了之前各模组各自实现的分散模式。

---

## Summary of Changes

| 类别 | 数量 | 详情 |
|------|------|------|
| 完全移除的模块 | 5 | emi, rei, top, ysm, (gun/tacz) |
| 新增模块 | 3 | extracontainer, iris, tbackpack |
| 大幅简化 | 10 | embeddium, sodium, jei, kubejs, patchouli, patpat, ponder, simplehats, kaleidoscope, farmersdelight/jmc/gun |
| 无显著变化 | 12 | aquaculture, carryon, cloth, curios, immersivemelodies, invtweaks, ipn, ironchest, jade, oculus, sbackpack, (oculus) |
