package studio.fantasyit.maid_storage_manager.maid.memory;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.mutable.MutableObject;
import studio.fantasyit.maid_storage_manager.storage.Storage;
import studio.fantasyit.maid_storage_manager.util.InvUtil;

import java.util.*;

public class ViewedInventoryMemory extends AbstractTargetMemory {

    public static class ItemCount {
        public static final Codec<ItemCount> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        ItemStack.CODEC.fieldOf("item").forGetter(ItemCount::getItem),
                        Codec.INT.fieldOf("count").forGetter(ItemCount::getCount)
                ).apply(instance, ItemCount::new)
        );
        public ItemStack item;
        public int count;

        public ItemCount(ItemStack item, int count) {
            this.item = item;
            this.count = count;
        }

        public ItemStack getItem() {
            return item;
        }

        public int getCount() {
            return count;
        }

        public ItemStack getFirst() {
            return item;
        }

        public int getSecond() {
            return count;
        }
    }

    public static final Codec<ViewedInventoryMemory> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    TargetData.CODEC.fieldOf("targetData")
                            .forGetter(ViewedInventoryMemory::getTargetData),
                    Codec.unboundedMap(
                                    Codec.STRING,
                                    Codec.unboundedMap(Codec.STRING,
                                            Codec.list(
                                                    ItemCount.CODEC
                                            )
                                    )
                            ).fieldOf("viewedInventory")
                            .forGetter(ViewedInventoryMemory::getViewedInventory),
                    Codec.INT.fieldOf("coolingDown")
                            .forGetter(ViewedInventoryMemory::getCoolingDown),
                    Storage.CODEC.listOf().fieldOf("mark_changed")
                            .forGetter(ViewedInventoryMemory::getMarkChanged)
            ).apply(instance, ViewedInventoryMemory::new)
    );
    public Map<String, Map<String, List<ItemCount>>> viewedInventory;
    private LinkedList<Storage> markChanged;
    public int coolingDown;

    public ViewedInventoryMemory(TargetData targetData,
                                 Map<String, Map<String, List<ItemCount>>> viewedInventory,
                                 int coolingDown,
                                 List<Storage> markChanged) {
        super(targetData);
        this.viewedInventory = new HashMap<>();
        for (Map.Entry<String, Map<String, List<ItemCount>>> entry : viewedInventory.entrySet()) {
            Map<String, List<ItemCount>> tmp = new HashMap<>();
            for (Map.Entry<String, List<ItemCount>> slot : entry.getValue().entrySet()) {
                tmp.put(slot.getKey(), new ArrayList<>(slot.getValue()));
            }
            this.viewedInventory.put(entry.getKey(), tmp);
        }
        this.coolingDown = coolingDown;
        this.markChanged = new LinkedList<>(markChanged);
    }

    public ViewedInventoryMemory() {
        super();
        viewedInventory = new HashMap<>();
        coolingDown = 0;
        markChanged = new LinkedList<>();
    }

    public int getCoolingDown() {
        return coolingDown;
    }

    public Map<String, Map<String, List<ItemCount>>> getViewedInventory() {
        return viewedInventory;
    }

    public Map<Storage, List<ItemCount>> positionFlatten() {
        Map<Storage, List<ItemCount>> result = new HashMap<>();
        for (Map.Entry<String, Map<String, List<ItemCount>>> blockEntry : viewedInventory.entrySet()) {
            List<ItemCount> itemCounts = new ArrayList<>();
            for (Map.Entry<String, List<ItemCount>> slot : blockEntry.getValue().entrySet()) {
                slot.getValue().forEach(itemCount -> {
                    boolean found = false;
                    for (int i = 0; i < itemCounts.size(); i++) {
                        if (ItemStack.isSameItemSameTags(itemCounts.get(i).getFirst(), itemCount.getFirst())) {
                            itemCounts.set(i, new ItemCount(itemCounts.get(i).getFirst(),
                                    itemCounts.get(i).getCount() + itemCount.getCount()));
                            found = true;
                            break;
                        }
                    }
                    if (!found)
                        itemCounts.add(itemCount);
                });
            }
            Storage pos = Storage.fromStoreString(blockEntry.getKey());
            if (pos != null) {
                result.put(pos, itemCounts);
            }
        }
        return result;
    }

    public List<Pair<ItemStack, Integer>> flatten() {
        List<Pair<ItemStack, Integer>> result = new ArrayList<>();
        for (Map.Entry<String, Map<String, List<ItemCount>>> blockEntry : viewedInventory.entrySet()) {
            for (Map.Entry<String, List<ItemCount>> slot : blockEntry.getValue().entrySet()) {
                for (ItemCount itemCount : slot.getValue()) {
                    if (itemCount.getFirst().isEmpty()) continue;
                    boolean found = false;
                    for (int i = 0; i < result.size(); i++) {
                        if (ItemStack.isSameItemSameTags(result.get(i).getFirst(), itemCount.getFirst())) {
                            result.set(i, new Pair<>(result.get(i).getFirst(),
                                    result.get(i).getSecond() + itemCount.getSecond()));
                            found = true;
                            break;
                        }
                    }
                    if (!found)
                        result.add(new Pair<>(itemCount.getItem(), itemCount.getCount()));
                }
            }
        }
        return result;
    }

    public void ambitiousRemoveItem(ServerLevel level, Storage target, ItemStack itemStack, int count) {
        MutableObject<Storage> realTarget = new MutableObject<>(target);
        InvUtil.checkNearByContainers(level, target.getPos(), pos -> {
            Storage m = target.sameType(pos, null);
            if (viewedInventory.containsKey(m.toStoreString()))
                realTarget.setValue(m);
        });
        removeItem(realTarget.getValue(), itemStack, count);
    }

    public void ambitiousAddItem(ServerLevel level, Storage target, ItemStack itemStack) {
        MutableObject<Storage> realTarget = new MutableObject<>(target);
        InvUtil.checkNearByContainers(level, target.getPos(), pos -> {
            Storage m = target.sameType(pos, null);
            if (viewedInventory.containsKey(m.toStoreString()))
                realTarget.setValue(m);
        });
        addItem(realTarget.getValue(), itemStack);
    }

    public void removeItem(Storage pos, ItemStack itemStack, int count) {
        if (pos == null || itemStack == null || itemStack.isEmpty()) return;
        if (!viewedInventory.containsKey(pos.toStoreString()))
            return;
        Map<String, List<ItemCount>> map = viewedInventory.get(pos.toStoreString());

        String itemKey = Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(itemStack.getItem())).toString();
        List<ItemCount> list = map.getOrDefault(itemKey, new ArrayList<>());
        for (int i = 0; i < list.size(); i++) {
            ItemCount itemCount = list.get(i);
            if (ItemStack.isSameItemSameTags(itemCount.getFirst(), itemStack)) {
                list.set(i, new ItemCount(itemStack, itemCount.getSecond() - count));
                if (itemCount.getSecond() - count <= 0)
                    list.remove(i);
                break;
            }
        }
        map.put(itemKey, list);
        viewedInventory.put(pos.toStoreString(), map);
    }

    public void addItem(Storage pos, ItemStack itemStack) {
        if (pos == null || itemStack == null || itemStack.isEmpty()) return;
        if (!viewedInventory.containsKey(pos.toStoreString()))
            viewedInventory.put(pos.toStoreString(), new HashMap<>());
        Map<String, List<ItemCount>> map = viewedInventory.get(pos.toStoreString());

        String itemKey = Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(itemStack.getItem())).toString();
        List<ItemCount> list = map.getOrDefault(itemKey, new ArrayList<>());
        boolean found = false;
        for (int i = 0; i < list.size(); i++) {
            ItemCount itemCount = list.get(i);
            if (ItemStack.isSameItemSameTags(itemCount.getFirst(), itemStack)) {
                list.set(i, new ItemCount(itemStack, itemCount.getSecond() + itemStack.getCount()));
                found = true;
                break;
            }
        }
        if (!found)
            list.add(new ItemCount(itemStack.copyWithCount(1), itemStack.getCount()));
        map.put(itemKey, list);
        viewedInventory.put(pos.toStoreString(), map);
    }

    public void removeUnvisited() {
        ArrayList<String> posList = new ArrayList<>(viewedInventory.keySet());
        for (String pos : posList) {
            Storage storage = Storage.fromStoreString(pos);
            if (storage == null || !isVisitedPos(storage))
                viewedInventory.remove(pos);
        }
    }

    public void resetViewedInvForPos(Storage pos) {
        viewedInventory.remove(pos.toStoreString());
        viewedInventory.put(pos.toStoreString(), new HashMap<>());
    }

    public LinkedList<Storage> getMarkChanged() {
        return markChanged;
    }

    public void addMarkChanged(Storage pos) {
        if (!markChanged.contains(pos))
            markChanged.add(pos);
    }

    int failTime = 0;

    public void markFailTime() {
        if (markChanged.isEmpty()) return;
        if (failTime++ > 3) {
            markChanged.poll();
        }
    }

}