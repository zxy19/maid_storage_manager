package studio.fantasyit.maid_storage_manager.storage.base;

import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Function;

public interface IStorageExtractableContext extends IStorageContext,IAsyncContext<Function<ItemStack, ItemStack>> {
    void setExtract(List<ItemStack> itemList, boolean matchNbt);
}