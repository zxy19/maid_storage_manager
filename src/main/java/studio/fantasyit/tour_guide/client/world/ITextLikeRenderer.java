package studio.fantasyit.tour_guide.client.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

public interface ITextLikeRenderer {
    default void drawText(PoseStack pose, float partialTicks, Minecraft mc, Camera camera, Vec3 livingFrom, Component text, int textColor, float floatingTransform) {
        Vec3 fromPos = mc.player.getEyePosition(partialTicks);
        Vec3 posFromPlayer = fromPos.vectorTo(livingFrom);
        pose.pushPose();
        pose.translate(posFromPlayer.x, posFromPlayer.y, posFromPlayer.z);
        pose.translate(0, floatingTransform, 0);
        pose.mulPose(Axis.YP.rotationDegrees(-camera.getYRot()));
        pose.mulPose(Axis.XP.rotationDegrees(camera.getXRot()));
        pose.scale(-0.025f, -0.025f, -1f);
        pose.translate(-mc.font.width(text) / 2f, 0, 0);
//            guiGraphics.drawString(mc.font, key, 0, 0, textColor);
        mc.font.drawInBatch(text,
                0,
                0,
                textColor,
                mc.font.isBidirectional(),
                pose.last().pose(),
                mc.renderBuffers().bufferSource(),
                Font.DisplayMode.NORMAL,
                0,
                15728880);
        mc.renderBuffers().bufferSource().endBatch();
        pose.popPose();
    }
}
