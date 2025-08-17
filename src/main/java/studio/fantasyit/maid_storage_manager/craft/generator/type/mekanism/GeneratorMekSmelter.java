package studio.fantasyit.maid_storage_manager.craft.generator.type.mekanism;

import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.common.content.blocktype.FactoryType;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.cache.InputRecipeCache;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.factory.TileEntityItemStackToItemStackFactory;
import mekanism.common.tile.machine.TileEntityEnergizedSmelter;
import mekanism.common.tile.prefab.TileEntityConfigurableMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import studio.fantasyit.maid_storage_manager.craft.context.common.CommonPlaceItemAction;
import studio.fantasyit.maid_storage_manager.craft.context.common.CommonTakeItemAction;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.generator.algo.ICachableGeneratorGraph;
import studio.fantasyit.maid_storage_manager.craft.generator.config.ConfigTypes;
import studio.fantasyit.maid_storage_manager.craft.generator.util.GenerateCondition;
import studio.fantasyit.maid_storage_manager.craft.generator.util.RecipeUtil;
import studio.fantasyit.maid_storage_manager.craft.type.CommonType;
import studio.fantasyit.maid_storage_manager.craft.type.FurnaceType;
import studio.fantasyit.maid_storage_manager.data.InventoryItem;
import studio.fantasyit.maid_storage_manager.storage.ItemHandler.ItemHandlerStorage;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.util.StorageAccessUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GeneratorMekSmelter extends GeneratorMek<ItemStackToItemStackRecipe, SingleRecipeInput, InputRecipeCache.SingleItem<ItemStackToItemStackRecipe>> {
    ConfigTypes.ConfigType<Boolean> REPLACE_FURNACE = new ConfigTypes.ConfigType<>(
            "replace_furnace",
            true,
            Component.translatable("config.maid_storage_manager.crafting.generating.mekanism.smelter.replace_furnace"),
            ConfigTypes.ConfigTypeEnum.Boolean
    );

    @Override
    protected MekanismRecipeType<SingleRecipeInput, ItemStackToItemStackRecipe, InputRecipeCache.SingleItem<ItemStackToItemStackRecipe>> getRecipeType() {
        return MekanismRecipeType.SMELTING.get();
    }

    @Override
    public void generate(List<InventoryItem> inventory, Level level, BlockPos pos, ICachableGeneratorGraph graph, Map<ResourceLocation, List<BlockPos>> recognizedTypePositions) {
        StorageAccessUtil.Filter posFilter = GenerateCondition.getFilterOn(level, pos);
        if (level.getBlockEntity(pos) instanceof TileEntityConfigurableMachine machine) {
            level.getRecipeManager()
                    .getAllRecipesFor(getRecipeType())
                    .forEach(recipe -> generate(recipe, machine, level, pos, graph, recognizedTypePositions, posFilter));
            //原版的SMELTING配方
            level.getRecipeManager()
                    .getAllRecipesFor(RecipeType.SMELTING)
                    .forEach(holder -> {
                        SmeltingRecipe recipe = holder.value();
                        ItemStack output = recipe.getResultItem(level.registryAccess());
                        graph.addRecipe(wrapId(holder.id()),
                                recipe.getIngredients(),
                                recipe.getIngredients().stream().map(t -> 1).toList(),
                                output,
                                (items) -> {
                                    List<CraftGuideStepData> step = new ArrayList<>();
                                    if (addSteps(pos, machine, items, List.of(output), step)) {
                                        return new CraftGuideData(step, CommonType.TYPE);
                                    }
                                    return null;
                                });
                    });
        }
        if (REPLACE_FURNACE.getValue())
            graph.blockType(FurnaceType.TYPE);
    }

    @Override
    List<Ingredient> getRecipeIngredients(ItemStackToItemStackRecipe recipe, RecipeManager level, Map<ResourceLocation, List<BlockPos>> recognizedTypePositions) {
        return List.of(Ingredient.of(recipe.getInput().getRepresentations().stream()));
    }

    @Override
    protected boolean addSteps(BlockPos pos, TileEntityConfigurableMachine machine, ItemStackToItemStackRecipe recipe, List<ItemStack> inputs, List<ItemStack> outputs, List<CraftGuideStepData> steps) {
        return addSteps(pos, machine, inputs, outputs, steps);
    }

    private boolean addSteps(BlockPos pos, TileEntityConfigurableMachine machine, List<ItemStack> inputs, List<ItemStack> outputs, List<CraftGuideStepData> steps) {
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
                outputs,
                CommonTakeItemAction.TYPE,
                false,
                new CompoundTag()
        ));
        return true;
    }

    @Override
    public boolean isBlockValid(Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof TileEntityItemStackToItemStackFactory factory && factory.getFactoryType() == FactoryType.SMELTING)
            return true;
        return level.getBlockEntity(pos) instanceof TileEntityEnergizedSmelter;
    }

    @Override
    public Component getConfigName() {
        return Component.translatable("config.maid_storage_manager.crafting.generating.mekanism.smelter");
    }

    private ResourceLocation wrapId(ResourceLocation id) {
        return RecipeUtil.wrapLocation(getType(), id);
    }

    @Override
    public List<ConfigTypes.ConfigType<?>> getConfigurations() {
        return List.of(REPLACE_FURNACE);
    }
}
