package studio.fantasyit.maid_storage_manager.render.base;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.quad.MutableQuad;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.List;

public class LightmapSubmitNodeCollector implements SubmitNodeCollector {

    private final SubmitNodeCollector delegate;

    public LightmapSubmitNodeCollector(SubmitNodeCollector delegate) {
        this.delegate = delegate;
    }

    @Override
    public void submitItem(PoseStack poseStack, ItemDisplayContext displayContext,
                           int lightCoords, int overlayCoords, int outlineColor,
                           int[] tintLayers, List<BakedQuad> quads,
                           ItemStackRenderState.FoilType foilType) {
        delegate.submitItem(poseStack, displayContext,
                lightCoords, overlayCoords, outlineColor,
                tintLayers, remapQuads(quads), foilType);
    }

    private List<BakedQuad> remapQuads(List<BakedQuad> quads) {
        boolean needsRemap = false;
        for (BakedQuad quad : quads) {
            if (isItemQuad(quad)) {
                needsRemap = true;
                break;
            }
        }
        if (!needsRemap) {
            return quads;
        }

        List<BakedQuad> remapped = new ArrayList<>(quads.size());
        MutableQuad mutable = new MutableQuad();
        for (BakedQuad quad : quads) {
            if (isItemQuad(quad)) {
                mutable.setFrom(quad);
                RenderType renderType = quad.materialInfo().itemRenderType();
                RenderType lightmapType = LightmapRenderTypes.resolve(
                        quad.materialInfo().sprite().atlasLocation(),
                        renderType.hasBlending());
                mutable.setSprite(quad.materialInfo().sprite(), quad.materialInfo().layer(), lightmapType);
                remapped.add(mutable.toBakedQuad());
            } else {
                remapped.add(quad);
            }
        }
        return remapped;
    }

    private boolean isItemQuad(BakedQuad quad) {
        Identifier atlas = quad.materialInfo().sprite().atlasLocation();
        return atlas.equals(TextureAtlas.LOCATION_ITEMS) || atlas.equals(TextureAtlas.LOCATION_BLOCKS);
    }

    @Override
    public OrderedSubmitNodeCollector order(int order) {
        return delegate.order(order);
    }

    @Override
    public void submitShadow(PoseStack poseStack, float v, List<EntityRenderState.ShadowPiece> list) {
        delegate.submitShadow(poseStack, v, list);
    }

    @Override
    public void submitNameTag(PoseStack poseStack, @Nullable Vec3 vec3, int i, Component component, boolean b, int i1, double v, CameraRenderState cameraRenderState) {
        delegate.submitNameTag(poseStack, vec3, i, component, b, i1, v, cameraRenderState);
    }

    @Override
    public void submitText(PoseStack poseStack, float v, float v1, FormattedCharSequence formattedCharSequence, boolean b, Font.DisplayMode displayMode, int i, int i1, int i2, int i3) {
        delegate.submitText(poseStack, v, v1, formattedCharSequence, b, displayMode, i, i1, i2, i3);
    }

    @Override
    public void submitFlame(PoseStack poseStack, EntityRenderState entityRenderState, Quaternionf quaternionf) {
        delegate.submitFlame(poseStack, entityRenderState, quaternionf);
    }

    @Override
    public void submitLeash(PoseStack poseStack, EntityRenderState.LeashState leashState) {
        delegate.submitLeash(poseStack, leashState);
    }

    @Override
    public <S> void submitModel(Model<? super S> model, S s, PoseStack poseStack, RenderType renderType, int i, int i1, int i2, @Nullable TextureAtlasSprite textureAtlasSprite, int i3, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay) {
        delegate.submitModel(model, s, poseStack, renderType, i, i1, i2, textureAtlasSprite, i3, crumblingOverlay);
    }

    @Override
    public void submitModelPart(ModelPart modelPart, PoseStack poseStack, RenderType renderType, int i, int i1, @Nullable TextureAtlasSprite textureAtlasSprite, boolean b, boolean b1, int i2, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay, int i3) {
        delegate.submitModelPart(modelPart, poseStack, renderType, i, i1, textureAtlasSprite, b, b1, i2, crumblingOverlay, i3);
    }

    @Override
    public void submitMovingBlock(PoseStack poseStack, MovingBlockRenderState movingBlockRenderState) {
        delegate.submitMovingBlock(poseStack, movingBlockRenderState);
    }

    @Override
    public void submitBlockModel(PoseStack poseStack, RenderType renderType, List<BlockStateModelPart> list, int[] ints, int i, int i1, int i2) {
        delegate.submitBlockModel(poseStack, renderType, list, ints, i, i1, i2);
    }

    @Override
    public void submitBreakingBlockModel(PoseStack poseStack, BlockStateModel blockStateModel, long l, int i) {
        delegate.submitBreakingBlockModel(poseStack, blockStateModel, l, i);
    }

    @Override
    public void submitCustomGeometry(PoseStack poseStack, RenderType renderType, CustomGeometryRenderer customGeometryRenderer) {
        delegate.submitCustomGeometry(poseStack, renderType, customGeometryRenderer);
    }

    @Override
    public void submitParticleGroup(ParticleGroupRenderer particleGroupRenderer) {
        delegate.submitParticleGroup(particleGroupRenderer);
    }
}
