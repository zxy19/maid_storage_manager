package studio.fantasyit.maid_storage_manager.integration;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.LoadingModList;
import studio.fantasyit.maid_storage_manager.Config;

public class Integrations {
    public static boolean JEIIngredientRequest() {
        return ModList.get().isLoaded("jei") && Config.enableJeiIngredientRequest;
    }

    public static boolean JEIIngredientRequestLoading() {
        return LoadingModList.get().getModFileById("jei") != null;
    }

    public static boolean EMIngredientRequest() {
        return ModList.get().isLoaded("emi") && Config.enableEmiIngredientRequest;
    }

    public static boolean EMIngredientRequestLoading() {
        return LoadingModList.get().getModFileById("emi") != null;
    }

    public static boolean createStorage() {
        return ModList.get().isLoaded("create") && Config.enableCreateStorage;
    }

    public static boolean createStockManager() {
        return ModList.get().isLoaded("create") && Config.enableCreateStockManager;
    }

    public static boolean createLoading() {
        return LoadingModList.get().getModFileById("create") != null;
    }

    public static boolean tacz(){
        return ModList.get().isLoaded("tacz") && Config.enableTacz;
    }
}
