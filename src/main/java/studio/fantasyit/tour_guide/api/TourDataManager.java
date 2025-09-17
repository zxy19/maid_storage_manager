package studio.fantasyit.tour_guide.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import studio.fantasyit.tour_guide.api.event.TourDataRegisterEvent;
import studio.fantasyit.tour_guide.data.ITourDataFactory;
import studio.fantasyit.tour_guide.data.TourData;

import java.util.HashMap;
import java.util.Map;

public class TourDataManager {
    private static final Map<ResourceLocation, ITourDataFactory> TOURS = new HashMap<>();

    public static void register(ResourceLocation id, ITourDataFactory tourData) {
        TOURS.put(id, tourData);
    }

    public static void clearAndBroadcastRegister() {
        TOURS.clear();
        MinecraftForge.EVENT_BUS.post(new TourDataRegisterEvent());
    }

    public static TourData get(ResourceLocation id, ServerPlayer player) {
        return TOURS.get(id).create(player);
    }
}