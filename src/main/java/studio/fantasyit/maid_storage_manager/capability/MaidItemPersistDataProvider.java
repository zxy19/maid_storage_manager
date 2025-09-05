package studio.fantasyit.maid_storage_manager.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.Logger;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MaidItemPersistDataProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {

    public static final Capability<MaidPersistData> MAID_ITEM_PERSIST_DATA_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });
    public final MaidPersistData maidPersistData = new MaidPersistData();

    private final LazyOptional<MaidPersistData> opt = LazyOptional.of(() -> maidPersistData);

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return cap == MAID_ITEM_PERSIST_DATA_CAPABILITY ? opt.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        for (Map.Entry<UUID, Data> entry : maidPersistData.dataMap.entrySet()) {
            tag.put(entry.getKey().toString(), entry.getValue().toNbt());
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        for (String key : nbt.getAllKeys()) {
            try {
                UUID uuid = UUID.fromString(key);
                maidPersistData.set(uuid, Data.fromNbt(nbt.getCompound(key)));
            } catch (Exception e) {
                Logger.logger.error("In persist data", e);
            }
        }
    }

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

    public static class MaidPersistData {
        public Map<UUID, Data> dataMap = new ConcurrentHashMap<>();

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
    }

    public static MaidPersistData get(Level level) {
        return level.getCapability(MAID_ITEM_PERSIST_DATA_CAPABILITY).orElseThrow(() -> new RuntimeException("No maid item persist data capability"));
    }
}
