package studio.fantasyit.maid_storage_manager.integration.tour_guide;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.tour_guide.api.event.ItemTourGuideRegisterEvent;
import studio.fantasyit.tour_guide.api.event.TourDataRegisterEvent;

@EventBusSubscriber(modid = MaidStorageManager.MODID, bus = EventBusSubscriber.Bus.GAME)
public class TourGuideRegister {
    @SubscribeEvent
    public static void onRegisterTourGuideItem(ItemTourGuideRegisterEvent event) {
    }

    @SubscribeEvent
    public static void onRegisterTourGuide(TourDataRegisterEvent event) {
    }
}
