package studio.fantasyit.maid_storage_manager.render.base;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = MaidStorageManager.MODID, value = Dist.CLIENT)
public class LightmapRenderTypes {
    private static RenderPipeline PIPELINE_CUTOUT;
    private static RenderPipeline PIPELINE_TRANSLUCENT;
    private static volatile boolean registered = false;
    private static final Map<String, RenderType> cache = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void registerPipelines(RegisterRenderPipelinesEvent event) {
        RenderPipeline.Snippet snippet = RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET)
                .withVertexShader("core/entity")
                .withFragmentShader("core/entity")
                .withShaderDefine("NO_CARDINAL_LIGHTING")
                .withShaderDefine("NO_OVERLAY")
                .withSampler("Sampler0")
                .withSampler("Sampler2")
                .withVertexFormat(DefaultVertexFormat.ENTITY, VertexFormat.Mode.QUADS)
                .withDepthStencilState(DepthStencilState.DEFAULT)
                .buildSnippet();

        PIPELINE_CUTOUT = RenderPipeline.builder(snippet)
                .withLocation(Identifier.fromNamespaceAndPath(MaidStorageManager.MODID, "pipeline/item_lightmap"))
                .withShaderDefine("ALPHA_CUTOUT", 0.1F)
                .build();
        event.registerPipeline(PIPELINE_CUTOUT);

        PIPELINE_TRANSLUCENT = RenderPipeline.builder(snippet)
                .withLocation(Identifier.fromNamespaceAndPath(MaidStorageManager.MODID, "pipeline/item_lightmap_translucent"))
                .withShaderDefine("ALPHA_CUTOUT", 0.1F)
                .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                .build();
        event.registerPipeline(PIPELINE_TRANSLUCENT);

        registered = true;
    }

    public static RenderType resolve(Identifier atlas, boolean translucent) {
        String key = atlas.toString() + (translucent ? "/translucent" : "/cutout");
        return cache.computeIfAbsent(key, k -> {
            RenderPipeline pipeline = translucent ? PIPELINE_TRANSLUCENT : PIPELINE_CUTOUT;
            String name = (atlas.equals(TextureAtlas.LOCATION_BLOCKS) ? "block" : "item")
                    + (translucent ? "_translucent_lightmap" : "_cutout_lightmap");
            return RenderType.create(
                    MaidStorageManager.MODID + ":" + name,
                    RenderSetup.builder(pipeline)
                            .withTexture("Sampler0", atlas)
                            .useLightmap()
                            .setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
                            .affectsCrumbling()
                            .setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE)
                            .createRenderSetup()
            );
        });
    }
}
