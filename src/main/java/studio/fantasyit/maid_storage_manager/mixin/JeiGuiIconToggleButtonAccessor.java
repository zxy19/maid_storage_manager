package studio.fantasyit.maid_storage_manager.mixin;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.elements.GuiIconToggleButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import studio.fantasyit.maid_storage_manager.api.IJEIButtonGetter;

@Mixin(GuiIconToggleButton.class)
public abstract class JeiGuiIconToggleButtonAccessor implements IJEIButtonGetter {
    @Accessor(value="area",remap = false)
    public abstract ImmutableRect2i getArea();
    @Accessor(value="onIcon",remap = false)
    public abstract IDrawable getOnIcon();
    @Accessor(value="offIcon",remap = false)
    public abstract IDrawable getOffIcon();
}
