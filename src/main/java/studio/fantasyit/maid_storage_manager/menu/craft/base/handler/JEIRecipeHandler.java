package studio.fantasyit.maid_storage_manager.menu.craft.base.handler;

import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.network.CraftGuideGuiPacket;
import studio.fantasyit.maid_storage_manager.util.InventoryListUtil;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;

import java.util.Optional;

public class JEIRecipeHandler<C extends AbstractContainerMenu, R> implements IRecipeTransferHandler<C, R> {
    private final Class<C> containerClass;
    private final MenuType<C> menuType;
    private final RecipeType<R> recipeType;
    private final boolean keepEmpty;

    public JEIRecipeHandler(Class<C> containerClass, RecipeType<R> recipeType, MenuType<C> menuType, boolean keepEmpty) {
        this.containerClass = containerClass;
        this.menuType = menuType;
        this.recipeType = recipeType;
        this.keepEmpty = keepEmpty;
    }

    public JEIRecipeHandler(Class<C> containerClass, RecipeType<R> recipeType, MenuType<C> menuType) {
        this(containerClass, recipeType, menuType, true);
    }

    @Override
    public Class<C> getContainerClass() {
        return containerClass;
    }

    @Override
    public Optional<MenuType<C>> getMenuType() {
        return Optional.of(this.menuType);
    }

    @Override
    public RecipeType<R> getRecipeType() {
        return recipeType;
    }

    @Override
    public @Nullable IRecipeTransferError transferRecipe(C container, R recipe, IRecipeSlotsView recipeSlots, Player player, boolean maxTransfer, boolean doTransfer) {
        if (doTransfer) {
            ListTag inputs = new ListTag();
            recipeSlots.getSlotViews(RecipeIngredientRole.INPUT)
                    .stream()
                    .map(IRecipeSlotView::getItemStacks)
                    .map(l -> InventoryListUtil.getMatchingForPlayer(l.toList()))
                    .filter(t -> !t.isEmpty() || keepEmpty)
                    .map(t -> ItemStackUtil.saveStack(player.registryAccess(),t))
                    .forEach(inputs::add);
            CompoundTag data = new CompoundTag();
            data.put("inputs", inputs);
            PacketDistributor.sendToServer(new CraftGuideGuiPacket(
                    CraftGuideGuiPacket.Type.SET_ALL_INPUT,
                    0,
                    0,
                    data
            ));
        }
        return null;
    }
}
