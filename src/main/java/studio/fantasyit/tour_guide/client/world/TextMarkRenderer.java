package studio.fantasyit.tour_guide.client.world;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import studio.fantasyit.tour_guide.client.IWorldMarkRenderer;
import studio.fantasyit.tour_guide.mark.world.TextMark;

public class TextMarkRenderer implements IWorldMarkRenderer<TextMark>, ITextLikeRenderer {
    @Override
    public void render(MultiBufferSource source, LevelRenderer levelRenderer, PoseStack poseStack, Camera camera, float partialTicks, TextMark mark, Context context) {
        drawText(poseStack, partialTicks, Minecraft.getInstance(), camera, mark.pos(), mark.text(), mark.color(), 0);
    }
}
