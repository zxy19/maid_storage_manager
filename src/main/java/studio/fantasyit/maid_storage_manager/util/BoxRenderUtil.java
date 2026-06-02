package studio.fantasyit.maid_storage_manager.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.logging.log4j.util.Strings;
import studio.fantasyit.maid_storage_manager.render.SeeThroughBoxRenderType;
import studio.fantasyit.maid_storage_manager.storage.Target;

import java.util.Map;

public class BoxRenderUtil {
    public static boolean useSeeThroughBox = false;

    public static void renderStorage(Target storage, float[] colors, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera, String key, Map<BlockPos, Integer> floating) {
        renderStorage(storage, colors, poseStack, submitNodeCollector, camera, key, floating, 0xffffffff);
    }

    public static void renderStorage(Target storage, float[] colors, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera, String key, Map<BlockPos, Integer> floating, int textColor) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        Vec3 position = camera.pos.reverse();
        AABB aabb = new AABB(storage.getPos()).move(position);
        submitNodeCollector.submitCustomGeometry(poseStack, useSeeThroughBox ? SeeThroughBoxRenderType.seeThroughBox() : RenderTypes.LINES, (poseState, consumer) -> {
            renderBox(poseStack, consumer, aabb, colors[0], colors[1], colors[2], colors[3]);
        });
        if (storage.getSide().isPresent()) {
            BlockPos sidePos = storage.getPos().relative(storage.getSide().get());
            int dx = sidePos.getX() - storage.getPos().getX();
            int dy = sidePos.getY() - storage.getPos().getY();
            int dz = sidePos.getZ() - storage.getPos().getZ();
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
            final AABB finalSideAabb = sideAabb;
            submitNodeCollector.submitCustomGeometry(poseStack, useSeeThroughBox ? SeeThroughBoxRenderType.seeThroughBox() : RenderTypes.LINES, (poseState, consumer) -> {
                renderBox(poseStack, consumer, finalSideAabb, colors[0], colors[1], colors[2], colors[3]);
            });
        }
        if (!Strings.isBlank(key)) {
            Vec3 livingFrom = storage.getPos().getCenter().add(0, 0.7f, 0);
            drawText(poseStack, mc, camera, submitNodeCollector, livingFrom, key, textColor, floating.getOrDefault(storage.getPos(), 0) * 0.3f);
            floating.put(storage.getPos(), floating.getOrDefault(storage.getPos(), 0) + 1);
        }
    }

    public static void renderEntity(Entity entity, float[] colors, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera, float partialTick, String key) {
        renderEntity(entity, colors, poseStack, submitNodeCollector, camera, partialTick, key, 0xffffffff);
    }

    public static void renderEntity(Entity entity, float[] colors, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera, float partialTick, String key, int textColor) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        Vec3 position = camera.pos.reverse();
        AABB aabb = entity.getBoundingBox().move(position).inflate(0.3);
        submitNodeCollector.submitCustomGeometry(poseStack, useSeeThroughBox ? SeeThroughBoxRenderType.seeThroughBox() : RenderTypes.LINES, (poseState, consumer) -> {
            renderBox(poseStack, consumer, aabb, colors[0], colors[1], colors[2], colors[3]);
        });
        if (!Strings.isBlank(key)) {
            Vec3 livingFrom = entity.getPosition(partialTick).add(0, entity.getBbHeight() + 0.5f, 0);
            drawText(poseStack, mc, camera, submitNodeCollector, livingFrom, key, textColor, 0);
        }
    }

    public static void drawText(PoseStack poseStack, Minecraft mc, CameraRenderState camera, SubmitNodeCollector submitNodeCollector, Vec3 livingFrom, String key, int textColor, float floatingTransform) {
        float partialTick = mc.getDeltaTracker().getGameTimeDeltaPartialTick(true);
        Vec3 fromPos = mc.player.getEyePosition(partialTick);
        Vec3 posFromPlayer = fromPos.vectorTo(livingFrom);
        poseStack.pushPose();
        poseStack.translate(posFromPlayer.x, posFromPlayer.y, posFromPlayer.z);
        poseStack.translate(0, floatingTransform, 0);
        poseStack.mulPose(Axis.YP.rotationDegrees(-camera.yRot));
        poseStack.mulPose(Axis.XP.rotationDegrees(camera.xRot));
        poseStack.scale(-0.025f, -0.025f, -1f);
        poseStack.translate(-mc.font.width(key) / 2f, 0, 0);
        submitNodeCollector.submitText(poseStack, 0, 0,
                net.minecraft.util.FormattedCharSequence.forward(key, net.minecraft.network.chat.Style.EMPTY),
                mc.font.isBidirectional(),
                useSeeThroughBox ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL,
                LightCoordsUtil.FULL_BRIGHT, textColor, 0, 0);
        poseStack.popPose();
    }

    private static void renderBox(PoseStack poseStack, VertexConsumer consumer, AABB aabb, float r, float g, float b, float a) {
        VoxelShape shape = Shapes.create(aabb);
        int color = ARGB.color((int) (a * 255), (int) (r * 255), (int) (g * 255), (int) (b * 255));
        ShapeRenderer.renderShape(poseStack, consumer, shape, 0, 0, 0, color, 2.0f);
    }
}
