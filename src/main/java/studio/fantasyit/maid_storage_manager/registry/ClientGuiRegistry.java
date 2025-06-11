package studio.fantasyit.maid_storage_manager.registry;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
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

@Mod.EventBusSubscriber(modid = MaidStorageManager.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientGuiRegistry {
    @SubscribeEvent
    public static void init(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(GuiRegistry.ITEM_SELECTOR_MENU.get(), ItemSelectorScreen::new);
            MenuScreens.register(GuiRegistry.FILTER_MENU.get(), FilterScreen::new);
            MenuScreens.register(GuiRegistry.STORAGE_MANAGER_MAID_CONFIG_GUI.get(), StorageManagerMaidConfigGui::new);
            MenuScreens.register(GuiRegistry.LOGISTICS_GUIDE_MENU.get(), LogisticsGuideScreen::new);
            MenuScreens.register(GuiRegistry.CRAFT_GUIDE_MENU_COMMON.get(), CommonCraftScreen::new);
            MenuScreens.register(GuiRegistry.CRAFT_GUIDE_MENU_CRAFTING_TABLE.get(), CraftingTableCraftScreen::new);
            MenuScreens.register(GuiRegistry.CRAFT_GUIDE_MENU_ALTAR.get(), AltarCraftScreen::new);
            MenuScreens.register(GuiRegistry.CRAFT_GUIDE_MENU_FURNACE.get(), FurnaceCraftScreen::new);
            MenuScreens.register(GuiRegistry.CRAFT_GUIDE_MENU_SMITHING.get(), SmithingCraftScreen::new);
            MenuScreens.register(GuiRegistry.CRAFT_GUIDE_MENU_ANVIL.get(), AnvilCraftScreen::new);
            MenuScreens.register(GuiRegistry.CRAFT_GUIDE_MENU_BREWING.get(), BrewingCraftScreen::new);
            MenuScreens.register(GuiRegistry.CRAFT_GUIDE_MENU_STONE_CUTTER.get(), StoneCutterCraftScreen::new);
            MenuScreens.register(GuiRegistry.CRAFT_GUIDE_MENU_TACZ.get(), TaczCraftScreen::new);
        });
    }
}
