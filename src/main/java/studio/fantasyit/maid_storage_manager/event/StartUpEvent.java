package studio.fantasyit.maid_storage_manager.event;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.common.NeoForge;
import studio.fantasyit.maid_storage_manager.craft.CraftManager;
import studio.fantasyit.maid_storage_manager.integration.Integrations;
import studio.fantasyit.maid_storage_manager.integration.cloth.AddClothEvent;
import studio.fantasyit.maid_storage_manager.integration.cloth.ClothEntry;
import studio.fantasyit.maid_storage_manager.integration.create.CreateIntegration;
import studio.fantasyit.maid_storage_manager.storage.MaidStorage;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class StartUpEvent {
    @SubscribeEvent
    public static void onStartUp(FMLCommonSetupEvent event) {
        MaidStorage.getInstance().collectStorage();
        CraftManager.getInstance().collect();
    }

    @EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class StartUpEventClient {
        @SubscribeEvent
        public static void onStartUp(FMLLoadCompleteEvent event) {
            CreateIntegration.init();
            if (Integrations.clothConfig()) {
                ClothEntry.registryConfigPage();
                NeoForge.EVENT_BUS.addListener(AddClothEvent::init);
            }
        }
    }
}