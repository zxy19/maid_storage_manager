package studio.fantasyit.maid_storage_manager.event;

import com.github.tartaricacid.touhoulittlemaid.api.event.MaidAndItemTransformEvent;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.capability.MaidItemPersistDataProvider;
import studio.fantasyit.maid_storage_manager.maid.task.StorageManageTask;

import java.util.Optional;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = MaidStorageManager.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MaidItemDataEvent {
    @SubscribeEvent
    public static void onMaidToItem(MaidAndItemTransformEvent.ToItem event) {
        if (event.getMaid().level() instanceof ServerLevel sl) {
            EntityMaid maid = event.getMaid();
            CompoundTag data = event.getData();
            if (maid.getTask().getUid().equals(StorageManageTask.TASK_ID)) {
                CompoundTag brain = data.getCompound("Brain");
                if (!brain.contains("memories")) return;
                CompoundTag memories = brain.getCompound("memories");
                MaidItemPersistDataProvider.Data d = new MaidItemPersistDataProvider.Data(
                        memories.getCompound("maid_storage_manager:viewed_inventory")
                );
                memories.remove("maid_storage_manager:viewed_inventory");
                UUID uuid = UUID.randomUUID();
                MaidItemPersistDataProvider.get(sl.getServer().overworld()).set(uuid, d);
                data.putUUID("maid_storage_manager:persist_uuid", uuid);
            }
        }
    }

    @SubscribeEvent
    public static void onItemToMaid(MaidAndItemTransformEvent.ToMaid event) {
        if (event.getMaid().level() instanceof ServerLevel sl) {
            CompoundTag data = event.getData();
            if (data.contains("maid_storage_manager:persist_uuid")) {
                UUID uuid = data.getUUID("maid_storage_manager:persist_uuid");
                Optional<MaidItemPersistDataProvider.Data> d = MaidItemPersistDataProvider.get(sl.getServer().overworld()).getAndRemove(uuid);
                d.ifPresent(d2 -> {
                    CompoundTag brain = data.getCompound("Brain");
                    CompoundTag memories = brain.getCompound("memories");
                    memories.put("maid_storage_manager:viewed_inventory", d2.inventoryMemory());
                });
            }
        }
    }
}
