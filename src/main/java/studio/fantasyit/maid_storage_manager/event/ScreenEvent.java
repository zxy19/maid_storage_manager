package studio.fantasyit.maid_storage_manager.event;


import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.data.InScreenTipData;
import studio.fantasyit.maid_storage_manager.integration.request.IngredientRequestClient;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = MaidStorageManager.MODID, value = Dist.CLIENT)
public class ScreenEvent {
    @SubscribeEvent
    public static void onRender(net.minecraftforge.client.event.ScreenEvent.Render event) {
        InScreenTipData.tick();
        InScreenTipData.render(event.getGuiGraphics(), event.getMouseX(), event.getMouseY(), event.getScreen());
        IngredientRequestClient.renderGui(event.getGuiGraphics(), event.getMouseX(), event.getMouseY(), event.getScreen());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onScroll(net.minecraftforge.client.event.ScreenEvent.MouseScrolled event) {
        if (IngredientRequestClient.keyPressed) {
            IngredientRequestClient.scroll(event.getScrollDelta());
            event.setCanceled(true);
        }
    }

}
