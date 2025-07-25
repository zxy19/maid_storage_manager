package studio.fantasyit.maid_storage_manager.event;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.maid.behavior.ScheduleBehavior;
import studio.fantasyit.maid_storage_manager.network.MaidDataSyncToClientPacket;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;

@EventBusSubscriber(modid = MaidStorageManager.MODID, bus = EventBusSubscriber.Bus.GAME)
public class StartTracking {

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof EntityMaid maid && !maid.level().isClientSide) {
            ScheduleBehavior.Schedule current = MemoryUtil.getCurrentlyWorking(maid);
            CompoundTag nbt = new CompoundTag();
            nbt.putInt("id", current.ordinal());
            PacketDistributor.sendToPlayersTrackingEntity(
                    maid,
                    new MaidDataSyncToClientPacket(
                            MaidDataSyncToClientPacket.Type.WORKING,
                            maid.getId(),
                            nbt
                    )
            );
        }
    }

}
