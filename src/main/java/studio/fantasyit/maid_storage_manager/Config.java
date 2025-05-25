package studio.fantasyit.maid_storage_manager;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

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

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue ENABLE_DEBUG = BUILDER
            .comment("Print debug messages")
            .define("debug_msg", false);

    //兼容性选项
    private static final ForgeConfigSpec.BooleanValue ENABLE_AE2SUP = BUILDER
            .comment("Enable ae2 support(Maid may pickup things from terminal)")
            .define("compat.ae2_support", true);

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
    private static final ForgeConfigSpec.BooleanValue USE_ALL_STORAGE_BY_DEFAULT = BUILDER
            .comment("Enable maid from visiting all storages.")
            .define("behavior.use_all_storage", false);
    private static final ForgeConfigSpec.DoubleValue PICKUP_REQUIRE_WHEN_PLACE = BUILDER
            .comment("How many free slots required to pickup items when placing(Value indicates the percentage of empty slots in the total slots")
            .defineInRange("behavior.pickup_require_when_place", 0.5, 0.0, 1.0);
    private static final ForgeConfigSpec.BooleanValue REAL_WORK_SIM = BUILDER
            .comment("Maid will need to stop and stand by the block to work")
            .define("behavior.real_work_sim", false);

    //渲染控制选项
    private static final ForgeConfigSpec.ConfigValue<VirtualItemFrameRender> VIRTUAL_ITEM_FRAME_RENDER = BUILDER
            .comment("Virtual Item Frame's render method allow access/no access/filter.")
            .defineEnum("render.virtual_item_frame_render", VirtualItemFrameRender.LARGE);
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


    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static boolean enableDebug;
    public static boolean enableAe2Sup;
    public static double collectSpeed;
    public static double viewSpeed;
    public static double placeSpeed;
    public static double viewChangeSpeed;
    public static double craftWorkSpeed;
    public static int maxStoreTries;
    public static int maxCraftTries;
    public static boolean useAllStorageByDefault;
    public static double followSpeed;
    public static VirtualItemFrameRender virtualItemFrameRender;
    public static boolean twoStepAiResponse;
    public static double pickupRequireWhenPlace;
    public static boolean fastPathSchedule;
    public static boolean realWorkSim;
    public static boolean aiFunctions;
    public static boolean generateVirtualItemFrame;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        enableDebug = ENABLE_DEBUG.get();
        enableAe2Sup = ENABLE_AE2SUP.get();
        collectSpeed = COLLECT_SPEED.get();
        viewSpeed = VIEW_SPEED.get();
        placeSpeed = PLACE_SPEED.get();
        maxStoreTries = MAX_STORE_TRIES.get();
        maxCraftTries = MAX_CRAFT_TRIES.get();
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
    }
}
