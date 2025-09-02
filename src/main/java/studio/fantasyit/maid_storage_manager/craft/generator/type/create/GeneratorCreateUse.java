package studio.fantasyit.maid_storage_manager.craft.generator.type.create;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.kinetics.deployer.ManualApplicationRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.craft.context.common.CommonAttackAction;
import studio.fantasyit.maid_storage_manager.craft.context.common.CommonIdleAction;
import studio.fantasyit.maid_storage_manager.craft.context.common.CommonUseAction;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.generator.algo.ICachableGeneratorGraph;
import studio.fantasyit.maid_storage_manager.craft.generator.type.base.IAutoCraftGuideGenerator;
import studio.fantasyit.maid_storage_manager.craft.generator.util.GenerateCondition;
import studio.fantasyit.maid_storage_manager.craft.generator.util.GenerateIngredientUtil;
import studio.fantasyit.maid_storage_manager.craft.type.CommonType;
import studio.fantasyit.maid_storage_manager.data.InventoryItem;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;
import studio.fantasyit.maid_storage_manager.storage.ItemHandler.ItemHandlerStorage;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.util.PosUtil;
import studio.fantasyit.maid_storage_manager.util.StorageAccessUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GeneratorCreateUse implements IAutoCraftGuideGenerator {
    @Override
    public @NotNull ResourceLocation getType() {
        return AllRecipeTypes.ITEM_APPLICATION.getId();
    }

    @Override
    public boolean isBlockValid(Level level, BlockPos pos) {
        if (level.getBlockState(pos).isCollisionShapeFullBlock(level, pos)) {
            if (StorageAccessUtil.getMarksForPosSet((ServerLevel) level, Target.virtual(pos, null), List.of(pos.east(), pos.west(), pos.north(), pos.south()))
                    .stream()
                    .map(Pair::getB)
                    .anyMatch(t -> t.is(ItemRegistry.ALLOW_ACCESS.get()))) {
                if (PosUtil.findInUpperSquare(pos.above(), 1, 3, 1, t -> (level.getBlockState(t).isAir() ? null : true)) == null) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void generate(List<InventoryItem> inventory, Level level, BlockPos pos, ICachableGeneratorGraph graph, Map<ResourceLocation, List<BlockPos>> recognizedTypePositions) {
        StorageAccessUtil.Filter posFilter = GenerateCondition.getFilterOn(level, pos);
        level.getRecipeManager()
                .getAllRecipesFor(AllRecipeTypes.ITEM_APPLICATION.getType())
                .forEach((recipe) -> {
                    if ((Recipe<?>) recipe instanceof ManualApplicationRecipe manualApplicationRecipe) {
                        if (manualApplicationRecipe.getRequiredHeldItem().isEmpty()) {
                            return;
                        }
                        ItemStack resultItem = manualApplicationRecipe.getResultItem(level.registryAccess());
                        if (!posFilter.isAvailable(resultItem)) {
                            return;
                        }
                        List<Ingredient> ingredients = new ArrayList<>(manualApplicationRecipe.getIngredients());
                        Optional<Ingredient> toolOptional = GenerateIngredientUtil.optionalIngredient(
                                GenerateIngredientUtil.getIngredientForDestroyBlockItem(resultItem)
                        );
                        toolOptional.ifPresent(ingredients::add);
                        graph.addRecipe(
                                manualApplicationRecipe.getId(),
                                ingredients,
                                ingredients.stream().map(t -> 1).toList(),
                                resultItem
                                , (items) -> {
                                    List<CraftGuideStepData> craftGuideData = new ArrayList<>();
                                    craftGuideData.add(new CraftGuideStepData(
                                            new Target(ItemHandlerStorage.TYPE, pos, Direction.UP),
                                            List.of(items.get(0)),
                                            List.of(),
                                            CommonUseAction.TYPE
                                    ));
                                    craftGuideData.add(new CraftGuideStepData(
                                            new Target(ItemHandlerStorage.TYPE, pos.above(), Direction.UP),
                                            List.of(items.get(1)),
                                            List.of(),
                                            CommonUseAction.TYPE
                                    ));

                                    craftGuideData.add(new CraftGuideStepData(
                                            new Target(ItemHandlerStorage.TYPE, pos.above()),
                                            items.size() > 2 ? List.of(items.get(2)) : List.of(),
                                            List.of(resultItem),
                                            CommonAttackAction.TYPE
                                    ));
                                    if (items.size() > 2)
                                        craftGuideData.add(new CraftGuideStepData(
                                                new Target(ItemHandlerStorage.TYPE, pos.above()),
                                                List.of(),
                                                List.of(items.get(2)),
                                                CommonIdleAction.TYPE
                                        ));
                                    return new CraftGuideData(
                                            craftGuideData,
                                            CommonType.TYPE
                                    );

                                });
                    }
                });
    }

    @Override
    public void onCache(RecipeManager manager) {
    }

    @Override
    public Component getConfigName() {
        return Component.translatable("config.maid_storage_manager.crafting.generating.create.application");
    }
}