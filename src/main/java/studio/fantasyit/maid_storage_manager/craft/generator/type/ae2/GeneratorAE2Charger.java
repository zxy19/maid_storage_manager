package studio.fantasyit.maid_storage_manager.craft.generator.type.ae2;

import appeng.core.definitions.AEBlocks;
import appeng.recipes.handlers.ChargerRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
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
import java.util.List;
import java.util.Map;

public class GeneratorAE2Charger implements IAutoCraftGuideGenerator {
    @Override
    public @NotNull ResourceLocation getType() {
        return new ResourceLocation("ae2", "charger");
    }

    @Override
    public boolean isBlockValid(Level level, BlockPos pos) {
        return level.getBlockState(pos).is(AEBlocks.CHARGER.block());
    }

    @Override
    public void generate(List<InventoryItem> inventory, Level level, BlockPos pos, ICachableGeneratorGraph graph, Map<ResourceLocation, List<BlockPos>> recognizedTypePositions) {
        StorageAccessUtil.Filter posFilter = GenerateCondition.getFilterOn(level, pos);
        level.getRecipeManager()
                .getAllRecipesFor(ChargerRecipe.TYPE)
                .forEach(recipe -> {
                    if (!posFilter.isAvailable(recipe.getResultItem()))
                        return;
                    graph.addRecipe(
                            recipe,
                            (List<ItemStack> items) -> {
                                List<CraftGuideStepData> steps = new ArrayList<>();
                                steps.add(new CraftGuideStepData(
                                        new Target(ItemHandlerStorage.TYPE, pos),
                                        items,
                                        List.of(),
                                        CommonPlaceItemAction.TYPE
                                ));
                                steps.add(new CraftGuideStepData(
                                        new Target(ItemHandlerStorage.TYPE, pos),
                                        List.of(),
                                        List.of(recipe.getResultItem()),
                                        CommonTakeItemAction.TYPE
                                ));
                                return new CraftGuideData(
                                        steps,
                                        CommonType.TYPE
                                );
                            }
                    );
                });
    }

    @Override
    public void onCache(RecipeManager manager) {
        manager.getAllRecipesFor(ChargerRecipe.TYPE)
                .forEach(RecipeIngredientCache::addRecipeCache);
    }

    @Override
    public Component getConfigName() {
        return Component.translatable("config.maid_storage_manager.crafting.generating.ae2.charger");
    }
}
