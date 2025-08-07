package studio.fantasyit.maid_storage_manager.craft.algo.base;

import net.minecraft.world.item.ItemStack;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.craft.algo.utils.ResultListUtils;
import studio.fantasyit.maid_storage_manager.craft.data.CraftResultContext;
import studio.fantasyit.maid_storage_manager.craft.work.CraftLayer;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;
import studio.fantasyit.maid_storage_manager.util.MathUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BiCraftCountCalculator {
    private final ICraftGraphLike availableCraftGraph;
    private final int availableSlots;
    private List<Pair<ItemStack, Integer>> fails = new ArrayList<>();
    private List<Pair<ItemStack, Integer>> fullGroupFails = List.of();
    private final ItemStack item;
    int currentRequire = 0;
    int maxRequire = 0;
    boolean singleItemProcess = false;
    List<CraftLayer> results = new ArrayList<>();

    boolean hasAnySuccessCraftingCalc = false;

    public BiCraftCountCalculator(ICraftGraphLike availableCraftGraph, ItemStack item, int requireCount, int availableSlots) {
        this.availableCraftGraph = availableCraftGraph;
        currentRequire = requireCount;
        maxRequire = requireCount;
        this.availableSlots = availableSlots;
        this.item = item;
        availableCraftGraph.setSpeed(128);
        availableCraftGraph.startContext(this.item, requireCount);
    }

    public boolean tick() {
        if (!availableCraftGraph.buildGraph()) return true;
        if (!availableCraftGraph.processQueues()) return true;
        List<CraftLayer> currentResults = availableCraftGraph.getResults();
        CraftResultContext context = null;
        boolean success = false;
        if (currentResults != null && !currentResults.isEmpty()) {
            hasAnySuccessCraftingCalc = true;
            context = new CraftResultContext(currentResults);
            //正常情况下的合成占用
            if (context.getSlotConsume() <= availableSlots) {
                success = true;
            }
            //尝试中途进行一个存储
            if (!success) {
                context.splitTaskWith(availableSlots);
                if (context.getSlotConsume() <= availableSlots) {
                    success = true;
                }
            }
            //如果仍然不成功，尝试分离到单步，然后进行合成
            if (!success) {
                ResultListUtils.unsetPlaceBefore(currentResults);
                currentResults = ResultListUtils.splitIntoSingleStep(currentResults);
                context = new CraftResultContext(currentResults);
            }
            if (context.getSlotConsume() <= availableSlots) {
                success = true;
            }
            //仍然不成功，再次尝试中途进行存储
            if (!success) {
                context.splitTaskWith(availableSlots);
                if (context.getSlotConsume() <= availableSlots) {
                    success = true;
                }
            }
            //仍然不成功，那么就放弃
            if (!success)
                currentResults = null;
        }
        if (currentResults != null && !currentResults.isEmpty()) {
            if (!singleItemProcess && currentRequire != 1 && availableCraftGraph.shouldStartUsingSingleItemProcess()) {
                singleItemProcess = true;
                currentRequire = 1;
                availableCraftGraph.restoreCurrentAndStartContext(this.item, currentRequire);
                return true;
            }

            //当前数量可以进行合成。那么先记录结果层。
            results.addAll(currentResults);
            maxRequire -= currentRequire;
            currentRequire = maxRequire;
            if (singleItemProcess) currentRequire = 1;

            //余产物处理。此处不直接调用addItem.部分算法不需要进行此操作
            //背包剩余的加入合成树
            context.forEachRemaining(availableCraftGraph::addRemainItem);
            //当前合成结束后，不属于产物的物品也应该加入合成树
            currentResults.get(currentResults.size() - 1).getItems().stream()
                    .filter(itemStack -> !ItemStackUtil.isSame(itemStack, this.item, false))
                    .forEach(t -> availableCraftGraph.addRemainItem(t, t.getCount()));

            if (maxRequire <= 0) return false;
            availableCraftGraph.startContext(this.item, currentRequire);
            fullGroupFails = List.of();
        } else {
            if (maxRequire == currentRequire) fullGroupFails = availableCraftGraph.getFails();
            //当前数量不能完成合成
            currentRequire /= 2;
            //算法提供了最大可行计数
            Optional<Integer> maxAvailable = availableCraftGraph.getMaxAvailable();
            if (maxAvailable.isPresent()) {
                while (maxAvailable.get() < currentRequire)
                    currentRequire /= 2;
            }

            if (currentRequire == 0) {
                fails.addAll(fullGroupFails);
                availableCraftGraph.restoreCurrent();
                return false;
            }

            availableCraftGraph.setSpeed(64);
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

    public boolean hasAnySuccessCraftingCalc() {
        return hasAnySuccessCraftingCalc;
    }

    public int getWorstRestSteps() {
        return MathUtil.biMaxStepCalc(maxRequire) - (int) MathUtil.log2((double) maxRequire / currentRequire);
    }

    public int getNotCraftedCount() {
        return maxRequire;
    }
}
