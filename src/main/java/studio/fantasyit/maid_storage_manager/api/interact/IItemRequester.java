package studio.fantasyit.maid_storage_manager.api.interact;

import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.UUID;

public interface IItemRequester {
    UUID getUUID();

    boolean isRequesting();

    List<ItemStack> getToRequestItems();
}
