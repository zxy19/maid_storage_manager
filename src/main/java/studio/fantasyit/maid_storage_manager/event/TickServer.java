package studio.fantasyit.maid_storage_manager.event;

import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.capability.CraftBlockOccupyDataProvider;
import studio.fantasyit.maid_storage_manager.storage.ItemHandler.SimulateTargetInteractHelper;

@Mod.EventBusSubscriber(modid = MaidStorageManager.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TickServer {
    @SubscribeEvent
    public static void onTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            SimulateTargetInteractHelper.removeInvalid();
        }
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.LevelTickEvent event) {
        if (event.level.isClientSide) return;
        CraftBlockOccupyDataProvider.get(event.level).tick((ServerLevel) event.level);
    }
}
