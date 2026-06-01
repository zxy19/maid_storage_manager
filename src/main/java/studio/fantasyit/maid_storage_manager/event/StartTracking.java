package studio.fantasyit.maid_storage_manager.event;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.maid.behavior.ScheduleBehavior;
import studio.fantasyit.maid_storage_manager.network.MaidScheduleSyncPacket;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;

@EventBusSubscriber(modid = MaidStorageManager.MODID)
public class StartTracking {

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof EntityMaid maid && !maid.level().isClientSide()) {
            ScheduleBehavior.Schedule current = MemoryUtil.getCurrentlyWorking(maid);
            PacketDistributor.sendToPlayersTrackingEntity(
                    maid,
                    new MaidScheduleSyncPacket(maid.getId(), current.ordinal())
            );
        }
    }

}
