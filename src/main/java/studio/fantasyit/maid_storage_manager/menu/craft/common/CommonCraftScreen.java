package studio.fantasyit.maid_storage_manager.menu.craft.common;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.PacketDistributor;
import org.anti_ad.mc.ipn.api.IPNIgnore;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.craft.action.CraftAction;
import studio.fantasyit.maid_storage_manager.craft.context.common.CommonIdleAction;
import studio.fantasyit.maid_storage_manager.menu.base.AbstractFilterScreen;
import studio.fantasyit.maid_storage_manager.menu.base.ImageAsset;
import studio.fantasyit.maid_storage_manager.menu.container.FilterSlot;
import studio.fantasyit.maid_storage_manager.menu.container.NoPlaceFilterSlot;
import studio.fantasyit.maid_storage_manager.menu.container.SelectButtonWidget;
import studio.fantasyit.maid_storage_manager.menu.craft.base.ICraftGuiPacketReceiver;
import studio.fantasyit.maid_storage_manager.network.CraftGuideGuiPacket;
import studio.fantasyit.maid_storage_manager.network.Network;
import studio.fantasyit.maid_storage_manager.storage.Target;
import yalter.mousetweaks.api.MouseTweaksDisableWheelTweak;

import java.util.*;
import java.util.function.Consumer;

@MouseTweaksDisableWheelTweak
@IPNIgnore
public class CommonCraftScreen extends AbstractFilterScreen<CommonCraftMenu> implements ICraftGuiPacketReceiver {
    protected enum BUTTON_TYPE_COMMON {
        ACTION,
        SORT_UP,
        SORT_REMOVE,
        SORT_DOWN,
        OPTIONAL,
        TIME
    }

    protected enum BUTTON_TYPE_SPECIAL {BLOCK}

    private final ResourceLocation background = ResourceLocation.fromNamespaceAndPath(MaidStorageManager.MODID, "textures/gui/craft/type/common.png");

    public final List<HashMap<BUTTON_TYPE_COMMON, SelectButtonWidget<?>>> buttonsByRow = new ArrayList<>();
    public final List<EditBox> editBoxes = new ArrayList<>();
    public final List<HashMap<BUTTON_TYPE_SPECIAL, AbstractButton>> otherButton = new ArrayList<>();
    public final List<List<Integer>> buttonYOffset = new ArrayList<>();
    SelectButtonWidget<?> pageUpBtn;
    SelectButtonWidget<?> pageDownBtn;
    CommonActionSelectionWidget actionSelector;

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
        this.addButtons();
    }

    private static final int[] SLOT_Y = new int[]{28, 71, 114};

    private void addButtons() {
        buttonsByRow.clear();
        buttonYOffset.clear();
        otherButton.clear();
        editBoxes.clear();

        for (int i = 0; i < this.menu.steps.size(); i++) {
            HashMap<BUTTON_TYPE_COMMON, SelectButtonWidget<?>> objects = new HashMap<>();
            ArrayList<Integer> yOffsets = new ArrayList<>();
            addActionButton(objects, yOffsets, SLOT_Y[i % 3] - 5, i);
            addSortButtons(objects, yOffsets, SLOT_Y[i % 3] - 5, i);
            addOptionButtons(objects, yOffsets, SLOT_Y[i % 3] - 5, i);
            addTimeButton(objects, yOffsets, SLOT_Y[i % 3] - 5, i);
            HashMap<BUTTON_TYPE_SPECIAL, AbstractButton> objectsSpecial = new HashMap<>();
            addBlockButton(objectsSpecial, yOffsets, SLOT_Y[i % 3] - 5, i);
            otherButton.add(objectsSpecial);
            buttonsByRow.add(objects);
            buttonYOffset.add(yOffsets);
        }
        addPageButton();
        updateButtons();
    }

    private void addBlockButton(HashMap<BUTTON_TYPE_SPECIAL, AbstractButton> objects, ArrayList<Integer> yOffsets, int sy, int i) {
        yOffsets.add(1);
        objects.put(BUTTON_TYPE_SPECIAL.BLOCK, addRenderableWidget(new AbstractButton(
                this.leftPos + menu.targetBlockSlots.get(i).x + 1,
                this.topPos + menu.targetBlockSlots.get(i).y - 3,
                20,
                20,
                Component.literal("")
        ) {
            @Override
            protected void updateWidgetNarration(NarrationElementOutput p_259858_) {
                if (menu.targetBlockSlots.size() > i && menu.steps.size() > i) {
                    p_259858_.add(NarratedElementType.HINT, menu.targetBlockSlots.get(i).getItem().getDisplayName());
                    p_259858_.add(NarratedElementType.HINT, getStorageSideTranslate(menu.steps.get(i).step.storage));
                }
            }

            @Override
            protected void renderWidget(GuiGraphics graphics, int x, int y, float p_282542_) {
                if (isMouseOver(x, y)) {
                    graphics.pose().pushPose();
                    graphics.pose().translate(0, 0, 200);
                    graphics.fill(getX(), getY(), getX() + width, getY() + font.lineHeight, 0x80000000);
                    graphics.drawString(font, getStorageSideTranslate(menu.steps.get(i).step.storage), getX(), getY(), 0xFFFFFFFF);
                    graphics.pose().popPose();
                }
            }

            @Override
            public void onPress() {
                Optional<Direction> currentSide = menu.steps.get(i).step.storage.getSide();
                int nextOri;
                if (currentSide.isEmpty())
                    nextOri = 0;
                else if (currentSide.get().ordinal() + 1 == Direction.values().length)
                    nextOri = -1;
                else
                    nextOri = currentSide.get().ordinal() + 1;
                sendAndTriggerLocalPacket(new CraftGuideGuiPacket(CraftGuideGuiPacket.Type.SIDE, i, nextOri));
            }
        }));
    }

    private void addTimeButton(HashMap<BUTTON_TYPE_COMMON, SelectButtonWidget<?>> objects, ArrayList<Integer> yOffsets, int sy, int i1) {
        int dy = 24;
        yOffsets.add(dy);
        objects.put(BUTTON_TYPE_COMMON.TIME, addRenderableWidget(new SelectButtonWidget<Integer>(121, sy + dy - 1, (value) -> {
            int v;
            CompoundTag extraData = menu.steps.get(i1).step.getExtraData();
            if (menu.steps.get(i1).actionType.type().equals(CommonIdleAction.TYPE)) {
                if (extraData.contains("u"))
                    v = extraData.getInt("u");
                else
                    v = 0;
            } else
                v = 0;
            if (value != null) {
                v = (value == 0 ? 1 : 0);
                editBoxes.get(i1).setFocused(true);
                int finalV = v;
                sendExtra(i1, t -> t.putInt("u", finalV));
            }


            return new SelectButtonWidget.Option<>(
                    v,
                    CommonCraftAssets.SMALL_BUTTON,
                    CommonCraftAssets.SMALL_BUTTON_HOVER,
                    v == 0 ? Component.translatable("gui.maid_storage_manager.craft_guide.common.idle_tick") : Component.translatable("gui.maid_storage_manager.craft_guide.common.idle_second")
            );
        }, this)));


        EditBox editBox = addRenderableWidget(new EditBox(font,
                getGuiLeft() + 90,
                getGuiTop() + sy + dy,
                30,
                8,
                Component.literal("")));
        editBox.setValue("0");
        editBox.setBordered(false);
        editBox.setMaxLength(3);
        editBox.setFilter(s -> StringUtils.isNumeric(s) && Integer.parseInt(s) <= 999);
        editBox.setResponder(t -> sendExtra(i1, c -> c.putInt("time", Integer.parseInt(t))));
        editBoxes.add(editBox);
    }

    private void addActionButton(Map<BUTTON_TYPE_COMMON, SelectButtonWidget<?>> buttons, List<Integer> yOffsets, int sy, int i) {
        actionSelector = new CommonActionSelectionWidget(0, 0, this);
        addWidget(actionSelector);
        buttons.put(BUTTON_TYPE_COMMON.ACTION, addRenderableWidget(new SelectButtonWidget<CraftAction>(94, SLOT_Y[i % 3], (value) -> {
            if (value == null) value = menu.craftGuideData.steps.get(i).actionType;
            else {
                startSelectActionFor(i);
            }
            return new SelectButtonWidget.Option<>(
                    value,
                    CommonCraftAssets.BUTTON_ACTION,
                    CommonCraftAssets.BUTTON_ACTION_HOVER,
                    CommonCraftAssets.translationForAction(value.type())
            );
        }, this)));
        yOffsets.add(0);
    }

    private void addOptionButtons(Map<BUTTON_TYPE_COMMON, SelectButtonWidget<?>> buttons, List<Integer> yOffsets, int sy, int i) {
        yOffsets.add(5);
        buttons.put(BUTTON_TYPE_COMMON.OPTIONAL, addRenderableWidget(new SelectButtonWidget<Boolean>(137, sy + 5, (value) -> {
            if (value == null)
                value = !menu.steps.get(i).optional;
            else {
                value = !value;
                sendAndTriggerLocalPacket(new CraftGuideGuiPacket(CraftGuideGuiPacket.Type.OPTIONAL, i, value ? 1 : 0));
            }
            return new SelectButtonWidget.Option<>(
                    value,
                    value ? CommonCraftAssets.BUTTON_OPTIONAL_POSI : CommonCraftAssets.BUTTON_OPTIONAL_NEGI,
                    value ? CommonCraftAssets.BUTTON_OPTIONAL_POSI_HOVER : CommonCraftAssets.BUTTON_OPTIONAL_NEGI_HOVER,
                    value ? Component.translatable("gui.maid_storage_manager.craft_guide.common.required") : Component.translatable("gui.maid_storage_manager.craft_guide.common.optional")
            );

        }, this)));
    }

    private void addSortButtons(Map<BUTTON_TYPE_COMMON, SelectButtonWidget<?>> buttons, List<Integer> yOffsets, int sy, int i) {
        buttons.put(BUTTON_TYPE_COMMON.SORT_UP, addRenderableWidget(new SelectButtonWidget<Integer>(23, sy, (value) -> {
            if (value != null)
                sendAndTriggerLocalPacket(new CraftGuideGuiPacket(CraftGuideGuiPacket.Type.UP, i));
            return new SelectButtonWidget.Option<>(
                    1,
                    CommonCraftAssets.BUTTON_UP,
                    CommonCraftAssets.BUTTON_UP_HOVER,
                    Component.translatable("gui.maid_storage_manager.craft_guide.common.up"));
        }, this)));
        yOffsets.add(-5);
        buttons.put(BUTTON_TYPE_COMMON.SORT_REMOVE, addRenderableWidget(new SelectButtonWidget<Integer>(23, sy + 10, (value) -> {
            if (value != null)
                sendAndTriggerLocalPacket(new CraftGuideGuiPacket(CraftGuideGuiPacket.Type.REMOVE, i));
            return new SelectButtonWidget.Option<>(
                    1,
                    CommonCraftAssets.BUTTON_REMOVE,
                    CommonCraftAssets.BUTTON_REMOVE_HOVER,
                    Component.translatable("gui.maid_storage_manager.craft_guide.common.remove"));
        }, this)));
        yOffsets.add(5);

        buttons.put(BUTTON_TYPE_COMMON.SORT_DOWN, addRenderableWidget(new SelectButtonWidget<Integer>(23, sy + 23, (value) -> {
            if (value != null)
                sendAndTriggerLocalPacket(new CraftGuideGuiPacket(CraftGuideGuiPacket.Type.DOWN, i));

            return new SelectButtonWidget.Option<>(
                    1,
                    CommonCraftAssets.BUTTON_DOWN,
                    CommonCraftAssets.BUTTON_DOWN_HOVER,
                    Component.translatable("gui.maid_storage_manager.craft_guide.common.down"));
        }, this)));
        yOffsets.add(15);
    }

    private void addPageButton() {
        pageUpBtn = addRenderableWidget(new SelectButtonWidget<Integer>(122, 151, (value) -> {
            if (value != null)
                sendAndTriggerLocalPacket(new CraftGuideGuiPacket(CraftGuideGuiPacket.Type.PAGE_UP, value));
            return new SelectButtonWidget.Option<>(
                    1,
                    CommonCraftAssets.BUTTON_PREV_PAGE,
                    CommonCraftAssets.BUTTON_PREV_PAGE_HOVER,
                    Component.translatable("gui.maid_storage_manager.craft_guide.common.page_up"));
        }, this));

        pageDownBtn = addRenderableWidget(new SelectButtonWidget<Integer>(140, 151, (value) -> {
            if (value != null)
                sendAndTriggerLocalPacket(new CraftGuideGuiPacket(CraftGuideGuiPacket.Type.PAGE_DOWN, value));
            return new SelectButtonWidget.Option<>(
                    1,
                    CommonCraftAssets.BUTTON_NEXT_PAGE,
                    CommonCraftAssets.BUTTON_NEXT_PAGE_HOVER,
                    Component.translatable("gui.maid_storage_manager.craft_guide.common.page_down"));
        }, this));
    }

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

        guiGraphics.blit(background,
                relX,
                relY,
                0,
                0,
                0,
                this.imageWidth,
                this.imageHeight,
                256,
                256);

        for (Slot slot : this.getMenu().slots) {
            if (slot instanceof FilterSlot filterSlot && filterSlot.container instanceof CommonStepDataContainer container && filterSlot.isActive()) {
                ImageAsset back = switch (filterSlot.getContainerSlot()) {
                    case 0 -> CommonCraftAssets.SLOT_L;
                    case 1 -> container.getContainerSize() == 2 ? CommonCraftAssets.SLOT_M : CommonCraftAssets.SLOT_R;
                    case 2 -> CommonCraftAssets.SLOT_R;
                    default -> null;
                };
                if (back != null)
                    back.blit(guiGraphics, relX + slot.x - 1, relY + slot.y - 1);
            }
        }

        renderSeparator(guiGraphics);
    }

    @Override
    protected void renderTooltip(@NotNull GuiGraphics graphics, int x, int y) {
        if (actionSelector.visible) return;
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 400);
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
        RenderSystem.disableDepthTest();
        renderUnderLine(graphics);
        RenderSystem.disableDepthTest();
        renderBlockIndicator(graphics);
        RenderSystem.disableDepthTest();
        super.render(graphics, p_283661_, p_281248_, p_281886_);
        RenderSystem.disableDepthTest();
        renderNumberLabel(graphics);
        RenderSystem.disableDepthTest();
        renderButtonIcon(graphics);
        RenderSystem.disableDepthTest();
        renderArrow(graphics);
        RenderSystem.disableDepthTest();
        renderTooltip(graphics, p_283661_, p_281248_);
        RenderSystem.disableDepthTest();
        actionSelector.render(graphics, p_283661_, p_281248_, p_281886_);
        RenderSystem.enableDepthTest();
    }

    private void renderUnderLine(@NotNull GuiGraphics graphics) {
        for (EditBox editBox : editBoxes) {
            if (editBox.isVisible())
                graphics.blit(background, editBox.getX(), editBox.getY() + editBox.getHeight(), 197, 100, editBox.getWidth() + 1, 1);
        }
        graphics.flush();
    }

    private void renderArrow(@NotNull GuiGraphics graphics) {
        for (int i = 0; i < menu.steps.size(); i++) {
            if (menu.isIdCurrentPage(i)) {
                if (menu.steps.get(i).inputCount > 0 && menu.steps.get(i).outputCount > 0) {
                    CommonCraftAssets.ARROW.blit(graphics, getGuiLeft() + 59, getGuiTop() + SLOT_Y[i % 3] + 5);
                }
            }
        }
        graphics.flush();
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

    @SuppressWarnings("unchecked")
    private void renderButtonIcon(@NotNull GuiGraphics graphics) {
        for (int i = 0; i < buttonsByRow.size(); i++) {
            if (menu.isIdCurrentPage(i)) {
                SelectButtonWidget<CraftAction> cab = (SelectButtonWidget<CraftAction>) buttonsByRow.get(i).get(BUTTON_TYPE_COMMON.ACTION);
                CommonCraftAssets
                        .imageForAction(cab.getData().type())
                        .blit(graphics,
                                cab.getX() + 2,
                                cab.getY() + 2
                        );

                SelectButtonWidget<Integer> timeBtn = (SelectButtonWidget<Integer>) buttonsByRow.get(i).get(BUTTON_TYPE_COMMON.TIME);
                if (timeBtn.isVisible()) {
                    graphics.pose().pushPose();
                    graphics.pose().translate(timeBtn.getX() + 3, timeBtn.getY() + 3, 0);
                    graphics.pose().scale(0.7f, 0.7f, 1);
                    graphics.drawString(this.font,
                            timeBtn.getData() == 0 ? "T" : "S",
                            0,
                            0,
                            0xFFFFFF);
                    RenderSystem.disableDepthTest();
                    graphics.pose().popPose();
                }
            }
        }
        graphics.flush();
    }

    private void renderBlockIndicator(@NotNull GuiGraphics graphics) {
        graphics.pose().pushPose();
        float scale = 1.3f;
        graphics.pose().scale(scale, scale, 1);
        for (int i = 0; i < menu.targetBlockSlots.size(); i++) {
            if (menu.isIdCurrentPage(i)) {
                RenderSystem.disableDepthTest();
                graphics.renderItem(
                        menu.targetBlockSlots.get(i).getItem(),
                        (int) ((this.leftPos + menu.targetBlockSlots.get(i).x + 1) / scale),
                        (int) ((this.topPos + menu.targetBlockSlots.get(i).y - (scale - 1) * 18 / 2) / scale)
                );
            }
        }
        graphics.flush();
        graphics.pose().popPose();
    }

    private void renderSeparator(GuiGraphics graphics) {
        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - this.imageHeight) / 2;
        int lineCount = menu.steps.size() - menu.page * 3;
        for (int i = 0; i < lineCount && i < 2; i++) {
            CommonCraftAssets.SEPARATOR.blit(
                    graphics,
                    relX + 24,
                    relY + SLOT_Y[i] + 28
            );
        }

        graphics.flush();
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
        if (x - getGuiLeft() > 14 && y - getGuiTop() > 18 && x - getGuiLeft() < 158 && y - getGuiTop() < 145) {
            if (p_94688_ < 0 && menu.page < (menu.steps.size() + 2) / 3 - 1) {
                sendAndTriggerLocalPacket(new CraftGuideGuiPacket(CraftGuideGuiPacket.Type.PAGE_DOWN, 0));
            }
            if (p_94688_ > 0 && menu.page > 0) {
                sendAndTriggerLocalPacket(new CraftGuideGuiPacket(CraftGuideGuiPacket.Type.PAGE_UP, 0));
            }
        }
        return super.mouseScrolled(x, y, p_94688_);
    }

    @Override
    public void accept(FilterSlot slot, ItemStack item) {
        if (slot instanceof NoPlaceFilterSlot) return;
        if (!slot.isActive()) return;
        slot.set(item);
        sendAndTriggerLocalPacket(new CraftGuideGuiPacket(CraftGuideGuiPacket.Type.SET_ITEM, slot.index, 0, item.save(new CompoundTag())));
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
        switch (type) {
            case REMOVE -> {
                buttonsByRow.get(buttonsByRow.size() - 1).values().forEach(button -> button.setVisible(false));
                otherButton.get(otherButton.size() - 1).values().forEach(button -> button.active = false);
                buttonsByRow.remove(buttonsByRow.size() - 1);
            }
        }
        updateButtons();
    }

    private void updateButtons() {
        for (HashMap<BUTTON_TYPE_COMMON, SelectButtonWidget<?>> buttons : buttonsByRow) {
            buttons.get(BUTTON_TYPE_COMMON.ACTION).setOption(null);
            buttons.get(BUTTON_TYPE_COMMON.OPTIONAL).setOption(null);
        }
        for (int i = 0; i < menu.steps.size(); i++) {
            HashMap<BUTTON_TYPE_COMMON, SelectButtonWidget<?>> buttons = buttonsByRow.get(i);
            int finalI = i;
            buttons.values().forEach(button -> button.setVisible(menu.isIdCurrentPage(finalI)));
            otherButton.get(i).values().forEach(button -> button.active = menu.isIdCurrentPage(finalI));
            if (menu.steps.get(i).actionType.type() == CommonIdleAction.TYPE) {
                buttons.get(BUTTON_TYPE_COMMON.TIME).setVisible(menu.isIdCurrentPage(i));
                buttons.get(BUTTON_TYPE_COMMON.TIME).setOption(null);
                String newTime = String.valueOf(menu.steps.get(i).step.extraData.getInt("time"));
                if (!newTime.equals(editBoxes.get(i).getValue()))
                    editBoxes.get(i).setValue(newTime);
                editBoxes.get(i).visible = menu.isIdCurrentPage(i);
                editBoxes.get(i).active = menu.isIdCurrentPage(i);
            } else {
                buttons.get(BUTTON_TYPE_COMMON.TIME).setVisible(false);
                editBoxes.get(i).visible = false;
                editBoxes.get(i).active = false;
            }
        }
        pageUpBtn.setVisible(menu.page > 0);
        pageDownBtn.setVisible(menu.page < (menu.steps.size() + 2) / 3 - 1);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        for (EditBox eb : editBoxes) {
            String value = eb.getValue();
            if (value.length() < 3) value += "_";
            int width1 = Math.max(Math.min(font.width(value), 30), 8);
            eb.setWidth(width1);
            eb.setX(getGuiLeft() + 90 + 30 - width1);
        }
    }

    private void sendExtra(int i, @Nullable Consumer<CompoundTag> transformer) {
        CompoundTag tag = new CompoundTag();
        if (menu.steps.get(i).actionType.type().equals(CommonIdleAction.TYPE)) {
            tag.putInt("time", Integer.parseInt(editBoxes.get(i).getValue()));
            tag.putInt("u", (int) buttonsByRow.get(i).get(BUTTON_TYPE_COMMON.TIME).getData());
        }
        if (transformer != null)
            transformer.accept(tag);
        sendAndTriggerLocalPacket(new CraftGuideGuiPacket(CraftGuideGuiPacket.Type.EXTRA, i, 0, tag));
    }

    private Component getStorageSideTranslate(Target target) {
        return Component.translatable("gui.maid_storage_manager.craft_guide.common.side",
                Component.translatable(
                        "gui.maid_storage_manager.craft_guide.common.side_" +
                                target.getSide().map(t -> t.name().toLowerCase()).orElse("none")
                )
        );
    }


    private void startSelectActionFor(int i) {
        SelectButtonWidget<?> btn = buttonsByRow.get(i).get(BUTTON_TYPE_COMMON.ACTION);
        actionSelector.setSelectedAction((CraftAction) btn.getData());
        actionSelector.setCallback(value -> {
            CompoundTag data = new CompoundTag();
            data.putString("ns", value.type().getNamespace());
            data.putString("id", value.type().getPath());
            sendAndTriggerLocalPacket(new CraftGuideGuiPacket(CraftGuideGuiPacket.Type.SET_MODE, i, data));
            btn.setOption(null);
        });
        actionSelector.expandFrom(btn);
    }

    @Override
    public boolean mouseClicked(double p_97748_, double p_97749_, int p_97750_) {
        if (actionSelector.visible) {
            if (!actionSelector.isMouseOver(p_97748_, p_97749_)) {
                actionSelector.hide();
            } else return actionSelector.mouseClicked(p_97748_, p_97749_, p_97750_);
        }
        return super.mouseClicked(p_97748_, p_97749_, p_97750_);
    }
}