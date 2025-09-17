package studio.fantasyit.tour_guide.client.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import studio.fantasyit.tour_guide.client.IWorldMarkRenderer;
import studio.fantasyit.tour_guide.mark.world.EntityMark;

public class EntityMarkRenderer implements IWorldMarkRenderer<EntityMark>, ITextLikeRenderer {
    @Override
    public void render(MultiBufferSource source, LevelRenderer levelRenderer, PoseStack poseStack, Camera camera, float partialTicks, EntityMark mark, Context context) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        Entity entity = mc.level.getEntity(mark.id());
        if (entity == null) {
            return;
        }
        Vec3 position = camera.getPosition().reverse();
        AABB aabb = entity.getBoundingBox().move(position).inflate(0.3);
        VertexConsumer buffer = mc.renderBuffers().bufferSource().getBuffer(RenderType.LINES);
        int tColor = mark.color();
        LevelRenderer.renderLineBox(poseStack, buffer, aabb, (tColor & 0xff) / 255.0f, ((tColor >> 8) & 0xff) / 255.0f, ((tColor >> 16) & 0xff) / 255.0f, ((tColor >> 24) & 0xff) / 255.0f);
        Vec3 livingFrom = entity.getPosition(partialTicks).add(0, entity.getBbHeight() + 0.5f, 0);
        drawText(poseStack, partialTicks, mc, camera, livingFrom, mark.text(), 0xffffffff, 0);
    }
}
