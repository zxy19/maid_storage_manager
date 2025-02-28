package studio.fantasyit.maid_storage_manager.storage.base;

import net.minecraft.world.item.ItemStack;

public interface IFilterable {
    boolean isAvailable(ItemStack itemStack);
    boolean isWhitelist();
    default boolean isRequestOnly() {
        return false;
    }
}