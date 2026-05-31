package studio.fantasyit.maid_storage_manager.items.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideRenderData;
import studio.fantasyit.maid_storage_manager.items.data.FilterItemStackList;
import studio.fantasyit.maid_storage_manager.registry.DataComponentRegistry;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;

import java.util.List;

public class CustomItemRenderer {

    public static void renderByItem(ItemStack itemStack,
                                    ItemDisplayContext context,
                                    PoseStack pose,
                                    SubmitNodeCollector submitNodeCollector,
                                    int light,
                                    int overlay) {
        pose.pushPose();
        if (context != ItemDisplayContext.FIXED && context != ItemDisplayContext.GUI) {
            pose.scale(0.5f, 0.5f, 1);
            pose.translate(0.5, 0.8, 0);
        }
        ItemModelResolver resolver = new ItemModelResolver(Minecraft.getInstance().getModelManager());
        ItemStackRenderState state = new ItemStackRenderState();
        if (itemStack.is(ItemRegistry.FILTER_LIST.get()))
            renderFilter(itemStack, context, pose, submitNodeCollector, light, overlay, resolver, state);
        if (itemStack.is(ItemRegistry.CRAFT_GUIDE.get()))
            renderCraft(itemStack, context, pose, submitNodeCollector, light, overlay, resolver, state);
        if (itemStack.is(ItemRegistry.LOGISTICS_GUIDE.get()))
            renderLogistics(itemStack, context, pose, submitNodeCollector, light, overlay, resolver, state);
        pose.popPose();
    }

    private static void renderLogistics(ItemStack itemStack,
                                        ItemDisplayContext context,
                                        PoseStack pose,
                                        SubmitNodeCollector submitNodeCollector,
                                        int light,
                                        int overlay,
                                        ItemModelResolver resolver,
                                        ItemStackRenderState state) {
        resolver.updateForTopItem(state, itemStack, context, Minecraft.getInstance().level, null, 0);
        state.submit(pose, submitNodeCollector, light, overlay, 0);
    }

    private static void renderFilter(ItemStack itemStack,
                                     ItemDisplayContext context,
                                     PoseStack pose,
                                     SubmitNodeCollector submitNodeCollector,
                                     int light,
                                     int overlay,
                                     ItemModelResolver resolver,
                                     ItemStackRenderState baseState) {
        resolver.updateForTopItem(baseState, itemStack, context, Minecraft.getInstance().level, null, 0);
        baseState.submit(pose, submitNodeCollector, light, overlay, 0);

        List<ItemStack> items = itemStack.getOrDefault(DataComponentRegistry.FILTER_ITEMS, new FilterItemStackList().toImmutable())
                .list()
                .stream()
                .filter(i -> !i.isEmpty())
                .toList();
        if (!items.isEmpty()) {
            int i = (Minecraft.getInstance().player.tickCount / 20) % items.size();
            ItemStack item = items.get(i);
            if (!item.isEmpty()) {
                pose.pushPose();
                if (context != ItemDisplayContext.FIXED) {
                    pose.translate(0.22, 0.22, 0.54);
                } else {
                    Quaternionf rotation = new Quaternionf();
                    rotation.rotateAxis((float) Math.PI, 0, 1, 0);
                    pose.mulPose(rotation);
                    pose.translate(-0.78, 0.23, -0.46);
                }
                pose.scale(0.55f, 0.55f, 1);
                pose.mulPose(new Matrix4f().scale(1, 1, 1));
                pose.translate(0.5F, 0.5F, 0.5F);
                ItemStackRenderState overlayState = new ItemStackRenderState();
                resolver.updateForTopItem(overlayState, item, ItemDisplayContext.GUI, Minecraft.getInstance().level, null, 0);
                overlayState.submit(pose, submitNodeCollector, light, overlay, 0);
                pose.popPose();
            }
        }
    }

    private static void renderCraft(ItemStack itemStack,
                                    ItemDisplayContext context,
                                    PoseStack pose,
                                    SubmitNodeCollector submitNodeCollector,
                                    int light,
                                    int overlay,
                                    ItemModelResolver resolver,
                                    ItemStackRenderState baseState) {
        CraftGuideRenderData data = itemStack.getOrDefault(DataComponentRegistry.CRAFT_GUIDE_RENDER, CraftGuideRenderData.EMPTY);
        List<ItemStack> items = data.outputs;
        resolver.updateForTopItem(baseState, itemStack, context, Minecraft.getInstance().level, null, 0);
        baseState.submit(pose, submitNodeCollector, light, overlay, 0);

        if (!items.isEmpty()) {
            int i = (Minecraft.getInstance().player.tickCount / 20) % items.size();
            ItemStack item = items.get(i);
            if (!item.isEmpty()) {
                pose.pushPose();
                if (context != ItemDisplayContext.FIXED) {
                    pose.translate(0.22, 0.22, 0.54);
                } else {
                    Quaternionf rotation = new Quaternionf();
                    rotation.rotateAxis((float) Math.PI, 0, 1, 0);
                    pose.mulPose(rotation);
                    pose.translate(-0.78, 0.23, -0.46);
                }
                pose.translate(0, -0.05, 0);
                pose.scale(0.55f, 0.55f, 1);
                pose.mulPose(new Matrix4f().scale(1, 1, 1));
                pose.translate(0.5F, 0.5F, 0.5F);
                ItemStackRenderState overlayState = new ItemStackRenderState();
                resolver.updateForTopItem(overlayState, item, ItemDisplayContext.GUI, Minecraft.getInstance().level, null, 0);
                overlayState.submit(pose, submitNodeCollector, light, overlay, 0);
                pose.popPose();
            }
        }

        if (!data.icon.isEmpty()) {
            ItemStack icon = data.icon;
            if (icon != null) {
                pose.pushPose();
                if (context != ItemDisplayContext.FIXED) {
                    pose.translate(0.22, 0.22, 0.54);
                } else {
                    Quaternionf rotation = new Quaternionf();
                    rotation.rotateAxis((float) Math.PI, 0, 1, 0);
                    pose.mulPose(rotation);
                    pose.translate(-0.78, 0.23, -0.46);
                }
                pose.translate(0.3F, -0.2F, 0.02F);
                pose.scale(0.40f, 0.40f, 1);
                pose.mulPose(new Matrix4f().scale(1, 1, 1));
                pose.translate(0.5F, 0.5F, 0.5F);
                ItemStackRenderState iconState = new ItemStackRenderState();
                resolver.updateForTopItem(iconState, icon, ItemDisplayContext.GUI, Minecraft.getInstance().level, null, 0);
                iconState.submit(pose, submitNodeCollector, light, overlay, 0);
                pose.popPose();
            }
        }
    }
}

