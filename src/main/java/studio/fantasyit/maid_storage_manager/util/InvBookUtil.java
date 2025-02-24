package studio.fantasyit.maid_storage_manager.util;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Objects;

public class InvBookUtil {
    public static boolean isInvBookFor(ItemStack stack, EntityMaid maid) {
        if (stack.is(Items.WRITTEN_BOOK)) {
            CompoundTag tag = stack.getOrCreateTag();
            return tag.getUUID("maid").equals(maid.getUUID());
        }
        return false;
    }

    public static ItemStack updateSign(ItemStack stack, EntityMaid maid) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putUUID("maid", maid.getUUID());
        tag.putString(WrittenBookItem.TAG_AUTHOR, maid.getName().getString());
        tag.putString(WrittenBookItem.TAG_TITLE, Component.translatable("item.maid_inv_book", maid.getName().getString()).getString());
        tag.putBoolean(WrittenBookItem.TAG_RESOLVED, false);
        stack.setTag(tag);
        return stack;
    }

    public static ItemStack getInvBookFor(EntityMaid maid) {
        ItemStack stack = new ItemStack(Items.WRITTEN_BOOK);
        CompoundTag tag = stack.getOrCreateTag();
        stack.setTag(tag);
        return stack;
    }

    public static ItemStack clearItemRecordFor(ItemStack book, BlockPos from) {
        CompoundTag tag = book.getOrCreateTag();
        CompoundTag items = tag.getCompound("items");
        items.put(from.toShortString(), new CompoundTag());
        tag.put("items", items);
        book.setTag(tag);
        return book;
    }

    public static ItemStack recordItemList(ItemStack book, BlockPos from, List<ItemStack> itemList) {
        CompoundTag tag = book.getOrCreateTag();
        CompoundTag items = tag.getCompound("items");
        CompoundTag itemsFromPos = items.getCompound(from.toShortString());
        for (ItemStack item : itemList) {
            String key = Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(item.getItem())).toString();
            CompoundTag itemTag = itemsFromPos.getCompound(key);
            itemTag.putInt("count", item.getCount() + itemTag.getInt("count"));
            itemsFromPos.put(key, itemTag);
        }
        items.put(from.toShortString(), itemsFromPos);
        tag.put("items", items);
        book.setTag(tag);
        return book;
    }

}
