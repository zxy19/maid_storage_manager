package studio.fantasyit.tour_guide.client.event;

import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.tour_guide.api.TourDataManager;

@Mod.EventBusSubscriber(modid = MaidStorageManager.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ReloadedEvent {

    @SubscribeEvent
    public static void onReloaded(TagsUpdatedEvent event) {
        TourDataManager.clearAndBroadcastRegister();
    }
}
