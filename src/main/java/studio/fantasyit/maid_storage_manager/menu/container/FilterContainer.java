package studio.fantasyit.maid_storage_manager.menu.container;


import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;
import studio.fantasyit.maid_storage_manager.items.data.FilterItemStackList;
import studio.fantasyit.maid_storage_manager.items.data.RequestItemStackList;

import java.util.Arrays;
import java.util.List;

public class FilterContainer implements Container {
    private final AbstractContainerMenu menu;
    protected int size;
    ItemStack[] items;

    public MutableInt[] count;
    public MutableInt[] collected;
    public MutableInt[] done;


    public FilterContainer(int size, AbstractContainerMenu menu) {
        this.menu = menu;
        this.size = size;
        this.items = new ItemStack[size];
        for (int i = 0; i < size; i++) {
            items[i] = ItemStack.EMPTY;
        }
        count = new MutableInt[size];
        collected = new MutableInt[size];
        done = new MutableInt[size];
        for (int i = 0; i < size; i++) {
            count[i] = new MutableInt(1);
            collected[i] = new MutableInt(0);
            done[i] = new MutableInt(0);
        }
    }

    public void reset() {
        for (int i = 0; i < size; i++) {
            collected[i].setValue(0);
            done[i].setValue(0);
        }
        if (menu instanceof ISaveFilter isf)
            isf.save();
    }

    @Override
    public int getContainerSize() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return Arrays.stream(items).allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int p_18941_) {
        return items[p_18941_];
    }

    @Override
    public @NotNull ItemStack removeItem(int p_18942_, int p_18943_) {
        if (!items[p_18942_].isEmpty()) {
            ItemStack ret = items[p_18942_].split(p_18943_);
            this.setChanged();
            return ret;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int p_18951_) {
        if (!items[p_18951_].isEmpty()) {
            ItemStack itemStack = items[p_18951_];
            items[p_18951_] = ItemStack.EMPTY;
            return itemStack;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int p_18944_, ItemStack p_18945_) {
        setItemNoTrigger(p_18944_, p_18945_);
        this.setChanged();
    }

    public void setItemNoTrigger(int p_18944_, ItemStack p_18945_) {
        items[p_18944_] = p_18945_.copyWithCount(1);
        count[p_18944_].setValue(1);
    }

    @Override
    public void setChanged() {
        if (menu instanceof ISaveFilter isf)
            isf.save();
    }

    @Override
    public boolean stillValid(Player p_18946_) {
        return true;
    }

    @Override
    public void clearContent() {
        Arrays.fill(items, ItemStack.EMPTY);
        for (int i = 0; i < size; i++) {
            count[i].setValue(1);
        }
        this.setChanged();
    }


    public void setCount(int index, int count) {
        if (count != this.count[index].getValue()) {
            this.count[index].setValue(count);
        }
    }

    public MutableInt getCountMutable(int index) {
        return count[index];
    }

    public int getCount(int index) {
        return count[index].getValue();
    }


    public void loadFromFilterItemStackList(FilterItemStackList.Immutable filterItemStack) {
        List<ItemStack> list = filterItemStack.list();
        for (int i = 0; i < list.size(); i++) {
            if (i >= size) continue;
            setItemNoTrigger(i, list.get(i));
            setCount(i, 1);
        }
        setChanged();
    }

    public void loadFromRequestData(RequestItemStackList.Immutable requestData) {
        List<RequestItemStackList.ImmutableItem> list = requestData.list();
        for (int i = 0; i < list.size(); i++) {
            if (i >= size) continue;
            RequestItemStackList.ImmutableItem tmp = list.get(i);
            setItemNoTrigger(i, tmp.item());
            setCount(i, tmp.requested());
            done[i].setValue(tmp.done() ? 1 : 0);
            collected[i].setValue(tmp.collected());
        }
    }

    @Override
    public boolean canTakeItem(Container p_273520_, int p_272681_, ItemStack p_273702_) {
        return false;
    }
}
