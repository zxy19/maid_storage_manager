package studio.fantasyit.maid_storage_manager.storage.base;

import net.minecraft.world.item.ItemStack;

public interface ISlotBasedStorage {
    int getSlots();
    ItemStack getStackInSlot(int slot);
}