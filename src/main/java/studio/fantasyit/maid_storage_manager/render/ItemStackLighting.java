package studio.fantasyit.maid_storage_manager.render;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class ItemStackLighting {
    private static final List<Vector3f[]> shaderLightDirectionsStack = new ArrayList<>();

    public static void setup(Vector3f vec) {
        Vector3f shaderLightDirections$1 = new Vector3f(RenderSystem.shaderLightDirections[0]);
        Vector3f shaderLightDirections$2 = new Vector3f(RenderSystem.shaderLightDirections[1]);
        shaderLightDirectionsStack.add(new Vector3f[]{shaderLightDirections$1, shaderLightDirections$2});
        vec = vec.normalize();
        RenderSystem.setShaderLights(vec, new Vector3f(0, -1, 0));
    }

    public static void setup() {
        if (shaderLightDirectionsStack.size() > 10)
            throw new RuntimeException("ItemStackLighting.setup() called too many times without release");
        Vector3f shaderLightDirections$1 = new Vector3f(RenderSystem.shaderLightDirections[0]);
        Vector3f shaderLightDirections$2 = new Vector3f(RenderSystem.shaderLightDirections[1]);
        shaderLightDirectionsStack.add(new Vector3f[]{shaderLightDirections$1, shaderLightDirections$2});
        Lighting.setupForFlatItems();
    }

    public static void restore() {
        if (shaderLightDirectionsStack.isEmpty())
            throw new RuntimeException("ItemStackLighting.restore() called without setup()");
        Vector3f[] shaderLightDirections = shaderLightDirectionsStack.remove(shaderLightDirectionsStack.size() - 1);
        RenderSystem.setShaderLights(shaderLightDirections[0], shaderLightDirections[1]);
    }
}
