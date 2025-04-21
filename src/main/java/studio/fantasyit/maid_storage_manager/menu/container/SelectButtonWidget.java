package studio.fantasyit.maid_storage_manager.menu.container;


import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.menu.base.ImageAsset;

import java.util.function.Function;

public class SelectButtonWidget<T> extends AbstractWidget {
    public T getData() {
        return option.data;
    }

    public record Option<T>(T data, ImageAsset image, ImageAsset hoverImage, Component tooltip) {
    }

    private boolean visible;
    private Option<T> option;
    private final Function<@Nullable T, Option<T>> getNext;
    private final AbstractContainerScreen<?> screen;


    public SelectButtonWidget(int x, int y,
                              Function<@Nullable T, Option<T>> getNext,
                              AbstractContainerScreen<?> screen) {
        super(x, y, 0, 0, Component.literal(""));
        this.getNext = getNext;
        this.option = getNext.apply(null);
        this.active = true;
        this.screen = screen;
        this.width = option.image.w;
        this.height = option.image.h;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int x, int y, float p) {
        if (!visible) return;
        if (this.isHovered) option.hoverImage.blit(guiGraphics, getX(), getY());
        else option.image.blit(guiGraphics, getX(), getY());
    }

    public void setOption(Option<T> option) {
        this.option = option;
        if (this.option == null)
            this.option = getNext.apply(null);
        this.width = this.option.image.w;
        this.height = this.option.image.h;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput p_259858_) {
        p_259858_.add(NarratedElementType.HINT, Component.translatable("narration.button", option.tooltip));
    }

    @Override
    protected boolean isValidClickButton(int p_93652_) {
        if(!visible) return false;
        return true;
    }

    @Override
    public boolean isMouseOver(double p_93672_, double p_93673_) {
        if (!visible) return false;
        return super.isMouseOver(p_93672_, p_93673_);
    }

    @Override
    public int getX() {
        return super.getX() + screen.getGuiLeft();
    }

    @Override
    public int getY() {
        return super.getY() + screen.getGuiTop();
    }

    @Override
    public void onClick(double p_93634_, double p_93635_) {
        if (!visible) return;
        super.onClick(p_93634_, p_93635_);
        option = getNext.apply(option.data);
        height = option.image.h;
        width = option.image.w;
    }

    @Nullable
    @Override
    public Tooltip getTooltip() {
        return Tooltip.create(option.tooltip);
    }

    public Component getTooltipComponent() {
        return option.tooltip;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}