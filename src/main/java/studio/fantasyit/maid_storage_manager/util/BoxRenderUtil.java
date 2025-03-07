package studio.fantasyit.maid_storage_manager.util;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import org.apache.logging.log4j.util.Strings;
import studio.fantasyit.maid_storage_manager.data.IGuiGraphicsGetter;
import studio.fantasyit.maid_storage_manager.storage.Storage;

import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class BoxRenderUtil {
    public static void renderStorage(Storage storage, float[] colors, RenderLevelStageEvent event, String key, Map<BlockPos, Integer> floating) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        Vec3 position = event.getCamera().getPosition().reverse();
        AABB aabb = new AABB(storage.getPos()).move(position);
        VertexConsumer buffer = mc.renderBuffers().bufferSource().getBuffer(RenderType.LINES);
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
            final GuiGraphics guiGraphics = ((IGuiGraphicsGetter) Minecraft.getInstance()).getGuiGraphics(event.getPoseStack());
            Vec3 fromPos = mc.player.getEyePosition(event.getPartialTick());
            Vec3 livingFrom = storage.getPos().getCenter().add(0, 0.7f, 0);
            Vec3 posFromPlayer = fromPos.vectorTo(livingFrom);
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(posFromPlayer.x, posFromPlayer.y, posFromPlayer.z);
            guiGraphics.pose().translate(0, floating.getOrDefault(storage.getPos(), 0) * 0.3f, 0);
            floating.put(storage.getPos(), floating.getOrDefault(storage.getPos(), 0) + 1);
            guiGraphics.pose().mulPose(Axis.YP.rotationDegrees(-event.getCamera().getYRot()));
            guiGraphics.pose().mulPose(Axis.XP.rotationDegrees(event.getCamera().getXRot()));
            guiGraphics.pose().scale(-0.025f, -0.025f, -1f);
            guiGraphics.pose().translate(-mc.font.width(key) / 2f, 0, 0);
            guiGraphics.drawString(mc.font, key, 0, 0, 0xffffff);
            guiGraphics.pose().popPose();
        }
    }
}
