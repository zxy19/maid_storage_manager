package studio.fantasyit.tour_guide.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;

import java.util.Map;

public interface IWorldMarkRenderer<T> {
    record Context(Map<BlockPos, Integer> floating) {
    }

    void render(MultiBufferSource source, LevelRenderer levelRenderer, PoseStack poseStack, Camera camera, float partialTicks, T mark, Context context);
}
