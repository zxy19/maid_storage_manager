package studio.fantasyit.maid_storage_manager.menu;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.menu.container.ButtonWidget;
import studio.fantasyit.maid_storage_manager.menu.container.FilterContainer;
import studio.fantasyit.maid_storage_manager.menu.container.FilterSlot;
import studio.fantasyit.maid_storage_manager.network.ItemSelectorGuiPacket;
import studio.fantasyit.maid_storage_manager.network.Network;
import yalter.mousetweaks.api.MouseTweaksDisableWheelTweak;

import java.util.List;
import java.util.Optional;

import static studio.fantasyit.maid_storage_manager.network.Network.sendItemSelectorSetItemPacket;

@MouseTweaksDisableWheelTweak
public class CraftGuideScreen extends AbstractFilterScreen<CraftGuideMenu> {
    private final ResourceLocation background = new ResourceLocation(MaidStorageManager.MODID, "textures/gui/craft_guide.png");

    public CraftGuideScreen(CraftGuideMenu p_97741_, Inventory p_97742_, Component p_97743_) {
        super(p_97741_, p_97742_, p_97743_);
        this.imageWidth = 176;
        this.imageHeight = 239;
        this.inventoryLabelY = this.imageHeight - 94;
        this.inventoryLabelX = 136;
    }

    @Override
    protected void init() {
        super.init();
        this.addButtons();
    }

    private void addTagBtn(CraftGuideMenu.TargetOps ops, int x, int y, int id) {
        this.addRenderableWidget(new ButtonWidget(
                x, y, 16, 16,
                background,
                (widget) -> {
                    if (ops.matchTag) {
                        return new Pair<>(176, widget.isHovered() ? 16 : 0);
                    } else {
                        return new Pair<>(192, widget.isHovered() ? 16 : 0);
                    }
                },
                () -> ops.matchTag ?
                        Component.translatable("gui.maid_storage_manager.request_list.match_tag_on") :
                        Component.translatable("gui.maid_storage_manager.request_list.match_tag_off"),
                () -> {
                    ops.matchTag = !ops.matchTag;
                    Network.sendItemSelectorGuiPacket(
                            ItemSelectorGuiPacket.SlotType.MATCH_TAG,
                            id,
                            ops.matchTag ? 1 : 0
                    );
                },
                this
        ));
    }

    private void addButtons() {
        //TODO:目前配方树计算需要使用准确物品进行匹配，暂时不支持模糊物品作为配方拓扑的节点。
        // 所以忽略NBT功能暂时不能实现。也许节点中的ItemStack应该被更换?
//        addTagBtn(this.menu.inputSlot1, 75, 18, 1);
//        addTagBtn(this.menu.inputSlot2, 75, 126, 2);
//        addTagBtn(this.menu.outputSlot, 104, 113, 3);
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
    }

    @Override
    protected void renderTooltip(@NotNull GuiGraphics graphics, int x, int y) {
        if (this.menu.getCarried().isEmpty()) {
            int inGuiX = x - this.getGuiLeft();
            int inGuiY = y - this.getGuiTop();
            for (Slot slot : this.getMenu().slots) {
                if (slot.x <= inGuiX && slot.x + 30 >= inGuiX && slot.y <= inGuiY && slot.y + 16 >= inGuiY) {
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
    public void render(GuiGraphics graphics, int p_283661_, int p_281248_, float p_281886_) {
        super.render(graphics, p_283661_, p_281248_, p_281886_);
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 2000);
        renderTooltip(graphics, p_283661_, p_281248_);
        graphics.pose().popPose();
        RenderSystem.disableDepthTest();
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 1000);
        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - this.imageHeight) / 2;
        for (Slot slot : this.getMenu().slots) {
            FilterContainer filters = this.getMenu().filters.get(slot.index);
            Integer iid = this.getMenu().iid.get(slot.index);
            if (filters == null) continue;
            if (slot instanceof FilterSlot filterSlot) {
                if (filterSlot.hasItem()) {
                    MutableInt count = filters.count[iid];
                    String text = String.valueOf(count.getValue());
                    if (count.getValue() == -1) {
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
        RenderSystem.enableDepthTest();
    }


    @Override
    public boolean mouseScrolled(double p_94686_, double p_94687_, double p_94688_) {
        @Nullable Slot slot = this.getSlotUnderMouse();
        if (slot instanceof FilterSlot filterSlot) {
            FilterContainer filters = this.getMenu().filters.get(filterSlot.index);
            Integer iid = this.getMenu().iid.get(filterSlot.index);
            if (filters == null) return super.mouseScrolled(p_94686_, p_94687_, p_94688_);
            MutableInt count = filters.count[iid];
            int dv = (int) (Math.abs(p_94688_) / p_94688_);
            if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), InputConstants.KEY_LSHIFT))
                dv *= 10;

            if (dv > 0) {
                if (count.addAndGet(dv) == 0) count.addAndGet(1);
            } else {
                if (count.addAndGet(dv) <= 0) count.setValue(-1);
            }
            Network.sendItemSelectorGuiPacket(
                    ItemSelectorGuiPacket.SlotType.COUNT,
                    filterSlot.index,
                    count.getValue()
            );
        }
        return super.mouseScrolled(p_94686_, p_94687_, p_94688_);
    }

    @Override
    public void accept(FilterSlot menu, ItemStack item) {
        if (menu instanceof CraftGuideMenu.NoPlaceFilterSlot) return;
        if (!menu.isActive()) return;
        ItemStack itemStack = item.copyWithCount(1);
        FilterContainer filter = this.menu.filters.get(menu.index);
        filter.setItem(menu.getContainerSlot(), itemStack);
        sendItemSelectorSetItemPacket(menu.index, itemStack);
    }

    @Override
    public List<FilterSlot> getSlots() {
        return this.menu.slots.stream()
                .filter(slot -> slot instanceof FilterSlot)
                .map(slot -> (FilterSlot) slot)
                .toList();
    }
}
