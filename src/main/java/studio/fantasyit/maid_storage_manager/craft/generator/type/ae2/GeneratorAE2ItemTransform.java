package studio.fantasyit.maid_storage_manager.craft.generator.type.ae2;

import appeng.recipes.AERecipeTypes;
import appeng.recipes.transform.TransformRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.NotNull;
import studio.fantasyit.maid_storage_manager.craft.context.common.CommonPickupItemAction;
import studio.fantasyit.maid_storage_manager.craft.context.common.CommonThrowItemAction;
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

public class GeneratorAE2ItemTransform implements IAutoCraftGuideGenerator {
    @Override
    public @NotNull ResourceLocation getType() {
        return ResourceLocation.fromNamespaceAndPath("ae2", "item_transform");
    }

    @Override
    public boolean isBlockValid(Level level, BlockPos pos) {
        return level.getBlockState(pos).is(Blocks.WATER);
    }

    @Override
    public void generate(List<InventoryItem> inventory, Level level, BlockPos pos, ICachableGeneratorGraph graph, Map<ResourceLocation, List<BlockPos>> recognizedTypePositions) {
        level.getRecipeManager()
                .getAllRecipesFor(AERecipeTypes.TRANSFORM)
                .stream()
                .filter(t -> t.value().circumstance.isFluid() && t.value().circumstance.isFluid(Fluids.WATER))
                .forEach(holder -> {
                    TransformRecipe recipe = holder.value();
                    graph.addRecipe(
                            holder,
                            (List<ItemStack> items) -> {
                                List<CraftGuideStepData> steps = new ArrayList<>();
                                steps.add(new CraftGuideStepData(
                                        new Target(ItemHandlerStorage.TYPE, pos),
                                        items,
                                        List.of(),
                                        CommonThrowItemAction.TYPE
                                ));
                                steps.add(new CraftGuideStepData(
                                        new Target(ItemHandlerStorage.TYPE, pos),
                                        List.of(),
                                        List.of(recipe.getResultItem()),
                                        CommonPickupItemAction.TYPE
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
        manager.getAllRecipesFor(AERecipeTypes.TRANSFORM)
                .stream()
                .filter(t -> t.value().circumstance.isFluid() && t.value().circumstance.isFluid(Fluids.WATER))
                .forEach(RecipeIngredientCache::addRecipeCache);
    }

    @Override
    public Component getConfigName() {
        return Component.translatable("config.maid_storage_manager.crafting.generating.ae2.item_transform");
    }
}
