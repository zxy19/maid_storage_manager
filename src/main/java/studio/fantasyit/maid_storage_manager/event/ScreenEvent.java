package studio.fantasyit.maid_storage_manager.event;


import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent.MouseScrolled;
import net.neoforged.neoforge.client.event.ScreenEvent.Render;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.data.InScreenTipData;
import studio.fantasyit.maid_storage_manager.integration.request.IngredientRequestClient;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = MaidStorageManager.MODID, value = Dist.CLIENT)
public class ScreenEvent {
    @SubscribeEvent
    public static void onRender(Render.Post event) {
        InScreenTipData.tick();
        InScreenTipData.render(event.getGuiGraphics(), event.getMouseX(), event.getMouseY(), event.getScreen());
        IngredientRequestClient.renderGui(event.getGuiGraphics(), event.getMouseX(), event.getMouseY(), event.getScreen());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onScroll(MouseScrolled.Pre event) {
        if (IngredientRequestClient.keyPressed) {
            IngredientRequestClient.scroll(event.getScrollDeltaY());
            event.setCanceled(true);
        }
    }

}
