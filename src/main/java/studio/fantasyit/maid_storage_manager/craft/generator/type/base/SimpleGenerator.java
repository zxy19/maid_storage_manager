package studio.fantasyit.maid_storage_manager.craft.generator.type.base;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.generator.algo.ICachableGeneratorGraph;
import studio.fantasyit.maid_storage_manager.craft.generator.cache.RecipeIngredientCache;
import studio.fantasyit.maid_storage_manager.craft.generator.util.GenerateCondition;
import studio.fantasyit.maid_storage_manager.craft.type.CraftingType;
import studio.fantasyit.maid_storage_manager.data.InventoryItem;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.util.StorageAccessUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public abstract class SimpleGenerator<T extends Recipe<C>, C extends Container> implements IAutoCraftGuideGenerator {
    protected abstract RecipeType<T> getRecipeType();

    protected abstract ResourceLocation getCraftType();

    abstract protected C getWrappedContainer(T recipe, List<ItemStack> inputs);

    protected List<ItemStack> wrapInputs(T recipe, List<ItemStack> inputs) {
        return inputs;
    }

    protected List<ItemStack> wrapOutputs(T recipe, List<ItemStack> inputs, C container, List<ItemStack> outputs) {
        recipe
                .getRemainingItems(container)
                .stream()
                .filter(i -> !i.isEmpty())
                .forEach(outputs::add);
        return outputs;
    }

    protected List<Ingredient> ingredientsTransform(List<InventoryItem> inventory, Level level, T recipe) {
        return cacheIngredientsTransform(recipe);
    }

    protected List<Ingredient> cacheIngredientsTransform(T recipe) {
        return recipe.getIngredients();
    }

    protected List<Integer> ingredientCountsTransform(List<InventoryItem> inventory, Level level, T recipe, List<Ingredient> ingredient) {
        return ingredient
                .stream()
                .map(t -> Arrays.stream(t.getItems()).findFirst().map(ItemStack::getCount).orElse(1))
                .toList();
    }

    protected ItemStack outputTransform(List<InventoryItem> inventory, Level level, T recipe) {
        return recipe.getResultItem(level.registryAccess());
    }

    protected boolean isValid(List<InventoryItem> inventory, Level level, BlockPos pos, T recipe) {
        return isValid(recipe);
    }

    protected boolean isValid(T recipe) {
        return true;
    }

    protected boolean shouldCache(T recipe) {
        return true;
    }

    @Override
    public @NotNull ResourceLocation getType() {
        return getCraftType();
    }

    @Override
    public void generate(List<InventoryItem> inventory, Level level, BlockPos pos, ICachableGeneratorGraph graph, Map<ResourceLocation, List<BlockPos>> recognizedTypePositions) {
        StorageAccessUtil.Filter posFilter = GenerateCondition.getFilterOn(level, pos);
        level.getRecipeManager()
                .getAllRecipesFor(getRecipeType())
                .forEach((T recipe) -> {
                    if (!isValid(inventory, level, pos, recipe))
                        return;
                    List<Ingredient> ingredients = ingredientsTransform(inventory, level, recipe);
                    ItemStack output = outputTransform(inventory, level, recipe);
                    if (!posFilter.isAvailable(output))
                        return;
                    List<Integer> ingredientCounts = ingredientCountsTransform(inventory, level, recipe, ingredients);
                    List<ItemStack> resultItem = List.of(recipe.getResultItem(level.registryAccess()));
                    graph.addRecipe(recipe.getId(), ingredients, ingredientCounts, output, (items) -> {
                        C container = getWrappedContainer(recipe, items);
                        List<ItemStack> result = new ArrayList<>(resultItem);
                        CraftGuideStepData step = new CraftGuideStepData(
                                new Target(CraftingType.TYPE, pos),
                                wrapInputs(recipe, items),
                                wrapOutputs(recipe, items, container, result),
                                getCraftType()
                        );
                        return new CraftGuideData(
                                List.of(step),
                                getCraftType()
                        );
                    });
                });
    }

    @Override
    public void onCache(RecipeManager manager) {
        manager.getAllRecipesFor(getRecipeType()).forEach(recipe -> {
            if (isValid(recipe) && shouldCache(recipe)) {
                List<Ingredient> ingredients = cacheIngredientsTransform(recipe);
                RecipeIngredientCache.addRecipeCache(recipe.getId(), ingredients);
            }
        });
    }
}
