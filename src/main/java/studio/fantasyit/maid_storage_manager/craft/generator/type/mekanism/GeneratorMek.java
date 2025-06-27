package studio.fantasyit.maid_storage_manager.craft.generator.type.mekanism;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.MaidPathFindingBFS;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.cache.IInputRecipeCache;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.prefab.TileEntityConfigurableMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.generator.algo.ICachableGeneratorGraph;
import studio.fantasyit.maid_storage_manager.craft.generator.type.base.IAutoCraftGuideGenerator;
import studio.fantasyit.maid_storage_manager.craft.generator.util.GenerateCondition;
import studio.fantasyit.maid_storage_manager.craft.type.CommonType;
import studio.fantasyit.maid_storage_manager.data.InventoryItem;
import studio.fantasyit.maid_storage_manager.integration.mekanism.MekanismIntegration;
import studio.fantasyit.maid_storage_manager.util.StorageAccessUtil;

import java.util.*;

public abstract class GeneratorMek<T extends MekanismRecipe, C extends IInputRecipeCache> implements IAutoCraftGuideGenerator {
    protected abstract MekanismRecipeType<T, C> getRecipeType();

    @Override
    public @NotNull ResourceLocation getType() {
        return getRecipeType().getRegistryName();
    }

    abstract List<Ingredient> getRecipeIngredients(T recipe, RecipeManager level, Map<ResourceLocation, List<BlockPos>> recognizedTypePositions);

    public List<Integer> getIngredientCounts(T recipe, List<Ingredient> ingredients) {
        return ingredients.stream().map(t -> Arrays.stream(t.getItems()).findFirst().map(ItemStack::getCount).orElse(1)).toList();
    }

    public List<ItemStack> getRecipeOutputs(T recipe, RegistryAccess registryAccess) {
        return List.of(recipe.getResultItem(registryAccess));
    }

    protected @Nullable Direction getTypeDirection(TileEntityConfigurableMachine machine, List<DataType> dataTypes) {
        ConfigInfo config = machine.getConfig().getConfig(TransmissionType.ITEM);
        if (config == null) return null;
        for (DataType dataType : dataTypes) {
            Set<Direction> sidesForData = config.getSidesForData(dataType);
            if (!sidesForData.isEmpty())
                return sidesForData.iterator().next();
        }
        return null;
    }

    abstract protected boolean addSteps(BlockPos pos, TileEntityConfigurableMachine machine, T recipe, List<ItemStack> inputs, List<ItemStack> outputs, List<CraftGuideStepData> steps);

    @Override
    public void generate(List<InventoryItem> inventory, Level level, BlockPos pos, ICachableGeneratorGraph graph, Map<ResourceLocation, List<BlockPos>> recognizedTypePositions) {
        StorageAccessUtil.Filter posFilter = GenerateCondition.getFilterOn(level, pos);
        if (level.getBlockEntity(pos) instanceof TileEntityConfigurableMachine machine) {
            level.getRecipeManager()
                    .getAllRecipesFor(getRecipeType())
                    .forEach(recipe -> generate(recipe, machine, level, pos, graph, recognizedTypePositions, posFilter));
        }
    }

    public void generate(T recipe, TileEntityConfigurableMachine machine, Level level, BlockPos pos, ICachableGeneratorGraph graph, Map<ResourceLocation, List<BlockPos>> recognizedTypePositions, StorageAccessUtil.Filter posFilter) {
        List<Ingredient> ingredient = getRecipeIngredients(recipe, level.getRecipeManager(), recognizedTypePositions);
        List<Integer> counts = getIngredientCounts(recipe, ingredient);
        List<ItemStack> outputs = getRecipeOutputs(recipe, level.registryAccess());
        if (outputs.isEmpty() || !posFilter.isAvailable(outputs.get(0)))
            return;
        generateForIOR(recipe, machine, pos, graph, ingredient, counts, outputs);
    }

    protected void generateForIOR(T recipe, TileEntityConfigurableMachine machine, BlockPos pos, ICachableGeneratorGraph graph, List<Ingredient> ingredient, List<Integer> counts, List<ItemStack> outputs) {
        graph.addRecipe(recipe.getId(), ingredient, counts, outputs, (items) -> {
            List<CraftGuideStepData> step = new ArrayList<>();
            if (addSteps(pos, machine, recipe, items, outputs, step)) {
                return new CraftGuideData(step, CommonType.TYPE);
            }
            return null;
        });
    }

    @Override
    public void onCache(RecipeManager manager) {
    }

    @Override
    public boolean positionalAvailable(ServerLevel level, EntityMaid maid, BlockPos pos, MaidPathFindingBFS pathFinding) {
        if (level.getBlockEntity(pos) instanceof TileEntityMekanism machine)
            if (!MekanismIntegration.isAccessibleByMaid(machine, maid))
                return false;
        return IAutoCraftGuideGenerator.super.positionalAvailable(level, maid, pos, pathFinding);
    }
}