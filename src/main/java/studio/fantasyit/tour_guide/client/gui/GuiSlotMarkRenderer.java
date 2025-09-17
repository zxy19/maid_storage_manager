package studio.fantasyit.tour_guide.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import studio.fantasyit.tour_guide.client.IGuiMarkRenderer;
import studio.fantasyit.tour_guide.mark.gui.GuiSlotMark;

public class GuiSlotMarkRenderer implements IGuiMarkRenderer<GuiSlotMark> {
    @Override
    public void render(GuiGraphics graphics, GuiSlotMark mark, Screen screen) {
        if (screen instanceof AbstractContainerScreen<?> containerScreen && containerScreen.getMenu().slots.size() > mark.id()) {
            Slot slot = containerScreen.getMenu().getSlot(mark.id());
            int thickness = 3;
            graphics.fill(slot.x, slot.y, slot.x + thickness, slot.y + 18, mark.color());
            graphics.fill(slot.x, slot.y, slot.x + 18, slot.y + thickness, mark.color());
            graphics.fill(slot.x + 18 - thickness, slot.y, slot.x + 18, slot.y + 18, mark.color());
            graphics.fill(slot.x, slot.y + 18 - thickness, slot.x + 18, slot.y + 18, mark.color());
        }
    }
}
