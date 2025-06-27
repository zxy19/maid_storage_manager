package studio.fantasyit.maid_storage_manager.craft.generator.type.vanilla;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import studio.fantasyit.maid_storage_manager.craft.WorkBlockTags;
import studio.fantasyit.maid_storage_manager.craft.generator.type.base.SimpleGenerator;
import studio.fantasyit.maid_storage_manager.craft.type.FurnaceType;

import java.util.List;

public class GeneratorFurnace extends SimpleGenerator<SmeltingRecipe, Container> {
    @Override
    protected RecipeType getRecipeType() {
        return RecipeType.SMELTING;
    }

    @Override
    protected ResourceLocation getCraftType() {
        return FurnaceType.TYPE;
    }

    @Override
    protected List<Ingredient> cacheIngredientsTransform(SmeltingRecipe recipe) {
        NonNullList<Ingredient> ingredients = recipe.getIngredients();
        if (recipe.getResultItem(RegistryAccess.EMPTY).is(Items.CHARCOAL) && !ingredients.isEmpty()) {
            return List.of(
                    ingredients.get(0),
                    Ingredient.merge(
                            List.of(
                                    Ingredient.of(ItemTags.LOGS_THAT_BURN),
                                    Ingredient.of(ItemTags.PLANKS),
                                    Ingredient.of(ItemTags.COALS)
                            )
                    )
            );
        } else {
            return List.of(
                    ingredients.get(0),
                    Ingredient.of(ItemTags.COALS)
            );
        }
    }

    @Override
    protected Container getWrappedContainer(SmeltingRecipe recipe, List<ItemStack> inputs) {
        SimpleContainer c = new SimpleContainer(2);
        for (int i = 0; i < Math.min(2, inputs.size()); i++) {
            c.setItem(i, inputs.get(i));
        }
        return c;
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