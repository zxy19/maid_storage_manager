package studio.fantasyit.maid_storage_manager;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import studio.fantasyit.maid_storage_manager.craft.algo.misc.CraftPlanEvaluator;

import java.util.Arrays;
import java.util.List;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Forge's config APIs
@EventBusSubscriber(modid = MaidStorageManager.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Config {

    public enum VirtualItemFrameRender {
        SMALL,
        LARGE,
        FRAME,
        CORNER
    }

    public enum ThrowMethod {
        FINALLY_POS,
        GO_THROUGH,
        FIXED
    }

    public enum CraftSolver {
        TOPOLOGY,
        DFS,
        DFS_QUEUED,
        DFS_THREADED
    }

    public enum CraftGenerator {
        RELEVANCE,
        RELEVANCE_THREADED
    }


    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue ENABLE_DEBUG = BUILDER
            .comment("Print debug messages")
            .define("debug_msg", false);

    //兼容性选项
    private static final ModConfigSpec.BooleanValue ENABLE_AE2SUP = BUILDER
            .comment("Enable ae2 support(Maid may pickup things from terminal)")
            .define("compat.ae2_support", true);
    private static final ModConfigSpec.BooleanValue ENABLE_RS_SUP = BUILDER
            .comment("Enable rs support(Maid may pickup things from rs)")
            .define("compat.rs_support", true);
    private static final ModConfigSpec.BooleanValue ENABLE_MEK_SUP = BUILDER
            .comment("Enable Mekanism QIO support(Maid may pickup things from rs)")
            .define("compat.mek_support", true);
    private static final ModConfigSpec.BooleanValue ENABLE_JEI_INGREDIENT_REQUEST = BUILDER
            .comment("Ingredient request for JEI")
            .define("compat.jei_ingredient_request", true);
    private static final ModConfigSpec.BooleanValue ENABLE_EMI_INGREDIENT_REQUEST = BUILDER
            .comment("Ingredient request for EMI")
            .define("compat.emi_ingredient_request", true);
    private static final ModConfigSpec.BooleanValue ENABLE_CREATE_STORAGE = BUILDER
            .comment("Enable create's stock ticker support for maid")
            .define("compat.create_storage", true);
    private static final ModConfigSpec.BooleanValue ENABLE_TACZ = BUILDER
            .comment("Enable tacz recipe support")
            .define("compat.create_stock_manager", true);
    private static final ModConfigSpec.BooleanValue ENABLE_CREATE_STORAGE_MANAGER = BUILDER
            .comment("Allow maid to act as create stock keeper around a stock ticker.")
            .define("compat.create_stock_keeper", true);
    private static final ModConfigSpec.IntValue ENABLE_CREATE_STOCK_RANGE_V = BUILDER
            .comment("How far maid can control the stock ticker. Vertically")
            .defineInRange("compat.create_stock_keeper_range_v", 16, 1, 256);
    private static final ModConfigSpec.IntValue ENABLE_CREATE_STOCK_RANGE_H = BUILDER
            .comment("How far maid can control the stock ticker. Horizontally")
            .defineInRange("compat.create_stock_keeper_range_h", 7, 1, 64);
    private static final ModConfigSpec.ConfigValue<String> CREATE_ADDRESS_PATTERN = BUILDER
            .comment("The format of create package address.<UUID>,<UUID4>,<UUID8>,<TYPE>,<TYPE1>")
            .define("compat.create_address_pattern", "maid<TYPE1>:<UUID4>");


    //速度控制选项
    private static final ModConfigSpec.DoubleValue CRAFT_WORK_SPEED = BUILDER
            .comment("Speed when working crafting")
            .defineInRange("speed.crafting_speed", 0.5, 0.0, 3.0);
    private static final ModConfigSpec.DoubleValue COLLECT_SPEED = BUILDER
            .comment("Speed when collecting requested list")
            .defineInRange("speed.collect_speed", 0.5, 0.0, 3.0);

    private static final ModConfigSpec.DoubleValue VIEW_SPEED = BUILDER
            .comment("Speed when viewing chests in spare time")
            .defineInRange("speed.view_speed", 0.3, 0.0, 3.0);
    private static final ModConfigSpec.DoubleValue VIEW_CHANGE_SPEED = BUILDER
            .comment("Speed when viewing chests in spare time")
            .defineInRange("speed.view_change_speed", 0.5, 0.0, 3.0);
    private static final ModConfigSpec.DoubleValue PLACE_SPEED = BUILDER
            .comment("Speed when placing items to chests")
            .defineInRange("speed.place_speed", 0.5, 0.0, 3.0);
    private static final ModConfigSpec.DoubleValue FOLLOW_SPEED = BUILDER
            .comment("Speed when following")
            .defineInRange("speed.follow_speed", 0.5, 0.0, 3.0);

    //行为控制选项
    private static final ModConfigSpec.IntValue MAX_STORE_TRIES = BUILDER
            .comment("Maximum times Maid will try to store items")
            .defineInRange("behavior.max_store_tries", 3, 0, 999999);
    private static final ModConfigSpec.IntValue MAX_CRAFT_TRIES = BUILDER
            .comment("Maximum tick Maid will try to perform craft action")
            .defineInRange("behavior.max_craft_tries", 600, 0, 9999999);
    private static final ModConfigSpec.IntValue MAX_LOGISTICS_TRIES = BUILDER
            .comment("Maximum logistics extract try will maid take")
            .defineInRange("behavior.max_logistics_tries", 50, 0, 9999999);

    private static final ModConfigSpec.BooleanValue USE_ALL_STORAGE_BY_DEFAULT = BUILDER
            .comment("Enable maid from visiting all storages.")
            .define("behavior.use_all_storage", false);
    private static final ModConfigSpec.DoubleValue PICKUP_REQUIRE_WHEN_PLACE = BUILDER
            .comment("How many free slots required to pickup items when placing(Value indicates the percentage of empty slots in the total slots")
            .defineInRange("behavior.pickup_require_when_place", 0.5, 0.0, 1.0);
    private static final ModConfigSpec.BooleanValue REAL_WORK_SIM = BUILDER
            .comment("Maid will need to stop and stand by the block to work")
            .define("behavior.real_work_sim", false);
    private static final ModConfigSpec.BooleanValue PICKUP_IGNORE_DELAY = BUILDER
            .comment("Maid will ignore delay when picking up items.")
            .define("behavior.pickup_ignore_delay", true);
    private static final ModConfigSpec.BooleanValue NO_BUBBLE_FOR_SUB_TASK = BUILDER
            .comment("No showing bubbles for sub tasks")
            .define("behavior.no_bubble_for_sub_task", false);

    private static final ModConfigSpec.EnumValue<ThrowMethod> THROW_ITEM_VECTOR = BUILDER
            .comment("How maid will throw Item.FINALLY_POS will try make the item stop at the position. GO_THROUGH will try to make item go through the target position. FIXED will always use the vector of length 0.6")
            .defineEnum("behavior.throw_item_vector", ThrowMethod.FINALLY_POS, ThrowMethod.values());
    //渲染控制选项
    private static final ModConfigSpec.ConfigValue<VirtualItemFrameRender> VIRTUAL_ITEM_FRAME_RENDER = BUILDER
            .comment("Virtual Item Frame's render method allow access/no access/filter.")
            .defineEnum("render.virtual_item_frame_render", VirtualItemFrameRender.LARGE);
    private static final ModConfigSpec.BooleanValue RENDER_MAID_WHEN_INGREDIENT_REQUEST = BUILDER
            .comment("Render the maid at the bottom of the screen when pressing ingredient request key.")
            .define("render.maid_render_ingredient_request", true);

    //性能
    private static final ModConfigSpec.BooleanValue FAST_PATH_SCHEDULE = BUILDER
            .comment("Disable the 'shortest path limitation' to gain faster path finding.")
            .define("performance.fast_path_schedule", false);
    //AI
    private static final ModConfigSpec.BooleanValue TWO_STEP_AI_RESPONSE = BUILDER
            .comment("Allow Maid call AI two times when doing some request.")
            .define("ai.two_step_ai_response", true);

    private static final ModConfigSpec.BooleanValue AI_FUNCTIONS = BUILDER
            .comment("Enable function calls from this mod")
            .define("ai.functions", true);
    private static final ModConfigSpec.BooleanValue GENERATE_VIRTUAL_ITEM_FRAME = BUILDER
            .comment("Generate virtual item frame entity when shift right-click with certain items.")
            .define("utility.generate_virtual_item_frame", true);
    private static final ModConfigSpec.ConfigValue<List<String>> CRAFTING_SOLVER = BUILDER
            .comment("Crafting solver to use. [DFS/DFS_QUEUED/DFS_THREADED/TOPOLOGY]. Topology algorithm costs least but dose not support circular recipes.")
            .define("crafting.solver",
                    List.of(CraftSolver.DFS_THREADED.name()),
                    o -> o instanceof List && Arrays.stream(CraftSolver.values()).map(CraftSolver::name).toList().containsAll((List<?>) o)
            );
    private static final ModConfigSpec.IntValue LOOP_SOLVER_MAX_KEEP_LENGTH = BUILDER
            .comment("Max length to calculate in loop solver.")
            .defineInRange("crafting.loop_solver.max_length", 10, 0, 100);
    private static final ModConfigSpec.BooleanValue LOOP_SOLVER_PREVENT_INDIRECT_ITEM_SUPPLY = BUILDER
            .comment("Prevent item in loop that are fully input outside the loop.")
            .define("crafting.loop_solver.prevent_indirect", true);
    private static final ModConfigSpec.BooleanValue LOOP_SOLVER_PREVENT_NEW_BYPRODUCT = BUILDER
            .comment("Prevent new byproduct from loop that's not available in graph.")
            .define("crafting.loop_solver.prevent_new_byproduct", false);
    private static final ModConfigSpec.BooleanValue USE_NBT = BUILDER
            .comment("Match components for all items by default. Use #maid_storage_manager:no_components and #maid_storage_manager:use_components to modify.")
            .define("crafting.components.default", false);
    private static final ModConfigSpec.ConfigValue<List<? extends String>> NBT_NO_MATCH_PATH = BUILDER
            .comment("Specific those components that are ignored from comparing.")
            .defineListAllowEmpty("crafting.components.no_matching_path", () -> List.of("damage"), o -> o instanceof String);
    private static final ModConfigSpec.BooleanValue CRAFTING_GENERATE_CRAFT_GUIDE = BUILDER
            .comment("Generate craft guides for vanilla recipes.")
            .define("crafting.generate", false);
    private static final ModConfigSpec.BooleanValue CRAFTING_GENERATING_PARTIAL = BUILDER
            .comment("Generate recipes that has not all ingredients available.")
            .define("crafting.generating.keep_partial", false);
    private static final ModConfigSpec.BooleanValue CRAFTING_GENERATING_NEAREST_ONLY = BUILDER
            .comment("Only generate recipe with nearest block to maid.")
            .define("crafting.generating.nearest_only", false);
    private static final ModConfigSpec.EnumValue<CraftGenerator> CRAFTING_GENERATOR = BUILDER
            .comment("Crafting generator algorithm to use.")
            .defineEnum("crafting.generating.algorithm", CraftGenerator.RELEVANCE_THREADED, CraftGenerator.values());
    private static final ModConfigSpec.BooleanValue CRAFTING_NO_CALCULATOR = BUILDER
            .comment("No need portable calculator for crafting")
            .define("crafting.no_calculator", false);
    private static final ModConfigSpec.EnumValue<CraftPlanEvaluator> CRAFTING_PREFER_SHORTEST_PATH = BUILDER
            .comment("The way to select the correct crafting path.")
            .defineEnum("crafting.shortest_path_evaluator", CraftPlanEvaluator.CRAFT_GUIDES, CraftPlanEvaluator.values());


    static final ModConfigSpec SPEC = BUILDER.build();

    public static boolean enableDebug;
    public static boolean enableDebugInv = false;
    public static boolean enableAe2Sup;
    public static boolean enableRsSup;
    public static boolean enableMekSup;
    public static boolean enableJeiIngredientRequest;
    public static boolean enableEmiIngredientRequest;
    public static boolean enableCreateStorage;
    public static boolean enableCreateStockManager;
    public static boolean enableTacz;
    public static int createStockKeeperRangeV;
    public static int createStockKeeperRangeH;
    public static String createAddress;
    public static double collectSpeed;
    public static double viewSpeed;
    public static double placeSpeed;
    public static double viewChangeSpeed;
    public static double craftWorkSpeed;
    public static int maxStoreTries;
    public static int maxCraftTries;
    public static int maxLogisticsTries;
    public static boolean useAllStorageByDefault;
    public static double followSpeed;
    public static ThrowMethod throwItemVector;
    public static VirtualItemFrameRender virtualItemFrameRender;
    public static boolean renderMaidWhenIngredientRequest;
    public static boolean twoStepAiResponse;
    public static double pickupRequireWhenPlace;
    public static boolean fastPathSchedule;
    public static boolean realWorkSim;
    public static boolean aiFunctions;
    public static boolean generateVirtualItemFrame;
    public static boolean pickupIgnoreDelay;
    public static List<CraftSolver> craftingSolver;
    public static boolean craftingMatchTag;
    public static int craftingLoopSolverMaxSize;
    public static boolean craftingLoopSolverPreventIndirect;
    public static boolean craftingLoopSolverPreventNewByProduct;
    public static List<String> noMatchPaths;
    public static boolean craftingGenerateCraftGuide;
    public static boolean craftingNoCalculator;
    public static CraftPlanEvaluator craftingShortestPathEvaluator;
    public static boolean generatePartial;
    public static boolean generateNearestOnly;
    public static CraftGenerator craftingGenerator;
    public static boolean noBubbleForSub;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        enableDebug = ENABLE_DEBUG.get();
        enableAe2Sup = ENABLE_AE2SUP.get();
        enableRsSup = ENABLE_RS_SUP.get();
        enableMekSup = ENABLE_MEK_SUP.get();
        enableJeiIngredientRequest = ENABLE_JEI_INGREDIENT_REQUEST.get();
        enableEmiIngredientRequest = ENABLE_EMI_INGREDIENT_REQUEST.get();
        enableCreateStorage = ENABLE_CREATE_STORAGE.get();
        enableCreateStockManager = ENABLE_CREATE_STORAGE_MANAGER.get();
        enableTacz = ENABLE_TACZ.get();
        createStockKeeperRangeV = ENABLE_CREATE_STOCK_RANGE_V.get();
        createStockKeeperRangeH = ENABLE_CREATE_STOCK_RANGE_H.get();
        createAddress = CREATE_ADDRESS_PATTERN.get();
        collectSpeed = COLLECT_SPEED.get();
        viewSpeed = VIEW_SPEED.get();
        placeSpeed = PLACE_SPEED.get();
        maxStoreTries = MAX_STORE_TRIES.get();
        maxCraftTries = MAX_CRAFT_TRIES.get();
        maxLogisticsTries = MAX_LOGISTICS_TRIES.get();
        craftWorkSpeed = CRAFT_WORK_SPEED.get();
        virtualItemFrameRender = VIRTUAL_ITEM_FRAME_RENDER.get();
        useAllStorageByDefault = USE_ALL_STORAGE_BY_DEFAULT.get();
        viewChangeSpeed = VIEW_CHANGE_SPEED.get();
        followSpeed = FOLLOW_SPEED.get();
        twoStepAiResponse = TWO_STEP_AI_RESPONSE.get();
        pickupRequireWhenPlace = PICKUP_REQUIRE_WHEN_PLACE.get();
        fastPathSchedule = FAST_PATH_SCHEDULE.get();
        realWorkSim = REAL_WORK_SIM.get();
        aiFunctions = AI_FUNCTIONS.get();
        generateVirtualItemFrame = GENERATE_VIRTUAL_ITEM_FRAME.get();
        renderMaidWhenIngredientRequest = RENDER_MAID_WHEN_INGREDIENT_REQUEST.get();
        craftingSolver = CRAFTING_SOLVER.get().stream().map(CraftSolver::valueOf).toList();
        pickupIgnoreDelay = PICKUP_IGNORE_DELAY.get();
        craftingMatchTag = USE_NBT.get();
        noMatchPaths = NBT_NO_MATCH_PATH.get().stream().map(t -> (String) t).toList();
        throwItemVector = THROW_ITEM_VECTOR.get();
        craftingGenerateCraftGuide = CRAFTING_GENERATE_CRAFT_GUIDE.get();
        craftingNoCalculator = CRAFTING_NO_CALCULATOR.get();
        generatePartial = CRAFTING_GENERATING_PARTIAL.get();
        craftingLoopSolverMaxSize = LOOP_SOLVER_MAX_KEEP_LENGTH.get();
        craftingLoopSolverPreventIndirect = LOOP_SOLVER_PREVENT_INDIRECT_ITEM_SUPPLY.get();
        craftingLoopSolverPreventNewByProduct = LOOP_SOLVER_PREVENT_NEW_BYPRODUCT.get();
        craftingGenerator = CRAFTING_GENERATOR.get();
        craftingShortestPathEvaluator = CRAFTING_PREFER_SHORTEST_PATH.get();
        noBubbleForSub = NO_BUBBLE_FOR_SUB_TASK.get();
        generateNearestOnly = CRAFTING_GENERATING_NEAREST_ONLY.get();
    }

    public static void save() {
        ENABLE_DEBUG.set(enableDebug);
        ENABLE_AE2SUP.set(enableAe2Sup);
        ENABLE_RS_SUP.set(enableRsSup);
        ENABLE_MEK_SUP.set(enableMekSup);
        ENABLE_JEI_INGREDIENT_REQUEST.set(enableJeiIngredientRequest);
        ENABLE_EMI_INGREDIENT_REQUEST.set(enableEmiIngredientRequest);
        ENABLE_CREATE_STORAGE.set(enableCreateStorage);
        ENABLE_CREATE_STORAGE_MANAGER.set(enableCreateStockManager);
        ENABLE_TACZ.set(enableTacz);
        ENABLE_CREATE_STOCK_RANGE_V.set(createStockKeeperRangeV);
        ENABLE_CREATE_STOCK_RANGE_H.set(createStockKeeperRangeH);
        CREATE_ADDRESS_PATTERN.set(createAddress);
        COLLECT_SPEED.set(collectSpeed);
        VIEW_SPEED.set(viewSpeed);
        PLACE_SPEED.set(placeSpeed);
        MAX_STORE_TRIES.set(maxStoreTries);
        MAX_CRAFT_TRIES.set(maxCraftTries);
        MAX_LOGISTICS_TRIES.set(maxLogisticsTries);
        CRAFT_WORK_SPEED.set(craftWorkSpeed);
        VIRTUAL_ITEM_FRAME_RENDER.set(virtualItemFrameRender);
        USE_ALL_STORAGE_BY_DEFAULT.set(useAllStorageByDefault);
        VIEW_CHANGE_SPEED.set(viewChangeSpeed);
        FOLLOW_SPEED.set(followSpeed);
        TWO_STEP_AI_RESPONSE.set(twoStepAiResponse);
        PICKUP_REQUIRE_WHEN_PLACE.set(pickupRequireWhenPlace);
        FAST_PATH_SCHEDULE.set(fastPathSchedule);
        REAL_WORK_SIM.set(realWorkSim);
        AI_FUNCTIONS.set(aiFunctions);
        GENERATE_VIRTUAL_ITEM_FRAME.set(generateVirtualItemFrame);
        RENDER_MAID_WHEN_INGREDIENT_REQUEST.set(renderMaidWhenIngredientRequest);
        CRAFTING_SOLVER.set(craftingSolver.stream().map(CraftSolver::name).toList());
        PICKUP_IGNORE_DELAY.set(pickupIgnoreDelay);
        USE_NBT.set(craftingMatchTag);
        NBT_NO_MATCH_PATH.set(noMatchPaths);
        THROW_ITEM_VECTOR.set(throwItemVector);
        CRAFTING_GENERATE_CRAFT_GUIDE.set(craftingGenerateCraftGuide);
        CRAFTING_NO_CALCULATOR.set(craftingNoCalculator);
        CRAFTING_GENERATING_PARTIAL.set(generatePartial);
        LOOP_SOLVER_MAX_KEEP_LENGTH.set(craftingLoopSolverMaxSize);
        LOOP_SOLVER_PREVENT_INDIRECT_ITEM_SUPPLY.set(craftingLoopSolverPreventIndirect);
        LOOP_SOLVER_PREVENT_NEW_BYPRODUCT.set(craftingLoopSolverPreventNewByProduct);
        CRAFTING_GENERATOR.set(craftingGenerator);
        CRAFTING_PREFER_SHORTEST_PATH.set(craftingShortestPathEvaluator);
        NO_BUBBLE_FOR_SUB_TASK.set(noBubbleForSub);
        CRAFTING_GENERATING_NEAREST_ONLY.set(generateNearestOnly);
    }

    public static void saveAfter(Runnable o) {
        o.run();
        Config.save();
    }
}
