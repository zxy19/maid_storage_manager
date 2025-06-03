package studio.fantasyit.maid_storage_manager.menu.craft.base.handler;

import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.menu.craft.base.AbstractCraftMenu;
import studio.fantasyit.maid_storage_manager.network.CraftGuideGuiPacket;
import studio.fantasyit.maid_storage_manager.network.Network;
import studio.fantasyit.maid_storage_manager.util.InventoryListUtil;

import java.util.List;

public class EmiRecipeHandler<C extends AbstractCraftMenu<?>, R> implements dev.emi.emi.api.recipe.handler.EmiRecipeHandler<C> {
    private final EmiRecipeCategory recipe;
    private final boolean keepEmpty;

    public EmiRecipeHandler(EmiRecipeCategory recipe, boolean keepEmpty) {
        this.recipe = recipe;
        this.keepEmpty = keepEmpty;
    }

    public EmiRecipeHandler(EmiRecipeCategory recipe) {
        this(recipe, true);
    }

    @Override
    public EmiPlayerInventory getInventory(AbstractContainerScreen<C> screen) {
        return new EmiPlayerInventory(List.of());
    }

    @Override
    public boolean supportsRecipe(EmiRecipe recipe) {
        return this.recipe.id.equals(recipe.getCategory().id);
    }


    @Override
    public boolean canCraft(EmiRecipe recipe, EmiCraftContext<C> context) {
        return recipe.getCategory().id.equals(recipe.getCategory().id);
    }

    @Override
    public boolean craft(EmiRecipe recipe, EmiCraftContext<C> context) {
        ListTag inputs = new ListTag();
        recipe.getInputs()
                .stream()
                .map(EmiIngredient::getEmiStacks)
                .map(l -> l.size() == 0 ? ItemStack.EMPTY : InventoryListUtil.getMatchingForPlayer(l.stream().map(EmiStack::getItemStack).toList()))
                .filter(t -> !t.isEmpty() || keepEmpty)
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
        return true;
    }
}
