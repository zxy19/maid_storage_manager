package studio.fantasyit.maid_storage_manager.recipe;

import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.items.RequestListItem;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;

public class CopyConfigRecipe extends ShapelessRecipe {
    public CopyConfigRecipe(ShapelessRecipe recipe) {
        super(recipe.getId(),
                recipe.getGroup(),
                recipe.category(),
                recipe.getResultItem(RegistryAccess.EMPTY),
                recipe.getIngredients()
        );
    }

    protected @Nullable Pair<ItemStack, ItemStack> getToCopyItem(CraftingContainer inv) {
        ItemStack first = null;
        ItemStack second = null;
        for (int i = 0; i < inv.getContainerSize(); i++) {
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
    public @NotNull ItemStack assemble(@NotNull CraftingContainer inv, @NotNull RegistryAccess p_267165_) {
        Pair<ItemStack, ItemStack> toCopyItem = getToCopyItem(inv);
        if (toCopyItem != null) {
            return applyCopy(toCopyItem.getB().copy(), toCopyItem.getA());
        }
        return ItemStack.EMPTY;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer p_44004_) {
        NonNullList<ItemStack> nonnulllist = NonNullList.withSize(p_44004_.getContainerSize(), ItemStack.EMPTY);
        Pair<ItemStack, ItemStack> toCopyItem = getToCopyItem(p_44004_);
        if (toCopyItem == null)
            return nonnulllist;
        for (int i = 0; i < p_44004_.getContainerSize(); i++) {
            if (!p_44004_.getItem(i).isEmpty()) {
                ItemStack itemStack = p_44004_.getItem(i).copy();
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
            CompoundTag tag = toCopy.getOrCreateTag();
            newStack.setTag(tag.copy());
            return newStack;
        } else if (newStack.is(ItemRegistry.REQUEST_LIST_ITEM.get())) {
            if (!toCopy.is(ItemRegistry.REQUEST_LIST_ITEM.get()) || RequestListItem.isVirtual(toCopy) || RequestListItem.isVirtual(newStack)) {
                return ItemStack.EMPTY;
            }
            CompoundTag tag = toCopy.getOrCreateTag();
            newStack.setTag(tag.copy());
            RequestListItem.clearItemProcess(newStack);
            return newStack;
        } else if (newStack.is(ItemRegistry.CRAFT_GUIDE.get())) {
            if (toCopy.is(newStack.getItem())) {
                CompoundTag tag = toCopy.getOrCreateTag();
                newStack.setTag(tag.copy());
                return newStack;
            }
        }
        return ItemStack.EMPTY;
    }

    public static class Serializer implements RecipeSerializer<CopyConfigRecipe> {
        @Override
        public CopyConfigRecipe fromJson(ResourceLocation p_44103_, JsonObject p_44104_) {
            return new CopyConfigRecipe(RecipeSerializer.SHAPELESS_RECIPE.fromJson(p_44103_, p_44104_));
        }

        @Override
        public @Nullable CopyConfigRecipe fromNetwork(ResourceLocation p_44105_, FriendlyByteBuf p_44106_) {
            @Nullable ShapelessRecipe compose = RecipeSerializer.SHAPELESS_RECIPE.fromNetwork(p_44105_, p_44106_);
            return compose == null ? null : new CopyConfigRecipe(compose);
        }

        @Override
        public void toNetwork(FriendlyByteBuf p_44101_, CopyConfigRecipe p_44102_) {
            RecipeSerializer.SHAPELESS_RECIPE.toNetwork(p_44101_, p_44102_);
        }
    }
}