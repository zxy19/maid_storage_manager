package studio.fantasyit.maid_storage_manager.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.apache.logging.log4j.util.Strings;
import studio.fantasyit.maid_storage_manager.render.SeeThroughBoxRenderType;
import studio.fantasyit.maid_storage_manager.storage.Target;

import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class BoxRenderUtil {
    public static boolean useSeeThroughBox = false;

    /**
     * 渲染存储对象
     * @param storage 存储对象
     * @param colors 颜色
     * @param event 渲染事件
     * @param key 名字
     * @param floating 浮动位置列表
     */
    public static void renderStorage(Target storage, float[] colors, RenderLevelStageEvent event, String key, Map<BlockPos, Integer> floating) {
        renderStorage(storage, colors, event, key, floating, 0xffffff);
    }
    /**
     * 渲染存储对象
     * @param storage 存储对象
     * @param colors 颜色
     * @param event 渲染事件
     * @param key 名字
     * @param floating 浮动位置列表
     * @param textColor 文本颜色
     */
    public static void renderStorage(Target storage, float[] colors, RenderLevelStageEvent event, String key, Map<BlockPos, Integer> floating, int textColor) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        Vec3 position = event.getCamera().getPosition().reverse();
        AABB aabb = new AABB(storage.getPos()).move(position);
        VertexConsumer buffer = mc.renderBuffers().bufferSource().getBuffer(useSeeThroughBox ? SeeThroughBoxRenderType.seeThroughBox() : RenderType.LINES);
        LevelRenderer.renderLineBox(event.getPoseStack(), buffer, aabb, colors[0], colors[1], colors[2], colors[3]);
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
            LevelRenderer.renderLineBox(event.getPoseStack(), buffer, sideAabb, colors[0], colors[1], colors[2], colors[3]);
        }
        if (!Strings.isBlank(key)) {
            Vec3 livingFrom = storage.getPos().getCenter().add(0, 0.7f, 0);
            drawText(event, mc, livingFrom, key, textColor, floating.getOrDefault(storage.getPos(), 0) * 0.3f);
            floating.put(storage.getPos(), floating.getOrDefault(storage.getPos(), 0) + 1);
        }
    }

    /**
     * 渲染目标实体
     * @param entity 目标实体
     * @param colors 颜色
     * @param event 渲染事件
     * @param key 名字
     */
    public static void renderEntity(Entity entity, float[] colors, RenderLevelStageEvent event, String key) {
        renderEntity(entity, colors, event, key, 0xffffff);
    }

    /**
     * 渲染目标实体
     * @param entity 目标实体
     * @param colors 颜色
     * @param event 渲染事件
     * @param key 名字
     * @param textColor 文字颜色
     */
    public static void renderEntity(Entity entity, float[] colors, RenderLevelStageEvent event, String key, int textColor) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        Vec3 position = event.getCamera().getPosition().reverse();
        AABB aabb = entity.getBoundingBox().move(position).inflate(0.3);
        VertexConsumer buffer = mc.renderBuffers().bufferSource().getBuffer(useSeeThroughBox ? SeeThroughBoxRenderType.seeThroughBox() : RenderType.LINES);
        LevelRenderer.renderLineBox(event.getPoseStack(), buffer, aabb, colors[0], colors[1], colors[2], colors[3]);
        if (!Strings.isBlank(key)) {
            Vec3 livingFrom = entity.getPosition(event.getPartialTick().getGameTimeDeltaTicks()).add(0, entity.getBbHeight() + 0.5f, 0);
            drawText(event, mc, livingFrom, key, textColor, 0);
        }
    }

    public static void drawText(RenderLevelStageEvent event, Minecraft mc, Vec3 livingFrom, String key, int textColor, float floatingTransform) {
        PoseStack pose = event.getPoseStack();
        Vec3 fromPos = mc.player.getEyePosition(event.getPartialTick().getGameTimeDeltaTicks());
        Vec3 posFromPlayer = fromPos.vectorTo(livingFrom);
        pose.pushPose();
        pose.translate(posFromPlayer.x, posFromPlayer.y, posFromPlayer.z);
        pose.translate(0, floatingTransform, 0);
        pose.mulPose(Axis.YP.rotationDegrees(-event.getCamera().getYRot()));
        pose.mulPose(Axis.XP.rotationDegrees(event.getCamera().getXRot()));
        pose.scale(-0.025f, -0.025f, -1f);
        pose.translate(-mc.font.width(key) / 2f, 0, 0);
//            guiGraphics.drawString(mc.font, key, 0, 0, textColor);
        mc.font.drawInBatch(key,
                0,
                0,
                textColor,
                mc.font.isBidirectional(),
                pose.last().pose(),
                mc.renderBuffers().bufferSource(),
                useSeeThroughBox ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL,
                0,
                15728880);
        mc.renderBuffers().bufferSource().endBatch();
        pose.popPose();
    }
}
