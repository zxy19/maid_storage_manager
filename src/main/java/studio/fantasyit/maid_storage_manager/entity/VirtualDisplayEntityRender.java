package studio.fantasyit.maid_storage_manager.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemFrameRenderer;
import net.minecraft.client.renderer.entity.state.ItemFrameRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
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
        // FIXME: MC 26.1 rendering pipeline refactored. MultiBufferSource replaced by SubmitNodeCollector.
        // Old render(entity, yaw, partialTick, poseStack, bufferSource, light) with custom item rendering
        // at specific scales (LARGE: 0.7, CORNER: 0.35, default: 0.5).
        // New submit(ItemFrameRenderState, PoseStack, SubmitNodeCollector, CameraRenderState) needs complete
        // rewrite for item rendering at custom scales without the frame model.
        if (Config.virtualItemFrameRender == Config.VirtualItemFrameRender.FRAME) {
            super.submit(state, poseStack, submitNodeCollector, camera);
        }
        // Non-FRAME rendering (CORNER, LARGE, ICON) not yet ported to 26.1 submit-based API.
    }
}
