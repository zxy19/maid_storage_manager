package studio.fantasyit.maid_storage_manager.storage.base;

import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Function;

public interface IStorageInsertableContext extends IStorageContext {
    ItemStack insert(ItemStack item);
}
