package studio.fantasyit.maid_storage_manager.event;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.data.MaidProgressData;

@EventBusSubscriber(modid = MaidStorageManager.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class JoinLevelEvent {
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onJoin(PlayerEvent.PlayerLoggedInEvent event) {
        MaidProgressData.clearAll();
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onJoin(PlayerEvent.PlayerLoggedOutEvent event) {
        MaidProgressData.clearAll();
    }
}
