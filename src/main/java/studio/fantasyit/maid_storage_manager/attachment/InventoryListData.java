package studio.fantasyit.maid_storage_manager.attachment;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.network.PacketDistributor;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.data.InventoryItem;
import studio.fantasyit.maid_storage_manager.network.PartialInventoryListData;
import studio.fantasyit.maid_storage_manager.registry.DataAttachmentRegistry;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class InventoryListData implements INBTSerializable<CompoundTag> {
    public static InventoryListData get(Level world) {
        return world.getData(DataAttachmentRegistry.INVENTORY_LIST_DATA);
    }

    public Map<UUID, Map<String, List<InventoryItem>>> dataMap = new ConcurrentHashMap<>();

    public void remove(UUID uuid) {
        dataMap.remove(uuid);
    }

    public void set(HolderLookup.Provider provider, UUID uuid, ListTag listTag) {
        ConcurrentHashMap<String, List<InventoryItem>> map = new ConcurrentHashMap<>();
        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag tag = listTag.getCompound(i);
            String key = tag.getString("key");
            ListTag itemList = tag.getList("items", ListTag.TAG_COMPOUND);
            List<InventoryItem> items = new ArrayList<>();
            for (int j = 0; j < itemList.size(); j++) {
                items.add(InventoryItem.fromNbt(provider, itemList.getCompound(j)));
            }
            map.put(key, items);
        }
        dataMap.put(uuid, map);
    }


    public void addWithCraftable(RegistryAccess provider, UUID uuid, List<InventoryItem> flatten) {
        set(provider, uuid, flatten);
        addAllMissingCraftable(provider, uuid, flatten);
    }

    public void set(RegistryAccess provider, UUID uuid, List<InventoryItem> list) {
        if (!dataMap.containsKey(uuid)) {
            dataMap.put(uuid, new ConcurrentHashMap<>());
        }
        Registry<Item> reg = provider.registryOrThrow(Registries.ITEM);
        for (InventoryItem pair : list) {
            ItemStack item = pair.itemStack;
            String key = String.valueOf(reg.getKey(item.getItem()));
            if (!dataMap.get(uuid).containsKey(key)) {
                dataMap.get(uuid).put(key, new ArrayList<>());
            }
            dataMap.get(uuid).get(key).add(pair);
        }
    }

    public void addAllMissingCraftable(RegistryAccess provider, UUID uuid, List<InventoryItem> list) {
        if (!dataMap.containsKey(uuid)) {
            dataMap.put(uuid, new ConcurrentHashMap<>());
        }
        Registry<Item> reg = provider.registryOrThrow(Registries.ITEM);
        for (InventoryItem existingItem : list) {
            if (existingItem.itemStack.is(ItemRegistry.CRAFT_GUIDE.get())) {
                CraftGuideData cgd = CraftGuideData.fromItemStack(existingItem.itemStack);
                if (cgd.available()) {
                    cgd.getOutput().forEach(itemStack -> {
                        String key = String.valueOf(reg.getKey(itemStack.getItem()));
                        if (!dataMap.get(uuid).containsKey(key)) {
                            dataMap.get(uuid).put(key, new ArrayList<>());
                        }
                        List<InventoryItem> matches = dataMap.get(uuid).get(key).stream().filter(
                                p -> ItemStackUtil.isSameInCrafting(p.itemStack, itemStack)
                                        //目标不能是当前物品
                                        && !ItemStackUtil.isSame(p.itemStack, existingItem.itemStack, true)
                        ).toList();
                        if (!matches.isEmpty()) {
                            existingItem.posAndSlot.forEach(p -> matches.forEach(p1 -> p1.addCraftGuidePos(p.pos())));
                        } else {
                            InventoryItem inventoryItem = new InventoryItem(itemStack, 0);
                            existingItem.posAndSlot.forEach(p -> inventoryItem.addCraftGuidePos(p.pos()));
                            dataMap.get(uuid).get(key).add(inventoryItem);
                        }
                    });
                }
            }

        }
    }

    public ListTag get(HolderLookup.Provider access, UUID uuid) {
        if (!dataMap.containsKey(uuid))
            return new ListTag();
        ListTag listTag = new ListTag();
        dataMap.get(uuid).entrySet().stream().forEach(entry -> {
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.putString("key", entry.getKey());
            ListTag sameItem = new ListTag();
            for (InventoryItem inventoryItem : entry.getValue()) {
                sameItem.add(inventoryItem.serializeNBT(access));
            }
            compoundTag.put("items", sameItem);
            listTag.add(compoundTag);
        });
        return listTag;
    }

    public void sendTo(UUID key, ServerPlayer sender) {
        if (!dataMap.containsKey(key)) return;
        Set<Map.Entry<String, List<InventoryItem>>> keys = dataMap.get(key).entrySet();
        List<InventoryItem> list = new ArrayList<>();
        for (Map.Entry<String, List<InventoryItem>> entry : keys) {
            for (InventoryItem pair : entry.getValue()) {
                list.add(pair);
                if (list.size() >= 10) {
                    PacketDistributor.sendToPlayer(sender,
                            new PartialInventoryListData(key, list)
                    );
                    list = new ArrayList<>();
                }
            }
        }
        if (!list.isEmpty()) {
            PacketDistributor.sendToPlayer(sender,
                    new PartialInventoryListData(key, list)
            );
        }
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider var1) {
        CompoundTag tag = new CompoundTag();
        for (Map.Entry<UUID, Map<String, List<InventoryItem>>> entry : dataMap.entrySet()) {
            ListTag listTag = get(var1, entry.getKey());
            tag.put(entry.getKey().toString(), listTag);
        }
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider var1, CompoundTag nbt) {
        for (String key : nbt.getAllKeys()) {
            try {
                UUID uuid = UUID.fromString(key);
                ListTag listTag = nbt.getList(key, ListTag.TAG_COMPOUND);
                set(var1, uuid, listTag);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
