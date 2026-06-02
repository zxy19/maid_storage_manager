package studio.fantasyit.maid_storage_manager.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.api.IRequestTaskHandler;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;

import java.util.ArrayList;
import java.util.List;

public class CopyConfigRecipe extends ShapelessRecipe {
    public CopyConfigRecipe(ShapelessRecipe recipe) {
        super(
                new Recipe.CommonInfo(recipe.showNotification()),
                new CraftingRecipe.CraftingBookInfo(recipe.category(), recipe.group()),
                recipe.result(),
                new ArrayList<>(recipe.placementInfo().ingredients())
        );
    }

    public CopyConfigRecipe(String p_249640_, CraftingBookCategory p_249390_, ItemStackTemplate p_252071_, List<Ingredient> p_250689_) {
        super(
                new Recipe.CommonInfo(true),
                new CraftingRecipe.CraftingBookInfo(p_249390_, p_249640_),
                p_252071_,
                p_250689_
        );
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
    public ItemStack assemble(CraftingInput inv) {
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
            IRequestTaskHandler toCopyHandler = IRequestTaskHandler.of(toCopy);
            IRequestTaskHandler newStackHandler = IRequestTaskHandler.of(newStack);
            if (toCopyHandler == null || newStackHandler == null ||
                    toCopyHandler.isVirtual(toCopy) || newStackHandler.isVirtual(newStack)) {
                return ItemStack.EMPTY;
            }
            newStack = toCopy.copy();
            newStackHandler.clearItemProcess(newStack);
            return newStack;
        } else if (newStack.is(ItemRegistry.CRAFT_GUIDE.get())) {
            if (toCopy.is(newStack.getItem())) {
                return toCopy.copy();
            }
        }
        return ItemStack.EMPTY;
    }

    private static final MapCodec<CopyConfigRecipe> CODEC = RecordCodecBuilder.mapCodec((p_340779_) ->
            p_340779_.group(Codec.STRING.optionalFieldOf("group", "").forGetter(Recipe::group),
                    CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(CraftingRecipe::category),
                    ItemStackTemplate.CODEC.fieldOf("result").forGetter((r) -> r.result()),
                    Ingredient.CODEC.listOf().fieldOf("ingredients").forGetter((r) -> r.placementInfo().ingredients())
            ).apply(p_340779_, CopyConfigRecipe::new));

    private static final StreamCodec<RegistryFriendlyByteBuf, CopyConfigRecipe> STREAM_CODEC = StreamCodec.composite(
            ShapelessRecipe.STREAM_CODEC,
            t -> t,
            CopyConfigRecipe::new
    );

    @Override
    @SuppressWarnings("unchecked")
    public RecipeSerializer<ShapelessRecipe> getSerializer() {
        return (RecipeSerializer<ShapelessRecipe>) (Object) SERIALIZER;
    }

    public static final RecipeSerializer<CopyConfigRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);
}