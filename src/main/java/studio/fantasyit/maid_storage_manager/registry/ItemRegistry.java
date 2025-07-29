package studio.fantasyit.maid_storage_manager.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.items.*;

import java.util.function.Supplier;

public class ItemRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, MaidStorageManager.MODID);

    protected static <T extends Item> DeferredHolder<Item, T> item(String name, Supplier<T> properties) {
        return ITEMS.register(name, properties);
    }

    protected static DeferredHolder<Item, Item> item(String name) {
        return item(name, () -> new Item(new Item.Properties()));
    }

    protected static DeferredHolder<Item, Item> item(DeferredHolder<Block, Block> block) {
        return item(block.getId().getPath(), () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

    public static final DeferredHolder<Item, RequestListItem> REQUEST_LIST_ITEM = item("request_list", RequestListItem::new);
    public static final DeferredHolder<Item, MaidInteractItem> INVENTORY_LIST = item("inventory_list", MaidInteractItem::new);
    public static final DeferredHolder<Item, WrittenInvListItem> WRITTEN_INVENTORY_LIST = item("written_inventory_list", WrittenInvListItem::new);
    public static final DeferredHolder<Item, FilterListItem> FILTER_LIST = item("filter_list", FilterListItem::new);
    public static final DeferredHolder<Item, StorageDefineBauble> STORAGE_DEFINE_BAUBLE = item("storage_define_bauble", StorageDefineBauble::new);
    public static final DeferredHolder<Item, HangUpItem> NO_ACCESS = item("no_access", HangUpItem::new);
    public static final DeferredHolder<Item, HangUpItem> ALLOW_ACCESS = item("allow_access", HangUpItem::new);

    public static final DeferredHolder<Item, CraftGuide> CRAFT_GUIDE = item("craft_guide", CraftGuide::new);
    public static final DeferredHolder<Item, PortableCraftCalculatorBauble> PORTABLE_CRAFT_CALCULATOR_BAUBLE = item("portable_craft_calculator_bauble", PortableCraftCalculatorBauble::new);
    public static final DeferredHolder<Item, WorkCardItem> WORK_CARD = item("work_card", WorkCardItem::new);

    public static final DeferredHolder<Item, LogisticsGuide> LOGISTICS_GUIDE = item("logistics_guide", LogisticsGuide::new);
    public static final DeferredHolder<Item, ChangeFlag> CHANGE_FLAG = item("change_flag", ChangeFlag::new);

    public static final DeferredHolder<Item, ProgressPad> PROGRESS_PAD = item("progress_pad", ProgressPad::new);
}