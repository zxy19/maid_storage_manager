package studio.fantasyit.maid_storage_manager.craft;

import net.minecraft.world.item.ItemStack;

import java.util.List;

public class BiCraftCountCalculator {
    private final AvailableCraftGraph availableCraftGraph;
    private final int availableSlots;
    private final ItemStack item;
    int currentRequire = 0;
    int maxRequire = 0;
    List<CraftLayer> results;

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
        if (currentResults != null) {
            //当前数量可以进行合成。那么先记录结果层。
            results.addAll(currentResults);
            maxRequire -= currentRequire;
            currentRequire = maxRequire;
            if (maxRequire <= 0) return false;
        } else {
            if (currentRequire == 1) return false;
            //当前数量不能完成合成
            currentRequire = (int) (currentRequire / 2);
            availableCraftGraph.setSpeed(128);
            availableCraftGraph.reverseCurrentAndStartContext(this.item, currentRequire);
        }
        return true;
    }
}
