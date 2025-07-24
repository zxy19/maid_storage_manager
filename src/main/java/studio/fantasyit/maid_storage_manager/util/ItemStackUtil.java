package studio.fantasyit.maid_storage_manager.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

public class ItemStackUtil {
    public static boolean isSame(ItemStack stack1, ItemStack stack2, boolean matchTag) {
        if (stack1.isEmpty() && stack2.isEmpty()) return true;
        if (stack1.isEmpty() || stack2.isEmpty()) return false;
        if (matchTag)
            return ItemStack.isSameItemSameTags(stack1, stack2);
        return ItemStack.isSameItem(stack1, stack2);
    }

    public static TagKey<Item> MatchItem = TagKey.create(ForgeRegistries.ITEMS.getRegistryKey(), ResourceLocation.fromNamespaceAndPath(MaidStorageManager.MODID, "no_nbt"));
    public static TagKey<Item> NoMatchItem = TagKey.create(ForgeRegistries.ITEMS.getRegistryKey(), ResourceLocation.fromNamespaceAndPath(MaidStorageManager.MODID, "use_nbt"));


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
        CompoundTag tag1 = Optional.ofNullable(stack1.getTag()).map(CompoundTag::copy).orElseGet(CompoundTag::new);
        CompoundTag tag2 = Optional.ofNullable(stack2.getTag()).map(CompoundTag::copy).orElseGet(CompoundTag::new);
        for (String tagPath : Config.noMatchPaths) {
            String[] path = tagPath.split("[\\.\\[]");
            tag1 = CompoundTagUtil.removeKeyFrom(tag1, path, 0);
            tag2 = CompoundTagUtil.removeKeyFrom(tag2, path, 0);
        }
        return tag1.equals(tag2);
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
