package studio.fantasyit.maid_storage_manager.integration;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.LoadingModList;
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
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
        return ModList.get().isLoaded("create")
                && ModList.get().getMods().stream().anyMatch(modInfo -> modInfo.getModId().equals("create")
                && modInfo.getVersion().compareTo(new DefaultArtifactVersion("6.0.0")) >= 0)
                && Config.enableCreateStorage;
    }

    public static boolean createGenerator() {
        return ModList.get().isLoaded("create");
    }

    public static boolean createStockManager() {
        return ModList.get().isLoaded("create")
                && ModList.get().getMods().stream().anyMatch(modInfo -> modInfo.getModId().equals("create")
                && modInfo.getVersion().compareTo(new DefaultArtifactVersion("6.0.0")) >= 0)
                && Config.enableCreateStockManager;
    }

    public static boolean createLoading() {
        ModFileInfo create = LoadingModList.get().getModFileById("create");
        if (create == null) return false;
        return create.getMods().stream().anyMatch(modInfo -> modInfo.getModId().equals("create")
                && modInfo.getVersion().compareTo(new DefaultArtifactVersion("6.0.0")) >= 0);
    }

    public static boolean tacz() {
        return ModList.get().isLoaded("tacz") && Config.enableTacz;
    }

    public static boolean clothConfig() {
        return ModList.get().isLoaded("cloth_config");
    }
}
