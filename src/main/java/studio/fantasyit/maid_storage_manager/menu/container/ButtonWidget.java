package studio.fantasyit.maid_storage_manager.menu.container;


import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;

import java.util.function.Supplier;

public class ButtonWidget extends AbstractWidget {
    @FunctionalInterface
    public interface UVSupplier {
        Pair<Integer, Integer> get(ButtonWidget buttonWidget);
    }
    private final Runnable callback;
    private final ResourceLocation image;
    private final UVSupplier uvSupplier;
    private final int x, y;
    private final Supplier<Component> tooltipSupplier;
    private final AbstractContainerScreen<?> screen;


    public ButtonWidget(int x,
                        int y,
                        int w,
                        int h,
                        ResourceLocation image,
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
    protected void renderWidget(GuiGraphics guiGraphics, int x, int y, float p) {
        Pair<Integer, Integer> uv = uvSupplier.get(this);
        guiGraphics.blit(this.image,
                getX(),
                getY(),
                0,
                uv.getA(),
                uv.getB(),
                this.width,
                this.height,
                256,
                256);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput p_259858_) {
        p_259858_.add(NarratedElementType.HINT, Component.translatable("narration.button", this.tooltipSupplier.get()));
    }

    @Override
    protected boolean isValidClickButton(int p_93652_) {
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
    public void onClick(double p_93634_, double p_93635_) {
        super.onClick(p_93634_, p_93635_);
        callback.run();
    }

    @Nullable
    @Override
    public Tooltip getTooltip() {
        return Tooltip.create(tooltipSupplier.get());
    }

    public Component getTooltipComponent() {
        return tooltipSupplier.get();
    }
}