package studio.fantasyit.maid_storage_manager.event;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.data.InventoryListDataClient;
import studio.fantasyit.maid_storage_manager.integration.request.IngredientRequestClient;
import studio.fantasyit.maid_storage_manager.items.CraftGuide;
import studio.fantasyit.maid_storage_manager.items.LogisticsGuide;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;

@EventBusSubscriber(modid = MaidStorageManager.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class TickClient {
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onTick(ClientTickEvent.Post event) {
        InventoryListDataClient.tickShowingInv();
        InventoryListDataClient.getInstance().tickRequest();
        IngredientRequestClient.tickClient();

        showCraftGuideTip();
    }

    private static void showCraftGuideTip() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (mc.player.getMainHandItem().is(ItemRegistry.CRAFT_GUIDE.get())) {
            mc.player.displayClientMessage(CraftGuide.getStatusMessage(mc.player.getMainHandItem()), true);
        } else if (mc.player.getMainHandItem().is(ItemRegistry.LOGISTICS_GUIDE.get())) {
            mc.player.displayClientMessage(LogisticsGuide.getTip(mc.player.getMainHandItem()), true);
        }
    }
}
