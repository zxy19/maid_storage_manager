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
import studio.fantasyit.maid_storage_manager.items.RequestListItem;
import studio.fantasyit.maid_storage_manager.items.data.TargetList;
import studio.fantasyit.maid_storage_manager.registry.DataComponentRegistry;
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

    public ListClearRecipe(String p_249640_, CraftingBookCategory p_249390_, ItemStack p_252071_, NonNullList<Ingredient> p_250689_) {
        super(p_249640_, p_249390_, p_252071_, p_250689_);
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
                tmp.set(DataComponentRegistry.TARGETS, new TargetList().toImmutable());
                return tmp;
            }
            if (stack.is(ItemRegistry.WRITTEN_INVENTORY_LIST.get())) {
                ItemStack tmp = ItemRegistry.INVENTORY_LIST.get().getDefaultInstance().copy();
                if (stack.has(DataComponentRegistry.INVENTORY_UUID)) {
                    tmp.set(DataComponentRegistry.INVENTORY_UUID, stack.get(DataComponentRegistry.INVENTORY_UUID));
                }
                return tmp;
            }
            if (stack.is(ItemRegistry.PROGRESS_PAD.get())) {
                ItemStack tmp = stack.copy();
                tmp.remove(DataComponentRegistry.PROGRESS_PAD_BINDING);
                return tmp;
            }
        }
        return this.getResultItem(p_267165_).copy();
    }


    public static class Serializer implements RecipeSerializer<ListClearRecipe> {
        private static final MapCodec<ListClearRecipe> CODEC = RecordCodecBuilder.mapCodec((p_340779_) ->
                p_340779_.group(Codec.STRING.optionalFieldOf("group", "").forGetter(ShapelessRecipe::getGroup),
                        CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(ShapelessRecipe::category),
                        ItemStack.STRICT_CODEC.fieldOf("result").forGetter((p_301142_) -> p_301142_.getResultItem(RegistryAccess.EMPTY)),
                        Ingredient.CODEC_NONEMPTY.listOf().fieldOf("ingredients")
                                .flatXmap((p_301021_) -> DataResult.success(NonNullList.of(Ingredient.EMPTY, p_301021_.toArray(Ingredient[]::new))), DataResult::success)
                                .forGetter(ShapelessRecipe::getIngredients)
                ).apply(p_340779_, ListClearRecipe::new));
        private static final StreamCodec<RegistryFriendlyByteBuf, ListClearRecipe> STREAM_CODEC = StreamCodec.composite(
                RecipeSerializer.SHAPELESS_RECIPE.streamCodec(),
                t -> t,
                ListClearRecipe::new
        );

        public MapCodec<ListClearRecipe> codec() {
            return CODEC;
        }

        public StreamCodec<RegistryFriendlyByteBuf, ListClearRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}