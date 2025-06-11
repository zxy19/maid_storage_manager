package studio.fantasyit.maid_storage_manager.integration.emi;

import dev.emi.emi.api.EmiDragDropHandler;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.menu.base.AbstractFilterScreen;
import studio.fantasyit.maid_storage_manager.menu.container.FilterSlot;

import java.util.Optional;

public class GhostIngredientHandler implements EmiDragDropHandler<Screen> {
    @Override
    public boolean dropStack(Screen _screen, EmiIngredient stack, int x, int y) {
        if(_screen instanceof AbstractFilterScreen<?>  screen) {
            int inGuiX = x - screen.getGuiLeft();
            int inGuiY = y - screen.getGuiTop();
            Optional<FilterSlot> child = screen
                    .getSlots()
                    .stream()
                    .filter(s -> s.x <= inGuiX && s.y <= inGuiY && s.x >= inGuiX - 16 && s.y >= inGuiY - 16)
                    .findFirst();
            if (child.isEmpty()) {
                return false;
            }
            FilterSlot filterSlot = child.get();
            Optional<ItemStack> first = stack.getEmiStacks().stream().map(EmiStack::getItemStack).filter(i -> !i.isEmpty()).findFirst();
            if (first.isEmpty()) {
                return false;
            }
            if (!filterSlot.readonly) {
                screen.accept(filterSlot, first.get());
                return true;
            }
        }
        return false;
    }
}
