package studio.fantasyit.maid_storage_manager;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import studio.fantasyit.maid_storage_manager.registry.*;

@Mod(MaidStorageManager.MODID)
public class MaidStorageManager {
    public static final String MODID = "maid_storage_manager";

    public MaidStorageManager() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        GuiRegistry.init(modEventBus);
        ItemRegistry.register(modEventBus);
        MemoryModuleRegistry.register(modEventBus);
        CreativeTabRegistry.register(modEventBus);
        RecipesRegistry.register(modEventBus);
        EntityRegistry.init(modEventBus);
        ArgumentRegistry.init(modEventBus);
        SoundEventRegistry.register(modEventBus);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }
}
