package studio.fantasyit.maid_storage_manager.craft.generator.util;

import net.minecraft.resources.ResourceLocation;

public class RecipeUtil {
    public static ResourceLocation wrapLocation(ResourceLocation generator, ResourceLocation location) {
        return ResourceLocation.fromNamespaceAndPath(location.getNamespace(), location.getPath() + "/" + generator.getNamespace() + "/" + generator.getPath());
    }
}
