package studio.fantasyit.maid_storage_manager.api;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.common.util.ImmutableRect2i;

public interface IJEIButtonGetter {
    ImmutableRect2i getArea();
    IDrawable getOnIcon();
    IDrawable getOffIcon();
}
