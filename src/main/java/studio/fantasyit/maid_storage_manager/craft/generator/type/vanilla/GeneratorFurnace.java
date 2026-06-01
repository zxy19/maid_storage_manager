package studio.fantasyit.maid_storage_manager.craft.generator.type.vanilla;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.IntersectionIngredient;
import studio.fantasyit.maid_storage_manager.craft.WorkBlockTags;
import studio.fantasyit.maid_storage_manager.craft.generator.type.base.SimpleGenerator;
import studio.fantasyit.maid_storage_manager.craft.type.FurnaceType;
import studio.fantasyit.maid_storage_manager.data.InventoryItem;

import java.util.List;

//import studio.fantasyit.maid_storage_manager.craft.type.FurnaceType;

public class GeneratorFurnace extends SimpleGenerator<SmeltingRecipe, SingleRecipeInput> {
    @Override
    protected RecipeType getRecipeType() {
        return RecipeType.SMELTING;
    }

    @Override
    protected Identifier getCraftType() {
        return FurnaceType.TYPE;
    }

    @Override
    protected ItemStack getOutputItem(List<InventoryItem> inventory, Level level, SmeltingRecipe recipe) {
        return recipe.assemble(new SingleRecipeInput(ItemStack.EMPTY));
    }

    @Override
    protected List<Ingredient> cacheIngredientsTransform(SmeltingRecipe recipe) {
        List<Ingredient> ingredients = recipe.placementInfo().ingredients();
        ItemStack resultOutput = recipe.assemble(new SingleRecipeInput(ItemStack.EMPTY));
        if (resultOutput.is(Items.CHARCOAL) && !ingredients.isEmpty()) {
            return List.of(
                    ingredients.get(0),
                    IntersectionIngredient.of(
                            Ingredient.of(BuiltInRegistries.ITEM.get(ItemTags.LOGS_THAT_BURN).orElseThrow()),
                            Ingredient.of(BuiltInRegistries.ITEM.get(ItemTags.PLANKS).orElseThrow()),
                            Ingredient.of(BuiltInRegistries.ITEM.get(ItemTags.COALS).orElseThrow())
                    )
            );
        } else {
            return List.of(
                    ingredients.get(0),
                    Ingredient.of(BuiltInRegistries.ITEM.get(ItemTags.COALS).orElseThrow())
            );
        }
    }

    @Override
    protected SingleRecipeInput getWrappedContainer(SmeltingRecipe recipe, List<ItemStack> inputs) {
        if (inputs.size() > 1) {
            return new SingleRecipeInput(inputs.get(1));
        }
        return new SingleRecipeInput(ItemStack.EMPTY);
    }

    @Override
    public boolean isBlockValid(Level level, BlockPos pos) {
        return level.getBlockState(pos).is(WorkBlockTags.FURNACE);
    }

    @Override
    public Component getConfigName() {
        return Component.translatable("config.maid_storage_manager.crafting.generating.maid_storage_manager.furnace");
    }
}