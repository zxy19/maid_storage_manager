package studio.fantasyit.maid_storage_manager.integration.tour_guide;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.integration.tour_guide.tours.InventoryListTour;
import studio.fantasyit.maid_storage_manager.integration.tour_guide.tours.RequestListTour;
import studio.fantasyit.maid_storage_manager.menu.InventoryListScreen;
import studio.fantasyit.maid_storage_manager.menu.ItemSelectorScreen;
import studio.fantasyit.tour_guide.api.event.ScreenPredicatorRegisterEvent;

@Mod.EventBusSubscriber(modid = MaidStorageManager.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ScreenPredicator {

    @SubscribeEvent
    public static void register(ScreenPredicatorRegisterEvent event) {
        event.register(InventoryListTour.GUI_INVENTORY_SCREEN, t -> t instanceof InventoryListScreen);
        event.register(RequestListTour.GUI_REQUEST_LIST, t -> t instanceof ItemSelectorScreen);
    }
}
