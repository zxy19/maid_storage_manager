package studio.fantasyit.maid_storage_manager.integration.kubejs.helper;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import studio.fantasyit.maid_storage_manager.craft.generator.cache.RecipeIngredientCache;

import java.util.List;

public class CacheOperator  {
    public static final CacheOperator INSTANCE = new CacheOperator();

    public void addRecipeObj(RecipeHolder<? extends Recipe<?>> recipe) {
        RecipeIngredientCache.addRecipeCache(recipe);
    }

    public void addRecipe(ResourceLocation id, Ingredient[] ingredients) {
        RecipeIngredientCache.addRecipeCache(id, List.of(ingredients));
    }
}