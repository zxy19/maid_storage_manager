package studio.fantasyit.maid_storage_manager.integration.jei;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import studio.fantasyit.maid_storage_manager.menu.AbstractFilterScreen;
import studio.fantasyit.maid_storage_manager.menu.container.FilterSlot;

import java.util.ArrayList;
import java.util.List;

public class GhostIngredientHandler implements IGhostIngredientHandler<AbstractFilterScreen> {

    @Override
    public <I> @NotNull List<Target<I>> getTargetsTyped(AbstractFilterScreen gui, ITypedIngredient<I> ingredient, boolean doStart) {
        if (!(ingredient.getType() == VanillaTypes.ITEM_STACK))
            return List.of();
        List<Target<I>> result = new ArrayList<>();
        List<FilterSlot> slots = gui.getSlots();
        for (FilterSlot slot : slots) {
            result.add(new GhostTarget<>(gui, slot));
        }
        return result;
    }

    @Override
    public void onComplete() {

    }

    private static class GhostTarget<I> implements Target<I> {

        private final Rect2i area;
        private final AbstractFilterScreen<?> gui;
        private final FilterSlot slot;

        public GhostTarget(AbstractFilterScreen<?> gui, FilterSlot slot) {
            this.gui = gui;
            this.slot = slot;
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
            gui.accept(slot, stack);
        }
    }
}
