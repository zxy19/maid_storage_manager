package studio.fantasyit.maid_storage_manager.integration.emi;

import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.EmiRecipeHandler;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;
import studio.fantasyit.maid_storage_manager.menu.ItemSelectorMenu;
import studio.fantasyit.maid_storage_manager.network.Network;
import studio.fantasyit.maid_storage_manager.util.InventoryListUtil;

import java.util.ArrayList;
import java.util.List;

public class RequestRecipeHandler implements EmiRecipeHandler<ItemSelectorMenu> {
    @Override
    public EmiPlayerInventory getInventory(AbstractContainerScreen<ItemSelectorMenu> screen) {
        return new EmiPlayerInventory(List.of());
    }
    @Override
    public boolean supportsRecipe(EmiRecipe recipe) {
        return true;
    }
    @Override
    public boolean canCraft(EmiRecipe recipe, EmiCraftContext<ItemSelectorMenu> context) {
        List<ItemStack> inputs = recipe.getInputs()
                .stream()
                .map(e -> InventoryListUtil
                        .getMatchingForPlayer(
                                e.getEmiStacks()
                                        .stream()
                                        .map(EmiStack::getItemStack)
                                        .filter(i -> !i.isEmpty())
                                        .toList()
                        )
                )
                .filter(i -> !i.isEmpty())
                .toList();
        return inputs.size() <= 10;
    }

    @Override
    public boolean craft(EmiRecipe recipe, EmiCraftContext<ItemSelectorMenu> context) {
        List<ItemStack> inputs = recipe.getInputs()
                .stream()
                .map(e -> InventoryListUtil
                        .getMatchingForPlayer(
                                e.getEmiStacks()
                                        .stream()
                                        .map(EmiStack::getItemStack)
                                        .filter(i -> !i.isEmpty())
                                        .toList()
                        )
                )
                .filter(i -> !i.isEmpty())
                .toList();
        List<Pair<Integer, ItemStack>> list = new ArrayList<>();

        for (int i = 0; i < Math.min(inputs.size(), 10); i++) {
            list.add(Pair.of(i, inputs.get(i)));
            context.getScreenHandler().filteredItems.setItem(i, inputs.get(i));
        }
        Network.sendItemSelectorSetItemPacket(list);
        return true;
    }
}
