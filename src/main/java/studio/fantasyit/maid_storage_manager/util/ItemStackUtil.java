package studio.fantasyit.maid_storage_manager.util;

import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ItemStackUtil {
    public static boolean isSame(ItemStack stack1, ItemStack stack2, boolean matchTag) {
        if (stack1.isEmpty() && stack2.isEmpty()) return true;
        if (stack1.isEmpty() || stack2.isEmpty()) return false;
        if (matchTag)
            return ItemStack.isSameItemSameTags(stack1, stack2);
        return ItemStack.isSameItem(stack1, stack2);
    }

    public static ItemStack removeIsMatchInList(List<ItemStack> list, ItemStack itemStack, boolean matchTag) {
        for (int i = 0; i < list.size(); i++) {
            ItemStack item = list.get(i);
            if (isSame(item, itemStack, matchTag)) {
                int costCount = Math.min(item.getCount(), itemStack.getCount());
                item.shrink(costCount);
                itemStack.shrink(costCount);
                if (item.isEmpty()) {
                    list.remove(i);
                    i--;
                }
            }
            if (itemStack.isEmpty())
                return ItemStack.EMPTY;
        }
        return itemStack;
    }

    public static void addToList(List<ItemStack> list, ItemStack itemStack, boolean matchTag) {
        for (int i = 0; i < list.size(); i++) {
            ItemStack item = list.get(i);
            if (isSame(item, itemStack, matchTag)) {
                item.grow(itemStack.getCount());
                return;
            }
        }
        list.add(itemStack);
    }
}
