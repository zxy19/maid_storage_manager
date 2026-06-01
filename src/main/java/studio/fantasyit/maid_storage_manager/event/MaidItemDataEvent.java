package studio.fantasyit.maid_storage_manager.event;

import com.github.tartaricacid.touhoulittlemaid.api.event.MaidAndItemTransformEvent;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.attachment.MaidItemPersistData;
import studio.fantasyit.maid_storage_manager.maid.task.StorageManageTask;

import java.util.Optional;
import java.util.UUID;

@EventBusSubscriber(modid = MaidStorageManager.MODID)
public class MaidItemDataEvent {
    @SubscribeEvent
    public static void onMaidToItem(MaidAndItemTransformEvent.ToItem event) {
        if (event.getMaid().level() instanceof ServerLevel sl) {
            EntityMaid maid = event.getMaid();
            CompoundTag data = event.getData();
            if (maid.getTask().getUid().equals(StorageManageTask.TASK_ID)) {
                CompoundTag brain = data.getCompoundOrEmpty("Brain");
                if (!brain.contains("memories")) return;
                CompoundTag memories = brain.getCompoundOrEmpty("memories");
                MaidItemPersistData.Data d = new MaidItemPersistData.Data(
                        memories.getCompoundOrEmpty("maid_storage_manager:viewed_inventory")
                );
                memories.remove("maid_storage_manager:viewed_inventory");
                UUID uuid = UUID.randomUUID();
                MaidItemPersistData.get(sl.getServer().overworld()).set(uuid, d);
                data.store("maid_storage_manager:persist_uuid", UUIDUtil.CODEC, uuid);
            }
        }
    }

    @SubscribeEvent
    public static void onItemToMaid(MaidAndItemTransformEvent.ToMaid event) {
        if (event.getMaid().level() instanceof ServerLevel sl) {
            CompoundTag data = event.getData();
            if (data.contains("maid_storage_manager:persist_uuid")) {
                UUID uuid = data.read("maid_storage_manager:persist_uuid", UUIDUtil.CODEC).orElse(null);
                Optional<MaidItemPersistData.Data> d = MaidItemPersistData.get(sl.getServer().overworld()).getAndRemove(uuid);
                d.ifPresent(d2 -> {
                    CompoundTag brain = data.getCompoundOrEmpty("Brain");
                    CompoundTag memories = brain.getCompoundOrEmpty("memories");
                    memories.put("maid_storage_manager:viewed_inventory", d2.inventoryMemory());
                });
            }
        }
    }
}
