package studio.fantasyit.maid_storage_manager.integration.tour_guide;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.integration.tour_guide.tours.InventoryListTour;
import studio.fantasyit.maid_storage_manager.integration.tour_guide.tours.RequestListTour;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;
import studio.fantasyit.tour_guide.api.event.ItemTourGuideRegisterEvent;
import studio.fantasyit.tour_guide.api.event.TourDataRegisterEvent;

@Mod.EventBusSubscriber(modid = MaidStorageManager.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TourGuideRegister {
    @SubscribeEvent
    public static void onRegisterTourGuideItem(ItemTourGuideRegisterEvent event) {
        event.register(InventoryListTour.ID, ItemRegistry.INVENTORY_LIST.get());
        event.register(RequestListTour.ID, ItemRegistry.REQUEST_LIST_ITEM.get());
    }

    @SubscribeEvent
    public static void onRegisterTourGuide(TourDataRegisterEvent event) {
        event.register(InventoryListTour.ID, InventoryListTour.get());
        event.register(RequestListTour.ID, RequestListTour.get());
    }
}
