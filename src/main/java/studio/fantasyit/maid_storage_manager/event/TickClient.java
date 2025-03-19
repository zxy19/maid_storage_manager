package studio.fantasyit.maid_storage_manager.event;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.data.InventoryListDataClient;

@Mod.EventBusSubscriber(modid = MaidStorageManager.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class TickClient {
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event) {
        if (InventoryListDataClient.showingInv == null)
            return;
        InventoryListDataClient.showingTime--;
        if (InventoryListDataClient.showingTime <= 0) {
            InventoryListDataClient.showingInv = null;
        }
    }
}
