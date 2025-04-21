package studio.fantasyit.maid_storage_manager.menu.craft.common;

import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IUniversalRecipeTransferHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.registry.GuiRegistry;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CommonCraftRecipeHandler implements IUniversalRecipeTransferHandler<CommonCraftMenu> {

    @Override
    public @NotNull Class getContainerClass() {
        return CommonCraftMenu.class;
    }

    @Override
    public @NotNull Optional<MenuType<CommonCraftMenu>> getMenuType() {
        return Optional.of(GuiRegistry.CRAFT_GUIDE_MENU_COMMON.get());
    }

    @Override
    public @Nullable IRecipeTransferError transferRecipe(@NotNull CommonCraftMenu container,
                                                         @NotNull Object recipe,
                                                         IRecipeSlotsView recipeSlots,
                                                         @NotNull Player player,
                                                         boolean maxTransfer,
                                                         boolean doTransfer) {
        List<ItemStack> outputs = new ArrayList<>();
        recipeSlots.getSlotViews(RecipeIngredientRole.OUTPUT)
                .stream()
                .map(e -> e.getItemStacks().findFirst().orElse(ItemStack.EMPTY))
                .filter(i -> !i.isEmpty())
                .forEach(i -> ItemStackUtil.addToList(outputs, i, true));

        List<ItemStack> inputs = new ArrayList<>();
        recipeSlots.getSlotViews(RecipeIngredientRole.INPUT)
                .stream()
                .map(e -> e.getItemStacks().findFirst().orElse(ItemStack.EMPTY))
                .filter(i -> !i.isEmpty())
                .forEach(i -> ItemStackUtil.addToList(inputs, i, true));

        int inputId = 0;
        int outputId = 0;
        for (CommonStepDataContainer step : container.steps) {
            for (int i = 0; i < step.step.actionType.inputCount(); i++) {
                if (inputId < inputs.size()) {
                    if (doTransfer)
                        step.setItem(i, inputs.get(inputId));
                    inputId++;
                }
            }
            for (int i = 0; i < step.step.actionType.outputCount(); i++) {
                if (outputId < outputs.size()) {
                    if (doTransfer)
                        step.setItem(i + step.step.actionType.inputCount(), outputs.get(outputId));
                    outputId++;
                }
            }
        }
        if (inputId != inputs.size() || outputId != outputs.size()) {
            return new ExceedError();
        }
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
