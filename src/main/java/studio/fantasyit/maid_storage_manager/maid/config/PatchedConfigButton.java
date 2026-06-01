package studio.fantasyit.maid_storage_manager.maid.config;

import com.github.tartaricacid.touhoulittlemaid.client.gui.widget.button.MaidConfigButton;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

public class PatchedConfigButton extends MaidConfigButton {
    public PatchedConfigButton(int x, int y, Component title, Component value, OnPress onLeftPressIn, OnPress onRightPressIn) {
        super(x, y, title, value, onLeftPressIn, onRightPressIn);
    }

    @Override
    public void drawCenteredStringWithoutShadow(GuiGraphicsExtractor graphics, Font pFont, Component pText, int pX, int pY, int pColor) {
        FormattedCharSequence formattedcharsequence = pText.getVisualOrderText();
        int textWidth = pFont.width(formattedcharsequence);
        int drawWidth = Math.max(textWidth, 44);
        int alignWidth = Math.min(44, textWidth);
        float scale = (float) 44 / drawWidth;
        graphics.pose().popMatrix();
        graphics.pose().scale(scale, scale);
        graphics.text(pFont, formattedcharsequence, (int) ((pX - (float) alignWidth / 2) / scale), (int) ((pY - 3 + (14 - 8 * scale) / 2) / scale), pColor, false);
        graphics.pose().popMatrix();
    }
}
