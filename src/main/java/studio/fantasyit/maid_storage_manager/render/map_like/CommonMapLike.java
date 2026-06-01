package studio.fantasyit.maid_storage_manager.render.map_like;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.joml.Matrix4f;

public class CommonMapLike {
    public static void renderBgSliced(float x1, float y1, float x2, float y2, float borderSz, PoseStack pPoseStack, SubmitNodeCollector submitNodeCollector, int combinedLight, RenderType renderType) {
        float borderUV_W = 0.1f;
        float borderUV_H = 0.1f;
        //TL
        renderBgSlicedPiece(x1, y1, x1 + borderSz, y1 + borderSz, 0, 0, borderUV_W, borderUV_H, pPoseStack, submitNodeCollector, combinedLight, renderType);
        //TR
        renderBgSlicedPiece(x2 - borderSz, y1, x2, y1 + borderSz, 1 - borderUV_W, 0, 1, borderUV_H, pPoseStack, submitNodeCollector, combinedLight, renderType);
        //BL
        renderBgSlicedPiece(x1, y2 - borderSz, x1 + borderSz, y2, 0, 1 - borderUV_H, borderUV_W, 1, pPoseStack, submitNodeCollector, combinedLight, renderType);
        //BR
        renderBgSlicedPiece(x2 - borderSz, y2 - borderSz, x2, y2, 1 - borderUV_W, 1 - borderUV_H, 1, 1, pPoseStack, submitNodeCollector, combinedLight, renderType);

        // T
        renderBgSlicedPiece(x1 + borderSz, y1, x2 - borderSz, y1 + borderSz, borderUV_W, 0, 1 - borderUV_W, borderUV_H, pPoseStack, submitNodeCollector, combinedLight, renderType);
        // B
        renderBgSlicedPiece(x1 + borderSz, y2 - borderSz, x2 - borderSz, y2, borderUV_W, 1 - borderUV_H, 1 - borderUV_W, 1, pPoseStack, submitNodeCollector, combinedLight, renderType);
        // L
        renderBgSlicedPiece(x1, y1 + borderSz, x1 + borderSz, y2 - borderSz, 0, borderUV_H, borderUV_W, 1 - borderUV_H, pPoseStack, submitNodeCollector, combinedLight, renderType);
        // R
        renderBgSlicedPiece(x2 - borderSz, y1 + borderSz, x2, y2 - borderSz, 1 - borderUV_W, borderUV_H, 1, 1 - borderUV_H, pPoseStack, submitNodeCollector, combinedLight, renderType);

        // CENTER
        renderBgSlicedPiece(x1 + borderSz, y1 + borderSz, x2 - borderSz, y2 - borderSz, borderUV_W, borderUV_H, 1 - borderUV_W, 1 - borderUV_H, pPoseStack, submitNodeCollector, combinedLight, renderType);
    }

    private static void renderBgSlicedPiece(float x1, float y1, float x2, float y2, float u1, float v1, float u2, float v2, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int combinedLight, RenderType renderType) {
        submitNodeCollector.submitCustomGeometry(poseStack, renderType, (poseState, consumer) -> {
            Matrix4f m = poseState.pose();
            consumer.addVertex(m, x1, y2, 0.0F).setColor(255, 255, 255, 255).setUv(u1, v2).setLight(combinedLight);
            consumer.addVertex(m, x2, y2, 0.0F).setColor(255, 255, 255, 255).setUv(u2, v2).setLight(combinedLight);
            consumer.addVertex(m, x2, y1, 0.0F).setColor(255, 255, 255, 255).setUv(u2, v1).setLight(combinedLight);
            consumer.addVertex(m, x1, y1, 0.0F).setColor(255, 255, 255, 255).setUv(u1, v1).setLight(combinedLight);
        });
    }
}
