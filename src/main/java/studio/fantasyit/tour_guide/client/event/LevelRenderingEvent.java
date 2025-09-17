package studio.fantasyit.tour_guide.client.event;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.tour_guide.client.MarkRendererManager;
import studio.fantasyit.tour_guide.client.TourGuidingClientData;

@Mod.EventBusSubscriber(modid = MaidStorageManager.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class LevelRenderingEvent {

    @SubscribeEvent
    public static void onLevelRender(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES)
            return;
        TourGuidingClientData.getMarks().forEach(mark -> {
            MarkRendererManager.dispatchWorldRender(
                    mark,
                    Minecraft.getInstance().renderBuffers().bufferSource(),
                    event.getCamera(),
                    event.getLevelRenderer(),
                    event.getPoseStack(),
                    event.getPartialTick()
            );
        });
    }
}
