package studio.fantasyit.maid_storage_manager.craft.generator.util;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.crafting.IntersectionIngredient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class GenerateIngredientUtil {
    public static Ingredient getToolIngredient() {
        return IntersectionIngredient.of(
                Ingredient.of(ItemTags.SWORDS),
                Ingredient.of(ItemTags.AXES),
                Ingredient.of(ItemTags.HOES),
                Ingredient.of(ItemTags.PICKAXES),
                Ingredient.of(ItemTags.SHOVELS)
        );
    }

    public static Ingredient getIngredientForDestroyBlockItem(ItemStack itemStack) {
        if (itemStack.getItem() instanceof BlockItem blockItem) {
            BlockState blockState = blockItem.getBlock().defaultBlockState();
            ItemStack[] items = getToolIngredient().getItems();
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

    public static void each3items(List<ItemStack> itemStackList, Consumer<List<ItemStack>> consumer) {
        for (int i = 0; i < itemStackList.size(); i += 3) {
            List<ItemStack> itemStackList1 = itemStackList.subList(i, Math.min(itemStackList.size(), i + 3));
            consumer.accept(itemStackList1);
        }
    }
}
