package studio.fantasyit.maid_storage_manager.menu.craft.common;

import com.mojang.blaze3d.systems.RenderSystem;
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
import studio.fantasyit.maid_storage_manager.craft.CraftManager;
import studio.fantasyit.maid_storage_manager.craft.action.CraftAction;
import studio.fantasyit.maid_storage_manager.menu.base.ImageAsset;
import studio.fantasyit.maid_storage_manager.menu.container.SelectButtonWidget;
import studio.fantasyit.maid_storage_manager.registry.SoundEventRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class CommonActionSelectionWidget extends AbstractWidget {
    private final static ResourceLocation background = ResourceLocation.fromNamespaceAndPath(MaidStorageManager.MODID, "textures/gui/craft/type/common.png");
    private final static ImageAsset BUTTON_NORMAL = new ImageAsset(background, 176, 214, 56, 15);
    private final static ImageAsset BUTTON_HOVER = new ImageAsset(background, 176, 229, 56, 15);
    private final static ImageAsset ARROW = new ImageAsset(background, 219, 120, 11, 9);
    private final static ImageAsset BACK = new ImageAsset(background, 176, 131, 70, 83);

    private final static int GAP_SIZE = 1;
    private final static int DIFF_X = 6;
    private final static int DIFF_Y = 13;


    List<SelectButtonWidget<CraftAction>> buttons = new ArrayList<>();
    final List<CraftAction> actions;
    int currentId = 0;
    boolean visible = false;
    Consumer<CraftAction> callback = null;
    AbstractContainerScreen<?> screen;

    public CommonActionSelectionWidget(int p_93629_, int p_93630_, AbstractContainerScreen<?> screen) {
        super(p_93629_, p_93630_, BACK.w, BACK.h, Component.literal(""));
        actions = CraftManager.getInstance().getCommonActions();
        for (int i = 0; i < 4; i++) {
            buttons.add(new SelectButtonWidget<>(0, 0, this.getIdForButtonI(i), screen));
        }
        this.screen = screen;
    }

    public Function<@Nullable CraftAction, SelectButtonWidget.Option<CraftAction>> getIdForButtonI(int i) {
        return ca -> {
            if (ca == null) {
                ca = actions.get((currentId + i) % actions.size());
            } else {
                setSelectedAction(ca);
                if (this.callback != null)
                    this.callback.accept(getSelected());
                this.hide();
            }
            return new SelectButtonWidget.Option<>(
                    ca,
                    BUTTON_NORMAL,
                    BUTTON_HOVER,
                    CommonCraftAssets.translationForAction(ca.type())
            );
        };
    }

    public void setSelectedAction(CraftAction action) {
        for (int i = 0; i < actions.size(); i++) {
            if (actions.get(i).type().equals(action.type())) {
                currentId = i;
                buttons.forEach(b -> b.setOption(null));
                return;
            }
        }

    }

    public void expandFrom(AbstractWidget widget) {
        for (int i = 0; i < buttons.size(); i++) {
            buttons.get(i).setX(widget.getX() - screen.getGuiLeft());
            buttons.get(i).setY(widget.getY() - screen.getGuiTop() + (BUTTON_NORMAL.h + GAP_SIZE) * i);
        }
        this.setX(widget.getX() - DIFF_X);
        this.setY(widget.getY() - DIFF_Y);
        this.visible = true;
        this.active = true;
    }

    public void hide() {
        this.visible = false;
        this.active = false;
    }

    public void setCallback(Consumer<CraftAction> callback) {
        this.callback = callback;
    }

    public CraftAction getSelected() {
        return actions.get(currentId);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int x, int y, float pt) {
        if (!visible) return;
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 1000);
        int left = getX();
        int top = getY();
        BACK.blit(guiGraphics, left, top);
        for (SelectButtonWidget<CraftAction> button : buttons) {
            button.render(guiGraphics, x, y, pt);
            CommonCraftAssets
                    .imageForAction(button.getData().type())
                    .blit(guiGraphics,
                            button.getX() + 2,
                            button.getY() + 2
                    );
            RenderSystem.disableDepthTest();
            guiGraphics.drawString(
                    this.screen.getMinecraft().font,
                    CommonCraftAssets.translationForAction(button.getData().type()),
                    button.getX() + 18,
                    button.getY() + 3,
                    0xffffffff
            );
            RenderSystem.disableDepthTest();
        }
        ARROW.blit(guiGraphics, left - 4, top + DIFF_Y + BUTTON_HOVER.h / 2 - 4);
        guiGraphics.pose().popPose();
    }

    @Override
    public void mouseMoved(double x, double y) {
        buttons.forEach(b -> b.mouseMoved(x, y));
    }

    long lastRollTick = 0;

    @Override
    public boolean mouseScrolled(double p_94734_, double p_94735_, double p_94736_) {
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
            currentId = actions.size() - 1;
        }
        if (currentId >= actions.size()) {
            currentId = 0;
        }
        if (callback != null) callback.accept(actions.get(currentId));
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
            for (SelectButtonWidget<CraftAction> button : buttons) {
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
