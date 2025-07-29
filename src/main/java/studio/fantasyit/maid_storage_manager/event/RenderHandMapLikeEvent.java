package studio.fantasyit.maid_storage_manager.event;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.api.IGuiGraphics;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, modid = MaidStorageManager.MODID, value = Dist.CLIENT)
public class RenderHandMapLikeEvent {
    public interface MapLikeRenderItem {
        default float getWidth() {
            return 142.0F;
        }

        default float getHeight() {
            return 142.0F;
        }

        RenderType backgroundRenderType(Minecraft mc, PoseStack pPoseStack, MultiBufferSource pBuffer, int pCombinedLight, ItemStack pStack);

        void renderOnHand(GuiGraphics graphics, ItemStack pStack, int pCombinedLight);
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        InteractionHand hand = event.getHand();
        ItemStack stack = event.getItemStack();

        if (hand == InteractionHand.MAIN_HAND && stack.getItem() instanceof MapLikeRenderItem) {
            event.getPoseStack().pushPose();
            if (player.getOffhandItem().isEmpty())
                renderTwoHandedMap(mc,
                        event.getPoseStack(),
                        event.getMultiBufferSource(),
                        event.getPackedLight(),
                        event.getInterpolatedPitch(),
                        event.getEquipProgress(),
                        event.getSwingProgress(),
                        stack);
            else
                renderOneHandedMap(mc,
                        event.getPoseStack(),
                        event.getMultiBufferSource(),
                        event.getPackedLight(),
                        event.getEquipProgress(),
                        player.getMainArm(),
                        event.getSwingProgress(),
                        stack);
            event.getPoseStack().popPose();
            event.setCanceled(true);
        } else if (hand == InteractionHand.OFF_HAND && stack.getItem() instanceof MapLikeRenderItem) {
            event.getPoseStack().pushPose();
            renderOneHandedMap(mc,
                    event.getPoseStack(),
                    event.getMultiBufferSource(),
                    event.getPackedLight(),
                    event.getEquipProgress(),
                    player.getMainArm().getOpposite(),
                    event.getSwingProgress(),
                    stack);
            event.getPoseStack().popPose();
            event.setCanceled(true);
        }
    }

    private static void renderMapHand(Minecraft mc, PoseStack pPoseStack, MultiBufferSource pBuffer, int pCombinedLight, HumanoidArm pSide) {
        RenderSystem.setShaderTexture(0, mc.player.getSkinTextureLocation());
        PlayerRenderer playerrenderer = (PlayerRenderer) mc.getEntityRenderDispatcher().<AbstractClientPlayer>getRenderer(mc.player);
        pPoseStack.pushPose();
        float f = pSide == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        pPoseStack.mulPose(Axis.YP.rotationDegrees(92.0F));
        pPoseStack.mulPose(Axis.XP.rotationDegrees(45.0F));
        pPoseStack.mulPose(Axis.ZP.rotationDegrees(f * -41.0F));
        pPoseStack.translate(f * 0.3F, -1.1F, 0.45F);
        if (pSide == HumanoidArm.RIGHT) {
            playerrenderer.renderRightHand(pPoseStack, pBuffer, pCombinedLight, mc.player);
        } else {
            playerrenderer.renderLeftHand(pPoseStack, pBuffer, pCombinedLight, mc.player);
        }

        pPoseStack.popPose();
    }

    private static void renderOneHandedMap(Minecraft mc, PoseStack pPoseStack, MultiBufferSource pBuffer, int pCombinedLight, float pEquippedProgress, HumanoidArm pHand, float pSwingProgress, ItemStack pStack) {
        float f = pHand == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        pPoseStack.translate(f * 0.125F, -0.125F, 0.0F);
        if (!mc.player.isInvisible()) {
            pPoseStack.pushPose();
            pPoseStack.mulPose(Axis.ZP.rotationDegrees(f * 10.0F));
            renderPlayerArm(mc, pPoseStack, pBuffer, pCombinedLight, pEquippedProgress, pSwingProgress, pHand);
            pPoseStack.popPose();
        }

        pPoseStack.pushPose();
        pPoseStack.translate(f * 0.51F, -0.08F + pEquippedProgress * -1.2F, -0.75F);
        float f1 = Mth.sqrt(pSwingProgress);
        float f2 = Mth.sin(f1 * (float) Math.PI);
        float f3 = -0.5F * f2;
        float f4 = 0.4F * Mth.sin(f1 * ((float) Math.PI * 2F));
        float f5 = -0.3F * Mth.sin(pSwingProgress * (float) Math.PI);
        pPoseStack.translate(f * f3, f4 - 0.3F * f2, f5);
        pPoseStack.mulPose(Axis.XP.rotationDegrees(f2 * -45.0F));
        pPoseStack.mulPose(Axis.YP.rotationDegrees(f * f2 * -30.0F));
        doRenderOnHand(mc, pPoseStack, pBuffer, pCombinedLight, pStack);
        pPoseStack.popPose();
    }

    private static float calculateMapTilt(float pPitch) {
        float f = 1.0F - pPitch / 45.0F + 0.1F;
        f = Mth.clamp(f, 0.0F, 1.0F);
        return -Mth.cos(f * (float) Math.PI) * 0.5F + 0.5F;
    }

    private static void renderTwoHandedMap(Minecraft mc, PoseStack pPoseStack, MultiBufferSource pBuffer, int pCombinedLight, float pPitch, float pEquippedProgress, float pSwingProgress, ItemStack pStack) {
        float f = Mth.sqrt(pSwingProgress);
        float f1 = -0.2F * Mth.sin(pSwingProgress * (float) Math.PI);
        float f2 = -0.4F * Mth.sin(f * (float) Math.PI);
        pPoseStack.translate(0.0F, -f1 / 2.0F, f2);
        float f3 = calculateMapTilt(pPitch);
        pPoseStack.translate(0.0F, 0.04F + pEquippedProgress * -1.2F + f3 * -0.5F, -0.72F);
        pPoseStack.mulPose(Axis.XP.rotationDegrees(f3 * -85.0F));
        if (!mc.player.isInvisible()) {
            pPoseStack.pushPose();
            pPoseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
            renderMapHand(mc, pPoseStack, pBuffer, pCombinedLight, HumanoidArm.RIGHT);
            renderMapHand(mc, pPoseStack, pBuffer, pCombinedLight, HumanoidArm.LEFT);
            pPoseStack.popPose();
        }

        float f4 = Mth.sin(f * (float) Math.PI);
        pPoseStack.mulPose(Axis.XP.rotationDegrees(f4 * 20.0F));
        pPoseStack.scale(2.0F, 2.0F, 2.0F);
        doRenderOnHand(mc, pPoseStack, pBuffer, pCombinedLight, pStack);
    }

    private static void doRenderOnHand(Minecraft mc, PoseStack pPoseStack, MultiBufferSource pBuffer, int pCombinedLight, ItemStack pStack) {
        if (pStack.getItem() instanceof MapLikeRenderItem mlr) {
            pPoseStack.pushPose();
            pPoseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            pPoseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
            pPoseStack.scale(0.38F, 0.38F, 0.38F);
            pPoseStack.translate(-0.5F, -0.5F, 0.0F);
            pPoseStack.scale(0.0078125F, 0.0078125F, 0.0078125F);
            //渲染背景图片
            VertexConsumer vertexconsumer = pBuffer.getBuffer(mlr.backgroundRenderType(mc, pPoseStack, pBuffer, pCombinedLight, pStack));
            Matrix4f matrix4f = pPoseStack.last().pose();
            vertexconsumer.vertex(matrix4f, 64 - mlr.getWidth() / 2, mlr.getHeight() - 7, 0.0F).color(255, 255, 255, 255).uv(0.0F, 1.0F).uv2(pCombinedLight).endVertex();
            vertexconsumer.vertex(matrix4f, 64 + mlr.getWidth() / 2, mlr.getHeight() - 7, 0.0F).color(255, 255, 255, 255).uv(1.0F, 1.0F).uv2(pCombinedLight).endVertex();
            vertexconsumer.vertex(matrix4f, 64 + mlr.getWidth() / 2, -7.0F, 0.0F).color(255, 255, 255, 255).uv(1.0F, 0.0F).uv2(pCombinedLight).endVertex();
            vertexconsumer.vertex(matrix4f, 64 - mlr.getWidth() / 2, -7.0F, 0.0F).color(255, 255, 255, 255).uv(0.0F, 0.0F).uv2(pCombinedLight).endVertex();
            if (pBuffer instanceof MultiBufferSource.BufferSource bs) {
                bs.endBatch();
                pPoseStack.translate(-mlr.getWidth() / 2, -mlr.getHeight() + 7, 1);
                GuiGraphics graphics = new GuiGraphics(mc, bs);
                ((IGuiGraphics) graphics).maid_storage_manager$setPose(pPoseStack);
                mlr.renderOnHand(graphics, pStack, pCombinedLight);
                graphics.flush();
            }
            pPoseStack.popPose();
        }
    }

    private static void renderPlayerArm(Minecraft mc, PoseStack pPoseStack, MultiBufferSource pBuffer, int pCombinedLight, float pEquippedProgress, float pSwingProgress, HumanoidArm pSide) {
        boolean flag = pSide != HumanoidArm.LEFT;
        float f = flag ? 1.0F : -1.0F;
        float f1 = Mth.sqrt(pSwingProgress);
        float f2 = -0.3F * Mth.sin(f1 * (float) Math.PI);
        float f3 = 0.4F * Mth.sin(f1 * ((float) Math.PI * 2F));
        float f4 = -0.4F * Mth.sin(pSwingProgress * (float) Math.PI);
        pPoseStack.translate(f * (f2 + 0.64000005F), f3 + -0.6F + pEquippedProgress * -0.6F, f4 + -0.71999997F);
        pPoseStack.mulPose(Axis.YP.rotationDegrees(f * 45.0F));
        float f5 = Mth.sin(pSwingProgress * pSwingProgress * (float) Math.PI);
        float f6 = Mth.sin(f1 * (float) Math.PI);
        pPoseStack.mulPose(Axis.YP.rotationDegrees(f * f6 * 70.0F));
        pPoseStack.mulPose(Axis.ZP.rotationDegrees(f * f5 * -20.0F));
        AbstractClientPlayer abstractclientplayer = mc.player;
        RenderSystem.setShaderTexture(0, abstractclientplayer.getSkinTextureLocation());
        pPoseStack.translate(f * -1.0F, 3.6F, 3.5F);
        pPoseStack.mulPose(Axis.ZP.rotationDegrees(f * 120.0F));
        pPoseStack.mulPose(Axis.XP.rotationDegrees(200.0F));
        pPoseStack.mulPose(Axis.YP.rotationDegrees(f * -135.0F));
        pPoseStack.translate(f * 5.6F, 0.0F, 0.0F);
        PlayerRenderer playerrenderer = (PlayerRenderer) mc.getEntityRenderDispatcher().<AbstractClientPlayer>getRenderer(abstractclientplayer);
        if (flag) {
            playerrenderer.renderRightHand(pPoseStack, pBuffer, pCombinedLight, abstractclientplayer);
        } else {
            playerrenderer.renderLeftHand(pPoseStack, pBuffer, pCombinedLight, abstractclientplayer);
        }

    }
}
