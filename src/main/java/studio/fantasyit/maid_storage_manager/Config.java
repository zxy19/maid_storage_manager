package studio.fantasyit.maid_storage_manager;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Forge's config APIs
@Mod.EventBusSubscriber(modid = MaidStorageManager.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue ENABLE_DEBUG = BUILDER
            .comment("Print debug messages")
            .define("debug_msg", false);

    private static final ForgeConfigSpec.BooleanValue ENABLE_AE2SUP = BUILDER
            .comment("Enable ae2 support(Maid may pickup things from terminal)")
            .define("ae2_support", true);

    private static final ForgeConfigSpec.DoubleValue COLLECT_SPEED = BUILDER
            .comment("Speed when collecting requested list")
            .defineInRange("collect_speed", 0.5, 0.0, 3.0);

    private static final ForgeConfigSpec.DoubleValue CRAFT_WORK_SPEED = BUILDER
            .comment("Speed when working crafting")
            .defineInRange("crafting_speed", 0.5, 0.0, 3.0);
    private static final ForgeConfigSpec.DoubleValue VIEW_SPEED = BUILDER
            .comment("Speed when viewing chests in spare time")
            .defineInRange("view_speed", 0.3, 0.0, 3.0);
    private static final ForgeConfigSpec.DoubleValue VIEW_CHANGE_SPEED = BUILDER
            .comment("Speed when viewing chests in spare time")
            .defineInRange("view_change_speed", 0.5, 0.0, 3.0);
    private static final ForgeConfigSpec.DoubleValue PLACE_SPEED = BUILDER
            .comment("Speed when placing items to chests")
            .defineInRange("place_speed", 0.5, 0.0, 3.0);
    private static final ForgeConfigSpec.DoubleValue FOLLOW_SPEED = BUILDER
            .comment("Speed when following")
            .defineInRange("follow_speed", 0.5, 0.0, 3.0);
    private static final ForgeConfigSpec.IntValue MAX_STORE_TRIES = BUILDER
            .comment("Maximum times Maid will try to store items")
            .defineInRange("max_store_tries", 3, 0, 999999);
    private static final ForgeConfigSpec.IntValue MAX_CRAFT_TRIES = BUILDER
            .comment("Maximum tick Maid will try to perform craft action")
            .defineInRange("max_craft_tries", 600, 0, 9999999);
    private static final ForgeConfigSpec.BooleanValue USE_ALL_STORAGE_BY_DEFAULT = BUILDER
            .comment("Enable maid from visiting all storages.")
            .define("use_all_storage", false);

    private static final ForgeConfigSpec.BooleanValue USING_VISIBLE_FRAME = BUILDER
            .comment("Use visible frame when placing allow access/no access/enable.")
            .define("using_visible_frame", true);
    private static final ForgeConfigSpec.BooleanValue TWO_STEP_AI_RESPONSE = BUILDER
            .comment("Allow Maid call AI two times when doing some request.")
            .define("two_step_ai_response", true);
    private static final ForgeConfigSpec.DoubleValue PICKUP_REQUIRE_WHEN_PLACE = BUILDER
            .comment("How many free slots required to pickup items when placing(Value indicates the percentage of empty slots in the total slots")
            .defineInRange("pickup_require_when_place", 0.5, 0.0, 1.0);
    private static final ForgeConfigSpec.BooleanValue FAST_PATH_SCHEDULE = BUILDER
            .comment("Disable the 'shortest path limitation' to gain faster path finding.")
            .define("fast_path_schedule", false);
    private static final ForgeConfigSpec.BooleanValue REAL_WORK_SIM = BUILDER
            .comment("Maid will need to stop and stand by the block to work")
            .define("real_work_sim", true);

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
    public static boolean usingVisibleFrame;
    public static boolean twoStepAiResponse;
    public static double pickupRequireWhenPlace;
    public static boolean fastPathSchedule;
    public static boolean realWorkSim;
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
        usingVisibleFrame = USING_VISIBLE_FRAME.get();
        useAllStorageByDefault = USE_ALL_STORAGE_BY_DEFAULT.get();
        viewChangeSpeed = VIEW_CHANGE_SPEED.get();
        followSpeed = FOLLOW_SPEED.get();
        twoStepAiResponse = TWO_STEP_AI_RESPONSE.get();
        pickupRequireWhenPlace = PICKUP_REQUIRE_WHEN_PLACE.get();
        fastPathSchedule = FAST_PATH_SCHEDULE.get();
        realWorkSim = REAL_WORK_SIM.get();
    }
}
