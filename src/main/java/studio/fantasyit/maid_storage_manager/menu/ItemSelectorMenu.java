package studio.fantasyit.maid_storage_manager.menu;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;
import studio.fantasyit.maid_storage_manager.items.RequestListItem;
import studio.fantasyit.maid_storage_manager.items.StorageDefineBauble;
import studio.fantasyit.maid_storage_manager.menu.container.CountSlot;
import studio.fantasyit.maid_storage_manager.menu.container.FilterContainer;
import studio.fantasyit.maid_storage_manager.menu.container.FilterSlot;
import studio.fantasyit.maid_storage_manager.menu.container.ISaveFilter;
import studio.fantasyit.maid_storage_manager.network.ItemSelectorGuiPacket;
import studio.fantasyit.maid_storage_manager.registry.GuiRegistry;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;
import studio.fantasyit.tour_guide.api.TourGuideTrigger;

public class ItemSelectorMenu extends AbstractContainerMenu implements ISaveFilter {
    public Player player;
    ItemStack target;
    public FilterContainer filteredItems;
    public SimpleContainer storageHandler;
    public ItemStackUtil.MATCH_TYPE matching = ItemStackUtil.MATCH_TYPE.AUTO;
    public boolean blackmode = false;
    public boolean stockMode = false;
    public boolean shouldClear = false;
    public boolean unitSecond = false;
    public int repeat = 0;

    public ItemSelectorMenu(int p_38852_, Player player) {
        super(GuiRegistry.ITEM_SELECTOR_MENU.get(), p_38852_);
        this.player = player;
        target = player.getMainHandItem();
        CompoundTag tag = target.getOrCreateTag();
        filteredItems = new FilterContainer(10, this);
        filteredItems.deserializeNBT(tag.getList(RequestListItem.TAG_ITEMS, ListTag.TAG_COMPOUND));
        matching = RequestListItem.getMatchType(target);
        repeat = tag.getInt(RequestListItem.TAG_REPEAT_INTERVAL);
        blackmode = tag.getBoolean(RequestListItem.TAG_BLACKMODE);
        stockMode = tag.getBoolean(RequestListItem.TAG_STOCK_MODE);
        unitSecond = tag.getBoolean(RequestListItem.TAG_UNIT_SECOND);
        if (unitSecond)
            repeat /= 20;
        storageHandler = new SimpleContainer(1) {
            @Override
            public void setChanged() {
                super.setChanged();
                save();
            }
        };
        storageHandler.setItem(0, ItemStackUtil.parseStack(tag.getCompound(StorageDefineBauble.TAG_STORAGE_DEFINE)));
        addPlayerSlots();
        addFilterSlots();
        addSpecialSlots();
    }

    public void clear() {
        RequestListItem.clearItemProcess(target);
        filteredItems.reset();
        this.broadcastChanges();
    }

    public void save() {
        if (player.level().isClientSide) return;
        CompoundTag tag = target.getOrCreateTag();
        ListTag list = new ListTag();
        if (tag.contains(RequestListItem.TAG_ITEMS))
            list = tag.getList(RequestListItem.TAG_ITEMS, ListTag.TAG_COMPOUND);
        while (list.size() <= filteredItems.getContainerSize())
            list.add(new CompoundTag());
        for (int i = 0; i < filteredItems.getContainerSize(); i++) {
            CompoundTag tmp = list.getCompound(i);
            tmp.put(RequestListItem.TAG_ITEMS_ITEM, filteredItems.getItem(i).serializeNBT());
            tmp.putInt(RequestListItem.TAG_ITEMS_REQUESTED, filteredItems.count[i].getValue());
            list.set(i, tmp);
        }
        tag.put(RequestListItem.TAG_ITEMS, list);
        tag.putInt(RequestListItem.TAG_MATCH, matching.ordinal());
        if (unitSecond)
            tag.putInt(RequestListItem.TAG_REPEAT_INTERVAL, repeat * 20);
        else
            tag.putInt(RequestListItem.TAG_REPEAT_INTERVAL, repeat);
        tag.putBoolean(RequestListItem.TAG_BLACKMODE, blackmode);
        tag.putBoolean(RequestListItem.TAG_STOCK_MODE, stockMode);
        tag.putBoolean(RequestListItem.TAG_UNIT_SECOND, unitSecond);
        tag.put(StorageDefineBauble.TAG_STORAGE_DEFINE, storageHandler.getItem(0).serializeNBT());
        target.setTag(tag);
    }

    private void addFilterSlots() {
        //46 18
        final int cellHeight = 18;
        final int startY = 18;
        for (int i = 0; i < 5; i++) {
            this.addSlot(new FilterSlot(filteredItems, i, 40, startY + i * cellHeight));
            this.addSlot(new FilterSlot(filteredItems, i + 5, 75, startY + i * cellHeight));
            this.addDataSlot(new CountSlot(filteredItems.count[i], filteredItems));
            this.addDataSlot(new CountSlot(filteredItems.count[i + 5], filteredItems));
            this.addDataSlot(new BasicData(filteredItems.done[i]));
            this.addDataSlot(new BasicData(filteredItems.done[i + 5]));
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
        addSlot(new Slot(storageHandler, 0, 8, 71));
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return unitSecond ? 1 : 0;
            }

            @Override
            public void set(int p_40208_) {
                unitSecond = p_40208_ == 1;
                save();
            }
        });
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return matching.ordinal();
            }

            @Override
            public void set(int p_40208_) {
                matching = ItemStackUtil.MATCH_TYPE.values()[p_40208_];
                save();
            }
        });
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return blackmode ? 1 : 0;
            }

            @Override
            public void set(int p_40208_) {
                blackmode = p_40208_ == 1;
                save();
            }
        });
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return stockMode ? 1 : 0;
            }

            @Override
            public void set(int p_40208_) {
                stockMode = p_40208_ == 1;
                save();
            }
        });
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return 0;
            }

            @Override
            public void set(int p_40208_) {
                if (p_40208_ != 0) {
                    clear();
                }
            }
        });
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return repeat;
            }

            @Override
            public void set(int p_40208_) {
                repeat = p_40208_;
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
                    if (ItemStack.isSameItemSameTags(this.filteredItems.getItem(i), slot.getItem()))
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
        return player.getMainHandItem() == target;
    }


    public void handleUpdate(ItemSelectorGuiPacket.SlotType type, int key, int value) {
        switch (type) {
            case COUNT:
                filteredItems.count[key].setValue(value);
                save();
                break;
            case MATCH_TAG:
                matching = ItemStackUtil.MATCH_TYPE.values()[value];
                save();
                break;
            case CLEAR:
                clear();
                break;
            case REPEAT:
                repeat = value;
                save();
                break;
            case BLACKLIST:
                blackmode = value == 1;
                save();
                break;
            case STOCKMODE:
                stockMode = value == 1;
                save();
                break;
            case UNITSECOND:
                if (unitSecond != (value == 1)) {
                    if (unitSecond)
                        repeat *= 20;
                    else
                        repeat /= 20;
                }
                unitSecond = value == 1;
                save();
                break;
        }
    }

    @Override
    public boolean canDragTo(Slot p_38945_) {
        return !(p_38945_ instanceof FilterSlot);
    }

    @Override
    public void slotsChanged(Container p_38868_) {
        super.slotsChanged(p_38868_);
        save();
    }

    protected static class BasicData extends DataSlot {
        private final MutableInt value;

        BasicData(MutableInt value) {
            super();
            this.value = value;
        }


        @Override
        public int get() {
            return value.getValue();
        }

        @Override
        public void set(int p_39402_) {
            value.setValue(p_39402_);
        }
    }

    @Override
    public void removed(Player p_38940_) {
        super.removed(p_38940_);
        if (p_38940_ instanceof ServerPlayer p)
            TourGuideTrigger.trigger(p, "item_selector_save");
    }
}
