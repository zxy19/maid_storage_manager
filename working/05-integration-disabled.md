# Integration Disabled Files

> 总计 108 个 integration 相关文件被禁用，等待兼容模组发布 MC 26.1 版本。
> 禁用方法：文件重命名为 `.java.disabled`，不会被 javac 编译。
> 已恢复: 19 个文件 (AE2 完整 10 + JEI 核心 7 + Cloth Config 2)

---

## 1. 已恢复

### 1.1 AE2 (10 files) — ✅ RECOVERED

适配 MC 26.1 API (AE2 version 8129795)。

- `storage/ae2/Ae2BaseContext.java`
- `storage/ae2/Ae2CollectContext.java`
- `storage/ae2/Ae2PlacingContext.java`
- `storage/ae2/Ae2Storage.java`
- `storage/ae2/Ae2ViewContext.java`
- `craft/context/special/AeCraftingAction.java`
- `craft/generator/type/ae2/GeneratorAE2Charger.java`
- `craft/generator/type/ae2/GeneratorAE2Inscriber.java`
- `craft/generator/type/ae2/GeneratorAE2ItemTransform.java`
- `craft/type/AE2Type.java`

**API 变更 (v6763533 → v8129795):**
- `ChargerRecipe`/`InscriberRecipe`/`TransformRecipe`: `getResultItem()` → `result().create()`
- `InscriberRecipe`: `getTopOptional()`/`getBottomOptional()` 返回 `Optional<Ingredient>`
- `Level.getRecipeManager()` → `(RecipeManager) level.recipeAccess()`
- `RecipeManager.getAllRecipesFor()` → `.recipeMap().byType()`
- `RecipeHolder.id()` → `RecipeHolder.id().identifier()`

### 1.2 JEI 核心集成 (7 files) — ✅ RECOVERED

JEI API 核心适配完成，mixin 和请求系统仍等待 JEI MC 26.1 版本。

- `integration/jei/Plugin.java`
- `integration/jei/GhostIngredientHandler.java`
- `integration/jei/IFilterScreen.java`
- `integration/jei/RequestRecipeHandler.java`
- `menu/craft/base/handler/JEIRecipeHandler.java`
- `menu/craft/common/JEICommonRecipeHandler.java`
- `menu/craft/stone_cutter/JeiStoneCutterRecipeHandler.java`

### 1.3 Cloth Config (2 files) — ✅ RECOVERED

适配 MC 26.1 Cloth Config API，IConfigScreenFactory 注册正常。

- `integration/cloth/AddClothEvent.java`
- `integration/cloth/ClothEntry.java`

---

## 2. 等待模组更新

### 2.1 Refined Storage (7 files)

waiting: RS 暂无 MC 26.1 兼容版本

- `craft/type/RSType.java`
- `craft/context/special/RsCraftingAction.java`
- `storage/rs/AbstractRSContext.java`
- `storage/rs/RSCollectContext.java`
- `storage/rs/RSInsertContext.java`
- `storage/rs/RsStorage.java`
- `storage/rs/RSViewContext.java`

### 2.2 Create (28 files)

waiting: Create 暂无 MC 26.1 兼容版本

**Craft 生成器 (9):**
- `craft/generator/type/create/GeneratorCreate.java`
- `craft/generator/type/create/GeneratorCreateCompact.java`
- `craft/generator/type/create/GeneratorCreateCrushing.java`
- `craft/generator/type/create/GeneratorCreateDeployer.java`
- `craft/generator/type/create/GeneratorCreateFanRecipes.java`
- `craft/generator/type/create/GeneratorCreateMilling.java`
- `craft/generator/type/create/GeneratorCreateMix.java`
- `craft/generator/type/create/GeneratorCreatePress.java`
- `craft/generator/type/create/GeneratorCreateUse.java`

**Integration (5):**
- `integration/create/AddCreateStockButtonForMaid.java`
- `integration/create/CreateIntegration.java`
- `integration/create/CreateMultiBlockVault.java`
- `integration/create/CreateStockButton.java`
- `integration/create/StockManagerInteract.java`

**Storage (6):**
- `storage/create/place/CreateChainConveyorStorage.java`
- `storage/create/place/CreatePlacePackageContext.java`
- `storage/create/stock/AbstractCreateContext.java`
- `storage/create/stock/CreateCollectContext.java`
- `storage/create/stock/CreateStockTickerStorage.java`
- `storage/create/stock/CreateViewContext.java`

**Mixin (3):**
- `mixin/CreateStockKeeperMenuMixin.java`
- `mixin/CreateStockKeeperScreenMixin.java`
- `mixin/CreateStockTickerBEMixin.java`

**相关网络包:** `CreateStockManagerPacket` handler (`Network.java`)

### 2.3 Mekanism + QIO (15 files)

waiting: Mekanism 暂无 MC 26.1 兼容版本

**Craft 生成器 (8):**
- `craft/generator/type/mekanism/GeneratorMek.java`
- `craft/generator/type/mekanism/GeneratorMekCombine.java`
- `craft/generator/type/mekanism/GeneratorMekCrushing.java`
- `craft/generator/type/mekanism/GeneratorMekEnrichment.java`
- `craft/generator/type/mekanism/GeneratorMekInfusion.java`
- `craft/generator/type/mekanism/GeneratorMekOsmiumComp.java`
- `craft/generator/type/mekanism/GeneratorMekSawing.java`
- `craft/generator/type/mekanism/GeneratorMekSmelter.java`

**QIO Storage (5):**
- `storage/qio/QIOBaseContext.java`
- `storage/qio/QIOCollectContext.java`
- `storage/qio/QIOInsertContext.java`
- `storage/qio/QIOStorage.java`
- `storage/qio/QIOViewContext.java`

**Integration (1):**
- `integration/mekanism/MekanismIntegration.java`

### 2.4 Ars Nouveau (4 files)

waiting: Ars Nouveau 暂无 MC 26.1 兼容版本

- `craft/generator/type/ars/GeneratorArsNouveauApparatus.java`
- `craft/generator/type/ars/GeneratorArsNouveauEnchantApp.java`
- `craft/generator/type/ars/GeneratorArsNouveauEnchanting.java`
- `craft/generator/type/ars/GeneratorArsNouveauImbuement.java`

### 2.5 TACZ (12 files)

waiting: TACZ 暂无 MC 26.1 兼容版本

- `craft/type/TaczType.java`
- `craft/context/special/TaczRecipeAction.java`
- `craft/generator/type/misc/GeneratorTACZ.java`
- `integration/tacz/TaczRecipe.java`
- `menu/craft/tacz/TaczCraftMenu.java`
- `menu/craft/tacz/TaczCraftScreen.java`
- `menu/craft/tacz/JEITaczRecipeTransfer.java`
- `menu/craft/tacz/EMITaczRecipeTransfer.java`
- `ClientGuiRegistry` 中 TaczCraftScreen 注册仍注释

### 2.6 JEI — 仍等待 (8 files)

waiting: JEI MC 26.1 版本发布

**Mixin (2):**
- `mixin/JeiGuiIconToggleButtonAccessor.java`
- `mixin/JEIRecipeTransferHook.java`

**Request 系统 (4):**
- `integration/request/JEIClient.java`
- `integration/request/JEIRequestDisplayError.java`

**Ingredient Request (2):**
- `integration/request/IngredientRequest.java`
- `integration/request/IngredientRequestClient.java`

**API (1):**
- `api/mixin/IJEIButtonGetter.java`

**相关网络包:** `JEIRequestPacket` handler, `JEIRequestResultPacket` handler (均被注释)

### 2.7 EMI (8 files)

waiting: EMI 暂无 MC 26.1 兼容版本

**Integration (3):**
- `integration/emi/Plugin.java`
- `integration/emi/GhostIngredientHandler.java`
- `integration/emi/RequestRecipeHandler.java`

**Mixin (1):**
- `mixin/EMIRecipeTransferHook.java`

**Request (1):**
- `integration/request/EMIClient.java`

**配方处理器 (3):**
- `menu/craft/base/handler/EmiRecipeHandler.java`
- `menu/craft/common/EmiCommonRecipeHandler.java`
- `menu/craft/stone_cutter/EmiStoneCutterRecipeHandler.java`

### 2.8 KubeJS (34 files)

waiting: KubeJS 暂无 MC 26.1 兼容版本 — API 版本跨越较大，可能需要大幅重写

**Binding (5):**
- `integration/kubejs/binding/KJSMSMBinding.java`
- `integration/kubejs/binding/KJSMSMCompacted.java`
- `integration/kubejs/binding/KJSMSMMemories.java`
- `integration/kubejs/binding/KJSMSMTypeCasting.java`
- `integration/kubejs/binding/KJSMSMUtilities.java`

**Core (3):**
- `integration/kubejs/KJSEventPort.java`
- `integration/kubejs/KJSPlugin.java`
- `integration/kubejs/KJSRegEvent.java`

**Event (1):** `integration/kubejs/event/KJSCraftEvent.java`

**Helper (7):**
- `integration/kubejs/helper/ActionOptionOperator.java`
- `integration/kubejs/helper/CacheOperator.java`
- `integration/kubejs/helper/CraftContextOperator.java`
- `integration/kubejs/helper/CraftGuideOperator.java`
- `integration/kubejs/helper/GeneratorConfigOperator.java`
- `integration/kubejs/helper/GraphOperator.java`
- `integration/kubejs/helper/TargetOperator.java`

**Util (2):**
- `integration/kubejs/util/FunctionUtil.java`
- `integration/kubejs/util/TypeCastingUtil.java`

**Wrapped - Base (4):** `AbstractObjectWrapped`, `AbstractWrapped`, `BaseSupplierWrapper`, `BaseWrappedWrapper`
**Wrapped - Craft Context (3):** `IKJSCraftContext`, `KJSCraftContext`, `KJSWrapCraftContext`
**Wrapped - Craft ContextSupplier (2):** `IKJSCraftContextSupplier`, `KJSCraftContextSupplier`
**Wrapped - Craft Generator (2):** `IKJSAutoCraftGuideGenerator`, `KJSAutoCraftGuideGenerator`
**Wrapped - Craft Type (2):** `IKJSCraftType`, `KJSCraftType`
**Wrapped - Item (3):** `KJSItemListWrapper`, `KJSItemPair`, `KJSItemPairWrapper`

### 2.9 Jade (2 files)

waiting: Jade API 变化，需要适配新的 MC 26.1 Jade API

- `integration/jade/JadePlugin.java`
- `integration/jade/VirtualFrameDataProvider.java`

### 2.10 Sophisticated Storage (1 file)

waiting: Sophisticated Storage 暂无 MC 26.1 兼容版本

- `integration/sophisticated_storage/SophisticatedStorageMultiBlock.java`

### 2.11 Botania

waiting: Botania 暂无 MC 26.1 兼容版本

> 注: 6 个 Botania generator 文件 (`craft/generator/type/botania/GeneratorBotania*.java`) 为 active `.java`，但 `CraftManager.java` 中 Botania 注册仍被注释。需要等待 Botania 发布后解除注释。

### 2.12 The One Probe

**已移除**: `integration/top/` 目录完全移除，无遗留代码。需等待 TOP 发布 MC 26.1 版本后重新创建。

---

## 3. CraftManager 中仍被注释的兼容模组注册

以下注册块在 `CraftManager.java` 中仍被注释，等待对应模组恢复：

| 模组 | 位置 | 内容 |
|------|------|------|
| AE2 | :235-247 | 已解除注释 ✅ |
| RS | :248-260 | addCraftType + addAction + generators |
| TACZ | :261-263 | addCraftType + addAction |
| Create | :264-273 | generators |
| Mekanism | :274-282 | generators |
| Botania | :288-295 | generators |
| Ars Nouveau | :296-300 | generators |

---

## 4. 恢复按模组优先级

| 优先级 | 模组 | 文件数 | 说明 |
|--------|------|--------|------|
| P1 | JEI (剩余) | 8 | 核心 API 已适配，mixin 和请求系统等版本 |
| P2 | Create | 28 | 影响最大（含 storage, craft, mixin） |
| P3 | Mekanism | 15 | 含 QIO 存储 |
| P4 | RS | 7 | RS replatform 需要时间 |
| P5 | KubeJS | 34 | API 跨度大，可能需要重写 |
| P6 | TACZ | 12 | 含 GUI 注册 |
| P7 | EMI | 8 | 等 EMI 版本 |
| P8 | Ars Nouveau | 4 | - |
| P9 | Jade | 2 | API 适配 |
| P10 | Sophisticated Storage | 1 | - |

---

## 5. 恢复记录

| 日期 | 模组 | 恢复数 | 文件 |
|------|------|--------|------|
| Phase 1 | AE2 | 10 | storage/ae2 (5) + craft/generator/ae2 (3) + craft/type/AE2Type + craft/action/AeCraftingAction |
| Phase 1 | JEI 核心 | 7 | integration/jei (4) + menu JEI handlers (3) |
| Phase 2 | Cloth Config | 2 | integration/cloth (2) |

**累计恢复: 19 个 integration 文件**

---

*文档创建于 2026-06-02*
