package studio.fantasyit.maid_storage_manager;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.util.Arrays;
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
    private static final ForgeConfigSpec.DoubleValue VIEW_SPEED = BUILDER
            .comment("Speed when viewing chests in spare time")
            .defineInRange("view_speed", 0.3, 0.0, 3.0);
    private static final ForgeConfigSpec.DoubleValue PLACE_SPEED = BUILDER
            .comment("Speed when placing items to chests")
            .defineInRange("place_speed", 0.5, 0.0, 3.0);
    private static final ForgeConfigSpec.ConfigValue<String> DEBUG = BUILDER
            .comment("debug")
            .define("debug", "-0.86,0.1,-0.55,0.08,0.15,0.45,0.8");

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static boolean enableDebug;
    public static boolean enableAe2Sup;
    public static double collectSpeed;
    public static double viewSpeed;
    public static double placeSpeed;
    public static List<Double> debug;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        enableDebug = ENABLE_DEBUG.get();
        enableAe2Sup = ENABLE_AE2SUP.get();
        collectSpeed = COLLECT_SPEED.get();
        viewSpeed = VIEW_SPEED.get();
        placeSpeed = PLACE_SPEED.get();
        debug = Arrays.stream(DEBUG.get().split(",")).map(Double::parseDouble).collect(Collectors.toList());
    }
}
