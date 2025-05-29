package studio.fantasyit.maid_storage_manager.menu;

import me.towdium.jecharacters.utils.Match;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.fml.ModList;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.data.InventoryItem;
import studio.fantasyit.maid_storage_manager.data.InventoryListDataClient;
import studio.fantasyit.maid_storage_manager.storage.Target;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class InventoryListScreen extends Screen {
    private final Button prevButton;
    private final Button nextButton;
    private final InventoryListDataClient data;
    private final UUID uuid;
    protected int left = 0;
    protected int top = 0;
    protected int width = 0;
    protected int height = 0;

    protected int gridSize = 0;
    private int gridStart = 0;
    private int columns;
    private int rows;
    private List<InventoryItem> originalList;
    private List<InventoryItem> list;
    private String search;
    private EditBox searchBox;

    public InventoryListScreen(UUID uuid) {
        super(Component.empty());
        this.uuid = uuid;
        this.prevButton = Button.builder(Component.translatable("gui.maid_storage_manager.written_inventory_list.previous"), (button) -> this.doPrev())
                .size(16, 16)
                .build();
        this.nextButton = Button.builder(Component.translatable("gui.maid_storage_manager.written_inventory_list.next"), (button) -> this.doNext())
                .size(16, 16)
                .build();
        InventoryListDataClient.clearShowingInv();
        this.data = InventoryListDataClient.getInstance();
        this.originalList = data.get(uuid);
        this.list = originalList;
    }

    private void doPrev() {
        if (gridStart == 0)
            return;
        gridStart -= gridSize;
    }

    private void doNext() {
        if (data.get(uuid).size() <= gridStart + gridSize)
            return;
        gridStart += gridSize;
    }


    protected List<Component> getTooltipForResult(int index) {
        InventoryItem inventoryItem = list.get(index);
        List<Component> tooltip = new ArrayList<>(Screen.getTooltipFromItem(minecraft, inventoryItem.itemStack));
        tooltip.add(Component.translatable("gui.maid_storage_manager.written_inventory_list.find"));
        for (Pair<Target, Integer> pair : inventoryItem.posAndSlot) {
            tooltip.add(Component.translatable("gui.maid_storage_manager.written_inventory_list.pos",
                    pair.getA().getPos().getX(),
                    pair.getA().getPos().getY(),
                    pair.getA().getPos().getZ(),
                    pair.getB()
            ).withStyle(ChatFormatting.DARK_GRAY));
        }
        return tooltip;
    }

    @Override
    protected void init() {
        super.init();
        this.width = Math.min(this.minecraft.getWindow().getGuiScaledWidth(), 300);
        this.height = Math.min(this.minecraft.getWindow().getGuiScaledHeight(), 300);
        this.left = (this.minecraft.getWindow().getGuiScaledWidth() - this.width) / 2;
        this.top = (this.minecraft.getWindow().getGuiScaledHeight() - this.height) / 2;

        this.searchBox = new EditBox(this.font,
                this.left + (this.width - 100) / 2,
                this.top + this.height - 16,
                100,
                16,
                Component.translatable("gui.maid_storage_manager.written_inventory_list.search"));

        this.columns = (this.width - 10) / 18;
        this.rows = (this.height - 50) / 18;

        this.gridSize = this.rows * this.columns;
        this.gridStart = 0;

        this.prevButton.setX(this.left);
        this.prevButton.setY(this.top);
        this.nextButton.setX(this.left + this.width - this.nextButton.getWidth());
        this.nextButton.setY(this.top);

        this.addRenderableWidget(this.prevButton);
        this.addRenderableWidget(this.nextButton);
        this.addRenderableWidget(this.searchBox);
    }

    @Override
    public void tick() {
        super.tick();

        if (data.get(uuid).size() != originalList.size()) {
            originalList = data.get(uuid);
            list = originalList;
            search = null;
        }
        if (!searchBox.getValue().equals(search)) {
            search = searchBox.getValue();
            if (search.equals(""))
                list = originalList;
            else
                list = originalList.stream()
                        .filter(inventoryItem -> {
                            ItemStack itemStack = inventoryItem.itemStack;
                            if (ModList.get().isLoaded("jecharacters")) {
                                if (Match.matches(itemStack.getHoverName().getString(), search))
                                    return true;
                                if (Match.matches(Component.translatable(itemStack.getDescriptionId()).getString(), search))
                                    return true;
                                if (itemStack.getTooltipLines(null, TooltipFlag.ADVANCED).stream().anyMatch(component -> Match.matches(component.getString(), search))) {
                                    return true;
                                }
                            }
                            if (itemStack.getHoverName().getString().contains(search))
                                return true;
                            if (Component.translatable(itemStack.getDescriptionId()).getString().contains(search))
                                return true;
                            return itemStack.getTooltipLines(null, TooltipFlag.ADVANCED).stream().anyMatch(component -> component.getString().contains(search));
                        }).toList();
        }


        if (gridStart >= list.size() && gridStart != 0)
            gridStart -= gridSize;
        nextButton.active = (list.size() >= gridStart + gridSize);
        prevButton.active = gridStart != 0;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int x, int y, float p_282465_) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, x, y, p_282465_);
        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.columns; j++) {
                int index = i * this.columns + j + gridStart;
                if (index < list.size()) {
                    int ix = this.left + j * 18;
                    int iy = this.top + 20 + i * 18;
                    ItemStack item = list.get(index).itemStack;
                    guiGraphics.renderItem(
                            item,
                            ix,
                            iy
                    );
                    guiGraphics.pose().pushPose();
                    guiGraphics.pose().translate(0, 0, 1000);
                    guiGraphics.renderItemDecorations(this.font, item, ix, iy);
                    guiGraphics.pose().scale(0.5F, 0.5F, 1);
                    guiGraphics.drawString(this.font,
                            Integer.toString(list.get(index).totalCount),
                            2 * ix + 32 - this.font.width(Integer.toString(list.get(index).totalCount)),
                            2 * iy + 32 - this.font.lineHeight,
                            0xffffff
                    );
                    guiGraphics.pose().popPose();
                    if (x >= ix && x < ix + 16 && y >= iy && y < iy + 16) {
                        guiGraphics.pose().pushPose();
                        guiGraphics.pose().translate(0, 0, 5000);
                        guiGraphics.renderTooltip(this.font,
                                this.getTooltipForResult(index),
                                Optional.empty(),
                                x,
                                y
                        );
                        guiGraphics.pose().popPose();
                    }
                }
            }
        }
        int currentPage = (gridStart / gridSize) + 1;
        int totalPage = (int) Math.ceil((double) list.size() / gridSize);
        MutableComponent component = Component.translatable("gui.maid_storage_manager.written_inventory_list.inventory_list_page", currentPage, totalPage);
        guiGraphics.drawString(this.font,
                component,
                this.left + this.width / 2 - this.font.width(component) / 2,
                this.top,
                0xFFFFFF);
    }

    @Override
    public boolean mouseClicked(double x, double y, int p_94697_) {
        int ix = (int) Math.floor((x - this.left) / 18);
        int iy = (int) Math.floor((y - this.top - 20) / 18);
        if (ix >= 0 && ix < columns && iy >= 0 && iy < rows) {
            int idx = iy * columns + ix + gridStart;
            if (idx < list.size()) {
                InventoryListDataClient.setShowingInv(list.get(idx), 400);
                minecraft.setScreen(null);
            }
        }
        return super.mouseClicked(x, y, p_94697_);
    }
}
