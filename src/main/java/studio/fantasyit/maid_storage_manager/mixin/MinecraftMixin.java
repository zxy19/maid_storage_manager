package studio.fantasyit.maid_storage_manager.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderBuffers;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import studio.fantasyit.maid_storage_manager.api.IGuiGraphics;
import studio.fantasyit.maid_storage_manager.api.IGuiGraphicsGetter;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin implements IGuiGraphicsGetter {
        @Shadow
        @Final
        private RenderBuffers renderBuffers;

        private GuiGraphics getGuiGraphicsRaw() {
            return new GuiGraphics((Minecraft) (Object) this, renderBuffers.bufferSource());
        }

        @Override
        public GuiGraphics getGuiGraphics(PoseStack pose) {
            final GuiGraphics graphics = getGuiGraphicsRaw();
            ((IGuiGraphics) graphics).battery_shield$setPose(pose);
            return graphics;
        }

}
