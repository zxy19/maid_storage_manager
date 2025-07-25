package studio.fantasyit.maid_storage_manager.craft.generator.type.ars;


import com.hollingsworth.arsnouveau.common.block.tile.EnchantingApparatusTile;
import com.hollingsworth.arsnouveau.common.crafting.recipes.EnchantingApparatusRecipe;
import com.hollingsworth.arsnouveau.setup.registry.BlockRegistry;
import com.hollingsworth.arsnouveau.setup.registry.RecipeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;
import studio.fantasyit.maid_storage_manager.craft.context.common.CommonPlaceItemAction;
import studio.fantasyit.maid_storage_manager.craft.context.common.CommonTakeItemAction;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.generator.algo.ICachableGeneratorGraph;
import studio.fantasyit.maid_storage_manager.craft.generator.cache.RecipeIngredientCache;
import studio.fantasyit.maid_storage_manager.craft.generator.type.base.IAutoCraftGuideGenerator;
import studio.fantasyit.maid_storage_manager.craft.type.CommonType;
import studio.fantasyit.maid_storage_manager.data.InventoryItem;
import studio.fantasyit.maid_storage_manager.storage.ItemHandler.ItemHandlerStorage;
import studio.fantasyit.maid_storage_manager.storage.Target;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class GeneratorArsNouveauEnchantApp<T extends EnchantingApparatusRecipe> implements IAutoCraftGuideGenerator {
    protected abstract DeferredHolder<RecipeType<?>, RecipeRegistry.ModRecipeType<T>> getRecipeType();

    @Override
    public @NotNull ResourceLocation getType() {
        return Objects.requireNonNull(getRecipeType().getId());
    }

    @Override
    public boolean isBlockValid(Level level, BlockPos pos) {
        return level.getBlockState(pos).is(BlockRegistry.ENCHANTING_APP_BLOCK.get()) && level.getBlockState(pos.below()).is(BlockRegistry.ARCANE_CORE_BLOCK.get());
    }

    @Override
    public void generate(List<InventoryItem> inventory, Level level, BlockPos pos, ICachableGeneratorGraph graph, Map<ResourceLocation, List<BlockPos>> recognizedTypePositions) {
        if (level.getBlockEntity(pos) instanceof EnchantingApparatusTile tile) {
            List<BlockPos> blockPos = tile.pedestalList();
            level.getRecipeManager()
                    .getAllRecipesFor(getRecipeType().get())
                    .forEach(holder -> {
                        T recipe = holder.value();
                        ArrayList<Ingredient> ingredients = new ArrayList<>(recipe.pedestalItems());
                        ingredients.add(recipe.reagent());
                        if (recipe.pedestalItems().size() <= blockPos.size())
                            graph.addRecipe(
                                    holder.id(),
                                    ingredients,
                                    ingredients.stream().map(t -> 1).toList(),
                                    recipe.result(),
                                    items -> {
                                        List<CraftGuideStepData> steps = new ArrayList<>();
                                        for (int i = 0; i < items.size() - 1; i++) {
                                            steps.add(new CraftGuideStepData(
                                                    new Target(ItemHandlerStorage.TYPE, blockPos.get(i)),
                                                    List.of(items.get(i)),
                                                    List.of(),
                                                    CommonPlaceItemAction.TYPE,
                                                    false,
                                                    new CompoundTag()
                                            ));
                                        }
                                        steps.add(new CraftGuideStepData(
                                                new Target(ItemHandlerStorage.TYPE, pos),
                                                List.of(items.get(items.size() - 1)),
                                                List.of(),
                                                CommonPlaceItemAction.TYPE,
                                                false,
                                                new CompoundTag()
                                        ));
                                        steps.add(new CraftGuideStepData(
                                                new Target(ItemHandlerStorage.TYPE, pos),
                                                List.of(),
                                                List.of(recipe.result()),
                                                CommonTakeItemAction.TYPE,
                                                false,
                                                new CompoundTag()
                                        ));
                                        return new CraftGuideData(
                                                steps,
                                                CommonType.TYPE
                                        );
                                    }
                            );
                    });
        }
    }

    @Override
    public void onCache(RecipeManager manager) {
        manager.getAllRecipesFor(getRecipeType().get())
                .forEach(t -> {
                    T recipe = t.value();
                    ArrayList<Ingredient> ingredients = new ArrayList<>(recipe.pedestalItems());
                    ingredients.add(recipe.reagent());
                    RecipeIngredientCache.addRecipeCache(t.id(), ingredients);
                });
    }
}
