package studio.fantasyit.maid_storage_manager.menu.craft.common;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.math.Divisor;
import it.unimi.dsi.fastutil.ints.IntIterator;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.apache.commons.lang3.StringUtils;
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
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.util.GuiTools;
import studio.fantasyit.maid_storage_manager.util.InventoryListUtil;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CommonCraftScreen extends AbstractFilterScreen<CommonCraftMenu> implements ICraftGuiPacketReceiver {
    CommonActionSelectionWidget actionSelector;
    SelectButtonWidget<CraftAction> actionSelectorButton;
    SelectButtonWidget<?> sortButtonUp, sortButtonDown;
    SelectButtonWidget<Integer> generatorButton;
    List<Pair<SelectButtonWidget<Integer>, EditBox>> options = new ArrayList<>();

    public CommonCraftScreen(CommonCraftMenu p_97741_, Inventory p_97742_, Component p_97743_) {
        super(p_97741_, p_97742_, p_97743_, 176, 245);
        this.inventoryLabelY = this.imageHeight - 94;
        this.inventoryLabelX = 8;
    }

    @Override
    protected void init() {
        super.init();
        options.clear();
        addOptionButtons();
        addSortButtons();
        addActionButtons();
        addGeneratorButtons();
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
                        if (menu.currentEditingItems.options.size() <= optionIdx) {
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

        addRenderableWidget(new SelectButtonWidget<Boolean>(
                114, 126,
                (value) -> {
                    if (value == null) {
                        value = menu.craftGuideData.isMergeable();
                    } else {
                        value = !value;
                        sendAndTriggerLocalPacket(new CraftGuideGuiPacket(CraftGuideGuiPacket.Type.GLOBAL, 0, value ? 1 : 0));
                    }
                    return new SelectButtonWidget.Option<>(
                            value,
                            value ? CommonCraftAssets.BTN_MERGEABLE : CommonCraftAssets.BTN_NOT_MERGEABLE,
                            value ? CommonCraftAssets.BTN_MERGEABLE_HOVER : CommonCraftAssets.BTN_NOT_MERGEABLE_HOVER,
                            value ?
                                    Component.translatable("gui.maid_storage_manager.craft_guide.global.mergeable") :
                                    Component.translatable("gui.maid_storage_manager.craft_guide.global.not_mergeable")
                    );
                },
                this
        ));
        addRenderableWidget(new SelectButtonWidget<Boolean>(
                132, 126,
                (value) -> {
                    if (value == null) {
                        value = menu.craftGuideData.isNoOccupy();
                    } else {
                        value = !value;
                        sendAndTriggerLocalPacket(new CraftGuideGuiPacket(CraftGuideGuiPacket.Type.GLOBAL, 1, value ? 1 : 0));
                    }
                    return new SelectButtonWidget.Option<>(
                            value,
                            value ? CommonCraftAssets.BTN_NO_OCCUPY : CommonCraftAssets.BTN_OCCUPY,
                            value ? CommonCraftAssets.BTN_NO_OCCUPY_HOVER : CommonCraftAssets.BTN_OCCUPY_HOVER,
                            value ?
                                    Component.translatable("gui.maid_storage_manager.craft_guide.global.no_occupy") :
                                    Component.translatable("gui.maid_storage_manager.craft_guide.global.occupy")
                    );
                },
                this
        ));
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
                    actionSelector.setSelectedAction(value);
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

    private void addGeneratorButtons() {
        generatorButton = addRenderableWidget(new SelectButtonWidget<>(
                126, 102, (value) -> {
            if (value == null)
                value = 0;
            else {
                value = value + 1;
                if (value == 2) {
                    if (menu.selectedGeneratorIndex >= getGeneratorOutputsWithFilter(searchFilterStr).size()) {
                        menu.selectedGeneratorIndex = -1;
                    }
                    if (menu.selectedGeneratorIndex != -1) {
                        int realId = getGeneratorOutputsWithFilter(searchFilterStr).get(menu.selectedGeneratorIndex).getA();
                        List<ItemStack> list = menu.generatedRecipes.ingredients.get(realId).stream()
                                .map(i -> i.items().map(h -> h.value().getDefaultInstance()).toList())
                                .map(i -> InventoryListUtil.getMatchingForPlayer(i))
                                .toList();

                        CompoundTag tag = new CompoundTag();
                        ListTag listTag = new ListTag();
                        for (ItemStack itemStack : list) {
                            listTag.add(ItemStackUtil.saveStack(menu.player.registryAccess(), itemStack));
                        }
                        tag.put("inputs", listTag);
                        ClientPacketDistributor.sendToServer(new CraftGuideGuiPacket(
                                CraftGuideGuiPacket.Type.GENERATOR,
                                0,
                                realId,
                                tag
                        ));
                        generatorButton.visible = false;
                        menu.selectedGeneratorIndex = -1;
                    }
                }
            }
            return new SelectButtonWidget.Option<>(
                    value,
                    CommonCraftAssets.GENERATOR_CONFIRM_BTN,
                    CommonCraftAssets.GENERATOR_CONFIRM_BTN_HOVER,
                    Component.translatable("gui.maid_storage_manager.craft_guide.common.generate_alarm")
            );
        }, this
        ));
        generatorButton.visible = false;

        EditBox editBox = addRenderableWidget(new EditBox(
                this.font,
                getGuiLeft() + GENERATOR_BOX_X + 1,
                getGuiTop() + GENERATOR_BOX_Y + 32,
                CommonCraftAssets.GENERATOR_SELECTOR_BOX_LEFT.w + CommonCraftAssets.GENERATOR_SELECTOR_BOX_RIGHT.w,
                10,
                Component.translatable("gui.maid_storage_manager.craft_guide.common.generator_filter")
        ));
        editBox.setTextColor(0x000000FF);
        editBox.setBordered(false);
        editBox.setResponder(v -> this.searchFilterStr = v);
    }
    //endregion

    private void sendAndTriggerLocalPacket(CraftGuideGuiPacket packet) {
        ClientPacketDistributor.sendToServer(packet);
        menu.handleGuiPacket(packet.type, packet.key, packet.value, packet.data);
        this.handleGuiPacket(packet.type, packet.key, packet.value, packet.data);
    }

    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - this.imageHeight) / 2;

        GuiTools.guiBlit(guiGraphics,
                CommonCraftAssets.BACKGROUND,
                relX,
                relY,
                0,
                0,
                this.imageWidth,
                this.imageHeight);

        int c = 0;
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 2; y++) {
                if (menu.filterSlots[c].isActive()) {
                    ImageAsset imageAsset = (menu.isHandRelated && c == 1) ? CommonCraftAssets.SLOT_HAND : CommonCraftAssets.SLOT_NORMAL;
                    imageAsset.blit(guiGraphics, relX + menu.filterSlots[c].x - 1, relY + menu.filterSlots[c].y - 1);
                }
                c++;
            }
        }
    }

    @Override
    protected void extractTooltip(@NotNull GuiGraphicsExtractor graphics, int x, int y) {
        if (actionSelector.visible) return;
        graphics.pose().pushMatrix();
        graphics.pose().translate(0, 0);
        if (this.menu.getCarried().isEmpty()) {
            int inGuiX = x - this.getGuiLeft();
            int inGuiY = y - this.getGuiTop();
            for (Slot slot : this.getMenu().slots) {
                if (slot.x <= inGuiX && slot.x + 16 >= inGuiX && slot.y <= inGuiY && slot.y + 16 >= inGuiY) {
                    if (slot instanceof FilterSlot filterSlot && filterSlot.isActive()) {
                        if (!filterSlot.getItem().isEmpty())
                            graphics.setTooltipForNextFrame(this.font,
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
                        graphics.setTooltipForNextFrame(this.font,
                                buttonWidget.getTooltipComponent(),
                                x,
                                y
                        );
                    }
                }
            });
            if (menu.selectedGenerator && (x - getGuiLeft() > GENERATOR_BOX_X && y - getGuiTop() > GENERATOR_BOX_Y
                    && x - getGuiLeft() < GENERATOR_BOX_X + CommonCraftAssets.GENERATOR_SELECTOR_BOX_LEFT.w + CommonCraftAssets.GENERATOR_SELECTOR_BOX_RIGHT.w
                    && y - getGuiTop() < GENERATOR_BOX_Y + CommonCraftAssets.GENERATOR_SELECTOR_BOX_RIGHT.h
            )) {
                int sIdx = getSelectedGeneratorItem(x, y);
                if (sIdx >= 0 && sIdx < cachedInputs.size() && sIdx < cachedGeneratorOutputs.size()) {
                    List<Component> tooltips = new ArrayList<>();
                    cachedInputs.get(sIdx).forEach(itemStack ->
                            tooltips.add(
                                    Component.translatable("gui.maid_storage_manager.craft_guide.common.generator_input", itemStack.getDisplayName())
                                            .withStyle(ChatFormatting.GREEN)
                            )
                    );
                    cachedGeneratorOutputs.get(sIdx).getB().forEach(itemStack ->
                            tooltips.add(
                                    Component.translatable("gui.maid_storage_manager.craft_guide.common.generator_output", itemStack.getDisplayName())
                                            .withStyle(ChatFormatting.YELLOW)
                            )
                    );
                    graphics.setTooltipForNextFrame(this.font, convertComponentList(tooltips), x, y);
                }
            }
        }
        super.extractTooltip(graphics, x, y);
        graphics.pose().popMatrix();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int p_283661_, int p_281248_, float p_281886_) {
        super.extractRenderState(graphics, p_283661_, p_281248_, p_281886_);
        renderOptionEditorOrTip(graphics);
        renderBlockIndicator(graphics);
        renderNumberLabel(graphics);
        renderButtonIcon(graphics);
        renderArrow(graphics);
        renderScrollList(graphics, p_283661_, p_281248_);
        renderScrollBar(graphics, p_283661_, p_281248_);
        renderGeneratorList(graphics, p_283661_, p_281248_);
        renderGeneratorScrollBar(graphics, p_283661_, p_281248_);
        renderMiniBarInfo(graphics);
        renderGeneratorDecorations(graphics);
            actionSelector.extractWidgetRenderState(graphics, p_283661_, p_281248_, p_281886_);
        extractTooltip(graphics, p_283661_, p_281248_);
    }


    private void renderOptionEditorOrTip(@NotNull GuiGraphicsExtractor graphics) {
        for (int i = 0; i < options.size(); i++) {
            if (menu.currentEditingItems.options.size() <= i) continue;
            ActionOption opt = menu.currentEditingItems.options.get(i);
            Pair<SelectButtonWidget<Integer>, EditBox> editBox = options.get(i);
            if (editBox.getB().isVisible())
                CommonCraftAssets.OPTION_UNDERLINE.blit(graphics, editBox.getB().getX(), editBox.getB().getY() + editBox.getB().getHeight());
            else if (editBox.getA().isVisible() && !opt.valuePredicatorOrGetter().hasPredicator()) {
                Object ab = opt.converter().ab(editBox.getA().getData());
                graphics.pose().pushMatrix();
                graphics.pose().translate(editBox.getB().getX(), editBox.getB().getY() + 2);
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
                graphics.pose().popMatrix();
            }

            if (editBox.getA().isVisible()) {
                Identifier asset = opt.icon()[editBox.getA().getData()];
                GuiTools.guiBlit(graphics, asset, editBox.getA().getX(), editBox.getA().getY(), 0, 0, 11, 11);
            }
        }
    }

    private void renderArrow(@NotNull GuiGraphicsExtractor graphics) {
        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - this.imageHeight) / 2;
        if (menu.currentEditingItems.inputCount > 0) {
            CommonCraftAssets.ARROW_DOWN.blit(graphics, relX + 118, relY + 60);
        }
        if (menu.currentEditingItems.outputCount > 0) {
            CommonCraftAssets.ARROW_UP.blit(graphics, relX + 138, relY + 60);
        }
    }

    private void renderNumberLabel(@NotNull GuiGraphicsExtractor graphics) {
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
                    graphics.pose().pushMatrix();
                    graphics.pose().scale(0.6f, 0.6f);
                    graphics.pose().translate(0, 0);
                    graphics.text(this.font, text,
                            (int) ((relX + filterSlot.x + 16 - this.font.width(text) * 0.6) / 0.6f),
                            (int) ((relY + filterSlot.y + 16 - this.font.lineHeight * 0.6) / 0.6f),
                            0xFFFFFFFF,
                            false);
                    graphics.pose().popMatrix();
                }
            }
        }
        }

    private void renderButtonIcon(@NotNull GuiGraphicsExtractor graphics) {
        if (actionSelectorButton.isVisible()) {
            CommonCraftAssets.imageForAction(actionSelectorButton.getData().type())
                    .blit(graphics,
                            actionSelectorButton.getX() + 2,
                            actionSelectorButton.getY() + 2
                    );
        }
        }

    private void renderBlockIndicator(@NotNull GuiGraphicsExtractor graphics) {
        graphics.pose().pushMatrix();
        float scale = 1.3f;
        graphics.pose().scale(scale, scale);
        graphics.item(
                menu.blockIndicator.getItem(),
                (int) ((this.leftPos + menu.blockIndicator.x - 1) / scale),
                (int) ((this.topPos + menu.blockIndicator.y + 1) / scale)
        );
        graphics.pose().popMatrix();
    }

    private void renderMiniBarInfo(@NotNull GuiGraphicsExtractor graphics) {
        if (menu.currentEditingItems.step == null) {
            graphics.pose().pushMatrix();
            graphics.pose().translate(getGuiLeft() + 36, getGuiTop() + 131);
            graphics.pose().scale(0.6f, 0.6f);
            graphics.text(font,
                    Component.translatable("gui.maid_storage_manager.craft_guide.common.no_step_selected"),
                    0,
                    0,
                    0xFFFFFFFF,
                    false);
            graphics.pose().popMatrix();
            return;
        }
        NoPlaceFilterSlot bi = menu.blockIndicator;
        graphics.pose().pushMatrix();
        graphics.pose().translate(getGuiLeft() + 28, getGuiTop() + 128);
        graphics.pose().scale(0.7f, 0.7f);
        graphics.item(bi.getItem(), 0, 0);
        graphics.pose().popMatrix();

        graphics.pose().pushMatrix();
        graphics.pose().translate(getGuiLeft() + 41, getGuiTop() + 128);
        graphics.pose().scale(0.55f, 0.55f);
        graphics.text(font,
                Component.translatable("gui.maid_storage_manager.craft_guide.common.step_index", menu.selectedIndex + 1),
                0,
                0,
                0xFFFFFFFF,
                false
        );
        graphics.pose().popMatrix();

        graphics.pose().pushMatrix();
        graphics.pose().translate(getGuiLeft() + 41, getGuiTop() + 134);
        graphics.pose().scale(0.65f, 0.65f);
        graphics.text(font,
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
                0xFFFFFFFF,
                false
        );
        graphics.pose().popMatrix();
    }

    private void renderGeneratorDecorations(@NotNull GuiGraphicsExtractor graphics) {
        if (!menu.selectedGenerator) return;
        if (menu.generatorError) {
            graphics.pose().pushMatrix();
            graphics.pose().translate(getGuiLeft() + 113, getGuiTop() + 72);
            graphics.pose().scale(0.5f, 0.5f);
            int wordWrapColor = 0xFFFFFFFF;
            for (FormattedCharSequence line : font.split(Component.translatable("gui.maid_storage_manager.craft_guide.common.generator_error").withStyle(ChatFormatting.RED), 90)) {
                graphics.text(font, line, 0, 0, wordWrapColor);
                // FIXME: y offset needed for multiline
            }
            graphics.pose().popMatrix();
        }
        if (generatorButton.visible) {
            drawCenteredString(
                    graphics,
                    font,
                    generatorButton.getData() == 1 ?
                            Component.translatable("gui.maid_storage_manager.craft_guide.common.generator_confirm_1") :
                            Component.translatable("gui.maid_storage_manager.craft_guide.common.generator_confirm_0"),
                    generatorButton.getX() + 1,
                    generatorButton.getY() + 5,
                    generatorButton.getWidth() - 2,
                    0xFFFFFFFF,
                    false
            );
            CommonCraftAssets.GENERATOR_ALARM.blit(
                    graphics,
                    generatorButton.getX() - CommonCraftAssets.GENERATOR_ALARM.w - 5,
                    generatorButton.getY()
            );
        }
        if (menu.selectedGeneratorIndex != -1 && menu.selectedGeneratorIndex < getGeneratorOutputsWithFilter(searchFilterStr).size() && !menu.generatorError) {
            graphics.pose().pushMatrix();
            float scale = 1.3f;
            graphics.pose().scale(scale, scale);
            int idx = menu.player.tickCount / (Minecraft.getInstance().getFps() + 1);
            Pair<Integer, List<ItemStack>> os = getGeneratorOutputsWithFilter(searchFilterStr).get(menu.selectedGeneratorIndex);
            if (!os.getB().isEmpty())
                graphics.item(
                        os.getB().get(idx % os.getB().size()),
                        (int) ((getGuiLeft() + 122) / scale),
                        (int) ((getGuiTop() + 72) / scale)
                );
            graphics.pose().popMatrix();
        }
    }

    @Override
    public boolean mouseScrolled(double x, double y, double dx, double p_94688_) {
        if (actionSelector.visible) return actionSelector.mouseScrolled(x, y, dx, p_94688_);
        @Nullable Slot slot = this.getSlotUnderMouse();
        if (slot instanceof FilterSlot filterSlot && filterSlot.container instanceof CommonStepDataContainer sdc) {
            MutableInt count = new MutableInt(sdc.getCount(filterSlot.getContainerSlot()));
            int dv = (int) (Math.abs(p_94688_) / p_94688_);
            if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), InputConstants.KEY_LSHIFT))
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
        if (child.isPresent() && child.get() instanceof EditBox eb && eb.isVisible() && StringUtils.isNumeric(eb.getValue())) {
            int dv = (int) (Math.abs(p_94688_) / p_94688_);
            if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), InputConstants.KEY_LSHIFT))
                dv *= 10;
            int ov = Integer.parseInt(eb.getValue());
            eb.setValue(String.valueOf(ov + dv));
            return true;
        }
        if (x - getGuiLeft() > 26 && y - getGuiTop() > 26 && x - getGuiLeft() < 100 && y - getGuiTop() < 119) {
            scroll((float) -p_94688_ * 3);
        }
        if (x - getGuiLeft() > GENERATOR_BOX_X && y - getGuiTop() > GENERATOR_BOX_Y
                && x - getGuiLeft() < GENERATOR_BOX_X + CommonCraftAssets.GENERATOR_SELECTOR_BOX_LEFT.w + CommonCraftAssets.GENERATOR_SELECTOR_BOX_RIGHT.w
                && y - getGuiTop() < GENERATOR_BOX_Y + CommonCraftAssets.GENERATOR_SELECTOR_BOX_RIGHT.h
        )
            scrollGenerator((float) -p_94688_ * 3);
        return super.mouseScrolled(x, y, dx, p_94688_);
    }

    public void accept(FilterSlot slot, ItemStack item) {
        if (slot instanceof NoPlaceFilterSlot) return;
        if (!slot.isActive()) return;
        slot.set(item);
        sendAndTriggerLocalPacket(new CraftGuideGuiPacket(CraftGuideGuiPacket.Type.SET_ITEM, slot.index, 0, ItemStackUtil.saveStack(menu.player.registryAccess(), item)));
    }

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
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double x = event.x();
        double y = event.y();
        int p_97750_ = event.button();
        if (actionSelector.visible) {
            if (!actionSelector.isMouseOver(x, y)) {
                actionSelector.hide();
            } else return actionSelector.mouseClicked(event, doubleClick);
        }
        if (isInScrollBlockArea(x, y)) {
            mouseDraggingScrollingBar = y;
            mouseStartDraggingOffset = (double) scrollOffsetTop;
            scrollingGeneratorList = false;
        }
        if (isInGeneratorScrollArea(x, y)) {
            mouseDraggingScrollingBar = y;
            mouseStartDraggingOffset = (double) generatorScrollOffsetTop;
            scrollingGeneratorList = true;
        }

        if (x - getGuiLeft() > 26 && y - getGuiTop() > 26 && x - getGuiLeft() < 100 && y - getGuiTop() < 119) {
            clickInScrollList(x, y);
        }

        if (x - getGuiLeft() > GENERATOR_BOX_X && y - getGuiTop() > GENERATOR_BOX_Y
                && x - getGuiLeft() < GENERATOR_BOX_X + CommonCraftAssets.GENERATOR_SELECTOR_BOX_LEFT.w + CommonCraftAssets.GENERATOR_SELECTOR_BOX_RIGHT.w
                && y - getGuiTop() < GENERATOR_BOX_Y + CommonCraftAssets.GENERATOR_SELECTOR_BOX_RIGHT.h
        )
            clickInGeneratorScrollList(x, y);
        return super.mouseClicked(event, doubleClick);
    }


    //region scrolling control
    private static final int SCROLL_AREA_TOTAL_HEIGHT = 93;
    private boolean scrollingGeneratorList = false;
    private float scrollOffsetTop = 0;
    private float generatorScrollOffsetTop = 0;
    private Double mouseDraggingScrollingBar = null;
    private Double mouseStartDraggingOffset = null;


    private float getScrollBlockHeight() {
        return Math.max(
                Math.min(2 * SCROLL_AREA_TOTAL_HEIGHT - (menu.craftGuideData.steps.size() + (menu.hasGeneratorResult ? 1 : 0)) * (CommonCraftAssets.ROW.h - 1), SCROLL_AREA_TOTAL_HEIGHT + 6),
                10
        );
    }

    private float getScrollBlockOffset() {
        float scrollableHeight = getMaxListOffset();
        float totalMaxHeight = getMaxScrollOffset();
        return scrollOffsetTop * scrollableHeight / totalMaxHeight;
    }

    private float getMaxScrollOffset() {
        return (menu.craftGuideData.steps.size() + (menu.hasGeneratorResult ? 1 : 0)) * (CommonCraftAssets.ROW.h - 1) - SCROLL_AREA_TOTAL_HEIGHT;
    }

    private float getMaxListOffset() {
        return SCROLL_AREA_TOTAL_HEIGHT + 6 - getScrollBlockHeight();
    }

    private void makeScreenScissor(GuiGraphicsExtractor graphics, int x, int y, int x1, int y1) {
        graphics.enableScissor(getGuiLeft() + x, getGuiTop() + y, getGuiLeft() + x1, getGuiTop() + y1);
    }

    private void releaseScreenScissor(GuiGraphicsExtractor graphics) {
        graphics.disableScissor();
    }

    private void scroll(float delta) {
        scrollOffsetTop += delta;
        if (scrollOffsetTop > getMaxScrollOffset()) {
            scrollOffsetTop = getMaxScrollOffset();
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

    private void renderScrollList(GuiGraphicsExtractor graphics, int x, int y) {
        makeScreenScissor(graphics, 26, 26, 100, 119);
        int relX = (this.width - this.imageWidth) / 2 + 27;
        int relY = (this.height - this.imageHeight) / 2 + 27;
        int selected = getSelectedStep(x, y);
        graphics.pose().pushMatrix();
        graphics.pose().translate(relX, relY - scrollOffsetTop);
        graphics.pose().pushMatrix();
        for (int i = 0; i < menu.craftGuideData.steps.size(); i++) {
            CommonCraftAssets.ROW.blit(graphics, 0, 0);
            graphics.pose().translate(0, CommonCraftAssets.ROW.h - 1);
        }
        graphics.pose().popMatrix();
        graphics.pose().pushMatrix();
        for (int i = 0; i < menu.craftGuideData.steps.size(); i++) {
            if (i == selected) {
                CommonCraftAssets.ROW_HOVER.blit(graphics, 0, 0);
            }
            graphics.pose().translate(0, CommonCraftAssets.ROW.h - 1);
        }
        graphics.pose().popMatrix();

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
                    selected == i,
                    i == menu.selectedIndex,
                    x - relX,
                    (int) (y - relY + scrollOffsetTop - i * (CommonCraftAssets.ROW.h - 1))
            );
            graphics.pose().translate(0, CommonCraftAssets.ROW.h - 1);
        }
        if (menu.hasGeneratorResult) {
            CommonCraftAssets.GENERATOR_TIP.blit(graphics, 1, 1);
            if (menu.selectedGenerator || selected == menu.craftGuideData.steps.size())
                CommonCraftAssets.GENERATOR_TIP_HOVER.blit(graphics, 1, 1);
            drawCenteredString(graphics, font,
                    Component.translatable("gui.maid_storage_manager.craft_guide.common.generator_tip"),
                    8,
                    4,
                    CommonCraftAssets.GENERATOR_TIP.w - 16,
                    0xFFFFFFFF,
                    true
            );
            graphics.pose().translate(0, CommonCraftAssets.GENERATOR_TIP.h - 1);
        }
        graphics.pose().popMatrix();
        releaseScreenScissor(graphics);
    }

    private void renderScrollListRow(GuiGraphicsExtractor graphics, CraftGuideStepData step, boolean hover, boolean selected, int x, int y) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(47, 4);
        graphics.pose().scale(0.7f, 0.7f);
        CommonCraftAssets.imageForAction(step.action).blit(graphics, 0, 0);
        graphics.pose().popMatrix();
        graphics.pose().pushMatrix();
        graphics.pose().translate(2, 4);
        graphics.pose().scale(0.6f, 0.6f);
        boolean hasInput = false;
        for (ItemStack itemStack : step.getNonEmptyInput()) {
            graphics.item(itemStack, 0, 0);
            String text = String.valueOf(itemStack.getCount());
            graphics.pose().translate(0, 0);
            graphics.text(this.font,
                    text,
                    16 - this.font.width(text),
                    (int) (16 - this.font.lineHeight),
                    0xFFFFFFFF,
                    false);
            graphics.pose().translate(16, 0);
            hasInput = true;
        }
        if (hasInput) {
            graphics.pose().translate(4, 0);
        }
        for (ItemStack itemStack : step.getNonEmptyOutput()) {
            graphics.item(itemStack, 0, 0);
            String text = String.valueOf(itemStack.getCount());
            graphics.pose().translate(0, 0);
            graphics.text(this.font,
                    text,
                    16 - this.font.width(text),
                    (int) (16 - this.font.lineHeight),
                    0xFFFFFFFF,
                    false);
            graphics.pose().translate(16, 0);
        }
        graphics.pose().popMatrix();
        graphics.pose().pushMatrix();
        graphics.pose().translate(58, 2);
        graphics.pose().scale(0.7f, 0.7f);
        ItemStack blockIndicator = menu.player.level().getBlockState(step.storage.pos).getBlock().asItem().getDefaultInstance();
        graphics.item(blockIndicator, 0, 0);
        graphics.pose().popMatrix();

        if (hover && selected) {
            graphics.pose().pushMatrix();
            graphics.pose().translate(0, 0);
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
            graphics.pose().popMatrix();
        }
    }

    private void renderScrollBar(GuiGraphicsExtractor graphics, int x, int y) {
        boolean active = mouseDraggingScrollingBar != null && (!scrollingGeneratorList);
        ImageAsset base = active ? CommonCraftAssets.SCROLL_BASE_HOVER : CommonCraftAssets.SCROLL_BASE;
        GuiTools.blitNineSliced(
                graphics,
                CommonCraftAssets.BACKGROUND,
                getGuiLeft() + 100,
                getGuiTop() + 23 + (int) getScrollBlockOffset(),
                base.w,
                (int) getScrollBlockHeight(),
                1,
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
                getGuiTop() + 23 + (int) getScrollBlockOffset() + (int) (getScrollBlockHeight() / 2 - (float) deco.h / 2)
        );
    }

    private void clickInScrollList(double x, double y) {
        int id = getSelectedStep(x, y);
        if (id == -1 || id > menu.craftGuideData.steps.size()) return;
        if (id == menu.craftGuideData.steps.size() && !menu.hasGeneratorResult) return;
        if (isClickedOnDeleteBtn(x, y, id) && menu.selectedIndex == id) {
            sendAndTriggerLocalPacket(new CraftGuideGuiPacket(CraftGuideGuiPacket.Type.REMOVE, id));
        } else {
            if (id != menu.craftGuideData.steps.size()) {
                generatorButton.visible = false;
                menu.selectedGeneratorIndex = -1;
            }
            sendAndTriggerLocalPacket(new CraftGuideGuiPacket(CraftGuideGuiPacket.Type.SELECT, id));
        }
    }

    private boolean isInScrollBlockArea(double x, double y) {
        double rx = x - getGuiLeft();
        double ry = y - getGuiTop() - 23;
        if (rx > 104 || rx < 100) return false;
        if (ry < getScrollBlockOffset()) return false;
        if (ry > getScrollBlockOffset() + getScrollBlockHeight()) return false;
        return true;
    }


    private static final int GENERATOR_BOX_X = 109;
    private static final int GENERATOR_BOX_Y = 23;
    private static final int GENERATOR_BOX_ROWS = 3;
    private static final int GENERATOR_BOX_COLS = 4;

    String searchFilterStr = "";
    String cachedFilterStr = null;
    List<Pair<Integer, List<ItemStack>>> cachedGeneratorOutputs = new ArrayList<>();
    List<List<ItemStack>> cachedInputs = new ArrayList<>();

    public List<Pair<Integer, List<ItemStack>>> getGeneratorOutputsWithFilter(String filterStr) {
        if (menu.generatedUpdated) {
            cachedFilterStr = null;
            menu.generatedUpdated = false;
        }
        if (cachedFilterStr != null && cachedFilterStr.equals(filterStr))
            return cachedGeneratorOutputs;
        cachedFilterStr = filterStr;
        cachedInputs.clear();
        cachedGeneratorOutputs.clear();
        for (int i = 0; i < menu.generatedRecipes.outputs.size(); i++) {
            List<ItemStack> itemStacks = menu.generatedRecipes.outputs.get(i);
            List<Ingredient> inputs = menu.generatedRecipes.ingredients.get(i);
            if (itemStacks.stream().anyMatch(ii -> InventoryListUtil.isMatchSearchStr(ii, filterStr))) {
                cachedGeneratorOutputs.add(new Pair<>(i, itemStacks));
                cachedInputs.add(inputs.stream().map(ing -> ing.items().map(h -> h.value().getDefaultInstance()).toList()).map(ii -> InventoryListUtil.getMatchingForPlayer(ii)).toList());
            }
        }
        return cachedGeneratorOutputs;
    }

    private int getSelectedGeneratorItem(double x, double y) {
        int rh = CommonCraftAssets.SLOT_BACKGROUND_GENERATOR.h;
        int cw = CommonCraftAssets.SLOT_BACKGROUND_GENERATOR.w;

        if (x - getGuiLeft() > GENERATOR_BOX_X && y - getGuiTop() > GENERATOR_BOX_Y && x - getGuiLeft() < GENERATOR_BOX_X + cw * GENERATOR_BOX_COLS && y - getGuiTop() < GENERATOR_BOX_Y + rh * GENERATOR_BOX_ROWS) {
            int offsetY = (int) ((int) (y - getGuiTop() - GENERATOR_BOX_Y));
            int offsetX = (int) (x - getGuiLeft() - GENERATOR_BOX_X);
            int rid = (int) ((offsetY + generatorScrollOffsetTop) / rh);
            int cid = offsetX / cw;
            return rid * 4 + cid;
        }
        return -1;
    }

    private void renderGeneratorList(GuiGraphicsExtractor graphics, int x, int y) {
        if (!menu.selectedGenerator) return;
        int rh = CommonCraftAssets.SLOT_BACKGROUND_GENERATOR.h;
        int cw = CommonCraftAssets.SLOT_BACKGROUND_GENERATOR.w;
        int selected = getSelectedGeneratorItem(x, y);
        graphics.pose().pushMatrix();
        graphics.pose().translate(getGuiLeft() + GENERATOR_BOX_X, getGuiTop() + GENERATOR_BOX_Y);
        CommonCraftAssets.GENERATOR_SELECTOR_BOX_LEFT.blit(graphics, 0, 0);
        CommonCraftAssets.GENERATOR_SELECTOR_BOX_RIGHT.blit(graphics, CommonCraftAssets.GENERATOR_SELECTOR_BOX_LEFT.w, 0);
        makeScreenScissor(graphics, GENERATOR_BOX_X + 1, GENERATOR_BOX_Y + 1, GENERATOR_BOX_X + GENERATOR_BOX_COLS * cw - 1, GENERATOR_BOX_Y + GENERATOR_BOX_ROWS * rh);
        graphics.pose().translate(2, -generatorScrollOffsetTop);
        for (int i = 0; i < getGeneratorOutputsWithFilter(searchFilterStr).size(); i++) {
            if (i == selected)
                CommonCraftAssets.SLOT_BACKGROUND_GENERATOR.blit(graphics, 0, 0);
            graphics.pose().pushMatrix();
            graphics.pose().translate(1, 1);
            graphics.pose().scale((cw - 2) / 16.0f, (rh - 2) / 16.0f);
            if (getGeneratorOutputsWithFilter(searchFilterStr).get(i).getB().isEmpty())
                graphics.item(Items.BARRIER.getDefaultInstance(), 0, 0);
            else {
                int idx = menu.player.tickCount / (Minecraft.getInstance().getFps() + 1);
                graphics.item(getGeneratorOutputsWithFilter(searchFilterStr).get(i).getB().get(idx % getGeneratorOutputsWithFilter(searchFilterStr).get(i).getB().size()), 0, 0);
            }
            graphics.pose().popMatrix();
            if ((i + 1) % GENERATOR_BOX_COLS == 0) {
                graphics.pose().translate(-(cw - 1) * 3, rh);
            } else {
                graphics.pose().translate(cw - 1, 0);
            }
        }
        graphics.pose().popMatrix();
        releaseScreenScissor(graphics);
    }

    private static final int GENERATOR_SCROLL_AREA_TOTAL_HEIGHT = 30;

    private float getGeneratorScrollBlockHeight() {
        int cw = CommonCraftAssets.SLOT_BACKGROUND_GENERATOR.w;
        return Math.max(
                Math.min(2 * GENERATOR_SCROLL_AREA_TOTAL_HEIGHT - getGeneratorOutputsWithFilter(searchFilterStr).size() / 4 * cw, GENERATOR_SCROLL_AREA_TOTAL_HEIGHT),
                10
        );
    }

    private float getGeneratorScrollBlockOffset() {
        float scrollableHeight = getMaxGeneratorListOffset();
        float totalMaxHeight = getMaxGeneratorScrollOffset();
        return generatorScrollOffsetTop * scrollableHeight / totalMaxHeight;
    }

    private float getMaxGeneratorScrollOffset() {
        int cw = CommonCraftAssets.SLOT_BACKGROUND_GENERATOR.w;
        return ((int) (getGeneratorOutputsWithFilter(searchFilterStr).size() / 4) * cw - GENERATOR_SCROLL_AREA_TOTAL_HEIGHT);
    }

    private float getMaxGeneratorListOffset() {
        return GENERATOR_SCROLL_AREA_TOTAL_HEIGHT - getGeneratorScrollBlockHeight();
    }

    private boolean isInGeneratorScrollArea(double x, double y) {
        double rx = x - getGuiLeft() - GENERATOR_BOX_X - (CommonCraftAssets.GENERATOR_SELECTOR_BOX_LEFT.w + CommonCraftAssets.GENERATOR_SELECTOR_BOX_RIGHT.w);
        double ry = y - getGuiTop() - 23;
        if (rx < -3 || rx > 0)
            return false;
        if (ry < getGeneratorScrollBlockOffset()) return false;
        if (ry > getGeneratorScrollBlockOffset() + getGeneratorScrollBlockHeight()) return false;
        return true;
    }

    private void scrollGenerator(float delta) {
        generatorScrollOffsetTop += delta;
        if (generatorScrollOffsetTop > getMaxGeneratorScrollOffset()) {
            generatorScrollOffsetTop = getMaxGeneratorScrollOffset();
        }
        if (generatorScrollOffsetTop < 0) {
            generatorScrollOffsetTop = 0;
        }
    }


    private void renderGeneratorScrollBar(GuiGraphicsExtractor graphics, int x, int y) {
        if (!menu.selectedGenerator) return;
        boolean active = mouseDraggingScrollingBar != null && scrollingGeneratorList;
        ImageAsset base = active ? CommonCraftAssets.SCROLL_BLOCK_GENERATOR : CommonCraftAssets.SCROLL_BLOCK_GENERATOR;
        blitNineSliced(
                graphics,
                CommonCraftAssets.BACKGROUND,
                getGuiLeft() + GENERATOR_BOX_X + CommonCraftAssets.GENERATOR_SELECTOR_BOX_RIGHT.w + CommonCraftAssets.GENERATOR_SELECTOR_BOX_LEFT.w - 3,
                getGuiTop() + GENERATOR_BOX_Y + (int) getGeneratorScrollBlockOffset() + 1,
                base.w,
                (int) getGeneratorScrollBlockHeight(),
                1,
                1,
                base.w,
                base.h,
                base.u,
                base.v
        );
    }

    private void clickInGeneratorScrollList(double x, double y) {
        int id = getSelectedGeneratorItem(x, y);
        if (id == -1 || id > getGeneratorOutputsWithFilter(searchFilterStr).size()) return;
        menu.selectedGeneratorIndex = id;
        generatorButton.visible = true;
        generatorButton.setOption(null);
        menu.generatorError = false;
    }


    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        double x = event.x();
        double y = event.y();
        int p_97754_ = event.button();
        if (mouseDraggingScrollingBar != null && mouseStartDraggingOffset != null) {
            if (scrollingGeneratorList) {
                generatorScrollOffsetTop = (float) (mouseStartDraggingOffset + (y - mouseDraggingScrollingBar) / getMaxGeneratorListOffset() * getMaxGeneratorScrollOffset());
                scrollGenerator(0);
            } else {
                scrollOffsetTop = (float) (mouseStartDraggingOffset + (y - mouseDraggingScrollingBar) / getMaxListOffset() * getMaxScrollOffset());
                scroll(0);
            }
        }
        return super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        double p_97812_ = event.x();
        double p_97813_ = event.y();
        int p_97814_ = event.button();
        if (mouseDraggingScrollingBar != null || mouseStartDraggingOffset != null) {
            mouseDraggingScrollingBar = null;
            mouseStartDraggingOffset = null;
        }
        return super.mouseReleased(event);
    }


    public void drawCenteredString(GuiGraphicsExtractor graphics, Font pFont, Component pText, int pX, int pY, int maxWidth, int pColor, boolean shadow) {
        FormattedCharSequence formattedcharsequence = pText.getVisualOrderText();
        int textWidth = pFont.width(formattedcharsequence);
        int drawWidth = Math.max(textWidth, maxWidth);
        int alignWidth = Math.min(maxWidth, textWidth);
        float scale = (float) maxWidth / drawWidth;
        graphics.pose().pushMatrix();
        graphics.pose().scale(scale, scale);
        graphics.text(pFont, formattedcharsequence, (int)(pX / scale), (int)((pY - 3 + (14 - 8 * scale) / 2) / scale), pColor, shadow);
        graphics.pose().popMatrix();
    }

    public static List<FormattedCharSequence> convertComponentList(List<Component> components) {
        return components.stream().map(Component::getVisualOrderText).toList();
    }

    public void blitNineSliced(GuiGraphicsExtractor graphics, Identifier pAtlasLocation, int pX, int pY, int pWidth, int pHeight, int pSliceWidth, int pSliceHeight, int pUWidth, int pVHeight, int pTextureX, int pTextureY) {
        GuiTools.blitNineSliced(graphics, pAtlasLocation, pX, pY, pWidth, pHeight, pSliceWidth, pSliceHeight, pUWidth, pVHeight, pTextureX, pTextureY);
    }

    public void blitNineSliced(GuiGraphicsExtractor graphics, Identifier pAtlasLocation, int pX, int pY, int pWidth, int pHeight, int pLeftSliceWidth, int pTopSliceHeight, int pRightSliceWidth, int pBottomSliceHeight, int pUWidth, int pVHeight, int pTextureX, int pTextureY) {
        GuiTools.blitNineSliced(graphics, pAtlasLocation, pX, pY, pWidth, pHeight, pLeftSliceWidth, pTopSliceHeight, pRightSliceWidth, pBottomSliceHeight, pUWidth, pVHeight, pTextureX, pTextureY);
    }

    private static IntIterator slices(int pTarget, int pTotal) {
        int i = Mth.positiveCeilDiv(pTarget, pTotal);
        return new Divisor(pTarget, i);
    }
}