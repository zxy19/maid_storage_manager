package studio.fantasyit.maid_storage_manager.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import studio.fantasyit.maid_storage_manager.api.IGuiGraphics;

@Mixin(GuiGraphics.class)
public abstract class GuiGraphicMixin implements IGuiGraphics {
    @Mutable
    @Shadow
    @Final
    private PoseStack pose;

    @Override
    public void battery_shield$setPose(PoseStack pose) {
        this.pose = pose;
    }
}
