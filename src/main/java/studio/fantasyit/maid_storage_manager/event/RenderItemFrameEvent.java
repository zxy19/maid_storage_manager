package studio.fantasyit.maid_storage_manager.event;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderItemInFrameEvent;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.entity.VirtualDisplayEntity;
import studio.fantasyit.maid_storage_manager.render.CustomGraphics;
import studio.fantasyit.maid_storage_manager.render.ItemStackLighting;
import studio.fantasyit.maid_storage_manager.render.map_like.CommonMapLike;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.GAME, modid = MaidStorageManager.MODID)
public class RenderItemFrameEvent {

    private static final ModelResourceLocation MAP_FRAME_LOCATION = ModelResourceLocation.vanilla("item_frame", "map=true");

    @SubscribeEvent
    public static void renderItemFrame(RenderItemInFrameEvent event) {
        if (event.getItemStack().getItem() instanceof RenderHandMapLikeEvent.MapLikeRenderItem mli && event.getMultiBufferSource() instanceof MultiBufferSource.BufferSource bs) {
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

            //渲染边框
            BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
            ModelManager modelmanager = blockRenderer.getBlockModelShaper().getModelManager();
            poseStack.pushPose();
            poseStack.translate(0.5 - 1.875 * width / 128, 0.5 - 1.883 * height / 128, -0.9975);
            poseStack.scale(1.83f * width / 128, 1.85f * height / 128, 1);
            blockRenderer.getModelRenderer().renderModel(poseStack.last(),
                    event.getMultiBufferSource().getBuffer(Sheets.solidBlockSheet()),
                    null,
                    modelmanager.getModel(MAP_FRAME_LOCATION),
                    1.0F,
                    1.0F,
                    1.0F,
                    pCombinedLight,
                    OverlayTexture.NO_OVERLAY);
            poseStack.popPose();


            poseStack.pushPose();
            poseStack.translate(0, 0, -0.0575);
            poseStack.translate(0.5f, 0.5f, -0.01f);
            poseStack.scale(-0.015f, -0.015f, -0.015f);

            CommonMapLike.renderBgSliced(0, 0, width, height, 8, poseStack, bs, pCombinedLight, mlr.backgroundRenderType(Minecraft.getInstance(), poseStack, bs, pCombinedLight, event.getItemStack()));
            bs.endBatch();

            mlr.extraTransform(poseStack, context);
            CustomGraphics graphics = new CustomGraphics(Minecraft.getInstance(), poseStack, bs);
            poseStack.scale(1, 1, 1f);
            poseStack.translate(0, 0, 0.01f);
            RenderSystem.enableDepthTest();

            ItemStackLighting.setup(event.getItemFrameEntity().getDirection().getOpposite().step());
            mlr.renderOnHand(graphics, event.getItemStack(), pCombinedLight, context);
            graphics.flush();
            ItemStackLighting.restore();
            poseStack.popPose();
            poseStack.popPose();
            event.setCanceled(true);
        }
    }
}
