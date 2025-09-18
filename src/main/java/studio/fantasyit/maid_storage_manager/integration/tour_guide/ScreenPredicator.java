package studio.fantasyit.maid_storage_manager.integration.tour_guide;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.integration.tour_guide.tours.InventoryListTour;
import studio.fantasyit.maid_storage_manager.integration.tour_guide.tours.RequestListTour;
import studio.fantasyit.maid_storage_manager.menu.InventoryListScreen;
import studio.fantasyit.maid_storage_manager.menu.ItemSelectorScreen;
import studio.fantasyit.tour_guide.api.event.ScreenPredicatorRegisterEvent;

@EventBusSubscriber(modid = MaidStorageManager.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ScreenPredicator {

    @SubscribeEvent
    public static void register(ScreenPredicatorRegisterEvent event) {
        event.register(InventoryListTour.GUI_INVENTORY_SCREEN, t -> t instanceof InventoryListScreen);
        event.register(RequestListTour.GUI_REQUEST_LIST, t -> t instanceof ItemSelectorScreen);
        event.register(RequestListTour.GUI_REQUEST_LIST_NO_OFFSET, t -> t instanceof ItemSelectorScreen);
        event.register(RequestListTour.GUI_REQUEST_LIST_NO_OFFSET, (t, g) -> {
            if (t instanceof ItemSelectorScreen iss) {
                g.pose().translate(iss.getGuiLeft() + 88, 0, 0);
            }
        });
    }
}
