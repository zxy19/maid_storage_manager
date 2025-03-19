package studio.fantasyit.maid_storage_manager;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Forge's config APIs
@Mod.EventBusSubscriber(modid = MaidStorageManager.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue ENABLE_DEBUG = BUILDER
            .comment("Enable debug box render")
            .define("debug_box", false);

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


    //    private static final ForgeConfigSpec.ConfigValue<String> DEBUG = BUILDER
//            .comment("Debug values")
//            .define("debug", "0.5,0.5,0");
    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static boolean enableDebug;
    public static boolean enableAe2Sup;
    public static double collectSpeed;
    public static double viewSpeed;
    public static double placeSpeed;
    public static double viewChangeSpeed;
    public static double craftWorkSpeed;
    public static List<Float> debug;
    public static int maxStoreTries;
    public static int maxCraftTries;
    public static boolean useAllStorageByDefault;
    public static double followSpeed;

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
//        debug = Arrays.stream(DEBUG.get().split(",")).map(Float::parseFloat).toList();
        useAllStorageByDefault = USE_ALL_STORAGE_BY_DEFAULT.get();
        viewChangeSpeed = VIEW_CHANGE_SPEED.get();
        followSpeed = FOLLOW_SPEED.get();
    }
}
