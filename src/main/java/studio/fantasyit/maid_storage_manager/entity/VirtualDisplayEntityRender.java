package studio.fantasyit.maid_storage_manager.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemFrameRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderItemInFrameEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;
import studio.fantasyit.maid_storage_manager.Config;

public class VirtualDisplayEntityRender extends ItemFrameRenderer<VirtualDisplayEntity> {
    public VirtualDisplayEntityRender(EntityRendererProvider.Context p_174204_) {
        super(p_174204_);
    }

    @Override
    public void render(@NotNull VirtualDisplayEntity entity, float p_115077_, float p_115078_, PoseStack p_115079_, MultiBufferSource p_115080_, int p_115081_) {
        if (Config.virtualItemFrameRender == Config.VirtualItemFrameRender.FRAME)
            super.render(entity, p_115077_, p_115078_, p_115079_, p_115080_, p_115081_);
        else {
            p_115079_.pushPose();
            Direction direction = entity.getDirection();
            Vec3 vec3 = this.getRenderOffset(entity, p_115078_);
            p_115079_.translate(-vec3.x(), -vec3.y(), -vec3.z());
            p_115079_.translate((double) direction.getStepX() * 0.46875D, (double) direction.getStepY() * 0.46875D, (double) direction.getStepZ() * 0.46875D);
            p_115079_.mulPose(Axis.XP.rotationDegrees(entity.getXRot()));
            p_115079_.mulPose(Axis.YP.rotationDegrees(180.0F - entity.getYRot()));
            p_115079_.translate(0.0F, 0.0F, 0.5F);
            if (Config.virtualItemFrameRender == Config.VirtualItemFrameRender.CORNER)
                p_115079_.translate(0.25, 0.25, 0);
            p_115079_.mulPose(Axis.ZP.rotationDegrees((float) entity.getRotation() * 360.0F / 8.0F));

            if (!NeoForge.EVENT_BUS.post(new RenderItemInFrameEvent(entity, this, p_115079_, p_115080_, p_115081_)).isCanceled()) {
                if (Config.virtualItemFrameRender == Config.VirtualItemFrameRender.LARGE)
                    p_115079_.scale(0.7F, 0.7F, 0.7F);
                else if (Config.virtualItemFrameRender == Config.VirtualItemFrameRender.CORNER)
                    p_115079_.scale(0.35F, 0.35F, 0.35F);
                else
                    p_115079_.scale(0.5F, 0.5F, 0.5F);
                this.itemRenderer.renderStatic(entity.getItem(),
                        ItemDisplayContext.FIXED,
                        p_115081_,
                        OverlayTexture.NO_OVERLAY,
                        p_115079_,
                        p_115080_,
                        entity.level(),
                        entity.getId());
            }
            p_115079_.popPose();
        }
    }
}
