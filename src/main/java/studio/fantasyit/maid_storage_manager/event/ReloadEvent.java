package studio.fantasyit.maid_storage_manager.event;

import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.craft.CraftingTableRecipeStore;

import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, modid = MaidStorageManager.MODID)
public class ReloadEvent {
    @SubscribeEvent
    static void regReload(AddReloadListenerEvent event) {
        event.addListener((p_10638_, p_10639_, p_10640_, p_10641_, p_10642_, p_10643_)
                ->
                CompletableFuture.runAsync(ReloadEvent::reload).thenCompose((p_10638_::wait)));
    }

    public static void reload() {
        CraftingTableRecipeStore.getInstance().invalidate();
    }
}
