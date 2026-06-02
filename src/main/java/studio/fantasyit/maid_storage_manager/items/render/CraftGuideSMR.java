package studio.fantasyit.maid_storage_manager.items.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideRenderData;
import studio.fantasyit.maid_storage_manager.registry.DataComponentRegistry;
import studio.fantasyit.maid_storage_manager.render.base.LightmapSubmitNodeCollector;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public record CraftGuideSMR(List<BakedQuad> baseQuads,
                            List<BakedQuad> blankQuads) implements SpecialModelRenderer<CraftGuideRenderData> {

    @Override
    public void submit(@Nullable CraftGuideRenderData data, PoseStack poseStack,
                       SubmitNodeCollector submitNodeCollector, int light, int overlay, boolean hasFoil, int outline) {

        Minecraft mc = Minecraft.getInstance();
        ItemModelResolver resolver = mc.getItemModelResolver();
        if (data == null || data.outputs.isEmpty()) {
            submitNodeCollector.submitItem(poseStack, ItemDisplayContext.NONE,
                    light, overlay, 0, new int[0], baseQuads,
                    ItemStackRenderState.FoilType.NONE);
            return;
        }

        submitNodeCollector.submitItem(poseStack, ItemDisplayContext.NONE,
                light, overlay, 0, new int[0], blankQuads,
                ItemStackRenderState.FoilType.NONE);
        List<ItemStack> outputs = data.outputs;
        if (mc.player != null) {
            int i = (mc.player.tickCount / 20) % outputs.size();
            ItemStack item = outputs.get(i);
            if (!item.isEmpty()) {
                ItemStackRenderState outputState = new ItemStackRenderState();
                resolver.updateForTopItem(outputState, item, ItemDisplayContext.GUI, mc.level, null, 0);

                poseStack.pushPose();
                poseStack.translate(0.52f, 0.50f, 0.54f);
                poseStack.scale(0.7f, 0.7f, 0.0001f);
                outputState.submit(poseStack, new LightmapSubmitNodeCollector(submitNodeCollector), light, overlay, outline);
                poseStack.popPose();
            }
        }

        ItemStack icon = data.icon;
        if (!icon.isEmpty()) {
            ItemStackRenderState iconState = new ItemStackRenderState();
            resolver.updateForTopItem(iconState, icon, ItemDisplayContext.GUI, mc.level, null, 0);

            poseStack.pushPose();
            poseStack.translate(0.8f, 0.2f, 0.54f);
            poseStack.scale(0.40f, 0.4f, 0.0001f);
            poseStack.translate(0, 0, 1.0f);
            iconState.submit(poseStack, new LightmapSubmitNodeCollector(submitNodeCollector), light, overlay, outline);
            poseStack.popPose();
        }
    }

    @Override
    public void getExtents(Consumer<Vector3fc> consumer) {
        consumer.accept(new Vector3f(0, 0, 0));
        consumer.accept(new Vector3f(16, 16, 16));
    }

    @Override
    public @Nullable CraftGuideRenderData extractArgument(ItemStack itemStack) {
        CraftGuideRenderData data = itemStack.getOrDefault(DataComponentRegistry.CRAFT_GUIDE_RENDER, CraftGuideRenderData.EMPTY);
        if (data == CraftGuideRenderData.EMPTY) return null;
        return data;
    }

    public record Unbaked(Identifier modelId,
                          Identifier blankModelId) implements SpecialModelRenderer.Unbaked<CraftGuideRenderData> {
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Identifier.CODEC.fieldOf("model").forGetter(Unbaked::modelId),
                Identifier.CODEC.fieldOf("blank_model").forGetter(Unbaked::blankModelId)
        ).apply(instance, Unbaked::new));

        @Override
        public MapCodec<? extends SpecialModelRenderer.Unbaked<CraftGuideRenderData>> type() {
            return MAP_CODEC;
        }

        @Override
        public @Nullable SpecialModelRenderer<CraftGuideRenderData> bake(BakingContext ctx) {
            ItemModel.BakingContext itemCtx = (ItemModel.BakingContext) ctx;
            ModelBaker baker = itemCtx.blockModelBaker();

            ResolvedModel model = baker.getModel(this.modelId);
            TextureSlots slots = model.getTopTextureSlots();
            QuadCollection quads = model.bakeTopGeometry(slots, baker, BlockModelRotation.IDENTITY);

            ResolvedModel blankModel = baker.getModel(this.blankModelId);
            TextureSlots blankSlots = blankModel.getTopTextureSlots();
            QuadCollection blankQuads = blankModel.bakeTopGeometry(blankSlots, baker, BlockModelRotation.IDENTITY);
            return new CraftGuideSMR(quads.getAll(), blankQuads.getAll());
        }
    }
}
