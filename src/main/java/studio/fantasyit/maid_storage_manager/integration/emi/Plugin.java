package studio.fantasyit.maid_storage_manager.integration.emi;

import com.github.tartaricacid.touhoulittlemaid.compat.emi.MaidEmiPlugin;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import studio.fantasyit.maid_storage_manager.menu.craft.base.handler.EmiRecipeHandler;
import studio.fantasyit.maid_storage_manager.menu.craft.common.EmiCommonRecipeHandler;
import studio.fantasyit.maid_storage_manager.menu.craft.stone_cutter.EmiStoneCutterRecipeHandler;
import studio.fantasyit.maid_storage_manager.registry.GuiRegistry;

@EmiEntrypoint
public class Plugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        registry.addGenericDragDropHandler(new GhostIngredientHandler());
        registry.addRecipeHandler(GuiRegistry.ITEM_SELECTOR_MENU.get(), new RequestRecipeHandler());
        registry.addRecipeHandler(GuiRegistry.CRAFT_GUIDE_MENU_COMMON.get(), new EmiCommonRecipeHandler());
        registry.addRecipeHandler(GuiRegistry.CRAFT_GUIDE_MENU_SMITHING.get(), new EmiRecipeHandler<>(VanillaEmiRecipeCategories.SMITHING));
        registry.addRecipeHandler(GuiRegistry.CRAFT_GUIDE_MENU_CRAFTING_TABLE.get(), new EmiRecipeHandler<>(VanillaEmiRecipeCategories.CRAFTING));
        registry.addRecipeHandler(GuiRegistry.CRAFT_GUIDE_MENU_FURNACE.get(), new EmiRecipeHandler<>(VanillaEmiRecipeCategories.SMELTING));
        registry.addRecipeHandler(GuiRegistry.CRAFT_GUIDE_MENU_ANVIL.get(), new EmiRecipeHandler<>(VanillaEmiRecipeCategories.ANVIL_REPAIRING));
        registry.addRecipeHandler(GuiRegistry.CRAFT_GUIDE_MENU_STONE_CUTTER.get(), new EmiStoneCutterRecipeHandler());
        registry.addRecipeHandler(GuiRegistry.CRAFT_GUIDE_MENU_ALTAR.get(), new EmiRecipeHandler<>(MaidEmiPlugin.ALTAR));
    }
}
