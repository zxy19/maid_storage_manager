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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
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
import studio.fantasyit.maid_storage_manager.craft.type.CommonType;
import studio.fantasyit.maid_storage_manager.data.InventoryItem;
import studio.fantasyit.maid_storage_manager.storage.ItemHandler.ItemHandlerStorage;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.util.StorageAccessUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class GeneratorAE2Inscriber implements IAutoCraftGuideGenerator {
    @Override
    public @NotNull ResourceLocation getType() {
        return ResourceLocation.fromNamespaceAndPath("ae2", "inscriber");
    }

    @Override
    public boolean isBlockValid(Level level, BlockPos pos) {
        if (!level.getBlockState(pos).is(AEBlocks.INSCRIBER.block())) return false;
        if (level.getBlockEntity(pos) instanceof InscriberBlockEntity inscriber) {
            return inscriber.getConfigManager().getSetting(Settings.AUTO_EXPORT) != YesNo.YES;
        }
        return false;
    }

    public ResourceLocation transformRecipeId(ResourceLocation recipeId, boolean skipFirst) {
        if (skipFirst)
            return ResourceLocation.fromNamespaceAndPath(recipeId.getNamespace(), recipeId.getPath() + "_skipped_first");
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
    public void generate(List<InventoryItem> inventory, Level level, BlockPos pos, ICachableGeneratorGraph graph, Map<ResourceLocation, List<BlockPos>> recognizedTypePositions) {
        if (level.getBlockEntity(pos) instanceof InscriberBlockEntity inscriber) {
            StorageAccessUtil.Filter posFilter = GenerateCondition.getFilterOn(level, pos);
            ItemStack topItem = inscriber.getInternalInventory().getStackInSlot(0);
            level.getRecipeManager()
                    .getAllRecipesFor(AERecipeTypes.INSCRIBER)
                    .forEach(holder -> {
                        InscriberRecipe recipe = holder.value();
                        if (!posFilter.isAvailable(recipe.getResultItem()))
                            return;
                        //复制配方，全都不处理。
                        if (!recipe.getTopOptional().isEmpty() && recipe.getTopOptional().test(recipe.getResultItem())) {
                            return;
                        }
                        boolean available = true;
                        boolean hasPriority = false;
                        boolean skipFirst;
                        if (!topItem.isEmpty()) {
                            if (recipe.getTopOptional().isEmpty()) {
                                available = false;
                            } else if (!recipe.getTopOptional().test(topItem)) {
                                available = false;
                            }
                            skipFirst = true;
                        } else if (recipe.getTopOptional().isEmpty()) {
                            skipFirst = true;
                        } else if (recipe.getProcessType() == InscriberProcessType.INSCRIBE) {
                            //压印类型，如果不分离侧面，则要求必须顶部存在物品，否则放入的物品无法取出
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
                                ingredients.add(recipe.getTopOptional());
                            ingredients.add(recipe.getMiddleInput());
                            if (!recipe.getBottomOptional().isEmpty())
                                ingredients.add(recipe.getBottomOptional());

                            ItemStack result = recipe.getResultItem();
                            if (hasPriority)
                                graph.blockRecipe(transformRecipeId(holder.id(), false));
                            graph.addRecipe(
                                    transformRecipeId(holder.id(), skipFirst),
                                    ingredients,
                                    ingredients
                                            .stream()
                                            .map(Ingredient::getItems)
                                            .map(items -> Arrays.stream(items).findFirst().map(ItemStack::getCount).orElse(0))
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
                                                    CommonPlaceItemAction.TYPE,
                                                    false,
                                                    new CompoundTag()
                                            ));
                                        steps.add(new CraftGuideStepData(
                                                new Target(ItemHandlerStorage.TYPE, pos, Direction.WEST),
                                                List.of(items.get(id++)),
                                                List.of(),
                                                CommonPlaceItemAction.TYPE,
                                                false,
                                                new CompoundTag()
                                        ));
                                        if (id < items.size())
                                            steps.add(new CraftGuideStepData(
                                                    new Target(ItemHandlerStorage.TYPE, pos, Direction.DOWN),
                                                    List.of(items.get(id++)),
                                                    List.of(),
                                                    CommonPlaceItemAction.TYPE,
                                                    false,
                                                    new CompoundTag()
                                            ));
                                        steps.add(new CraftGuideStepData(
                                                new Target(ItemHandlerStorage.TYPE, pos),
                                                List.of(),
                                                List.of(result),
                                                CommonTakeItemAction.TYPE,
                                                false,
                                                new CompoundTag()
                                        ));
                                        //如果输入物品保留第一个而且需要取回
                                        if (!skipFirst && recipe.getProcessType() == InscriberProcessType.INSCRIBE)
                                            steps.add(new CraftGuideStepData(
                                                    new Target(ItemHandlerStorage.TYPE, pos, Direction.UP),
                                                    List.of(),
                                                    List.of(items.get(0)),
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
                        }
                    });

        }
    }

    @Override
    public void onCache(RecipeManager manager) {
        manager.getAllRecipesFor(AERecipeTypes.INSCRIBER)
                .forEach(holder -> {
                    InscriberRecipe recipe = holder.value();
                    if (recipe.getProcessType() == InscriberProcessType.INSCRIBE && !recipe.getTopOptional().isEmpty()) {
                        List<Ingredient> ingredients = new ArrayList<>();
                        ingredients.add(recipe.getMiddleInput());
                        if (!recipe.getBottomOptional().isEmpty())
                            ingredients.add(recipe.getBottomOptional());
                        RecipeIngredientCache.addRecipeCache(
                                transformRecipeId(holder.id(), true),
                                ingredients
                        );
                    }
                    List<Ingredient> ingredientsFull = new ArrayList<>();
                    if (!recipe.getTopOptional().isEmpty())
                        ingredientsFull.add(recipe.getTopOptional());
                    ingredientsFull.add(recipe.getMiddleInput());
                    if (!recipe.getBottomOptional().isEmpty())
                        ingredientsFull.add(recipe.getBottomOptional());
                    RecipeIngredientCache.addRecipeCache(
                            transformRecipeId(holder.id(), false),
                            ingredientsFull
                    );
                });
    }

    @Override
    public Component getConfigName() {
        return Component.translatable("config.maid_storage_manager.crafting.generating.ae2.inscriber");
    }
}
