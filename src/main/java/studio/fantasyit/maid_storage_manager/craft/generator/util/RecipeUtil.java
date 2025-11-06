package studio.fantasyit.maid_storage_manager.craft.generator.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import studio.fantasyit.maid_storage_manager.Config;

import java.util.List;

public class RecipeUtil {
    public static ResourceLocation wrapLocation(ResourceLocation generator, ResourceLocation location) {
        return new ResourceLocation(location.getNamespace(), location.getPath() + "/" + generator.getNamespace() + "/" + generator.getPath());
    }

    public static boolean shouldSkip(ResourceLocation id, List<Ingredient> ingredients, List<Integer> ingredientCounts, List<ItemStack> output) {
        return Config.generateSkipRecipeIdPattern.stream().anyMatch(patten -> id.toString().matches(patten));
    }
}
