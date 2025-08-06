package studio.fantasyit.maid_storage_manager.event;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.data.MaidProgressData;

@Mod.EventBusSubscriber(modid = MaidStorageManager.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
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
