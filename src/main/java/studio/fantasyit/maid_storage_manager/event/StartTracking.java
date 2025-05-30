package studio.fantasyit.maid_storage_manager.event;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.maid.behavior.ScheduleBehavior;
import studio.fantasyit.maid_storage_manager.network.MaidDataSyncToClientPacket;
import studio.fantasyit.maid_storage_manager.network.Network;
import studio.fantasyit.maid_storage_manager.util.MemoryUtil;

@Mod.EventBusSubscriber(modid = MaidStorageManager.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class StartTracking {

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof EntityMaid maid && !maid.level().isClientSide) {
            ScheduleBehavior.Schedule current = MemoryUtil.getCurrentlyWorking(maid);
            CompoundTag nbt = new CompoundTag();
            nbt.putInt("id", current.ordinal());
            Network.INSTANCE.send(
                    PacketDistributor.TRACKING_ENTITY.with(() -> maid),
                    new MaidDataSyncToClientPacket(
                            MaidDataSyncToClientPacket.Type.WORKING,
                            maid.getId(),
                            nbt
                    )
            );
        }
    }

}
