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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import studio.fantasyit.maid_storage_manager.craft.context.common.CommonPlaceItemAction;
import studio.fantasyit.maid_storage_manager.craft.context.common.CommonTakeItemAction;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.storage.ItemHandler.ItemHandlerStorage;
import studio.fantasyit.maid_storage_manager.storage.Target;

import java.util.List;
import java.util.Map;

public class GeneratorMekEnrichment extends GeneratorMek<ItemStackToItemStackRecipe, InputRecipeCache.SingleItem<ItemStackToItemStackRecipe>> {
    @Override
    protected MekanismRecipeType<ItemStackToItemStackRecipe, InputRecipeCache.SingleItem<ItemStackToItemStackRecipe>> getRecipeType() {
        return MekanismRecipeType.ENRICHING.get();
    }

    @Override
    List<Ingredient> getRecipeIngredients(ItemStackToItemStackRecipe recipe, RecipeManager level, Map<ResourceLocation, List<BlockPos>> recognizedTypePositions) {
        return List.of(Ingredient.of(recipe.getInput().getRepresentations().stream()));
    }

    @Override
    protected boolean addSteps(Level level, BlockPos pos, TileEntityConfigurableMachine machine, ItemStackToItemStackRecipe recipe, List<ItemStack> inputs, List<ItemStack> outputs, List<CraftGuideStepData> steps) {
        Direction inputSide = getTypeDirection(machine, List.of(DataType.INPUT, DataType.INPUT_OUTPUT));
        Direction outputSide = getTypeDirection(machine, List.of(DataType.OUTPUT, DataType.INPUT_OUTPUT));
        if (inputSide == null || outputSide == null)
            return false;
        steps.add(new CraftGuideStepData(
                new Target(ItemHandlerStorage.TYPE, pos, inputSide),
                inputs,
                List.of(),
                CommonPlaceItemAction.TYPE,
                false,
                new CompoundTag()
        ));
        steps.add(new CraftGuideStepData(
                new Target(ItemHandlerStorage.TYPE, pos, outputSide),
                List.of(),
                List.of(recipe.getResultItem(level.registryAccess())),
                CommonTakeItemAction.TYPE,
                false,
                new CompoundTag()
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
}
