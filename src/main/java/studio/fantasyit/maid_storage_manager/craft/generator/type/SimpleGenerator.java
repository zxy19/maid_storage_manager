package studio.fantasyit.maid_storage_manager.craft.generator.type;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.generator.IAutoCraftGuideGenerator;
import studio.fantasyit.maid_storage_manager.craft.generator.algo.GeneratorGraph;
import studio.fantasyit.maid_storage_manager.craft.generator.cache.RecipeIngredientCache;
import studio.fantasyit.maid_storage_manager.craft.type.CraftingType;
import studio.fantasyit.maid_storage_manager.data.InventoryItem;
import studio.fantasyit.maid_storage_manager.storage.Target;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public abstract class SimpleGenerator<T extends Recipe<C>, C extends Container> implements IAutoCraftGuideGenerator {
    protected abstract RecipeType<T> getRecipeType();

    protected abstract ResourceLocation getCraftType();

    abstract protected Optional<T> validateAndGetRealRecipe(Level level, T recipe, List<ItemStack> inputs, C container);

    abstract protected C getWrappedContainer(Level level, T recipe, List<ItemStack> inputs);

    protected List<ItemStack> wrapInputs(Level level, T recipe, List<ItemStack> inputs) {
        return inputs;
    }

    protected List<ItemStack> wrapOutputs(Level level, T recipe, List<ItemStack> inputs, C container, List<ItemStack> outputs) {
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
    public ResourceLocation getType() {
        return getCraftType();
    }

    @Override
    public void generate(List<InventoryItem> inventory, Level level, BlockPos pos, GeneratorGraph graph) {
        level.getRecipeManager()
                .getAllRecipesFor(getRecipeType())
                .forEach((T recipe) -> {
                    if (!isValid(inventory, level, pos, recipe))
                        return;
                    List<Ingredient> ingredients = ingredientsTransform(inventory, level, recipe);
                    ItemStack output = outputTransform(inventory, level, recipe);
                    List<Integer> ingredientCounts = ingredientCountsTransform(inventory, level, recipe, ingredients);
                    graph.addRecipe(recipe.getId(), ingredients, ingredientCounts, output, (items) -> {
                        C container = getWrappedContainer(level, recipe, items);
                        Optional<T> realRecipe = validateAndGetRealRecipe(level, recipe, items, container);
                        if (realRecipe.isEmpty() || !realRecipe.get().getId().equals(recipe.getId())) {
                            return null;
                        }
                        T validRecipe = realRecipe.get();
                        List<ItemStack> result = new ArrayList<>(List.of(validRecipe.getResultItem(level.registryAccess())));
                        CraftGuideStepData step = new CraftGuideStepData(
                                new Target(CraftingType.TYPE, pos),
                                wrapInputs(level, recipe, items),
                                wrapOutputs(level, validRecipe, items, container, result),
                                getCraftType(),
                                false,
                                new CompoundTag()
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
