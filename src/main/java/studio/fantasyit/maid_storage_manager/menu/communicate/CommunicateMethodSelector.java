package studio.fantasyit.maid_storage_manager.menu.communicate;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.communicate.data.TaskDefaultCommunicate;
import studio.fantasyit.maid_storage_manager.menu.base.ImageAsset;
import studio.fantasyit.maid_storage_manager.menu.container.SelectButtonWidget;
import studio.fantasyit.maid_storage_manager.registry.SoundEventRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class CommunicateMethodSelector extends AbstractWidget {


    static final ResourceLocation background = ResourceLocation.fromNamespaceAndPath(MaidStorageManager.MODID, "textures/gui/communicate_terminal.png");
    static final ImageAsset BACK1 = ImageAsset.from4Point(background, 219, 64, 245, 150);
    static final ImageAsset BACK2 = ImageAsset.from4Point(background, 219, 151, 244, 237);
    static final ImageAsset MANUAL = ImageAsset.from4Point(background, 176, 32, 218, 47);
    static final ImageAsset MANUAL_HOVER = ImageAsset.from4Point(background, 176, 48, 218, 63);
    static final ImageAsset PAGE_DOWN = ImageAsset.from4Point(background, 219, 50, 225, 53);
    static final ImageAsset PAGE_UP = ImageAsset.from4Point(background, 219, 54, 225, 57);
    static final ImageAsset ARROW = ImageAsset.from4Point(background, 226, 50, 236, 58);

    private final static int GAP_SIZE = 1;
    private final static int DIFF_X = 5;
    private final static int DIFF_Y = 13;


    List<SelectButtonWidget<ResourceLocation>> buttons = new ArrayList<>();
    List<CommunicateRollingTextWidget> texts = new ArrayList<>();
    final List<ResourceLocation> methods;
    int currentId = 0;
    int mOffset = 0;
    boolean visible = false;
    Consumer<ResourceLocation> callback = null;
    AbstractContainerScreen<?> screen;

    public CommunicateMethodSelector(int p_93629_, int p_93630_, AbstractContainerScreen<?> screen) {
        super(p_93629_, p_93630_, BACK1.w + BACK2.w, BACK1.h, Component.empty());
        this.methods = new ArrayList<>();
        methods.add(TaskDefaultCommunicate.DUMMY_AUTO_DETECT_TASK);
        methods.add(TaskDefaultCommunicate.DUMMY_USE_CURRENT_DATA);
        TaskDefaultCommunicate.each(methods::add);
        for (int i = 0; i < 4; i++) {
            texts.add(new CommunicateRollingTextWidget(0, 0, MANUAL.w - 2, MANUAL.h - 2, Component.empty()));
            buttons.add(new SelectButtonWidget<>(0, 0, this.getIdForButtonI(i), screen));
        }
        this.screen = screen;
    }

    public Function<@Nullable ResourceLocation, SelectButtonWidget.Option<ResourceLocation>> getIdForButtonI(int i) {
        return ca -> {
            if (ca == null) {
                ca = methods.get((currentId + i) % methods.size());
                texts.get(i).setText(TaskDefaultCommunicate.getTranslate(ca));
            } else {
                setSelected(ca);
                if (this.callback != null)
                    this.callback.accept(getSelected());
                this.hide();
            }
            return new SelectButtonWidget.Option<>(
                    ca,
                    MANUAL,
                    MANUAL_HOVER,
                    TaskDefaultCommunicate.getTranslate(ca)
            );
        };
    }

    public void setSelected(ResourceLocation id) {
        for (int i = 0; i < methods.size(); i++) {
            if (methods.get(i).equals(id)) {
                currentId = i;
                if (currentId >= methods.size() - 5) {
                    mOffset = currentId - methods.size() + 5;
                    currentId = methods.size() - 5;
                } else {
                    mOffset = 0;
                }
                buttons.forEach(b -> b.setOption(null));
                return;
            }
        }

    }

    public void expandFrom(AbstractWidget widget) {
        expandFrom(widget.getX(), widget.getY());
    }

    public void expandFrom(int x, int y) {
        for (int i = 0; i < buttons.size(); i++) {
            buttons.get(i).setX(x - screen.getGuiLeft());
            buttons.get(i).setY(y - screen.getGuiTop() + (MANUAL.h + GAP_SIZE) * i);
            texts.get(i).setX(buttons.get(i).getX() + 1);
            texts.get(i).setY(buttons.get(i).getY() + 1);
        }
        this.setX(x - DIFF_X);
        this.setY(y - DIFF_Y);
        this.visible = true;
        this.active = true;
    }

    public void hide() {
        this.visible = false;
        this.active = false;
    }

    public void setCallback(Consumer<ResourceLocation> callback) {
        this.callback = callback;
    }

    public ResourceLocation getSelected() {
        return methods.get(currentId + mOffset);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int x, int y, float pt) {
        if (!visible) return;
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 1000);
        int left = getX();
        int top = getY();
        BACK1.blit(guiGraphics, left, top);
        BACK2.blit(guiGraphics, left + BACK1.w, top);
        for (int i = 0; i < buttons.size(); i++) {
            buttons.get(i).render(guiGraphics, x, y, pt);
            texts.get(i).render(guiGraphics, x, y, pt);
        }
        if (currentId > 0)
            PAGE_UP.blit(guiGraphics, getX() + getWidth() / 2 - PAGE_UP.w / 2, getY() + 6);
        if (currentId < methods.size() - 5)
            PAGE_DOWN.blit(guiGraphics, getX() + getWidth() / 2 - PAGE_DOWN.w / 2, getY() - 1 + getHeight() - PAGE_DOWN.h);
        ARROW.blit(guiGraphics, left - 4, top + DIFF_Y + MANUAL.h / 2 - 4 + (MANUAL.h + GAP_SIZE) * mOffset);
        guiGraphics.pose().popPose();
    }

    @Override
    public void mouseMoved(double x, double y) {
        buttons.forEach(b -> b.mouseMoved(x, y));
    }

    long lastRollTick = 0;

    @Override
    public boolean mouseScrolled(double p_94734_, double p_94735_, double dx, double p_94736_) {
        long currentTime = System.currentTimeMillis();
        if (lastRollTick + 50 > currentTime) {
            return false;
        }
        lastRollTick = currentTime;
        if (p_94736_ < 0) {
            currentId++;
        } else {
            currentId--;
        }
        if (currentId < 0) {
            currentId = 0;
            if (mOffset > 0)
                mOffset--;
        }
        if (currentId >= methods.size() - 5) {
            currentId = methods.size() - 5;
            if (mOffset < 3)
                mOffset++;
        }
        if (callback != null) callback.accept(methods.get(currentId + mOffset));
        buttons.forEach(b -> b.setOption(null));
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEventRegistry.CRAFT_ACTION_ROLL.get(), 1.0F));
        return true;
    }

    @Override
    public boolean isMouseOver(double p_93672_, double p_93673_) {
        return super.isMouseOver(p_93672_, p_93673_);
    }

    @Override
    public boolean mouseClicked(double x, double y, int p_93643_) {
        if (visible && active)
            for (SelectButtonWidget<ResourceLocation> button : buttons) {
                if (button.mouseClicked(x, y, p_93643_)) {
                    return true;
                }
            }
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput p_259858_) {

    }
}
