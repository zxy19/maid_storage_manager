package studio.fantasyit.maid_storage_manager.event;

import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.attachment.CraftBlockOccupy;
import studio.fantasyit.maid_storage_manager.storage.ItemHandler.SimulateTargetInteractHelper;
import studio.fantasyit.maid_storage_manager.storage.StorageVisitLock;

@EventBusSubscriber(modid = MaidStorageManager.MODID, bus = EventBusSubscriber.Bus.GAME)
public class TickServer {
    @SubscribeEvent
    public static void onTick(ServerTickEvent.Post event) {
        SimulateTargetInteractHelper.removeInvalid();
        StorageVisitLock.invalidateInvalidedLock();
    }

    @SubscribeEvent
    public static void onWorldTick(LevelTickEvent.Post event) {
        if (event.getLevel().isClientSide) return;
        CraftBlockOccupy.get(event.getLevel()).tick((ServerLevel) event.getLevel());
    }
}
