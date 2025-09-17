package studio.fantasyit.tour_guide.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import studio.fantasyit.tour_guide.client.IGuiMarkRenderer;
import studio.fantasyit.tour_guide.mark.gui.GuiRectMark;

public class GuiRectMarkRenderer implements IGuiMarkRenderer<GuiRectMark> {
    @Override
    public void render(GuiGraphics graphics, GuiRectMark mark, Screen screen) {
        int thickness = 3;
        graphics.fill(mark.x(), mark.y(), mark.x() + thickness, mark.y() + mark.height(), mark.color());
        graphics.fill(mark.x(), mark.y(), mark.x() + mark.width(), mark.y() + thickness, mark.color());
        graphics.fill(mark.x() + mark.width() - thickness, mark.y(), mark.x() + mark.width(), mark.y() + mark.height(), mark.color());
        graphics.fill(mark.x(), mark.y() + mark.height() - thickness, mark.x() + mark.width(), mark.y() + mark.height(), mark.color());
        graphics.fill(mark.x() + thickness, mark.y() + thickness, mark.x() + mark.width() - thickness, mark.y() + mark.height() - thickness, mark.fill());
    }
}
