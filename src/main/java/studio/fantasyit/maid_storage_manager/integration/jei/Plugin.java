package studio.fantasyit.maid_storage_manager.integration.jei;

import com.github.tartaricacid.touhoulittlemaid.compat.jei.altar.AltarRecipeCategory;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.menu.base.AbstractFilterScreen;
import studio.fantasyit.maid_storage_manager.menu.craft.altar.AltarCraftMenu;
import studio.fantasyit.maid_storage_manager.menu.craft.anvil.AnvilCraftMenu;
import studio.fantasyit.maid_storage_manager.menu.craft.base.handler.JEIRecipeHandler;
import studio.fantasyit.maid_storage_manager.menu.craft.brewing.BrewingCraftMenu;
import studio.fantasyit.maid_storage_manager.menu.craft.common.JEICommonRecipeHandler;
import studio.fantasyit.maid_storage_manager.menu.craft.crafting_table.CraftingTableCraftMenu;
import studio.fantasyit.maid_storage_manager.menu.craft.furnace.FurnaceCraftMenu;
import studio.fantasyit.maid_storage_manager.menu.craft.smithing.SmithingCraftMenu;
import studio.fantasyit.maid_storage_manager.menu.craft.stone_cutter.JeiStoneCutterRecipeHandler;
import studio.fantasyit.maid_storage_manager.menu.craft.tacz.JEITaczRecipeTransfer;
import studio.fantasyit.maid_storage_manager.registry.GuiRegistry;

@JeiPlugin
public class Plugin implements IModPlugin {
    public static IJeiRuntime jeiRuntime;

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(MaidStorageManager.MODID, "jei");
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGhostIngredientHandler(AbstractFilterScreen.class, new GhostIngredientHandler());
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addUniversalRecipeTransferHandler(new RequestRecipeHandler());
        registration.addUniversalRecipeTransferHandler(new JEICommonRecipeHandler());
        registration.addRecipeTransferHandler(
                new JEIRecipeHandler<>(
                        CraftingTableCraftMenu.class,
                        RecipeTypes.CRAFTING,
                        GuiRegistry.CRAFT_GUIDE_MENU_CRAFTING_TABLE.get()
                ), RecipeTypes.CRAFTING);
        registration.addRecipeTransferHandler(
                new JEIRecipeHandler<>(
                        FurnaceCraftMenu.class,
                        RecipeTypes.SMELTING,
                        GuiRegistry.CRAFT_GUIDE_MENU_FURNACE.get()
                ), RecipeTypes.SMELTING);
        registration.addRecipeTransferHandler(
                new JEIRecipeHandler<>(
                        BrewingCraftMenu.class,
                        RecipeTypes.BREWING,
                        GuiRegistry.CRAFT_GUIDE_MENU_BREWING.get()
                ), RecipeTypes.BREWING);
        registration.addRecipeTransferHandler(
                new JEIRecipeHandler<>(
                        AnvilCraftMenu.class,
                        RecipeTypes.ANVIL,
                        GuiRegistry.CRAFT_GUIDE_MENU_ANVIL.get()
                ), RecipeTypes.ANVIL);
        registration.addRecipeTransferHandler(
                new JEIRecipeHandler<>(
                        AltarCraftMenu.class,
                        AltarRecipeCategory.ALTAR,
                        GuiRegistry.CRAFT_GUIDE_MENU_ALTAR.get()
                ), AltarRecipeCategory.ALTAR);
        registration.addRecipeTransferHandler(
                new JEIRecipeHandler<>(
                        SmithingCraftMenu.class,
                        RecipeTypes.SMITHING,
                        GuiRegistry.CRAFT_GUIDE_MENU_SMITHING.get()
                ), RecipeTypes.SMITHING);
        registration.addRecipeTransferHandler(new JeiStoneCutterRecipeHandler(), RecipeTypes.STONECUTTING);
        registration.addUniversalRecipeTransferHandler(new JEITaczRecipeTransfer());
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        Plugin.jeiRuntime = jeiRuntime;
    }
}
