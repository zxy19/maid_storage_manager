package studio.fantasyit.maid_storage_manager.craft.generator.type.create;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.AllTags;
import com.simibubi.create.content.equipment.sandPaper.SandPaperPolishingRecipe;
import com.simibubi.create.content.kinetics.deployer.ItemApplicationRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.craft.context.common.CommonPlaceItemAction;
import studio.fantasyit.maid_storage_manager.craft.context.common.CommonTakeItemAction;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.generator.algo.ICachableGeneratorGraph;
import studio.fantasyit.maid_storage_manager.craft.generator.cache.RecipeIngredientCache;
import studio.fantasyit.maid_storage_manager.craft.generator.type.base.IAutoCraftGuideGenerator;
import studio.fantasyit.maid_storage_manager.craft.generator.util.GenerateCondition;
import studio.fantasyit.maid_storage_manager.craft.type.CommonType;
import studio.fantasyit.maid_storage_manager.data.InventoryItem;
import studio.fantasyit.maid_storage_manager.storage.ItemHandler.ItemHandlerStorage;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.util.StorageAccessUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class GeneratorCreateDeployer implements IAutoCraftGuideGenerator {
    @Override
    public @NotNull ResourceLocation getType() {
        return AllRecipeTypes.DEPLOYING.getId();
    }

    @Override
    public boolean isBlockValid(Level level, BlockPos pos) {
        if (level.getBlockState(pos).is(AllBlocks.DEPOT.get()) && level.getBlockState(pos.above().above()).is(AllBlocks.DEPLOYER.get())) {
            return true;
        }
        return false;
    }

    @Override
    public void generate(List<InventoryItem> inventory, Level level, BlockPos pos, ICachableGeneratorGraph graph, Map<ResourceLocation, List<BlockPos>> recognizedTypePositions) {
        StorageAccessUtil.Filter posFilter = GenerateCondition.getFilterOn(level, pos);
        level.getRecipeManager()
                .getAllRecipesFor(AllRecipeTypes.DEPLOYING.getType())
                .forEach((holder) -> {
                    Recipe<RecipeInput> recipe = holder.value();
                    if (!posFilter.isAvailable(recipe.getResultItem(level.registryAccess())))
                        return;
                    graph.addRecipe(holder, this.getCraftGuideSupplier(graph, recipe, level, pos));
                });
        level.getRecipeManager()
                .getAllRecipesFor(AllRecipeTypes.ITEM_APPLICATION.getType())
                .forEach((holder) -> {
                    Recipe<RecipeInput> recipe = holder.value();
                    if (!posFilter.isAvailable(recipe.getResultItem(level.registryAccess())))
                        return;
                    graph.addRecipe(holder, this.getCraftGuideSupplier(graph, recipe, level, pos));
                });
        level.getRecipeManager()
                .getAllRecipesFor(AllRecipeTypes.SANDPAPER_POLISHING.getType())
                .forEach((holder) -> {
                    Recipe<RecipeInput> recipe = holder.value();
                    if (!posFilter.isAvailable(recipe.getResultItem(level.registryAccess())))
                        return;
                    List<Ingredient> ingredients = new ArrayList<>(recipe.getIngredients());
                    ingredients.add(Ingredient.of(AllTags.AllItemTags.SANDPAPER.tag));
                    graph.addRecipe(holder.id(),
                            ingredients,
                            List.of(8, 1),
                            recipe.getResultItem(level.registryAccess()),
                            this.getCraftGuideSupplier(graph, recipe, level, pos));
                });

    }

    protected Function<List<ItemStack>, @Nullable CraftGuideData> getCraftGuideSupplier(ICachableGeneratorGraph graph, Recipe<?> recipe, Level level, BlockPos pos) {
        ItemStack resultItem = recipe.getResultItem(level.registryAccess());
        return ((outputs) -> {
            List<CraftGuideStepData> craftGuideData = new ArrayList<>();
            craftGuideData.add(new CraftGuideStepData(
                    new Target(ItemHandlerStorage.TYPE, pos.above().above()),
                    List.of(outputs.get(1)),
                    List.of(),
                    CommonPlaceItemAction.TYPE,
                    false,
                    new CompoundTag()
            ));
            craftGuideData.add(new CraftGuideStepData(
                    new Target(ItemHandlerStorage.TYPE, pos),
                    List.of(outputs.get(0)),
                    List.of(),
                    CommonPlaceItemAction.TYPE,
                    false,
                    new CompoundTag()
            ));

            craftGuideData.add(new CraftGuideStepData(
                    new Target(ItemHandlerStorage.TYPE, pos),
                    List.of(),
                    List.of(resultItem.copyWithCount(outputs.get(0).getCount())),
                    CommonTakeItemAction.TYPE,
                    false,
                    new CompoundTag()
            ));
            if (recipe instanceof ItemApplicationRecipe iar && iar.shouldKeepHeldItem())
                craftGuideData.add(new CraftGuideStepData(
                        new Target(ItemHandlerStorage.TYPE, pos.above().above()),
                        List.of(),
                        List.of(outputs.get(1)),
                        CommonTakeItemAction.TYPE,
                        false,
                        new CompoundTag()
                ));
            if (recipe instanceof SandPaperPolishingRecipe)
                craftGuideData.add(new CraftGuideStepData(
                        new Target(ItemHandlerStorage.TYPE, pos.above().above()),
                        List.of(),
                        List.of(outputs.get(1)),
                        CommonTakeItemAction.TYPE,
                        true,
                        new CompoundTag()
                ));
            return new CraftGuideData(
                    craftGuideData,
                    CommonType.TYPE
            );
        });
    }


    @Override
    public void onCache(RecipeManager manager) {
        manager.getAllRecipesFor(AllRecipeTypes.DEPLOYING.getType())
                .forEach(RecipeIngredientCache::addRecipeCache);
    }

    @Override
    public Component getConfigName() {
        return Component.translatable("config.maid_storage_manager.crafting.generating.create.deploying");
    }
}