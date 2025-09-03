package studio.fantasyit.maid_storage_manager.menu.craft.common;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.PacketDistributor;
import org.anti_ad.mc.ipn.api.IPNIgnore;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.craft.CraftManager;
import studio.fantasyit.maid_storage_manager.craft.action.ActionOption;
import studio.fantasyit.maid_storage_manager.craft.action.CraftAction;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.menu.base.AbstractFilterScreen;
import studio.fantasyit.maid_storage_manager.menu.base.ImageAsset;
import studio.fantasyit.maid_storage_manager.menu.container.FilterSlot;
import studio.fantasyit.maid_storage_manager.menu.container.NoPlaceFilterSlot;
import studio.fantasyit.maid_storage_manager.menu.container.SelectButtonWidget;
import studio.fantasyit.maid_storage_manager.menu.craft.base.ICraftGuiPacketReceiver;
import studio.fantasyit.maid_storage_manager.network.CraftGuideGuiPacket;
import studio.fantasyit.maid_storage_manager.network.Network;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;
import yalter.mousetweaks.api.MouseTweaksDisableWheelTweak;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@MouseTweaksDisableWheelTweak
@IPNIgnore
public class CommonCraftScreen extends AbstractFilterScreen<CommonCraftMenu> implements ICraftGuiPacketReceiver {
    CommonActionSelectionWidget actionSelector;
    SelectButtonWidget<CraftAction> actionSelectorButton;
    SelectButtonWidget<?> sortButtonUp, sortButtonDown;
    List<Pair<SelectButtonWidget<Integer>, EditBox>> options = new ArrayList<>();

    public CommonCraftScreen(CommonCraftMenu p_97741_, Inventory p_97742_, Component p_97743_) {
        super(p_97741_, p_97742_, p_97743_);
        this.imageWidth = 176;
        this.imageHeight = 245;
        this.inventoryLabelY = this.imageHeight - 94;
        this.inventoryLabelX = 8;
    }

    @Override
    protected void init() {
        super.init();
        addOptionButtons();
        addSortButtons();
        addActionButtons();
        actionSelector = new CommonActionSelectionWidget(0, 0, this);
        updateButtons();
    }

    private void syncOption(int idx, Integer nv, String value) {
        if (value == null)
            value = options.get(idx).getB().getValue();
        if (nv == null)
            nv = options.get(idx).getA().getData();
        sendAndTriggerLocalPacket(new CraftGuideGuiPacket(CraftGuideGuiPacket.Type.OPTION, idx, nv, CraftGuideGuiPacket.singleValue(value)));
    }

    //region buttons
    private void addOptionButtons() {
        final int sx = 113;
        int sy = 96;
        for (int i = 0; i < 2; i++) {
            final int optionIdx = i;
            SelectButtonWidget<Integer> btn = addRenderableWidget(new SelectButtonWidget<>(
                    sx, sy + i * 12,
                    (value) -> {
                        if (menu.currentEditingItems.options.isEmpty()) {
                            return new SelectButtonWidget.Option<>(
                                    0,
                                    CommonCraftAssets.BTN_OPTION,
                                    CommonCraftAssets.BTN_OPTION_HOVER,
                                    Component.empty()
                            );
                        }
                        ActionOption<?> opt = menu.currentEditingItems.options.get(optionIdx);
                        int nv = (value == null ? menu.currentEditingItems.step.getOptionSelectionId(opt).orElse(0) : value + 1);
                        nv %= opt.tooltip().length;
                        if (value != null) {
                            syncOption(optionIdx, nv, null);
                        }
                        return new SelectButtonWidget.Option<>(
                                nv,
                                CommonCraftAssets.BTN_OPTION,
                                CommonCraftAssets.BTN_OPTION_HOVER,
                                opt.tooltip()[nv]
                        );
                    },
                    this
            ));

            EditBox editBox = addRenderableWidget(new EditBox(font,
                    getGuiLeft() + sx + 14,
                    getGuiTop() + sy + i * 12,
                    21,
                    9,
                    Component.literal("")));
            editBox.setValue("");
            editBox.setBordered(false);
            editBox.setFilter(s -> {
                if (menu.currentEditingItems.options.isEmpty()) return false;
                ActionOption<?> opt = menu.currentEditingItems.options.get(optionIdx);
                return opt.valuePredicatorOrGetter().predicate(s);
            });
            editBox.setResponder(t -> {
                ActionOption<?> opt = menu.currentEditingItems.options.get(optionIdx);
                if (opt.valuePredicatorOrGetter().hasPredicator()) {
                    syncOption(optionIdx, null, t);
                }
            });
            options.add(new Pair<>(btn, editBox));
        }
    }

    private void addSortButtons() {
        sortButtonUp = addRenderableWidget(new SelectButtonWidget<Integer>(91, 129, (value) -> {
            if (value != null) {
                sendAndTriggerLocalPacket(new CraftGuideGuiPacket(CraftGuideGuiPacket.Type.UP, 0));
            }
            return new SelectButtonWidget.Option<>(
                    0,
                    CommonCraftAssets.BTN_UP,
                    CommonCraftAssets.BTN_UP,
                    Component.translatable("gui.maid_storage_manager.craft_guide.common.up")
            );
        }, this));
        sortButtonDown = addRenderableWidget(new SelectButtonWidget<Integer>(91, 136, (value) -> {
            if (value != null) {
                sendAndTriggerLocalPacket(new CraftGuideGuiPacket(CraftGuideGuiPacket.Type.DOWN, 0));
            }
            return new SelectButtonWidget.Option<>(
                    0,
                    CommonCraftAssets.BTN_DOWN,
                    CommonCraftAssets.BTN_DOWN,
                    Component.translatable("gui.maid_storage_manager.craft_guide.common.down")
            );
        }, this));
    }

    private void addActionButtons() {
        actionSelectorButton = addRenderableWidget(new SelectButtonWidget<>(
                112, 73,
                (value) -> {
                    if (value == null) {
                        CraftAction action = menu.currentEditingItems.step != null ? menu.currentEditingItems.actionType : CraftManager.getInstance().getDefaultAction();
                        return new SelectButtonWidget.Option<>(
                                action,
                                CommonCraftAssets.BTN_ACTION,
                                CommonCraftAssets.BTN_ACTION_HOVER,
                                CommonCraftAssets.translationForAction(action.type())
                        );
                    }
                    actionSelector.setCallback(t -> {
                        actionSelectorButton.setOption(new SelectButtonWidget.Option<>(
                                t,
                                CommonCraftAssets.BTN_ACTION,
                                CommonCraftAssets.BTN_ACTION_HOVER,
                                CommonCraftAssets.translationForAction(t.type())
                        ));
                        CompoundTag tag = new CompoundTag();
                        tag.putString("ns", t.type().getNamespace());
                        tag.putString("id", t.type().getPath());
                        sendAndTriggerLocalPacket(new CraftGuideGuiPacket(CraftGuideGuiPacket.Type.SET_MODE, 0, tag));
                    });
                    actionSelector.expandFrom(actionSelectorButton);
                    return new SelectButtonWidget.Option<>(
                            value,
                            CommonCraftAssets.BTN_ACTION,
                            CommonCraftAssets.BTN_ACTION_HOVER,
                            CommonCraftAssets.translationForAction(value.type())
                    );
                },
                this
        ));
    }

    //endregion

    private void sendAndTriggerLocalPacket(CraftGuideGuiPacket packet) {
        Network.INSTANCE.send(
                PacketDistributor.SERVER.noArg(),
                packet);
        menu.handleGuiPacket(packet.type, packet.key, packet.value, packet.data);
        this.handleGuiPacket(packet.type, packet.key, packet.value, packet.data);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float p_97788_, int p_97789_, int p_97790_) {
        renderBackground(guiGraphics);
        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - this.imageHeight) / 2;

        guiGraphics.blit(CommonCraftAssets.BACKGROUND,
                relX,
                relY,
                0,
                0,
                0,
                this.imageWidth,
                this.imageHeight,
                256,
                256);

        int c = 0;
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 2; y++) {
                if (menu.filterSlots[c].isActive()) {
                    ImageAsset imageAsset = (menu.isHandRelated && c == 1 && menu.filterSlots[c].getItem().isEmpty()) ? CommonCraftAssets.SLOT_HAND : CommonCraftAssets.SLOT_NORMAL;
                    imageAsset.blit(guiGraphics, relX + menu.filterSlots[c].x - 1, relY + menu.filterSlots[c].y - 1);
                }
                c++;
            }
        }
    }

    @Override
    protected void renderTooltip(@NotNull GuiGraphics graphics, int x, int y) {
        if (actionSelector.visible) return;
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 200);
        RenderSystem.enableDepthTest();
        if (this.menu.getCarried().isEmpty()) {
            int inGuiX = x - this.getGuiLeft();
            int inGuiY = y - this.getGuiTop();
            for (Slot slot : this.getMenu().slots) {
                if (slot.x <= inGuiX && slot.x + 16 >= inGuiX && slot.y <= inGuiY && slot.y + 16 >= inGuiY) {
                    if (slot instanceof FilterSlot filterSlot && filterSlot.isActive()) {
                        if (!filterSlot.getItem().isEmpty())
                            graphics.renderTooltip(this.font,
                                    filterSlot.getItem(),
                                    x,
                                    y
                            );
                        return;
                    }
                }
            }
            this.children().forEach(renderable -> {
                if (renderable.isMouseOver(x, y)) {
                    if (renderable instanceof SelectButtonWidget<?> buttonWidget && buttonWidget.isActive()) {
                        graphics.renderTooltip(this.font,
                                buttonWidget.getTooltipComponent(),
                                x,
                                y
                        );
                    }
                }
            });
        }
        super.renderTooltip(graphics, x, y);
        graphics.flush();
        graphics.pose().popPose();
    }

    @Override
    public void render(GuiGraphics graphics, int p_283661_, int p_281248_, float p_281886_) {
        super.render(graphics, p_283661_, p_281248_, p_281886_);
        renderOptionEditorOrTip(graphics);
        renderBlockIndicator(graphics);
        renderNumberLabel(graphics);
        renderButtonIcon(graphics);
        renderArrow(graphics);
        renderScrollList(graphics, p_283661_, p_281248_);
        renderScrollBar(graphics, p_283661_, p_281248_);
        renderMiniBarInfo(graphics);
        actionSelector.render(graphics, p_283661_, p_281248_, p_281886_);
        renderTooltip(graphics, p_283661_, p_281248_);
    }


    private void renderOptionEditorOrTip(@NotNull GuiGraphics graphics) {
        for (int i = 0; i < options.size(); i++) {
            if (menu.currentEditingItems.options.size() <= i) continue;
            ActionOption opt = menu.currentEditingItems.options.get(i);
            Pair<SelectButtonWidget<Integer>, EditBox> editBox = options.get(i);
            if (editBox.getB().isVisible())
                CommonCraftAssets.OPTION_UNDERLINE.blit(graphics, editBox.getB().getX(), editBox.getB().getY() + editBox.getB().getHeight());
            else if (editBox.getA().isVisible() && !opt.valuePredicatorOrGetter().hasPredicator()) {
                Object ab = opt.converter().ab(editBox.getA().getData());
                graphics.pose().pushPose();
                graphics.pose().translate(editBox.getB().getX(), editBox.getB().getY() + 2, 0);
                drawCenteredString(
                        graphics,
                        font,
                        (Component) opt.valuePredicatorOrGetter().getValue(ab).orElse(Component.empty()),
                        0,
                        0,
                        23,
                        0xFFFFFFFF,
                        true
                );
                graphics.pose().popPose();
            }

            if (editBox.getA().isVisible()) {
                ResourceLocation asset = opt.icon()[editBox.getA().getData()];
                graphics.blit(asset, editBox.getA().getX(), editBox.getA().getY(), 0, 0, 11, 11, 11, 11);
            }
        }
    }

    private void renderArrow(@NotNull GuiGraphics graphics) {
        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - this.imageHeight) / 2;
        if (menu.currentEditingItems.inputCount > 0) {
            CommonCraftAssets.ARROW_DOWN.blit(graphics, relX + 118, relY + 60);
        }
        if (menu.currentEditingItems.outputCount > 0) {
            CommonCraftAssets.ARROW_UP.blit(graphics, relX + 138, relY + 60);
        }
    }

    private void renderNumberLabel(@NotNull GuiGraphics graphics) {
        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - this.imageHeight) / 2;
        for (Slot slot : this.getMenu().slots) {
            if (slot instanceof FilterSlot filterSlot && filterSlot.container instanceof CommonStepDataContainer sdc && filterSlot.isActive()) {
                if (filterSlot.hasItem()) {
                    int count = sdc.getCount(filterSlot.getContainerSlot());
                    String text = String.valueOf(count);
                    if (count == -1) {
                        text = "*";
                    }
                    graphics.pose().pushPose();
                    graphics.pose().scale(0.6f, 0.6f, 1);
                    graphics.pose().translate(0, 0, 350);
                    RenderSystem.enableDepthTest();
                    graphics.drawString(this.font, text,
                            (int) ((relX + filterSlot.x + 16 - this.font.width(text) * 0.6) / 0.6f),
                            (int) ((relY + filterSlot.y + 16 - this.font.lineHeight * 0.6) / 0.6f),
                            0xffffff);
                    graphics.pose().popPose();
                }
            }
        }
        graphics.flush();
    }

    private void renderButtonIcon(@NotNull GuiGraphics graphics) {
        if (actionSelectorButton.isVisible()) {
            CommonCraftAssets.imageForAction(actionSelectorButton.getData().type())
                    .blit(graphics,
                            actionSelectorButton.getX() + 2,
                            actionSelectorButton.getY() + 2
                    );
        }
        graphics.flush();
    }

    private void renderBlockIndicator(@NotNull GuiGraphics graphics) {
        graphics.pose().pushPose();
        float scale = 1.3f;
        graphics.pose().scale(scale, scale, 1);
        graphics.renderItem(
                menu.blockIndicator.getItem(),
                (int) ((this.leftPos + menu.blockIndicator.x - 1) / scale),
                (int) ((this.topPos + menu.blockIndicator.y + 1) / scale)
        );
        graphics.pose().popPose();
    }

    private void renderMiniBarInfo(@NotNull GuiGraphics graphics) {
        if (menu.currentEditingItems.step == null) {
            graphics.pose().pushPose();
            graphics.pose().translate(getGuiLeft() + 36, getGuiTop() + 131, 0);
            graphics.pose().scale(0.6f, 0.6f, 1);
            graphics.drawString(font,
                    Component.translatable("gui.maid_storage_manager.craft_guide.common.no_step_selected"),
                    0,
                    0,
                    0xffffffff);
            graphics.pose().popPose();
            return;
        }
        NoPlaceFilterSlot bi = menu.blockIndicatorForSteps.get(menu.selectedIndex);
        graphics.pose().pushPose();
        graphics.pose().translate(getGuiLeft() + 28, getGuiTop() + 128, 0);
        graphics.pose().scale(0.7f, 0.7f, 1);
        graphics.renderItem(bi.getItem(), 0, 0);
        graphics.pose().popPose();

        graphics.pose().pushPose();
        graphics.pose().translate(getGuiLeft() + 41, getGuiTop() + 128, 0);
        graphics.pose().scale(0.55f, 0.55f, 1);
        graphics.drawString(font,
                Component.translatable("gui.maid_storage_manager.craft_guide.common.step_index", menu.selectedIndex + 1),
                0,
                0,
                0xffffffff
        );
        graphics.pose().popPose();

        graphics.pose().pushPose();
        graphics.pose().translate(getGuiLeft() + 41, getGuiTop() + 134, 0);
        graphics.pose().scale(0.65f, 0.65f, 1);
        graphics.drawString(font,
                Component.translatable(
                        "gui.maid_storage_manager.craft_guide.common.step_pos",
                        menu.currentEditingItems.step.storage.pos.getX(),
                        menu.currentEditingItems.step.storage.pos.getY(),
                        menu.currentEditingItems.step.storage.pos.getZ(),
                        Component.translatable(
                                "gui.maid_storage_manager.craft_guide.common.side_" +
                                        menu.currentEditingItems.step.storage.getSide().map(t -> t.name().toLowerCase()).orElse("none")
                        )
                ),
                0,
                0,
                0xffffffff
        );
        graphics.pose().popPose();
    }


    @Override
    public boolean mouseScrolled(double x, double y, double p_94688_) {
        if (actionSelector.visible) return actionSelector.mouseScrolled(x, y, p_94688_);
        @Nullable Slot slot = this.getSlotUnderMouse();
        if (slot instanceof FilterSlot filterSlot && filterSlot.container instanceof CommonStepDataContainer sdc) {
            MutableInt count = new MutableInt(sdc.getCount(filterSlot.getContainerSlot()));
            int dv = (int) (Math.abs(p_94688_) / p_94688_);
            if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), InputConstants.KEY_LSHIFT))
                dv *= 10;

            if (dv > 0) {
                if (count.addAndGet(dv) == 0) count.addAndGet(1);
            } else {
                if (count.addAndGet(dv) <= 0) count.setValue(1);
            }
            sendAndTriggerLocalPacket(
                    new CraftGuideGuiPacket(
                            CraftGuideGuiPacket.Type.COUNT,
                            filterSlot.index,
                            count.getValue()
                    )
            );
            return true;
        }
        Optional<GuiEventListener> child = this.getChildAt(x, y);
        if (child.isPresent() && child.get() instanceof EditBox eb && eb.isVisible()) {
            int dv = (int) (Math.abs(p_94688_) / p_94688_);
            if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), InputConstants.KEY_LSHIFT))
                dv *= 10;
            Integer ov = Integer.valueOf(eb.getValue());
            eb.setValue(String.valueOf(ov + dv));
            return true;
        }
        if (x - getGuiLeft() > 26 && y - getGuiTop() > 26 && x - getGuiLeft() < 100 && y - getGuiTop() < 119) {
            scroll((float) -p_94688_ * 3);
        }
        return super.mouseScrolled(x, y, p_94688_);
    }

    @Override
    public void accept(FilterSlot slot, ItemStack item) {
        if (slot instanceof NoPlaceFilterSlot) return;
        if (!slot.isActive()) return;
        slot.set(item);
        sendAndTriggerLocalPacket(new CraftGuideGuiPacket(CraftGuideGuiPacket.Type.SET_ITEM, slot.index, 0, ItemStackUtil.saveStack(item)));
    }

    @Override
    public List<FilterSlot> getSlots() {
        return this.menu.slots.stream()
                .filter(slot -> slot instanceof FilterSlot fs && !(slot instanceof NoPlaceFilterSlot) && slot.isActive())
                .map(slot -> (FilterSlot) slot)
                .toList();
    }

    @Override
    public void handleGuiPacket(CraftGuideGuiPacket.Type type, int key, int value, @Nullable CompoundTag data) {
        if (type == CraftGuideGuiPacket.Type.SELECT || type == CraftGuideGuiPacket.Type.SET_MODE) {
            updateButtons();
        }
        if (type == CraftGuideGuiPacket.Type.SELECT) {
            actionSelectorButton.setOption(null);
        }
    }

    private void updateButtons() {
        List<ActionOption<?>> actionOptions = menu.currentEditingItems.options;
        for (int i = 0; i < options.size(); i++) {
            boolean show = i < actionOptions.size();
            options.get(i).getA().setVisible(show);
            if (show) {
                options.get(i).getA().setOption(null);
            }
            if (show && actionOptions.get(i).valuePredicatorOrGetter().hasPredicator()) {
                options.get(i).getB().setVisible(true);
                options.get(i).getB().setFilter(actionOptions.get(i).valuePredicatorOrGetter()::predicate);
                options.get(i).getB().setValue(actionOptions.get(i).getOptionValue(menu.currentEditingItems.step));
            } else {
                options.get(i).getB().setVisible(false);
            }
        }
        boolean hasStepSelected = (menu.currentEditingItems.step != null);
        actionSelectorButton.setVisible(hasStepSelected);
        sortButtonDown.setVisible(hasStepSelected);
        sortButtonUp.setVisible(hasStepSelected);
    }

    private Component getStorageSideTranslate(Target target) {
        return Component.translatable("gui.maid_storage_manager.craft_guide.common.side",
                Component.translatable(
                        "gui.maid_storage_manager.craft_guide.common.side_" +
                                target.getSide().map(t -> t.name().toLowerCase()).orElse("none")
                )
        );
    }

    @Override
    public boolean mouseClicked(double x, double y, int p_97750_) {
        if (actionSelector.visible) {
            if (!actionSelector.isMouseOver(x, y)) {
                actionSelector.hide();
            } else return actionSelector.mouseClicked(x, y, p_97750_);
        }
        if (isInScrollBlockArea(x, y)) {
            mouseDraggingScrollingBar = y;
            mouseStartDraggingOffset = (double) scrollOffsetTop;
        }

        if (x - getGuiLeft() > 26 && y - getGuiTop() > 26 && x - getGuiLeft() < 100 && y - getGuiTop() < 119) {
            clickInScrollList(x, y);
        }
        return super.mouseClicked(x, y, p_97750_);
    }


    //region scrolling control
    private static final int SCROLL_AREA_TOTAL_HEIGHT = 93;
    private float scrollOffsetTop = 0;
    private Double mouseDraggingScrollingBar = null;
    private Double mouseStartDraggingOffset = null;

    private float getScrollBlockHeight() {
        return Math.min(2 * SCROLL_AREA_TOTAL_HEIGHT - menu.craftGuideData.steps.size() * (CommonCraftAssets.ROW.h - 1), SCROLL_AREA_TOTAL_HEIGHT + 6);
    }

    private void makeScreenScissor(GuiGraphics graphics) {
        graphics.enableScissor(getGuiLeft() + 26, getGuiTop() + 26, getGuiLeft() + 100, getGuiTop() + 119);
    }

    private void releaseScreenScissor(GuiGraphics graphics) {
        graphics.disableScissor();
    }

    private void scroll(float delta) {
        scrollOffsetTop += delta;
        if (scrollOffsetTop > SCROLL_AREA_TOTAL_HEIGHT + 6 - getScrollBlockHeight()) {
            scrollOffsetTop = SCROLL_AREA_TOTAL_HEIGHT + 6 - getScrollBlockHeight();
        }
        if (scrollOffsetTop < 0) {
            scrollOffsetTop = 0;
        }
    }

    private int getSelectedStep(double x, double y) {
        int rh = CommonCraftAssets.ROW.h - 1;

        if (x - getGuiLeft() > 26 && y - getGuiTop() > 26 && x - getGuiLeft() < 100 && y - getGuiTop() < 119) {
            int offsetY = (int) (y - getGuiTop() - 26);
            return (int) ((offsetY + scrollOffsetTop) / rh);
        }
        return -1;
    }

    boolean isClickedOnDeleteBtn(double x, double y, int seleted) {
        int rh = CommonCraftAssets.ROW.h - 1;
        if (x - getGuiLeft() > 26 && y - getGuiTop() > 26 && x - getGuiLeft() < 100 && y - getGuiTop() < 119) {
            double lDist = x - getGuiLeft() - 26;
            double tDist = (y - getGuiTop() - 26 + scrollOffsetTop - seleted * rh);
            if (lDist > CommonCraftAssets.ROW.w - 5 && tDist < 5)
                return true;
        }
        return false;
    }

    private void renderScrollList(GuiGraphics graphics, int x, int y) {
        makeScreenScissor(graphics);
        int relX = (this.width - this.imageWidth) / 2 + 27;
        int relY = (this.height - this.imageHeight) / 2 + 27;
        int selected = getSelectedStep(x, y);
        graphics.pose().pushPose();
        graphics.pose().translate(relX, relY - scrollOffsetTop, 0);
        graphics.pose().pushPose();
        for (int i = 0; i < menu.craftGuideData.steps.size(); i++) {
            CommonCraftAssets.ROW.blit(graphics, 0, 0);
            graphics.pose().translate(0, CommonCraftAssets.ROW.h - 1, 0);
        }
        graphics.pose().popPose();
        graphics.pose().pushPose();
        for (int i = 0; i < menu.craftGuideData.steps.size(); i++) {
            if (i == selected) {
                CommonCraftAssets.ROW_HOVER.blit(graphics, 0, 0);
            }
            graphics.pose().translate(0, CommonCraftAssets.ROW.h - 1, 0);
        }
        graphics.pose().popPose();

        for (int i = 0; i < menu.craftGuideData.steps.size(); i++) {
            CraftGuideStepData step = menu.craftGuideData.steps.get(i);
            if (i == menu.selectedIndex) {
                CommonCraftAssets.ROW_HIGHLIGHT.blit(graphics, -1, -1);
                if (i == selected) {
                    CommonCraftAssets.ROW_HOVER.blit(graphics, 0, 0);
                } else {
                    CommonCraftAssets.ROW.blit(graphics, 0, 0);
                }
            }
            renderScrollListRow(graphics,
                    step,
                    menu.blockIndicatorForSteps.get(i).getItem(),
                    selected == i,
                    i == menu.selectedIndex,
                    x - relX,
                    (int) (y - relY + scrollOffsetTop - i * (CommonCraftAssets.ROW.h - 1))
            );
            graphics.pose().translate(0, CommonCraftAssets.ROW.h - 1, 0);
        }
        graphics.pose().popPose();
        graphics.flush();
        releaseScreenScissor(graphics);
    }

    private void renderScrollListRow(GuiGraphics graphics, CraftGuideStepData step, ItemStack blockIndicator, boolean hover, boolean selected, int x, int y) {
        graphics.pose().pushPose();
        graphics.pose().translate(47, 4, 0);
        graphics.pose().scale(0.7f, 0.7f, 1f);
        CommonCraftAssets.imageForAction(step.action).blit(graphics, 0, 0);
        graphics.pose().popPose();
        graphics.pose().pushPose();
        graphics.pose().translate(2, 4, 0);
        graphics.pose().scale(0.6f, 0.6f, 1f);
        boolean hasInput = false;
        for (ItemStack itemStack : step.getNonEmptyInput()) {
            graphics.renderItem(itemStack, 0, 0);
            String text = String.valueOf(itemStack.getCount());
            graphics.pose().translate(0, 0, 400);
            graphics.drawString(this.font,
                    text,
                    16 - this.font.width(text),
                    (int) (16 - this.font.lineHeight),
                    0xffffffff);
            graphics.pose().translate(0, 0, -400);
            graphics.pose().translate(16, 0, 0);
            hasInput = true;
        }
        if (hasInput) {
            graphics.pose().translate(4, 0, 0);
        }
        for (ItemStack itemStack : step.getNonEmptyOutput()) {
            graphics.renderItem(itemStack, 0, 0);
            String text = String.valueOf(itemStack.getCount());
            graphics.pose().translate(0, 0, 400);
            graphics.drawString(this.font,
                    text,
                    16 - this.font.width(text),
                    (int) (16 - this.font.lineHeight),
                    0xffffffff);
            graphics.pose().translate(0, 0, -400);
            graphics.pose().translate(16, 0, 0);
        }
        graphics.pose().popPose();
        graphics.pose().pushPose();
        graphics.pose().translate(58, 2, 0);
        graphics.pose().scale(0.7f, 0.7f, 1f);
        graphics.renderItem(blockIndicator, 0, 0);
        graphics.pose().popPose();

        if (hover && selected) {
            graphics.pose().pushPose();
            graphics.pose().translate(0, 0, 600);
            CommonCraftAssets.DELETE_GRAY.blit(graphics,
                    CommonCraftAssets.ROW.w - 5,
                    2
            );
            if (x >= CommonCraftAssets.ROW.w - 5 && y < 5) {
                CommonCraftAssets.DELETE.blit(graphics,
                        CommonCraftAssets.ROW.w - 5,
                        2
                );
            }
            graphics.pose().popPose();
        }
    }

    private void renderScrollBar(GuiGraphics graphics, int x, int y) {
        boolean active = mouseDraggingScrollingBar != null;
        ImageAsset base = active ? CommonCraftAssets.SCROLL_BASE_HOVER : CommonCraftAssets.SCROLL_BASE;
        graphics.blitNineSliced(
                CommonCraftAssets.BACKGROUND,
                getGuiLeft() + 100,
                getGuiTop() + 23 + (int) scrollOffsetTop,
                base.w,
                (int) getScrollBlockHeight(),
                1,
                base.w,
                base.h,
                base.u,
                base.v
        );
        ImageAsset deco = active ? CommonCraftAssets.SCROLL_DECO_HOVER : CommonCraftAssets.SCROLL_DECO;
        deco.blit(
                graphics,
                getGuiLeft() + 100,
                getGuiTop() + 23 + (int) scrollOffsetTop + (int) (getScrollBlockHeight() / 2 - (float) deco.h / 2)
        );
    }


    private void clickInScrollList(double x, double y) {
        int id = getSelectedStep(x, y);
        if (id == -1 || id >= menu.craftGuideData.steps.size()) return;
        if (isClickedOnDeleteBtn(x, y, id) && menu.selectedIndex == id) {
            sendAndTriggerLocalPacket(new CraftGuideGuiPacket(CraftGuideGuiPacket.Type.REMOVE, id));
        } else {
            sendAndTriggerLocalPacket(new CraftGuideGuiPacket(CraftGuideGuiPacket.Type.SELECT, id));
        }
    }

    private boolean isInScrollBlockArea(double x, double y) {
        double rx = x - getGuiLeft();
        double ry = y - getGuiTop() - 23;
        if (rx > 104 || rx < 100) return false;
        if (ry < scrollOffsetTop) return false;
        if (ry > scrollOffsetTop + getScrollBlockHeight()) return false;
        return true;
    }

    @Override
    public boolean mouseDragged(double x, double y, int p_97754_, double p_97755_, double p_97756_) {
        if (mouseDraggingScrollingBar != null && mouseStartDraggingOffset != null) {
            scrollOffsetTop = (float) (mouseStartDraggingOffset + (y - mouseDraggingScrollingBar));
            scroll(0);
        }
        return super.mouseDragged(x, y, p_97754_, p_97755_, p_97756_);
    }

    @Override
    public boolean mouseReleased(double p_97812_, double p_97813_, int p_97814_) {
        if (mouseDraggingScrollingBar != null || mouseStartDraggingOffset != null) {
            mouseDraggingScrollingBar = null;
            mouseStartDraggingOffset = null;
        }
        return super.mouseReleased(p_97812_, p_97813_, p_97814_);
    }


    public void drawCenteredString(GuiGraphics graphics, Font pFont, Component pText, int pX, int pY, int maxWidth, int pColor, boolean shadow) {
        FormattedCharSequence formattedcharsequence = pText.getVisualOrderText();
        int textWidth = pFont.width(formattedcharsequence);
        int drawWidth = Math.max(textWidth, maxWidth);
        int alignWidth = Math.min(maxWidth, textWidth);
        float scale = (float) maxWidth / drawWidth;
        graphics.pose().pushPose();
        graphics.pose().scale(scale, scale, scale);
        graphics.drawString(pFont, formattedcharsequence, pX / scale, (pY - 3 + (14 - 8 * scale) / 2) / scale, pColor, shadow);
        graphics.pose().popPose();
    }
}