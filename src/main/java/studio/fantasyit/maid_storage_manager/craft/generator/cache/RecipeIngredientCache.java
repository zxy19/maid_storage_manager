package studio.fantasyit.maid_storage_manager.craft.generator.cache;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import studio.fantasyit.maid_storage_manager.craft.CraftManager;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.generator.algo.ICachableGeneratorGraph;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

public class RecipeIngredientCache {
    public static final ConcurrentHashMap<ResourceLocation, List<UUID>> CACHE = new ConcurrentHashMap<>();
    public static final List<CachedIngredient> cachedNode = new ArrayList<>();
    private static final ReentrantReadWriteLock LOCK = new ReentrantReadWriteLock();

    public static void invalidateAll() {
        LOCK.writeLock().lock();
        CACHE.clear();
        cachedNode.clear();
        LOCK.writeLock().unlock();
    }

    public static boolean isCached(ResourceLocation recipeId) {
        return false;
    }

    public static boolean addCahcedRecipeToGraph(ICachableGeneratorGraph graph,
                                                 ResourceLocation id,
                                                 List<Ingredient> ingredients,
                                                 List<Integer> ingredientCounts,
                                                 List<ItemStack> output,
                                                 Function<List<ItemStack>, CraftGuideData> craftGuideSupplier, ResourceLocation type, boolean isOneTime) {
        return false;
    }

    public static void addRecipeCache(RecipeHolder<? extends Recipe<?>> holder) {
        RecipeIngredientCache.addRecipeCache(holder.id(), holder.value().getIngredients());
    }

    public static void addRecipeCache(ResourceLocation id, List<Ingredient> ingredients) {
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

    public static UUID cacheIngredient(Ingredient ingredient) {
        return UUID.randomUUID();
    }

    public static class CachedIngredient {
        public UUID cachedUUID;

        public CachedIngredient(ItemStack[] possibleItems, UUID cachedUUID) {
            this.cachedUUID = cachedUUID;
        }
    }
}
