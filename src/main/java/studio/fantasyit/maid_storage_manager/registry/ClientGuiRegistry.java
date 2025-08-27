package studio.fantasyit.maid_storage_manager.registry;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.maid.config.StorageManagerMaidConfigGui;
import studio.fantasyit.maid_storage_manager.menu.FilterScreen;
import studio.fantasyit.maid_storage_manager.menu.ItemSelectorScreen;
import studio.fantasyit.maid_storage_manager.menu.LogisticsGuideScreen;
import studio.fantasyit.maid_storage_manager.menu.craft.altar.AltarCraftScreen;
import studio.fantasyit.maid_storage_manager.menu.craft.anvil.AnvilCraftScreen;
import studio.fantasyit.maid_storage_manager.menu.craft.brewing.BrewingCraftScreen;
import studio.fantasyit.maid_storage_manager.menu.craft.common.CommonCraftScreen;
import studio.fantasyit.maid_storage_manager.menu.craft.crafting_table.CraftingTableCraftScreen;
import studio.fantasyit.maid_storage_manager.menu.craft.furnace.FurnaceCraftScreen;
import studio.fantasyit.maid_storage_manager.menu.craft.smithing.SmithingCraftScreen;
import studio.fantasyit.maid_storage_manager.menu.craft.stone_cutter.StoneCutterCraftScreen;
import studio.fantasyit.maid_storage_manager.menu.craft.tacz.TaczCraftScreen;
//import studio.fantasyit.maid_storage_manager.menu.craft.tacz.TaczCraftScreen;

@EventBusSubscriber(modid = MaidStorageManager.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientGuiRegistry {
    @SubscribeEvent
    public static void init(RegisterMenuScreensEvent event) {
        event.register(GuiRegistry.ITEM_SELECTOR_MENU.get(), ItemSelectorScreen::new);
        event.register(GuiRegistry.FILTER_MENU.get(), FilterScreen::new);
        event.register(GuiRegistry.STORAGE_MANAGER_MAID_CONFIG_GUI.get(), StorageManagerMaidConfigGui::new);
        event.register(GuiRegistry.LOGISTICS_GUIDE_MENU.get(), LogisticsGuideScreen::new);
        event.register(GuiRegistry.CRAFT_GUIDE_MENU_COMMON.get(), CommonCraftScreen::new);
        event.register(GuiRegistry.CRAFT_GUIDE_MENU_CRAFTING_TABLE.get(), CraftingTableCraftScreen::new);
        event.register(GuiRegistry.CRAFT_GUIDE_MENU_ALTAR.get(), AltarCraftScreen::new);
        event.register(GuiRegistry.CRAFT_GUIDE_MENU_FURNACE.get(), FurnaceCraftScreen::new);
        event.register(GuiRegistry.CRAFT_GUIDE_MENU_SMITHING.get(), SmithingCraftScreen::new);
        event.register(GuiRegistry.CRAFT_GUIDE_MENU_ANVIL.get(), AnvilCraftScreen::new);
        event.register(GuiRegistry.CRAFT_GUIDE_MENU_BREWING.get(), BrewingCraftScreen::new);
        event.register(GuiRegistry.CRAFT_GUIDE_MENU_STONE_CUTTER.get(), StoneCutterCraftScreen::new);
        event.register(GuiRegistry.CRAFT_GUIDE_MENU_TACZ.get(), TaczCraftScreen::new);
    }
}
