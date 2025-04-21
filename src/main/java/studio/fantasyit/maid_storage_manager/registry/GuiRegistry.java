package studio.fantasyit.maid_storage_manager.registry;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.maid.config.StorageManagerMaidConfigGui;
import studio.fantasyit.maid_storage_manager.menu.FilterMenu;
import studio.fantasyit.maid_storage_manager.menu.ItemSelectorMenu;
import studio.fantasyit.maid_storage_manager.menu.craft.altar.AltarCraftMenu;
import studio.fantasyit.maid_storage_manager.menu.craft.common.CommonCraftMenu;
import studio.fantasyit.maid_storage_manager.menu.craft.crafting_table.CraftingTableCraftMenu;
import studio.fantasyit.maid_storage_manager.menu.craft.furnace.FurnaceCraftMenu;

public class GuiRegistry {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MaidStorageManager.MODID);
    public static final RegistryObject<MenuType<ItemSelectorMenu>> ITEM_SELECTOR_MENU = MENU_TYPES.register("item_selector",
            () -> IForgeMenuType.create((windowId, inv, data) -> new ItemSelectorMenu(windowId, inv.player)));
    public static final RegistryObject<MenuType<FilterMenu>> FILTER_MENU = MENU_TYPES.register("filter_menu",
            () -> IForgeMenuType.create((windowId, inv, data) -> new FilterMenu(windowId, inv.player)));
    public static final RegistryObject<MenuType<CommonCraftMenu>> CRAFT_GUIDE_MENU_COMMON = MENU_TYPES.register("craft_guide_menu_common",
            () -> IForgeMenuType.create((windowId, inv, data) -> new CommonCraftMenu(windowId, inv.player)));
    public static final RegistryObject<MenuType<CraftingTableCraftMenu>> CRAFT_GUIDE_MENU_CRAFTING_TABLE = MENU_TYPES.register("craft_guide_menu_crafting_table",
            () -> IForgeMenuType.create((windowId, inv, data) -> new CraftingTableCraftMenu(windowId, inv.player)));

    public static final RegistryObject<MenuType<AltarCraftMenu>> CRAFT_GUIDE_MENU_ALTAR = MENU_TYPES.register("craft_guide_menu_altar",
            () -> IForgeMenuType.create((windowId, inv, data) -> new AltarCraftMenu(windowId, inv.player)));
    public static final RegistryObject<MenuType<FurnaceCraftMenu>> CRAFT_GUIDE_MENU_FURNACE = MENU_TYPES.register("craft_guide_menu_furnace",
            () -> IForgeMenuType.create((windowId, inv, data) -> new FurnaceCraftMenu(windowId, inv.player)));

    public static final RegistryObject<MenuType<StorageManagerMaidConfigGui.Container>> STORAGE_MANAGER_MAID_CONFIG_GUI = MENU_TYPES.register("storage_manager_maid_config_gui",
            () -> IForgeMenuType.create((windowId, inv, data) -> new StorageManagerMaidConfigGui.Container(windowId, inv, data.readInt())));

    public static void init(IEventBus modEventBus) {
        MENU_TYPES.register(modEventBus);
    }
}