package studio.fantasyit.maid_storage_manager.storage.base;

import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Function;

public interface IStorageRequestContext extends IStorageContext {
    void request(List<ItemStack> itemList, boolean matchNbt, Function<ItemStack, Integer> preFetch, Function<ItemStack, ItemStack> process);
}