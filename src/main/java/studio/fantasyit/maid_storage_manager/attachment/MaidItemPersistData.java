package studio.fantasyit.maid_storage_manager.attachment;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import studio.fantasyit.maid_storage_manager.Logger;
import studio.fantasyit.maid_storage_manager.registry.DataAttachmentRegistry;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MaidItemPersistData implements ValueIOSerializable {
    public record Data(CompoundTag inventoryMemory) {
        public CompoundTag toNbt() {
            CompoundTag tag = new CompoundTag();
            tag.put("inventoryMemory", inventoryMemory);
            return tag;
        }

        public static Data fromNbt(CompoundTag tag) {
            return new Data(
                    tag.getCompound("inventoryMemory").get()
            );
        }
    }

    public Map<UUID, Data> dataMap = new ConcurrentHashMap<>();

    @Override
    public void serialize(ValueOutput output) {
        for (Map.Entry<UUID, Data> entry : dataMap.entrySet()) {
            output.store(entry.getKey().toString(), CompoundTag.CODEC, entry.getValue().toNbt());
        }
    }

    @Override
    public void deserialize(ValueInput input) {
        for (String key : input.keySet()) {
            try {
                UUID uuid = UUID.fromString(key);
                input.read(key, CompoundTag.CODEC).ifPresent(tag -> {
                    set(uuid, Data.fromNbt(tag));
                });
            } catch (Exception e) {
                Logger.logger.error("In persist data", e);
            }
        }
    }


    public void remove(UUID uuid) {
        dataMap.remove(uuid);
    }

    public void set(UUID uuid, Data data) {
        dataMap.put(uuid, data);
    }

    public Optional<Data> get(UUID uuid) {
        if (!dataMap.containsKey(uuid))
            return Optional.empty();
        return Optional.of(dataMap.get(uuid));
    }

    public Optional<Data> getAndRemove(UUID uuid) {
        return Optional.ofNullable(dataMap.remove(uuid));
    }

    public static MaidItemPersistData get(Level world) {
        return world.getData(DataAttachmentRegistry.MAID_ITEM_PERSIST_DATA);
    }
}
