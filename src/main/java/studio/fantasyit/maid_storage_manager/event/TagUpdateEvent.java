package studio.fantasyit.maid_storage_manager.event;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.communicate.data.TaskDefaultCommunicate;

@EventBusSubscriber(modid = MaidStorageManager.MODID, bus = EventBusSubscriber.Bus.GAME)
public class TagUpdateEvent {
    @SubscribeEvent
    public static void onTagUpdate(TagsUpdatedEvent event) {
        TaskDefaultCommunicate.init();
    }
}
