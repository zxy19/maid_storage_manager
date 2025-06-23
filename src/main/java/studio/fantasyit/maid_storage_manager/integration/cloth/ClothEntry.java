package studio.fantasyit.maid_storage_manager.integration.cloth;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.AbstractFieldBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.craft.CraftManager;
import studio.fantasyit.maid_storage_manager.craft.generator.config.ConfigTypes;
import studio.fantasyit.maid_storage_manager.craft.generator.config.GeneratingConfig;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

public class ClothEntry {
    public static void registryConfigPage() {
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () ->
                new ConfigScreenHandler.ConfigScreenFactory((client, parent) -> {
                    ConfigBuilder root = ConfigBuilder.create().setTitle(Component.translatable("config.maid_storage_manager.title"));
                    root.setGlobalized(true);
                    root.setGlobalizedExpanded(false);
                    createEntry(root, root.entryBuilder());
                    return root.setParentScreen(parent).build();
                }));
    }

    public static void createEntry(ConfigBuilder root, ConfigEntryBuilder entryBuilder) {
        ConfigCategory category = root.getOrCreateCategory(Component.translatable("config.maid_storage_manager.title"));
        category.addEntry(
                entryBuilder.startBooleanToggle(Component.translatable("config.maid_storage_manager.debug"), Config.enableDebug)
                        .setSaveConsumer(t -> Config.saveAfter(() -> Config.enableDebug = t))
                        .build()
        );
        subCategory(category, entryBuilder, Component.translatable("config.maid_storage_manager.integration"), ClothEntry::addEntryIntegration);
        subCategory(category, entryBuilder, Component.translatable("config.maid_storage_manager.behavior"), ClothEntry::addEntryBehavior);
        subCategory(category, entryBuilder, Component.translatable("config.maid_storage_manager.speed"), ClothEntry::addEntrySpeed);
        subCategory(category, entryBuilder, Component.translatable("config.maid_storage_manager.performance"), ClothEntry::addEntryPerformance);
        subCategory(category, entryBuilder, Component.translatable("config.maid_storage_manager.ai"), ClothEntry::addEntryAI);
        subCategory(category, entryBuilder, Component.translatable("config.maid_storage_manager.utility"), ClothEntry::addEntryUtility);
        subCategory(category, entryBuilder, Component.translatable("config.maid_storage_manager.render"), ClothEntry::addEntryRender);
        subCategory(category, entryBuilder, Component.translatable("config.maid_storage_manager.crafting"), ClothEntry::addEntryCrafting);
    }

    private static void subCategory(ConfigCategory category, ConfigEntryBuilder entryBuilder, Component title, BiConsumer<SubCategoryBuilder, ConfigEntryBuilder> builder) {
        SubCategoryBuilder sb = entryBuilder.startSubCategory(title);
        sb.setExpanded(true);
        builder.accept(sb, entryBuilder);
        category.addEntry(sb.build());
    }

    private static void addEntryIntegration(SubCategoryBuilder builder, ConfigEntryBuilder entryBuilder) {
        builder.add(
                entryBuilder.startBooleanToggle(Component.translatable("config.maid_storage_manager.integration.ae2"), Config.enableAe2Sup)
                        .setSaveConsumer(t -> Config.saveAfter(() -> Config.enableAe2Sup = t))
                        .build()
        );
        builder.add(
                entryBuilder.startBooleanToggle(Component.translatable("config.maid_storage_manager.integration.rs"), Config.enableAe2Sup)
                        .setSaveConsumer(t -> Config.saveAfter(() -> Config.enableRsSup = t))
                        .build()
        );
        builder.add(
                entryBuilder.startBooleanToggle(Component.translatable("config.maid_storage_manager.integration.mek"), Config.enableMekSup)
                        .setSaveConsumer(t -> Config.saveAfter(() -> Config.enableMekSup = t))
                        .build()
        );
        builder.add(
                entryBuilder.startBooleanToggle(Component.translatable("config.maid_storage_manager.integration.create_storage"), Config.enableCreateStorage)
                        .setSaveConsumer(t -> Config.saveAfter(() -> Config.enableCreateStorage = t))
                        .build()
        );
        builder.add(
                entryBuilder.startBooleanToggle(Component.translatable("config.maid_storage_manager.integration.create_stock_keeper"), Config.enableCreateStockManager)
                        .setTooltip(Component.translatable("config.maid_storage_manager.integration.create_stock_keeper.tooltip"))
                        .setSaveConsumer(t -> Config.saveAfter(() -> Config.enableCreateStockManager = t))
                        .build()
        );
        builder.add(
                entryBuilder.startIntField(Component.translatable("config.maid_storage_manager.integration.create_stock_keeper_v"), Config.createStockKeeperRangeV)
                        .setSaveConsumer(t -> Config.saveAfter(() -> Config.createStockKeeperRangeV = t))
                        .build()
        );
        builder.add(
                entryBuilder.startIntField(Component.translatable("config.maid_storage_manager.integration.create_stock_keeper_h"), Config.createStockKeeperRangeH)
                        .setSaveConsumer(t -> Config.saveAfter(() -> Config.createStockKeeperRangeH = t))
                        .build()
        );
        builder.add(
                entryBuilder.startStrField(Component.translatable("config.maid_storage_manager.integration.create_package_address"), Config.createAddress)
                        .setTooltip(Component.translatable("config.maid_storage_manager.integration.create_package_address.tooltip"))
                        .setSaveConsumer(t -> Config.saveAfter(() -> Config.createAddress = t))
                        .build()
        );
        builder.add(
                entryBuilder.startBooleanToggle(Component.translatable("config.maid_storage_manager.integration.tacz"), Config.enableTacz)
                        .setSaveConsumer(t -> Config.saveAfter(() -> Config.enableTacz = t))
                        .build()
        );
        builder.add(
                entryBuilder.startBooleanToggle(Component.translatable("config.maid_storage_manager.integration.jei_ingredient_request"), Config.enableJeiIngredientRequest)
                        .setSaveConsumer(t -> Config.saveAfter(() -> Config.enableJeiIngredientRequest = t))
                        .build()
        );
        builder.add(
                entryBuilder.startBooleanToggle(Component.translatable("config.maid_storage_manager.integration.emi_ingredient_request"), Config.enableEmiIngredientRequest)
                        .setSaveConsumer(t -> Config.saveAfter(() -> Config.enableEmiIngredientRequest = t))
                        .build()
        );
    }

    private static void addEntryBehavior(SubCategoryBuilder builder, ConfigEntryBuilder entryBuilder) {
        builder.add(
                entryBuilder.startIntField(Component.translatable("config.maid_storage_manager.behavior.max_store_tries"), Config.maxStoreTries)
                        .setTooltip(Component.translatable("config.maid_storage_manager.behavior.max_store_tries.tooltip"))
                        .setSaveConsumer(t -> Config.saveAfter(() -> Config.maxStoreTries = t))
                        .setMin(0)
                        .build()
        );
        builder.add(
                entryBuilder.startIntField(Component.translatable("config.maid_storage_manager.behavior.max_craft_tries"), Config.maxCraftTries)
                        .setTooltip(Component.translatable("config.maid_storage_manager.behavior.max_craft_tries.tooltip"))
                        .setSaveConsumer(t -> Config.saveAfter(() -> Config.maxCraftTries = t))
                        .setMin(0)
                        .build()
        );
        builder.add(
                entryBuilder.startIntField(Component.translatable("config.maid_storage_manager.behavior.max_logistics_tries"), Config.maxLogisticsTries)
                        .setTooltip(Component.translatable("config.maid_storage_manager.behavior.max_logistics_tries.tooltip"))
                        .setSaveConsumer(t -> Config.saveAfter(() -> Config.maxLogisticsTries = t))
                        .setMin(0)
                        .build()
        );
        builder.add(
                entryBuilder.startBooleanToggle(Component.translatable("config.maid_storage_manager.behavior.use_all_storage"), Config.useAllStorageByDefault)
                        .setSaveConsumer(t -> Config.saveAfter(() -> Config.useAllStorageByDefault = t))
                        .build()
        );
        builder.add(
                entryBuilder.startDoubleField(Component.translatable("config.maid_storage_manager.behavior.pickup_require_when_place"), Config.pickupRequireWhenPlace)
                        .setSaveConsumer(t -> Config.saveAfter(() -> Config.pickupRequireWhenPlace = t))
                        .setMin(0.0)
                        .setMax(1.0)
                        .build()
        );
        builder.add(
                entryBuilder.startBooleanToggle(Component.translatable("config.maid_storage_manager.behavior.real_work_simulation"), Config.realWorkSim)
                        .setTooltip(Component.translatable("config.maid_storage_manager.behavior.real_work_simulation.tooltip"))
                        .setSaveConsumer(t -> Config.saveAfter(() -> Config.realWorkSim = t))
                        .build()
        );
        builder.add(
                entryBuilder.startBooleanToggle(Component.translatable("config.maid_storage_manager.behavior.pickup_ignore_delay"), Config.pickupIgnoreDelay)
                        .setSaveConsumer(t -> Config.saveAfter(() -> Config.pickupIgnoreDelay = t))
                        .build()
        );
        builder.add(
                entryBuilder.startEnumSelector(Component.translatable("config.maid_storage_manager.behavior.throw_item_vector"), Config.ThrowMethod.class, Config.throwItemVector)
                        .setEnumNameProvider(t -> Component.translatable("config.maid_storage_manager.behavior.throw_item_vector." + t.name().toLowerCase()))
                        .setSaveConsumer(t -> Config.saveAfter(() -> Config.throwItemVector = t))
                        .build()
        );
    }

    private static void addEntrySpeed(SubCategoryBuilder builder, ConfigEntryBuilder entryBuilder) {
        builder.add(
                entryBuilder.startDoubleField(Component.translatable("config.maid_storage_manager.speed.craft_work"), Config.craftWorkSpeed)
                        .setSaveConsumer(t -> Config.saveAfter(() -> Config.craftWorkSpeed = t))
                        .build()
        );
        builder.add(
                entryBuilder.startDoubleField(Component.translatable("config.maid_storage_manager.speed.collect"), Config.collectSpeed)
                        .setSaveConsumer(t -> Config.saveAfter(() -> Config.collectSpeed = t))
                        .build()
        );
        builder.add(
                entryBuilder.startDoubleField(Component.translatable("config.maid_storage_manager.speed.view"), Config.viewSpeed)
                        .setSaveConsumer(t -> Config.saveAfter(() -> Config.viewSpeed = t))
                        .build()
        );
        builder.add(
                entryBuilder.startDoubleField(Component.translatable("config.maid_storage_manager.speed.view_changed"), Config.viewChangeSpeed)
                        .setSaveConsumer(t -> Config.saveAfter(() -> Config.viewChangeSpeed = t))
                        .build()
        );
        builder.add(
                entryBuilder.startDoubleField(Component.translatable("config.maid_storage_manager.speed.placing"), Config.placeSpeed)
                        .setSaveConsumer(t -> Config.saveAfter(() -> Config.placeSpeed = t))
                        .build()
        );
        builder.add(
                entryBuilder.startDoubleField(Component.translatable("config.maid_storage_manager.speed.follow"), Config.followSpeed)
                        .setSaveConsumer(t -> Config.saveAfter(() -> Config.followSpeed = t))
                        .build()
        );
    }

    private static void addEntryPerformance(SubCategoryBuilder builder, ConfigEntryBuilder entryBuilder) {
        builder.add(
                entryBuilder.startBooleanToggle(Component.translatable("config.maid_storage_manager.performance.fast_path_schedule"), Config.fastPathSchedule)
                        .setTooltip(Component.translatable("config.maid_storage_manager.performance.fast_path_schedule.tooltip"))
                        .setSaveConsumer(t -> Config.saveAfter(() -> Config.fastPathSchedule = t))
                        .build()
        );
    }

    private static void addEntryAI(SubCategoryBuilder builder, ConfigEntryBuilder entryBuilder) {
        builder.add(
                entryBuilder.startBooleanToggle(Component.translatable("config.maid_storage_manager.ai.functions"), Config.aiFunctions)
                        .setSaveConsumer(t -> Config.saveAfter(() -> Config.aiFunctions = t))
                        .build()
        );
        builder.add(
                entryBuilder.startBooleanToggle(Component.translatable("config.maid_storage_manager.ai.two_step_ai_response"), Config.twoStepAiResponse)
                        .setTooltip(Component.translatable("config.maid_storage_manager.ai.two_step_ai_response.tooltip"))
                        .setSaveConsumer(t -> Config.saveAfter(() -> Config.twoStepAiResponse = t))
                        .build()
        );
    }

    private static void addEntryUtility(SubCategoryBuilder builder, ConfigEntryBuilder entryBuilder) {
        builder.add(
                entryBuilder.startBooleanToggle(Component.translatable("config.maid_storage_manager.utility.generate_virtual_item_frame"), Config.generateVirtualItemFrame)
                        .setTooltip(Component.translatable("config.maid_storage_manager.utility.generate_virtual_item_frame.tooltip"))
                        .setSaveConsumer(t -> Config.saveAfter(() -> Config.generateVirtualItemFrame = t))
                        .build()
        );
    }

    private static void addEntryRender(SubCategoryBuilder builder, ConfigEntryBuilder entryBuilder) {
        builder.add(
                entryBuilder.startEnumSelector(Component.translatable("config.maid_storage_manager.render.virtual_item_frame_render"), Config.VirtualItemFrameRender.class, Config.virtualItemFrameRender)
                        .setTooltip(Component.translatable("config.maid_storage_manager.render.virtual_item_frame_render.tooltip"))
                        .setEnumNameProvider(t -> Component.translatable("config.maid_storage_manager.render.virtual_item_frame_render." + t.name().toLowerCase()))
                        .setSaveConsumer(t -> Config.saveAfter(() -> Config.virtualItemFrameRender = t))
                        .build()
        );
        builder.add(
                entryBuilder.startBooleanToggle(Component.translatable("config.maid_storage_manager.render.maid_render_ingredient_request"), Config.renderMaidWhenIngredientRequest)
                        .setTooltip(Component.translatable("config.maid_storage_manager.render.maid_render_ingredient_request.tooltip"))
                        .setSaveConsumer(t -> Config.saveAfter(() -> Config.renderMaidWhenIngredientRequest = t))
                        .build()
        );
    }

    private static void addEntryCrafting(SubCategoryBuilder builder, ConfigEntryBuilder entryBuilder) {
        builder.add(
                entryBuilder.startStrList(Component.translatable("config.maid_storage_manager.crafting.solver"), Config.craftingSolver.stream().map(Config.CraftSolver::name).toList())
                        .setTooltip(Component.translatable("config.maid_storage_manager.crafting.solver.tooltip"))
                        .setCellErrorSupplier(s -> Arrays.stream(Config.CraftSolver.values()).anyMatch(v -> v.name().equals(s))
                                ? Optional.empty()
                                : Optional.of(Component.translatable("config.maid_storage_manager.crafting.solver.error"))
                        )
                        .setSaveConsumer(s -> Config.saveAfter(() -> Config.craftingSolver = s.stream().map(Config.CraftSolver::valueOf).toList()))
                        .build()
        );
        builder.add(
                entryBuilder.startBooleanToggle(Component.translatable("config.maid_storage_manager.crafting.match_tag"), Config.craftingMatchTag)
                        .setSaveConsumer(t -> Config.saveAfter(() -> Config.craftingMatchTag = t))
                        .build()
        );
        builder.add(
                entryBuilder.startTextDescription(Component.translatable("config.maid_storage_manager.crafting.match_tag_description")).build()
        );
        builder.add(
                entryBuilder.startStrList(Component.translatable("config.maid_storage_manager.crafting.no_matching_path"), Config.noMatchPaths)
                        .setSaveConsumer(t -> Config.saveAfter(() -> Config.noMatchPaths = t))
                        .build()
        );
        builder.add(
                entryBuilder.startBooleanToggle(Component.translatable("config.maid_storage_manager.crafting.generate"), Config.craftingGenerateCraftGuide)
                        .setSaveConsumer(t -> Config.saveAfter(() -> Config.craftingGenerateCraftGuide = t))
                        .build()
        );
        builder.add(
                entryBuilder.startBooleanToggle(Component.translatable("config.maid_storage_manager.crafting.generate_partial"), Config.generatePartial)
                        .setTooltip(Component.translatable("config.maid_storage_manager.crafting.generate_partial.tooltip"))
                        .setSaveConsumer(t -> Config.saveAfter(() -> Config.generatePartial = t))
                        .build()
        );
        builder.add(
                entryBuilder.startBooleanToggle(Component.translatable("config.maid_storage_manager.crafting.no_calculator"), Config.craftingNoCalculator)
                        .setTooltip(Component.translatable("config.maid_storage_manager.crafting.no_calculator.tooltip"))
                        .setSaveConsumer(t -> Config.saveAfter(() -> Config.craftingNoCalculator = t))
                        .build()
        );

        SubCategoryBuilder b = entryBuilder.startSubCategory(Component.translatable("config.maid_storage_manager.crafting.generating"));
        b.setExpanded(true);
        addEntryGenerating(b, entryBuilder);
        builder.add(b.build());
    }

    @SuppressWarnings("unchecked")
    private static void addEntryGenerating(SubCategoryBuilder builder, ConfigEntryBuilder entryBuilder) {
        Map<String, SubCategoryBuilder> categories = new HashMap<>();
        CraftManager.getInstance()
                .getAutoCraftGuideGenerators()
                .forEach(generator -> {
                    String namespace = generator.getType().getNamespace();
                    if (!categories.containsKey(namespace))
                        categories.put(namespace, entryBuilder.startSubCategory(Component.translatable("config.maid_storage_manager.crafting.generating." + namespace)).setExpanded(true));
                    SubCategoryBuilder sub = categories.get(namespace);
                    //启用-通用项目
                    sub.add(
                            entryBuilder.startBooleanToggle(
                                            Component.translatable(
                                                    "config.maid_storage_manager.crafting.generating.common.enable",
                                                    generator.getConfigName()
                                            ),
                                            GeneratingConfig.isEnabled(generator.getType())
                                    ).setSaveConsumer(b -> GeneratingConfig.setEnable(generator.getType(), b))
                                    .build()
                    );
                    for (ConfigTypes.ConfigType<?> configType : generator.getConfigurations()) {
                        Component translatableName = Component.translatable("config.maid_storage_manager.crafting.generating.common.sub",
                                generator.getConfigName(),
                                configType.getTranslatableName()
                        );
                        AbstractFieldBuilder<?, ?, ?> e =
                                switch (configType.type) {
                                    case String ->
                                            entryBuilder.startStrField(translatableName, (String) configType.getValue())
                                                    .setSaveConsumer(((ConfigTypes.ConfigType<String>) configType)::setValue);
                                    case Integer ->
                                            entryBuilder.startIntField(translatableName, (Integer) configType.getValue())
                                                    .setSaveConsumer(((ConfigTypes.ConfigType<Integer>) configType)::setValue);
                                    case Boolean ->
                                            entryBuilder.startBooleanToggle(translatableName, (Boolean) configType.getValue())
                                                    .setSaveConsumer(((ConfigTypes.ConfigType<Boolean>) configType)::setValue);
                                    case Double ->
                                            entryBuilder.startDoubleField(translatableName, (Double) configType.getValue())
                                                    .setSaveConsumer(((ConfigTypes.ConfigType<Double>) configType)::setValue);
                                    default -> null;
                                };
                        if (e != null) {
                            sub.add(e.build());
                        }
                    }
                });
        builder.addAll(categories.values().stream().map(t -> t.setExpanded(true)).map(SubCategoryBuilder::build).toList());
    }
}
