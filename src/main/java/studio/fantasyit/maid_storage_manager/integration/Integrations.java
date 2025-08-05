package studio.fantasyit.maid_storage_manager.integration;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.LoadingModList;
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import studio.fantasyit.maid_storage_manager.Config;

public class Integrations {
    public static boolean TLMFeatureCenterSidePathFinding() {
        ModFileInfo create = LoadingModList.get().getModFileById("touhou_little_maid");
        if (create == null) return false;
        return create.getMods().stream().anyMatch(modInfo -> modInfo.getModId().equals("touhou_little_maid")
                && modInfo.getVersion().compareTo(new DefaultArtifactVersion("1.3.6")) <= 0);
    }

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

    public static boolean create() {
        return ModList.get().isLoaded("create");
    }

    public static boolean create6() {
        return ModList.get().isLoaded("create")
                && ModList.get().getMods().stream().anyMatch(modInfo -> modInfo.getModId().equals("create")
                && modInfo.getVersion().compareTo(new DefaultArtifactVersion("6.0.0")) >= 0);
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

    public static boolean taczRecipe() {
        return ModList.get().isLoaded("tacz") && Config.enableTacz;
    }

    public static boolean tacz() {
        return ModList.get().isLoaded("tacz");
    }

    public static boolean clothConfig() {
        return ModList.get().isLoaded("cloth_config");
    }

    public static boolean mekanism() {
        return ModList.get().isLoaded("mekanism");
    }

    public static boolean mekanismStorage() {
        return ModList.get().isLoaded("mekanism") && Config.enableMekSup;
    }


    public static boolean ae2Storage() {
        return ModList.get().isLoaded("ae2") && Config.enableAe2Sup;
    }

    public static boolean ae2() {
        return ModList.get().isLoaded("ae2");
    }

    public static boolean rs() {
        return ModList.get().isLoaded("refinedstorage");
    }

    public static boolean rsStorage() {
        return ModList.get().isLoaded("refinedstorage") && Config.enableRsSup;
    }

    public static boolean kjs() {
        return ModList.get().isLoaded("kubejs");
    }

    public static boolean botania() {
        return ModList.get().isLoaded("botania");
    }

    public static boolean ars() {
        return ModList.get().isLoaded("ars_nouveau");
    }
}
