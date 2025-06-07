package studio.fantasyit.maid_storage_manager;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.util.Arrays;
import java.util.List;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Forge's config APIs
@Mod.EventBusSubscriber(modid = MaidStorageManager.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {

    public enum VirtualItemFrameRender {
        SMALL,
        LARGE,
        FRAME,
        CORNER
    }

    public enum CraftSolver {
        TOPOLOGY,
        DFS,
        DFS_QUEUED
    }

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue ENABLE_DEBUG = BUILDER
            .comment("Print debug messages")
            .define("debug_msg", false);

    //兼容性选项
    private static final ForgeConfigSpec.BooleanValue ENABLE_AE2SUP = BUILDER
            .comment("Enable ae2 support(Maid may pickup things from terminal)")
            .define("compat.ae2_support", true);
    private static final ForgeConfigSpec.BooleanValue ENABLE_RS_SUP = BUILDER
            .comment("Enable rs support(Maid may pickup things from rs)")
            .define("compat.rs_support", true);
    private static final ForgeConfigSpec.BooleanValue ENABLE_JEI_INGREDIENT_REQUEST = BUILDER
            .comment("Ingredient request for JEI")
            .define("compat.jei_ingredient_request", true);
    private static final ForgeConfigSpec.BooleanValue ENABLE_EMI_INGREDIENT_REQUEST = BUILDER
            .comment("Ingredient request for EMI")
            .define("compat.emi_ingredient_request", true);
    private static final ForgeConfigSpec.BooleanValue ENABLE_CREATE_STORAGE = BUILDER
            .comment("Enable create's stock ticker support for maid")
            .define("compat.create_storage", true);
    private static final ForgeConfigSpec.BooleanValue ENABLE_CREATE_STORAGE_MANAGER = BUILDER
            .comment("Allow maid to act as create stock keeper around a stock ticker.")
            .define("compat.create_stock_keeper", true);
    private static final ForgeConfigSpec.IntValue ENABLE_CREATE_STOCK_RANGE_V = BUILDER
            .comment("How far maid can control the stock ticker. Vertically")
            .defineInRange("compat.create_stock_keeper_range_v", 16, 1, 256);
    private static final ForgeConfigSpec.IntValue ENABLE_CREATE_STOCK_RANGE_H = BUILDER
            .comment("How far maid can control the stock ticker. Horizontally")
            .defineInRange("compat.create_stock_keeper_range_h", 7, 1, 64);


    //速度控制选项
    private static final ForgeConfigSpec.DoubleValue CRAFT_WORK_SPEED = BUILDER
            .comment("Speed when working crafting")
            .defineInRange("speed.crafting_speed", 0.5, 0.0, 3.0);
    private static final ForgeConfigSpec.DoubleValue COLLECT_SPEED = BUILDER
            .comment("Speed when collecting requested list")
            .defineInRange("speed.collect_speed", 0.5, 0.0, 3.0);

    private static final ForgeConfigSpec.DoubleValue VIEW_SPEED = BUILDER
            .comment("Speed when viewing chests in spare time")
            .defineInRange("speed.view_speed", 0.3, 0.0, 3.0);
    private static final ForgeConfigSpec.DoubleValue VIEW_CHANGE_SPEED = BUILDER
            .comment("Speed when viewing chests in spare time")
            .defineInRange("speed.view_change_speed", 0.5, 0.0, 3.0);
    private static final ForgeConfigSpec.DoubleValue PLACE_SPEED = BUILDER
            .comment("Speed when placing items to chests")
            .defineInRange("speed.place_speed", 0.5, 0.0, 3.0);
    private static final ForgeConfigSpec.DoubleValue FOLLOW_SPEED = BUILDER
            .comment("Speed when following")
            .defineInRange("speed.follow_speed", 0.5, 0.0, 3.0);

    //行为控制选项
    private static final ForgeConfigSpec.IntValue MAX_STORE_TRIES = BUILDER
            .comment("Maximum times Maid will try to store items")
            .defineInRange("behavior.max_store_tries", 3, 0, 999999);
    private static final ForgeConfigSpec.IntValue MAX_CRAFT_TRIES = BUILDER
            .comment("Maximum tick Maid will try to perform craft action")
            .defineInRange("behavior.max_craft_tries", 600, 0, 9999999);
    private static final ForgeConfigSpec.IntValue MAX_LOGISTICS_TRIES = BUILDER
            .comment("Maximum logistics extract try will maid take")
            .defineInRange("behavior.max_logistics_tries", 50, 0, 9999999);

    private static final ForgeConfigSpec.BooleanValue USE_ALL_STORAGE_BY_DEFAULT = BUILDER
            .comment("Enable maid from visiting all storages.")
            .define("behavior.use_all_storage", false);
    private static final ForgeConfigSpec.DoubleValue PICKUP_REQUIRE_WHEN_PLACE = BUILDER
            .comment("How many free slots required to pickup items when placing(Value indicates the percentage of empty slots in the total slots")
            .defineInRange("behavior.pickup_require_when_place", 0.5, 0.0, 1.0);
    private static final ForgeConfigSpec.BooleanValue REAL_WORK_SIM = BUILDER
            .comment("Maid will need to stop and stand by the block to work")
            .define("behavior.real_work_sim", false);
    private static final ForgeConfigSpec.BooleanValue PICKUP_IGNORE_DELAY = BUILDER
            .comment("Maid will ignore delay when picking up items.")
            .define("behavior.pickup_ignore_delay", true);
    //渲染控制选项
    private static final ForgeConfigSpec.ConfigValue<VirtualItemFrameRender> VIRTUAL_ITEM_FRAME_RENDER = BUILDER
            .comment("Virtual Item Frame's render method allow access/no access/filter.")
            .defineEnum("render.virtual_item_frame_render", VirtualItemFrameRender.LARGE);
    private static final ForgeConfigSpec.BooleanValue RENDER_MAID_WHEN_INGREDIENT_REQUEST = BUILDER
            .comment("Render the maid at the bottom of the screen when pressing Alt.")
            .define("render.maid_render_ingredient_request", true);

    //性能
    private static final ForgeConfigSpec.BooleanValue FAST_PATH_SCHEDULE = BUILDER
            .comment("Disable the 'shortest path limitation' to gain faster path finding.")
            .define("performance.fast_path_schedule", false);
    //AI
    private static final ForgeConfigSpec.BooleanValue TWO_STEP_AI_RESPONSE = BUILDER
            .comment("Allow Maid call AI two times when doing some request.")
            .define("ai.two_step_ai_response", true);

    private static final ForgeConfigSpec.BooleanValue AI_FUNCTIONS = BUILDER
            .comment("Enable function calls from this mod")
            .define("ai.functions", true);
    private static final ForgeConfigSpec.BooleanValue GENERATE_VIRTUAL_ITEM_FRAME = BUILDER
            .comment("Generate virtual item frame entity when shift right-click with certain items.")
            .define("utility.generate_virtual_item_frame", true);
    private static final ForgeConfigSpec.ConfigValue<List<String>> CRAFTING_SOLVER = BUILDER
            .comment("Crafting solver to use. [DFS/DFS_QUEUED/TOPOLOGY]")
            .define("crafting.solver",
                    List.of(CraftSolver.DFS_QUEUED.name()),
                    o -> o instanceof List && Arrays.stream(CraftSolver.values()).map(CraftSolver::name).toList().containsAll((List<?>) o)
            );

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static boolean enableDebug;
    public static boolean enableAe2Sup;
    public static boolean enableRsSup;
    public static boolean enableJeiIngredientRequest;
    public static boolean enableEmiIngredientRequest;
    public static boolean enableCreateStorage;
    public static boolean enableCreateStockManager;
    public static double createStockKeeperRangeV;
    public static double createStockKeeperRangeH;
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

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        enableDebug = ENABLE_DEBUG.get();
        enableAe2Sup = ENABLE_AE2SUP.get();
        enableRsSup = ENABLE_RS_SUP.get();
        enableJeiIngredientRequest = ENABLE_JEI_INGREDIENT_REQUEST.get();
        enableEmiIngredientRequest = ENABLE_EMI_INGREDIENT_REQUEST.get();
        enableCreateStorage = ENABLE_CREATE_STORAGE.get();
        enableCreateStockManager = ENABLE_CREATE_STORAGE_MANAGER.get();
        createStockKeeperRangeV = ENABLE_CREATE_STOCK_RANGE_V.get();
        createStockKeeperRangeH = ENABLE_CREATE_STOCK_RANGE_H.get();
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
    }
}
