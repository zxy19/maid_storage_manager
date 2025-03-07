package studio.fantasyit.maid_storage_manager.registry;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.menu.CraftGuideScreen;
import studio.fantasyit.maid_storage_manager.menu.FilterScreen;
import studio.fantasyit.maid_storage_manager.menu.ItemSelectorScreen;

@Mod.EventBusSubscriber(modid = MaidStorageManager.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientGuiRegistry {
    @SubscribeEvent
    public static void init(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(GuiRegistry.ITEM_SELECTOR_MENU.get(), ItemSelectorScreen::new);
        });
        event.enqueueWork(() -> {
            MenuScreens.register(GuiRegistry.FILTER_MENU.get(), FilterScreen::new);
        });
        event.enqueueWork(() -> {
            MenuScreens.register(GuiRegistry.CRAFT_GUIDE_MENU.get(), CraftGuideScreen::new);
        });
    }
}
