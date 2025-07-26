package studio.fantasyit.maid_storage_manager.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.items.RequestListItem;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;

public class CopyConfigRecipe extends ShapelessRecipe {
    public CopyConfigRecipe(ShapelessRecipe recipe) {
        super(
                recipe.getGroup(),
                recipe.category(),
                recipe.getResultItem(RegistryAccess.EMPTY),
                recipe.getIngredients()
        );
    }

    public CopyConfigRecipe(String p_249640_, CraftingBookCategory p_249390_, ItemStack p_252071_, NonNullList<Ingredient> p_250689_) {
        super(p_249640_, p_249390_, p_252071_, p_250689_);
    }


    protected @Nullable Pair<ItemStack, ItemStack> getToCopyItem(CraftingInput inv) {
        ItemStack first = null;
        ItemStack second = null;
        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty()) {
                if (first == null) {
                    first = stack;
                } else if (second == null) {
                    second = stack;
                } else return null;
            }
        }
        if (first == null || second == null) return null;
        return new Pair<>(first, second);
    }

    @Override
    public ItemStack assemble(CraftingInput inv, HolderLookup.Provider p_335725_) {
        Pair<ItemStack, ItemStack> toCopyItem = getToCopyItem(inv);
        if (toCopyItem != null) {
            return applyCopy(toCopyItem.getB().copy(), toCopyItem.getA());
        }
        return ItemStack.EMPTY;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> nonnulllist = NonNullList.withSize(input.size(), ItemStack.EMPTY);
        Pair<ItemStack, ItemStack> toCopyItem = getToCopyItem(input);
        if (toCopyItem == null)
            return nonnulllist;
        for (int i = 0; i < input.size(); i++) {
            if (!input.getItem(i).isEmpty()) {
                ItemStack itemStack = input.getItem(i).copy();
                nonnulllist.set(i, itemStack);
                break;
            }
        }
        return nonnulllist;
    }

    public ItemStack applyCopy(ItemStack newStack, ItemStack toCopy) {
        if (
                newStack.is(ItemRegistry.STORAGE_DEFINE_BAUBLE.get()) ||
                        newStack.is(ItemRegistry.FILTER_LIST.get()) ||
                        newStack.is(ItemRegistry.CHANGE_FLAG.get())
        ) {
            if (!newStack.is(toCopy.getItem())) return ItemStack.EMPTY;
            return toCopy.copy();
        } else if (newStack.is(ItemRegistry.REQUEST_LIST_ITEM.get())) {
            if (!toCopy.is(ItemRegistry.REQUEST_LIST_ITEM.get()) || RequestListItem.isVirtual(toCopy) || RequestListItem.isVirtual(newStack)) {
                return ItemStack.EMPTY;
            }
            newStack = toCopy.copy();
            RequestListItem.clearItemProcess(newStack);
            return newStack;
        } else if (newStack.is(ItemRegistry.CRAFT_GUIDE.get())) {
            if (toCopy.is(newStack.getItem())) {
                return toCopy.copy();
            }
        }
        return ItemStack.EMPTY;
    }

    public static class Serializer implements RecipeSerializer<CopyConfigRecipe> {
        private static final MapCodec<CopyConfigRecipe> CODEC = RecordCodecBuilder.mapCodec((p_340779_) ->
                p_340779_.group(Codec.STRING.optionalFieldOf("group", "").forGetter(ShapelessRecipe::getGroup),
                        CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(ShapelessRecipe::category),
                        ItemStack.STRICT_CODEC.fieldOf("result").forGetter((p_301142_) -> p_301142_.getResultItem(RegistryAccess.EMPTY)),
                        Ingredient.CODEC_NONEMPTY.listOf().fieldOf("ingredients")
                                .flatXmap((p_301021_) -> DataResult.success(NonNullList.of(Ingredient.EMPTY, p_301021_.toArray(Ingredient[]::new))), DataResult::success)
                                .forGetter(ShapelessRecipe::getIngredients)
                ).apply(p_340779_, CopyConfigRecipe::new));
        private static final StreamCodec<RegistryFriendlyByteBuf, CopyConfigRecipe> STREAM_CODEC = StreamCodec.composite(
                RecipeSerializer.SHAPELESS_RECIPE.streamCodec(),
                t -> t,
                CopyConfigRecipe::new
        );

        @Override
        public @NotNull MapCodec<CopyConfigRecipe> codec() {
            return CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, CopyConfigRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}