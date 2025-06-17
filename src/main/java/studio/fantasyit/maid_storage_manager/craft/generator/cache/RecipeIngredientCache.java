package studio.fantasyit.maid_storage_manager.craft.generator.cache;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import studio.fantasyit.maid_storage_manager.craft.CraftManager;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.generator.algo.GeneratorGraph;
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

    public static boolean isCached(Recipe<?> recipe) {
        return CACHE.containsKey(recipe.getId());
    }

    public static boolean addCahcedRecipeToGraph(GeneratorGraph graph, Recipe<?> recipe, Function<List<ItemStack>, CraftGuideData> craftGuideSupplier) {
        if (CACHE.containsKey(recipe.getId())) {
            List<GeneratorGraph.IngredientNode> ingredientNodes = new ArrayList<>();
            NonNullList<Ingredient> ingredients = recipe.getIngredients();
            for (int i = 0; i < ingredients.size(); i++) {
                Ingredient ingredient = ingredients.get(i);
                UUID uuid = CACHE.get(recipe.getId()).get(i);
                GeneratorGraph.IngredientNode ingredientNode = graph.addOrGetCahcedIngredientNode(ingredient, uuid);
                ingredientNodes.add(ingredientNode);
            }
            graph.addRecipeWithIngredients(recipe, ingredientNodes, craftGuideSupplier);
            return true;
        }
        return false;
    }

    public static void addRecipeCache(Recipe<?> recipe) {
        ResourceLocation id = recipe.getId();
        List<UUID> cachedIngredientNodeUUID = new ArrayList<>();
        for (Ingredient ingredient : recipe.getIngredients()) {
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

    public static int getUncachedRecipeIngredient(Recipe<?> recipe, GeneratorGraph generatorGraph) {
        if (!isCached(recipe)) return recipe.getIngredients().size();
        int c = 0;
        for (UUID ingredient : CACHE.get(recipe.getId())) {
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
