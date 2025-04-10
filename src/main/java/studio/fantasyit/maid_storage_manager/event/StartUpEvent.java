package studio.fantasyit.maid_storage_manager.event;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import studio.fantasyit.maid_storage_manager.craft.CraftManager;
import studio.fantasyit.maid_storage_manager.storage.MaidStorage;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class StartUpEvent {
    @SubscribeEvent
    public static void onStartUp(FMLLoadCompleteEvent event) {
        MaidStorage.getInstance().collectStorage();
        CraftManager.getInstance().collect();
    }
}