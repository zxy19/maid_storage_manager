package studio.fantasyit.tour_guide.api.event;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.IModBusEvent;
import studio.fantasyit.tour_guide.client.IGuiMarkRenderer;
import studio.fantasyit.tour_guide.client.IWorldMarkRenderer;
import studio.fantasyit.tour_guide.client.MarkRendererManager;

public class ClientMarkRendererRegisterEvent extends Event implements IModBusEvent {
    public void register(ResourceLocation id, IGuiMarkRenderer<?> renderer) {
        MarkRendererManager.register(id, renderer);
    }

    public void register(ResourceLocation id, IWorldMarkRenderer<?> renderer) {
        MarkRendererManager.register(id, renderer);
    }
}
