package studio.fantasyit.maid_storage_manager.menu.filter;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.integration.jei.IFilterScreen;
import studio.fantasyit.maid_storage_manager.menu.base.AbstractFilterScreen;
import studio.fantasyit.maid_storage_manager.menu.base.IItemTarget;
import studio.fantasyit.maid_storage_manager.menu.container.ButtonWidget;
import studio.fantasyit.maid_storage_manager.menu.container.FilterSlot;
import studio.fantasyit.maid_storage_manager.menu.container.InventorySelectButton;
import studio.fantasyit.maid_storage_manager.network.ItemSelectorGuiPacket;
import studio.fantasyit.maid_storage_manager.network.Network;
import studio.fantasyit.maid_storage_manager.util.GuiTools;
import studio.fantasyit.maid_storage_manager.util.InventoryListUtil;

import java.util.List;

import static studio.fantasyit.maid_storage_manager.network.Network.sendItemSelectorSetItemPacket;

public class FilterScreen extends AbstractFilterScreen<FilterMenu> implements IItemTarget, IFilterScreen {
    private static final Identifier background = Identifier.fromNamespaceAndPath(MaidStorageManager.MODID, "textures/gui/filter_list.png");
    private InventorySelectButton inventorySelectButton;

    public FilterScreen(FilterMenu p_97741_, Inventory p_97742_, Component p_97743_) {
        super(p_97741_, p_97742_, p_97743_, 176, 200);
        this.inventoryLabelY = this.imageHeight - 94;
        addButtons();
    }

    @Override
    protected void init() {
        super.init();
        addButtons();
        addInventoryListButton();
        refreshUUID(true);
    }

    private void addButtons() {
        this.addRenderableWidget(new ButtonWidget(
                137, 83, 16, 16,
                background,
                (widget) -> {
                    if (this.getMenu().matchTag) {
                        return new Pair<>(208, widget.isHovered() ? 16 : 0);
                    } else {
                        return new Pair<>(224, widget.isHovered() ? 16 : 0);
                    }
                },
                () -> this.getMenu().matchTag ?
                        Component.translatable("gui.maid_storage_manager.filter_list.match_tag_on") :
                        Component.translatable("gui.maid_storage_manager.filter_list.match_tag_off"),
                () -> {
                    this.getMenu().matchTag = !this.getMenu().matchTag;
                    Network.sendItemSelectorGuiPacket(
                            ItemSelectorGuiPacket.SlotType.MATCH_TAG,
                            0,
                            this.getMenu().matchTag ? 1 : 0
                    );
                },
                this
        ));
        this.addRenderableWidget(new ButtonWidget(
                137, 65, 16, 16,
                background,
                (widget) -> {
                    if (this.getMenu().isBlackList) {
                        return new Pair<>(192, widget.isHovered() ? 16 : 0);
                    } else {
                        return new Pair<>(176, widget.isHovered() ? 16 : 0);
                    }
                },
                () -> this.getMenu().isBlackList ?
                        Component.translatable("gui.maid_storage_manager.filter_list.blacklist") :
                        Component.translatable("gui.maid_storage_manager.filter_list.whitelist"),
                () -> {
                    this.getMenu().isBlackList = !this.getMenu().isBlackList;
                    Network.sendItemSelectorGuiPacket(
                            ItemSelectorGuiPacket.SlotType.BLACKLIST,
                            0,
                            this.getMenu().isBlackList ? 1 : 0
                    );
                },
                this
        ));
    }

    protected void addInventoryListButton() {
        inventorySelectButton = this.addRenderableWidget(new InventorySelectButton(
                getGuiLeft() + 12,
                getGuiTop() + 85,
                this
        ));
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
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int p_283661_, int p_281248_, float p_281886_) {
        super.extractRenderState(graphics, p_283661_, p_281248_, p_281886_);
        extractTooltip(graphics, p_283661_, p_281248_);
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int x, int y) {
        if (this.menu.getCarried().isEmpty()) {
            int inGuiX = x - this.getGuiLeft();
            int inGuiY = y - this.getGuiTop();
            for (Slot slot : this.getMenu().slots) {
                if (slot.x <= inGuiX && slot.x + 16 >= inGuiX && slot.y <= inGuiY && slot.y + 16 >= inGuiY) {
                    if (slot instanceof FilterSlot filterSlot) {
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
                    }
                }
            });
        }
        super.extractTooltip(graphics, x, y);
    }

    public void accept(FilterSlot menu, ItemStack item) {
        ItemStack itemStack = item.copyWithCount(1);
        getMenu().filteredItems.setItem(menu.getContainerSlot(), itemStack);
        sendItemSelectorSetItemPacket(menu.getContainerSlot(), itemStack);
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
