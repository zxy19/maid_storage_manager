package studio.fantasyit.maid_storage_manager.craft;

import net.minecraft.world.item.ItemStack;
import oshi.util.tuples.Pair;

import java.util.*;
import java.util.function.BiConsumer;

public class BiCraftCountCalculator {
    private final AvailableCraftGraph availableCraftGraph;
    private final int availableSlots;
    private List<Pair<ItemStack, Integer>> fails = new ArrayList<>();
    private List<Pair<ItemStack, Integer>> fullGroupFails = List.of();
    private final ItemStack item;
    int currentRequire = 0;
    int maxRequire = 0;
    List<CraftLayer> results = new ArrayList<>();

    public BiCraftCountCalculator(AvailableCraftGraph availableCraftGraph, ItemStack item, int requireCount, int availableSlots) {
        this.availableCraftGraph = availableCraftGraph;
        currentRequire = requireCount;
        maxRequire = requireCount;
        this.availableSlots = availableSlots;
        this.item = item;
        availableCraftGraph.setSpeed(32);
        availableCraftGraph.startContext(this.item, requireCount);
    }

    public boolean tick() {
        if (!availableCraftGraph.buildGraph()) return true;
        if (!availableCraftGraph.processQueues()) return true;
        List<CraftLayer> currentResults = availableCraftGraph.getResults();
        if (currentResults != null && !currentResults.isEmpty()) {
            if (evaluateLayersSlotCount(currentResults) > availableSlots) {
                currentResults = null;
            }
        }
        if (currentResults != null && !currentResults.isEmpty()) {
            //当前数量可以进行合成。那么先记录结果层。
            results.addAll(currentResults);
            maxRequire -= currentRequire;
            currentRequire = maxRequire;
            if (maxRequire <= 0) return false;
            availableCraftGraph.startContext(this.item, currentRequire);
            fullGroupFails = List.of();
        } else {
            if (maxRequire == currentRequire) fullGroupFails = availableCraftGraph.getFails();
            if (currentRequire == 1) {
                fails.addAll(fullGroupFails);
                availableCraftGraph.restoreCurrent();
                return false;
            }
            //当前数量不能完成合成
            currentRequire = (int) (currentRequire / 2);
            availableCraftGraph.setSpeed(128);
            availableCraftGraph.restoreCurrentAndStartContext(this.item, currentRequire);
        }
        return true;
    }

    public List<CraftLayer> getResults() {
        return results;
    }

    public List<Pair<ItemStack, Integer>> getFails() {
        return fails;
    }

    public int evaluateLayersSlotCount(List<CraftLayer> layers) {
        int result = 0;
        Map<ItemStack, Integer> itemConsumeCount = new HashMap<>();
        BiConsumer<ItemStack, Integer> addConsumeCount = (itemStack, count) -> {
            HashSet<ItemStack> ks = new HashSet<>(itemConsumeCount.keySet());
            for (ItemStack itemStack1 : ks) {
                if (ItemStack.isSameItemSameTags(itemStack1, itemStack)) {
                    int currentCount = Math.min(itemStack1.getMaxStackSize(), count);
                    count -= currentCount;
                    itemConsumeCount.put(itemStack1, itemConsumeCount.get(itemStack1) + currentCount);
                    if (count <= 0)
                        return;
                }
            }
            while (count > 0) {
                int currentCount = Math.min(itemStack.getMaxStackSize(), count);
                itemConsumeCount.put(itemStack.copy(), currentCount);
                count -= currentCount;
            }
        };
        BiConsumer<ItemStack, Integer> removeConsumeCount = (itemStack, count) -> {
            HashSet<ItemStack> ks = new HashSet<>(itemConsumeCount.keySet());
            for (ItemStack itemStack1 : ks) {
                if (ItemStack.isSameItemSameTags(itemStack1, itemStack)) {
                    int currentCount = Math.min(itemConsumeCount.get(itemStack1), count);
                    count -= currentCount;
                    itemConsumeCount.put(itemStack1, itemConsumeCount.get(itemStack1) - currentRequire);
                    if (itemConsumeCount.get(itemStack1) == 0)
                        itemConsumeCount.remove(itemStack1);
                    else
                        return;
                }
            }
        };

        for (CraftLayer layer : layers) {
            layer.items.forEach(i -> addConsumeCount.accept(i, i.getCount()));
            result = Math.max(result, itemConsumeCount.keySet().size());
            Optional<CraftGuideData> craftData = layer.getCraftData();
            if (craftData.isPresent()) {
                craftData.get().getOutput().getItems().forEach(i -> addConsumeCount.accept(i, i.getCount() * layer.getCount()));
                result = Math.max(result, itemConsumeCount.keySet().size());
                craftData.get().getInput1().getItems().forEach(i -> removeConsumeCount.accept(i, i.getCount() * layer.getCount()));
                craftData.get().getInput2().getItems().forEach(i -> removeConsumeCount.accept(i, i.getCount() * layer.getCount()));
            }
        }
        return result;
    }
}
