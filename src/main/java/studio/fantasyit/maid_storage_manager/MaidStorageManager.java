package studio.fantasyit.maid_storage_manager;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import studio.fantasyit.maid_storage_manager.registry.*;

@Mod(MaidStorageManager.MODID)
public class MaidStorageManager {
    public static final String MODID = "maid_storage_manager";

    public MaidStorageManager() {
        IEventBus modEventBus = ModLoadingContext.get().getActiveContainer().getEventBus();
        GuiRegistry.init(modEventBus);
        ItemRegistry.register(modEventBus);
        MemoryModuleRegistry.register(modEventBus);
        CreativeTabRegistry.register(modEventBus);
        RecipesRegistry.register(modEventBus);
        EntityRegistry.init(modEventBus);
        SoundEventRegistry.register(modEventBus);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }
}
