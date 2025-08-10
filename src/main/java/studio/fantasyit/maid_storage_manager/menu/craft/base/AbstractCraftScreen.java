package studio.fantasyit.maid_storage_manager.menu.craft.base;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.PacketDistributor;
import org.anti_ad.mc.ipn.api.IPNIgnore;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.menu.base.AbstractFilterScreen;
import studio.fantasyit.maid_storage_manager.menu.container.FilterSlot;
import studio.fantasyit.maid_storage_manager.menu.container.SelectButtonWidget;
import studio.fantasyit.maid_storage_manager.network.CraftGuideGuiPacket;
import studio.fantasyit.maid_storage_manager.network.Network;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;
import yalter.mousetweaks.api.MouseTweaksDisableWheelTweak;

import java.util.List;

@MouseTweaksDisableWheelTweak
@IPNIgnore
abstract public class AbstractCraftScreen<T extends AbstractCraftMenu> extends AbstractFilterScreen<T> implements ICraftGuiPacketReceiver {
    protected boolean enableScroll = false;
    protected final ResourceLocation iBackground;
    public AbstractCraftScreen(T p_97741_, Inventory p_97742_, Component p_97743_,ResourceLocation background){
        this(p_97741_, p_97742_, p_97743_, background,false);
    }
    public AbstractCraftScreen(T p_97741_, Inventory p_97742_, Component p_97743_,ResourceLocation background, boolean enableScroll) {
        super(p_97741_, p_97742_, p_97743_);
        this.enableScroll = enableScroll;
        this.iBackground = background;
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

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float p_97788_, int p_97789_, int p_97790_) {
        renderBackground(guiGraphics);
        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - this.imageHeight) / 2;

        guiGraphics.blit(iBackground,
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

    protected void addButtons(){

    }

    protected void sendAndTriggerLocalPacket(CraftGuideGuiPacket packet) {
        Network.INSTANCE.send(
                PacketDistributor.SERVER.noArg(),
                packet);
        menu.handleGuiPacket(packet.type, packet.key, packet.value, packet.data);
        this.handleGuiPacket(packet.type, packet.key, packet.value, packet.data);
    }


    @Override
    protected void renderTooltip(@NotNull GuiGraphics graphics, int x, int y) {
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
    }

    @Override
    public void render(GuiGraphics graphics, int p_283661_, int p_281248_, float p_281886_) {
        super.render(graphics, p_283661_, p_281248_, p_281886_);
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 2000);
        renderTooltip(graphics, p_283661_, p_281248_);
        graphics.pose().popPose();
        RenderSystem.disableDepthTest();
        renderNumberLabel(graphics);
        RenderSystem.enableDepthTest();
    }

    private void renderNumberLabel(@NotNull GuiGraphics graphics) {
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 1000);
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
                    graphics.pose().pushPose();
                    graphics.pose().scale(0.6f, 0.6f, 1);
                    graphics.drawString(this.font, text,
                            (int) ((relX + filterSlot.x + 16 - this.font.width(text) * 0.6) / 0.6f),
                            (int) ((relY + filterSlot.y + 16 - this.font.lineHeight * 0.6) / 0.6f),
                            0xffffff);
                    graphics.pose().popPose();
                }
            }
        }

        graphics.pose().popPose();
    }

    @Override
    public void accept(FilterSlot slot, ItemStack item) {
        if (!slot.isActive() || slot.readonly) return;
        slot.set(item);
        sendAndTriggerLocalPacket(new CraftGuideGuiPacket(CraftGuideGuiPacket.Type.SET_ITEM, slot.index, 0, ItemStackUtil.saveStack(item)));
    }

    @Override
    public List<FilterSlot> getSlots() {
        return this.menu.slots.stream()
                .filter(slot -> slot instanceof FilterSlot fs && !fs.readonly)
                .map(slot -> (FilterSlot) slot)
                .toList();
    }


    @Override
    public boolean mouseScrolled(double p_94686_, double p_94687_, double p_94688_) {
        if(!enableScroll) return false;
        @Nullable Slot slot = this.getSlotUnderMouse();
        if (slot instanceof FilterSlot filterSlot && filterSlot.container instanceof StepDataContainer sdc && !filterSlot.readonly) {
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
                            filterSlot.getContainerSlot(),
                            count.getValue()
                    )
            );
        }
        return super.mouseScrolled(p_94686_, p_94687_, p_94688_);
    }
    @Override
    public void handleGuiPacket(CraftGuideGuiPacket.Type type, int key, int value, @Nullable CompoundTag data) {
    }

}