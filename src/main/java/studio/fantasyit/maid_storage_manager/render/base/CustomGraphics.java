package studio.fantasyit.maid_storage_manager.render.base;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.joml.Matrix4f;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.Queue;

public class CustomGraphics implements ICustomGraphics {

    private final PoseStack pose;
    private final MultiBufferSource.BufferSource bufferSource;
    private final Minecraft minecraft;

    private record ItemStackRenderInfo(LivingEntity entity, ItemStack stack, PoseStack pose, int x, int y, int state) {
    }

    private Queue<ItemStackRenderInfo> itemStackRenderQueue = new LinkedList<>();

    public CustomGraphics(Minecraft mc, PoseStack p_281669_, MultiBufferSource.BufferSource p_281893_) {
        this.minecraft = mc;
        this.pose = p_281669_;
        this.bufferSource = p_281893_;
    }

    public PoseStack pose() {
        return this.pose;
    }

    public int drawString(Font p_282636_, FormattedCharSequence p_281596_, float p_281586_, float p_282816_, int p_281743_, boolean p_282394_) {
        int i = p_282636_.drawInBatch(p_281596_, p_281586_, p_282816_, p_281743_, p_282394_, this.pose.last().pose(), this.bufferSource, Font.DisplayMode.NORMAL, 0, 15728880);
        return i;
    }

    public int drawString(Font p_283343_, @Nullable String p_281896_, float p_283569_, float p_283418_, int p_281560_, boolean p_282130_) {
        if (p_281896_ == null) {
            return 0;
        } else {
            int i = p_283343_.drawInBatch(p_281896_, p_283569_, p_283418_, p_281560_, p_282130_, this.pose.last().pose(), this.bufferSource, Font.DisplayMode.NORMAL, 0, 15728880, p_283343_.isBidirectional());
            return i;
        }
    }


    public void flush() {
        RenderSystem.disableDepthTest();
        this.bufferSource.endBatch();
        RenderSystem.enableDepthTest();
    }

    public void renderItem(ItemStack p_281978_, int p_282647_, int p_281944_) {
        this.renderItem(this.minecraft.player, this.minecraft.level, p_281978_, p_282647_, p_281944_, 0, 0);
    }

    private void renderItem(@Nullable LivingEntity p_282619_, @Nullable Level p_281754_, ItemStack p_281675_, int p_281271_, int p_282210_, int p_283260_, int p_281995_) {
        if (!p_281675_.isEmpty()) {
            BakedModel bakedmodel = this.minecraft.getItemRenderer().getModel(p_281675_, p_281754_, p_282619_, p_283260_);
            this.pose.pushPose();
            this.pose.translate((float) (p_281271_ + 8), (float) (p_282210_ + 8), 0.1);
            this.pose.scale(16.0F, -16.0F, 16.0F);
            this.pose.mulPose(new Matrix4f().scale(1, 1, 0.01f));
            this.minecraft.getItemRenderer().render(p_281675_, ItemDisplayContext.GUI, false, this.pose, this.bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, bakedmodel);
            this.pose.popPose();
        }
    }

    public void blit(ResourceLocation p_282639_, int p_282732_, int p_283541_, int p_281760_, int p_283298_, int p_283429_, int p_282193_, int p_281980_, float p_282660_, float p_281522_, int p_282315_, int p_281436_) {
        this.innerBlit(p_282639_, p_282732_, p_283541_, p_281760_, p_283298_, p_283429_, (p_282660_ + 0.0F) / (float) p_282315_, (p_282660_ + (float) p_282193_) / (float) p_282315_, (p_281522_ + 0.0F) / (float) p_281436_, (p_281522_ + (float) p_281980_) / (float) p_281436_);
    }

    void innerBlit(ResourceLocation p_283461_, int p_281399_, int p_283222_, int p_283615_, int p_283430_, int p_281729_, float p_283247_, float p_282598_, float p_282883_, float p_283017_) {
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.text(p_283461_));
        Matrix4f matrix4f = pose().last().pose();
        buffer.addVertex(matrix4f, (float) p_281399_, (float) p_283615_, (float) p_281729_).setColor(255, 255, 255, 255).setUv(p_283247_, p_282883_).setLight(LightTexture.FULL_BRIGHT);
        buffer.addVertex(matrix4f, (float) p_281399_, (float) p_283430_, (float) p_281729_).setColor(255, 255, 255, 255).setUv(p_283247_, p_283017_).setLight(LightTexture.FULL_BRIGHT);
        buffer.addVertex(matrix4f, (float) p_283222_, (float) p_283430_, (float) p_281729_).setColor(255, 255, 255, 255).setUv(p_282598_, p_283017_).setLight(LightTexture.FULL_BRIGHT);
        buffer.addVertex(matrix4f, (float) p_283222_, (float) p_283615_, (float) p_281729_).setColor(255, 255, 255, 255).setUv(p_282598_, p_282883_).setLight(LightTexture.FULL_BRIGHT);
    }
}
