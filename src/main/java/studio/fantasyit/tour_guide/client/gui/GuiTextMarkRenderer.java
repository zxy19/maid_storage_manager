package studio.fantasyit.tour_guide.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.tour_guide.client.IGuiMarkRenderer;
import studio.fantasyit.tour_guide.mark.gui.GuiTextMark;

public class GuiTextMarkRenderer implements IGuiMarkRenderer<GuiTextMark> {
    @Override
    public void render(GuiGraphics graphics, GuiTextMark mark, @Nullable Screen screen) {
        int height = Minecraft.getInstance().font.split(mark.text(), mark.width()).size() * (Minecraft.getInstance().font.lineHeight + 2);
        graphics.fill(mark.x() - 2, mark.y() - 2, mark.x() + mark.width() + 2, mark.y() + height + 2, mark.background());
        graphics.drawWordWrap(Minecraft.getInstance().font, mark.text(), mark.x(), mark.y(), mark.width(), mark.color());
    }
}
