package studio.fantasyit.maid_storage_manager.craft.generator.type.mekanism;

import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.common.content.blocktype.FactoryType;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.cache.InputRecipeCache;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.factory.TileEntityItemStackToItemStackFactory;
import mekanism.common.tile.machine.TileEntityEnrichmentChamber;
import mekanism.common.tile.prefab.TileEntityConfigurableMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import studio.fantasyit.maid_storage_manager.craft.context.common.CommonPlaceItemAction;
import studio.fantasyit.maid_storage_manager.craft.context.common.CommonTakeItemAction;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.generator.config.ConfigTypes;
import studio.fantasyit.maid_storage_manager.storage.ItemHandler.ItemHandlerStorage;
import studio.fantasyit.maid_storage_manager.storage.Target;

import java.util.List;
import java.util.Map;

public class GeneratorMekEnrichment extends GeneratorMek<ItemStackToItemStackRecipe, SingleRecipeInput, InputRecipeCache.SingleItem<ItemStackToItemStackRecipe>> {
    ConfigTypes.ConfigType<Boolean> FACTORY_PARALLEL = new ConfigTypes.ConfigType<>(
            "factory_parallel",
            true,
            Component.translatable("config.maid_storage_manager.crafting.generating.mekanism.general.use_factory_parallel"),
            ConfigTypes.ConfigTypeEnum.Boolean
    );

    @Override
    public List<ConfigTypes.ConfigType<?>> getConfigurations() {
        return List.of(
                FACTORY_PARALLEL
        );
    }

    @Override
    protected MekanismRecipeType<SingleRecipeInput, ItemStackToItemStackRecipe, InputRecipeCache.SingleItem<ItemStackToItemStackRecipe>> getRecipeType() {
        return MekanismRecipeType.ENRICHING.get();
    }

    @Override
    List<Ingredient> getRecipeIngredients(ItemStackToItemStackRecipe recipe, RecipeManager level, Map<ResourceLocation, List<BlockPos>> recognizedTypePositions) {
        return List.of(Ingredient.of(recipe.getInput().getRepresentations().stream()));
    }

    @Override
    protected boolean addSteps(BlockPos pos, TileEntityConfigurableMachine machine, ItemStackToItemStackRecipe recipe, List<ItemStack> inputs, List<ItemStack> outputs, List<CraftGuideStepData> steps) {
        Direction inputSide = getTypeDirection(machine, List.of(DataType.INPUT, DataType.INPUT_OUTPUT));
        Direction outputSide = getTypeDirection(machine, List.of(DataType.OUTPUT, DataType.INPUT_OUTPUT));
        if (inputSide == null || outputSide == null)
            return false;
        steps.add(new CraftGuideStepData(
                new Target(ItemHandlerStorage.TYPE, pos, inputSide),
                inputs,
                List.of(),
                CommonPlaceItemAction.TYPE
        ));
        steps.add(new CraftGuideStepData(
                new Target(ItemHandlerStorage.TYPE, pos, outputSide),
                List.of(),
                outputs,
                CommonTakeItemAction.TYPE
        ));
        return true;
    }

    @Override
    public boolean isBlockValid(Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof TileEntityItemStackToItemStackFactory factory && factory.getFactoryType() == FactoryType.ENRICHING)
            return true;
        return level.getBlockEntity(pos) instanceof TileEntityEnrichmentChamber;
    }

    @Override
    public Component getConfigName() {
        return Component.translatable("config.maid_storage_manager.crafting.generating.mekanism.enrichment");
    }
    @Override
    protected int getRecipeMultiplier(BlockEntity machine, ItemStackToItemStackRecipe recipe) {
        if(FACTORY_PARALLEL.getValue()){
            return getFactoryParallel(machine);
        }
        return 1;
    }
}
