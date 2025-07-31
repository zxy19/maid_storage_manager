package studio.fantasyit.maid_storage_manager.render;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import org.joml.Vector3f;

public class ItemStackLighting {

    private static Vector3f shaderLightDirections$1 = null;
    private static Vector3f shaderLightDirections$2 = null;
    private static int depth = 0;

    public static void setup() {
        if (depth > 10)
            throw new RuntimeException("ItemStackLighting.setup() called too many times without release");
        if (depth == 0) {
            shaderLightDirections$1 = new Vector3f(RenderSystem.shaderLightDirections[0]);
            shaderLightDirections$2 = new Vector3f(RenderSystem.shaderLightDirections[1]);
            Lighting.setupForFlatItems();
        }
        depth++;
    }

    public static void restore() {
        if (depth == 0)
            throw new RuntimeException("ItemStackLighting.restore() called without setup()");
        depth--;
        if (depth == 0) {
            RenderSystem.setShaderLights(shaderLightDirections$1, shaderLightDirections$2);
            shaderLightDirections$1 = null;
            shaderLightDirections$2 = null;
        }
    }
}
