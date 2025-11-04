package studio.fantasyit.maid_storage_manager.menu.communicate;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.communicate.data.SlotType;
import studio.fantasyit.maid_storage_manager.communicate.data.TaskDefaultCommunicate;
import studio.fantasyit.maid_storage_manager.menu.base.AbstractFilterScreen;
import studio.fantasyit.maid_storage_manager.menu.base.ImageAsset;
import studio.fantasyit.maid_storage_manager.menu.container.FilterSlot;
import studio.fantasyit.maid_storage_manager.menu.container.SelectButtonWidget;
import studio.fantasyit.maid_storage_manager.network.CommunicateMarkGuiPacket;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CommunicateMarkScreen extends AbstractFilterScreen<CommunicateMarkMenu> {
    private static final ResourceLocation background = new ResourceLocation(MaidStorageManager.MODID, "textures/gui/communicate_terminal.png");

    List<AbstractWidget> listItems = new ArrayList<>();
    EditBox maxValue;
    EditBox minValue;
    EditBox threshold;
    SelectButtonWidget<ItemStackUtil.MATCH_TYPE> btnMatchNbt;
    SelectButtonWidget<Boolean> btnWhitelist;
    SelectButtonWidget<ResourceLocation> btnManual;
    SelectButtonWidget<SlotType> btnSlot;
    CommunicateSlotSelector selector;
    CommunicateMethodSelector methodSelector;
    CommunicateRollingTextWidget slotText;
    CommunicateRollingTextWidget btnManualText;

    public CommunicateMarkScreen(CommunicateMarkMenu p_97741_, Inventory p_97742_, Component p_97743_) {
        super(p_97741_, p_97742_, p_97743_);
        this.imageWidth = 176;
        this.imageHeight = 246;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        addMainList();
        addInputs();
        addButtons();
        addSelector();
    }

    private void addMainList() {
        int x = 29;
        int y = 45;
        for (int i = 0; i < 6; i++) {
            CommunicateListItemWidget communicateListItemWidget = new CommunicateListItemWidget(
                    getGuiLeft() + x,
                    getGuiTop() + y,
                    33,
                    15,
                    menu.data,
                    i,
                    idx -> {
                        menu.selected = idx;
                        menu.updateCurrentSelected();
                        menu.sendPacketToOpposite(new CommunicateMarkGuiPacket(CommunicateMarkGuiPacket.Type.SELECT, idx));
                        maxValue.setValue(String.valueOf(menu.item.max));
                        minValue.setValue(String.valueOf(menu.item.min));
                        threshold.setValue(String.valueOf(menu.item.thresholdCount));
                        btnMatchNbt.setOption(null);
                        btnWhitelist.setOption(null);
                        btnSlot.setOption(null);
                    }
            );
            listItems.add(communicateListItemWidget);
            addRenderableWidget(communicateListItemWidget);
            y += 16;
        }
    }

    private void addInputs() {
        maxValue = addRenderableWidget(new EditBox(font,
                getGuiLeft() + 131,
                getGuiTop() + 70,
                21,
                9,
                Component.literal("")));
        minValue = addRenderableWidget(new EditBox(font,
                getGuiLeft() + 131,
                getGuiTop() + 83,
                21,
                9,
                Component.literal("")));
        threshold = addRenderableWidget(new EditBox(font,
                getGuiLeft() + 129,
                getGuiTop() + 51,
                23,
                9,
                Component.literal("")));
        initBox(maxValue, menu.item.max, Component.translatable("gui.maid_storage_manager.communicate_terminal.max"), v -> {
            menu.item.max = v;
            menu.sendPacketToOpposite(new CommunicateMarkGuiPacket(CommunicateMarkGuiPacket.Type.MAX, 0, v));
        });
        initBox(minValue, menu.item.min, Component.translatable("gui.maid_storage_manager.communicate_terminal.min"), v -> {
            menu.item.min = v;
            menu.sendPacketToOpposite(new CommunicateMarkGuiPacket(CommunicateMarkGuiPacket.Type.MIN, 0, v));
        });
        initBox(threshold, menu.item.thresholdCount, Component.translatable("gui.maid_storage_manager.communicate_terminal.threshold"), v -> {
            menu.item.thresholdCount = v;
            menu.sendPacketToOpposite(new CommunicateMarkGuiPacket(CommunicateMarkGuiPacket.Type.THRESHOLD, 0, v));
        });
    }

    private void initBox(EditBox editBox, int value, Component tooltip, Consumer<Integer> responder) {
        editBox.setValue("");
        editBox.setBordered(false);
        editBox.active = true;
        editBox.setValue(String.valueOf(value));
        editBox.setFilter(s -> {
                    try {
                        Integer i = Integer.parseInt(s);
                        responder.accept(i);
                        return true;
                    } catch (Exception e) {
                        return false;
                    }
                }
        );
        editBox.setTooltip(Tooltip.create(tooltip));
        editBox.setResponder(t -> {
            try {
                Integer i = Integer.parseInt(t);
                responder.accept(i);
            } catch (Exception e) {
                editBox.setValue("0");
            }
        });
    }

    private void addValueToBox(EditBox editBox, double originalValue) {
        int value = originalValue > 0 ? 1 : -1;
        if (hasControlDown()) {
            value = value * 10;
        }
        if (hasShiftDown()) {
            value = value * 10;
        }
        int result = value;
        try {
            result += Integer.parseInt(editBox.getValue());
        } catch (NumberFormatException ignored) {
        }
        if (result > 99999)
            result = 99999;
        if (result < 0)
            result = -1;
        editBox.setValue(String.valueOf(result));
    }

    ImageAsset NBT_MATCH = new ImageAsset(background, 176, 0, 16, 16);
    ImageAsset NBT_MATCH_HOVER = new ImageAsset(background, 176, 16, 16, 16);
    ImageAsset NBT_NOT_MATCH = new ImageAsset(background, 192, 0, 16, 16);
    ImageAsset NBT_NOT_MATCH_HOVER = new ImageAsset(background, 192, 16, 16, 16);
    ImageAsset NBT_AUTO = new ImageAsset(background, 208, 0, 16, 16);
    ImageAsset NBT_AUTO_HOVER = new ImageAsset(background, 208, 16, 16, 16);
    ImageAsset WHITE = new ImageAsset(background, 224, 0, 16, 16);
    ImageAsset WHITE_HOVER = new ImageAsset(background, 224, 16, 16, 16);
    ImageAsset BLACK = new ImageAsset(background, 240, 0, 16, 16);
    ImageAsset BLACK_HOVER = new ImageAsset(background, 240, 16, 16, 16);
    ImageAsset MANUAL = ImageAsset.from4Point(background, 176, 32, 218, 47);
    ImageAsset MANUAL_HOVER = ImageAsset.from4Point(background, 176, 48, 218, 63);
    ImageAsset SLOT = ImageAsset.from4Point(background, 176, 200, 218, 219);
    ImageAsset SLOT_HOVER = ImageAsset.from4Point(background, 176, 220, 218, 239);

    private void addButtons() {

        btnMatchNbt = addRenderableWidget(new SelectButtonWidget<>(73, 45, data -> {
            if (data == null)
                data = menu.item.match;
            else {
                data = ItemStackUtil.MATCH_TYPE.values()[(data.ordinal() + 1) % ItemStackUtil.MATCH_TYPE.values().length];
                menu.item.match = data;
                menu.sendPacketToOpposite(new CommunicateMarkGuiPacket(CommunicateMarkGuiPacket.Type.MATCH, 0, data.ordinal()));
            }
            return new SelectButtonWidget.Option<>(
                    data,
                    switch (data) {
                        case AUTO -> NBT_AUTO;
                        case NOT_MATCHING -> NBT_NOT_MATCH;
                        case MATCHING -> NBT_MATCH;
                    },
                    switch (data) {
                        case AUTO -> NBT_AUTO_HOVER;
                        case NOT_MATCHING -> NBT_NOT_MATCH_HOVER;
                        case MATCHING -> NBT_MATCH_HOVER;
                    },
                    switch (data) {
                        case AUTO ->
                                Component.translatable("gui.maid_storage_manager.communicate_terminal.match_tag_auto");
                        case NOT_MATCHING ->
                                Component.translatable("gui.maid_storage_manager.communicate_terminal.match_tag_off");
                        case MATCHING ->
                                Component.translatable("gui.maid_storage_manager.communicate_terminal.match_tag_on");
                    }
            );
        }, this));
        btnWhitelist = addRenderableWidget(new SelectButtonWidget<>(93, 45, data -> {
            if (data == null)
                data = menu.item.whiteMode;
            else {
                data = !data;
                menu.item.whiteMode = data;
                menu.sendPacketToOpposite(new CommunicateMarkGuiPacket(CommunicateMarkGuiPacket.Type.WHITE_MODE, 0, data ? 1 : 0));
            }
            return new SelectButtonWidget.Option<>(
                    data,
                    data ? WHITE : BLACK,
                    data ? WHITE_HOVER : BLACK_HOVER,
                    data ? Component.translatable("gui.maid_storage_manager.communicate_terminal.whitelist") :
                            Component.translatable("gui.maid_storage_manager.communicate_terminal.blacklist")
            );
        }, this));
        slotText = addRenderableOnly(new CommunicateRollingTextWidget(
                getGuiLeft() + 77,
                getGuiTop() + 72,
                23,
                16,
                Component.empty()
        ));
        btnSlot = addRenderableWidget(new SelectButtonWidget<>(73, 71, data -> {
            if (data == null) {
                data = menu.item.slot;
                slotText.setText(Component.translatable("gui.maid_storage_manager.communicate_terminal.slot", menu.item.slot.getName()));
            } else {
                selector.show();
            }
            return new SelectButtonWidget.Option<>(
                    data,
                    SLOT,
                    SLOT_HOVER,
                    Component.empty()
            );
        }, this));

        btnManualText = addRenderableOnly(new CommunicateRollingTextWidget(
                getGuiLeft() + 22,
                getGuiTop() + 19,
                MANUAL.w,
                MANUAL.h,
                Component.empty()
        ));
        btnManual = addRenderableWidget(new SelectButtonWidget<>(22, 19, data -> {
            if (data == null) {
                data = menu.isManual ? TaskDefaultCommunicate.DUMMY_USE_CURRENT_DATA : TaskDefaultCommunicate.DUMMY_AUTO_DETECT_TASK;
                btnManualText.setText(TaskDefaultCommunicate.getTranslate(data));
            } else {
                methodSelector.setSelected(data);
                methodSelector.expandFrom(btnManual);
            }
            return new SelectButtonWidget.Option<>(
                    data,
                    MANUAL,
                    MANUAL_HOVER,
                    Component.empty()
            );
        }, this));
    }

    private void addSelector() {
        methodSelector = new CommunicateMethodSelector(getGuiLeft() + 38, getGuiTop() + 26, this);
        methodSelector.setCallback(method -> {
            if (method.equals(TaskDefaultCommunicate.DUMMY_AUTO_DETECT_TASK)) {
                menu.isManual = false;
            } else if (method.equals(TaskDefaultCommunicate.DUMMY_USE_CURRENT_DATA)) {
                menu.isManual = true;
            } else {
                menu.isManual = true;
                menu.sendPacketToOpposite(new CommunicateMarkGuiPacket(CommunicateMarkGuiPacket.Type.USE_ID, 0, CommunicateMarkGuiPacket.singleValue(method.toString())));
            }
            btnManual.setOption(new SelectButtonWidget.Option<>(method, MANUAL, MANUAL_HOVER, Component.empty()));
            menu.sendPacketToOpposite(new CommunicateMarkGuiPacket(CommunicateMarkGuiPacket.Type.MANUAL, 0, menu.isManual ? 1 : 0));
            activateOrDe(menu.isManual);
        });
        addRenderableWidget(methodSelector);
        selector = new CommunicateSlotSelector(getGuiLeft() + 38, getGuiTop() + 26, s -> {
            menu.item.slot = s;
            menu.sendPacketToOpposite(new CommunicateMarkGuiPacket(CommunicateMarkGuiPacket.Type.SLOT, 0, s.ordinal()));
            slotText.setText(Component.translatable("gui.maid_storage_manager.communicate_terminal.slot", s.getName()));
        });
        addRenderableWidget(selector);
    }

    public void activateOrDe(boolean active) {
        btnMatchNbt.active = active;
        btnWhitelist.active = active;
        btnSlot.active = active;
        for (Slot slot : this.menu.slots) {
            if (slot instanceof FilterSlot fs)
                fs.setActive(active);
        }
        for (AbstractWidget item : listItems) {
            item.active = active;
        }
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int x, int y, float pt) {
        super.render(graphics, x, y, pt);
        this.renderTooltip(graphics, x, y);
        if (!menu.isManual && !methodSelector.visible) {
            graphics.flush();
            graphics.pose().pushPose();
            graphics.pose().translate(0, 0, 1000);
            graphics.fill(getGuiLeft() + 22,
                    getGuiTop() + 39,
                    getGuiLeft() + 153,
                    getGuiTop() + 141,
                    0x90ffffff);
            graphics.drawCenteredString(font,
                    Component.translatable("gui.maid_storage_manager.communicate_terminal.non_manual_mode_tip"),
                    getGuiLeft() + 88,
                    getGuiTop() + 72,
                    0xFFFFFFFF
            );
            graphics.drawCenteredString(font,
                    Component.translatable("gui.maid_storage_manager.communicate_terminal.non_manual_mode_tip2"),
                    getGuiLeft() + 88,
                    getGuiTop() + 100,
                    0xFFFFFFFF
            );
            graphics.pose().popPose();
            return;
        }
        if (btnSlot != null)
            menu.item.slot.drawGold(graphics, btnSlot.getX() + btnSlot.getWidth() - 17, btnSlot.getY() + 3);
        graphics.flush();
        graphics.setColor(1, 1, 1, 0.5f);
        if (menu.workCard.isEmpty())
            graphics.renderItem(ItemRegistry.WORK_CARD.get().getDefaultInstance(), getGuiLeft() + 139, getGuiTop() + 19);
        graphics.setColor(1, 1, 1, 1);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics graphics, float p_97788_, int p_97789_, int p_97790_) {
        renderBackground(graphics);
        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - this.imageHeight) / 2;
        graphics.blit(background,
                relX,
                relY,
                0,
                0,
                0,
                this.imageWidth,
                this.imageHeight,
                256,
                256);
    }

    @Override
    public void accept(FilterSlot slot, ItemStack item) {
        menu.sendPacketToOpposite(new CommunicateMarkGuiPacket(
                CommunicateMarkGuiPacket.Type.SET_ITEM,
                slot.getContainerSlot(),
                0,
                ItemStackUtil.saveStack(item)
        ));
    }

    @Override
    public List<FilterSlot> getSlots() {
        return menu.slots.stream()
                .filter(slot -> slot instanceof FilterSlot fs && !fs.readonly)
                .map(slot -> (FilterSlot) slot)
                .toList();
    }

    @Override
    public boolean mouseClicked(double p_97748_, double p_97749_, int p_97750_) {
        if (selector.visible) {
            if (selector.isMouseOver(p_97748_, p_97749_))
                return selector.mouseClicked(p_97748_, p_97749_, p_97750_);
            else {
                selector.visible = false;
                return true;
            }
        }
        if (methodSelector.visible) {
            if (methodSelector.isMouseOver(p_97748_, p_97749_))
                return methodSelector.mouseClicked(p_97748_, p_97749_, p_97750_);
            else {
                methodSelector.visible = false;
                return true;
            }
        }
        return super.mouseClicked(p_97748_, p_97749_, p_97750_);
    }

    @Override
    public boolean mouseScrolled(double p_94686_, double p_94687_, double p_94688_) {
        if (maxValue.isMouseOver(p_94686_, p_94687_))
            addValueToBox(maxValue, p_94688_);
        if (minValue.isMouseOver(p_94686_, p_94687_))
            addValueToBox(minValue, p_94688_);
        if (threshold.isMouseOver(p_94686_, p_94687_))
            addValueToBox(threshold, p_94688_);
        if (methodSelector.visible) {
            return methodSelector.mouseScrolled(p_94686_, p_94687_, p_94688_);
        }
        return super.mouseScrolled(p_94686_, p_94687_, p_94688_);
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int x, int y) {
        if (this.menu.getCarried().isEmpty()) {
            int inGuiX = x - this.getGuiLeft();
            int inGuiY = y - this.getGuiTop();
            for (Slot slot : this.getMenu().slots) {
                if (slot.x <= inGuiX && slot.x + 16 >= inGuiX && slot.y <= inGuiY && slot.y + 16 >= inGuiY) {
                    if (slot instanceof FilterSlot filterSlot) {
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
                    if (renderable instanceof SelectButtonWidget<?> buttonWidget && !buttonWidget.getTooltipComponent().getString().isEmpty()) {
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
    }
}
