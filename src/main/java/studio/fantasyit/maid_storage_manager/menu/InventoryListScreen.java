package studio.fantasyit.maid_storage_manager.menu;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.data.InventoryItem;
import studio.fantasyit.maid_storage_manager.data.InventoryListDataClient;
import studio.fantasyit.maid_storage_manager.integration.tour_guide.tours.InventoryListTour;
import studio.fantasyit.maid_storage_manager.menu.base.IItemTarget;
import studio.fantasyit.maid_storage_manager.util.InventoryListUtil;
import studio.fantasyit.tour_guide.api.TourGuideTrigger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class InventoryListScreen extends Screen {
    private enum FilterOption {
        ALL(Component.translatable("gui.maid_storage_manager.written_inventory_list.filter.all"), inventoryItem -> true),
        STORED(Component.translatable("gui.maid_storage_manager.written_inventory_list.filter.stored"),
                inventoryItem -> inventoryItem.posAndSlot.stream().anyMatch(t -> !t.isCraftGuide())),
        CRAFTABLE(Component.translatable("gui.maid_storage_manager.written_inventory_list.filter.craftable"),
                inventoryItem -> inventoryItem.posAndSlot.stream().anyMatch(InventoryItem.PositionCount::isCraftGuide));
        public final Component component;
        public final Predicate<InventoryItem> predicate;

        FilterOption(Component component, Predicate<InventoryItem> predicate) {
            this.component = component;
            this.predicate = predicate;
        }
    }

    private enum SortOption {
        DEFAULT(Component.translatable("gui.maid_storage_manager.written_inventory_list.sorting.default"), (a, b) -> 0),
        NAME(Component.translatable("gui.maid_storage_manager.written_inventory_list.sorting.name_p"),
                Comparator.comparing(a -> a.itemStack.getHoverName().getString())),
        NAME_N(Component.translatable("gui.maid_storage_manager.written_inventory_list.sorting.name_n"),
                (a, b) -> -a.itemStack.getHoverName().getString().compareTo(b.itemStack.getHoverName().getString())),
        COUNT(Component.translatable("gui.maid_storage_manager.written_inventory_list.sorting.count_p"),
                Comparator.comparingInt(t -> t.totalCount)),
        COUNT_N(Component.translatable("gui.maid_storage_manager.written_inventory_list.sorting.count_n"),
                Comparator.comparingInt(t -> -t.totalCount));
        public final Component component;
        public final Comparator<InventoryItem> comparator;

        SortOption(Component component, Comparator<InventoryItem> comparator) {
            this.component = component;
            this.comparator = comparator;
        }
    }


    private static SortOption sortingOption = SortOption.DEFAULT;
    private static FilterOption filterOption = FilterOption.ALL;
    private final Button prevButton;
    private final Button nextButton;
    private final Button sortingButton;
    private final Button filterButton;

    private final InventoryListDataClient data;
    private final Object uuid;
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
    private Screen toSelectTarget = null;

    public InventoryListScreen(Object uuid, Screen toSelectTarget) {
        this(uuid);
        this.toSelectTarget = toSelectTarget;
    }

    public InventoryListScreen(Object uuid) {
        super(Component.empty());
        this.uuid = uuid;
        this.prevButton = Button.builder(Component.translatable("gui.maid_storage_manager.written_inventory_list.previous"), (button) -> this.doPrev())
                .size(16, 16)
                .build();
        this.nextButton = Button.builder(Component.translatable("gui.maid_storage_manager.written_inventory_list.next"), (button) -> this.doNext())
                .size(16, 16)
                .build();
        this.sortingButton = Button.builder(Component.literal(""), btn -> this.switchSorting())
                .size(96, 16)
                .build();
        this.filterButton = Button.builder(Component.literal(""), (button) -> this.switchFilter())
                .size(96, 16)
                .build();
        InventoryListDataClient.clearShowingInv();
        this.data = InventoryListDataClient.getInstance();
        this.originalList = data.get(uuid);
        this.list = originalList;
    }

    private void switchFilter() {
        filterOption = FilterOption.values()[(filterOption.ordinal() + 1) % FilterOption.values().length];
        filterButton.setMessage(filterOption.component);
        reGenerateList();
    }

    private void switchSorting() {
        sortingOption = SortOption.values()[(sortingOption.ordinal() + 1) % SortOption.values().length];
        sortingButton.setMessage(sortingOption.component);
        reGenerateList();
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
        if (toSelectTarget == null) {
            if (inventoryItem.posAndSlot.stream().anyMatch(InventoryItem.PositionCount::isCraftGuide))
                tooltip.add(Component.translatable("gui.maid_storage_manager.written_inventory_list.craftable"));
            if (inventoryItem.posAndSlot.stream().anyMatch(t -> !t.isCraftGuide()))
                tooltip.add(Component.translatable("gui.maid_storage_manager.written_inventory_list.find"));
            for (InventoryItem.PositionCount pair : inventoryItem.posAndSlot) {
                if (!pair.isCraftGuide())
                    tooltip.add(Component.translatable("gui.maid_storage_manager.written_inventory_list.pos",
                            pair.pos().getPos().getX(),
                            pair.pos().getPos().getY(),
                            pair.pos().getPos().getZ(),
                            pair.count()
                    ).withStyle(ChatFormatting.DARK_GRAY));
                else
                    tooltip.add(Component.translatable("gui.maid_storage_manager.written_inventory_list.craft_guide_pos",
                            pair.pos().getPos().getX(),
                            pair.pos().getPos().getY(),
                            pair.pos().getPos().getZ()
                    ).withStyle(ChatFormatting.DARK_GRAY));
            }
        } else {
            tooltip.add(Component.translatable("gui.maid_storage_manager.written_inventory_list.select"));
        }
        return tooltip;
    }

    @Override
    protected void init() {
        super.init();
        this.width = (Math.min(this.minecraft.getWindow().getGuiScaledWidth(), 300) - 10) / 18 * 18 + 10;
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

        this.sortingButton.setX(this.left);
        this.sortingButton.setY(this.top + this.height - 16);
        this.sortingButton.setMessage(sortingOption.component);
        this.filterButton.setX(this.left + this.width - filterButton.getWidth());
        this.filterButton.setY(this.top + this.height - 16);
        this.filterButton.setMessage(filterOption.component);

        this.addRenderableWidget(this.prevButton);
        this.addRenderableWidget(this.nextButton);
        this.addRenderableWidget(this.searchBox);
        this.addRenderableWidget(this.sortingButton);
        this.addRenderableWidget(this.filterButton);
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
            reGenerateList();
        }


        if (gridStart >= list.size() && gridStart != 0)
            gridStart -= gridSize;
        nextButton.active = (list.size() >= gridStart + gridSize);
        prevButton.active = gridStart != 0;
    }

    private void reGenerateList() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (search.equals(""))
            list = originalList;
        else
            list = originalList.stream()
                    .filter(inventoryItem -> InventoryListUtil.isMatchSearchStr(inventoryItem.itemStack, search)).toList();

        list = list.stream().filter(filterOption.predicate).sorted(sortingOption.comparator).toList();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int x, int y, float p_282465_) {

        super.render(guiGraphics, x, y, p_282465_);
        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.columns; j++) {
                int index = i * this.columns + j + gridStart;
                if (index < list.size()) {
                    int ix = this.left + 5 + j * 18;
                    int iy = this.top + 20 + i * 18;
                    ItemStack item = list.get(index).itemStack;
                    guiGraphics.renderItem(
                            item,
                            ix,
                            iy
                    );
                    guiGraphics.pose().pushPose();
                    guiGraphics.pose().translate(0, 0, 200);
                    guiGraphics.renderItemDecorations(this.font, item, ix, iy);
                    guiGraphics.pose().scale(0.5F, 0.5F, 1);
                    guiGraphics.drawString(this.font,
                            Integer.toString(list.get(index).totalCount),
                            2 * ix + 32 - this.font.width(Integer.toString(list.get(index).totalCount)),
                            2 * iy + 32 - this.font.lineHeight,
                            0xffffff
                    );
                    guiGraphics.pose().popPose();
                }
            }
        }
        guiGraphics.flush();
        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.columns; j++) {
                int index = i * this.columns + j + gridStart;
                if (index < list.size()) {
                    int ix = this.left + 5 + j * 18;
                    int iy = this.top + 20 + i * 18;
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


    public boolean mouseScrolled(double p_94686_, double p_94687_, double dx, double dy) {
        if (dy < 0)
            doNext();
        else if (dy > 0)
            doPrev();
        return true;
    }

    @Override
    public boolean mouseClicked(double x, double y, int p_94697_) {
        int ix = (int) Math.floor((x - this.left) / 18);
        int iy = (int) Math.floor((y - this.top - 20) / 18);
        if (ix >= 0 && ix < columns && iy >= 0 && iy < rows) {
            int idx = iy * columns + ix + gridStart;
            if (idx < list.size()) {
                if (toSelectTarget != null) {
                    if (toSelectTarget instanceof IItemTarget iit) {
                        iit.itemSelected(list.get(idx).itemStack);
                    }
                    minecraft.setScreen(toSelectTarget);
                } else {
                    InventoryListDataClient.setShowingInv(list.get(idx), 400);
                    TourGuideTrigger.triggerClient(InventoryListTour.TRIGGER_CLICK_INV,new CompoundTag());
                    minecraft.setScreen(null);
                }
            }
        }
        return super.mouseClicked(x, y, p_94697_);
    }
}
