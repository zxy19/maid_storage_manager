package studio.fantasyit.maid_storage_manager.event;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import studio.fantasyit.maid_storage_manager.craft.CraftManager;
import studio.fantasyit.maid_storage_manager.integration.Integrations;
import studio.fantasyit.maid_storage_manager.integration.cloth.AddClothEvent;
import studio.fantasyit.maid_storage_manager.integration.cloth.ClothEntry;
import studio.fantasyit.maid_storage_manager.integration.create.CreateIntegration;
import studio.fantasyit.maid_storage_manager.storage.MaidStorage;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class StartUpEvent {
    @SubscribeEvent
    public static void onStartUp(FMLLoadCompleteEvent event) {
        MaidStorage.getInstance().collectStorage();
        CraftManager.getInstance().collect();
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class StartUpEventClient {
        @SubscribeEvent
        public static void onStartUp(FMLLoadCompleteEvent event) {
            CreateIntegration.init();
            if (Integrations.clothConfig()) {
                ClothEntry.registryConfigPage();
                MinecraftForge.EVENT_BUS.addListener(AddClothEvent::init);
            }
        }
    }
}