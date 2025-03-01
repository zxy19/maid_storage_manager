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

@MouseTweaksDisableWheelTweak
public class ItemSelectorScreen extends AbstractContainerScreen<ItemSelectorMenu> {
    private final ResourceLocation background = new ResourceLocation(MaidStorageManager.MODID, "textures/gui/item_selector.png");

    public ItemSelectorScreen(ItemSelectorMenu p_97741_, Inventory p_97742_, Component p_97743_) {
        super(p_97741_, p_97742_, p_97743_);
        this.imageWidth = 176;
        this.imageHeight = 200;
        this.inventoryLabelY = this.imageHeight - 94;
        this.addButtons();
        this.addRepeatControl();
    }

    protected List<Component> getTooltipForResult(int slot) {
        FilterContainer filteredItems = this.getMenu().filteredItems;
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

        tooltip.add(Component.translatable("tooltip.maid_storage_manager.request_list.collected", collected, String.valueOf(requested == -1 ? "*" : requested)));

        return tooltip;
    }

    private void addButtons() {
        this.addRenderableWidget(new ButtonWidget(
                135, 60, 16, 16,
                background,
                (widget) -> {
                    if (this.getMenu().matchTag) {
                        return new Pair<>(208, widget.isHovered() ? 16 : 0);
                    } else {
                        return new Pair<>(224, widget.isHovered() ? 16 : 0);
                    }
                },
                () -> this.getMenu().matchTag ?
                        Component.translatable("gui.maid_storage_manager.request_list.match_tag_on") :
                        Component.translatable("gui.maid_storage_manager.request_list.match_tag_off"),
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
                134, 81, 18, 18,
                background,
                (widget) -> new Pair<>(176, widget.isHovered() ? 50 : 32),
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
        this.addRenderableWidget(new AbstractWidget(
                128,
                20,
                32,
                28,
                Component.translatable("gui.maid_storage_manager.request_list.repeat")
        ) {
            @Override
            public int getX() {
                return getGuiLeft() + super.getX();
            }

            @Override
            public int getY() {
                return getGuiTop() + super.getY();
            }

            @Override
            protected void renderWidget(GuiGraphics graphics, int p_268034_, int p_268009_, float p_268085_) {
                graphics.drawString(Minecraft.getInstance().font,
                        Component.translatable("gui.maid_storage_manager.request_list.repeat"),
                        this.getX() + 1,
                        this.getY() + 1,
                        0xFFFFFF,
                        false
                );
                MutableComponent repeatDesc = Component.translatable("gui.maid_storage_manager.request_list.never");
                if (getMenu().repeat != -1) {
                    repeatDesc = Component.translatable("gui.maid_storage_manager.request_list.repeat_desc", String.valueOf(getMenu().repeat));
                }
                graphics.drawString(Minecraft.getInstance().font,
                        repeatDesc,
                        this.getX() + 1,
                        this.getY() + 12,
                        0x2e7d32,
                        false
                );
            }

            @Override
            public boolean mouseScrolled(double p_94734_, double p_94735_, double p_94736_) {
                int dv = (int) (Math.abs(p_94736_) / p_94736_);
                if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), InputConstants.KEY_LSHIFT))
                    dv *= 10;
                if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), InputConstants.KEY_LCONTROL))
                    dv *= 10;
                getMenu().repeat = Math.max(-1, Math.min(getMenu().repeat + dv, 20 * 3600));
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
                    graphics.pose().pushPose();
                    graphics.pose().scale(0.6f, 0.6f, 1);
                    graphics.drawString(this.font, text,
                            (int) ((relX + filterSlot.x + 16 - this.font.width(text) * 0.6) / 0.6f),
                            (int) ((relY + filterSlot.y + 16 - this.font.lineHeight * 0.6) / 0.6f),
                            0xffffff);

                    String collectedStr = String.valueOf(collected.getValue());
                    graphics.drawString(this.font, collectedStr,
                            (int) ((relX + filterSlot.x + 30 - this.font.width(collectedStr) * 0.6) / 0.6),
                            (int) ((relY + filterSlot.y + 16 - this.font.lineHeight * 0.6) / 0.6),
                            0xffffff);
                    graphics.pose().popPose();

                    if (done.getValue() != 0) {
                        if (collected.getValue() < count.getValue() && count.getValue() != -1) {
                            graphics.blit(background,
                                    relX + filterSlot.x + 20,
                                    relY + filterSlot.y + 4,
                                    0,
                                    180,
                                    86,
                                    10, 10,
                                    256, 256
                            );
                        } else {
                            graphics.blit(background,
                                    relX + filterSlot.x + 20,
                                    relY + filterSlot.y + 4,
                                    0,
                                    180,
                                    72,
                                    10, 10,
                                    256, 256
                            );
                        }
                    }
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
            MutableInt count = this.getMenu().filteredItems.count[filterSlot.getContainerSlot()];
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
                    filterSlot.getContainerSlot(),
                    count.getValue()
            );
        }
        return super.mouseScrolled(p_94686_, p_94687_, p_94688_);
    }
}
