package studio.fantasyit.maid_storage_manager.render.base;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import javax.annotation.Nullable;

public class CustomCommonGraphics implements ICustomGraphics {

    private final PoseStack pose;
    private final MultiBufferSource bufferSource;
    private final Minecraft minecraft;

    private static final Matrix4f GUI_MAT4 = (new Matrix4f()).scaling(1.0F, -1.0F, 1.0F).rotateY((-(float) Math.PI / 8F)).rotateX(2.3561945F);
    private static final Vector3f DIFFUSE_LIGHT_0 = (new Vector3f(0.2F, 1.0F, -0.7F)).normalize();
    private static final Vector3f DIFFUSE_LIGHT_1 = (new Vector3f(-0.2F, 1.0F, 0.7F)).normalize();

    public CustomCommonGraphics(Minecraft mc, PoseStack p_281669_, MultiBufferSource p_281893_) {
        this.minecraft = mc;
        this.pose = p_281669_;
        this.bufferSource = p_281893_;
    }

    public PoseStack pose() {
        return this.pose;
    }

    public void drawString(Font p_282636_, FormattedCharSequence p_281596_, float p_281586_, float p_282816_, int p_281743_, boolean p_282394_) {
        p_282636_.drawInBatch(p_281596_, p_281586_, p_282816_, p_281743_, p_282394_, this.pose.last().pose(), this.bufferSource, Font.DisplayMode.NORMAL, 0, 15728880);
    }

    public void drawString(Font p_283343_, @Nullable String p_281896_, float p_283569_, float p_283418_, int p_281560_, boolean p_282130_) {
        if (p_281896_ == null) {
            return;
        } else {
            p_283343_.drawInBatch(p_281896_, p_283569_, p_283418_, p_281560_, p_282130_, this.pose.last().pose(), this.bufferSource, Font.DisplayMode.NORMAL, 0, 15728880);
        }
    }


    public void flush() {
    }

    public void renderItem(ItemStack p_281978_, int p_282647_, int p_281944_) {
    }

    public void blit(Identifier p_282639_, int p_282732_, int p_283541_, int p_281760_, int p_283298_, int p_283429_, int p_282193_, int p_281980_, float p_282660_, float p_281522_, int p_282315_, int p_281436_) {
        this.innerBlit(p_282639_, p_282732_, p_283541_, p_281760_, p_283298_, p_283429_, (p_282660_ + 0.0F) / (float) p_282315_, (p_282660_ + (float) p_282193_) / (float) p_282315_, (p_281522_ + 0.0F) / (float) p_281436_, (p_281522_ + (float) p_281980_) / (float) p_281436_);
    }

    void innerBlit(Identifier p_283461_, int p_281399_, int p_283222_, int p_283615_, int p_283430_, int p_281729_, float p_283247_, float p_282598_, float p_282883_, float p_283017_) {
        VertexConsumer buffer = bufferSource.getBuffer(RenderTypes.text(p_283461_));
        Matrix4f matrix4f = pose().last().pose();
        buffer.addVertex(matrix4f, (float) p_281399_, (float) p_283615_, (float) p_281729_).setColor(255, 255, 255, 255).setUv(p_283247_, p_282883_).setLight(LightCoordsUtil.FULL_BRIGHT);
        buffer.addVertex(matrix4f, (float) p_281399_, (float) p_283430_, (float) p_281729_).setColor(255, 255, 255, 255).setUv(p_283247_, p_283017_).setLight(LightCoordsUtil.FULL_BRIGHT);
        buffer.addVertex(matrix4f, (float) p_283222_, (float) p_283430_, (float) p_281729_).setColor(255, 255, 255, 255).setUv(p_282598_, p_283017_).setLight(LightCoordsUtil.FULL_BRIGHT);
        buffer.addVertex(matrix4f, (float) p_283222_, (float) p_283615_, (float) p_281729_).setColor(255, 255, 255, 255).setUv(p_282598_, p_282883_).setLight(LightCoordsUtil.FULL_BRIGHT);
    }
}
