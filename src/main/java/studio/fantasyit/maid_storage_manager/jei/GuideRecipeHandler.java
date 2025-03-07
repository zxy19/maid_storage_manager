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
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.menu.CraftGuideMenu;
import studio.fantasyit.maid_storage_manager.menu.ItemSelectorMenu;
import studio.fantasyit.maid_storage_manager.network.Network;
import studio.fantasyit.maid_storage_manager.registry.GuiRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GuideRecipeHandler implements IUniversalRecipeTransferHandler<CraftGuideMenu> {

    @Override
    public @NotNull Class getContainerClass() {
        return CraftGuideMenu.class;
    }

    @Override
    public @NotNull Optional<MenuType<CraftGuideMenu>> getMenuType() {
        return Optional.of(GuiRegistry.CRAFT_GUIDE_MENU.get());
    }

    @Override
    public @Nullable IRecipeTransferError transferRecipe(@NotNull CraftGuideMenu container,
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

        if (!container.inputSlot1.blockItem.isEmpty()) {
            for (int i = 0; i < Math.min(inputs.size(), 9); i++) {
                list.add(Pair.of(container.inputSlot1.slotIds.get(i), inputs.get(i)));
                if (doTransfer) {
                    container.inputSlot1.items.setItem(i, inputs.get(i));
                    container.inputSlot1.items.count[i].setValue(inputs.get(i).getCount());
                }
            }
            if (!container.outputSlot.blockItem.isEmpty())
                for (int ii = 9; ii < Math.min(inputs.size(), 18); ii++) {
                    int i = ii - 9;
                    list.add(Pair.of(container.inputSlot2.slotIds.get(i), inputs.get(ii)));
                    if (doTransfer) {
                        container.inputSlot2.items.setItem(i, inputs.get(ii));
                        container.inputSlot2.items.count[i].setValue(inputs.get(ii).getCount());
                    }
                }
        }
        if (!container.outputSlot.blockItem.isEmpty()
                || container.inputSlot1.blockItem.getItem(0).is(Items.CRAFTING_TABLE.asItem())
        ) {
            for (int i = 0; i < Math.min(outputs.size(), 3); i++) {
                list.add(Pair.of(container.outputSlot.slotIds.get(i), outputs.get(i)));
                if (doTransfer) {
                    container.outputSlot.items.setItem(i, outputs.get(i));
                    container.outputSlot.items.count[i].setValue(outputs.get(i).getCount());
                }
            }
        }
        if (doTransfer)
            Network.sendItemSelectorSetItemPacket(list);

        if (inputs.size() > 18 || outputs.size() > 3)
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
