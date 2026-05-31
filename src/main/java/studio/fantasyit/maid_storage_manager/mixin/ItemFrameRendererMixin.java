package studio.fantasyit.maid_storage_manager.mixin;

import net.minecraft.client.renderer.entity.ItemFrameRenderer;
import net.minecraft.client.renderer.entity.state.ItemFrameRenderState;
import net.minecraft.world.entity.decoration.ItemFrame;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import studio.fantasyit.maid_storage_manager.event.RenderItemFrameEvent;

@Mixin(ItemFrameRenderer.class)
public abstract class ItemFrameRendererMixin<T extends ItemFrame> {

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/decoration/ItemFrame;Lnet/minecraft/client/renderer/entity/state/ItemFrameRenderState;F)V",
            at = @At("HEAD"))
    private void onExtractRenderState(T entity, ItemFrameRenderState state, float partialTicks, CallbackInfo ci) {
        RenderItemFrameEvent.setCurrentRenderingItem(entity.getItem());
    }
}
