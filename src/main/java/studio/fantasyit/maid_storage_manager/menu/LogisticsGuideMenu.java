package studio.fantasyit.maid_storage_manager.menu;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import studio.fantasyit.maid_storage_manager.items.LogisticsGuide;
import studio.fantasyit.maid_storage_manager.items.data.ItemStackData;
import studio.fantasyit.maid_storage_manager.menu.container.FilterSlot;
import studio.fantasyit.maid_storage_manager.menu.container.ISaveFilter;
import studio.fantasyit.maid_storage_manager.menu.container.NoPlaceFilterSlot;
import studio.fantasyit.maid_storage_manager.network.ItemSelectorGuiPacket;
import studio.fantasyit.maid_storage_manager.registry.DataComponentRegistry;
import studio.fantasyit.maid_storage_manager.registry.GuiRegistry;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;
import studio.fantasyit.maid_storage_manager.storage.Target;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LogisticsGuideMenu extends AbstractContainerMenu implements ISaveFilter {
    Player player;
    ItemStack target;
    public SimpleContainer container;
    public boolean single_mode = false;
    Slot slotGuide;
    public static final int SLOT_GUIDE_OFFSET_Y = 1;
    public static final int SLOT_GUIDE_OFFSET_X = 8;
    List<Slot> playerSlots = new ArrayList<>();

    public LogisticsGuideMenu(int p_38852_, Player player) {
        super(GuiRegistry.LOGISTICS_GUIDE_MENU.get(), p_38852_);
        this.player = player;
        target = player.getMainHandItem();
        container = new SimpleContainer(1);
        container.setItem(0, target.getOrDefault(DataComponentRegistry.CONTAIN_ITEM, ItemStackData.EMPTY).itemStack(player.registryAccess()).copy());
        container.addListener((e) -> save());
        single_mode = target.getOrDefault(DataComponentRegistry.LOGISTICS_SINGLE, false);
        addPlayerSlots();
        addFilterSlots();
        addSpecialSlots();
    }

    public void save() {
        target.set(DataComponentRegistry.LOGISTICS_SINGLE, single_mode);
        target.set(DataComponentRegistry.CONTAIN_ITEM, new ItemStackData(player.registryAccess(), container.getItem(0).copy()));
    }

    private void addFilterSlots() {
        slotGuide = this.addSlot(new Slot(container, 0, 40, 24));
        Target input = LogisticsGuide.getInput(target);
        if (input != null)
            addSlot(new NoPlaceFilterSlot(
                    18, 78,
                    input.getBlockStateInLevel(player.level()).getBlock().asItem().getDefaultInstance()
                    , 0
            ));

        Target output = LogisticsGuide.getOutput(target);
        if (output != null)
            addSlot(new NoPlaceFilterSlot(
                    112, 78,
                    output.getBlockStateInLevel(player.level()).getBlock().asItem().getDefaultInstance()
                    , 1
            ));
    }

    private void addPlayerSlots() {
        final int cellHeight = 18;
        final int cellWidth = 18;
        final int startY = 118;
        final int startX = 8;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                playerSlots.add(this.addSlot(new Slot(player.getInventory(),
                        9 + i * 9 + j,
                        startX + j * cellWidth,
                        startY + i * cellHeight)));
            }
        }
        for (int i = 0; i < 9; i++) {
            playerSlots.add(this.addSlot(new Slot(player.getInventory(),
                    i,
                    startX + i * cellWidth,
                    startY + 58)));
        }
    }

    @Override
    public void clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
        if (slotGuide.index == slotId) {
            ItemStack held = getCarried();
            if (held.isEmpty() || held.is(ItemRegistry.CRAFT_GUIDE.get()) || held.is(ItemRegistry.FILTER_LIST.get()))
                super.clicked(slotId, dragType, clickTypeIn, player);
        } else {
            super.clicked(slotId, dragType, clickTypeIn, player);
        }
    }

    private void addSpecialSlots() {

        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return single_mode ? 1 : 0;
            }

            @Override
            public void set(int p_40208_) {
                single_mode = p_40208_ == 1;
                save();
            }
        });
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player p_38941_, int p_38942_) {
        Slot slot = this.getSlot(p_38942_);
        if (slot.hasItem()) {
            if (slot.index == slotGuide.index) {
                for (int i = 0; i < playerSlots.size(); i++) {
                    if (playerSlots.get(i).getItem().isEmpty()) {
                        playerSlots.get(i).set(slot.getItem());
                        slot.set(ItemStack.EMPTY);
                        save();
                        return ItemStack.EMPTY;
                    }
                }
            } else {
                if ((slot.getItem().is(ItemRegistry.CRAFT_GUIDE.get()) || slot.getItem().is(ItemRegistry.FILTER_LIST.get())) && slotGuide.getItem().isEmpty()) {
                    slotGuide.set(slot.getItem());
                    slot.set(ItemStack.EMPTY);
                    save();
                    return ItemStack.EMPTY;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.getMainHandItem() == target;
    }


    public void handleUpdate(ItemSelectorGuiPacket.SlotType type, int key, int value) {
        if (Objects.requireNonNull(type) == ItemSelectorGuiPacket.SlotType.STOCKMODE) {
            single_mode = value == 1;
            save();
        }
    }

    @Override
    public boolean canDragTo(Slot p_38945_) {
        return !(p_38945_ instanceof FilterSlot);
    }
}
