package studio.fantasyit.maid_storage_manager.event;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.data.InventoryListDataClient;
import studio.fantasyit.maid_storage_manager.integration.request.IngredientRequestClient;
import studio.fantasyit.maid_storage_manager.items.CraftGuide;
import studio.fantasyit.maid_storage_manager.items.LogisticsGuide;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;

@Mod.EventBusSubscriber(modid = MaidStorageManager.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class TickClient {
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event) {
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
