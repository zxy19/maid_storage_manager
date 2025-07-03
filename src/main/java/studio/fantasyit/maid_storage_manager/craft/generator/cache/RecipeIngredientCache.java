package studio.fantasyit.maid_storage_manager.craft.generator.cache;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import studio.fantasyit.maid_storage_manager.Logger;
import studio.fantasyit.maid_storage_manager.craft.CraftManager;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.generator.algo.ICachableGeneratorGraph;
import studio.fantasyit.maid_storage_manager.craft.generator.algo.node.IngredientNode;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

public class RecipeIngredientCache {
    public static final HashMap<ResourceLocation, List<UUID>> CACHE = new HashMap<>();
    public static final List<CachedIngredient> cachedNode = new ArrayList<>();

    public static void invalidateAll() {
        CACHE.clear();
        cachedNode.clear();
    }

    public static boolean isCached(ResourceLocation recipeId) {
        return CACHE.containsKey(recipeId);
    }

    public static boolean addCahcedRecipeToGraph(ICachableGeneratorGraph graph,
                                                 ResourceLocation id,
                                                 List<Ingredient> ingredients,
                                                 List<Integer> ingredientCounts,
                                                 List<ItemStack> output,
                                                 Function<List<ItemStack>, CraftGuideData> craftGuideSupplier, ResourceLocation type, boolean isOneTime) {
        if (CACHE.containsKey(id) && CACHE.get(id).size() == ingredients.size()) {
            List<IngredientNode> ingredientNodes = new ArrayList<>();
            for (int i = 0; i < ingredients.size(); i++) {
                Ingredient ingredient = ingredients.get(i);
                UUID uuid = CACHE.get(id).get(i);
                IngredientNode ingredientNode = graph.addOrGetCahcedIngredientNode(ingredient, uuid);
                ingredientNodes.add(ingredientNode);
            }
            graph.addRecipeWithIngredients(id, ingredients, ingredientCounts, output, ingredientNodes, craftGuideSupplier, type, isOneTime);
            return true;
        }
        if (CACHE.containsKey(id))
            Logger.error(
                    "Recipe %s was cached with incorrect ingredient count [%d cached and %d added]",
                    id,
                    CACHE.get(id).size(),
                    ingredients.size()
            );
        return false;
    }

    public static void addRecipeCache(Recipe<?> recipe) {
        RecipeIngredientCache.addRecipeCache(recipe.getId(), recipe.getIngredients());
    }

    public static void addRecipeCache(ResourceLocation id, List<Ingredient> ingredients) {
        List<UUID> cachedIngredientNodeUUID = new ArrayList<>();
        for (Ingredient ingredient : ingredients) {
            boolean found = false;
            for (CachedIngredient ingredientNode : cachedNode) {
                if (ingredientNode.isEqualTo(ingredient)) {
                    found = true;
                    cachedIngredientNodeUUID.add(ingredientNode.cachedUUID);
                    break;
                }
            }
            if (!found) {
                UUID uuid = UUID.randomUUID();
                cachedNode.add(new CachedIngredient(ingredient.getItems(), uuid));
                cachedIngredientNodeUUID.add(uuid);
            }
        }
        CACHE.put(id, cachedIngredientNodeUUID);
    }

    public static int getUncachedRecipeIngredient(ResourceLocation id, List<Ingredient> ingredients, ICachableGeneratorGraph generatorGraph) {
        if (!isCached(id)) return ingredients.size();
        int c = 0;
        for (UUID ingredient : CACHE.get(id)) {
            if (!generatorGraph.hasCachedIngredientNode(ingredient)) c++;
        }
        return c;
    }

    public static void preFetchCache(RecipeManager manager) {
        invalidateAll();
        CraftManager.getInstance().getAutoCraftGuideGenerators().forEach(generator -> {
            generator.onCache(manager);
        });
    }

    public static class CachedIngredient {
        public ItemStack[] possibleItems;
        public UUID cachedUUID;

        public CachedIngredient(ItemStack[] possibleItems, UUID cachedUUID) {
            this.possibleItems = possibleItems;
            this.cachedUUID = cachedUUID;
        }

        public boolean isEqualTo(Ingredient ingredient) {
            ItemStack[] items = ingredient.getItems();
            if (items.length != possibleItems.length)
                return false;
            for (int i = 0; i < items.length; i++) {
                if (!ItemStackUtil.isSameInCrafting(items[i], possibleItems[i])) {
                    return false;
                }
            }
            return true;
        }
    }
}
