package studio.fantasyit.maid_storage_manager.capability;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
        for (Map.Entry<UUID, Map<String, List<Pair<ItemStack, Integer>>>> entry : inventoryListData.dataMap.entrySet()) {
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
        public Map<UUID, Map<String, List<Pair<ItemStack, Integer>>>> dataMap = new ConcurrentHashMap<>();

        public void set(UUID uuid, ListTag listTag) {
            ConcurrentHashMap<String, List<Pair<ItemStack, Integer>>> map = new ConcurrentHashMap<>();
            for (int i = 0; i < listTag.size(); i++) {
                CompoundTag tag = listTag.getCompound(i);
                String key = tag.getString("key");
                ListTag sameItem = tag.getList("items", ListTag.TAG_COMPOUND);
                List<Pair<ItemStack, Integer>> items = new ArrayList<>();
                for (int j = 0; j < sameItem.size(); j++) {
                    CompoundTag tmp = sameItem.getCompound(j);
                    ItemStack item = ItemStack.of(tmp.getCompound("item"));
                    int count = tmp.getInt("count");
                    items.add(new Pair<>(item, count));
                }
                map.put(key, items);
            }
            dataMap.put(uuid, map);
        }

        public void set(UUID uuid, List<Pair<ItemStack, Integer>> list) {
            for(Pair<ItemStack, Integer> pair : list){
                ItemStack item = pair.getFirst();
                String key = String.valueOf(ForgeRegistries.ITEMS.getKey(item.getItem()));
                if (!dataMap.containsKey(uuid)) {
                    dataMap.put(uuid, new ConcurrentHashMap<>());
                }
                if (!dataMap.get(uuid).containsKey(key)) {
                    dataMap.get(uuid).put(key, new ArrayList<>());
                }
                dataMap.get(uuid).get(key).add(pair);
            }
        }

        public ListTag get(UUID uuid) {
            if (!dataMap.containsKey(uuid))
                return new ListTag();
            ListTag listTag = new ListTag();
            dataMap.get(uuid).entrySet().stream().forEach(entry -> {
                CompoundTag compoundTag = new CompoundTag();
                compoundTag.putString("key", entry.getKey());
                ListTag sameItem = new ListTag();
                for (Pair<ItemStack, Integer> pair : entry.getValue()) {
                    CompoundTag tmp = new CompoundTag();
                    tmp.put("item", pair.getFirst().save(new CompoundTag()));
                    tmp.putInt("count", pair.getSecond());
                    sameItem.add(tmp);
                }
                compoundTag.put("items", sameItem);
                listTag.add(compoundTag);
            });
            return listTag;
        }

        public void sendTo(UUID key, ServerPlayer sender) {
            if (!dataMap.containsKey(key)) return;
            Set<Map.Entry<String, List<Pair<ItemStack, Integer>>>> keys = dataMap.get(key).entrySet();
            List<Pair<ItemStack, Integer>> list = new ArrayList<>();
            for (Map.Entry<String, List<Pair<ItemStack, Integer>>> entry : keys) {
                for (Pair<ItemStack, Integer> pair : entry.getValue()) {
                    list.add(pair);
                    if (list.size() >= 10) {
                        Network.INSTANCE.send(PacketDistributor.PLAYER.with(() -> sender),
                                new PartialInventoryListData(key, list)
                        );
                        list = new ArrayList<>();
                    }
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
