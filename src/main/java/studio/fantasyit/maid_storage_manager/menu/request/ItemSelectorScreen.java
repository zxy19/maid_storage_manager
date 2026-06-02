package studio.fantasyit.maid_storage_manager.menu.request;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.anti_ad.mc.ipn.api.IPNIgnore;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.integration.jei.IFilterScreen;
import studio.fantasyit.maid_storage_manager.items.data.RequestItemStackList;
import studio.fantasyit.maid_storage_manager.menu.base.AbstractFilterScreen;
import studio.fantasyit.maid_storage_manager.menu.base.IItemTarget;
import studio.fantasyit.maid_storage_manager.menu.container.ButtonWidget;
import studio.fantasyit.maid_storage_manager.menu.container.FilterContainer;
import studio.fantasyit.maid_storage_manager.menu.container.FilterSlot;
import studio.fantasyit.maid_storage_manager.menu.container.InventorySelectButton;
import studio.fantasyit.maid_storage_manager.network.ItemSelectorGuiPacket;
import studio.fantasyit.maid_storage_manager.network.Network;
import studio.fantasyit.maid_storage_manager.registry.DataComponentRegistry;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;
import studio.fantasyit.maid_storage_manager.util.GuiTools;
import studio.fantasyit.maid_storage_manager.util.InventoryListUtil;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;
import yalter.mousetweaks.api.MouseTweaksDisableWheelTweak;

import java.util.List;
import java.util.Optional;

import static studio.fantasyit.maid_storage_manager.network.Network.sendItemSelectorSetItemPacket;

@MouseTweaksDisableWheelTweak
@IPNIgnore
public class ItemSelectorScreen extends AbstractFilterScreen<ItemSelectorMenu> implements IItemTarget, IFilterScreen {
    private final Identifier background = Identifier.fromNamespaceAndPath(MaidStorageManager.MODID, "textures/gui/item_selector.png");
    AbstractWidget repeatControl;
    InventorySelectButton inventorySelectButton;

    public ItemSelectorScreen(ItemSelectorMenu p_97741_, Inventory p_97742_, Component p_97743_) {
        super(p_97741_, p_97742_, p_97743_, 176, 200);
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        this.addButtons();
        this.addRepeatControl();
        this.addInventoryListButton();
        refreshUUID(true);
    }

    protected List<Component> getTooltipForResult(int slot) {
        FilterContainer filteredItems = this.getMenu().filteredItems;
        ItemStack listItem = null;
        if (Minecraft.getInstance().player != null)
            listItem = Minecraft.getInstance().player.getItemInHand(InteractionHand.MAIN_HAND);
        List<ItemStack> itemStackStream = null;
        Component additionFailMessage = null;
        if (listItem != null) {
            RequestItemStackList.Immutable data = listItem.getOrDefault(DataComponentRegistry.REQUEST_ITEMS, RequestItemStackList.EMPTY);
            itemStackStream = data
                    .list()
                    .get(slot)
                    .missing()
                    .stream()
                    .toList();
            if (!data.list().get(slot).failAddition().isBlank())
                additionFailMessage = Component.translatable(data.list().get(slot).failAddition());
        }

        List<Component> tooltip = this.getTooltipFromContainerItem(filteredItems.getItem(slot));
        Integer collected = filteredItems.collected[slot].getValue();
        Integer requested = filteredItems.count[slot].getValue();
        if (filteredItems.done[slot].getValue() == 1) {
            if (requested <= collected || collected == -1) {
                tooltip.add(Component.translatable("tooltip.maid_storage_manager.request_list.done_success").withStyle(ChatFormatting.GREEN));
            } else {
                tooltip.add(Component.translatable("tooltip.maid_storage_manager.request_list.done_fail").withStyle(ChatFormatting.RED));
            }
        } else {
            tooltip.add(Component.translatable("tooltip.maid_storage_manager.request_list.processing").withStyle(ChatFormatting.YELLOW));
        }

        if (itemStackStream != null && !itemStackStream.isEmpty()) {
            tooltip.add(Component.translatable("tooltip.maid_storage_manager.request_list.missing_items").withStyle(ChatFormatting.RED));
            for (ItemStack itemStack : itemStackStream) {
                tooltip.add(Component.translatable("tooltip.maid_storage_manager.request_list.missing_items_item", itemStack.getHoverName(), itemStack.getCount()));
            }
        }
        if (additionFailMessage != null)
            tooltip.add(additionFailMessage.copy().withStyle(ChatFormatting.RED));

        tooltip.add(Component.translatable("tooltip.maid_storage_manager.request_list.collected", collected, String.valueOf(requested == -1 ? "*" : requested)));

        return tooltip;
    }

    private void addButtons() {
        this.addRenderableWidget(new ButtonWidget(
                119, 70, 16, 16,
                background,
                (widget) ->
                        switch (this.getMenu().matching) {
                            case AUTO -> new Pair<>(224, widget.isHovered() ? 48 : 32);
                            case MATCHING -> new Pair<>(208, widget.isHovered() ? 16 : 0);
                            case NOT_MATCHING -> new Pair<>(224, widget.isHovered() ? 16 : 0);
                        },
                () -> switch (this.getMenu().matching) {
                    case AUTO -> Component.translatable("gui.maid_storage_manager.request_list.match_tag_auto");
                    case MATCHING -> Component.translatable("gui.maid_storage_manager.request_list.match_tag_on");
                    case NOT_MATCHING -> Component.translatable("gui.maid_storage_manager.request_list.match_tag_off");
                },
                () -> {
                    this.getMenu().matching = switch (this.getMenu().matching) {
                        case AUTO -> ItemStackUtil.MATCH_TYPE.MATCHING;
                        case MATCHING -> ItemStackUtil.MATCH_TYPE.NOT_MATCHING;
                        case NOT_MATCHING -> ItemStackUtil.MATCH_TYPE.AUTO;
                    };
                    Network.sendItemSelectorGuiPacket(
                            ItemSelectorGuiPacket.SlotType.MATCH_TAG,
                            0,
                            this.getMenu().matching.ordinal()
                    );
                },
                this
        ));
        this.addRenderableWidget(new ButtonWidget(
                148, 70, 16, 16,
                background,
                (widget) -> {
                    if (this.getMenu().stockMode) {
                        return new Pair<>(176, widget.isHovered() ? 48 : 32);
                    } else {
                        return new Pair<>(192, widget.isHovered() ? 48 : 32);
                    }
                },
                () -> this.getMenu().stockMode ?
                        Component.translatable("gui.maid_storage_manager.request_list.stock_mode_on") :
                        Component.translatable("gui.maid_storage_manager.request_list.stock_mode_off"),
                () -> {
                    this.getMenu().stockMode = !this.getMenu().stockMode;
                    if (this.getMenu().stockMode) {
                        this.getMenu().blackmode = false;
                        Network.sendItemSelectorGuiPacket(
                                ItemSelectorGuiPacket.SlotType.BLACKLIST,
                                0,
                                0
                        );
                    }
                    Network.sendItemSelectorGuiPacket(
                            ItemSelectorGuiPacket.SlotType.STOCKMODE,
                            0,
                            this.getMenu().stockMode ? 1 : 0
                    );
                },
                this
        ));
        this.addRenderableWidget(new ButtonWidget(
                119, 91, 16, 16,
                background,
                (widget) -> {
                    if (this.getMenu().blackmode) {
                        return new Pair<>(192, widget.isHovered() ? 16 : 0);
                    } else {
                        return new Pair<>(176, widget.isHovered() ? 16 : 0);
                    }
                },
                () -> this.getMenu().blackmode ?
                        Component.translatable("gui.maid_storage_manager.request_list.blackmode_on") :
                        Component.translatable("gui.maid_storage_manager.request_list.blackmode_off"),
                () -> {
                    this.getMenu().blackmode = !this.getMenu().blackmode;
                    if (this.getMenu().blackmode) {
                        this.getMenu().stockMode = false;
                        Network.sendItemSelectorGuiPacket(
                                ItemSelectorGuiPacket.SlotType.STOCKMODE,
                                0,
                                0
                        );
                    }
                    Network.sendItemSelectorGuiPacket(
                            ItemSelectorGuiPacket.SlotType.BLACKLIST,
                            0,
                            this.getMenu().blackmode ? 1 : 0
                    );
                },
                this
        ));
        this.addRenderableWidget(new ButtonWidget(
                148, 91, 16, 16,
                background,
                (widget) -> new Pair<>(208, widget.isHovered() ? 48 : 32),
                () -> Component.translatable("gui.maid_storage_manager.request_list.clear"),
                () -> {
                    this.getMenu().shouldClear = true;
                    Network.sendItemSelectorGuiPacket(
                            ItemSelectorGuiPacket.SlotType.CLEAR,
                            0,
                            1
                    );
                },
                this
        ));
    }

    private void addRepeatControl() {
        repeatControl = this.addRenderableWidget(new AbstractWidget(
                116,
                26,
                52,
                28,
                Component.translatable("gui.maid_storage_manager.request_list.repeat")
        ) {
            public void onClick(MouseButtonEvent event, boolean doubleClick) {
                menu.unitSecond = !menu.unitSecond;
                Network.sendItemSelectorGuiPacket(
                        ItemSelectorGuiPacket.SlotType.UNITSECOND,
                        0,
                        menu.unitSecond ? 1 : 0
                );
            }

            @Override
            public int getX() {
                return getGuiLeft() + super.getX();
            }

            @Override
            public int getY() {
                return getGuiTop() + super.getY();
            }

            @Override
            public void extractWidgetRenderState(GuiGraphicsExtractor graphics, int p_268034_, int p_268009_, float p_268085_) {
                graphics.text(Minecraft.getInstance().font,
                        Component.translatable("gui.maid_storage_manager.request_list.repeat"),
                        this.getX() + 6,
                        this.getY() + 4,
                        0xFFFFFF,
                        false
                );
                MutableComponent repeatDesc = Component.translatable("gui.maid_storage_manager.request_list.never");
                if (getMenu().repeat != 0) {
                    if (getMenu().unitSecond)
                        repeatDesc = Component.translatable("gui.maid_storage_manager.request_list.repeat_desc_s", String.valueOf(getMenu().repeat));
                    else
                        repeatDesc = Component.translatable("gui.maid_storage_manager.request_list.repeat_desc_t", String.valueOf(getMenu().repeat));
                }
                graphics.text(Minecraft.getInstance().font,
                        repeatDesc,
                        this.getX() + 5,
                        this.getY() + 15,
                        0x2e7d32,
                        false
                );
            }

            @Override
            public boolean mouseScrolled(double p_94734_, double p_94735_, double dx, double dy) {
                int dv = (int) (Math.abs(dy) / dy);
                if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), InputConstants.KEY_LSHIFT))
                    dv *= 10;
                if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), InputConstants.KEY_LCONTROL))
                    dv *= 10;
                getMenu().repeat = Math.max(0, Math.min(getMenu().repeat + dv, 20 * 3600));
                Network.sendItemSelectorGuiPacket(
                        ItemSelectorGuiPacket.SlotType.REPEAT,
                        0,
                        getMenu().repeat
                );
                return true;
            }

            @Override
            protected void updateWidgetNarration(NarrationElementOutput p_259858_) {
                p_259858_.add(NarratedElementType.HINT, this.getMessage());
            }
        });
    }

    protected void addInventoryListButton() {
        inventorySelectButton = this.addRenderableWidget(new InventorySelectButton(
                getGuiLeft() + 8,
                getGuiTop() + 50,
                this
        ));
    }

    @Override
    protected void containerTick() {
        refreshUUID(false);
    }

    protected void refreshUUID(boolean force) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && (player.tickCount % 20 == 0 || force)) {
            Object inventoryListUUIDFromPlayerInv = InventoryListUtil.getInventoryListUUIDFromPlayerInv(player.inventory.getNonEquipmentItems());
            if (inventoryListUUIDFromPlayerInv != null) {
                inventorySelectButton.setUUID(inventoryListUUIDFromPlayerInv);
            }
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        
        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - this.imageHeight) / 2;

        GuiTools.guiBlit(guiGraphics, background,
                relX,
                relY,
                0,
                0,
                this.imageWidth,
                this.imageHeight);

        // guiGraphics.setColor removed in MC 26.1
        guiGraphics.item(
                ItemRegistry.STORAGE_DEFINE_BAUBLE.get().getDefaultInstance(),
                relX + 8,
                relY + 71
        );
        // guiGraphics.setColor removed in MC 26.1
    }

    @Override
    protected void extractTooltip(@NotNull GuiGraphicsExtractor graphics, int x, int y) {
        if (this.menu.getCarried().isEmpty()) {
            int inGuiX = x - this.getGuiLeft();
            int inGuiY = y - this.getGuiTop();
            for (Slot slot : this.getMenu().slots) {
                if (slot.x <= inGuiX && slot.x + 30 >= inGuiX && slot.y <= inGuiY && slot.y + 16 >= inGuiY) {
                    if (slot instanceof FilterSlot filterSlot) {
                        if (!filterSlot.getItem().isEmpty())
                            graphics.setTooltipForNextFrame(this.font,
                                    getTooltipForResult(filterSlot.getContainerSlot()),
                                    Optional.empty(),
                                    x,
                                    y
                            );
                        return;
                    }
                }
            }
            this.children().forEach(renderable -> {
                if (renderable.isMouseOver(x, y)) {
                    if (renderable instanceof ButtonWidget buttonWidget) {
                        graphics.setTooltipForNextFrame(this.font,
                                buttonWidget.getTooltipComponent(),
                                x,
                                y
                        );
                    } else if (renderable instanceof InventorySelectButton buttonWidget) {
                        graphics.setTooltipForNextFrame(this.font,
                                buttonWidget.getTooltipComponent(),
                                x,
                                y
                        );
                    } else if (repeatControl == renderable) {
                        graphics.setTooltipForNextFrame(this.font,
                                List.of(Component.translatable("gui.maid_storage_manager.request_list.scroll_to_adjust"),
                                        Component.translatable("gui.maid_storage_manager.request_list.click_to_switch")),
                                Optional.empty(),
                                ItemStack.EMPTY,
                                x,
                                y
                        );
                    }
                }
            });
        }
        super.extractTooltip(graphics, x, y);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int p_283661_, int p_281248_, float p_281886_) {
        super.extractRenderState(graphics, p_283661_, p_281248_, p_281886_);
        graphics.pose().pushMatrix();
        graphics.pose().translate(0, 0);
        extractTooltip(graphics, p_283661_, p_281248_);
        graphics.pose().popMatrix();
        graphics.pose().pushMatrix();
        graphics.pose().translate(0, 0);
        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - this.imageHeight) / 2;
        FilterContainer filters = this.getMenu().filteredItems;
        for (Slot slot : this.getMenu().slots) {
            if (slot instanceof FilterSlot filterSlot) {
                if (filterSlot.hasItem()) {
                    MutableInt count = filters.count[filterSlot.getContainerSlot()];
                    MutableInt done = filters.done[filterSlot.getContainerSlot()];
                    MutableInt collected = filters.collected[filterSlot.getContainerSlot()];
                    String text = String.valueOf(count.getValue());
                    if (count.getValue() == -1) {
                        text = "*";
                    }
                    graphics.pose().pushMatrix();
                    graphics.pose().scale(0.6f, 0.6f);
                    graphics.text(this.font, text,
                            (int) ((relX + filterSlot.x + 16 - this.font.width(text) * 0.6) / 0.6f),
                            (int) ((relY + filterSlot.y + 16 - this.font.lineHeight * 0.6) / 0.6f),
                            0xffffff);

                    String collectedStr = String.valueOf(collected.getValue());
                    graphics.text(this.font, collectedStr,
                            (int) ((relX + filterSlot.x + 30 - this.font.width(collectedStr) * 0.6) / 0.6),
                            (int) ((relY + filterSlot.y + 16 - this.font.lineHeight * 0.6) / 0.6),
                            0xffffff);
                    graphics.pose().popMatrix();

                    if (done.getValue() != 0) {
                        if (collected.getValue() < count.getValue() && count.getValue() != -1 && !menu.blackmode) {
                            GuiTools.guiBlit(graphics, background,
                                    relX + filterSlot.x + 20,
                                    relY + filterSlot.y + 4,
                                    179,
                                    83,
                                    10, 10
                            );
                        } else {
                            GuiTools.guiBlit(graphics, background,
                                    relX + filterSlot.x + 20,
                                    relY + filterSlot.y + 4,
                                    179,
                                    67,
                                    10, 10
                            );
                        }
                    }
                }
            }
        }
        graphics.pose().popMatrix();
    }

    @Override
    public boolean mouseScrolled(double p_94686_, double p_94687_, double dx, double dy) {
        @Nullable Slot slot = this.getSlotUnderMouse();
        if (slot instanceof FilterSlot filterSlot) {
            MutableInt count = this.getMenu().filteredItems.count[filterSlot.getContainerSlot()];
            int dv = (int) (Math.abs(dy) / dy);
            if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), InputConstants.KEY_LSHIFT))
                dv *= 10;

            if (dv > 0) {
                if (count.addAndGet(dv) == 0) count.addAndGet(1);
            } else {
                if (count.addAndGet(dv) <= 0) count.setValue(-1);
            }
            Network.sendItemSelectorGuiPacket(
                    ItemSelectorGuiPacket.SlotType.COUNT,
                    filterSlot.getContainerSlot(),
                    count.getValue()
            );
        }
        return super.mouseScrolled(p_94686_, p_94687_, dx, dy);
    }

    public void accept(FilterSlot menu, ItemStack item) {
        ItemStack itemStack = item.copyWithCount(1);
        getMenu().filteredItems.setItem(menu.getContainerSlot(), itemStack);
        sendItemSelectorSetItemPacket(menu.getContainerSlot(), itemStack);
    }

    public List<FilterSlot> getSlots() {
        return this.getMenu().slots.stream().filter(slot -> slot instanceof FilterSlot).map(slot -> (FilterSlot) slot).toList();
    }

    @Override
    public void itemSelected(ItemStack stack) {
        for (FilterSlot slot : getSlots()) {
            if (slot.isActive() && slot.getItem().isEmpty()) {
                accept(slot, stack);
                break;
            }
        }
    }
}
