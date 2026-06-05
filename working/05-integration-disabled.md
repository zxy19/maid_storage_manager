# Integration Disabled Files

> 核验日期: 2026-06-04
> 当前 integration 相关 `.java.disabled`: 98 个。
> 当前核心项目 `.java.disabled`: 3 个，详见 [01-disabled-files.md](./01-disabled-files.md)。
> 统计依据: IDE `search_file *.java.disabled`，并按实际功能归类。

---

## 1. 已恢复或当前活跃的集成

### 1.1 AE2 (10 files) - 已恢复

适配 MC 26.1 API，`build.gradle` 当前使用 AE2 `8129795`。

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

当前注册状态:

- `CraftManager` 中 AE2 craft type/action 已启用。
- `CraftManager` 中 AE2 generators 已启用。
- `MaidStorage` 中 AE2 storage 已按 `Integrations.ae2Storage()` 启用。

### 1.2 JEI + Ingredient Request - 已恢复

`build.gradle` 当前使用 JEI `29.6.2.31`。当前未发现 JEI 相关 `.java.disabled`，只有 TACZ 的 `JEITaczRecipeTransfer.java.disabled` 仍随 TACZ 禁用。

已确认活跃文件:

- `integration/jei/Plugin.java`
- `integration/jei/GhostIngredientHandler.java`
- `integration/jei/IFilterScreen.java`
- `integration/jei/RequestRecipeHandler.java`
- `menu/craft/base/handler/JEIRecipeHandler.java`
- `menu/craft/common/JEICommonRecipeHandler.java`
- `menu/craft/stone_cutter/JeiStoneCutterRecipeHandler.java`
- `integration/request/JEIClient.java`
- `integration/request/JEIRequestDisplayError.java`
- `integration/request/IngredientRequest.java`
- `integration/request/IngredientRequestClient.java`
- `mixin/JEIRecipeTransferHook.java`
- `network/IngredientRequestC2SPacket.java`
- `network/IngredientRequestResultS2CPacket.java`

### 1.3 Cloth Config (2 files) - 已恢复

`build.gradle` 当前使用 Cloth Config `26.1.154+neoforge`。

- `integration/cloth/AddClothEvent.java`
- `integration/cloth/ClothEntry.java`

### 1.4 Jade (2 files) - 已恢复

`build.gradle` 当前使用 Jade `26.1.1+neoforge`。

- `integration/jade/JadePlugin.java`
- `integration/jade/VirtualFrameDataProvider.java`

### 1.5 Sophisticated Storage (1 file) - 已恢复

`build.gradle` 当前包含 `sophisticated-storage-619320:8185754` compileOnly。

- `integration/sophisticated_storage/SophisticatedStorageMultiBlock.java`

当前 `MaidStorage` 会在 `Integrations.sophisticatedStorage()` 为真时注册该 multi-block processor。

---

## 2. 当前仍禁用的集成文件

### 2.1 Refined Storage (7 files)

本地 `build.gradle` 中 RS 依赖仍注释，相关源码仍为 `.java.disabled`。

- `craft/type/RSType.java.disabled`
- `craft/context/special/RsCraftingAction.java.disabled`
- `storage/rs/AbstractRSContext.java.disabled`
- `storage/rs/RSCollectContext.java.disabled`
- `storage/rs/RSInsertContext.java.disabled`
- `storage/rs/RsStorage.java.disabled`
- `storage/rs/RSViewContext.java.disabled`

### 2.2 Create (23 files)

本地 `build.gradle` 中 Create/Ponder/Registrate 依赖仍注释。

**Craft 生成器 (9):**

- `craft/generator/type/create/GeneratorCreate.java.disabled`
- `craft/generator/type/create/GeneratorCreateCompact.java.disabled`
- `craft/generator/type/create/GeneratorCreateCrushing.java.disabled`
- `craft/generator/type/create/GeneratorCreateDeployer.java.disabled`
- `craft/generator/type/create/GeneratorCreateFanRecipes.java.disabled`
- `craft/generator/type/create/GeneratorCreateMilling.java.disabled`
- `craft/generator/type/create/GeneratorCreateMix.java.disabled`
- `craft/generator/type/create/GeneratorCreatePress.java.disabled`
- `craft/generator/type/create/GeneratorCreateUse.java.disabled`

**Integration (5):**

- `integration/create/AddCreateStockButtonForMaid.java.disabled`
- `integration/create/CreateIntegration.java.disabled`
- `integration/create/CreateMultiBlockVault.java.disabled`
- `integration/create/CreateStockButton.java.disabled`
- `integration/create/StockManagerInteract.java.disabled`

**Storage (6):**

- `storage/create/place/CreateChainConveyorStorage.java.disabled`
- `storage/create/place/CreatePlacePackageContext.java.disabled`
- `storage/create/stock/AbstractCreateContext.java.disabled`
- `storage/create/stock/CreateCollectContext.java.disabled`
- `storage/create/stock/CreateStockTickerStorage.java.disabled`
- `storage/create/stock/CreateViewContext.java.disabled`

**Mixin (3):**

- `mixin/CreateStockKeeperMenuMixin.java.disabled`
- `mixin/CreateStockKeeperScreenMixin.java.disabled`
- `mixin/CreateStockTickerBEMixin.java.disabled`

**相关网络状态:** `CreateStockManagerPacket` 已注册，但 handler 仍是 `// TODO wait create`。

### 2.3 Mekanism + QIO (14 files)

本地 `build.gradle` 中 Mekanism 依赖仍注释。

**Craft 生成器 (8):**

- `craft/generator/type/mekanism/GeneratorMek.java.disabled`
- `craft/generator/type/mekanism/GeneratorMekCombine.java.disabled`
- `craft/generator/type/mekanism/GeneratorMekCrushing.java.disabled`
- `craft/generator/type/mekanism/GeneratorMekEnrichment.java.disabled`
- `craft/generator/type/mekanism/GeneratorMekInfusion.java.disabled`
- `craft/generator/type/mekanism/GeneratorMekOsmiumComp.java.disabled`
- `craft/generator/type/mekanism/GeneratorMekSawing.java.disabled`
- `craft/generator/type/mekanism/GeneratorMekSmelter.java.disabled`

**QIO Storage (5):**

- `storage/qio/QIOBaseContext.java.disabled`
- `storage/qio/QIOCollectContext.java.disabled`
- `storage/qio/QIOInsertContext.java.disabled`
- `storage/qio/QIOStorage.java.disabled`
- `storage/qio/QIOViewContext.java.disabled`

**Integration (1):**

- `integration/mekanism/MekanismIntegration.java.disabled`

### 2.4 Ars Nouveau (4 files)

本地 `build.gradle` 中 Ars Nouveau/Geckolib 依赖仍注释。

- `craft/generator/type/ars/GeneratorArsNouveauApparatus.java.disabled`
- `craft/generator/type/ars/GeneratorArsNouveauEnchantApp.java.disabled`
- `craft/generator/type/ars/GeneratorArsNouveauEnchanting.java.disabled`
- `craft/generator/type/ars/GeneratorArsNouveauImbuement.java.disabled`

### 2.5 TACZ (8 files)

本地 TACZ 依赖仍注释。

- `craft/type/TaczType.java.disabled`
- `craft/context/special/TaczRecipeAction.java.disabled`
- `craft/generator/type/misc/GeneratorTACZ.java.disabled`
- `integration/tacz/TaczRecipe.java.disabled`
- `menu/craft/tacz/TaczCraftMenu.java.disabled`
- `menu/craft/tacz/TaczCraftScreen.java.disabled`
- `menu/craft/tacz/JEITaczRecipeTransfer.java.disabled`
- `menu/craft/tacz/EMITaczRecipeTransfer.java.disabled`

相关状态: `ClientGuiRegistry` 中 TaczCraftScreen 注册仍注释。

### 2.6 EMI (8 files)

本地 `build.gradle` 中 EMI 依赖仍注释。

- `integration/emi/Plugin.java.disabled`
- `integration/emi/GhostIngredientHandler.java.disabled`
- `integration/emi/RequestRecipeHandler.java.disabled`
- `mixin/EMIRecipeTransferHook.java.disabled`
- `integration/request/EMIClient.java.disabled`
- `menu/craft/base/handler/EmiRecipeHandler.java.disabled`
- `menu/craft/common/EmiCommonRecipeHandler.java.disabled`
- `menu/craft/stone_cutter/EmiStoneCutterRecipeHandler.java.disabled`

### 2.7 KubeJS (34 files)

本地 `build.gradle` 中 KubeJS/Rhino/Architectury 依赖仍注释。

**Binding (5):**

- `integration/kubejs/binding/KJSMSMBinding.java.disabled`
- `integration/kubejs/binding/KJSMSMCompacted.java.disabled`
- `integration/kubejs/binding/KJSMSMMemories.java.disabled`
- `integration/kubejs/binding/KJSMSMTypeCasting.java.disabled`
- `integration/kubejs/binding/KJSMSMUtilities.java.disabled`

**Core (3):**

- `integration/kubejs/KJSEventPort.java.disabled`
- `integration/kubejs/KJSPlugin.java.disabled`
- `integration/kubejs/KJSRegEvent.java.disabled`

**Event (1):**

- `integration/kubejs/event/KJSCraftEvent.java.disabled`

**Helper (7):**

- `integration/kubejs/helper/ActionOptionOperator.java.disabled`
- `integration/kubejs/helper/CacheOperator.java.disabled`
- `integration/kubejs/helper/CraftContextOperator.java.disabled`
- `integration/kubejs/helper/CraftGuideOperator.java.disabled`
- `integration/kubejs/helper/GeneratorConfigOperator.java.disabled`
- `integration/kubejs/helper/GraphOperator.java.disabled`
- `integration/kubejs/helper/TargetOperator.java.disabled`

**Util (2):**

- `integration/kubejs/util/FunctionUtil.java.disabled`
- `integration/kubejs/util/TypeCastingUtil.java.disabled`

**Wrapped (16):**

- `integration/kubejs/wrapped/base/AbstractObjectWrapped.java.disabled`
- `integration/kubejs/wrapped/base/AbstractWrapped.java.disabled`
- `integration/kubejs/wrapped/base/BaseSupplierWrapper.java.disabled`
- `integration/kubejs/wrapped/base/BaseWrappedWrapper.java.disabled`
- `integration/kubejs/wrapped/craft/context/IKJSCraftContext.java.disabled`
- `integration/kubejs/wrapped/craft/context/KJSCraftContext.java.disabled`
- `integration/kubejs/wrapped/craft/context/KJSWrapCraftContext.java.disabled`
- `integration/kubejs/wrapped/craft/contextSupplier/IKJSCraftContextSupplier.java.disabled`
- `integration/kubejs/wrapped/craft/contextSupplier/KJSCraftContextSupplier.java.disabled`
- `integration/kubejs/wrapped/craft/generator/IKJSAutoCraftGuideGenerator.java.disabled`
- `integration/kubejs/wrapped/craft/generator/KJSAutoCraftGuideGenerator.java.disabled`
- `integration/kubejs/wrapped/craft/type/IKJSCraftType.java.disabled`
- `integration/kubejs/wrapped/craft/type/KJSCraftType.java.disabled`
- `integration/kubejs/wrapped/item/KJSItemListWrapper.java.disabled`
- `integration/kubejs/wrapped/item/KJSItemPair.java.disabled`
- `integration/kubejs/wrapped/item/KJSItemPairWrapper.java.disabled`

---

## 3. 其他集成状态

### Botania

- 未发现 `*.java.disabled`。
- 6 个 `craft/generator/type/botania/GeneratorBotania*.java` 文件存在，但文件内容整体注释，等同于未启用。
- `CraftManager` 中 Botania generator 注册仍注释。
- 本地 `build.gradle` 未启用 Botania compileOnly 依赖。

### The One Probe

- 未发现 `integration/top/` 源码目录或 TOP 相关 `.java.disabled`。
- 当前状态仍是移除后等待重新创建。

---

## 4. 注册状态核验

### CraftManager

| 模组 | 当前状态 |
|------|----------|
| AE2 | craft type/action 与 3 个 generator 已启用 |
| RS | 注册块注释 |
| TACZ | 注册块注释 |
| Create | generator 注册块注释 |
| Mekanism | generator 注册块注释 |
| Botania | generator 注册块注释，且源文件整体注释 |
| Ars Nouveau | generator 注册块注释 |

### MaidStorage

| 模组 | 当前状态 |
|------|----------|
| AE2 | storage 注册启用 |
| RS | storage 注册注释 |
| Create | storage 与 multiblock 注册注释 |
| Mekanism QIO | storage 注册注释 |
| Sophisticated Storage | multiblock processor 注册启用 |

### Network

| 集成 | 当前状态 |
|------|----------|
| JEI/Ingredient Request | C2S/S2C 包已注册 |
| Create Stock Manager | 包已注册，但 handler 等待 Create，业务逻辑注释 |

---

## 5. 恢复优先级建议

| 优先级 | 模组 | disabled 文件数 | 说明 |
|--------|------|----------------|------|
| P1 | Create | 23 | 涉及 storage、craft、mixin、网络 handler |
| P2 | Mekanism/QIO | 14 | 涉及 QIO storage 和多种 generator |
| P3 | RS | 7 | storage + craft type/action |
| P4 | KubeJS | 34 | API 面大，适合单独迁移 |
| P5 | TACZ | 8 | 含 GUI/menu/JEI/EMI transfer |
| P6 | EMI | 8 | 可参考已恢复的 JEI request/handler 结构 |
| P7 | Ars Nouveau | 4 | generator-only 集成 |
| P8 | Botania | 0 `.disabled` | 源文件整体注释且注册注释，需先恢复依赖和源码 |
| P9 | TOP | 0 `.disabled` | 源码已移除，需重新创建 |

---

## 更新记录

| 日期 | 变更 |
|------|------|
| 2026-06-04 | 重新统计 integration disabled 为 98；确认 JEI request、Jade、Sophisticated Storage 已恢复；修正 Create/Mekanism/TACZ/EMI 文件数；记录 Botania 为整体注释而非 `.disabled` |
| 2026-06-02 | 旧版 integration disabled 清单 |
