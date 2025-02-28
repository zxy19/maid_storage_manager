package studio.fantasyit.maid_storage_manager.menu;

import me.towdium.jecharacters.utils.Match;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.fml.ModList;
import studio.fantasyit.maid_storage_manager.data.InventoryListDataClient;

import java.util.List;
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
    private List<ItemStack> originalList;
    private List<ItemStack> list;
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
        this.data = InventoryListDataClient.getInstance();
        this.originalList = data.get(uuid).stream().map(pair -> pair.getFirst().copyWithCount(pair.getSecond())).toList();
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

        this.gridSize = this.width * this.columns;
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
            originalList = data.get(uuid).stream().map(pair -> pair.getFirst().copyWithCount(pair.getSecond())).toList();
            list = originalList;
            search = null;
        }
        if (!searchBox.getValue().equals(search)) {
            search = searchBox.getValue();
            if (search.equals(""))
                list = originalList;
            else
                list = originalList.stream().filter(itemStack -> {
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
                    ItemStack item = list.get(index);
                    guiGraphics.renderItem(
                            item,
                            ix,
                            iy
                    );
                    guiGraphics.pose().pushPose();
                    guiGraphics.pose().scale(0.5F, 0.5F, 1);
                    guiGraphics.renderItemDecorations(this.font, item, 2 * ix + 16, 2 * iy + 16);
                    guiGraphics.pose().popPose();
                    if (x >= ix && x < ix + 16 && y >= iy && y < iy + 16) {
                        guiGraphics.pose().pushPose();
                        guiGraphics.pose().translate(0, 0, 2000);
                        guiGraphics.renderTooltip(this.font,
                                item,
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
}
