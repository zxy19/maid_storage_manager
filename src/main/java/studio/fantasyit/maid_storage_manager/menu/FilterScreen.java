package studio.fantasyit.maid_storage_manager.menu;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.anti_ad.mc.ipn.api.IPNIgnore;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.menu.container.ButtonWidget;
import studio.fantasyit.maid_storage_manager.menu.container.FilterSlot;
import studio.fantasyit.maid_storage_manager.network.ItemSelectorGuiPacket;
import studio.fantasyit.maid_storage_manager.network.Network;
import yalter.mousetweaks.api.MouseTweaksDisableWheelTweak;

import java.util.List;

import static studio.fantasyit.maid_storage_manager.network.Network.sendItemSelectorSetItemPacket;

@MouseTweaksDisableWheelTweak
@IPNIgnore
public class FilterScreen extends AbstractFilterScreen<FilterMenu>{
    private static final ResourceLocation background = new ResourceLocation(MaidStorageManager.MODID, "textures/gui/filter_list.png");

    public FilterScreen(FilterMenu p_97741_, Inventory p_97742_, Component p_97743_) {
        super(p_97741_, p_97742_, p_97743_);
        this.imageWidth = 176;
        this.imageHeight = 200;
        this.inventoryLabelY = this.imageHeight - 94;
        addButtons();
    }

    @Override
    protected void init() {
        super.init();
        addButtons();
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
    }

    @Override
    public void render(GuiGraphics graphics, int p_283661_, int p_281248_, float p_281886_) {
        super.render(graphics, p_283661_, p_281248_, p_281886_);
        renderTooltip(graphics, p_283661_, p_281248_);
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
                    if (renderable instanceof ButtonWidget buttonWidget) {
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

    @Override
    public void accept(FilterSlot menu, ItemStack item) {
        ItemStack itemStack = item.copyWithCount(1);
        getMenu().filteredItems.setItem(menu.getContainerSlot(), itemStack);
        sendItemSelectorSetItemPacket(menu.getContainerSlot(), itemStack);
    }

    @Override
    public List<FilterSlot> getSlots() {
        return this.getMenu().slots.stream().filter(slot -> slot instanceof FilterSlot).map(slot -> (FilterSlot) slot).toList();
    }


}
