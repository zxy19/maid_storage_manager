package studio.fantasyit.maid_storage_manager.jei;

import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IUniversalRecipeTransferHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.menu.craft.common.CommonCraftMenu;
import studio.fantasyit.maid_storage_manager.registry.GuiRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CommonGuideRecipeHandler implements IUniversalRecipeTransferHandler<CommonCraftMenu> {

    @Override
    public @NotNull Class getContainerClass() {
        return CommonCraftMenu.class;
    }

    @Override
    public @NotNull Optional<MenuType<CommonCraftMenu>> getMenuType() {
        return Optional.of(GuiRegistry.CRAFT_GUIDE_MENU.get());
    }

    @Override
    public @Nullable IRecipeTransferError transferRecipe(@NotNull CommonCraftMenu container,
                                                         @NotNull Object recipe,
                                                         IRecipeSlotsView recipeSlots,
                                                         @NotNull Player player,
                                                         boolean maxTransfer,
                                                         boolean doTransfer) {
        List<ItemStack> outputs = recipeSlots.getSlotViews(RecipeIngredientRole.OUTPUT)
                .stream()
                .map(e -> e.getItemStacks().findFirst().orElse(ItemStack.EMPTY))
                .filter(i -> !i.isEmpty())
                .toList();
        List<ItemStack> inputs = recipeSlots.getSlotViews(RecipeIngredientRole.INPUT)
                .stream()
                .map(e -> e.getItemStacks().findFirst().orElse(ItemStack.EMPTY))
                .toList();
        if (!(recipe instanceof CraftingRecipe)) {
            inputs = inputs
                    .stream()
                    .filter(i -> !i.isEmpty())
                    .toList();
        }
        List<Pair<Integer, ItemStack>> list = new ArrayList<>();

        //TODO 重写配方转移
//        if (doTransfer)
//            Network.sendItemSelectorSetItemPacket(list);
//
//        if (inputs.size() > 18 || outputs.size() > 3)
//            return new ExceedError();

        return null;
    }

    public static class ExceedError implements IRecipeTransferError {
        @Override
        public Type getType() {
            return Type.COSMETIC;
        }

        @Override
        public void getTooltip(ITooltipBuilder tooltip) {
            tooltip.add(Component.translatable("tooltip.maid_storage_manager.request_list.jei.too_many_to_transfer"));
        }
    }
}
