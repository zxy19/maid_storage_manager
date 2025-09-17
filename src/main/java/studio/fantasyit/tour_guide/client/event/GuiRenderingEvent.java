package studio.fantasyit.tour_guide.client.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.tour_guide.client.MarkRendererManager;
import studio.fantasyit.tour_guide.client.TourGuidingClientData;

@Mod.EventBusSubscriber(modid = MaidStorageManager.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class GuiRenderingEvent {

    @SubscribeEvent
    public static void onGuiRender(RenderGuiEvent.Post event) {
        Screen screen = Minecraft.getInstance().screen;
        if (screen == null)
            TourGuidingClientData.getMarks().forEach(mark -> {
                MarkRendererManager.dispatchGuiRender(mark, event.getGuiGraphics(), screen);
            });
    }

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        Screen screen = event.getScreen();
        TourGuidingClientData.getMarks().forEach(mark -> {
            MarkRendererManager.dispatchGuiRender(mark, event.getGuiGraphics(), screen);
        });
    }
}
