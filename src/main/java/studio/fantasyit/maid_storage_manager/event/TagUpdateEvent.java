package studio.fantasyit.maid_storage_manager.event;

import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.communicate.data.TaskDefaultCommunicate;

@Mod.EventBusSubscriber(modid = MaidStorageManager.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TagUpdateEvent {
    @SubscribeEvent
    public static void onTagUpdate(TagsUpdatedEvent event) {
        TaskDefaultCommunicate.init();
    }
}
