package studio.fantasyit.maid_storage_manager.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import studio.fantasyit.maid_storage_manager.api.IRequestTaskHandler;
import studio.fantasyit.maid_storage_manager.items.data.TargetList;
import studio.fantasyit.maid_storage_manager.registry.DataComponentRegistry;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;

import java.util.ArrayList;
import java.util.List;

public class ListClearRecipe extends ShapelessRecipe {
    public ListClearRecipe(ShapelessRecipe recipe) {
        super(
                new Recipe.CommonInfo(recipe.showNotification()),
                new CraftingRecipe.CraftingBookInfo(recipe.category(), recipe.group()),
                recipe.result(),
                new ArrayList<>(recipe.placementInfo().ingredients())
        );
    }

    public ListClearRecipe(String p_249640_, CraftingBookCategory p_249390_, ItemStack p_252071_, List<Ingredient> p_250689_) {
        super(
                new Recipe.CommonInfo(true),
                new CraftingRecipe.CraftingBookInfo(p_249390_, p_249640_),
                new ItemStackTemplate(p_252071_.getItem(), p_252071_.getCount(), p_252071_.getComponentsPatch()),
                p_250689_
        );
    }

    @Override
    public ItemStack assemble(CraftingInput inv) {
        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.is(ItemRegistry.REQUEST_LIST_ITEM.get())) {
                ItemStack tmp = stack.copy();
                IRequestTaskHandler handler = IRequestTaskHandler.of(tmp);
                if (handler != null) handler.clearItemProcess(tmp);
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
        return this.result().create().copy();
    }


    private static final MapCodec<ListClearRecipe> CODEC = RecordCodecBuilder.mapCodec((p_340779_) ->
            p_340779_.group(Codec.STRING.optionalFieldOf("group", "").forGetter(Recipe::group),
                    CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(CraftingRecipe::category),
                    ItemStack.CODEC.fieldOf("result").forGetter((r) -> r.result().create()),
                    Ingredient.CODEC.listOf().fieldOf("ingredients").forGetter((r) -> r.placementInfo().ingredients())
            ).apply(p_340779_, ListClearRecipe::new));

    private static final StreamCodec<RegistryFriendlyByteBuf, ListClearRecipe> STREAM_CODEC = StreamCodec.composite(
            ShapelessRecipe.STREAM_CODEC,
            t -> t,
            ListClearRecipe::new
    );

    public static final RecipeSerializer<ListClearRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);
}