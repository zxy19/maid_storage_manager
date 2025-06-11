package studio.fantasyit.maid_storage_manager.menu.craft.stone_cutter;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.anti_ad.mc.ipn.api.IPNIgnore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.menu.base.ImageAsset;
import studio.fantasyit.maid_storage_manager.menu.container.SelectButtonWidget;
import studio.fantasyit.maid_storage_manager.menu.craft.base.AbstractCraftScreen;
import studio.fantasyit.maid_storage_manager.network.CraftGuideGuiPacket;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;
import yalter.mousetweaks.api.MouseTweaksDisableWheelTweak;

import java.util.ArrayList;
import java.util.List;

@MouseTweaksDisableWheelTweak
@IPNIgnore
public class StoneCutterCraftScreen extends AbstractCraftScreen<StoneCutterCraftMenu> {
    private static final ResourceLocation background = new ResourceLocation(MaidStorageManager.MODID, "textures/gui/craft/type/stone_cutter.png");

    private final ImageAsset SLOT = new ImageAsset(background, 176, 16, 18, 18);
    private final ImageAsset SLOT_SELECTED = new ImageAsset(background, 176, 34, 18, 18);
    private final ImageAsset SLOT_HOVER = new ImageAsset(background, 176, 52, 18, 18);
    private final ImageAsset SCROLL_BAR = new ImageAsset(background, 176, 0, 12, 14);
    private final ImageAsset SCROLL_BAR_DISABLE = new ImageAsset(background, 188, 0, 12, 14);

    private final List<SelectButtonWidget<?>> buttons = new ArrayList<>();
    private boolean isScrolling = false;
    private int scrollTop = 0;

    public StoneCutterCraftScreen(StoneCutterCraftMenu p_97741_, Inventory p_97742_, Component p_97743_) {
        super(p_97741_, p_97742_, p_97743_, background, true);
        this.imageWidth = 176;
        this.imageHeight = 245;
        this.inventoryLabelY = this.imageHeight - 94;
        this.inventoryLabelX = 8;
    }

    @Override
    protected void slotClicked(Slot p_97778_, int p_97779_, int p_97780_, ClickType p_97781_) {
        super.slotClicked(p_97778_, p_97779_, p_97780_, p_97781_);
        buttons.forEach(b -> b.setOption(null));
        updateButtons();
    }

    @Override
    protected void init() {
        super.init();
        updateButtons();
    }

    @Override
    protected void addButtons() {
        buttons.clear();
        int sx = 39;
        int sy = 68;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 5; j++) {
                int x = sx + j * 18;
                int y = sy + i * 18;
                int index = i * 5 + j;
                buttons.add(this.addRenderableWidget(new SelectButtonWidget<>(
                        x,
                        y,
                        (data) -> {
                            if (!menu.stepDataContainer.getItem(1).isEmpty()
                                    && ItemStackUtil.isSameInCrafting(menu.stepDataContainer.getItem(1),
                                    menu.displayOnlySlots.getItem(index))) {
                                return new SelectButtonWidget.Option<>(
                                        1,
                                        SLOT_SELECTED,
                                        SLOT_SELECTED,
                                        null
                                );
                            }
                            if (data != null && !menu.displayOnlySlots.getItem(index).isEmpty()) {
                                sendAndTriggerLocalPacket(new CraftGuideGuiPacket(CraftGuideGuiPacket.Type.SET_ITEM,
                                        1,
                                        0,
                                        menu.displayOnlySlots.getItem(index).save(new CompoundTag())
                                ));
                            }
                            return new SelectButtonWidget.Option<>(
                                    1,
                                    SLOT,
                                    SLOT_HOVER,
                                    null
                            );
                        },
                        this
                )));
            }
        }
    }


    @Override
    protected void renderTooltip(@NotNull GuiGraphics graphics, int x, int y) {
        if (this.menu.getCarried().isEmpty()) {
            for (int i = 0; i < buttons.size(); i++) {
                ItemStack itemStack = menu.displayOnlySlots.getItem(i);
                if (buttons.get(i).isMouseOver(x, y)) {
                    if (!itemStack.isEmpty())
                        graphics.renderTooltip(this.font,
                                itemStack,
                                x,
                                y
                        );
                    return;
                }
            }
        }
        super.renderTooltip(graphics, x, y);
    }

    @Override
    public void render(GuiGraphics graphics, int p_283661_, int p_281248_, float p_281886_) {
        super.render(graphics, p_283661_, p_281248_, p_281886_);
        renderIcons(graphics);
        renderScrollbar(graphics);
    }

    private void renderScrollbar(GuiGraphics graphics) {
        int sy = 68;
        int x = 132;
        if (menu.maxPage == 1) {
            SCROLL_BAR_DISABLE.blit(graphics, getGuiLeft() + x, getGuiTop() + sy);
        } else {
            SCROLL_BAR.blit(graphics, getGuiLeft() + x, getGuiTop() + sy + scrollTop);
        }
    }

    private void renderIcons(GuiGraphics graphics) {
        for (int i = 0; i < buttons.size(); i++) {
            if (buttons.get(i).isVisible()) {
                graphics.renderItem(
                        menu.displayOnlySlots.getItem(i),
                        buttons.get(i).getX() + 1,
                        buttons.get(i).getY() + 1
                );
            }
        }
    }

    @Override
    public void handleGuiPacket(CraftGuideGuiPacket.Type type, int key, int value, @Nullable CompoundTag data) {
        switch (type) {
            case PAGE_DOWN, PAGE_UP -> {
                if (!isScrolling)
                    scrollTop = (int) (40.0 / Math.max(1, menu.maxPage - 1) * menu.page);
            }
            case SET_ITEM, SET_ALL_INPUT -> {
                buttons.forEach(b -> b.setOption(null));
            }
        }
        updateButtons();
    }

    @Override
    public boolean mouseScrolled(double p_94686_, double p_94687_, double p_94688_) {
        double inGuiX = p_94686_ - getGuiLeft();
        double inGuiY = p_94687_ - getGuiTop();
        if (inGuiX < 129 && inGuiY < 122 && inGuiX > 39 && inGuiY > 68) {
            if (p_94688_ > 0) {
                if (menu.page > 0) {
                    menu.page--;
                    sendAndTriggerLocalPacket(new CraftGuideGuiPacket(CraftGuideGuiPacket.Type.PAGE_DOWN, 0, menu.page, null));
                }
            } else {
                if (menu.page < menu.maxPage - 1) {
                    menu.page++;
                    sendAndTriggerLocalPacket(new CraftGuideGuiPacket(CraftGuideGuiPacket.Type.PAGE_UP, 0, menu.page, null));
                }
            }
        }
        return super.mouseScrolled(p_94686_, p_94687_, p_94688_);
    }

    @Override
    public boolean mouseClicked(double x, double y, int p_97750_) {
        if (x - getGuiLeft() >= 132 && y - getGuiTop() >= 68 && x - getGuiLeft() <= 143 && y - getGuiTop() <= 121) {
            isScrolling = true;
            return true;
        }
        return super.mouseClicked(x, y, p_97750_);
    }

    @Override
    public boolean mouseReleased(double p_97812_, double p_97813_, int p_97814_) {
        isScrolling = false;
        return super.mouseReleased(p_97812_, p_97813_, p_97814_);
    }

    @Override
    public boolean mouseDragged(double x, double y, int p_97754_, double p_97755_, double p_97756_) {
        if (isScrolling) {
            scrollTop = (int) Math.max(0, Math.min(40, y - getGuiTop() - 68 - 7));
            int newPage = (int) Math.round(scrollTop / 40.0 * Math.max(1, menu.maxPage - 1));
            if (menu.page != newPage) {
                sendAndTriggerLocalPacket(new CraftGuideGuiPacket(CraftGuideGuiPacket.Type.PAGE_DOWN, 0, newPage, null));
            }
        }
        return super.mouseDragged(x, y, p_97754_, p_97755_, p_97756_);
    }

    private void updateButtons() {
        for (int i = 0; i < buttons.size(); i++) {
            buttons.get(i).setVisible(!menu.displayOnlySlots.getItem(i).isEmpty());
        }
    }
}