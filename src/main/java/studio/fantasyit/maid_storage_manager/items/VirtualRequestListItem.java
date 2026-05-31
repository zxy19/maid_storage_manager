package studio.fantasyit.maid_storage_manager.items;

import net.minecraft.resources.Identifier;
import studio.fantasyit.maid_storage_manager.items.data.RequestItemStackList;
import studio.fantasyit.maid_storage_manager.registry.DataComponentRegistry;

public class VirtualRequestListItem extends MaidInteractItem {

    public VirtualRequestListItem(Identifier id) {
        super(id, new Properties()
                .stacksTo(1)
                .component(DataComponentRegistry.REQUEST_ITEMS.get(), new RequestItemStackList().toImmutable())
                .component(DataComponentRegistry.REQUEST_VIRTUAL.get(), true)
        );
    }
}
