package studio.fantasyit.maid_storage_manager.recipe;

import com.google.gson.JsonObject;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.items.RequestListItem;
import studio.fantasyit.maid_storage_manager.items.StorageDefineBauble;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;

public class ListClearRecipe extends ShapelessRecipe {
    public ListClearRecipe(ShapelessRecipe recipe) {
        super(recipe.getId(),
                recipe.getGroup(),
                recipe.category(),
                recipe.getResultItem(RegistryAccess.EMPTY),
                recipe.getIngredients()
        );
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull CraftingContainer inv, @NotNull RegistryAccess p_267165_) {
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.is(ItemRegistry.REQUEST_LIST_ITEM.get())) {
                ItemStack tmp = stack.copy();
                RequestListItem.clearItemProcess(tmp);
                return tmp;
            }
            if (stack.is(ItemRegistry.STORAGE_DEFINE_BAUBLE.get())) {
                ItemStack tmp = stack.copy();
                CompoundTag tag = tmp.getOrCreateTag();
                tag.put(StorageDefineBauble.TAG_STORAGES, new ListTag());
                tmp.setTag(tag);
                return tmp;
            }
        }
        return this.getResultItem(p_267165_).copy();
    }


    public static class Serializer implements RecipeSerializer<ListClearRecipe> {
        @Override
        public ListClearRecipe fromJson(ResourceLocation p_44103_, JsonObject p_44104_) {
            return new ListClearRecipe(RecipeSerializer.SHAPELESS_RECIPE.fromJson(p_44103_, p_44104_));
        }

        @Override
        public @Nullable ListClearRecipe fromNetwork(ResourceLocation p_44105_, FriendlyByteBuf p_44106_) {
            @Nullable ShapelessRecipe compose = RecipeSerializer.SHAPELESS_RECIPE.fromNetwork(p_44105_, p_44106_);
            return compose == null ? null : new ListClearRecipe(compose);
        }

        @Override
        public void toNetwork(FriendlyByteBuf p_44101_, ListClearRecipe p_44102_) {
            RecipeSerializer.SHAPELESS_RECIPE.toNetwork(p_44101_, p_44102_);
        }
    }
}