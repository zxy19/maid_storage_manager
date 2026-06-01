package studio.fantasyit.maid_storage_manager.event;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.render.base.CustomGraphics;
import studio.fantasyit.maid_storage_manager.render.base.ICustomGraphics;
import studio.fantasyit.maid_storage_manager.render.map_like.CommonMapLike;

@EventBusSubscriber(modid = MaidStorageManager.MODID, value = Dist.CLIENT)
public class RenderHandMapLikeEvent {
    public enum MapLikeRenderContext {
        MAIN_HAND,
        OFF_HAND,
        BOTH_HANDS,
        ITEM_FRAME_LARGE,
        ITEM_FRAME_SMALL,
        ITEM_FRAME_SIDE
    }

    public interface MapLikeRenderItem {
        MapLikeRenderer getRenderer();

        default boolean available(ItemStack stack) {
            return true;
        }
    }

    public interface MapLikeRenderer {
        default float getWidth(MapLikeRenderContext context) {
            return 142.0F;
        }

        default float getHeight(MapLikeRenderContext context) {
            return 142.0F;
        }

        RenderType backgroundRenderType(Minecraft mc, PoseStack pPoseStack, SubmitNodeCollector pSubmitNodeCollector, int pCombinedLight, ItemStack pStack);

        void renderOnHand(ICustomGraphics graphics, ItemStack pStack, int pCombinedLight, MapLikeRenderContext context);

        default void extraTransform(PoseStack pPoseStack, MapLikeRenderContext context) {
        }
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        InteractionHand hand = event.getHand();
        ItemStack stack = event.getItemStack();

        if (hand == InteractionHand.MAIN_HAND && stack.getItem() instanceof MapLikeRenderItem mli) {
            if (!mli.available(stack)) return;
            event.getPoseStack().pushPose();
            if (player.getOffhandItem().isEmpty())
                renderTwoHandedMap(mc,
                        event.getPoseStack(),
                        event.getSubmitNodeCollector(),
                        event.getPackedLight(),
                        event.getInterpolatedPitch(),
                        event.getEquipProgress(),
                        event.getSwingProgress(),
                        stack);
            else
                renderOneHandedMap(mc,
                        event.getPoseStack(),
                        event.getSubmitNodeCollector(),
                        event.getPackedLight(),
                        event.getEquipProgress(),
                        player.getMainArm(),
                        event.getSwingProgress(),
                        stack);
            event.getPoseStack().popPose();
            event.setCanceled(true);
        } else if (hand == InteractionHand.OFF_HAND && stack.getItem() instanceof MapLikeRenderItem mli) {
            if (!mli.available(stack)) return;
            event.getPoseStack().pushPose();
            renderOneHandedMap(mc,
                    event.getPoseStack(),
                    event.getSubmitNodeCollector(),
                    event.getPackedLight(),
                    event.getEquipProgress(),
                    player.getMainArm().getOpposite(),
                    event.getSwingProgress(),
                    stack);
            event.getPoseStack().popPose();
            event.setCanceled(true);
        }
    }

    private static void renderMapHand(Minecraft mc, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, HumanoidArm arm) {
        AvatarRenderer<AbstractClientPlayer> avatarRenderer = mc.getEntityRenderDispatcher().getPlayerRenderer(mc.player);
        poseStack.pushPose();
        float invert = arm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        poseStack.mulPose(Axis.YP.rotationDegrees(92.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(45.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(invert * -41.0F));
        poseStack.translate(invert * 0.3F, -1.1F, 0.45F);
        Identifier skinTexture = mc.player.getSkin().body().texturePath();
        if (arm == HumanoidArm.RIGHT) {
            avatarRenderer.renderRightHand(poseStack, submitNodeCollector, lightCoords, skinTexture,
                    mc.player.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE), mc.player);
        } else {
            avatarRenderer.renderLeftHand(poseStack, submitNodeCollector, lightCoords, skinTexture,
                    mc.player.isModelPartShown(PlayerModelPart.LEFT_SLEEVE), mc.player);
        }
        poseStack.popPose();
    }

    private static void renderOneHandedMap(Minecraft mc, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
                                           int lightCoords, float inverseArmHeight, HumanoidArm arm, float attackValue, ItemStack stack) {
        float invert = arm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        poseStack.translate(invert * 0.125F, -0.125F, 0.0F);
        if (!mc.player.isInvisible()) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.ZP.rotationDegrees(invert * 10.0F));
            renderPlayerArm(mc, poseStack, submitNodeCollector, lightCoords, inverseArmHeight, attackValue, arm);
            poseStack.popPose();
        }

        poseStack.pushPose();
        poseStack.translate(invert * 0.51F, -0.08F + inverseArmHeight * -1.2F, -0.75F);
        float sqrtAttackValue = Mth.sqrt(attackValue);
        float xSwing = Mth.sin(sqrtAttackValue * (float) Math.PI);
        float xSwingPos = -0.5F * xSwing;
        float ySwingPos = 0.4F * Mth.sin(sqrtAttackValue * ((float) Math.PI * 2F));
        float zSwingPos = -0.3F * Mth.sin(attackValue * (float) Math.PI);
        poseStack.translate(invert * xSwingPos, ySwingPos - 0.3F * xSwing, zSwingPos);
        poseStack.mulPose(Axis.XP.rotationDegrees(xSwing * -45.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(invert * xSwing * -30.0F));
        doRenderOnHand(mc, poseStack, submitNodeCollector, lightCoords, stack,
                arm == HumanoidArm.RIGHT ? MapLikeRenderContext.MAIN_HAND : MapLikeRenderContext.OFF_HAND);
        poseStack.popPose();
    }

    private static float calculateMapTilt(float xRot) {
        float tilt = 1.0F - xRot / 45.0F + 0.1F;
        tilt = Mth.clamp(tilt, 0.0F, 1.0F);
        return -Mth.cos(tilt * (float) Math.PI) * 0.5F + 0.5F;
    }

    private static void renderTwoHandedMap(Minecraft mc, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
                                           int lightCoords, float xRot, float inverseArmHeight, float attackValue, ItemStack stack) {
        float sqrtAttackValue = Mth.sqrt(attackValue);
        float ySwingPos = -0.2F * Mth.sin(attackValue * (float) Math.PI);
        float zSwingPos = -0.4F * Mth.sin(sqrtAttackValue * (float) Math.PI);
        poseStack.translate(0.0F, -ySwingPos / 2.0F, zSwingPos);
        float mapTilt = calculateMapTilt(xRot);
        poseStack.translate(0.0F, 0.04F + inverseArmHeight * -1.2F + mapTilt * -0.5F, -0.72F);
        poseStack.mulPose(Axis.XP.rotationDegrees(mapTilt * -85.0F));
        if (!mc.player.isInvisible()) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
            renderMapHand(mc, poseStack, submitNodeCollector, lightCoords, HumanoidArm.RIGHT);
            renderMapHand(mc, poseStack, submitNodeCollector, lightCoords, HumanoidArm.LEFT);
            poseStack.popPose();
        }

        float xzSwingRot = Mth.sin(sqrtAttackValue * (float) Math.PI);
        poseStack.mulPose(Axis.XP.rotationDegrees(xzSwingRot * 20.0F));
        poseStack.scale(2.0F, 2.0F, 2.0F);
        doRenderOnHand(mc, poseStack, submitNodeCollector, lightCoords, stack, MapLikeRenderContext.BOTH_HANDS);
    }

    private static void doRenderOnHand(Minecraft mc, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
                                       int combinedLight, ItemStack stack, MapLikeRenderContext context) {
        if (stack.getItem() instanceof MapLikeRenderItem mli) {
            MapLikeRenderer mlr = mli.getRenderer();
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
            poseStack.scale(0.38F, 0.38F, 0.38F);
            poseStack.translate(-0.5F, -0.5F, 0.0F);
            poseStack.scale(0.0078125F, 0.0078125F, 0.0078125F);
            mlr.extraTransform(poseStack, context);
            float height = mlr.getHeight(context);
            float width = mlr.getWidth(context);
            CommonMapLike.renderBgSliced(64 - width / 2, -7.0F, 64 + width / 2, height - 7.0F, 8,
                    poseStack, submitNodeCollector, combinedLight,
                    mlr.backgroundRenderType(mc, poseStack, submitNodeCollector, combinedLight, stack));
            poseStack.translate(64 - width / 2, -7, -1);
            poseStack.scale(1, 1, -1f);
            CustomGraphics graphics = new CustomGraphics(mc, poseStack, submitNodeCollector);
            mlr.renderOnHand(graphics, stack, combinedLight, context);
            poseStack.popPose();
        }
    }

    private static void renderPlayerArm(Minecraft mc, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
                                        int lightCoords, float inverseArmHeight, float attackValue, HumanoidArm arm) {
        boolean isRightArm = arm != HumanoidArm.LEFT;
        float invert = isRightArm ? 1.0F : -1.0F;
        float sqrtAttackValue = Mth.sqrt(attackValue);
        float xSwingPos = -0.3F * Mth.sin(sqrtAttackValue * (float) Math.PI);
        float ySwingPos = 0.4F * Mth.sin(sqrtAttackValue * ((float) Math.PI * 2F));
        float zSwingPos = -0.4F * Mth.sin(attackValue * (float) Math.PI);
        poseStack.translate(invert * (xSwingPos + 0.64000005F), ySwingPos + -0.6F + inverseArmHeight * -0.6F, zSwingPos + -0.71999997F);
        poseStack.mulPose(Axis.YP.rotationDegrees(invert * 45.0F));
        float zSwingRot = Mth.sin(attackValue * attackValue * (float) Math.PI);
        float ySwingRot = Mth.sin(sqrtAttackValue * (float) Math.PI);
        poseStack.mulPose(Axis.YP.rotationDegrees(invert * ySwingRot * 70.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(invert * zSwingRot * -20.0F));
        AbstractClientPlayer player = mc.player;
        poseStack.translate(invert * -1.0F, 3.6F, 3.5F);
        poseStack.mulPose(Axis.ZP.rotationDegrees(invert * 120.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(200.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(invert * -135.0F));
        poseStack.translate(invert * 5.6F, 0.0F, 0.0F);
        AvatarRenderer<AbstractClientPlayer> avatarRenderer = mc.getEntityRenderDispatcher().getPlayerRenderer(player);
        Identifier skinTexture = player.getSkin().body().texturePath();
        if (isRightArm) {
            avatarRenderer.renderRightHand(poseStack, submitNodeCollector, lightCoords, skinTexture,
                    player.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE), mc.player);
        } else {
            avatarRenderer.renderLeftHand(poseStack, submitNodeCollector, lightCoords, skinTexture,
                    player.isModelPartShown(PlayerModelPart.LEFT_SLEEVE), mc.player);
        }
    }
}
