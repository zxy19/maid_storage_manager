package studio.fantasyit.tour_guide.api;

import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public class TourGuideTrigger {
    public static void trigger(ServerPlayer player, String key) {
        Optional.ofNullable(TourManager.get(player)).ifPresent(t -> t.receiveTrigger(key));
    }

    public static void trigger(String key) {
        TourManager.each(
                t -> t.receiveTrigger(key)
        );
    }
}
