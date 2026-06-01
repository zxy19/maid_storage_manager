package studio.fantasyit.maid_storage_manager.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemFrameRenderer;
import net.minecraft.client.renderer.entity.state.ItemFrameRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RenderItemInFrameEvent;
import net.neoforged.neoforge.common.NeoForge;
import studio.fantasyit.maid_storage_manager.Config;

@OnlyIn(Dist.CLIENT)
public class VirtualDisplayEntityRender extends ItemFrameRenderer<VirtualDisplayEntity> {
    public VirtualDisplayEntityRender(EntityRendererProvider.Context p_174204_) {
        super(p_174204_);
    }

    @Override
    public void extractRenderState(VirtualDisplayEntity entity, ItemFrameRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
    }

    @Override
    public void submit(ItemFrameRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        if (Config.virtualItemFrameRender == Config.VirtualItemFrameRender.FRAME) {
            super.submit(state, poseStack, submitNodeCollector, camera);
            return;
        }

        poseStack.pushPose();

        Direction direction = state.direction;
        Vec3 renderOffset = this.getRenderOffset(state);
        poseStack.translate(-renderOffset.x(), -renderOffset.y(), -renderOffset.z());
        poseStack.translate(
                (double) direction.getStepX() * 0.46875,
                (double) direction.getStepY() * 0.46875,
                (double) direction.getStepZ() * 0.46875
        );

        float xRot, yRot;
        if (direction.getAxis().isHorizontal()) {
            xRot = 0.0F;
            yRot = 180.0F - direction.toYRot();
        } else {
            xRot = (float) (-90 * direction.getAxisDirection().getStep());
            yRot = 180.0F;
        }
        poseStack.mulPose(Axis.XP.rotationDegrees(xRot));
        poseStack.mulPose(Axis.YP.rotationDegrees(yRot));

        poseStack.translate(0.0F, 0.0F, 0.5F);

        if (Config.virtualItemFrameRender == Config.VirtualItemFrameRender.CORNER) {
            poseStack.translate(0.25, 0.25, 0);
        }

        if (!NeoForge.EVENT_BUS.post(new RenderItemInFrameEvent(state, this, poseStack, submitNodeCollector)).isCanceled()) {
            if (!state.item.isEmpty()) {
                poseStack.mulPose(Axis.ZP.rotationDegrees((float) state.rotation * 360.0F / 8.0F));

                float scale;
                if (Config.virtualItemFrameRender == Config.VirtualItemFrameRender.LARGE) {
                    scale = 0.7F;
                } else if (Config.virtualItemFrameRender == Config.VirtualItemFrameRender.CORNER) {
                    scale = 0.35F;
                } else {
                    scale = 0.5F;
                }
                poseStack.scale(scale, scale, scale);

                state.item.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
            }
        }

        poseStack.popPose();
    }
}
