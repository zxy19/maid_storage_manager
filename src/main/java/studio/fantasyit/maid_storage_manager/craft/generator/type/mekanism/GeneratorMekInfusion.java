package studio.fantasyit.maid_storage_manager.craft.generator.type.mekanism;

import mekanism.api.recipes.ItemStackChemicalToItemStackRecipe;
import mekanism.api.recipes.ItemStackToChemicalRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.vanilla_input.SingleItemChemicalRecipeInput;
import mekanism.common.content.blocktype.FactoryType;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.cache.InputRecipeCache;
import mekanism.common.tags.MekanismTags;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.factory.TileEntityItemStackChemicalToItemStackFactory;
import mekanism.common.tile.machine.TileEntityMetallurgicInfuser;
import mekanism.common.tile.prefab.TileEntityConfigurableMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
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

public class GeneratorMekInfusion extends GeneratorMek<ItemStackChemicalToItemStackRecipe, SingleItemChemicalRecipeInput, InputRecipeCache.ItemChemical<ItemStackChemicalToItemStackRecipe>> {
    @Override
    protected MekanismRecipeType<SingleItemChemicalRecipeInput, ItemStackChemicalToItemStackRecipe, InputRecipeCache.ItemChemical<ItemStackChemicalToItemStackRecipe>> getRecipeType() {
        return MekanismRecipeType.METALLURGIC_INFUSING.get();
    }

    protected List<Pair<ItemStack, Integer>> infusionInputs(RecipeManager manager, ChemicalStackIngredient input, boolean alwaysEnrich) {
        List<Pair<ItemStack, Integer>> ret = new ArrayList<>();
        List<RecipeHolder<ItemStackToChemicalRecipe>> infusionConverters = manager
                .getAllRecipesFor(MekanismRecipeType.CHEMICAL_CONVERSION.get());
        input.getRepresentations()
                .forEach(infusionStack -> {
                    if (infusionStack.getAmount() > 1e9) return;
                    int amount = (int) infusionStack.getAmount();
                    for (RecipeHolder<ItemStackToChemicalRecipe> holder : infusionConverters) {
                        ItemStackToChemicalRecipe recipe = holder.value();
                        if (recipe.getOutputDefinition().stream().anyMatch(output -> output.is(infusionStack.getChemical()))) {
                            for (ItemStack possibleInput : recipe.getInput().getRepresentations()) {
                                if (alwaysEnrich && !possibleInput.is(MekanismTags.Items.ENRICHED)) continue;
                                long getAmountL = recipe.getOutput(possibleInput).getAmount();
                                if (getAmountL > 1e9) continue;
                                int getAmount = (int) getAmountL;
                                int totalAmount = MathUtil.lcm(getAmount, amount);
                                ret.add(new Pair<>(possibleInput.copyWithCount(totalAmount / getAmount), totalAmount / amount));
                            }
                        }
                    }
                });
        return ret;
    }

    @Override
    List<Ingredient> getRecipeIngredients(ItemStackChemicalToItemStackRecipe recipe, RecipeManager manager, Map<ResourceLocation, List<BlockPos>> recognizedTypePositions) {
        return List.of();
    }

    @Override
    protected boolean addSteps(BlockPos pos, TileEntityConfigurableMachine machine, ItemStackChemicalToItemStackRecipe recipe, List<ItemStack> inputs, List<ItemStack> outputs, List<CraftGuideStepData> steps) {
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
    public void generate(RecipeHolder<ItemStackChemicalToItemStackRecipe> holder, TileEntityConfigurableMachine machine, Level level, BlockPos pos, ICachableGeneratorGraph graph, Map<ResourceLocation, List<BlockPos>> recognizedTypePositions, StorageAccessUtil.Filter posFilter) {
        ItemStackChemicalToItemStackRecipe recipe = holder.value();
        boolean alwaysEnrich = false;
        if (recognizedTypePositions.containsKey(MekanismRecipeType.ENRICHING.getRegistryName()) && !recognizedTypePositions.get(MekanismRecipeType.ENRICHING.getRegistryName()).isEmpty()) {
            alwaysEnrich = true;
        }
        List<Pair<ItemStack, Integer>> possibleInfusion = infusionInputs(level.getRecipeManager(), recipe.getChemicalInput(), alwaysEnrich);
        Ingredient ingredient = Ingredient.of(recipe.getItemInput().getRepresentations().stream());
        for (Pair<ItemStack, Integer> pair : possibleInfusion) {
            List<Ingredient> inputs = List.of(ingredient, Ingredient.of(pair.getA()));
            List<Integer> counts = List.of(pair.getB(), pair.getA().getCount());
            List<ItemStack> outputs = recipe.getOutputDefinition();
            if (outputs.isEmpty() || !posFilter.isAvailable(outputs.get(0)))
                continue;
            ResourceLocation oid = holder.id();
            @Nullable ResourceLocation itemKey = BuiltInRegistries.ITEM.getKey(pair.getA().getItem());
            if (itemKey == null)
                continue;
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(oid.getNamespace(), oid.getPath() + "_" + itemKey.getNamespace() + "_" + itemKey.getPath());

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
        if (level.getBlockEntity(pos) instanceof TileEntityItemStackChemicalToItemStackFactory factory && factory.getFactoryType() == FactoryType.INFUSING)
            return true;
        return level.getBlockEntity(pos) instanceof TileEntityMetallurgicInfuser;
    }

    @Override
    public boolean canCacheGraph() {
        return false;
    }

    @Override
    public Component getConfigName() {
        return Component.translatable("config.maid_storage_manager.crafting.generating.mekanism.meta_infuser");
    }
}
