package studio.fantasyit.maid_storage_manager.craft.generator.type.ae2;

import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.blockentity.misc.InscriberBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.recipes.AERecipeTypes;
import appeng.recipes.handlers.InscriberProcessType;
import appeng.recipes.handlers.InscriberRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import studio.fantasyit.maid_storage_manager.craft.context.common.CommonPlaceItemAction;
import studio.fantasyit.maid_storage_manager.craft.context.common.CommonTakeItemAction;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.generator.algo.ICachableGeneratorGraph;
import studio.fantasyit.maid_storage_manager.craft.generator.cache.RecipeIngredientCache;
import studio.fantasyit.maid_storage_manager.craft.generator.type.base.IAutoCraftGuideGenerator;
import studio.fantasyit.maid_storage_manager.craft.generator.util.GenerateCondition;
import studio.fantasyit.maid_storage_manager.craft.generator.util.GenerateIngredientUtil;
import studio.fantasyit.maid_storage_manager.craft.type.CommonType;
import studio.fantasyit.maid_storage_manager.data.InventoryItem;
import studio.fantasyit.maid_storage_manager.storage.ItemHandler.ItemHandlerStorage;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.util.StorageAccessUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GeneratorAE2Inscriber implements IAutoCraftGuideGenerator {
    @Override
    public @NotNull Identifier getType() {
        return Identifier.fromNamespaceAndPath("ae2", "inscriber");
    }

    @Override
    public boolean isBlockValid(Level level, BlockPos pos) {
        if (!level.getBlockState(pos).is(AEBlocks.INSCRIBER.block())) return false;
        if (level.getBlockEntity(pos) instanceof InscriberBlockEntity inscriber) {
            return inscriber.getConfigManager().getSetting(Settings.AUTO_EXPORT) != YesNo.YES;
        }
        return false;
    }

    public Identifier transformRecipeId(Identifier recipeId, boolean skipFirst) {
        if (skipFirst)
            return Identifier.fromNamespaceAndPath(recipeId.getNamespace(), recipeId.getPath() + "_skipped_first");
        return recipeId;
    }

    @Override
    public boolean allowMultiPosition() {
        return true;
    }

    @Override
    public boolean canCacheGraph() {
        return false;
    }

    @Override
    public void generate(List<InventoryItem> inventory, Level level, BlockPos pos, ICachableGeneratorGraph graph, Map<Identifier, List<BlockPos>> recognizedTypePositions) {
        if (level.getBlockEntity(pos) instanceof InscriberBlockEntity inscriber) {
            StorageAccessUtil.Filter posFilter = GenerateCondition.getFilterOn(level, pos);
            ItemStack topItem = inscriber.getInternalInventory().getStackInSlot(0);
            RecipeManager recipeManager = (RecipeManager) level.recipeAccess();
            recipeManager.recipeMap()
                    .byType(AERecipeTypes.INSCRIBER)
                    .forEach((RecipeHolder<InscriberRecipe> holder) -> {
                        InscriberRecipe recipe = holder.value();
                        if (!posFilter.isAvailable(recipe.result().create()))
                            return;
                        Optional<Ingredient> topOpt = recipe.getTopOptional();
                        if (topOpt.isPresent() && topOpt.get().test(recipe.result().create())) {
                            return;
                        }
                        boolean available = true;
                        boolean hasPriority = false;
                        boolean skipFirst;
                        if (!topItem.isEmpty()) {
                            if (topOpt.isEmpty()) {
                                available = false;
                            } else if (!topOpt.get().test(topItem)) {
                                available = false;
                            }
                            hasPriority = true;
                            skipFirst = true;
                        } else if (topOpt.isEmpty()) {
                            skipFirst = true;
                        } else if (recipe.getProcessType() == InscriberProcessType.INSCRIBE) {
                            if (inscriber.getConfigManager().getSetting(Settings.INSCRIBER_SEPARATE_SIDES) == YesNo.NO) {
                                skipFirst = false;
                                available = false;
                            } else {
                                skipFirst = false;
                            }
                        } else {
                            skipFirst = false;
                        }
                        if (available) {
                            List<Ingredient> ingredients = new ArrayList<>();
                            if (!skipFirst)
                                topOpt.ifPresent(ingredients::add);
                            ingredients.add(recipe.getMiddleInput());
                            recipe.getBottomOptional().ifPresent(ingredients::add);

                            ItemStack result = recipe.result().create();
                            if (hasPriority)
                                graph.blockRecipe(transformRecipeId(holder.id().identifier(), false));
                            graph.addRecipe(
                                    transformRecipeId(holder.id().identifier(), skipFirst),
                                    ingredients,
                                    ingredients
                                            .stream()
                                            .map(GenerateIngredientUtil::getIngredientCount)
                                            .toList(),
                                    result,
                                    (List<ItemStack> items) -> {
                                        List<CraftGuideStepData> steps = new ArrayList<>();
                                        int id = 0;
                                        if (!skipFirst)
                                            steps.add(new CraftGuideStepData(
                                                    new Target(ItemHandlerStorage.TYPE, pos, Direction.UP),
                                                    List.of(items.get(id++)),
                                                    List.of(),
                                                    CommonPlaceItemAction.TYPE
                                            ));
                                        steps.add(new CraftGuideStepData(
                                                new Target(ItemHandlerStorage.TYPE, pos, Direction.WEST),
                                                List.of(items.get(id++)),
                                                List.of(),
                                                CommonPlaceItemAction.TYPE
                                        ));
                                        if (id < items.size())
                                            steps.add(new CraftGuideStepData(
                                                    new Target(ItemHandlerStorage.TYPE, pos, Direction.DOWN),
                                                    List.of(items.get(id++)),
                                                    List.of(),
                                                    CommonPlaceItemAction.TYPE
                                            ));
                                        steps.add(new CraftGuideStepData(
                                                new Target(ItemHandlerStorage.TYPE, pos),
                                                List.of(),
                                                List.of(result),
                                                CommonTakeItemAction.TYPE
                                        ));
                                        if (!skipFirst && recipe.getProcessType() == InscriberProcessType.INSCRIBE)
                                            steps.add(new CraftGuideStepData(
                                                    new Target(ItemHandlerStorage.TYPE, pos, Direction.UP),
                                                    List.of(),
                                                    List.of(items.get(0)),
                                                    CommonTakeItemAction.TYPE
                                            ));


                                        return new CraftGuideData(
                                                steps,
                                                CommonType.TYPE
                                        );
                                    }
                            );
                        }
                    });

        }
    }

    @Override
    public void onCache(RecipeManager manager) {
        manager.recipeMap().byType(AERecipeTypes.INSCRIBER)
                .forEach(holder -> {
                    InscriberRecipe recipe = holder.value();
                    if (recipe.getProcessType() == InscriberProcessType.INSCRIBE && recipe.getTopOptional().isPresent()) {
                        List<Ingredient> ingredients = new ArrayList<>();
                        ingredients.add(recipe.getMiddleInput());
                        recipe.getBottomOptional().ifPresent(ingredients::add);
                        RecipeIngredientCache.addRecipeCache(
                                transformRecipeId(holder.id().identifier(), true),
                                ingredients
                        );
                    }
                    List<Ingredient> ingredientsFull = new ArrayList<>();
                    recipe.getTopOptional().ifPresent(ingredientsFull::add);
                    ingredientsFull.add(recipe.getMiddleInput());
                    recipe.getBottomOptional().ifPresent(ingredientsFull::add);
                    RecipeIngredientCache.addRecipeCache(
                            transformRecipeId(holder.id().identifier(), false),
                            ingredientsFull
                    );
                });
    }

    @Override
    public Component getConfigName() {
        return Component.translatable("config.maid_storage_manager.crafting.generating.ae2.inscriber");
    }
}
