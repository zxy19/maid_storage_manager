package studio.fantasyit.maid_storage_manager.storage.base;

import net.minecraft.world.item.ItemStack;
import oshi.util.tuples.Pair;

import java.util.List;
import java.util.function.Function;

public interface IStorageRequestContext extends IStorageContext {
    void request(List<Pair<ItemStack, Integer>> itemList, boolean matchNbt, Function<Pair<ItemStack, Integer>, Integer> test, Function<ItemStack, ItemStack> process);
}