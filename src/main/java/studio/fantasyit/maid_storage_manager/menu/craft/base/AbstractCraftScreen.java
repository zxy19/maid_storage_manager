package studio.fantasyit.maid_storage_manager.menu.craft.base;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.anti_ad.mc.ipn.api.IPNIgnore;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.integration.jei.IFilterScreen;
import studio.fantasyit.maid_storage_manager.menu.base.AbstractFilterScreen;
import studio.fantasyit.maid_storage_manager.menu.container.FilterSlot;
import studio.fantasyit.maid_storage_manager.menu.container.SelectButtonWidget;
import studio.fantasyit.maid_storage_manager.network.CraftGuideGuiPacket;
import studio.fantasyit.maid_storage_manager.util.GuiTools;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;
import yalter.mousetweaks.api.MouseTweaksDisableWheelTweak;

import java.util.List;

@MouseTweaksDisableWheelTweak
@IPNIgnore
abstract public class AbstractCraftScreen<T extends AbstractCraftMenu> extends AbstractFilterScreen<T> implements ICraftGuiPacketReceiver, IFilterScreen {
    protected boolean enableScroll = false;
    protected final Identifier iBackground;

    public AbstractCraftScreen(T p_97741_, Inventory p_97742_, Component p_97743_, Identifier background) {
        this(p_97741_, p_97742_, p_97743_, background, false);
    }

    public AbstractCraftScreen(T p_97741_, Inventory p_97742_, Component p_97743_, Identifier background, boolean enableScroll) {
        super(p_97741_, p_97742_, p_97743_, 176, 245);
        this.enableScroll = enableScroll;
        this.iBackground = background;
        this.inventoryLabelY = this.imageHeight - 94;
        this.inventoryLabelX = 8;
    }

    @Override
    protected void init() {
        super.init();
        this.addButtons();
    }

    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - this.imageHeight) / 2;

        GuiTools.guiBlit(guiGraphics, iBackground,
                relX,
                relY,
                0,
                0,
                this.imageWidth,
                this.imageHeight);
    }

    protected void addButtons() {

    }

    protected void sendAndTriggerLocalPacket(CraftGuideGuiPacket packet) {
        ClientPacketDistributor.sendToServer(packet);
        menu.handleGuiPacket(packet.type, packet.key, packet.value, packet.data);
        this.handleGuiPacket(packet.type, packet.key, packet.value, packet.data);
    }


    @Override
    protected void extractTooltip(@NotNull GuiGraphicsExtractor graphics, int x, int y) {
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
        renderNumberLabel(graphics);
    }

    private void renderNumberLabel(@NotNull GuiGraphicsExtractor graphics) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(0, 0);
        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - this.imageHeight) / 2;
        for (Slot slot : this.getMenu().slots) {
            if (slot instanceof FilterSlot filterSlot && filterSlot.container instanceof StepDataContainer sdc && filterSlot.isActive()) {
                if (filterSlot.hasItem()) {
                    int count = sdc.getCount(filterSlot.getContainerSlot());
                    String text = String.valueOf(count);
                    if (count == -1) {
                        text = "*";
                    }
                    graphics.pose().pushMatrix();
                    graphics.pose().scale(0.6f, 0.6f);
                    graphics.text(this.font, text,
                            (int) ((relX + filterSlot.x + 16 - this.font.width(text) * 0.6) / 0.6f),
                            (int) ((relY + filterSlot.y + 16 - this.font.lineHeight * 0.6) / 0.6f),
                            0xffffffff);
                    graphics.pose().popMatrix();
                }
            }
        }

        graphics.pose().popMatrix();
    }

    public void accept(FilterSlot slot, ItemStack item) {
        if (!slot.isActive() || slot.readonly) return;
        slot.set(item);
        sendAndTriggerLocalPacket(new CraftGuideGuiPacket(CraftGuideGuiPacket.Type.SET_ITEM, slot.index, 0, ItemStackUtil.saveStack(menu.registryAccess(), item)));
    }

    public List<FilterSlot> getSlots() {
        return this.menu.slots.stream()
                .filter(slot -> slot instanceof FilterSlot fs && !fs.readonly)
                .map(slot -> (FilterSlot) slot)
                .toList();
    }


    @Override
    public boolean mouseScrolled(double p_94686_, double p_94687_, double dx, double p_94688_) {
        if (!enableScroll) return false;
        @Nullable Slot slot = this.getSlotUnderMouse();
        if (slot instanceof FilterSlot filterSlot && filterSlot.container instanceof StepDataContainer sdc && !filterSlot.readonly) {
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
                            filterSlot.getContainerSlot(),
                            count.getValue()
                    )
            );
        }
        return super.mouseScrolled(p_94686_, p_94687_, dx, p_94688_);
    }

    @Override
    public void handleGuiPacket(CraftGuideGuiPacket.Type type, int key, int value, @Nullable CompoundTag data) {
    }

}
