package studio.fantasyit.maid_storage_manager.menu.craft.smithing;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SmithingRecipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.network.CraftGuideGuiPacket;
import studio.fantasyit.maid_storage_manager.network.Network;
import studio.fantasyit.maid_storage_manager.registry.GuiRegistry;

import java.util.Optional;

public class SmithingRecipeHandler implements IRecipeTransferHandler<SmithingCraftMenu, SmithingRecipe> {

    @Override
    public @NotNull Class getContainerClass() {
        return SmithingCraftMenu.class;
    }

    @Override
    public @NotNull Optional<MenuType<SmithingCraftMenu>> getMenuType() {
        return Optional.of(GuiRegistry.CRAFT_GUIDE_MENU_SMITHING.get());
    }

    @Override
    public @NotNull RecipeType<SmithingRecipe> getRecipeType() {
        return RecipeTypes.SMITHING;
    }

    @Override
    public @Nullable IRecipeTransferError transferRecipe(SmithingCraftMenu container, SmithingRecipe recipe, IRecipeSlotsView recipeSlots, Player player, boolean maxTransfer, boolean doTransfer) {
        if (doTransfer) {
            ListTag inputs = new ListTag();
            recipeSlots.getSlotViews(RecipeIngredientRole.INPUT)
                    .stream()
                    .map(IRecipeSlotView::getItemStacks)
                    .map(l -> l.findFirst().orElse(ItemStack.EMPTY))
                    .map(t -> t.save(new CompoundTag()))
                    .forEach(inputs::add);
            CompoundTag data = new CompoundTag();
            data.put("inputs", inputs);
            Network.INSTANCE.sendToServer(new CraftGuideGuiPacket(
                    CraftGuideGuiPacket.Type.SET_ALL_INPUT,
                    0,
                    0,
                    data
            ));
        }
        return null;
    }
}
