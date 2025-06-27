package studio.fantasyit.maid_storage_manager.craft.generator.type.vanilla;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.level.Level;
import studio.fantasyit.maid_storage_manager.craft.WorkBlockTags;
import studio.fantasyit.maid_storage_manager.craft.generator.type.base.SimpleGenerator;
import studio.fantasyit.maid_storage_manager.craft.type.SmithingType;
import studio.fantasyit.maid_storage_manager.data.InventoryItem;

import java.util.List;

public class GeneratorSmithingTable extends SimpleGenerator<SmithingRecipe, Container> {

    @Override
    public boolean isBlockValid(Level level, BlockPos pos) {
        return level.getBlockState(pos).is(WorkBlockTags.SMITHING_TABLE);
    }

    @Override
    protected RecipeType<SmithingRecipe> getRecipeType() {
        return RecipeType.SMITHING;
    }

    @Override
    protected ResourceLocation getCraftType() {
        return SmithingType.TYPE;
    }

    @Override
    protected boolean isValid(List<InventoryItem> inventory, Level level, BlockPos pos, SmithingRecipe recipe) {
        return recipe instanceof SmithingTransformRecipe;
    }

    @Override
    protected List<Ingredient> cacheIngredientsTransform(SmithingRecipe recipe) {
        if (recipe instanceof SmithingTransformRecipe recipe1)
            return List.of(
                    recipe1.template,
                    recipe1.base,
                    recipe1.addition
            );
        return List.of();
    }

    @Override
    protected List<ItemStack> wrapOutputs(SmithingRecipe recipe, List<ItemStack> inputs, Container container, List<ItemStack> outputs) {
        if (recipe instanceof SmithingTransformRecipe smithingTransformRecipe) {
            return List.of(smithingTransformRecipe.result);
        } else {
            return List.of();
        }
    }

    @Override
    protected Container getWrappedContainer(SmithingRecipe recipe, List<ItemStack> inputs) {
        SimpleContainer simpleContainer = new SimpleContainer(3);
        for (int i = 0; i < Math.min(3, inputs.size()); i++) {
            simpleContainer.setItem(i, inputs.get(i));
        }
        return simpleContainer;
    }
    @Override
    public Component getConfigName() {
        return Component.translatable("config.maid_storage_manager.crafting.generating.maid_storage_manager.smithing");
    }
}
