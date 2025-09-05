package studio.fantasyit.maid_storage_manager.attachment;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.UnknownNullability;
import studio.fantasyit.maid_storage_manager.Logger;
import studio.fantasyit.maid_storage_manager.registry.DataAttachmentRegistry;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MaidItemPersistData implements INBTSerializable<CompoundTag> {
    public record Data(CompoundTag inventoryMemory) {
        public CompoundTag toNbt() {
            CompoundTag tag = new CompoundTag();
            tag.put("inventoryMemory", inventoryMemory);
            return tag;
        }

        public static Data fromNbt(CompoundTag tag) {
            return new Data(
                    tag.getCompound("inventoryMemory")
            );
        }
    }

    public Map<UUID, Data> dataMap = new ConcurrentHashMap<>();

    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        for (Map.Entry<UUID, Data> entry : dataMap.entrySet()) {
            tag.put(entry.getKey().toString(), entry.getValue().toNbt());
        }
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        for (String key : nbt.getAllKeys()) {
            try {
                UUID uuid = UUID.fromString(key);
                set(uuid, Data.fromNbt(nbt.getCompound(key)));
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
