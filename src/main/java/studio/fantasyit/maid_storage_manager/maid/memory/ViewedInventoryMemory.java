package studio.fantasyit.maid_storage_manager.maid.memory;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public class ViewedInventoryMemory {
    public static final Codec<ViewedInventoryMemory> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.unboundedMap(
                                    Codec.STRING,
                                    Codec.unboundedMap(Codec.STRING,
                                            Codec.INT
                                    )
                            ).fieldOf("viewedInventory")
                            .forGetter(ViewedInventoryMemory::getViewedInventory),
                    Codec.list(BlockPos.CODEC)
                            .fieldOf("visitedPos")
                            .forGetter(ViewedInventoryMemory::getVisitedPos)
            ).apply(instance, ViewedInventoryMemory::new)
    );
    public Map<String, Map<String, Integer>> viewedInventory;
    public List<BlockPos> visitedPos;

    public ViewedInventoryMemory(Map<String, Map<String, Integer>> viewedInventory, List<BlockPos> visitedPos) {
        this.viewedInventory = new HashMap<>(viewedInventory);
        this.visitedPos = new ArrayList<>(visitedPos);
    }

    public ViewedInventoryMemory() {
        viewedInventory = new HashMap<>();
        visitedPos = new ArrayList<>();
    }

    public Map<String, Map<String, Integer>> getViewedInventory() {
        return viewedInventory;
    }

    public Map<String, Integer> flatten() {
        Map<String, Integer> result = new HashMap<>();
        for (Map.Entry<String, Map<String, Integer>> blockEntry : viewedInventory.entrySet()) {
            for (Map.Entry<String, Integer> slot : blockEntry.getValue().entrySet()) {
                result.put(slot.getKey(), slot.getValue() + result.getOrDefault(slot.getKey(), 0));
            }
        }
        return result;
    }

    public List<BlockPos> getVisitedPos() {
        return visitedPos;
    }

    public void addItem(BlockPos pos, ItemStack itemStack) {
        if (pos == null || itemStack == null || itemStack.isEmpty()) return;
        if (!viewedInventory.containsKey(pos.toShortString()))
            viewedInventory.put(pos.toShortString(), new HashMap<>());
        Map<String, Integer> map = viewedInventory.get(pos.toShortString());

        String itemKey = Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(itemStack.getItem())).toString();
        if (!map.containsKey(itemKey)) {
            map.put(itemKey, itemStack.getCount());
        } else {
            map.put(itemKey, map.get(itemKey) + itemStack.getCount());
        }

        viewedInventory.put(pos.toShortString(), map);
    }

    public void resetViewedInvForPos(BlockPos pos) {
        viewedInventory.remove(pos.toShortString());
    }

    public void addVisitedPos(BlockPos pos) {
        visitedPos.add(pos);
    }

    public void resetVisitedPos() {
        visitedPos.clear();
    }

    public boolean isVisitedPos(BlockPos pos) {
        return visitedPos.contains(pos);
    }
}
