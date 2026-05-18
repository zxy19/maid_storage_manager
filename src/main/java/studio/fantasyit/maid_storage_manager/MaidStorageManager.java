package studio.fantasyit.maid_storage_manager;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import studio.fantasyit.maid_storage_manager.api.IRequestTaskHandler;
import studio.fantasyit.maid_storage_manager.items.handler.DefaultRequestTaskHandler;
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
        ArgumentRegistry.init(modEventBus);
        SoundEventRegistry.register(modEventBus);
        DataComponentRegistry.register(modEventBus);
        DataAttachmentRegistry.register(modEventBus);
        ModLoadingContext.get().getActiveContainer().registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        modEventBus.addListener(RegisterCapabilitiesEvent.class, event -> {
            event.registerItem(
                    IRequestTaskHandler.CAPABILITY,
                    (stack, ctx) -> DefaultRequestTaskHandler.INSTANCE,
                    ItemRegistry.REQUEST_LIST_ITEM.get(),
                    ItemRegistry.VIRTUAL_REQUEST_LIST_ITEM.get()
            );
        });
    }
}
