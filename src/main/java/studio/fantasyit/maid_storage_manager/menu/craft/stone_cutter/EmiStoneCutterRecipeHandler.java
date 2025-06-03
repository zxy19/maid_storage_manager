package studio.fantasyit.maid_storage_manager.menu.craft.stone_cutter;

import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.EmiRecipeHandler;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.network.CraftGuideGuiPacket;
import studio.fantasyit.maid_storage_manager.network.Network;
import studio.fantasyit.maid_storage_manager.util.InventoryListUtil;

import java.util.List;

public class EmiStoneCutterRecipeHandler implements EmiRecipeHandler<StoneCutterCraftMenu> {
    @Override
    public EmiPlayerInventory getInventory(AbstractContainerScreen<StoneCutterCraftMenu> screen) {
        return new EmiPlayerInventory(List.of());
    }

    @Override
    public boolean supportsRecipe(EmiRecipe recipe) {
        return VanillaEmiRecipeCategories.STONECUTTING.id.equals(recipe.getCategory().id);
    }


    @Override
    public boolean canCraft(EmiRecipe recipe, EmiCraftContext<StoneCutterCraftMenu> context) {
        return recipe.getCategory().id.equals(recipe.getCategory().id);
    }

    @Override
    public boolean craft(EmiRecipe recipe, EmiCraftContext<StoneCutterCraftMenu> context) {
        ListTag inputs = new ListTag();
        recipe.getInputs()
                .stream()
                .map(EmiIngredient::getEmiStacks)
                .map(l -> l.size() == 0 ? ItemStack.EMPTY : InventoryListUtil.getMatchingForPlayer(l.stream().map(EmiStack::getItemStack).toList()))
                .map(t -> t.save(new CompoundTag()))
                .forEach(inputs::add);
        recipe.getOutputs()
                .stream()
                .map(EmiIngredient::getEmiStacks)
                .map(l -> l.size() == 0 ? ItemStack.EMPTY : InventoryListUtil.getMatchingForPlayer(l.stream().map(EmiStack::getItemStack).toList()))
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
