package studio.fantasyit.tour_guide.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

public interface IGuiMarkRenderer<T> {
    void render(GuiGraphics graphics, T mark, Screen screen);
}
