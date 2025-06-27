package studio.fantasyit.maid_storage_manager.craft.generator.type.vanilla;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.IShapedRecipe;
import studio.fantasyit.maid_storage_manager.craft.WorkBlockTags;
import studio.fantasyit.maid_storage_manager.craft.generator.type.base.SimpleGenerator;
import studio.fantasyit.maid_storage_manager.craft.type.CraftingType;
import studio.fantasyit.maid_storage_manager.util.RecipeUtil;

import java.util.ArrayList;
import java.util.List;

public class GeneratorCraftingTable extends SimpleGenerator<CraftingRecipe, CraftingContainer> {

    @Override
    public boolean isBlockValid(Level level, BlockPos pos) {
        return level.getBlockState(pos).is(WorkBlockTags.CRAFTING_TABLE);
    }

    @Override
    protected RecipeType getRecipeType() {
        return RecipeType.CRAFTING;
    }

    @Override
    protected ResourceLocation getCraftType() {
        return CraftingType.TYPE;
    }

    @Override
    protected CraftingContainer getWrappedContainer(CraftingRecipe recipe, List<ItemStack> inputs) {
        return RecipeUtil.wrapCraftingContainer(inputs, recipe);
    }

    @Override
    protected List<ItemStack> wrapInputs(CraftingRecipe recipe, List<ItemStack> inputs) {
        return wrapInputsForRecipe(inputs, recipe);
    }

    protected static List<ItemStack> wrapInputsForRecipe(List<ItemStack> items, CraftingRecipe recipe) {
        List<ItemStack> inputs = new ArrayList<>();
        for (int i = 0; i < 9; i++)
            inputs.add(ItemStack.EMPTY);
        if (recipe instanceof IShapedRecipe<?> shapedRecipe) {
            int c = 0;
            for (int y = 0; y < 3; y++) {
                for (int x = 0; x < 3; x++) {
                    if (x < shapedRecipe.getRecipeWidth() && y < shapedRecipe.getRecipeHeight() && c < items.size())
                        inputs.set(x + y * 3, items.get(c++));
                }
            }
        } else {
            for (int i = 0; i < items.size(); i++)
                inputs.set(i, items.get(i));
        }
        return inputs;
    }
    @Override
    public Component getConfigName() {
        return Component.translatable("config.maid_storage_manager.crafting.generating.maid_storage_manager.crafting");
    }
}
