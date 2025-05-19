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
import studio.fantasyit.maid_storage_manager.menu.container.NoPlaceFilterSlot;
import studio.fantasyit.maid_storage_manager.network.ItemSelectorGuiPacket;
import studio.fantasyit.maid_storage_manager.network.Network;
import yalter.mousetweaks.api.MouseTweaksDisableWheelTweak;

import java.util.List;

import static studio.fantasyit.maid_storage_manager.network.Network.sendItemSelectorSetItemPacket;

@MouseTweaksDisableWheelTweak
@IPNIgnore
public class LogisticsGuideScreen extends AbstractFilterScreen<LogisticsGuideMenu> {
    private static final ResourceLocation background = new ResourceLocation(MaidStorageManager.MODID, "textures/gui/logistics_guide.png");

    public LogisticsGuideScreen(LogisticsGuideMenu p_97741_, Inventory p_97742_, Component p_97743_) {
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
                146, 91, 16, 16,
                background,
                (widget) -> {
                    if (this.getMenu().single_mode) {
                        return new Pair<>(176, widget.isHovered() ? 16 : 0);
                    } else {
                        return new Pair<>(192, widget.isHovered() ? 16 : 0);
                    }
                },
                () -> this.getMenu().single_mode ?
                        Component.translatable("gui.maid_storage_manager.logistics_guide.single_mode") :
                        Component.translatable("gui.maid_storage_manager.logistics_guide.stack_mode"),
                () -> {
                    this.getMenu().single_mode = !this.getMenu().single_mode;
                    Network.sendItemSelectorGuiPacket(
                            ItemSelectorGuiPacket.SlotType.STOCKMODE,
                            0,
                            this.getMenu().single_mode ? 1 : 0
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

        guiGraphics.pose().pushPose();
        float scale = 1.3f;
        guiGraphics.pose().scale(scale, scale, 1);

        for (Slot slot : menu.slots) {
            if (slot instanceof NoPlaceFilterSlot)
                guiGraphics.renderItem(
                        slot.getItem(),
                        (int) ((this.leftPos + slot.x + 1) / scale),
                        (int) ((this.topPos + slot.y - (scale - 1) * 18 / 2) / scale)
                );
        }

        guiGraphics.pose().popPose();
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
        getMenu().container.setItem(menu.getContainerSlot(), itemStack);
        sendItemSelectorSetItemPacket(menu.getContainerSlot(), itemStack);
    }

    @Override
    public List<FilterSlot> getSlots() {
        return this.getMenu().slots.stream().filter(slot -> slot instanceof FilterSlot).map(slot -> (FilterSlot) slot).toList();
    }


}
