package studio.fantasyit.maid_storage_manager.menu.communicate;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

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
    public void extractWidgetRenderState(GuiGraphicsExtractor graphics, int p_268034_, int p_268009_, float p_268085_) {
        if (!visible) return;
        extractScrollingStringOverContents(graphics.textRendererForWidget(this, GuiGraphicsExtractor.HoveredTextEffects.NONE), text, 1);
    }

    public void setColor(int color) {
        this.color = color;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput p_259858_) {

    }
}
