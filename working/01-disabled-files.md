# Disabled Files Summary

> 总计 134 个文件被禁用，等待后续恢复或重新实现。
> 禁用方法：文件重命名为 `.java.disabled`，不会被 javac 编译。

---

## 1. 兼容模组集成（等待模组更新 26.1 版本）— 93 个文件

### 1.1 AE2 (10 files)

status: RECOVERED — 已恢复（AE2 版本 8129795，适配了 MC 26.1 API 变更）

- `craft/context/special/AeCraftingAction.java` (RECOVERED)
- `craft/generator/type/ae2/GeneratorAE2Charger.java` (RECOVERED, API adapted)
- `craft/generator/type/ae2/GeneratorAE2Inscriber.java` (RECOVERED, API adapted)
- `craft/generator/type/ae2/GeneratorAE2ItemTransform.java` (RECOVERED, API adapted)
- `craft/type/AE2Type.java` (RECOVERED)
- `storage/ae2/Ae2BaseContext.java` (RECOVERED)
- `storage/ae2/Ae2CollectContext.java` (RECOVERED)
- `storage/ae2/Ae2PlacingContext.java` (RECOVERED)
- `storage/ae2/Ae2Storage.java` (RECOVERED)
- `storage/ae2/Ae2ViewContext.java` (RECOVERED)

**API Changes (v6763533 → v8129795):**
- `ChargerRecipe`/`InscriberRecipe`/`TransformRecipe`: `getResultItem()` → `result().create()` (returns `ItemStackTemplate` now)
- `InscriberRecipe`: `getTopOptional()`/`getBottomOptional()` 返回 `Optional<Ingredient>` 代替 `Ingredient`
- `Level.getRecipeManager()` → `(RecipeManager) level.recipeAccess()`
- `RecipeManager.getAllRecipesFor()` → `.recipeMap().byType()`
- `RecipeHolder.id()` → `RecipeHolder.id().identifier()` (ResourceKey → ResourceLocation)

### 1.2 RS (7 files)

waiting: Refined Storage 暂无 MC 26.1 兼容版本

- `craft/context/special/RsCraftingAction.java`
- `craft/type/RSType.java`
- `storage/rs/AbstractRSContext.java`
- `storage/rs/RSCollectContext.java`
- `storage/rs/RSInsertContext.java`
- `storage/rs/RsStorage.java`
- `storage/rs/RSViewContext.java`

### 1.3 Create (23 files)

waiting: Create 暂无 MC 26.1 兼容版本

- `craft/generator/type/create/GeneratorCreate.java`
- `craft/generator/type/create/GeneratorCreateCompact.java`
- `craft/generator/type/create/GeneratorCreateCrushing.java`
- `craft/generator/type/create/GeneratorCreateDeployer.java`
- `craft/generator/type/create/GeneratorCreateFanRecipes.java`
- `craft/generator/type/create/GeneratorCreateMilling.java`
- `craft/generator/type/create/GeneratorCreateMix.java`
- `craft/generator/type/create/GeneratorCreatePress.java`
- `craft/generator/type/create/GeneratorCreateUse.java`
- `integration/create/AddCreateStockButtonForMaid.java`
- `integration/create/CreateIntegration.java`
- `integration/create/CreateMultiBlockVault.java`
- `integration/create/CreateStockButton.java`
- `integration/create/StockManagerInteract.java`
- `storage/create/place/CreateChainConveyorStorage.java`
- `storage/create/place/CreatePlacePackageContext.java`
- `storage/create/stock/AbstractCreateContext.java`
- `storage/create/stock/CreateCollectContext.java`
- `storage/create/stock/CreateStockTickerStorage.java`
- `storage/create/stock/CreateViewContext.java`
- `mixin/CreateStockKeeperMenuMixin.java`
- `mixin/CreateStockKeeperScreenMixin.java`
- `mixin/CreateStockTickerBEMixin.java`

### 1.4 Mekanism (14 files)

waiting: Mekanism 暂无 MC 26.1 兼容版本

- `craft/generator/type/mekanism/GeneratorMek.java`
- `craft/generator/type/mekanism/GeneratorMekCombine.java`
- `craft/generator/type/mekanism/GeneratorMekCrushing.java`
- `craft/generator/type/mekanism/GeneratorMekEnrichment.java`
- `craft/generator/type/mekanism/GeneratorMekInfusion.java`
- `craft/generator/type/mekanism/GeneratorMekOsmiumComp.java`
- `craft/generator/type/mekanism/GeneratorMekSawing.java`
- `craft/generator/type/mekanism/GeneratorMekSmelter.java`
- `integration/mekanism/MekanismIntegration.java`
- `storage/qio/QIOBaseContext.java`
- `storage/qio/QIOCollectContext.java`
- `storage/qio/QIOInsertContext.java`
- `storage/qio/QIOStorage.java`
- `storage/qio/QIOViewContext.java`

### 1.5 Ars Nouveau (4 files)

waiting: Ars Nouveau 暂无 MC 26.1 兼容版本

- `craft/generator/type/ars/GeneratorArsNouveauApparatus.java`
- `craft/generator/type/ars/GeneratorArsNouveauEnchantApp.java`
- `craft/generator/type/ars/GeneratorArsNouveauEnchanting.java`
- `craft/generator/type/ars/GeneratorArsNouveauImbuement.java`

### 1.6 TACZ (8 files)

waiting: TACZ 暂无 MC 26.1 兼容版本

- `craft/context/special/TaczRecipeAction.java`
- `craft/generator/type/misc/GeneratorTACZ.java`
- `craft/type/TaczType.java`
- `integration/tacz/TaczRecipe.java`
- `menu/craft/tacz/TaczCraftMenu.java`
- `menu/craft/tacz/TaczCraftScreen.java`
- `menu/craft/tacz/EMITaczRecipeTransfer.java`
- `menu/craft/tacz/JEITaczRecipeTransfer.java`

### 1.7 JEI (4 files)

waiting: JEI 暂无 MC 26.1 兼容版本 (核心 integration/jei/ 4 个文件已恢复，剩余文件等待)

- `integration/request/JEIClient.java`
- `integration/request/JEIRequestDisplayError.java`
- `mixin/JeiGuiIconToggleButtonAccessor.java` (见 2.3)
- `mixin/JEIRecipeTransferHook.java` (见 2.3)

### 1.8 EMI (8 files)

waiting: EMI 暂无 MC 26.1 兼容版本

- `integration/emi/GhostIngredientHandler.java`
- `integration/emi/Plugin.java`
- `integration/emi/RequestRecipeHandler.java`
- `integration/request/EMIClient.java`
- `mixin/EMIRecipeTransferHook.java`
- `menu/craft/base/handler/EmiRecipeHandler.java`
- `menu/craft/common/EmiCommonRecipeHandler.java`
- `menu/craft/stone_cutter/EmiStoneCutterRecipeHandler.java`

### 1.9 KubeJS (34 files)

waiting: KubeJS 暂无 MC 26.1 兼容版本

- `integration/kubejs/binding/KJSMSMBinding.java`
- `integration/kubejs/binding/KJSMSMCompacted.java`
- `integration/kubejs/binding/KJSMSMMemories.java`
- `integration/kubejs/binding/KJSMSMTypeCasting.java`
- `integration/kubejs/binding/KJSMSMUtilities.java`
- `integration/kubejs/event/KJSCraftEvent.java`
- `integration/kubejs/helper/ActionOptionOperator.java`
- `integration/kubejs/helper/CacheOperator.java`
- `integration/kubejs/helper/CraftContextOperator.java`
- `integration/kubejs/helper/CraftGuideOperator.java`
- `integration/kubejs/helper/GeneratorConfigOperator.java`
- `integration/kubejs/helper/GraphOperator.java`
- `integration/kubejs/helper/TargetOperator.java`
- `integration/kubejs/KJSEventPort.java`
- `integration/kubejs/KJSPlugin.java`
- `integration/kubejs/KJSRegEvent.java`
- `integration/kubejs/util/FunctionUtil.java`
- `integration/kubejs/util/TypeCastingUtil.java`
- `integration/kubejs/wrapped/base/AbstractObjectWrapped.java`
- `integration/kubejs/wrapped/base/AbstractWrapped.java`
- `integration/kubejs/wrapped/base/BaseSupplierWrapper.java`
- `integration/kubejs/wrapped/base/BaseWrappedWrapper.java`
- `integration/kubejs/wrapped/craft/context/IKJSCraftContext.java`
- `integration/kubejs/wrapped/craft/context/KJSCraftContext.java`
- `integration/kubejs/wrapped/craft/context/KJSWrapCraftContext.java`
- `integration/kubejs/wrapped/craft/contextSupplier/IKJSCraftContextSupplier.java`
- `integration/kubejs/wrapped/craft/contextSupplier/KJSCraftContextSupplier.java`
- `integration/kubejs/wrapped/craft/generator/IKJSAutoCraftGuideGenerator.java`
- `integration/kubejs/wrapped/craft/generator/KJSAutoCraftGuideGenerator.java`
- `integration/kubejs/wrapped/craft/type/IKJSCraftType.java`
- `integration/kubejs/wrapped/craft/type/KJSCraftType.java`
- `integration/kubejs/wrapped/item/KJSItemListWrapper.java`
- `integration/kubejs/wrapped/item/KJSItemPair.java`
- `integration/kubejs/wrapped/item/KJSItemPairWrapper.java`

### 1.10 Jade (2 files)

waiting: Jade API 变化，需要适配新的 MC 26.1 Jade API

- `integration/jade/JadePlugin.java`
- `integration/jade/VirtualFrameDataProvider.java`

### 1.11 Cloth Config (2 files)

waiting: Cloth Config 包名/API 变化，cloth 集成需要重写

- `integration/cloth/AddClothEvent.java`
- `integration/cloth/ClothEntry.java`

### 1.12 Sophisticated Storage (1 file)

- `integration/sophisticated_storage/SophisticatedStorageMultiBlock.java`

---

## 2. 混入（Mixin）— 6 个文件

### 2.1 等待 TLM API 适配（2 files）

waiting: ShapelessRecipe 构造器签名变化，Brain.serializeStart 签名变化

- `mixin/AltarRecipeMultiOutputMixin.java`
  - desc: 扩展 TLM AltarRecipe 的多输出支持。MC 26.1 中 ShapelessRecipe 构造器从 (String, Category, ItemStack, List) 变为 (CommonInfo, BookInfo, ItemStackTemplate, List)
- `mixin/LivingEntityBrainSerializeWrapper.java`
  - desc: 女仆大脑序列化包装。MC 26.1 中 Brain.serializeStart 需要 RegistryOps 参数

### 2.2 等待 Create API（3 files，随 Create 恢复）

waiting: Create 恢复后一同恢复

- `mixin/CreateStockKeeperMenuMixin.java`
- `mixin/CreateStockKeeperScreenMixin.java`
- `mixin/CreateStockTickerBEMixin.java`

### 2.3 等待 JEI/EMI API（3 files，随 JEI/EMI 恢复）

waiting: JEI/EMI 恢复后一同恢复

- `mixin/JeiGuiIconToggleButtonAccessor.java`
- `mixin/JEIRecipeTransferHook.java`
- `mixin/EMIRecipeTransferHook.java`

---

## 3. GUI/渲染/事件管线 — 8 个文件

### 3.1 等待渲染管线适配（2 files）

reason: MC 26.1 渲染从 MultiBufferSource 迁移到 SubmitNodeCollector，需要完整重写

- `render/ItemStackLighting.java`
- `util/BoxRenderUtil.java`

### 3.2 等待事件 API 适配（5 files）

- `event/BindingRender.java`
- `event/BindingRenderSyncSender.java`
- `event/InputEvent.java`
- `event/PlayerInteractClient.java`
- `event/TickClient.java`

### 3.3 Entity Render（1 file）

- `entity/VirtualItemEntityRender.java`

---

## 4. Craft 系统 — 5 个文件

reason: 兼容模组的 craft type/action/generator，等待各自模组更新

### 4.1 兼容模组 Craft Types（3 files）

waiting: 各自依赖模组未更新

- `craft/type/TaczType.java`
- `craft/type/AE2Type.java`
- `craft/type/RSType.java`

### 4.2 Craft 特殊 Action（3 files）

- `craft/context/special/AeCraftingAction.java`
- `craft/context/special/RsCraftingAction.java`
- `craft/context/special/TaczRecipeAction.java`

### 4.3 Craft Generator - Misc（1 file）

- `craft/generator/type/misc/GeneratorTACZ.java`

---

## 5. 网络数据包 — 1 个文件

waiting: AI 对话系统重新启用后恢复

- `network/AIMatchLocalizedItemS2CPacket.java`

---

## 6. 通信 — 1 个文件

reason: 级联禁用

- `communicate/wish/PlaceItemWishWithLimitation.java`

---

## 7. Datagen — 2 个文件

- `datagen/RecipeDataGen.java` - TLM AltarRecipeBuilder 不兼容 data component 配方
- `datagen/AdvancementDataGen.java` - TLM MaidEvent trigger 类在 datagen 环境不可用

---

## 8. API / 其他 — 1 个文件

- `api/mixin/IJEIButtonGetter.java` (等待 JEI 更新)

---

## 9. Ingredient Request（请求系统）— 2 个文件

waiting: JEI/EMI 恢复后一同恢复

- `integration/request/IngredientRequest.java`
- `integration/request/IngredientRequestClient.java`

---

## 已恢复文件列表（自上次文档更新以来）

以下文件已从 `.java.disabled` 恢复为 `.java`（共 27 个）：

### Craft Types（8 files）
- `craft/type/CommonType.java`
- `craft/type/CraftingType.java`
- `craft/type/AltarType.java`
- `craft/type/FurnaceType.java`
- `craft/type/BrewingType.java`
- `craft/type/SmithingType.java`
- `craft/type/AnvilType.java`
- `craft/type/StoneCuttingType.java`

### JEI 集成（7 files）
- `integration/jei/Plugin.java`
- `integration/jei/GhostIngredientHandler.java`
- `integration/jei/IFilterScreen.java`
- `integration/jei/RequestRecipeHandler.java`
- `menu/craft/base/handler/JEIRecipeHandler.java`
- `menu/craft/common/JEICommonRecipeHandler.java`
- `menu/craft/stone_cutter/JeiStoneCutterRecipeHandler.java`

### Maid 行为/任务（4 files）
- `maid/task/StorageManageTask.java`
- `maid/behavior/logistics/output/LogisticsOutputBehavior.java`
- `maid/behavior/logistics/recycle/LogisticsRecycleBehavior.java`
- `maid/behavior/view/WriteInventoryListBehavior.java`

### ItemHandler 存储（5 files）
- `storage/ItemHandler/AbstractItemHandlerContext.java`
- `storage/ItemHandler/ContextItemHandlerCollect.java`
- `storage/ItemHandler/ContextItemHandlerStore.java`
- `storage/ItemHandler/ContextItemHandlerView.java`
- `storage/ItemHandler/SimulateTargetInteractHelper.java`

### 其他（3 files）
- `craft/generator/type/misc/GeneratorAltar.java`
- `maid/config/StorageManagerMaidConfigGui.java`
- `registry/ClientGuiRegistry.java`（从删除恢复）

---

## 恢复优先级（更新）

1. **P1 — 事件/渲染**：Client events, BoxRender, VirtualItemEntityRender, ItemStackLighting
2. **P2 — Mixin 适配**：AltarRecipeMultiOutputMixin, LivingEntityBrainSerializeWrapper
3. **P2 — 网络包**：ItemSelectorSetItemPacket, CraftGuideGuiPacket 等
4. **P3 — 兼容模组**：AE2, Create, Mekanism, JEI/EMI, KubeJS 等（等待模组更新）
