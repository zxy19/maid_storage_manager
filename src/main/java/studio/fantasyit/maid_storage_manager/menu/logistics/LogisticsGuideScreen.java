package studio.fantasyit.maid_storage_manager.menu.logistics;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.anti_ad.mc.ipn.api.IPNIgnore;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.menu.container.ButtonWidget;
import studio.fantasyit.maid_storage_manager.menu.container.NoPlaceFilterSlot;
import studio.fantasyit.maid_storage_manager.network.ItemSelectorGuiPacket;
import studio.fantasyit.maid_storage_manager.network.Network;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;
import yalter.mousetweaks.api.MouseTweaksDisableWheelTweak;

@MouseTweaksDisableWheelTweak
@IPNIgnore
public class LogisticsGuideScreen extends AbstractContainerScreen<LogisticsGuideMenu> {
    private static final ResourceLocation background = ResourceLocation.fromNamespaceAndPath(MaidStorageManager.MODID, "textures/gui/logistics_guide.png");

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

        if (!menu.slotGuide.hasItem()) {
            int i = (menu.player.tickCount % 80) / 40;
            graphics.setColor(1, 1, 1, 0.5f);
            graphics.renderItem(switch (i) {
                        case 0 -> ItemRegistry.CRAFT_GUIDE.get().getDefaultInstance();
                        case 1 -> ItemRegistry.FILTER_LIST.get().getDefaultInstance();
                        default -> ItemStack.EMPTY;
                    },
                    menu.slotGuide.x + this.getGuiLeft(),
                    menu.slotGuide.y + this.getGuiTop()
            );
            graphics.setColor(1, 1, 1, 1);
        }
    }

    @Override
    protected boolean isHovering(int x, int y, int p_97770_, int p_97771_, double p_97772_, double p_97773_) {
        if (x == menu.slotGuide.x && y == menu.slotGuide.y) {
            return super.isHovering(x - LogisticsGuideMenu.SLOT_GUIDE_OFFSET_X,
                    y - LogisticsGuideMenu.SLOT_GUIDE_OFFSET_Y,
                    31,
                    17,
                    p_97772_,
                    p_97773_);
        }
        return super.isHovering(x, y, p_97770_, p_97771_, p_97772_, p_97773_);
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int x, int y) {
        if (this.menu.getCarried().isEmpty()) {
            int inGuiX = x - this.getGuiLeft();
            int inGuiY = y - this.getGuiTop();
            for (Slot slot : this.getMenu().slots) {
                if (slot == menu.slotGuide) {
                    if (slot.x <= inGuiX && slot.x + 31 >= inGuiX && slot.y <= inGuiY && slot.y + 17 >= inGuiY) {
                        if (!slot.getItem().isEmpty())
                            graphics.renderTooltip(this.font,
                                    slot.getItem(),
                                    x,
                                    y
                            );
                    }
                }
//                else if (slot.x <= inGuiX && slot.x + 16 >= inGuiX && slot.y <= inGuiY && slot.y + 16 >= inGuiY) {
//                    if (!(slot instanceof FilterSlot)){
//                        if (!slot.getItem().isEmpty())
//                            graphics.renderTooltip(this.font,
//                                    slot.getItem(),
//                                    x,
//                                    y
//                            );
//                        return;
//                    }
//                }
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
}
