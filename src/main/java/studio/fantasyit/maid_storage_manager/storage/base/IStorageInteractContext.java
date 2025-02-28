package studio.fantasyit.maid_storage_manager.storage.base;

import net.minecraft.world.item.ItemStack;

import java.util.function.Function;

public interface IStorageInteractContext extends IStorageContext {
    void tick(Function<ItemStack, ItemStack> process);
}
