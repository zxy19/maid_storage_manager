package studio.fantasyit.maid_storage_manager.menu;

import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import studio.fantasyit.maid_storage_manager.items.FilterListItem;
import studio.fantasyit.maid_storage_manager.items.data.FilterItemStackList;
import studio.fantasyit.maid_storage_manager.menu.container.FilterContainer;
import studio.fantasyit.maid_storage_manager.menu.container.FilterSlot;
import studio.fantasyit.maid_storage_manager.menu.container.ISaveFilter;
import studio.fantasyit.maid_storage_manager.network.ItemSelectorGuiPacket;
import studio.fantasyit.maid_storage_manager.registry.DataComponentRegistry;
import studio.fantasyit.maid_storage_manager.registry.GuiRegistry;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;

public class FilterMenu extends AbstractContainerMenu implements ISaveFilter {
    public boolean isBlackList;
    Player player;
    ItemStack target;
    ItemFrame targetIfr = null;
    public FilterContainer filteredItems;
    public boolean matchTag = false;

    public FilterMenu(int p_38852_, Player player, int entityId) {
        super(GuiRegistry.FILTER_MENU.get(), p_38852_);
        this.player = player;
        if (entityId == -1)
            target = player.getMainHandItem();
        else if (player.level().getEntity(entityId) instanceof ItemFrame ifr) {
            targetIfr = ifr;
            target = ifr.getItem();
        }
        filteredItems = new FilterContainer(20, this);
        filteredItems.loadFromFilterItemStackList(target.getOrDefault(DataComponentRegistry.FILTER_ITEMS, FilterListItem.EMPTY));
        matchTag = target.getOrDefault(DataComponentRegistry.FILTER_MATCH_TAG, false);
        isBlackList = target.getOrDefault(DataComponentRegistry.FILTER_BLACK_MODE, false);
        addPlayerSlots();
        addFilterSlots();
        addSpecialSlots();
    }

    public void save() {
        FilterItemStackList list = new FilterItemStackList();
        for (int i = 0; i < filteredItems.getContainerSize(); i++) {
            list.list.set(i, filteredItems.getItem(i));
        }
        target.set(DataComponentRegistry.FILTER_ITEMS, list.toImmutable());
        target.set(DataComponentRegistry.FILTER_MATCH_TAG, matchTag);
        target.set(DataComponentRegistry.FILTER_BLACK_MODE, isBlackList);
    }

    private void addFilterSlots() {
        final int cellHeight = 18;
        final int cellWidth = 18;
        final int startY = 17;
        final int startX = 45;
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 4; j++) {
                this.addSlot(new FilterSlot(filteredItems,
                        i * 4 + j,
                        startX + j * cellWidth,
                        startY + i * cellHeight));
            }
        }
    }

    private void addPlayerSlots() {
        final int cellHeight = 18;
        final int cellWidth = 18;
        final int startY = 118;
        final int startX = 8;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(player.getInventory(),
                        9 + i * 9 + j,
                        startX + j * cellWidth,
                        startY + i * cellHeight));
            }
        }
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(player.getInventory(),
                    i,
                    startX + i * cellWidth,
                    startY + 58));
        }
    }

    @Override
    public void clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
        if (slotId >= 0 && this.getSlot(slotId) instanceof FilterSlot fs) {
            int slot = fs.getContainerSlot();
            if (clickTypeIn == ClickType.THROW)
                return;

            ItemStack held = getCarried();
            if (clickTypeIn == ClickType.CLONE) {
                if (player.isCreative() && held.isEmpty()) {
                    ItemStack stackInSlot = filteredItems.getItem(slot)
                            .copy();
                    stackInSlot.setCount(stackInSlot.getMaxStackSize());
                    setCarried(stackInSlot);
                    return;
                }
                return;
            }

            ItemStack insert;
            if (held.isEmpty()) {
                insert = ItemStack.EMPTY;
            } else {
                insert = held.copy();
                insert.setCount(1);
            }
            filteredItems.setItem(slot, insert);
            getSlot(slotId).setChanged();
        } else {
            super.clicked(slotId, dragType, clickTypeIn, player);
        }
    }

    private void addSpecialSlots() {

        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return matchTag ? 1 : 0;
            }

            @Override
            public void set(int p_40208_) {
                matchTag = p_40208_ == 1;
                save();
            }
        });
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return isBlackList ? 1 : 0;
            }

            @Override
            public void set(int p_40208_) {
                isBlackList = p_40208_ == 1;
                save();
            }
        });
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player p_38941_, int p_38942_) {
        Slot slot = this.getSlot(p_38942_);
        if (slot.hasItem()) {
            if (slot instanceof FilterSlot fs) {
                fs.set(ItemStack.EMPTY);
            } else {
                int containerSize = this.filteredItems.getContainerSize();
                boolean found = false;
                for (int i = 0; i < containerSize; i++)
                    if (ItemStackUtil.isSame(this.filteredItems.getItem(i), slot.getItem(), true))
                        found = true;
                if (!found) {
                    for (int i = 0; i < containerSize; i++) {
                        if (this.filteredItems.getItem(i).isEmpty()) {
                            this.filteredItems.setItem(i, slot.getItem().copyWithCount(1));
                            break;
                        }
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        if (targetIfr != null && targetIfr.isAlive())
            return targetIfr.distanceTo(player) <= player.entityInteractionRange();
        return player.getMainHandItem() == target;
    }


    public void handleUpdate(ItemSelectorGuiPacket.SlotType type, int key, int value) {
        switch (type) {
            case MATCH_TAG -> {
                matchTag = value == 1;
                save();
            }
            case BLACKLIST -> {
                isBlackList = value == 1;
                save();
            }
        }
    }

    @Override
    public boolean canDragTo(Slot p_38945_) {
        return !(p_38945_ instanceof FilterSlot);
    }
}
