package studio.fantasyit.maid_storage_manager.craft.generator.config;


import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.electronwill.nightconfig.toml.TomlParser;
import com.electronwill.nightconfig.toml.TomlWriter;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.commons.lang3.mutable.MutableBoolean;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.craft.CraftManager;

import java.io.FileReader;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(modid = MaidStorageManager.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GeneratingConfig {
    static CommentedConfig config;
    public static final Path CONFIG_BASE_PATH = FMLPaths.CONFIGDIR.get();
    public static final String NAME = "maid_storage_manager-generating.toml";
    public static Map<ResourceLocation, Boolean> enabled = new HashMap<>();
    public static Map<ResourceLocation, List<ConfigTypes.ConfigType<?>>> configs = new HashMap<>();

    protected static <T> T getOrSetAndChange(String path, MutableBoolean change, T defaultValue) {
        return config.getOrElse(path, () -> {
            change.setValue(true);
            config.set(path, defaultValue);
            return defaultValue;
        });
    }

    public static String getByResourceLocation(ResourceLocation resourceLocation) {
        return resourceLocation.getNamespace() + "." + resourceLocation.getPath();
    }

    public static void load() {
        Path configPath = CONFIG_BASE_PATH.resolve(NAME);
        TomlParser parser = new TomlParser();

        if (configPath.toFile().exists())
            try {
                config = parser.parse(new FileReader(configPath.toFile()));
            } catch (Exception e) {
                config = TomlFormat.newConfig();
            }
        else
            config = TomlFormat.newConfig();

        MutableBoolean change = new MutableBoolean(false);


        CraftManager.getInstance().getAutoCraftGuideGenerators()
                .forEach(generator -> {
                    enabled.put(generator.getType(), getOrSetAndChange(
                            getByResourceLocation(generator.getType()) + ".enable",
                            change,
                            true
                    ));
                    List<ConfigTypes.ConfigType<?>> configuration = generator.getConfigurations();
                    configuration.forEach(configType -> {
                        configType.read(config, getByResourceLocation(generator.getType()), change);
                    });
                    configs.put(generator.getType(), configuration);
                });

        if (change.getValue()) save();
    }

    public static void save() {
        Path configPath = CONFIG_BASE_PATH.resolve(NAME);
        TomlWriter writer = new TomlWriter();

        CraftManager.getInstance().getAutoCraftGuideGenerators()
                .forEach(generator -> {
                    config.set(getByResourceLocation(generator.getType()) + ".enable", enabled.get(generator.getType()));
                    if (configs.containsKey(generator.getType()))
                        configs.get(generator.getType()).forEach(configType -> {
                            configType.save(config, getByResourceLocation(generator.getType()));
                        });
                });

        try {
            if (!configPath.toFile().exists())
                configPath.toFile().createNewFile();
            writer.write(config.unmodifiable(), configPath, WritingMode.REPLACE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isEnabled(ResourceLocation type) {
        if (!enabled.containsKey(type))
            return false;
        return enabled.get(type);
    }

    public static void setEnable(ResourceLocation type, Boolean b) {
        enabled.put(type, b);
        save();
    }

    @SubscribeEvent
    static void regReload(AddReloadListenerEvent event) {
        event.addListener((p_10638_, p_10639_, p_10640_, p_10641_, p_10642_, p_10643_)
                -> // 某个同步方法
                CompletableFuture.runAsync(GeneratingConfig::load).thenCompose((p_10638_::wait)));
    }
}
