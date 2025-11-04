package studio.fantasyit.maid_storage_manager.menu.communicate;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;

import java.util.Optional;

public class CommunicateRollingTextWidget extends AbstractWidget {
    private Component text;
    private int color = 0xffffffff;

    public CommunicateRollingTextWidget(int p_93629_, int p_93630_, int p_93631_, int p_93632_, Component p_93633_) {
        super(p_93629_, p_93630_, p_93631_, p_93632_, p_93633_);
        text = p_93633_;
    }

    public void setText(Component text) {
        this.text = text;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int p_268034_, int p_268009_, float p_268085_) {
        if (!visible) return;
        graphics.flush();
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 5);
        renderScrollingString(
                graphics,
                Minecraft.getInstance().font,
                text,
                getX(),
                getY(),
                getX() + getWidth(),
                getY() + getHeight(),
                Optional.ofNullable(text.getStyle().getColor()).map(TextColor::getValue).orElse(color)
        );
        graphics.pose().popPose();
    }

    public void setColor(int color) {
        this.color = color;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput p_259858_) {

    }
}
