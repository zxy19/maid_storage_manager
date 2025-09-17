package studio.fantasyit.tour_guide.client.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import studio.fantasyit.tour_guide.client.IWorldMarkRenderer;
import studio.fantasyit.tour_guide.mark.world.BlockMark;

public class BlockMarkRenderer implements IWorldMarkRenderer<BlockMark>, ITextLikeRenderer {
    @Override
    public void render(MultiBufferSource source, LevelRenderer levelRenderer, PoseStack poseStack, Camera camera, float partialTicks, BlockMark mark, Context context) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        Vec3 position = camera.getPosition().reverse();
        AABB aabb = new AABB(mark.pos()).move(position);
        VertexConsumer buffer = mc.renderBuffers().bufferSource().getBuffer(RenderType.LINES);
        int tColor = mark.color();
        LevelRenderer.renderLineBox(poseStack, buffer, aabb, (tColor & 0xff) / 255.0f, ((tColor >> 8) & 0xff) / 255.0f, ((tColor >> 16) & 0xff) / 255.0f, ((tColor >> 24) & 0xff)/255.0f);
        if (mark.face() != null) {
            BlockPos sidePos = mark.pos().relative(mark.face());
            int dx = sidePos.getX() - mark.pos().getX();
            int dy = sidePos.getY() - mark.pos().getY();
            int dz = sidePos.getZ() - mark.pos().getZ();
            AABB sideAabb = new AABB(sidePos).move(position);
            if (dx != 0) {
                if (dx > 0)
                    sideAabb = sideAabb.setMaxX(sideAabb.minX + 0.07);
                else
                    sideAabb = sideAabb.setMinX(sideAabb.maxX - 0.07);
            } else {
                sideAabb = sideAabb.setMaxX(sideAabb.maxX - 0.2);
                sideAabb = sideAabb.setMinX(sideAabb.minX + 0.2);
            }
            if (dy != 0) {
                if (dy > 0)
                    sideAabb = sideAabb.setMaxY(sideAabb.minY + 0.07);
                else
                    sideAabb = sideAabb.setMinY(sideAabb.maxY - 0.07);
            } else {
                sideAabb = sideAabb.setMaxY(sideAabb.maxY - 0.2);
                sideAabb = sideAabb.setMinY(sideAabb.minY + 0.2);
            }
            if (dz != 0) {
                if (dz > 0)
                    sideAabb = sideAabb.setMaxZ(sideAabb.minZ + 0.07);
                else
                    sideAabb = sideAabb.setMinZ(sideAabb.maxZ - 0.07);
            } else {
                sideAabb = sideAabb.setMaxZ(sideAabb.maxZ - 0.2);
                sideAabb = sideAabb.setMinZ(sideAabb.minZ + 0.2);
            }
            LevelRenderer.renderLineBox(poseStack, buffer, sideAabb,(tColor & 0xff) / 255.0f, ((tColor >> 8) & 0xff) / 255.0f, ((tColor >> 16) & 0xff) / 255.0f, ((tColor >> 24) & 0xff)/255.0f);
        }

        Vec3 livingFrom = mark.pos().getCenter().add(0, 0.7f, 0);
        drawText(poseStack, partialTicks, mc, camera, livingFrom, mark.text(), 0xffffffff, context.floating().getOrDefault(mark.pos(), 0) * 0.3f);
        context.floating().put(mark.pos(), context.floating().getOrDefault(mark.pos(), 0) + 1);
    }
}
