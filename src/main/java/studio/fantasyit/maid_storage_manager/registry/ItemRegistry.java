package studio.fantasyit.maid_storage_manager.registry;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.items.RequestListItem;
import studio.fantasyit.maid_storage_manager.items.WrittenInvListItem;

import java.util.function.Supplier;

public class ItemRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MaidStorageManager.MODID);

    protected static RegistryObject<Item> item(String name, Supplier<Item> properties) {
        RegistryObject<Item> item = ITEMS.register(name, properties);
        return item;
    }

    protected static RegistryObject<Item> item(String name) {
        return item(name, () -> new Item(new Item.Properties()));
    }

    protected static RegistryObject<Item> item(RegistryObject<Block> block) {
        return item(block.getId().getPath(), () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

    public static final RegistryObject<Item> REQUEST_LIST_ITEM = item("request_list", RequestListItem::new);
    public static final RegistryObject<Item> INVENTORY_LIST = item("inventory_list");
    public static final RegistryObject<Item> WRITTEN_INVENTORY_LIST = item("written_inventory_list", WrittenInvListItem::new);
}
