package studio.fantasyit.maid_storage_manager.render.base;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.Queue;

public class CustomGraphics implements ICustomGraphics {

    private final PoseStack pose;
    private final SubmitNodeCollector submitNodeCollector;
    private final Minecraft minecraft;

    private record ItemStackRenderInfo(LivingEntity entity, ItemStack stack, PoseStack pose, int x, int y, int state) {
    }

    private Queue<ItemStackRenderInfo> itemStackRenderQueue = new LinkedList<>();

    public CustomGraphics(Minecraft mc, PoseStack p_281669_, SubmitNodeCollector submitNodeCollector) {
        this.minecraft = mc;
        this.pose = p_281669_;
        this.submitNodeCollector = submitNodeCollector;
    }

    public PoseStack pose() {
        return this.pose;
    }

    public void drawString(Font p_282636_, FormattedCharSequence p_281596_, float p_281586_, float p_282816_, int p_281743_, boolean p_282394_) {
        this.submitNodeCollector.submitText(this.pose, p_281586_, p_282816_, p_281596_, p_282394_, Font.DisplayMode.NORMAL, LightCoordsUtil.FULL_BRIGHT, p_281743_, 0, 0);
    }

    public void drawString(Font p_283343_, @Nullable String p_281896_, float p_283569_, float p_283418_, int p_281560_, boolean p_282130_) {
        if (p_281896_ == null) {
            return;
        }
        this.submitNodeCollector.submitText(this.pose, p_283569_, p_283418_, FormattedCharSequence.forward(p_281896_, Style.EMPTY), p_282130_, Font.DisplayMode.NORMAL, LightCoordsUtil.FULL_BRIGHT, p_281560_, 0, 0);
    }


    public void flush() {
    }

    public void renderItem(ItemStack p_281978_, int x, int y) {
        ItemStackRenderState isrs = new ItemStackRenderState();
        Minecraft.getInstance().getItemModelResolver().updateForTopItem(isrs, p_281978_, ItemDisplayContext.GUI, minecraft.level, minecraft.player, 0);
        this.pose.pushPose();
        this.pose.scale(10, 10, 10);
        this.pose.translate(x, y, -10000);
        isrs.submit(this.pose, this.submitNodeCollector, LightCoordsUtil.FULL_BRIGHT, 0, 0);
        this.pose.popPose();
    }

    public void blit(Identifier p_282639_, int p_282732_, int p_283541_, int p_281760_, int p_283298_, int p_283429_, int p_282193_, int p_281980_, float p_282660_, float p_281522_, int p_282315_, int p_281436_) {
        this.innerBlit(p_282639_, p_282732_, p_283541_, p_281760_, p_283298_, p_283429_, (p_282660_ + 0.0F) / (float) p_282315_, (p_282660_ + (float) p_282193_) / (float) p_282315_, (p_281522_ + 0.0F) / (float) p_281436_, (p_281522_ + (float) p_281980_) / (float) p_281436_);
    }

    void innerBlit(Identifier p_283461_, int p_281399_, int p_283222_, int p_283615_, int p_283430_, int p_281729_, float u0, float u1, float v0, float v1) {
        this.submitNodeCollector.submitCustomGeometry(this.pose, RenderTypes.text(p_283461_), (poseState, consumer) -> {
            Matrix4f matrix4f = poseState.pose();
            consumer.addVertex(matrix4f, (float) p_281399_, (float) p_283615_, (float) p_281729_).setColor(255, 255, 255, 255).setUv(u0, v0).setLight(LightCoordsUtil.FULL_BRIGHT);
            consumer.addVertex(matrix4f, (float) p_281399_, (float) p_283430_, (float) p_281729_).setColor(255, 255, 255, 255).setUv(u0, v1).setLight(LightCoordsUtil.FULL_BRIGHT);
            consumer.addVertex(matrix4f, (float) p_283222_, (float) p_283430_, (float) p_281729_).setColor(255, 255, 255, 255).setUv(u1, v1).setLight(LightCoordsUtil.FULL_BRIGHT);
            consumer.addVertex(matrix4f, (float) p_283222_, (float) p_283615_, (float) p_281729_).setColor(255, 255, 255, 255).setUv(u1, v0).setLight(LightCoordsUtil.FULL_BRIGHT);
        });
    }
}
