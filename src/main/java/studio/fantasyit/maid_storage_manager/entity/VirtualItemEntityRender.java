package studio.fantasyit.maid_storage_manager.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class VirtualItemEntityRender extends EntityRenderer<VirtualItemEntity, ItemEntityRenderState> {
    private final ItemModelResolver itemModelResolver;
    private final RandomSource random = RandomSource.create();

    public VirtualItemEntityRender(EntityRendererProvider.Context context) {
        super(context);
        this.itemModelResolver = context.getItemModelResolver();
        this.shadowRadius = 0.15F;
        this.shadowStrength = 0.75F;
    }

    @Override
    public ItemEntityRenderState createRenderState() {
        return new ItemEntityRenderState();
    }

    @Override
    public void extractRenderState(VirtualItemEntity entity, ItemEntityRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.bobOffset = entity.bobOffs;
        state.shouldBob = true;
        state.shouldSpread = true;
        state.extractItemGroupRenderState(entity, entity.getItem(), this.itemModelResolver);
    }

    @Override
    public void submit(ItemEntityRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        if (!state.item.isEmpty()) {
            poseStack.pushPose();
            float bob = state.shouldBob ? Mth.sin(state.ageInTicks / 10.0F + state.bobOffset) * 0.1F + 0.1F : 0.0F;
            poseStack.translate(0.0F, bob + 0.25F, 0.0F);
            float spin = state.ageInTicks / 20.0F;
            poseStack.mulPose(Axis.YP.rotation(spin));
            ItemEntityRenderer.submitMultipleFromCount(poseStack, submitNodeCollector, state.lightCoords, state, this.random);
            poseStack.popPose();
            super.submit(state, poseStack, submitNodeCollector, camera);
        }
    }

}
