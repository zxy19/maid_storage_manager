package studio.fantasyit.maid_storage_manager.craft.generator.type.mekanism;

import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.ItemStackGasToItemStackRecipe;
import mekanism.api.recipes.ItemStackToGasRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.common.content.blocktype.FactoryType;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.cache.InputRecipeCache;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.factory.TileEntityItemStackGasToItemStackFactory;
import mekanism.common.tile.machine.TileEntityOsmiumCompressor;
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
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.craft.context.common.CommonPlaceItemAction;
import studio.fantasyit.maid_storage_manager.craft.context.common.CommonTakeItemAction;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.generator.algo.ICachableGeneratorGraph;
import studio.fantasyit.maid_storage_manager.craft.type.CommonType;
import studio.fantasyit.maid_storage_manager.storage.ItemHandler.ItemHandlerStorage;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.util.MathUtil;
import studio.fantasyit.maid_storage_manager.util.StorageAccessUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GeneratorMekOsmiumComp extends GeneratorMek<ItemStackGasToItemStackRecipe, InputRecipeCache.ItemChemical<Gas, GasStack, ItemStackGasToItemStackRecipe>> {
    @Override
    protected MekanismRecipeType<ItemStackGasToItemStackRecipe, InputRecipeCache.ItemChemical<Gas, GasStack, ItemStackGasToItemStackRecipe>> getRecipeType() {
        return MekanismRecipeType.COMPRESSING.get();
    }

    protected List<Pair<ItemStack, Integer>> infusionInputs(RecipeManager manager, ChemicalStackIngredient.GasStackIngredient input, long maxCapacity) {
        List<Pair<ItemStack, Integer>> ret = new ArrayList<>();
        List<ItemStackToGasRecipe> gasConverters = manager
                .getAllRecipesFor(MekanismRecipeType.GAS_CONVERSION.get());
        input.getRepresentations()
                .forEach(gasStack -> {
                    if (gasStack.getAmount() > 1e9) return;
//                    int amount = (int) gasStack.getAmount();
                    //? TODO: WHERE the 200 FROM?
                    int amount = 200;
                    for (ItemStackToGasRecipe recipe : gasConverters) {
                        if (recipe.getOutputDefinition().stream().anyMatch(output -> output.isTypeEqual(gasStack))) {
                            for (ItemStack possibleInput : recipe.getInput().getRepresentations()) {
                                long getAmountL = recipe.getOutput(possibleInput).getAmount();
                                if (getAmountL > 1e9) continue;
                                int getAmount = (int) getAmountL;
                                int totalAmount = MathUtil.lcm(getAmount, amount);
                                if (totalAmount > maxCapacity) continue;
                                ret.add(new Pair<>(possibleInput.copyWithCount(totalAmount / getAmount), totalAmount / amount));
                            }
                        }
                    }
                });
        return ret;
    }

    @Override
    List<Ingredient> getRecipeIngredients(ItemStackGasToItemStackRecipe recipe, RecipeManager manager, Map<ResourceLocation, List<BlockPos>> recognizedTypePositions) {
        return List.of();
    }

    @Override
    protected boolean addSteps(BlockPos pos, TileEntityConfigurableMachine machine, ItemStackGasToItemStackRecipe recipe, List<ItemStack> inputs, List<ItemStack> outputs, List<CraftGuideStepData> steps) {
        if (machine instanceof TileEntityOsmiumCompressor oc) {
            oc.getGasManager();
        }
        Direction inputSide = getTypeDirection(machine, List.of(DataType.INPUT, DataType.INPUT_OUTPUT));
        Direction outputSide = getTypeDirection(machine, List.of(DataType.OUTPUT, DataType.INPUT_OUTPUT));
        Direction extra = getTypeDirection(machine, List.of(DataType.EXTRA));
        if (inputSide == null || outputSide == null || extra == null)
            return false;
        steps.add(new CraftGuideStepData(
                new Target(ItemHandlerStorage.TYPE, pos, extra),
                List.of(inputs.get(1)),
                List.of(),
                CommonPlaceItemAction.TYPE,
                false,
                new CompoundTag()
        ));
        steps.add(new CraftGuideStepData(
                new Target(ItemHandlerStorage.TYPE, pos, inputSide),
                List.of(inputs.get(0)),
                List.of(),
                CommonPlaceItemAction.TYPE,
                false,
                new CompoundTag()
        ));
        steps.add(new CraftGuideStepData(
                new Target(ItemHandlerStorage.TYPE, pos, outputSide),
                List.of(),
                List.of(outputs.get(0).copyWithCount(inputs.get(0).getCount())),
                CommonTakeItemAction.TYPE,
                false,
                new CompoundTag()
        ));
        return true;
    }

    @Override
    public void generate(ItemStackGasToItemStackRecipe recipe, TileEntityConfigurableMachine machine, Level level, BlockPos pos, ICachableGeneratorGraph graph, Map<ResourceLocation, List<BlockPos>> recognizedTypePositions, StorageAccessUtil.Filter posFilter) {
        long capacity = 2000;
        if (machine instanceof TileEntityOsmiumCompressor oc)
            capacity = oc.gasTank.getCapacity();
        else if (machine instanceof TileEntityItemStackGasToItemStackFactory fc)
            capacity = fc.getGasTank().getCapacity();

        List<Pair<ItemStack, Integer>> possibleInfusion = infusionInputs(level.getRecipeManager(), recipe.getChemicalInput(), capacity);
        Ingredient ingredient = Ingredient.of(recipe.getItemInput().getRepresentations().stream());
        for (Pair<ItemStack, Integer> pair : possibleInfusion) {
            List<Ingredient> inputs = List.of(ingredient, Ingredient.of(pair.getA()));
            List<Integer> counts = List.of(pair.getB(), pair.getA().getCount());
            List<ItemStack> outputs = recipe.getOutputDefinition();
            if (outputs.isEmpty() || !posFilter.isAvailable(outputs.get(0)))
                continue;
            ResourceLocation oid = recipe.getId();
            @Nullable ResourceLocation itemKey = ForgeRegistries.ITEMS.getKey(pair.getA().getItem());
            if (itemKey == null)
                continue;
            ResourceLocation id = new ResourceLocation(oid.getNamespace(), oid.getPath() + "_" + itemKey.getNamespace() + "_" + itemKey.getPath());
            graph.addRecipe(id, inputs, counts, outputs, (items) -> {
                List<CraftGuideStepData> step = new ArrayList<>();
                if (addSteps(pos, machine, recipe, items, outputs, step)) {
                    return new CraftGuideData(step, CommonType.TYPE);
                }
                return null;
            });
        }
    }

    @Override
    public boolean isBlockValid(Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof TileEntityItemStackGasToItemStackFactory factory && factory.getFactoryType() == FactoryType.COMPRESSING)
            return true;
        return level.getBlockEntity(pos) instanceof TileEntityOsmiumCompressor;
    }

    @Override
    public Component getConfigName() {
        return Component.translatable("config.maid_storage_manager.crafting.generating.mekanism.osmium_compress");
    }
}
