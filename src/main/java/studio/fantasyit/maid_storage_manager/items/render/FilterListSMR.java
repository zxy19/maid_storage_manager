package studio.fantasyit.maid_storage_manager.items.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
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
import studio.fantasyit.maid_storage_manager.items.FilterListItem;
import studio.fantasyit.maid_storage_manager.items.data.FilterItemStackList;
import studio.fantasyit.maid_storage_manager.registry.DataComponentRegistry;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public record FilterListSMR(List<BakedQuad> baseQuads) implements SpecialModelRenderer<FilterItemStackList.Immutable> {

    @Override
    public void submit(@Nullable FilterItemStackList.Immutable filterItems, PoseStack poseStack,
                       SubmitNodeCollector submitNodeCollector, int light, int overlay, boolean hasFoil, int outline) {
        submitNodeCollector.submitItem(poseStack, ItemDisplayContext.NONE,
                light, overlay, 0, new int[0], baseQuads,
                ItemStackRenderState.FoilType.NONE);

        if (filterItems == null) return;

        List<ItemStack> items = filterItems.list().stream()
                .filter(i -> !i.isEmpty())
                .toList();
        if (items.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        int i = (mc.player.tickCount / 20) % items.size();
        ItemStack item = items.get(i);

        ItemModelResolver resolver = mc.getItemModelResolver();
        ItemStackRenderState overlayState = new ItemStackRenderState();
        resolver.updateForTopItem(overlayState, item, ItemDisplayContext.NONE, mc.level, null, 0);

        poseStack.pushPose();
        poseStack.translate(0.52f, 0.50f, 0.54f);
        poseStack.scale(0.7f, 0.7f, 0.0001f);
        overlayState.submit(poseStack, submitNodeCollector, light, overlay, outline);
        poseStack.popPose();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> consumer) {
        consumer.accept(new Vector3f(0, 0, 0));
        consumer.accept(new Vector3f(16, 16, 16));
    }

    @Override
    public @Nullable FilterItemStackList.Immutable extractArgument(ItemStack itemStack) {
        return itemStack.getOrDefault(DataComponentRegistry.FILTER_ITEMS, FilterListItem.EMPTY);
    }

    public record Unbaked(Identifier modelId) implements SpecialModelRenderer.Unbaked<FilterItemStackList.Immutable> {
        public static final MapCodec<Unbaked> MAP_CODEC = Identifier.CODEC
                .fieldOf("model")
                .xmap(Unbaked::new, Unbaked::modelId);

        @Override
        public MapCodec<? extends SpecialModelRenderer.Unbaked<FilterItemStackList.Immutable>> type() {
            return MAP_CODEC;
        }

        @Override
        public @Nullable SpecialModelRenderer<FilterItemStackList.Immutable> bake(BakingContext ctx) {
            ItemModel.BakingContext itemCtx = (ItemModel.BakingContext) ctx;
            ModelBaker baker = itemCtx.blockModelBaker();
            ResolvedModel model = baker.getModel(this.modelId);
            TextureSlots slots = model.getTopTextureSlots();
            QuadCollection quads = model.bakeTopGeometry(slots, baker, BlockModelRotation.IDENTITY);
            return new FilterListSMR(quads.getAll());
        }
    }
}
