package studio.fantasyit.maid_storage_manager.maid.memory;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.data.InventoryItem;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;
import studio.fantasyit.maid_storage_manager.util.StorageAccessUtil;

import java.util.*;
import java.util.function.Predicate;

public class ViewedInventoryMemory extends AbstractTargetMemory {

    public record ItemCount(ItemStack item, int count) {
        public static final Codec<ItemCount> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        ItemStack.CODEC.fieldOf("item").forGetter(ItemCount::item),
                        Codec.INT.fieldOf("count").forGetter(ItemCount::count)
                ).apply(instance, ItemCount::new)
        );

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
                    Target.CODEC.listOf().fieldOf("mark_changed")
                            .forGetter(ViewedInventoryMemory::getMarkChanged)
            ).apply(instance, ViewedInventoryMemory::new)
    );
    public Map<String, Map<String, List<ItemCount>>> viewedInventory;
    private LinkedList<Target> markChanged;
    public int coolingDown;

    public ViewedInventoryMemory(TargetData targetData,
                                 Map<String, Map<String, List<ItemCount>>> viewedInventory,
                                 int coolingDown,
                                 List<Target> markChanged) {
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

    public Map<Target, List<ItemCount>> positionFlatten() {
        Map<Target, List<ItemCount>> result = new HashMap<>();
        for (Map.Entry<String, Map<String, List<ItemCount>>> blockEntry : viewedInventory.entrySet()) {
            List<ItemCount> itemCounts = new ArrayList<>();
            for (Map.Entry<String, List<ItemCount>> slot : blockEntry.getValue().entrySet()) {
                slot.getValue().forEach(itemCount -> {
                    boolean found = false;
                    for (int i = 0; i < itemCounts.size(); i++) {
                        if (ItemStack.isSameItemSameTags(itemCounts.get(i).getFirst(), itemCount.getFirst())) {
                            itemCounts.set(i, new ItemCount(itemCounts.get(i).getFirst(),
                                    itemCounts.get(i).count() + itemCount.count()));
                            found = true;
                            break;
                        }
                    }
                    if (!found)
                        itemCounts.add(itemCount);
                });
            }
            Target pos = Target.fromStoreString(blockEntry.getKey());
            if (pos != null) {
                result.put(pos, itemCounts);
            }
        }
        return result;
    }

    public List<ItemCount> getItemsAt(Target target) {
        return viewedInventory.getOrDefault(target.toStoreString(), Collections.emptyMap())
                .values().stream().flatMap(List::stream).toList();
    }

    public Map<String, List<ItemCount>> getItemsAtInternal(Target target) {
        return viewedInventory.getOrDefault(target.toStoreString(), Collections.emptyMap());
    }

    public void setItemsAtInternal(Target target, Map<String, List<ItemCount>> items) {
        Map<String, List<ItemCount>> itemsCopy = new HashMap<>();
        items.forEach((key, value) -> itemsCopy.put(key, new ArrayList<>(value)));
        viewedInventory.put(target.toStoreString(), itemsCopy);
    }

    public List<InventoryItem> flatten() {
        List<InventoryItem> result = new ArrayList<>();
        for (Map.Entry<String, Map<String, List<ItemCount>>> blockEntry : viewedInventory.entrySet()) {
            @Nullable Target pos = Target.fromStoreString(blockEntry.getKey());
            for (Map.Entry<String, List<ItemCount>> slot : blockEntry.getValue().entrySet()) {
                for (ItemCount itemCount : slot.getValue()) {
                    if (itemCount.getFirst().isEmpty()) continue;
                    boolean found = false;
                    for (int i = 0; i < result.size(); i++) {
                        if (ItemStack.isSameItemSameTags(result.get(i).itemStack, itemCount.getFirst())) {
                            result.get(i).addCount(pos, itemCount.getSecond());
                            found = true;
                            break;
                        }
                    }
                    if (!found)
                        result.add(new InventoryItem(itemCount.item(),
                                        itemCount.count(),
                                        new ArrayList<>(List.of(new InventoryItem.PositionCount(pos, itemCount.getSecond(), false)))
                                )
                        );
                }
            }
        }
        return result;
    }

    public void ambitiousRemoveItem(ServerLevel level, Target target, ItemStack itemStack, int count) {
        Target realTarget = ambitiousPos(level, target);
        removeItem(realTarget, itemStack, count);
    }

    public void ambitiousAddItem(ServerLevel level, Target target, ItemStack itemStack) {
        Target realTarget = ambitiousPos(level, target);
        addItem(realTarget, itemStack);
    }

    public void removeItem(Target pos, ItemStack itemStack, int count) {
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

    public void addItem(Target pos, ItemStack itemStack) {
        addItem(pos, itemStack, itemStack.getCount());
    }

    public void addItem(Target pos, ItemStack itemStack, int count) {
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
                list.set(i, new ItemCount(itemStack, (int) Math.min((long) itemCount.getSecond() + (long) count, Integer.MAX_VALUE / 2)));
                found = true;
                break;
            }
        }
        if (!found)
            list.add(new ItemCount(itemStack.copyWithCount(1), Math.min(count, Integer.MAX_VALUE / 2)));
        map.put(itemKey, list);
        viewedInventory.put(pos.toStoreString(), map);
    }

    public void removeUnvisited() {
        ArrayList<String> posList = new ArrayList<>(viewedInventory.keySet());
        for (String pos : posList) {
            Target storage = Target.fromStoreString(pos);
            if (storage == null || !isVisitedPos(storage))
                viewedInventory.remove(pos);
        }
    }

    public void resetViewedInvForPosAsRemoved(Target pos) {
        viewedInventory.remove(pos.toStoreString());
    }

    public void resetViewedInvForPos(Target pos) {
        viewedInventory.remove(pos.toStoreString());
        viewedInventory.put(pos.toStoreString(), new HashMap<>());
    }

    public LinkedList<Target> getMarkChanged() {
        return markChanged;
    }

    public void addMarkChanged(Target pos) {
        if (!markChanged.contains(pos))
            markChanged.add(pos);
    }

    public Target ambitiousPos(ServerLevel level, Target storage) {
        if (viewedInventory.containsKey(storage.toStoreString()))
            return storage;
        MutableObject<Target> realTarget = new MutableObject<>(storage);
        StorageAccessUtil.checkNearByContainers(level, storage.getPos(), pos -> {
            Target m = storage.sameType(pos, null);
            if (viewedInventory.containsKey(m.toStoreString()))
                realTarget.setValue(m);
        });
        return realTarget.getValue();
    }

    public void removeItemFromAllTargets(ItemStack itemStack, Predicate<ItemStack> predicate) {
        for (String pos : viewedInventory.keySet()) {
            Map<String, List<ItemCount>> vi = viewedInventory.get(pos);
            for (String item : vi.keySet()) {
                vi.get(item).removeIf(itemCount -> ItemStackUtil.isSame(itemStack, itemCount.getFirst(), false) && predicate.test(itemCount.getFirst()));
            }
            Set<String> ks = new HashSet<>(vi.keySet());
            for (String k : ks) {
                if (vi.get(k).isEmpty())
                    vi.remove(k);
            }
        }
        Set<String> ks = new HashSet<>(viewedInventory.keySet());
        for (String k : ks) {
            if (viewedInventory.get(k).isEmpty())
                viewedInventory.remove(k);
        }
    }

    int failTime = 0;

    public void resetMarkFailTime() {
        failTime = 0;
    }

    public void markFailTime() {
        if (markChanged.isEmpty()) return;
        if (failTime++ > 3) {
            markChanged.poll();
        }
    }

    public void receiveFrom(ViewedInventoryMemory memory) {
        resetVisitedPos();
        removeUnvisited();
        for (String pos : memory.viewedInventory.keySet()) {
            Map<String, List<ItemCount>> data = new HashMap<>();
            for (String item : memory.viewedInventory.get(pos).keySet()) {
                data.put(item, new ArrayList<>(memory.viewedInventory.get(pos).get(item)));
            }
            viewedInventory.put(pos, data);
        }
    }
}