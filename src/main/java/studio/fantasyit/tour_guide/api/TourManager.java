package studio.fantasyit.tour_guide.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.tour_guide.data.TourData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class TourManager {
    private static final Map<UUID, TourData> RUNNING_TOUR = new HashMap<>();

    public static boolean start(ServerPlayer player, ResourceLocation id) {
        if (RUNNING_TOUR.containsKey(player.getUUID())) {
            stop(player);
        }
        TourData tourData = TourDataManager.get(id, player);
        if (tourData == null)
            return false;
        RUNNING_TOUR.put(player.getUUID(), tourData);
        tourData.start();
        return true;
    }

    public static void stop(ServerPlayer player) {
        if (!RUNNING_TOUR.containsKey(player.getUUID()))
            return;
        TourData tourData = RUNNING_TOUR.get(player.getUUID());
        tourData.stop();
    }

    public static void remove(ServerPlayer sender) {
        RUNNING_TOUR.remove(sender.getUUID());
    }

    public static @Nullable TourData get(ServerPlayer sender) {
        return RUNNING_TOUR.get(sender.getUUID());
    }

    public static void each(Consumer<TourData> o) {
        RUNNING_TOUR.values().forEach(o);
    }
}
