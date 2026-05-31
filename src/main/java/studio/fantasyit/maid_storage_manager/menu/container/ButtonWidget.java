package studio.fantasyit.maid_storage_manager.menu.container;


import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.util.GuiTools;

import java.util.function.Supplier;

public class ButtonWidget extends AbstractWidget {
    @FunctionalInterface
    public interface UVSupplier {
        Pair<Integer, Integer> get(ButtonWidget buttonWidget);
    }
    private final Runnable callback;
    private final Identifier image;
    private final UVSupplier uvSupplier;
    private final int x, y;
    private final Supplier<Component> tooltipSupplier;
    private final AbstractContainerScreen<?> screen;


    public ButtonWidget(int x,
                        int y,
                        int w,
                        int h,
                        Identifier image,
                        UVSupplier uvSupplier,
                        Supplier<Component> tooltipSupplier,
                        Runnable callback,
                        AbstractContainerScreen<?> screen) {
        super(x, y, w, h, tooltipSupplier.get());
        this.x = x;
        this.y = y;
        this.uvSupplier = uvSupplier;
        this.tooltipSupplier = tooltipSupplier;
        this.callback = callback;
        this.image = image;
        this.active = true;
        this.screen = screen;
    }

    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor guiGraphics, int x, int y, float p) {
        Pair<Integer, Integer> uv = uvSupplier.get(this);
        GuiTools.guiBlit(guiGraphics,
                this.image,
                getX(),
                getY(),
                uv.getA(),
                uv.getB(),
                this.width,
                this.height);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput p_259858_) {
        p_259858_.add(NarratedElementType.HINT, Component.translatable("narration.button", this.tooltipSupplier.get()));
    }

    @Override
    protected boolean isValidClickButton(MouseButtonInfo buttonInfo) {
        return true;
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
    public void onClick(MouseButtonEvent p_93634_, boolean doubleClick) {
        super.onClick(p_93634_, doubleClick);
        callback.run();
    }

    @Nullable
    public Tooltip getTooltip() {
        return Tooltip.create(tooltipSupplier.get());
    }

    public Component getTooltipComponent() {
        return tooltipSupplier.get();
    }
}