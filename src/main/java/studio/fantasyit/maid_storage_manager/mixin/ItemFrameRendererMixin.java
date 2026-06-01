package studio.fantasyit.maid_storage_manager.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.entity.ItemFrameRenderer;
import net.minecraft.client.renderer.entity.state.ItemFrameRenderState;
import net.minecraft.world.entity.decoration.ItemFrame;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import studio.fantasyit.maid_storage_manager.api.IItemFrameRenderStateItemVisitor;
import studio.fantasyit.maid_storage_manager.event.RenderHandMapLikeEvent;

@Mixin(ItemFrameRenderer.class)
public abstract class ItemFrameRendererMixin<T extends ItemFrame> {

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/decoration/ItemFrame;Lnet/minecraft/client/renderer/entity/state/ItemFrameRenderState;F)V",
            at = @At("HEAD"))
    private void onExtractRenderState(T entity, ItemFrameRenderState state, float partialTicks, CallbackInfo ci) {
        if (entity.getItem().getItem() instanceof RenderHandMapLikeEvent.MapLikeRenderItem mli && mli.available(entity.getItem()))
            if (state instanceof IItemFrameRenderStateItemVisitor iiv)
                iiv.maid_storage_manager$setItem(entity.getItem());
    }

    @WrapWithCondition(method = "submit(Lnet/minecraft/client/renderer/entity/state/ItemFrameRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/BlockModelRenderState;submitWithZOffset(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;III)V"))
    private boolean onSubmit(BlockModelRenderState instance, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, int overlayCoords, int outlineColor, ItemFrameRenderState state) {
        return !(state instanceof IItemFrameRenderStateItemVisitor iiv) || iiv.maid_storage_manager$getItem() == null;
    }
}
