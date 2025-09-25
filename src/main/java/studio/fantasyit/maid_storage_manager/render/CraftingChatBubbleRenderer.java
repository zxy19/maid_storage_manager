package studio.fantasyit.maid_storage_manager.render;

import com.github.tartaricacid.touhoulittlemaid.client.renderer.entity.EntityMaidRenderer;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.entity.chatbubble.EntityGraphics;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.entity.chatbubble.IChatBubbleRenderer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import studio.fantasyit.maid_storage_manager.api.mixin.IEntityGraphicsBufferSourceGetter;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class CraftingChatBubbleRenderer implements IChatBubbleRenderer {
    private static final int MAX_WIDTH = 240;
    private final Font font;
    private final ResourceLocation bg;
    private final List<FormattedCharSequence> split;
    private final int barBackgroundColor;
    private final int barForegroundColor1;
    private final int barForegroundColor;
    private final double progress;
    private final double progress1;
    private final int width;
    private final int height;

    public CraftingChatBubbleRenderer(ResourceLocation bg, Component text, int barBackgroundColor, int barForegroundColor, int barForegroundColor1, double progress, double progress1) {
        this.font = Minecraft.getInstance().font;
        this.bg = bg;
        this.split = this.font.split(text, 240);
        this.barBackgroundColor = barBackgroundColor;
        this.barForegroundColor = barForegroundColor;
        this.barForegroundColor1 = barForegroundColor1;
        this.progress1 = Mth.clamp(progress, 0.0F, 1.0F);
        this.progress = Mth.clamp(progress1, 0.0F, 1.0F);
        int tWid = split.stream().map(this.font::width).max(Comparator.comparingInt(t -> t)).orElse(100);
        this.width = Mth.clamp(tWid, 100, 240);
        Objects.requireNonNull(this.font);
        this.height = this.split.size() * 9 + 18;
    }

    public int getHeight() {
        return this.height;
    }

    public int getWidth() {
        return this.width;
    }

    public void render(EntityMaidRenderer renderer, EntityGraphics graphics) {
        int y = 0;

        for (FormattedCharSequence sequence : this.split) {
            int distance = this.width - this.font.width(sequence);
            graphics.drawString(this.font, sequence, (float) (distance / 2), (float) y, 0, false);
            y += 9;
        }

        y += 2;
        int margin = 1;
        int barHeight = 12;
        graphics.fill(0, y, this.width, y + barHeight, this.barBackgroundColor);
        if (this.progress > (double) 0.0F) {
            int barWidth = (int) ((double) (this.width - 2 * margin) * this.progress);
            graphics.getPoseStack().translate(0.0F, 0.0F, -0.01);
            graphics.fill(margin, y + margin, barWidth, y + barHeight - margin, this.barForegroundColor);
        }
        y += barHeight;
        graphics.fill(0, y, this.width, y + 6, this.barBackgroundColor);
        if (this.progress1 > (double) 0.0F) {
            int barWidth = (int) ((double) (this.width - 2 * margin) * this.progress1);
            graphics.getPoseStack().translate(0.0F, 0.0F, -0.01);
            graphics.fill(margin, y + margin, barWidth, y + 6 - margin, this.barForegroundColor1);
        }
    }

    protected void fill(EntityGraphics graphics, float minX, float minY, float maxX, float maxY, int color) {
        Matrix4f matrix4f = graphics.getPoseStack().last().pose();
        if (minX < maxX) {
            float i = minX;
            minX = maxX;
            maxX = i;
        }

        if (minY < maxY) {
            float j = minY;
            minY = maxY;
            maxY = j;
        }
        if (!(graphics instanceof IEntityGraphicsBufferSourceGetter iegbsg)) return;

        VertexConsumer vertexconsumer = iegbsg.getBufferSource().getBuffer(RenderType.textBackground());
        vertexconsumer.addVertex(matrix4f, minX, minY, 0.0f).setColor(color).setLight(graphics.getPackedLight());
        vertexconsumer.addVertex(matrix4f, minX, maxY, 0.0f).setColor(color).setLight(graphics.getPackedLight());
        vertexconsumer.addVertex(matrix4f, maxX, maxY, 0.0f).setColor(color).setLight(graphics.getPackedLight());
        vertexconsumer.addVertex(matrix4f, maxX, minY, 0.0f).setColor(color).setLight(graphics.getPackedLight());
    }

    public ResourceLocation getBackgroundTexture() {
        return this.bg;
    }
}
