package studio.fantasyit.maid_storage_manager.craft.algo;

import net.minecraft.world.item.ItemStack;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.craft.data.CraftLayer;
import studio.fantasyit.maid_storage_manager.craft.data.CraftResultContext;
import studio.fantasyit.maid_storage_manager.util.MathUtil;

import java.util.ArrayList;
import java.util.List;

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
        CraftResultContext context = null;
        if (currentResults != null && !currentResults.isEmpty()) {
            context = new CraftResultContext(currentResults);
            if (context.getSlotConsume() > availableSlots) {
                currentResults = null;
            }
        }
        if (currentResults != null && !currentResults.isEmpty()) {
            //当前数量可以进行合成。那么先记录结果层。
            results.addAll(currentResults);
            maxRequire -= currentRequire;
            currentRequire = maxRequire;
            context.forEachRemaining(availableCraftGraph::addCount);
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

    public int getWorstRestSteps() {
        return MathUtil.biMaxStepCalc(maxRequire) - (int) MathUtil.log2((double) maxRequire / currentRequire);
    }
}
