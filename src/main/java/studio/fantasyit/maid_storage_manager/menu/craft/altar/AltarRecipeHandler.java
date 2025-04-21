package studio.fantasyit.maid_storage_manager.menu.craft.altar;

import com.github.tartaricacid.touhoulittlemaid.compat.jei.altar.AltarRecipeCategory;
import com.github.tartaricacid.touhoulittlemaid.compat.jei.altar.AltarRecipeWrapper;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.network.CraftGuideGuiPacket;
import studio.fantasyit.maid_storage_manager.network.Network;
import studio.fantasyit.maid_storage_manager.registry.GuiRegistry;

import java.util.Optional;

public class AltarRecipeHandler implements IRecipeTransferHandler<AltarCraftMenu, AltarRecipeWrapper> {

    @Override
    public @NotNull Class getContainerClass() {
        return AltarCraftMenu.class;
    }

    @Override
    public @NotNull Optional<MenuType<AltarCraftMenu>> getMenuType() {
        return Optional.of(GuiRegistry.CRAFT_GUIDE_MENU_ALTAR.get());
    }

    @Override
    public @NotNull RecipeType<AltarRecipeWrapper> getRecipeType() {
        return AltarRecipeCategory.ALTAR;
    }

    @Override
    public @Nullable IRecipeTransferError transferRecipe(AltarCraftMenu container, AltarRecipeWrapper recipe, IRecipeSlotsView recipeSlots, Player player, boolean maxTransfer, boolean doTransfer) {
        if (doTransfer) {
            ListTag inputs = new ListTag();
            recipe.getInputs()
                    .stream()
                    .map(l -> l.size() == 0 ? ItemStack.EMPTY : l.get(0))
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
