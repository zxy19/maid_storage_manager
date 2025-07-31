package studio.fantasyit.maid_storage_manager.items.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideRenderData;
import studio.fantasyit.maid_storage_manager.items.data.FilterItemStackList;
import studio.fantasyit.maid_storage_manager.registry.DataComponentRegistry;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;

import java.util.List;

import static net.minecraft.client.renderer.entity.ItemRenderer.getFoilBuffer;

public class CustomItemRenderer extends BlockEntityWithoutLevelRenderer {
    static CustomItemRenderer instance;

    public static CustomItemRenderer getInstance() {
        if (instance == null) {
            instance = new CustomItemRenderer();
        }
        return instance;
    }

    private final BlockEntityRenderDispatcher dispatcher;

    public CustomItemRenderer() {
        super(
                Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                Minecraft.getInstance().getEntityModels()
        );
        this.dispatcher = Minecraft.getInstance().getBlockEntityRenderDispatcher();
    }

    @Override
    public void renderByItem(@NotNull ItemStack itemStack,
                             @NotNull ItemDisplayContext context,
                             @NotNull PoseStack pose,
                             @NotNull MultiBufferSource multiBufferSource,
                             int light,
                             int overlay) {
        pose.pushPose();
        if (context != ItemDisplayContext.FIXED && context != ItemDisplayContext.GUI) {
            pose.scale(0.5f, 0.5f, 1);
            pose.translate(0.5, 0.8, 0);
        }
        if (itemStack.is(ItemRegistry.FILTER_LIST.get()))
            renderFilter(itemStack, context, pose, multiBufferSource, light, overlay);
        if (itemStack.is(ItemRegistry.CRAFT_GUIDE.get()))
            renderCraft(itemStack, context, pose, multiBufferSource, light, overlay);
        if (itemStack.is(ItemRegistry.LOGISTICS_GUIDE.get()))
            renderLogistics(itemStack, context, pose, multiBufferSource, light, overlay);
        pose.popPose();
    }

    private void renderWrappedMulti(@NotNull ItemStack itemStack, @NotNull ItemDisplayContext context, @NotNull PoseStack pose, @NotNull MultiBufferSource multiBufferSource, int light, int overlay) {
        @Nullable List<ItemStack> items = itemStack.get(DataComponentRegistry.TO_SPAWN_ITEMS);
        if (items != null && !items.isEmpty()) {
            int i = (Minecraft.getInstance().player.tickCount / 20) % items.size();
            ItemStack item = items.get(i);
            Minecraft.getInstance().getItemRenderer().render(item,
                    context,
                    true,
                    pose,
                    multiBufferSource,
                    light,
                    overlay,
                    Minecraft.getInstance().getItemRenderer().getModel(
                            item,
                            null,
                            null,
                            0
                    )
            );
        }
    }

    private void renderLogistics(@NotNull ItemStack itemStack,
                                 @NotNull ItemDisplayContext context,
                                 @NotNull PoseStack pose,
                                 @NotNull MultiBufferSource multiBufferSource,
                                 int light,
                                 int overlay) {
        BakedModel model = Minecraft.getInstance().getModelManager().getModel(
                new ModelResourceLocation(ResourceLocation.fromNamespaceAndPath(MaidStorageManager.MODID, "logistics_guide"), ModelResourceLocation.INVENTORY_VARIANT)
        );
        for (var rendertype : model.getRenderTypes(itemStack, false)) {
            VertexConsumer vertexconsumer = getFoilBuffer(multiBufferSource, rendertype, true, itemStack.hasFoil());
            Minecraft.getInstance().getItemRenderer().renderModelLists(model, itemStack, light, overlay, pose, vertexconsumer);
        }
    }

    private void renderFilter(@NotNull ItemStack itemStack,
                              @NotNull ItemDisplayContext context,
                              @NotNull PoseStack pose,
                              @NotNull MultiBufferSource multiBufferSource,
                              int light,
                              int overlay) {
        BakedModel model = Minecraft.getInstance().getModelManager().getModel(
                new ModelResourceLocation(ResourceLocation.fromNamespaceAndPath(MaidStorageManager.MODID, "item/filter_list_base"), ModelResourceLocation.STANDALONE_VARIANT)
        );
        for (var rendertype : model.getRenderTypes(itemStack, false)) {
            VertexConsumer vertexconsumer = getFoilBuffer(multiBufferSource, rendertype, true, itemStack.hasFoil());
            Minecraft.getInstance().getItemRenderer().renderModelLists(model, itemStack, light, overlay, pose, vertexconsumer);
        }

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
//                pose.translate(0.04, 0.2, 0.45F);
                pose.scale(0.55f, 0.55f, 1);
                pose.mulPose(new Matrix4f().scale(1, 1, 0.01F));
                pose.translate(0.5F, 0.5F, 0.5F);
                Minecraft.getInstance().getItemRenderer().render(
                        item,
                        ItemDisplayContext.GUI,
                        true,
                        pose,
                        multiBufferSource,
                        light,
                        overlay,
                        Minecraft.getInstance().getItemRenderer().getModel(
                                item,
                                null,
                                null,
                                0
                        )
                );
                pose.popPose();
            }
        }
    }

    private void renderCraft(@NotNull ItemStack itemStack,
                             @NotNull ItemDisplayContext context,
                             @NotNull PoseStack pose,
                             @NotNull MultiBufferSource multiBufferSource,
                             int light,
                             int overlay) {
        CraftGuideRenderData data = itemStack.getOrDefault(DataComponentRegistry.CRAFT_GUIDE_RENDER, CraftGuideRenderData.EMPTY);
        List<ItemStack> items = data.outputs;
        BakedModel model = Minecraft.getInstance().getModelManager().getModel(
                new ModelResourceLocation(ResourceLocation.fromNamespaceAndPath(MaidStorageManager.MODID, items.isEmpty() ? "item/craft_guide_base" : "item/craft_guide_base_blank"), ModelResourceLocation.STANDALONE_VARIANT)
        );
        for (var rendertype : model.getRenderTypes(itemStack, false)) {
            VertexConsumer vertexconsumer = getFoilBuffer(multiBufferSource, rendertype, true, itemStack.hasFoil());
            Minecraft.getInstance().getItemRenderer().renderModelLists(model, itemStack, light, overlay, pose, vertexconsumer);
        }

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
//                pose.translate(0.04, 0.2, 0.45F);
                pose.scale(0.55f, 0.55f, 1);
                pose.mulPose(new Matrix4f().scale(1, 1, 0.01F));
                pose.translate(0.5F, 0.5F, 0.5F);
                Minecraft.getInstance().getItemRenderer().render(
                        item,
                        ItemDisplayContext.GUI,
                        true,
                        pose,
                        multiBufferSource,
                        light,
                        overlay,
                        Minecraft.getInstance().getItemRenderer().getModel(
                                item,
                                null,
                                null,
                                0
                        )
                );
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
                pose.mulPose(new Matrix4f().scale(1, 1, 0.01F));
                pose.translate(0.5F, 0.5F, 0.5F);
                Minecraft.getInstance().getItemRenderer().render(
                        icon,
                        ItemDisplayContext.GUI,
                        true,
                        pose,
                        multiBufferSource,
                        light,
                        overlay,
                        Minecraft.getInstance().getItemRenderer().getModel(
                                icon,
                                null,
                                null,
                                0
                        )
                );
                pose.popPose();
            }
        }
    }
}
