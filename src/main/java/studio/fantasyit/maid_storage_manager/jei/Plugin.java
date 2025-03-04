package studio.fantasyit.maid_storage_manager.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.menu.AbstractFilterScreen;
import studio.fantasyit.maid_storage_manager.menu.FilterScreen;
import studio.fantasyit.maid_storage_manager.menu.ItemSelectorScreen;

@JeiPlugin
public class Plugin implements IModPlugin {
    public static IJeiRuntime jeiRuntime;

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return new ResourceLocation(MaidStorageManager.MODID, "jei");
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGhostIngredientHandler(AbstractFilterScreen.class, new GhostIngredientHandler());
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addUniversalRecipeTransferHandler(new RecipeHandler());
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        Plugin.jeiRuntime = jeiRuntime;
    }
}
