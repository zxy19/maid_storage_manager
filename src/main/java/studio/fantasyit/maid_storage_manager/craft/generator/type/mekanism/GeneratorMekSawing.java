package studio.fantasyit.maid_storage_manager.craft.generator.type.mekanism;

import mekanism.api.recipes.SawmillRecipe;
import mekanism.common.content.blocktype.FactoryType;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.cache.InputRecipeCache;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.factory.TileEntityItemStackToItemStackFactory;
import mekanism.common.tile.machine.TileEntityPrecisionSawmill;
import mekanism.common.tile.prefab.TileEntityConfigurableMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
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

public class GeneratorMekSawing extends GeneratorMek<SawmillRecipe, InputRecipeCache.SingleItem<SawmillRecipe>> {
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
    protected MekanismRecipeType<SawmillRecipe, InputRecipeCache.SingleItem<SawmillRecipe>> getRecipeType() {
        return MekanismRecipeType.SAWING.get();
    }

    @Override
    List<Ingredient> getRecipeIngredients(SawmillRecipe recipe, RecipeManager level, Map<ResourceLocation, List<BlockPos>> recognizedTypePositions) {
        return List.of(Ingredient.of(recipe.getInput().getRepresentations().stream()));
    }

    @Override
    public List<ItemStack> getRecipeOutputs(SawmillRecipe recipe, RegistryAccess registryAccess) {
        return recipe.getMainOutputDefinition();
    }

    @Override
    protected boolean addSteps(BlockPos pos, TileEntityConfigurableMachine machine, SawmillRecipe recipe, List<ItemStack> inputs, List<ItemStack> outputs, List<CraftGuideStepData> steps) {
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
                recipe.getMainOutputDefinition(),
                CommonTakeItemAction.TYPE
        ));
        steps.add(new CraftGuideStepData(
                new Target(ItemHandlerStorage.TYPE, pos, outputSide),
                List.of(),
                recipe.getSecondaryOutputDefinition(),
                CommonTakeItemAction.TYPE,
                recipe.getSecondaryChance() < 1,
                new CompoundTag()
        ));
        return true;
    }

    @Override
    public boolean isBlockValid(Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof TileEntityItemStackToItemStackFactory factory && factory.getFactoryType() == FactoryType.SAWING)
            return true;
        return level.getBlockEntity(pos) instanceof TileEntityPrecisionSawmill;
    }

    @Override
    public Component getConfigName() {
        return Component.translatable("config.maid_storage_manager.crafting.generating.mekanism.precision");
    }
    @Override
    protected int getRecipeMultiplier(BlockEntity machine, SawmillRecipe recipe) {
        if(FACTORY_PARALLEL.getValue()){
            return getFactoryParallel(machine);
        }
        return 1;
    }
}
