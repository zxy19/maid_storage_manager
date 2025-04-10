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
import studio.fantasyit.maid_storage_manager.menu.AbstractFilterScreen;
import studio.fantasyit.maid_storage_manager.menu.base.ImageAsset;
import studio.fantasyit.maid_storage_manager.menu.container.ButtonWidget;
import studio.fantasyit.maid_storage_manager.menu.container.FilterSlot;
import studio.fantasyit.maid_storage_manager.menu.container.SelectButtonWidget;
import studio.fantasyit.maid_storage_manager.menu.craft.base.StepDataContainer;
import studio.fantasyit.maid_storage_manager.network.CraftGuideGuiPacket;
import studio.fantasyit.maid_storage_manager.network.Network;
import yalter.mousetweaks.api.MouseTweaksDisableWheelTweak;

import java.util.ArrayList;
import java.util.List;

@MouseTweaksDisableWheelTweak
@IPNIgnore
public class CommonCraftScreen extends AbstractFilterScreen<CommonCraftMenu> {
    private final ResourceLocation background = new ResourceLocation(MaidStorageManager.MODID, "textures/gui/craft/common.png");

    public final List<List<SelectButtonWidget<?>>> buttonsByRow = new ArrayList<>();

    public CommonCraftScreen(CommonCraftMenu p_97741_, Inventory p_97742_, Component p_97743_) {
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

    private static final int[] SLOT_Y = new int[]{28, 71, 114};

    private void addButtons() {
        //TODO:目前配方树计算需要使用准确物品进行匹配，暂时不支持模糊物品作为配方拓扑的节点。
        // 所以忽略NBT功能暂时不能实现。也许节点中的ItemStack应该被更换?
//        addTagBtn(this.menu.inputSlot1, 75, 18, 1);
//        addTagBtn(this.menu.inputSlot2, 75, 126, 2);
//        addTagBtn(this.menu.outputSlot, 104, 113, 3);

        for (int i = 0; i < this.menu.steps.size(); i++) {
            int finalI = i;
            ArrayList<SelectButtonWidget<?>> objects = new ArrayList<>();
            objects.add(addWidget(new SelectButtonWidget<Integer>(131, SLOT_Y[i % 3], (value) -> {
                if (value == null) value = -1;
                List<CraftManager.CraftAction> commons = CraftManager.getInstance().getCommonActions();
                CraftManager.CraftAction next = commons.get((value + 1) % commons.size());
                CompoundTag data = new CompoundTag();
                data.putString("ns", next.type().getNamespace());
                data.putString("id", next.type().getPath());
                Network.INSTANCE.send(
                        PacketDistributor.SERVER.noArg(),
                        new CraftGuideGuiPacket(CraftGuideGuiPacket.Type.SET_MODE, finalI, data));
                return new SelectButtonWidget.Option<>(
                        (value + 1) % commons.size(),
                        CommonCraftAssets.BUTTON_ACTION,
                        CommonCraftAssets.BUTTON_ACTION_HOVER,
                        CommonCraftAssets.translationForAction(next.type())
                );
            }, this)));
            addSortButtons(objects, SLOT_Y[i % 3] - 5, i);
            buttonsByRow.add(objects);
        }
    }

    private void addSortButtons(List<SelectButtonWidget<?>> buttons, int sy, int i) {
        buttons.add(addWidget(new SelectButtonWidget<Integer>(23, sy, (value) -> {
            Network.INSTANCE.send(
                    PacketDistributor.SERVER.noArg(),
                    new CraftGuideGuiPacket(CraftGuideGuiPacket.Type.UP, i));
            return new SelectButtonWidget.Option<>(
                    1,
                    CommonCraftAssets.BUTTON_UP,
                    CommonCraftAssets.BUTTON_UP_HOVER,
                    Component.translatable("gui.maid_storage_manager.craft_guide.common.up"));
        }, this)));
        buttons.add(addWidget(new SelectButtonWidget<Integer>(23, sy + 10, (value) -> {
            Network.INSTANCE.send(
                    PacketDistributor.SERVER.noArg(),
                    new CraftGuideGuiPacket(CraftGuideGuiPacket.Type.REMOVE, i));
            return new SelectButtonWidget.Option<>(
                    1,
                    CommonCraftAssets.BUTTON_REMOVE,
                    CommonCraftAssets.BUTTON_REMOVE_HOVER,
                    Component.translatable("gui.maid_storage_manager.craft_guide.common.remove"));
        }, this)));
        buttons.add(addWidget(new SelectButtonWidget<Integer>(23, sy + 20, (value) -> {
            Network.INSTANCE.send(
                    PacketDistributor.SERVER.noArg(),
                    new CraftGuideGuiPacket(CraftGuideGuiPacket.Type.DOWN, i));
            return new SelectButtonWidget.Option<>(
                    1,
                    CommonCraftAssets.BUTTON_DOWN,
                    CommonCraftAssets.BUTTON_DOWN,
                    Component.translatable("gui.maid_storage_manager.craft_guide.common.down"));
        }, this)));
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

        for (Slot slot : this.getMenu().slots) {
            if (slot instanceof FilterSlot filterSlot && filterSlot.container instanceof StepDataContainer container && filterSlot.isActive()) {
                ImageAsset back = switch (filterSlot.getContainerSlot()) {
                    case 0 -> CommonCraftAssets.SLOT_L;
                    case 1 -> container.getContainerSize() == 2 ? CommonCraftAssets.SLOT_M : CommonCraftAssets.SLOT_R;
                    case 2 -> CommonCraftAssets.SLOT_R;
                    default -> null;
                };
                if (back != null)
                    back.blit(guiGraphics, relX + slot.x, relY + slot.y);
            }
        }
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
            if (slot instanceof FilterSlot filterSlot && filterSlot.container instanceof StepDataContainer sdc) {
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
        RenderSystem.enableDepthTest();
    }


    @Override
    public boolean mouseScrolled(double p_94686_, double p_94687_, double p_94688_) {
        @Nullable Slot slot = this.getSlotUnderMouse();
        if (slot instanceof FilterSlot filterSlot && filterSlot.container instanceof StepDataContainer sdc) {
            MutableInt count = new MutableInt(sdc.getCount(filterSlot.getContainerSlot()));
            int dv = (int) (Math.abs(p_94688_) / p_94688_);
            if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), InputConstants.KEY_LSHIFT))
                dv *= 10;

            if (dv > 0) {
                if (count.addAndGet(dv) == 0) count.addAndGet(1);
            } else {
                if (count.addAndGet(dv) <= 0) count.setValue(-1);
            }
            Network.INSTANCE.sendToServer(
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
    public void accept(FilterSlot menu, ItemStack item) {
        if (menu instanceof CommonCraftMenu.NoPlaceFilterSlot) return;
        if (!menu.isActive()) return;
        menu.set(item);
    }

    @Override
    public List<FilterSlot> getSlots() {
        return this.menu.slots.stream()
                .filter(slot -> slot instanceof FilterSlot)
                .map(slot -> (FilterSlot) slot)
                .toList();
    }
}