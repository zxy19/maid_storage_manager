package studio.fantasyit.maid_storage_manager.event;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import studio.fantasyit.maid_storage_manager.communicate.data.TaskDefaultCommunicate;
import studio.fantasyit.maid_storage_manager.craft.generator.cache.GraphCache;
import studio.fantasyit.maid_storage_manager.craft.generator.cache.RecipeIngredientCache;

@EventBusSubscriber
public class DataPackSyncEvent {
    @SubscribeEvent
    public static void onDataPackSync(OnDatapackSyncEvent event) {
        if (event.getPlayer() != null)
            RecipeIngredientCache.preFetchCache(event.getPlayer().level().recipeAccess());
        GraphCache.invalidateAll();
        TaskDefaultCommunicate.init();
    }
}
