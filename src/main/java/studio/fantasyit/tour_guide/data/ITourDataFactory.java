package studio.fantasyit.tour_guide.data;

import net.minecraft.server.level.ServerPlayer;

public interface ITourDataFactory {
    TourData create(ServerPlayer player);
}
