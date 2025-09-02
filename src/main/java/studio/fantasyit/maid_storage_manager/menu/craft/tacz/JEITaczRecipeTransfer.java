package studio.fantasyit.maid_storage_manager.menu.craft.tacz;

import com.tacz.guns.crafting.GunSmithTableRecipe;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IUniversalRecipeTransferHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.craft.CraftManager;
import studio.fantasyit.maid_storage_manager.craft.context.special.TaczRecipeAction;
import studio.fantasyit.maid_storage_manager.craft.type.TaczType;
import studio.fantasyit.maid_storage_manager.network.CraftGuideGuiPacket;
import studio.fantasyit.maid_storage_manager.network.Network;
import studio.fantasyit.maid_storage_manager.registry.GuiRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JEITaczRecipeTransfer implements IUniversalRecipeTransferHandler<TaczCraftMenu> {
    @Override
    public Class<? extends TaczCraftMenu> getContainerClass() {
        return TaczCraftMenu.class;
    }

    @Override
    public Optional<MenuType<TaczCraftMenu>> getMenuType() {
        return Optional.of(GuiRegistry.CRAFT_GUIDE_MENU_TACZ.get());
    }

    @Override
    public @Nullable IRecipeTransferError transferRecipe(TaczCraftMenu container, Object _recipe, IRecipeSlotsView recipeSlots, Player player, boolean maxTransfer, boolean doTransfer) {
        if (_recipe instanceof GunSmithTableRecipe recipe) {
            List<Pair<ItemStack, String>> allRecipes = new ArrayList<>();
            container.getAllRecipes(allRecipes);
            if (allRecipes.stream().noneMatch(r -> r.getB().equals(recipe.getId().toString())))
                return new IncompatibleError();
            if (doTransfer) {
                Network.INSTANCE.sendToServer(new CraftGuideGuiPacket(
                        CraftGuideGuiPacket.Type.OPTION,
                        CraftManager.getInstance().getAction(TaczType.TYPE).getOptionIndex(TaczRecipeAction.OPTION_TACZ_RECIPE_ID),
                        0,
                        CraftGuideGuiPacket.singleValue(recipe.getId().toString())
                ));
                Network.INSTANCE.sendToServer(new CraftGuideGuiPacket(
                        CraftGuideGuiPacket.Type.OPTION,
                        CraftManager.getInstance().getAction(TaczType.TYPE).getOptionIndex(TaczRecipeAction.OPTION_TACZ_BLOCK_ID),
                        0,
                        CraftGuideGuiPacket.singleValue(container.getBlockId().toString())
                ));
            }
            return null;
        }
        return new IncompatibleError();
    }

    public static class IncompatibleError implements IRecipeTransferError {
        @Override
        public Type getType() {
            return Type.INTERNAL;
        }
    }
}
