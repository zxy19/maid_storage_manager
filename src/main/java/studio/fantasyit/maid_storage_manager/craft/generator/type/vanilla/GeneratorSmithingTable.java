package studio.fantasyit.maid_storage_manager.craft.generator.type.vanilla;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import studio.fantasyit.maid_storage_manager.craft.WorkBlockTags;
import studio.fantasyit.maid_storage_manager.craft.generator.type.base.SimpleGenerator;
//import studio.fantasyit.maid_storage_manager.craft.type.SmithingType;
import studio.fantasyit.maid_storage_manager.data.InventoryItem;

import java.util.ArrayList;
import java.util.List;

public class GeneratorSmithingTable extends SimpleGenerator<SmithingRecipe, SmithingRecipeInput> {

    @Override
    public boolean isBlockValid(Level level, BlockPos pos) {
        return level.getBlockState(pos).is(WorkBlockTags.SMITHING_TABLE);
    }

    @Override
    protected RecipeType<SmithingRecipe> getRecipeType() {
        return RecipeType.SMITHING;
    }

    @Override
    protected Identifier getCraftType() {
        return Identifier.fromNamespaceAndPath("maid_storage_manager", "disabled"); // SmithingType disabled
    }

    @Override
    protected boolean isValid(List<InventoryItem> inventory, Level level, BlockPos pos, SmithingRecipe recipe) {
        return recipe instanceof SmithingTransformRecipe;
    }

    @Override
    protected ItemStack outputTransform(List<InventoryItem> inventory, Level level, SmithingRecipe recipe) {
        if (recipe instanceof SmithingTransformRecipe str) {
            return str.result.create();
        }
        return ItemStack.EMPTY;
    }

    @Override
    protected List<Ingredient> cacheIngredientsTransform(SmithingRecipe recipe) {
        List<Ingredient> result = new ArrayList<>();
        if (recipe instanceof SmithingTransformRecipe recipe1) {
            recipe1.template.ifPresent(result::add);
            result.add(recipe1.base);
            recipe1.addition.ifPresent(result::add);
        }
        return result;
    }

    @Override
    protected List<ItemStack> wrapOutputs(SmithingRecipe recipe, List<ItemStack> inputs, SmithingRecipeInput container, List<ItemStack> outputs) {
        if (recipe instanceof SmithingTransformRecipe smithingTransformRecipe) {
            return List.of(smithingTransformRecipe.result.create());
        } else {
            return List.of();
        }
    }

    @Override
    protected SmithingRecipeInput getWrappedContainer(SmithingRecipe recipe, List<ItemStack> inputs) {
        ItemStack[] itemStacks = {
                ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
        };
        for (int i = 0; i < Math.min(3, inputs.size()); i++) {
            itemStacks[i] = inputs.get(i);
        }
        return new SmithingRecipeInput(itemStacks[0], itemStacks[1], itemStacks[2]);
    }

    @Override
    public Component getConfigName() {
        return Component.translatable("config.maid_storage_manager.crafting.generating.maid_storage_manager.smithing");
    }
}
