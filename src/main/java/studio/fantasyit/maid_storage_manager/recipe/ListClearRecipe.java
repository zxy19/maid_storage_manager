package studio.fantasyit.maid_storage_manager.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import studio.fantasyit.maid_storage_manager.items.RequestListItem;
import studio.fantasyit.maid_storage_manager.items.StorageDefineBauble;
import studio.fantasyit.maid_storage_manager.items.WrittenInvListItem;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;

public class ListClearRecipe extends ShapelessRecipe {
    public ListClearRecipe(ShapelessRecipe recipe) {
        super(
                recipe.getGroup(),
                recipe.category(),
                recipe.getResultItem(RegistryAccess.EMPTY),
                recipe.getIngredients()
        );
    }

    @Override
    public ItemStack assemble(CraftingInput inv, HolderLookup.Provider p_267165_) {
        //TODO
        for (int i = 0; i < inv.size(); i++) {
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
            if (stack.is(ItemRegistry.WRITTEN_INVENTORY_LIST.get())) {
                if (!stack.hasTag()) return stack;
                ItemStack tmp = ItemRegistry.INVENTORY_LIST.get().getDefaultInstance().copy();
                if (stack.getTag().contains(WrittenInvListItem.TAG_UUID)) {
                    CompoundTag tag = tmp.getOrCreateTag();
                    tag.putUUID(WrittenInvListItem.TAG_UUID, stack.getTag().getUUID(WrittenInvListItem.TAG_UUID));
                    tmp.setTag(tag);
                }
                return tmp;
            }
        }
        return this.getResultItem(p_267165_).copy();
    }


    public static class Serializer implements RecipeSerializer<ListClearRecipe> {

        @Override
        public MapCodec<ListClearRecipe> codec() {
            return RecordCodecBuilder.mapCodec(builder -> builder.group(
                            RecipeSerializer.SHAPELESS_RECIPE.codec().fieldOf("recipe").forGetter(t -> t)
                    ).apply(builder, ListClearRecipe::new)
            );
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ListClearRecipe> streamCodec() {
            return StreamCodec.composite(
                    RecipeSerializer.SHAPELESS_RECIPE.streamCodec(),
                    t -> t,
                    ListClearRecipe::new
            );
        }
    }
}