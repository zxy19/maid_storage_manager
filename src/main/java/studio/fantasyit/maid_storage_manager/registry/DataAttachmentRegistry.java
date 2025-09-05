package studio.fantasyit.maid_storage_manager.registry;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.attachment.CraftBlockOccupy;
import studio.fantasyit.maid_storage_manager.attachment.InventoryListData;
import studio.fantasyit.maid_storage_manager.attachment.MaidItemPersistData;

import java.util.function.Supplier;

public class DataAttachmentRegistry {
    // Create the DeferredRegister for attachment types
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, MaidStorageManager.MODID);


    public static final Supplier<AttachmentType<CraftBlockOccupy>> CRAFT_BLOCK_OCCUPY = ATTACHMENT_TYPES.register(
            "craft_block_occupy", () -> AttachmentType.serializable(CraftBlockOccupy::new).build());
    public static final Supplier<AttachmentType<InventoryListData>> INVENTORY_LIST_DATA = ATTACHMENT_TYPES.register(
            "inventory_list_data", () -> AttachmentType.serializable(InventoryListData::new).build());
    public static final Supplier<AttachmentType<MaidItemPersistData>> MAID_ITEM_PERSIST_DATA = ATTACHMENT_TYPES.register(
            "maid_item_persist_data", () -> AttachmentType.serializable(MaidItemPersistData::new).build());

    public static void register(IEventBus bus) {
        ATTACHMENT_TYPES.register(bus);
    }
}
