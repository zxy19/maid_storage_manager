package studio.fantasyit.maid_storage_manager.craft.generator.type.base;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.generator.algo.ICachableGeneratorGraph;
import studio.fantasyit.maid_storage_manager.craft.generator.cache.RecipeIngredientCache;
import studio.fantasyit.maid_storage_manager.craft.generator.util.GenerateCondition;
import studio.fantasyit.maid_storage_manager.craft.generator.util.GenerateIngredientUtil;
//import studio.fantasyit.maid_storage_manager.craft.type.CraftingType;
import studio.fantasyit.maid_storage_manager.data.InventoryItem;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.util.StorageAccessUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class SimpleGenerator<T extends Recipe<C>, C extends RecipeInput> implements IAutoCraftGuideGenerator {
    protected abstract RecipeType<T> getRecipeType();

    protected abstract Identifier getCraftType();

    abstract protected C getWrappedContainer(T recipe, List<ItemStack> inputs);

    protected List<ItemStack> wrapInputs(T recipe, List<ItemStack> inputs) {
        return inputs;
    }

    protected List<ItemStack> wrapOutputs(T recipe, List<ItemStack> inputs, C container, List<ItemStack> outputs) {
        if (recipe instanceof CraftingRecipe cr) {
            cr.getRemainingItems((CraftingInput) container)
                    .stream()
                    .filter(i -> !i.isEmpty())
                    .forEach(outputs::add);
        }
        return outputs;
    }

    protected List<Ingredient> ingredientsTransform(List<InventoryItem> inventory, Level level, T recipe) {
        return cacheIngredientsTransform(recipe);
    }

    protected List<Ingredient> cacheIngredientsTransform(T recipe) {
        return recipe.placementInfo().ingredients();
    }

    protected List<Integer> ingredientCountsTransform(List<InventoryItem> inventory, Level level, T recipe, List<Ingredient> ingredient) {
        return ingredient
                .stream()
                .map(GenerateIngredientUtil::getIngredientCount)
                .toList();
    }

    protected ItemStack outputTransform(List<InventoryItem> inventory, Level level, T recipe) {
        return recipe.placementInfo().ingredients().isEmpty()
                ? ItemStack.EMPTY
                : ItemStack.EMPTY;
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
    public @NotNull Identifier getType() {
        return getCraftType();
    }

    @Override
    public void generate(List<InventoryItem> inventory, Level level, BlockPos pos, ICachableGeneratorGraph graph, Map<Identifier, List<BlockPos>> recognizedTypePositions) {
        StorageAccessUtil.Filter posFilter = GenerateCondition.getFilterOn(level, pos);
        RecipeManager recipeManager = (RecipeManager) level.recipeAccess();
        recipeManager.recipeMap()
                .byType(getRecipeType())
                .forEach((RecipeHolder<T> holder) -> {
                    T recipe = holder.value();
                    if (!isValid(inventory, level, pos, recipe))
                        return;
                    List<Ingredient> ingredients = ingredientsTransform(inventory, level, recipe);
                    ItemStack output = outputTransform(inventory, level, recipe);
                    if (!posFilter.isAvailable(output))
                        return;
                    List<Integer> ingredientCounts = ingredientCountsTransform(inventory, level, recipe, ingredients);
                    graph.addRecipe(holder.id().identifier(), ingredients, ingredientCounts, output, (items) -> {
                        C container = getWrappedContainer(recipe, items);
                        List<ItemStack> result = new ArrayList<>(List.of(recipe.assemble(container)));
                        CraftGuideStepData step = new CraftGuideStepData(
                                new Target(Identifier.fromNamespaceAndPath("maid_storage_manager", "disabled"), pos), // CraftingType disabled
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
        manager.recipeMap().byType(getRecipeType()).forEach(holder -> {
            T recipe = holder.value();
            if (isValid(recipe) && shouldCache(recipe)) {
                List<Ingredient> ingredients = cacheIngredientsTransform(recipe);
                RecipeIngredientCache.addRecipeCache(holder.id().identifier(), ingredients);
            }
        });
    }
}
