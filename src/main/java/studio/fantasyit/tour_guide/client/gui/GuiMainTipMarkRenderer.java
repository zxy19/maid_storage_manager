package studio.fantasyit.tour_guide.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import studio.fantasyit.tour_guide.client.IGuiMarkRenderer;
import studio.fantasyit.tour_guide.client.event.ClientInputEvent;
import studio.fantasyit.tour_guide.mark.gui.GuiMainTipMark;

public class GuiMainTipMarkRenderer implements IGuiMarkRenderer<GuiMainTipMark> {
    private static final int COLOR = 0x60666666;
    private static final int FILL = 0x60000000;
    private static final int thickness = 3;
    private static final int x = 5;
    private static final int y = 5;
    private static final int _width = 160;

    @Override
    public void render(GuiGraphics graphics, GuiMainTipMark mark, Screen screen) {
        Component keyTip = mark.canSkip() ?
                Component.translatable("gui.tour_guide.key_tip",
                        ClientInputEvent.KEY_CHECK_STEP.get().getKey().getDisplayName(),
                        ClientInputEvent.KEY_SKIP.get().getKey().getDisplayName(),
                        ClientInputEvent.KEY_QUIT.get().getKey().getDisplayName()
                ) :
                Component.translatable("gui.tour_guide.key_tip_no_skip",
                        ClientInputEvent.KEY_CHECK_STEP.get().getKey().getDisplayName(),
                        ClientInputEvent.KEY_QUIT.get().getKey().getDisplayName()
                );

        int width = Math.max(Minecraft.getInstance().font.width(keyTip) + 2 * thickness, _width);
        int height = Minecraft.getInstance().font.split(mark.text(), width - thickness * 2 - 4).size() * (Minecraft.getInstance().font.lineHeight + 2) + 2 * thickness;
        graphics.fill(x, y, x + thickness, y + height, COLOR);
        graphics.fill(x + width - thickness, y, x + width, y + height, COLOR);
        graphics.fill(x + thickness, y, x + width - thickness, y + thickness, COLOR);
        graphics.fill(x + thickness, y + height - thickness, x + width - thickness, y + height, COLOR);
        graphics.fill(x + thickness, y + thickness, x + width - thickness, y + height - thickness, FILL);
        graphics.drawWordWrap(Minecraft.getInstance().font, mark.text(), x + thickness + 2, y + thickness + 2, width - thickness * 2 - 4, 0xFFFFFFFF);
        graphics.fill(x, y + height, x + width, y + height + Minecraft.getInstance().font.lineHeight + 2, COLOR);
        graphics.drawCenteredString(Minecraft.getInstance().font,
                keyTip,
                x + width / 2,
                y + height + 1,
                0xFFFFFFFF);
    }
}
