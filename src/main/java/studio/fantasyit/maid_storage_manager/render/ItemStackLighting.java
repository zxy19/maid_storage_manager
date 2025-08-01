package studio.fantasyit.maid_storage_manager.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class ItemStackLighting {
    private static final List<Vector3f[]> shaderLightDirectionsStack = new ArrayList<>();

    public static void setup(PoseStack poseStack) {
        Vector3f shaderLightDirections$1 = new Vector3f(RenderSystem.shaderLightDirections[0]);
        Vector3f shaderLightDirections$2 = new Vector3f(RenderSystem.shaderLightDirections[1]);
        shaderLightDirectionsStack.add(new Vector3f[]{shaderLightDirections$1, shaderLightDirections$2});
        Vector3f vec = poseStack.last().pose().transformDirection(new Vector3f(0, 0, 1)).normalize();
        RenderSystem.setShaderLights(vec, vec);
    }

    public static void flushAndRestore(MultiBufferSource multiBufferSource) {
        if (multiBufferSource instanceof MultiBufferSource.BufferSource bufferSource)
            bufferSource.endBatch();
        restore();
    }

    public static void restore() {
        if (shaderLightDirectionsStack.isEmpty())
            throw new RuntimeException("ItemStackLighting.restore() called without setup()");
        Vector3f[] shaderLightDirections = shaderLightDirectionsStack.removeLast();
        RenderSystem.setShaderLights(shaderLightDirections[0], shaderLightDirections[1]);
    }
}
