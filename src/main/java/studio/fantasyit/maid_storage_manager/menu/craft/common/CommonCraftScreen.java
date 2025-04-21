package studio.fantasyit.maid_storage_manager.menu.craft.common;

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
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.craft.CraftManager;
import studio.fantasyit.maid_storage_manager.craft.action.CraftAction;
import studio.fantasyit.maid_storage_manager.menu.AbstractFilterScreen;
import studio.fantasyit.maid_storage_manager.menu.base.ImageAsset;
import studio.fantasyit.maid_storage_manager.menu.container.FilterSlot;
import studio.fantasyit.maid_storage_manager.menu.container.SelectButtonWidget;
import studio.fantasyit.maid_storage_manager.menu.craft.base.ICraftGuiPacketReceiver;
import studio.fantasyit.maid_storage_manager.network.CraftGuideGuiPacket;
import studio.fantasyit.maid_storage_manager.network.Network;
import yalter.mousetweaks.api.MouseTweaksDisableWheelTweak;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@MouseTweaksDisableWheelTweak
@IPNIgnore
public class CommonCraftScreen extends AbstractFilterScreen<CommonCraftMenu> implements ICraftGuiPacketReceiver {
    protected enum BUTTON_TYPE_COMMON {
        ACTION,
        SORT_UP,
        SORT_REMOVE,
        SORT_DOWN,
        OPTIONAL
    }

    private final ResourceLocation background = new ResourceLocation(MaidStorageManager.MODID, "textures/gui/craft/type/common.png");

    public final List<HashMap<BUTTON_TYPE_COMMON, SelectButtonWidget<?>>> buttonsByRow = new ArrayList<>();
    public final List<List<Integer>> buttonYOffset = new ArrayList<>();

    SelectButtonWidget<?> pageUpBtn;
    SelectButtonWidget<?> pageDownBtn;

    public CommonCraftScreen(CommonCraftMenu p_97741_, Inventory p_97742_, Component p_97743_) {
        super(p_97741_, p_97742_, p_97743_);
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

    private static final int[] SLOT_Y = new int[]{28, 71, 114};

    private void addButtons() {
        buttonsByRow.clear();
        buttonYOffset.clear();
        //TODO:目前配方树计算需要使用准确物品进行匹配，暂时不支持模糊物品作为配方拓扑的节点。
        // 所以忽略NBT功能暂时不能实现。也许节点中的ItemStack应该被更换?

        for (int i = 0; i < this.menu.steps.size(); i++) {
            HashMap<BUTTON_TYPE_COMMON, SelectButtonWidget<?>> objects = new HashMap<>();
            ArrayList<Integer> yOffsets = new ArrayList<>();
            addActionButton(objects, yOffsets, SLOT_Y[i % 3] - 5, i);
            addSortButtons(objects, yOffsets, SLOT_Y[i % 3] - 5, i);
            addOptionButtons(objects, yOffsets, SLOT_Y[i % 3] - 5, i);
            buttonsByRow.add(objects);
            buttonYOffset.add(yOffsets);
        }
        addPageButton();
        updateButtons();
    }

    private void addActionButton(Map<BUTTON_TYPE_COMMON, SelectButtonWidget<?>> buttons, List<Integer> yOffsets, int sy, int i) {
        buttons.put(BUTTON_TYPE_COMMON.ACTION, addRenderableWidget(new SelectButtonWidget<CraftAction>(94, SLOT_Y[i % 3], (value) -> {
            if (value == null) value = menu.craftGuideData.steps.get(i).actionType;
            else {
                value = CraftManager.getInstance().getNextAction(value);
                CompoundTag data = new CompoundTag();
                data.putString("ns", value.type().getNamespace());
                data.putString("id", value.type().getPath());
                sendAndTriggerLocalPacket(new CraftGuideGuiPacket(CraftGuideGuiPacket.Type.SET_MODE, i, data));
            }
            return new SelectButtonWidget.Option<>(
                    value,
                    CommonCraftAssets.BUTTON_ACTION,
                    CommonCraftAssets.BUTTON_ACTION_HOVER,
                    CommonCraftAssets.translationForAction(value.type())
            );
        }, this)));
        yOffsets.add(0);
    }

    private void addOptionButtons(Map<BUTTON_TYPE_COMMON, SelectButtonWidget<?>> buttons, List<Integer> yOffsets, int sy, int i) {
        yOffsets.add(5);
        buttons.put(BUTTON_TYPE_COMMON.OPTIONAL, addRenderableWidget(new SelectButtonWidget<Boolean>(137, sy + 5, (value) -> {
            if (value == null)
                value = !menu.steps.get(i).optional;
            else {
                value = !value;
                sendAndTriggerLocalPacket(new CraftGuideGuiPacket(CraftGuideGuiPacket.Type.OPTIONAL, i, value ? 1 : 0));
            }
            return new SelectButtonWidget.Option<>(
                    value,
                    value ? CommonCraftAssets.BUTTON_OPTIONAL_POSI : CommonCraftAssets.BUTTON_OPTIONAL_NEGI,
                    value ? CommonCraftAssets.BUTTON_OPTIONAL_POSI_HOVER : CommonCraftAssets.BUTTON_OPTIONAL_NEGI_HOVER,
                    value ? Component.translatable("gui.maid_storage_manager.craft_guide.common.optional")
                            : Component.translatable("gui.maid_storage_manager.craft_guide.common.required")
            );

        }, this)));
    }

    private void addSortButtons(Map<BUTTON_TYPE_COMMON, SelectButtonWidget<?>> buttons, List<Integer> yOffsets, int sy, int i) {
        buttons.put(BUTTON_TYPE_COMMON.SORT_UP, addRenderableWidget(new SelectButtonWidget<Integer>(23, sy, (value) -> {
            if (value != null)
                sendAndTriggerLocalPacket(new CraftGuideGuiPacket(CraftGuideGuiPacket.Type.UP, i));
            return new SelectButtonWidget.Option<>(
                    1,
                    CommonCraftAssets.BUTTON_UP,
                    CommonCraftAssets.BUTTON_UP_HOVER,
                    Component.translatable("gui.maid_storage_manager.craft_guide.common.up"));
        }, this)));
        yOffsets.add(-5);
        buttons.put(BUTTON_TYPE_COMMON.SORT_REMOVE, addRenderableWidget(new SelectButtonWidget<Integer>(23, sy + 10, (value) -> {
            if (value != null)
                sendAndTriggerLocalPacket(new CraftGuideGuiPacket(CraftGuideGuiPacket.Type.REMOVE, i));
            return new SelectButtonWidget.Option<>(
                    1,
                    CommonCraftAssets.BUTTON_REMOVE,
                    CommonCraftAssets.BUTTON_REMOVE_HOVER,
                    Component.translatable("gui.maid_storage_manager.craft_guide.common.remove"));
        }, this)));
        yOffsets.add(5);

        buttons.put(BUTTON_TYPE_COMMON.SORT_DOWN, addRenderableWidget(new SelectButtonWidget<Integer>(23, sy + 23, (value) -> {
            if (value != null)
                sendAndTriggerLocalPacket(new CraftGuideGuiPacket(CraftGuideGuiPacket.Type.DOWN, i));

            return new SelectButtonWidget.Option<>(
                    1,
                    CommonCraftAssets.BUTTON_DOWN,
                    CommonCraftAssets.BUTTON_DOWN_HOVER,
                    Component.translatable("gui.maid_storage_manager.craft_guide.common.down"));
        }, this)));
        yOffsets.add(15);
    }

    private void addPageButton() {
        pageUpBtn = addRenderableWidget(new SelectButtonWidget<Integer>(122, 151, (value) -> {
            if (value != null)
                sendAndTriggerLocalPacket(new CraftGuideGuiPacket(CraftGuideGuiPacket.Type.PAGE_UP, value));
            return new SelectButtonWidget.Option<>(
                    1,
                    CommonCraftAssets.BUTTON_PREV_PAGE,
                    CommonCraftAssets.BUTTON_PREV_PAGE_HOVER,
                    Component.translatable("gui.maid_storage_manager.craft_guide.common.page_up"));
        }, this));

        pageDownBtn = addRenderableWidget(new SelectButtonWidget<Integer>(140, 151, (value) -> {
            if (value != null)
                sendAndTriggerLocalPacket(new CraftGuideGuiPacket(CraftGuideGuiPacket.Type.PAGE_DOWN, value));
            return new SelectButtonWidget.Option<>(
                    1,
                    CommonCraftAssets.BUTTON_NEXT_PAGE,
                    CommonCraftAssets.BUTTON_NEXT_PAGE_HOVER,
                    Component.translatable("gui.maid_storage_manager.craft_guide.common.page_down"));
        }, this));
    }

    private void sendAndTriggerLocalPacket(CraftGuideGuiPacket packet) {
        Network.INSTANCE.send(
                PacketDistributor.SERVER.noArg(),
                packet);
        menu.handleGuiPacket(packet.type, packet.key, packet.value, packet.data);
        this.handleGuiPacket(packet.type, packet.key, packet.value, packet.data);
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

        for (Slot slot : this.getMenu().slots) {
            if (slot instanceof FilterSlot filterSlot && filterSlot.container instanceof CommonStepDataContainer container && filterSlot.isActive()) {
                ImageAsset back = switch (filterSlot.getContainerSlot()) {
                    case 0 -> CommonCraftAssets.SLOT_L;
                    case 1 -> container.getContainerSize() == 2 ? CommonCraftAssets.SLOT_M : CommonCraftAssets.SLOT_R;
                    case 2 -> CommonCraftAssets.SLOT_R;
                    default -> null;
                };
                if (back != null)
                    back.blit(guiGraphics, relX + slot.x - 1, relY + slot.y - 1);
            }
        }

        renderSeparator(guiGraphics);
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
        renderButtonIcon(graphics);
        renderBlockIndicator(graphics);
        RenderSystem.enableDepthTest();
    }

    private void renderNumberLabel(@NotNull GuiGraphics graphics) {
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 1000);
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

    @SuppressWarnings("unchecked")
    private void renderButtonIcon(@NotNull GuiGraphics graphics) {
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 1100);

        for (int i = 0; i < buttonsByRow.size(); i++) {
            if (menu.isIdCurrentPage(i)) {
                SelectButtonWidget<CraftAction> cab = (SelectButtonWidget<CraftAction>) buttonsByRow.get(i).get(BUTTON_TYPE_COMMON.ACTION);
                CommonCraftAssets
                        .imageForAction(cab.getData().type())
                        .blit(graphics,
                                cab.getX() + 2,
                                cab.getY() + 2
                        );
            }
        }

        graphics.pose().popPose();
    }

    private void renderBlockIndicator(@NotNull GuiGraphics graphics) {
        graphics.pose().pushPose();
        float scale = 1.3f;
        graphics.pose().scale(scale, scale, 1);

        for (int i = 0; i < menu.targetBlockSlots.size(); i++) {
            if (menu.isIdCurrentPage(i)) {
                graphics.renderItem(
                        menu.targetBlockSlots.get(i).getItem(),
                        (int) ((this.leftPos + menu.targetBlockSlots.get(i).x + 1) / scale),
                        (int) ((this.topPos + menu.targetBlockSlots.get(i).y - (scale - 1) * 18 / 2) / scale)
                );
            }
        }

        graphics.pose().popPose();
    }

    private void renderSeparator(GuiGraphics graphics) {
        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - this.imageHeight) / 2;
        int lineCount = menu.steps.size() - menu.page * 3;
        for (int i = 0; i < lineCount && i < 2; i++) {
            CommonCraftAssets.SEPARATOR.blit(
                    graphics,
                    relX + 24,
                    relY + SLOT_Y[i] + 28
            );
        }
    }

    @Override
    public boolean mouseScrolled(double p_94686_, double p_94687_, double p_94688_) {
        @Nullable Slot slot = this.getSlotUnderMouse();
        if (slot instanceof FilterSlot filterSlot && filterSlot.container instanceof CommonStepDataContainer sdc) {
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
                            filterSlot.index,
                            count.getValue()
                    )
            );
        }
        return super.mouseScrolled(p_94686_, p_94687_, p_94688_);
    }

    @Override
    public void accept(FilterSlot slot, ItemStack item) {
        if (slot instanceof CommonCraftMenu.NoPlaceFilterSlot) return;
        if (!slot.isActive()) return;
        slot.set(item);
        sendAndTriggerLocalPacket(new CraftGuideGuiPacket(CraftGuideGuiPacket.Type.SET_ITEM, slot.index, 0, item.save(new CompoundTag())));
    }

    @Override
    public List<FilterSlot> getSlots() {
        return this.menu.slots.stream()
                .filter(slot -> slot instanceof FilterSlot && !(slot instanceof CommonCraftMenu.NoPlaceFilterSlot) && slot.isActive())
                .map(slot -> (FilterSlot) slot)
                .toList();
    }

    @Override
    public void handleGuiPacket(CraftGuideGuiPacket.Type type, int key, int value, @Nullable CompoundTag data) {
        switch (type) {
            case REMOVE -> {
                buttonsByRow.get(buttonsByRow.size() - 1).values().forEach(button -> button.setVisible(false));
                buttonsByRow.remove(buttonsByRow.size() - 1);
            }
        }
        updateButtons();
    }

    private void updateButtons() {
        for (HashMap<BUTTON_TYPE_COMMON, SelectButtonWidget<?>> buttons : buttonsByRow) {
            buttons.get(BUTTON_TYPE_COMMON.ACTION).setOption(null);
            buttons.get(BUTTON_TYPE_COMMON.OPTIONAL).setOption(null);
        }
        for (int i = 0; i < menu.steps.size(); i++) {
            HashMap<BUTTON_TYPE_COMMON, SelectButtonWidget<?>> buttons = buttonsByRow.get(i);
            int finalI = i;
            buttons.values().forEach(button -> button.setVisible(menu.isIdCurrentPage(finalI)));
        }
        pageUpBtn.setVisible(menu.page > 0);
        pageDownBtn.setVisible(menu.page < (menu.steps.size() + 2) / 3 - 1);
    }
}