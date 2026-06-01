package studio.fantasyit.maid_storage_manager.items.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public record LogisticsGuideSMR(List<BakedQuad> baseQuads) implements NoDataSpecialModelRenderer {

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
                       int light, int overlay, boolean hasFoil, int outline) {
        submitNodeCollector.submitItem(poseStack, ItemDisplayContext.NONE,
                light, overlay, 0, new int[0], baseQuads,
                ItemStackRenderState.FoilType.NONE);
    }

    @Override
    public void getExtents(Consumer<Vector3fc> consumer) {
        consumer.accept(new Vector3f(0, 0, 0));
        consumer.accept(new Vector3f(16, 16, 16));
    }

    public record Unbaked(Identifier modelId) implements NoDataSpecialModelRenderer.Unbaked {
        public static final MapCodec<Unbaked> MAP_CODEC = Identifier.CODEC
                .fieldOf("model")
                .xmap(Unbaked::new, Unbaked::modelId);

        @Override
        public MapCodec<? extends Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public @Nullable SpecialModelRenderer<Void> bake(BakingContext ctx) {
            ItemModel.BakingContext itemCtx = (ItemModel.BakingContext) ctx;
            ModelBaker baker = itemCtx.blockModelBaker();
            ResolvedModel model = baker.getModel(this.modelId);
            TextureSlots slots = model.getTopTextureSlots();
            QuadCollection quads = model.bakeTopGeometry(slots, baker, BlockModelRotation.IDENTITY);
            return new LogisticsGuideSMR(quads.getAll());
        }
    }
}
