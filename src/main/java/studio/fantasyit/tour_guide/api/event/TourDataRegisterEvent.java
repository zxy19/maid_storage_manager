package studio.fantasyit.tour_guide.api.event;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.Event;
import studio.fantasyit.tour_guide.api.TourDataManager;
import studio.fantasyit.tour_guide.data.ITourDataFactory;

public class TourDataRegisterEvent extends Event {
    public void register(ResourceLocation id, ITourDataFactory tourData) {
        TourDataManager.register(id, tourData);
    }
}
