package studio.fantasyit.maid_storage_manager.craft.generator.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.crafting.IntersectionIngredient;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class GenerateIngredientUtil {
    public static Ingredient getToolIngredient() {
        return IntersectionIngredient.of(
                Ingredient.of(BuiltInRegistries.ITEM.get(ItemTags.SWORDS).orElseThrow()),
                Ingredient.of(BuiltInRegistries.ITEM.get(ItemTags.AXES).orElseThrow()),
                Ingredient.of(BuiltInRegistries.ITEM.get(ItemTags.HOES).orElseThrow()),
                Ingredient.of(BuiltInRegistries.ITEM.get(ItemTags.PICKAXES).orElseThrow()),
                Ingredient.of(BuiltInRegistries.ITEM.get(ItemTags.SHOVELS).orElseThrow())
        );
    }

    public static Optional<Ingredient> getIngredientForDestroyBlockItem(ItemStack itemStack) {
        if (itemStack.getItem() instanceof BlockItem blockItem) {
            BlockState blockState = blockItem.getBlock().defaultBlockState();
            List<ItemLike> suitable = new ArrayList<>();
            getToolIngredient().items().forEach(holder -> {
                Item item = holder.value();
                if (item.getDefaultInstance().isCorrectToolForDrops(blockState)) {
                    suitable.add(item);
                }
            });
            if (!suitable.isEmpty())
                return Optional.of(Ingredient.of(suitable.toArray(new ItemLike[0])));
        }
        return Optional.empty();
    }

    @Nullable
    public static Ingredient optionalIngredient(Ingredient ingredient) {
        return ingredient.isEmpty() ? null : ingredient;
    }

    public static void each3items(List<ItemStack> itemStackList, Consumer<List<ItemStack>> consumer) {
        for (int i = 0; i < itemStackList.size(); i += 3) {
            List<ItemStack> itemStackList1 = itemStackList.subList(i, Math.min(itemStackList.size(), i + 3));
            consumer.accept(itemStackList1);
        }
    }

    public static List<ItemStack> getIngredientItems(Ingredient ingredient) {
        return ingredient.items().map(holder -> holder.value().getDefaultInstance()).toList();
    }

    public static int getIngredientCount(Ingredient ingredient) {
        return 1;
    }
}
