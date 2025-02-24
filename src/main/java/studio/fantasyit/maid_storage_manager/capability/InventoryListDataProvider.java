package studio.fantasyit.maid_storage_manager.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.network.Network;
import studio.fantasyit.maid_storage_manager.network.PartialInventoryListData;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class InventoryListDataProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {

    public static final Capability<InventoryListData> INVENTORY_LIST_DATA_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });
    public final InventoryListData inventoryListData = new InventoryListData();

    private final LazyOptional<InventoryListData> opt = LazyOptional.of(() -> inventoryListData);

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return cap == INVENTORY_LIST_DATA_CAPABILITY ? opt.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        for (Map.Entry<UUID, Map<String, Integer>> entry : inventoryListData.dataMap.entrySet()) {
            ListTag listTag = inventoryListData.get(entry.getKey());
            tag.put(entry.getKey().toString(), listTag);
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        for (String key : nbt.getAllKeys()) {
            try {
                UUID uuid = UUID.fromString(key);
                ListTag listTag = nbt.getList(key, ListTag.TAG_COMPOUND);
                inventoryListData.set(uuid, listTag);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static class InventoryListData {
        public Map<UUID, Map<String, Integer>> dataMap = new ConcurrentHashMap<>();

        public void set(UUID uuid, ListTag listTag) {
            ConcurrentHashMap<String, Integer> tmp = new ConcurrentHashMap<>();
            for (int i = 0; i < listTag.size(); i++) {
                CompoundTag tag = listTag.getCompound(i);
                tmp.put(tag.getString("item"), tag.getInt("count"));
            }
            dataMap.put(uuid, tmp);
        }

        public void set(UUID uuid, Map<String, Integer> map) {
            dataMap.put(uuid, map);
        }

        public ListTag get(UUID uuid) {
            if (!dataMap.containsKey(uuid))
                return new ListTag();
            ListTag listTag = new ListTag();
            dataMap.get(uuid).entrySet().stream().forEach(entry -> {
                CompoundTag compoundTag = new CompoundTag();
                compoundTag.putString("item", entry.getKey());
                compoundTag.putInt("count", entry.getValue());
                listTag.add(compoundTag);
            });
            return listTag;
        }

        public void sendTo(UUID key, ServerPlayer sender) {
            if (!dataMap.containsKey(key)) return;
            Set<Map.Entry<String, Integer>> keys = dataMap.get(key).entrySet();
            List<Pair<String, Integer>> list = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : keys) {
                list.add(new Pair<>(entry.getKey(), entry.getValue()));
                if (list.size() >= 10) {
                    Network.INSTANCE.send(PacketDistributor.PLAYER.with(() -> sender),
                            new PartialInventoryListData(key, list)
                    );
                    list = new ArrayList<>();
                }
            }
            if (!list.isEmpty()) {
                Network.INSTANCE.send(PacketDistributor.PLAYER.with(() -> sender),
                        new PartialInventoryListData(key, list)
                );
            }
        }
    }
}
