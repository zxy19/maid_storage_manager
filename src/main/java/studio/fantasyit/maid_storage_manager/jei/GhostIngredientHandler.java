package studio.fantasyit.maid_storage_manager.jei;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import studio.fantasyit.maid_storage_manager.menu.ItemSelectorMenu;
import studio.fantasyit.maid_storage_manager.menu.ItemSelectorScreen;

import java.util.ArrayList;
import java.util.List;

import static studio.fantasyit.maid_storage_manager.network.Network.sendItemSelectorSetItemPacket;

public class GhostIngredientHandler implements IGhostIngredientHandler<ItemSelectorScreen> {

    @Override
    public <I> @NotNull List<Target<I>> getTargetsTyped(ItemSelectorScreen gui, ITypedIngredient<I> ingredient, boolean doStart) {
        if (!(ingredient.getType() == VanillaTypes.ITEM_STACK))
            return List.of();
        List<Target<I>> result = new ArrayList<>();
        for (Slot slot : gui.getMenu().slots) {
            if (slot instanceof ItemSelectorMenu.FilterSlot ifs) {
                result.add(new GhostTarget<>(gui, ifs));
            }
        }
        return result;
    }

    @Override
    public void onComplete() {

    }

    private static class GhostTarget<I> implements Target<I> {

        private final Rect2i area;
        private final ItemSelectorScreen gui;
        private final int slotIndex;

        public GhostTarget(ItemSelectorScreen gui, Slot slot) {
            this.gui = gui;
            this.slotIndex = slot.getSlotIndex();
            this.area = new Rect2i(gui.getGuiLeft() + slot.x, gui.getGuiTop() + slot.y, 16, 16);
        }

        @Override
        public @NotNull Rect2i getArea() {
            return area;
        }

        @Override
        public void accept(@NotNull I ingredient) {
            ItemStack stack = ((ItemStack) ingredient).copy();
            stack.setCount(1);
            gui.getMenu().filteredItems.setItem(slotIndex, stack);
            sendItemSelectorSetItemPacket(slotIndex, stack);
        }
    }
}
