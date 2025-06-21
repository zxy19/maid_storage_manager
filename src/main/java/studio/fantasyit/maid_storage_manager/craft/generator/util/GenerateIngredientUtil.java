package studio.fantasyit.maid_storage_manager.craft.generator.util;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GenerateIngredientUtil {
    public static Ingredient getIngredientForDestroyBlockItem(ItemStack itemStack) {
        if (itemStack.getItem() instanceof BlockItem blockItem) {
            BlockState blockState = blockItem.getBlock().defaultBlockState();
            ItemStack[] items = Ingredient.of(ItemTags.TOOLS).getItems();
            List<ItemStack> suitable = new ArrayList<>();
            for (ItemStack item : items) {
                if (item.isCorrectToolForDrops(blockState)) {
                    suitable.add(item);
                }
            }
            if (!suitable.isEmpty())
                return Ingredient.of(suitable.toArray(new ItemStack[0]));
        }
        return Ingredient.EMPTY;
    }

    public static Optional<Ingredient> optionalIngredient(Ingredient ingredient) {
        return ingredient.isEmpty() ? Optional.empty() : Optional.of(ingredient);
    }
}
