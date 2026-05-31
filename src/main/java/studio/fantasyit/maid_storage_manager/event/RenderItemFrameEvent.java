package studio.fantasyit.maid_storage_manager.event;

import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderItemInFrameEvent;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;

//import studio.fantasyit.maid_storage_manager.render.ItemStackLighting;

@EventBusSubscriber(modid = MaidStorageManager.MODID, value = Dist.CLIENT)
public class RenderItemFrameEvent {

    private static final Identifier MAP_FRAME_LOCATION = Identifier.fromNamespaceAndPath("minecraft", "item_frame");

    @SubscribeEvent
    public static void renderItemFrame(RenderItemInFrameEvent event) {
        // FIXME: MC 26.1 RenderItemInFrameEvent API completely refactored.
        // Old API: getItemStack(), getItemFrameEntity(), getPackedLight(), getMultiBufferSource()
        // New API: getItemStackRenderState(), getItemFrameRenderState(), getSubmitNodeCollector()
        // ItemStackRenderState is not directly usable as ItemStack.
        // Needs complete rewrite using new render state API.
        /*
        if (event.getItemStack().getItem() instanceof RenderHandMapLikeEvent.MapLikeRenderItem mli) {
            if (!mli.available(event.getItemStack()))
                return;
            int pCombinedLight = event.getPackedLight();
            PoseStack poseStack = event.getPoseStack();
            poseStack.pushPose();
            poseStack.mulPose(Axis.ZP.rotationDegrees((float) event.getItemFrameEntity().getRotation() * (-360.0F) / 8.0F));
            int state = event.getItemFrameEntity().getRotation() % 4;
            RenderHandMapLikeEvent.MapLikeRenderContext context = switch (state) {
                case 0 -> RenderHandMapLikeEvent.MapLikeRenderContext.ITEM_FRAME_SMALL;
                case 1 -> RenderHandMapLikeEvent.MapLikeRenderContext.ITEM_FRAME_LARGE;
                default -> RenderHandMapLikeEvent.MapLikeRenderContext.ITEM_FRAME_SIDE;
            };
            RenderHandMapLikeEvent.MapLikeRenderer mlr = mli.getRenderer();
            float height = mlr.getHeight(context);
            float width = mlr.getWidth(context);

            if ((!(event.getItemFrameEntity() instanceof VirtualDisplayEntity) && !event.getItemFrameEntity().isInvisible()) ||
                    (event.getItemFrameEntity() instanceof VirtualDisplayEntity && Config.virtualItemFrameRender == Config.VirtualItemFrameRender.FRAME)) {
                poseStack.translate(0, 0, 0.0625);
            }

            if (state == 3)
                poseStack.translate(0.48, 0, 0);

            poseStack.pushPose();
            poseStack.translate(0, 0, -0.0575);
            poseStack.translate(0.5f, 0.5f, -0.01f);
            poseStack.scale(-0.015f, -0.015f, -0.015f);

            CommonMapLike.renderBgSliced(0, 0, width, height, 8, poseStack, event.getMultiBufferSource(), pCombinedLight, mlr.backgroundRenderType(Minecraft.getInstance(), poseStack, event.getMultiBufferSource(), pCombinedLight, event.getItemStack()));

            mlr.extraTransform(poseStack, context);
            ICustomGraphics graphics = (event.getMultiBufferSource() instanceof MultiBufferSource.BufferSource bs) ?
                    new CustomGraphics(Minecraft.getInstance(), poseStack, bs) :
                    new CustomCommonGraphics(Minecraft.getInstance(), poseStack, event.getMultiBufferSource());
            graphics.flush();
            poseStack.scale(1, 1, 1f);
            poseStack.translate(0, 0, 0.01f);
            RenderSystem.enableDepthTest();

            graphics.flush();
            ItemStackLighting.setup(poseStack);
            mlr.renderOnHand(graphics, event.getItemStack(), pCombinedLight, context);
            graphics.flush();
            ItemStackLighting.restore();
            poseStack.popPose();
            poseStack.popPose();
            event.setCanceled(true);
        }
        */
    }
}

