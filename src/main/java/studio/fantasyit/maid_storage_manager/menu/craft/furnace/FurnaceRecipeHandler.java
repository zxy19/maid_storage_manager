package studio.fantasyit.maid_storage_manager.menu.craft.furnace;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.network.CraftGuideGuiPacket;
import studio.fantasyit.maid_storage_manager.network.Network;
import studio.fantasyit.maid_storage_manager.registry.GuiRegistry;

import java.util.Optional;

public class FurnaceRecipeHandler implements IRecipeTransferHandler<FurnaceCraftMenu, SmeltingRecipe> {

    @Override
    public @NotNull Class getContainerClass() {
        return FurnaceCraftMenu.class;
    }

    @Override
    public @NotNull Optional<MenuType<FurnaceCraftMenu>> getMenuType() {
        return Optional.of(GuiRegistry.CRAFT_GUIDE_MENU_FURNACE.get());
    }

    @Override
    public @NotNull RecipeType<SmeltingRecipe> getRecipeType() {
        return RecipeTypes.SMELTING;
    }

    @Override
    public @Nullable IRecipeTransferError transferRecipe(FurnaceCraftMenu container, SmeltingRecipe recipe, IRecipeSlotsView recipeSlots, Player player, boolean maxTransfer, boolean doTransfer) {
        if (doTransfer) {
            ListTag inputs = new ListTag();
            recipe.getIngredients()
                    .stream()
                    .map(Ingredient::getItems)
                    .map(l -> l.length == 0 ? ItemStack.EMPTY : l[0])
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
