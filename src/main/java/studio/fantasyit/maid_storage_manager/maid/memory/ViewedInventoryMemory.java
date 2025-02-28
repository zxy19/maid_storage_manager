package studio.fantasyit.maid_storage_manager.maid.memory;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;

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
                            .forGetter(ViewedInventoryMemory::getCoolingDown)
            ).apply(instance, ViewedInventoryMemory::new)
    );
    public Map<String, Map<String, List<ItemCount>>> viewedInventory;
    public int coolingDown;

    public ViewedInventoryMemory(TargetData targetData,
                                 Map<String, Map<String, List<ItemCount>>> viewedInventory,
                                 int coolingDown) {
        super(targetData);
        this.viewedInventory = new HashMap<>(viewedInventory);
        this.coolingDown = coolingDown;
    }

    public ViewedInventoryMemory() {
        super();
        viewedInventory = new HashMap<>();
        coolingDown = 0;
    }

    public int getCoolingDown() {
        return coolingDown;
    }

    public Map<String, Map<String, List<ItemCount>>> getViewedInventory() {
        return viewedInventory;
    }

    public Map<BlockPos, List<ItemCount>> positionFlatten() {
        Map<BlockPos, List<ItemCount>> result = new HashMap<>();
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
            String[] posAxis = blockEntry.getKey().split(", ");
            if (posAxis.length == 3) {
                BlockPos blockPos = new BlockPos(Integer.parseInt(posAxis[0]),
                        Integer.parseInt(posAxis[1]),
                        Integer.parseInt(posAxis[2]));
                result.put(blockPos, itemCounts);
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


    public void addItem(BlockPos pos, ItemStack itemStack) {
        if (pos == null || itemStack == null || itemStack.isEmpty()) return;
        if (!viewedInventory.containsKey(pos.toShortString()))
            viewedInventory.put(pos.toShortString(), new HashMap<>());
        Map<String, List<ItemCount>> map = viewedInventory.get(pos.toShortString());

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
        viewedInventory.put(pos.toShortString(), map);
    }

    public void removeUnvisited() {
        ArrayList<String> posList = new ArrayList<>(viewedInventory.keySet());
        for (String pos : posList) {
            String[] posAxis = pos.split(", ");
            if (posAxis.length == 3) {
                BlockPos blockPos = new BlockPos(Integer.parseInt(posAxis[0]),
                        Integer.parseInt(posAxis[1]),
                        Integer.parseInt(posAxis[2]));

                if (!isVisitedPos(blockPos))
                    viewedInventory.remove(pos);
            }
        }
    }

    public void resetViewedInvForPos(BlockPos pos) {
        viewedInventory.remove(pos.toShortString());
        viewedInventory.put(pos.toShortString(), new HashMap<>());
    }
}
