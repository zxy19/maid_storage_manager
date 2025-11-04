package studio.fantasyit.maid_storage_manager.integration.jei;

import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IUniversalRecipeTransferHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.menu.request.ItemSelectorMenu;
import studio.fantasyit.maid_storage_manager.network.Network;
import studio.fantasyit.maid_storage_manager.registry.GuiRegistry;
import studio.fantasyit.maid_storage_manager.util.InventoryListUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RequestRecipeHandler implements IUniversalRecipeTransferHandler<ItemSelectorMenu> {

    @Override
    public @NotNull Class getContainerClass() {
        return ItemSelectorMenu.class;
    }

    @Override
    public @NotNull Optional<MenuType<ItemSelectorMenu>> getMenuType() {
        return Optional.of(GuiRegistry.ITEM_SELECTOR_MENU.get());
    }

    @Override
    public @Nullable IRecipeTransferError transferRecipe(@NotNull ItemSelectorMenu container,
                                                         @NotNull Object recipe,
                                                         IRecipeSlotsView recipeSlots,
                                                         @NotNull Player player,
                                                         boolean maxTransfer,
                                                         boolean doTransfer) {
        List<ItemStack> inputs = recipeSlots.getSlotViews(RecipeIngredientRole.INPUT)
                .stream()
                .map(e -> InventoryListUtil.getMatchingForPlayer(e.getItemStacks().toList()))
                .filter(i -> !i.isEmpty())
                .toList();
        List<Pair<Integer, ItemStack>> list = new ArrayList<>();

        for (int i = 0; i < Math.min(inputs.size(), 10); i++) {
            list.add(Pair.of(i, inputs.get(i)));
            if (doTransfer)
                container.filteredItems.setItem(i, inputs.get(i));
        }
        if (doTransfer)
            Network.sendItemSelectorSetItemPacket(list);

        if (inputs.size() > 10)
            return new ExceedError();

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
