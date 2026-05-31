package studio.fantasyit.maid_storage_manager.craft.generator.util;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import studio.fantasyit.maid_storage_manager.Config;

import java.util.List;

public class RecipeUtil {
    public static Identifier wrapLocation(Identifier generator, Identifier location) {
        return Identifier.fromNamespaceAndPath(location.getNamespace(), location.getPath() + "/" + generator.getNamespace() + "/" + generator.getPath());
    }

    public static boolean shouldSkip(Identifier id, List<Ingredient> ingredients, List<Integer> ingredientCounts, List<ItemStack> output) {
        return Config.generateSkipRecipeIdPattern.stream().anyMatch(patten -> id.toString().matches(patten));
    }
}
