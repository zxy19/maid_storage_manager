package studio.fantasyit.maid_storage_manager.craft.generator.cache;

import java.util.UUID;
import java.util.stream.Stream;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RecipeIngredientCacheTest {
    private static class CountingIngredient extends Ingredient {
        int calls;

        CountingIngredient() { super(Stream.empty()); }

        @Override
        public ItemStack[] getItems() {
            calls++;
            return new ItemStack[0];
        }
    }

    @AfterEach
    void clearCache() { RecipeIngredientCache.invalidateAll(); }

    @Test
    void resolvesOnceEvenOnMissAfterManyNodes() {
        RecipeIngredientCache.invalidateAll();
        // Length mismatches isolate candidate expansion from registry/NBT setup.
        for (int i = 0; i < 1000; i++) {
            RecipeIngredientCache.cachedNode.add(
                    new RecipeIngredientCache.CachedIngredient(new ItemStack[1], UUID.randomUUID()));
        }
        CountingIngredient ingredient = new CountingIngredient();
        UUID first = RecipeIngredientCache.cacheIngredient(ingredient);
        assertEquals(1, ingredient.calls);
        assertEquals(1001, RecipeIngredientCache.cachedNode.size());
        assertEquals(first, RecipeIngredientCache.cacheIngredient(ingredient));
        assertEquals(2, ingredient.calls);
        assertEquals(1001, RecipeIngredientCache.cachedNode.size());
    }

    @Test
    void doesNotKeepCandidatesAcrossCallsOrInvalidation() {
        CountingIngredient ingredient = new CountingIngredient();
        UUID first = RecipeIngredientCache.cacheIngredient(ingredient);
        RecipeIngredientCache.invalidateAll();
        UUID second = RecipeIngredientCache.cacheIngredient(ingredient);
        assertEquals(2, ingredient.calls);
        assertNotEquals(first, second);
    }

    @Test
    void existingIngredientComparisonEntryPointStillWorks() {
        CountingIngredient ingredient = new CountingIngredient();
        var cached = new RecipeIngredientCache.CachedIngredient(new ItemStack[0], UUID.randomUUID());
        assertTrue(cached.isEqualTo(ingredient));
        assertEquals(1, ingredient.calls);
    }
}
