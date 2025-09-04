package studio.fantasyit.maid_storage_manager.craft.algo.graph;

import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.mutable.MutableInt;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.craft.algo.base.CraftResultNode;
import studio.fantasyit.maid_storage_manager.craft.algo.base.HistoryAndResultGraph;
import studio.fantasyit.maid_storage_manager.craft.algo.base.VisitRecorder;
import studio.fantasyit.maid_storage_manager.craft.algo.base.node.CraftNodeBasic;
import studio.fantasyit.maid_storage_manager.craft.algo.base.node.ItemNodeBasic;
import studio.fantasyit.maid_storage_manager.craft.algo.base.node.Node;
import studio.fantasyit.maid_storage_manager.craft.algo.misc.CraftPlanEvaluator;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.debug.CraftingDebugContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SimpleSearchGraph extends HistoryAndResultGraph {
    int dfsDepth = 0;

    boolean addDepAndCheckIsTooDeep() {
        if (dfsDepth >= 800) {
            return true;
        }
        dfsDepth++;
        return false;
    }

    void removeDep() {
        dfsDepth--;
    }

    public SimpleSearchGraph(List<Pair<ItemStack, Integer>> items, List<CraftGuideData> craftGuides) {
        super(items, craftGuides);
    }

    protected int dfsCalcItemNodeRequired(ItemNodeBasic node, int maxRequire, int stepCount, VisitRecorder visit, boolean estimating) {
        if (!node.listed) {
            node.listed = true;
            listed.add(node);
        }
        debugContext.log(CraftingDebugContext.TYPE.CRAFT, "Item use available: %d", node.getCurrentRemain());
        //CASE:物品数量够用：直接返回不需要计算方案
        if (node.getCurrentRemain() >= maxRequire) {
            if (!node.isLoopedIngredient || node.hasKeepIngredient || node.loopInputIngredientCount == 0) {
                pushHistory(node, HistoryRecord.RECORD_REQUIRED, maxRequire);
                return maxRequire;
            } else if (node.getCurrentRemain() - node.loopInputIngredientCount >= maxRequire) {
                pushHistory(node, HistoryRecord.RECORD_REQUIRED, maxRequire);
                return maxRequire;
            }
        }

        //CASE:物品完全不能合成。此时的物品剩余数量就是可用数量，将其对齐到stepCount返回即可
        if (node.edges.isEmpty()) {
            int alignedRequire = (node.getCurrentRemain() / stepCount) * stepCount;
            pushHistory(node, HistoryRecord.RECORD_REQUIRED, alignedRequire);
            if (maxRequire > alignedRequire)
                node.maxLack = Math.max(node.maxLack, maxRequire - alignedRequire);
            return alignedRequire;
        }
        //CASE: 这次合成比当前合成链上上次请求合成的要多，说明是正权环，最终节点值无法到达0，直接断开。
        if (maxRequire >= visit.minStepRequire(node)) {
            int alignedRequire = (node.getCurrentRemain() / stepCount) * stepCount;
            if (maxRequire > alignedRequire)
                node.maxLack = Math.max(node.maxLack, maxRequire - alignedRequire);
            pushHistory(node, HistoryRecord.RECORD_REQUIRED, alignedRequire);

            //如果其他的有环情况断开的话，不满足成功数量单调。需要清空优化数值。
            node.clearMaxSuccessAfter = true;

            return alignedRequire;
        }
        //超过最大深度，直接退出
        if (addInStack(node) > maxDepthAllow || addDepAndCheckIsTooDeep()) {
            int alignedRequire = (node.getCurrentRemain() / stepCount) * stepCount;
            if (maxRequire > alignedRequire)
                node.maxLack = Math.max(node.maxLack, maxRequire - alignedRequire);
            pushHistory(node, HistoryRecord.RECORD_REQUIRED, alignedRequire);
            removeInStack(node);
            return alignedRequire;
        }
        // 本步骤的消耗。正常情况下，消耗所有的。
        int stepCost = node.getCurrentRemain();

        //回溯备用数据
        int oMaxRequire = maxRequire;
        int tNodeMinRequire = visit.minStepRequire(node);
        boolean tKeepIngredient = node.hasKeepIngredient;
        boolean keepCurrent = false;
        if (!node.hasKeepIngredient && node.loopInputIngredientCount > 0) {
            maxRequire += node.loopInputIngredientCount;
            debugContext.log(CraftingDebugContext.TYPE.CRAFT, "Add keep loop use %s : %d", node, node.loopInputIngredientCount);
            node.hasKeepIngredient = true;
            keepCurrent = true;
            //从这里开始计算新的子段。所以直接创建一个新的上下文用于记录
            visit = new VisitRecorder(getNodeCount());
        }
        if (node.loopInputIngredientCount > 0) {
            //如果循环配方，当前步骤至少保留一次循环用量，下一步骤再进行判断。
            stepCost -= node.loopInputIngredientCount;
            if (stepCost < 0)
                stepCost = 0;
        }
        visit.minStepRequire(node, maxRequire);

        MutableInt remainToCraft = new MutableInt(maxRequire - stepCost);
        debugContext.log(CraftingDebugContext.TYPE.CRAFT, "Item %s use -= %d", node, stepCost);
        debugContext.logEntryNewLevel(CraftingDebugContext.TYPE.CRAFT, "START OF ESTIMATING FOR %s", node);
        pushHistory(node, HistoryRecord.RECORD_REQUIRED, stepCost);
        int startsAt = dfsCalcItemNodeStartsAt(node, visit, remainToCraft.getValue());
        debugContext.logExitLevel(CraftingDebugContext.TYPE.CRAFT, "END OF ESTIMATING %s", node);
        for (int _i = 0; _i < node.edges.size(); _i++) {
            int i = (_i + startsAt) % node.edges.size();
            int to = node.edges.get(i).getA();
            int weight = node.edges.get(i).getB();
            CraftNodeBasic toNode = (CraftNodeBasic) getNode(to);
            int maxRequiredForCurrentCraftNode = (remainToCraft.getValue() + weight - 1) / weight;
            if (toNode.hasLoopIngredient)
                maxRequiredForCurrentCraftNode = Math.min((node.singleTimeCount + weight - 1) / weight, maxRequiredForCurrentCraftNode);
            debugContext.logEntryNewLevel(CraftingDebugContext.TYPE.CRAFT, "%s * %d", toNode, maxRequiredForCurrentCraftNode);
            int available = dfsCalcCraftNode(toNode, maxRequiredForCurrentCraftNode, visit, estimating);
            debugContext.logExitLevel(CraftingDebugContext.TYPE.CRAFT, "Craft Finish=%d", available);
            int collect = Math.min(available * weight, remainToCraft.getValue());
            if (available > 0) {
                //当前层是保留物品层。需要计算保留物品。即保证本层扣除结束后，至少剩余LoopInput的数量
                if (keepCurrent) {
                    collect = Math.min(node.getCurrentRemain() - node.loopInputIngredientCount, collect);
                    if (collect < 0) collect = 0;
                    debugContext.log(CraftingDebugContext.TYPE.CRAFT, "Item keep loop toTake=%d in totalSuccess=%d", collect, available * weight);
                }
                debugContext.log(CraftingDebugContext.TYPE.CRAFT, "Item use -= available %d", collect);
                pushHistory(node, HistoryRecord.RECORD_REQUIRED, collect);
                _i--;
            }
            remainToCraft.subtract(collect);
            if (remainToCraft.getValue() <= 0) {
                remainToCraft.setValue(0);
                break;
            }
        }

        visit.minStepRequire(node, tNodeMinRequire);
        node.hasKeepIngredient = tKeepIngredient;
        node.maxLack = Math.max(node.maxLack, remainToCraft.getValue());
        int crafted = maxRequire - remainToCraft.getValue();
        if (keepCurrent && crafted > oMaxRequire) {
            debugContext.log(CraftingDebugContext.TYPE.CRAFT, "Item exceed += %d", crafted - oMaxRequire);
            pushHistory(node, HistoryRecord.RECORD_CRAFTED, crafted - oMaxRequire);
        }
        if (remainToCraft.getValue() > 0) {
            node.maxSuccess = oMaxRequire - remainToCraft.getValue();
            if (node.maxSuccess == node.lastMaxSuccess) {
                node.maxSuccessCount++;
            } else {
                node.lastMaxSuccess = node.maxSuccess;
                node.maxSuccessCount = 1;
            }
        }
        if (node.clearMaxSuccessAfter) {
            removeListedUntil(node);
            node.clearMaxSuccessAfter = false;
        }
        removeInStack(node);
        removeDep();
        return Math.max(oMaxRequire - remainToCraft.getValue(), 0);
    }

    protected int dfsCalcItemNodeStartsAt(ItemNodeBasic node, VisitRecorder visit, int maxRequire) {
        if (Config.craftingShortestPathEvaluator == CraftPlanEvaluator.NONE) return 0;
        if (node.bestRecipeStartAt != -1) return node.bestRecipeStartAt;
        //估算循环，直接返回数值并清空优化缓存
        if (node.bestRecipeStartAtCalculating) {
            node.clearMaxSuccessAfter = true;
            return 0;
        }
        if (node.edges.size() <= 1) return 0;
        if (addDepAndCheckIsTooDeep()) return 0;
        node.bestRecipeStartAtCalculating = true;
        int historyId = this.historyId.getValue();
        int resultId = results.size();
        if (!node.listed) {
            node.listed = true;
            listed.add(node);
        }
        int maxCollected = 0;
        int minScore = Integer.MAX_VALUE;
        int startAt = 0;

        for (int i = 0; i < node.edges.size(); i++) {
            if (node.bestRecipeStartAt != -1 && i != node.bestRecipeStartAt) continue;

            int to = node.edges.get(i).getA();
            int weight = node.edges.get(i).getB();
            CraftNodeBasic toNode = (CraftNodeBasic) getNode(to);
            int maxRequiredForCurrentCraftNode = (maxRequire + weight - 1) / weight;
            if (toNode.hasLoopIngredient)
                maxRequiredForCurrentCraftNode = (node.singleTimeCount + weight - 1) / weight;
            debugContext.logEntryNewLevel(CraftingDebugContext.TYPE.CRAFT, "Estimating Cost: %s * %d", toNode, maxRequiredForCurrentCraftNode);
            int available = dfsCalcCraftNode(toNode, maxRequiredForCurrentCraftNode, visit, true);
            if (toNode.hasLoopIngredient && available == maxRequiredForCurrentCraftNode) {
                available = maxRequire;
            }
            debugContext.logExitLevel(CraftingDebugContext.TYPE.CRAFT, "Estimating Cost: Finish=%d", available);
            int collect = Math.min(available * weight, maxRequire);

            Map<Integer, Integer> changeMap = popHistoryAtAndCollectChanges(historyId);
            List<CraftResultNode> addResults = new ArrayList<>();
            while (resultId < results.size()) addResults.add(results.removeLast());

            int score = Config.craftingShortestPathEvaluator.getScore(changeMap, addResults, this);

            if (collect > maxCollected || (collect == maxCollected && score < minScore)) {
                maxCollected = collect;
                minScore = score;
                startAt = i;
            }
        }
        node.bestRecipeStartAt = startAt;
        node.bestRecipeStartAtCalculating = false;
        if (node.clearMaxSuccessAfter) {
            removeListedUntil(node);
            node.clearMaxSuccessAfter = false;
        }
        removeDep();
        return startAt;
    }

    public int dfsCalcCraftNode(CraftNodeBasic node, int maxRequire, VisitRecorder visit, boolean estimating) {
        if (addInStack(node) > maxDepthAllow) {
            removeInStack(node);
            return 0;
        }
        if (addDepAndCheckIsTooDeep())
            return 0;
        if (results.size() >= Config.craftingMaxLayerLimit) {
            removeInStack(node);
            return 0;
        }
        int restRequire = maxRequire;
        if (node.maxSuccess < restRequire)
            restRequire = node.maxSuccess;
        int simulateRequire = maxRequire;
        int totalSuccess = 0;
        //无原料合成，直接返回全部成功
        if (node.edges.isEmpty()) {
            totalSuccess = maxRequire;
            simulateRequire = 0;
            restRequire = 0;
        } else {
            for (Pair<Integer, Integer> toNodePair : node.edges) {
                Node toNode = getNode(toNodePair.getA());
                if (simulateRequire * toNodePair.getB() > toNode.maxSuccess) {
                    simulateRequire = toNode.maxSuccess / toNodePair.getB();
                }
            }
        }
        //对合成进行模拟。假设每次合成simulateRequire个
        while (simulateRequire > 0) {
            //回溯备用
            int historyId = this.historyId.getValue();
            int resultId = results.size();
            boolean anyFail = false;
            //对每个物品都进行判断
            for (Pair<Integer, Integer> edge : node.edges) {
                ItemNodeBasic toNode = (ItemNodeBasic) getNode(edge.getA());

                debugContext.logEntryNewLevel(CraftingDebugContext.TYPE.CRAFT, "Item %s * %d", toNode, simulateRequire * edge.getB());
                //当前物品可以获取的最大数量
                int currentRequire = dfsCalcItemNodeRequired(toNode,
                        simulateRequire * edge.getB(),
                        edge.getB(),
                        visit,
                        estimating);
                debugContext.log(CraftingDebugContext.TYPE.CRAFT, "Item Finish=%d", currentRequire);
                currentRequire /= edge.getB();
                debugContext.logExitLevel(CraftingDebugContext.TYPE.CRAFT, "Co Craft Finish=%d", currentRequire);
                //如果当前物品获取数量小于模拟数量，说明模拟数量不能顺利完成。此时可以直接回溯
                if (currentRequire < simulateRequire) {
                    //至少对于当前物品，这个数量是可以获取到的
                    simulateRequire = currentRequire;
                    //回溯
                    popHistoryAt(historyId);
                    while (resultId < results.size()) results.removeLast();
                    anyFail = true;
                    debugContext.log(CraftingDebugContext.TYPE.CRAFT, "Craft lack C:%d,%d", currentRequire, simulateRequire);
                    break;
                }
            }

            if (!anyFail) {
                debugContext.log(CraftingDebugContext.TYPE.CRAFT, "Craft add %d", simulateRequire);
                totalSuccess += simulateRequire;
                restRequire -= simulateRequire;
                simulateRequire = restRequire;
                if (node.hasLoopIngredient && restRequire > 0) {
                    simulateRequire = 1;
                }
            }
        }
        if (totalSuccess > 0) {
            debugContext.log(CraftingDebugContext.TYPE.CRAFT, "Craft finally success %d", totalSuccess);
            results.addLast(new CraftResultNode(node.id, totalSuccess, true));
            for (Pair<Integer, Integer> to : node.revEdges) {
                ItemNodeBasic cn = (ItemNodeBasic) getNode(to.getA());
                debugContext.log(CraftingDebugContext.TYPE.CRAFT, "Item %s crafted += %d", cn, to.getB() * totalSuccess);
                pushHistory(cn, HistoryRecord.RECORD_CRAFTED, to.getB() * totalSuccess);
            }
        }
        pushHistory(node, HistoryRecord.RECORD_SCHEDULED, totalSuccess);
        if (totalSuccess < maxRequire) {
            node.maxSuccess = totalSuccess;
            if (node.maxSuccess == node.lastMaxSuccess) {
                node.maxSuccessCount++;
            } else {
                node.lastMaxSuccess = node.maxSuccess;
                node.maxSuccessCount = 1;
            }
        }
        removeInStack(node);
        removeDep();
        return totalSuccess;
    }

    @Override
    public boolean process() {
        ItemNodeBasic itemNode = (ItemNodeBasic) getNode(targetItemNodeId);
        for (int i = 10; i < 61; i += 10) {
            dfsDepth = 0;
            maxDepthAllow = i;
            targetAvailable = dfsCalcItemNodeRequired(itemNode, targetCount, targetCount, new VisitRecorder(getNodeCount()), false);
            if (targetAvailable >= targetCount) return true;
            restoreCurrentAndStartContext(itemNode.id, targetCount);
            while (!processLoopSolver()) ;
        }
        maxDepthAllow = Config.craftingMaxLayerLimit;
        targetAvailable = dfsCalcItemNodeRequired(itemNode, targetCount, targetCount, new VisitRecorder(getNodeCount()), false);
        return true;
    }
}
