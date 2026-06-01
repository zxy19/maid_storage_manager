package studio.fantasyit.maid_storage_manager.event;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.ItemFrameRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderItemInFrameEvent;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.render.base.CustomGraphics;
import studio.fantasyit.maid_storage_manager.render.base.ICustomGraphics;
import studio.fantasyit.maid_storage_manager.render.map_like.CommonMapLike;

@EventBusSubscriber(modid = MaidStorageManager.MODID, value = Dist.CLIENT)
public class RenderItemFrameEvent {

    private static final Identifier MAP_FRAME_LOCATION = Identifier.fromNamespaceAndPath("minecraft", "item_frame");

    private static final ThreadLocal<ItemStack> CURRENT_RENDERING_ITEM = new ThreadLocal<>();

    public static void setCurrentRenderingItem(ItemStack stack) {
        CURRENT_RENDERING_ITEM.set(stack);
    }

    public static void clearCurrentRenderingItem() {
        CURRENT_RENDERING_ITEM.remove();
    }

    @SubscribeEvent
    public static void renderItemFrame(RenderItemInFrameEvent event) {
        ItemStack itemStack = CURRENT_RENDERING_ITEM.get();
        if (itemStack == null || !(itemStack.getItem() instanceof RenderHandMapLikeEvent.MapLikeRenderItem mli))
            return;
        if (!mli.available(itemStack))
            return;

        ItemFrameRenderState frameState = event.getItemFrameRenderState();
        PoseStack poseStack = event.getPoseStack();
        int rotation = frameState.rotation;

        RenderHandMapLikeEvent.MapLikeRenderContext context = switch (rotation % 4) {
            case 0 -> RenderHandMapLikeEvent.MapLikeRenderContext.ITEM_FRAME_SMALL;
            case 1 -> RenderHandMapLikeEvent.MapLikeRenderContext.ITEM_FRAME_LARGE;
            default -> RenderHandMapLikeEvent.MapLikeRenderContext.ITEM_FRAME_SIDE;
        };

        RenderHandMapLikeEvent.MapLikeRenderer mlr = mli.getRenderer();
        float height = mlr.getHeight(context);
        float width = mlr.getWidth(context);

        int combinedLight = LightCoordsUtil.FULL_BRIGHT;
        SubmitNodeCollector submitNodeCollector = event.getSubmitNodeCollector();

        poseStack.pushPose();
        poseStack.mulPose(Axis.ZP.rotationDegrees((float) rotation * (-360.0F) / 8.0F));

        Config.VirtualItemFrameRender renderMode = Config.virtualItemFrameRender;
        boolean frameVisible = renderMode == Config.VirtualItemFrameRender.FRAME;
        if (frameVisible) {
            poseStack.translate(0, 0, 0.0625);
        }

        if (rotation % 4 == 3)
            poseStack.translate(0.48, 0, 0);

        poseStack.pushPose();
        poseStack.translate(0, 0, -0.0575);
        poseStack.translate(0.5f, 0.5f, -0.01f);
        poseStack.scale(-0.015f, -0.015f, 1f);

        var bgRenderType = mlr.backgroundRenderType(Minecraft.getInstance(), poseStack, submitNodeCollector, combinedLight, itemStack);
        if (bgRenderType != null) {
            CommonMapLike.renderBgSliced(0, 0, width, height, 8, poseStack, submitNodeCollector, combinedLight, bgRenderType);
        }

        mlr.extraTransform(poseStack, context);
        poseStack.scale(1f, 1f, 1f);
        poseStack.translate(0, 0, 0.01f);

        ICustomGraphics graphics = new CustomGraphics(Minecraft.getInstance(), poseStack, submitNodeCollector);
        mlr.renderOnHand(graphics, itemStack, combinedLight, context);

        poseStack.popPose();
        poseStack.popPose();
        event.setCanceled(true);
    }
}

