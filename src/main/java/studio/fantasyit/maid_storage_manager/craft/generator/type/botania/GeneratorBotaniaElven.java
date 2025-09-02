package studio.fantasyit.maid_storage_manager.craft.generator.type.botania;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import studio.fantasyit.maid_storage_manager.craft.context.common.CommonPickupItemAction;
import studio.fantasyit.maid_storage_manager.craft.context.common.CommonThrowItemAction;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.generator.algo.ICachableGeneratorGraph;
import studio.fantasyit.maid_storage_manager.craft.generator.cache.RecipeIngredientCache;
import studio.fantasyit.maid_storage_manager.craft.generator.type.base.IAutoCraftGuideGenerator;
import studio.fantasyit.maid_storage_manager.craft.generator.util.GenerateIngredientUtil;
import studio.fantasyit.maid_storage_manager.craft.type.CommonType;
import studio.fantasyit.maid_storage_manager.data.InventoryItem;
import studio.fantasyit.maid_storage_manager.storage.Target;
import vazkii.botania.api.recipe.ElvenTradeRecipe;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.crafting.BotaniaRecipeTypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class GeneratorBotaniaElven implements IAutoCraftGuideGenerator {
    @Override
    public @NotNull ResourceLocation getType() {
        return ElvenTradeRecipe.TYPE_ID;
    }

    @Override
    public boolean isBlockValid(Level level, BlockPos pos) {
        return level.getBlockState(pos).is(BotaniaBlocks.alfPortal);
    }

    @Override
    public void generate(List<InventoryItem> inventory, Level level, BlockPos pos, ICachableGeneratorGraph graph, Map<ResourceLocation, List<BlockPos>> recognizedTypePositions) {
        level.getRecipeManager()
                .getAllRecipesFor(BotaniaRecipeTypes.ELVEN_TRADE_TYPE)
                .forEach(recipe -> {
                    List<Integer> ingredientCounts = recipe.getIngredients()
                            .stream()
                            .map(t -> Arrays.stream(t.getItems()).findFirst().map(ItemStack::getCount).orElse(1))
                            .toList();
                    graph.addRecipe(recipe.getId(),
                            recipe.getIngredients(),
                            ingredientCounts,
                            recipe.getOutputs(),
                            items -> {
                                List<CraftGuideStepData> steps = new ArrayList<>();
                                GenerateIngredientUtil.each3items(items, t -> steps.add(new CraftGuideStepData(
                                        Target.virtual(pos, Direction.UP),
                                        t,
                                        List.of(),
                                        CommonThrowItemAction.TYPE
                                )));
                                GenerateIngredientUtil.each3items(recipe.getOutputs(), t -> steps.add(new CraftGuideStepData(
                                        Target.virtual(pos, Direction.UP),
                                        List.of(),
                                        recipe.getOutputs(),
                                        CommonPickupItemAction.TYPE
                                )));
                                return new CraftGuideData(steps, CommonType.TYPE);
                            });
                });
    }

    @Override
    public void onCache(RecipeManager manager) {
        manager.getAllRecipesFor(BotaniaRecipeTypes.ELVEN_TRADE_TYPE)
                .forEach(RecipeIngredientCache::addRecipeCache);
    }

    @Override
    public Component getConfigName() {
        return Component.translatable("config.maid_storage_manager.crafting.generating.botania.elven");
    }
}
