package studio.fantasyit.maid_storage_manager.util;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;

import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

public class ItemStackUtil {
    public static boolean isSame(ItemStack stack1, ItemStack stack2, boolean matchTag) {
        if (stack1.isEmpty() && stack2.isEmpty()) return true;
        if (stack1.isEmpty() || stack2.isEmpty()) return false;
        if (matchTag)
            return ItemStack.isSameItemSameComponents(stack1, stack2);
        return ItemStack.isSameItem(stack1, stack2);
    }

    public static TagKey<Item> MatchItem = TagKey.create(BuiltInRegistries.ITEM.key(), ResourceLocation.fromNamespaceAndPath(MaidStorageManager.MODID, "no_components"));
    public static TagKey<Item> NoMatchItem = TagKey.create(BuiltInRegistries.ITEM.key(), ResourceLocation.fromNamespaceAndPath(MaidStorageManager.MODID, "use_components"));


    public static boolean isSameInCrafting(ItemStack stack1, ItemStack stack2) {
        if (stack1.getItem() != stack2.getItem()) return false;
        if (stack1.isEmpty() || stack2.isEmpty()) return true;
        boolean matchTag = Config.craftingMatchTag;
        if (stack1.is(MatchItem)) matchTag = false;
        if (stack1.is(NoMatchItem)) matchTag = true;
        if (!matchTag) return true;
        return isSameTagInCrafting(stack1, stack2);
    }

    public static boolean isSameTagInCrafting(ItemStack stack1, ItemStack stack2) {
        DataComponentMap components1 = stack1.getComponents();
        DataComponentMap components2 = stack2.getComponents();
        for (TypedDataComponent<?> c : components1) {
            if (Config.noMatchPaths.contains(c.type().toString())) continue;
            if (!components2.has(c.type())) return false;
            if (!Objects.equals(components2.get(c.type()), c.value())) return false;
        }
        return true;
    }

    public static ItemStack removeIsMatchInList(List<ItemStack> list, ItemStack itemStack, boolean matchTag) {
        return removeIsMatchInList(list, itemStack, (a, b) -> isSame(a, b, matchTag));
    }

    public static ItemStack removeIsMatchInList(List<ItemStack> list, ItemStack itemStack, BiFunction<ItemStack, ItemStack, Boolean> isMatch) {
        for (int i = 0; i < list.size(); i++) {
            ItemStack item = list.get(i);
            if (isMatch.apply(item, itemStack)) {
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

    public static ItemStack addToList(List<ItemStack> list, ItemStack itemStack, boolean matchTag) {
        return ItemStackUtil.addToList(list, itemStack, (a, b) -> isSame(a, b, matchTag));
    }

    public static ItemStack addToList(List<ItemStack> list, ItemStack itemStack, BiFunction<ItemStack, ItemStack, Boolean> isMatch) {
        for (int i = 0; i < list.size(); i++) {
            ItemStack item = list.get(i);
            if (isMatch.apply(item, itemStack)) {
                item.grow(itemStack.getCount());
                return item;
            }
        }
        list.add(itemStack.copy());
        return itemStack.copy();
    }
}
