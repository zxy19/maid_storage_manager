package studio.fantasyit.maid_storage_manager.maid.config;

import com.github.tartaricacid.touhoulittlemaid.client.gui.entity.maid.task.MaidTaskConfigGui;
import com.github.tartaricacid.touhoulittlemaid.inventory.container.task.TaskConfigContainer;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import org.apache.commons.lang3.mutable.MutableObject;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.maid.data.StorageManagerConfigData;
import studio.fantasyit.maid_storage_manager.menu.base.ImageAsset;
import studio.fantasyit.maid_storage_manager.network.MaidDataSyncPacket;
import studio.fantasyit.maid_storage_manager.network.Network;
import studio.fantasyit.maid_storage_manager.registry.GuiRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class StorageManagerMaidConfigGui extends MaidTaskConfigGui<StorageManagerMaidConfigGui.Container> {
    private StorageManagerConfigData.Data currentMAData;
    private OptionRow configButton;
    float scrollOffset = 0;
    Float startDragScrollOffset = 0F;
    Float startDragScreenOffset = 0F;

    record OptionRow(Component label, MutableObject<Component> value, Consumer<MutableObject<Component>> left,
                     Consumer<MutableObject<Component>> right) {
        public OptionRow(Component label, Component value, Consumer<MutableObject<Component>> left,
                         Consumer<MutableObject<Component>> right) {
            this(label, new MutableObject<>(value), left, right);
        }

        public void setValue(Component value) {
            this.value.setValue(value);
        }
    }

    private final List<OptionRow> options = new ArrayList<>();

    public StorageManagerMaidConfigGui(Container screenContainer, Inventory inv, Component titleIn) {
        super(screenContainer, inv, titleIn);
    }

    public static class Container extends TaskConfigContainer {
        public Container(int id, Inventory inventory, int entityId) {
            super(GuiRegistry.STORAGE_MANAGER_MAID_CONFIG_GUI.get(), id, inventory, entityId);
        }
    }

    @Override
    protected void initAdditionData() {
        this.currentMAData = this.maid.getOrCreateData(StorageManagerConfigData.KEY, StorageManagerConfigData.Data.getDefault());
    }

    @Override
    protected void initAdditionWidgets() {
        super.initAdditionWidgets();
        this.options.clear();
        this.options.add(new OptionRow(
                Component.translatable("gui.maid_storage_manager.config.memory_assistant"),
                Component.translatable(StorageManagerConfigData.getTranslationKey(this.currentMAData.memoryAssistant())),
                button -> {
                    int oNxt = Math.max(this.currentMAData.memoryAssistant().ordinal() - 1, 0);
                    StorageManagerConfigData.MemoryAssistant v = StorageManagerConfigData.MemoryAssistant.values()[oNxt];
                    this.currentMAData.memoryAssistant(v);
                    button.setValue(Component.translatable(StorageManagerConfigData.getTranslationKey(v)));
                    Network.sendMaidDataSync(MaidDataSyncPacket.Type.MemoryAssistant, this.maid.getId(), v.ordinal());
                },
                button -> {
                    int oNxt = Math.min(this.currentMAData.memoryAssistant().ordinal() + 1, StorageManagerConfigData.MemoryAssistant.values().length - 1);
                    StorageManagerConfigData.MemoryAssistant v = StorageManagerConfigData.MemoryAssistant.values()[oNxt];
                    this.currentMAData.memoryAssistant(v);
                    button.setValue(Component.translatable(StorageManagerConfigData.getTranslationKey(v)));
                    Network.sendMaidDataSync(MaidDataSyncPacket.Type.MemoryAssistant, this.maid.getId(), v.ordinal());
                }
        ));
        this.options.add(new OptionRow(
                Component.translatable("gui.maid_storage_manager.config.no_sort_placement"),
                Component.translatable(StorageManagerConfigData.getTranslationKey(this.currentMAData.noSortPlacement())),
                button -> {
                    this.currentMAData.noSortPlacement(false);
                    button.setValue(Component.translatable(StorageManagerConfigData.getTranslationKey(false)));
                    Network.sendMaidDataSync(MaidDataSyncPacket.Type.NoPlaceSort, this.maid.getId(), 0);
                    configButton.setValue(Component.translatable(StorageManagerConfigData.getTranslationKey(this.currentMAData.suppressStrategy())));
                },
                button -> {
                    this.currentMAData.noSortPlacement(true);
                    button.setValue(Component.translatable(StorageManagerConfigData.getTranslationKey(true)));
                    Network.sendMaidDataSync(MaidDataSyncPacket.Type.NoPlaceSort, this.maid.getId(), 1);
                    configButton.setValue(Component.translatable("gui.maid_storage_manager.config.fast_sort.disable"));
                }
        ));
        this.options.add(new OptionRow(
                Component.translatable("gui.maid_storage_manager.config.co_work"),
                Component.translatable(StorageManagerConfigData.getTranslationKey(this.currentMAData.coWorkMode())),
                button -> {
                    this.currentMAData.coWorkMode(false);
                    button.setValue(Component.translatable(StorageManagerConfigData.getTranslationKey(false)));
                    Network.sendMaidDataSync(MaidDataSyncPacket.Type.CoWork, this.maid.getId(), 0);
                },
                button -> {
                    this.currentMAData.coWorkMode(true);
                    button.setValue(Component.translatable(StorageManagerConfigData.getTranslationKey(true)));
                    Network.sendMaidDataSync(MaidDataSyncPacket.Type.CoWork, this.maid.getId(), 1);
                }
        ));

        this.options.add(configButton = new OptionRow(
                Component.translatable("gui.maid_storage_manager.config.fast_sort_mode"),
                Component.translatable(StorageManagerConfigData.getTranslationKey(this.currentMAData.suppressStrategy())),
                button -> {
                    if (this.currentMAData.noSortPlacement()) return;
                    int oNxt = Math.max(this.currentMAData.suppressStrategy().ordinal() - 1, 0);
                    StorageManagerConfigData.SuppressStrategy v = StorageManagerConfigData.SuppressStrategy.values()[oNxt];
                    this.currentMAData.suppressStrategy(v);
                    button.setValue(Component.translatable(StorageManagerConfigData.getTranslationKey(v)));
                    Network.sendMaidDataSync(MaidDataSyncPacket.Type.FastSort, this.maid.getId(), v.ordinal());
                },
                button -> {
                    if (this.currentMAData.noSortPlacement()) return;
                    int oNxt = Math.min(this.currentMAData.suppressStrategy().ordinal() + 1, StorageManagerConfigData.SuppressStrategy.values().length - 1);
                    StorageManagerConfigData.SuppressStrategy v = StorageManagerConfigData.SuppressStrategy.values()[oNxt];
                    this.currentMAData.suppressStrategy(v);
                    button.setValue(Component.translatable(StorageManagerConfigData.getTranslationKey(v)));
                    Network.sendMaidDataSync(MaidDataSyncPacket.Type.FastSort, this.maid.getId(), v.ordinal());
                }
        ));
        this.options.add(new OptionRow(
                Component.translatable("gui.maid_storage_manager.config.allow_seek_meal"),
                Component.translatable(StorageManagerConfigData.getTranslationKey(this.currentMAData.allowSeekWorkMeal())),
                button -> {
                    this.currentMAData.allowSeekWorkMeal(false);
                    button.setValue(Component.translatable(StorageManagerConfigData.getTranslationKey(false)));
                    Network.sendMaidDataSync(MaidDataSyncPacket.Type.AllowSeekWorkMeal, this.maid.getId(), 0);
                },
                button -> {
                    this.currentMAData.allowSeekWorkMeal(true);
                    button.setValue(Component.translatable(StorageManagerConfigData.getTranslationKey(true)));
                    Network.sendMaidDataSync(MaidDataSyncPacket.Type.AllowSeekWorkMeal, this.maid.getId(), 1);
                }
        ));

        this.options.add(new OptionRow(
                Component.translatable("gui.maid_storage_manager.config.memorize_craft_guide"),
                Component.translatable(StorageManagerConfigData.getTranslationKey(this.currentMAData.useMemorizedCraftGuide())),
                button -> {
                    this.currentMAData.useMemorizedCraftGuide(false);
                    button.setValue(Component.translatable(StorageManagerConfigData.getTranslationKey(false)));
                    Network.sendMaidDataSync(MaidDataSyncPacket.Type.MemorizeCraftGuide, this.maid.getId(), 0);
                },
                button -> {
                    this.currentMAData.useMemorizedCraftGuide(true);
                    button.setValue(Component.translatable(StorageManagerConfigData.getTranslationKey(true)));
                    Network.sendMaidDataSync(MaidDataSyncPacket.Type.MemorizeCraftGuide, this.maid.getId(), 1);
                }
        ));
        this.options.add(new OptionRow(
                Component.translatable("gui.maid_storage_manager.config.max_parallel"),
                Component.literal(String.valueOf(this.currentMAData.maxParallel())),
                button -> {
                    this.currentMAData.maxParallel(this.currentMAData.maxParallel() - 1);
                    button.setValue(Component.literal(String.valueOf(this.currentMAData.maxParallel())));
                    Network.sendMaidDataSync(MaidDataSyncPacket.Type.MaxParallel, this.maid.getId(), this.currentMAData.maxParallel());
                },
                button -> {
                    this.currentMAData.maxParallel(this.currentMAData.maxParallel() + 1);
                    button.setValue(Component.literal(String.valueOf(this.currentMAData.maxParallel())));
                    Network.sendMaidDataSync(MaidDataSyncPacket.Type.MaxParallel, this.maid.getId(), this.currentMAData.maxParallel());
                }
        ));

        this.options.add(new OptionRow(
                Component.translatable("gui.maid_storage_manager.config.crafting_repeat_count"),
                Component.literal(String.valueOf(this.currentMAData.maxCraftingLayerRepeatCount())),
                button -> {
                    this.currentMAData.maxCraftingLayerRepeatCount(this.currentMAData.maxCraftingLayerRepeatCount() / 2);
                    button.setValue(Component.literal(String.valueOf(this.currentMAData.maxCraftingLayerRepeatCount())));
                    Network.sendMaidDataSync(MaidDataSyncPacket.Type.CraftingRepeatCount, this.maid.getId(), this.currentMAData.maxCraftingLayerRepeatCount());
                },
                button -> {
                    this.currentMAData.maxCraftingLayerRepeatCount(this.currentMAData.maxCraftingLayerRepeatCount() * 2);
                    button.setValue(Component.literal(String.valueOf(this.currentMAData.maxCraftingLayerRepeatCount())));
                    Network.sendMaidDataSync(MaidDataSyncPacket.Type.CraftingRepeatCount, this.maid.getId(), this.currentMAData.maxCraftingLayerRepeatCount());
                }
        ));

        this.options.add(new OptionRow(
                Component.translatable("gui.maid_storage_manager.config.auto_sorting"),
                Component.translatable(StorageManagerConfigData.getTranslationKey(this.currentMAData.autoSorting())),
                button -> {
                    this.currentMAData.autoSorting(false);
                    button.setValue(Component.translatable(StorageManagerConfigData.getTranslationKey(false)));
                    Network.sendMaidDataSync(MaidDataSyncPacket.Type.AutoSorting, this.maid.getId(), 0);
                },
                button -> {
                    this.currentMAData.autoSorting(true);
                    button.setValue(Component.translatable(StorageManagerConfigData.getTranslationKey(true)));
                    Network.sendMaidDataSync(MaidDataSyncPacket.Type.AutoSorting, this.maid.getId(), 1);
                }
        ));

        this.options.add(new OptionRow(
                Component.translatable("gui.maid_storage_manager.config.item_type_limit"),
                getItemTypeLimitButtonText(),
                button -> {
                    this.currentMAData.itemTypeLimit(switch (this.currentMAData.itemTypeLimit()) {
                        case 0, -1 -> -1;
                        default -> this.currentMAData.itemTypeLimit() / 2;
                    });
                    button.setValue(getItemTypeLimitButtonText());
                    Network.sendMaidDataSync(MaidDataSyncPacket.Type.ItemTypeLimit, this.maid.getId(), this.currentMAData.itemTypeLimit());
                },
                button -> {
                    this.currentMAData.itemTypeLimit(switch (this.currentMAData.itemTypeLimit()) {
                        case 0 -> 1;
                        case -1 -> 0;
                        default -> this.currentMAData.itemTypeLimit() * 2;
                    });
                    button.setValue(getItemTypeLimitButtonText());
                    Network.sendMaidDataSync(MaidDataSyncPacket.Type.ItemTypeLimit, this.maid.getId(), this.currentMAData.itemTypeLimit());
                }
        ));
    }

    private Component getItemTypeLimitButtonText() {
        if (this.currentMAData.itemTypeLimit() == -1)
            return Component.translatable("gui.maid_storage_manager.config.item_type_limit_unlimited");
        if (this.currentMAData.itemTypeLimit() == 0)
            return Component.translatable("gui.maid_storage_manager.config.item_type_limit_keep");
        return Component.literal(String.valueOf(this.currentMAData.itemTypeLimit()));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int x, int y) {
        super.renderBg(graphics, partialTicks, x, y);
        int startLeft = leftPos + 87;
        int startTop = topPos + 36;
        graphics.enableScissor(startLeft, startTop, startLeft + ROW_BG.w + 11, startTop + SCROLL_AREA_HEIGHT);
        graphics.pose().pushPose();
        graphics.pose().translate(startLeft, startTop - scrollOffset, 0);
        renderScrollList(graphics, x, y, startLeft, (int) (startTop - scrollOffset));
        graphics.pose().popPose();
        graphics.pose().pushPose();
        graphics.pose().translate(startLeft, startTop, 0);
        BAR.blit(graphics, ROW_BG.w + 1, 1);
        BAR_BLOCK.blit(graphics, ROW_BG.w + 2, (int) getScrollBlockTop() + 1);
        graphics.pose().popPose();
        graphics.disableScissor();
    }

    private static final int SCROLL_AREA_HEIGHT = 112;
    private static final ImageAsset ROW_BG_HOVER = new ImageAsset(new ResourceLocation(MaidStorageManager.MODID, "textures/gui/maid_work_setting.png"), 1, 136, 154, 13);
    private static final ImageAsset ROW_BG = new ImageAsset(new ResourceLocation(MaidStorageManager.MODID, "textures/gui/maid_work_setting.png"), 1, 123, 154, 13);
    private static final ImageAsset BAR = ImageAsset.from4Point(new ResourceLocation(MaidStorageManager.MODID, "textures/gui/maid_work_setting.png"), 0, 0, 8, 111);
    private static final ImageAsset BAR_BLOCK = ImageAsset.from4Point(new ResourceLocation(MaidStorageManager.MODID, "textures/gui/maid_work_setting.png"), 0, 112, 6, 120);
    private static final ImageAsset BAR_BLOCK_ACTIVE = ImageAsset.from4Point(new ResourceLocation(MaidStorageManager.MODID, "textures/gui/maid_work_setting.png"), 7, 112, 12, 120);

    int lastScrolling = -1;
    long startMills = -1;

    private void renderScrollList(GuiGraphics graphics, int x, int y, int baseX, int baseY) {
        int hoverId = getMouseOverRowId(x, y);
        for (int i = 0; i < options.size(); i++) {
            (hoverId == i ? ROW_BG_HOVER : ROW_BG).blit(graphics, 0, i * 13);
            if (hoverId == i && lastScrolling != i) {
                lastScrolling = i;
                startMills = Util.getMillis();
            }
            drawScrollingStringWithoutShadow(graphics, font, options.get(i).label(), 5, i * 13 + 3, 0x444444, 103, baseX, baseY, hoverId == i);
            drawCenteredStringWithoutShadow(graphics, font, options.get(i).value().getValue(), 134, i * 13 + 3, ChatFormatting.GREEN.getColor(), 40);
        }
    }

    private boolean isInScrollArea(double mouseX, double mouseY) {
        return mouseX >= leftPos + 87 && mouseX <= leftPos + 87 + ROW_BG.w + 10
                && mouseY >= topPos + 36 && mouseY <= topPos + 36 + SCROLL_AREA_HEIGHT;
    }

    private int getMouseOverRowId(double mouseX, double mouseY) {
        if (isInScrollArea(mouseX, mouseY)) {
            return (int) (mouseY - topPos - 36 + scrollOffset) / ROW_BG.h;
        }
        return -1;
    }

    public void drawCenteredStringWithoutShadow(GuiGraphics graphics, Font pFont, Component pText, int pX, int pY, int pColor, int maxWidth) {
        FormattedCharSequence formattedcharsequence = pText.getVisualOrderText();
        int textWidth = pFont.width(formattedcharsequence);
        int drawWidth = Math.max(textWidth, maxWidth);
        int alignWidth = Math.min(maxWidth, textWidth);
        float scale = (float) maxWidth / drawWidth;
        graphics.pose().pushPose();
        graphics.pose().scale(scale, scale, scale);
        graphics.drawString(pFont, formattedcharsequence, (pX - (float) alignWidth / 2) / scale, (pY - 3 + (14 - 8 * scale) / 2) / scale, pColor, false);
        graphics.pose().popPose();
    }

    public void drawScrollingStringWithoutShadow(GuiGraphics graphics, Font pFont, Component pText, int pX, int pY, int pColor, int maxWidth, int baseX, int baseY, boolean enableScroll) {
        int i = pFont.width(pText);
        int maxY = pY + font.lineHeight;
        int maxX = pX + maxWidth;
        int j = (pY + maxY - 9) / 2 + 1;
        int k = maxX - pX;
        graphics.enableScissor(pX + baseX, pY + baseY, maxX + baseX, maxY + baseY);
        if (i > k && enableScroll) {
            int l = i - k;
            double d0 = (double) (Util.getMillis() - startMills) / 600.0D;
            double d1 = Math.max((double) l * 0.5D, 3.0D);
            double d2 = Math.sin((Math.PI / 2D) * Math.cos((Math.PI * 2D) * d0 / d1) - Math.PI) / 2.0D + 0.5D;
            double d3 = Mth.lerp(d2, 0.0D, (double) l);
            graphics.drawString(pFont, pText, pX - (int) d3, pY, pColor, false);
        } else {
            graphics.drawString(pFont, pText, pX, pY, pColor, false);
        }
        graphics.disableScissor();
    }

    @Override
    public boolean mouseScrolled(double p_94686_, double p_94687_, double p_94688_) {
        if (isInScrollArea(p_94686_, p_94687_)) {
            scroll(-3 * p_94688_);
            return true;
        }
        return super.mouseScrolled(p_94686_, p_94687_, p_94688_);
    }

    private double getS2SFactor() {
        return (double) (options.size() * ROW_BG.h - SCROLL_AREA_HEIGHT) / (BAR.h - BAR_BLOCK.h - 4);
    }

    public double getScrollBlockTop() {
        return scrollOffset / getS2SFactor() + 1;
    }

    public boolean isOnScrollBlock(double mouseX, double mouseY) {
        if (!isInScrollArea(mouseX, mouseY))
            return false;
        return mouseX - leftPos - 87 > ROW_BG.w && mouseY - topPos - 36 > getScrollBlockTop() && mouseY - topPos - 36 < getScrollBlockTop() + BAR_BLOCK.h;
    }

    public void scroll(double delta) {
        scrollOffset += (float) delta;
        if (scrollOffset > options.size() * ROW_BG.h - SCROLL_AREA_HEIGHT)
            scrollOffset = options.size() * ROW_BG.h - SCROLL_AREA_HEIGHT;
        if (scrollOffset < 0)
            scrollOffset = 0;
    }

    @Override
    public boolean mouseClicked(double p_97748_, double p_97749_, int p_97750_) {
        if (isInScrollArea(p_97748_, p_97749_)) {
            if (isOnScrollBlock(p_97748_, p_97749_)) {
                startDragScrollOffset = scrollOffset;
                startDragScreenOffset = (float) p_97749_;
                return true;
            }
            int rowId = getMouseOverRowId(p_97748_, p_97749_);
            int dx = (int) (p_97748_ - leftPos - 87);
            if (rowId >= 0 && rowId < options.size()) {
                OptionRow option = options.get(rowId);
                if (dx > 109 && dx < 122)
                    option.left.accept(option.value);
                else if (dx > 144 && dx < 155)
                    option.right.accept(option.value);
            }
            return true;
        }
        return super.mouseClicked(p_97748_, p_97749_, p_97750_);
    }

    @Override
    public boolean mouseDragged(double p_97752_, double p_97753_, int p_97754_, double p_97755_, double p_97756_) {
        if (startDragScreenOffset != null && startDragScrollOffset != null) {
            scrollOffset = (float) (startDragScrollOffset + (float) (p_97753_ - startDragScreenOffset) * getS2SFactor());
            scroll(0);
        }
        return super.mouseDragged(p_97752_, p_97753_, p_97754_, p_97755_, p_97756_);
    }

    @Override
    public boolean mouseReleased(double p_97812_, double p_97813_, int p_97814_) {
        startDragScreenOffset = null;
        startDragScrollOffset = null;
        return super.mouseReleased(p_97812_, p_97813_, p_97814_);
    }
}
