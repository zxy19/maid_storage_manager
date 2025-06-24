package studio.fantasyit.maid_storage_manager.craft.algo.graph;

import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.mutable.MutableInt;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.craft.algo.base.CraftResultNode;
import studio.fantasyit.maid_storage_manager.craft.algo.base.HistoryAndResultGraph;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;

import java.util.List;

public class SimpleSearchGraph extends HistoryAndResultGraph {
    public SimpleSearchGraph(List<Pair<ItemStack, Integer>> items, List<CraftGuideData> craftGuides) {
        super(items, craftGuides);
    }

    private int dfsCalcItemNodeRequired(ItemNode node, int maxRequire, int stepCount) {
        logger.log("Item use available: %d", node.getCurrentRemain());
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
        if (maxRequire >= node.minStepRequire) {
            int alignedRequire = (node.getCurrentRemain() / stepCount) * stepCount;
            if (maxRequire > alignedRequire)
                node.maxLack = Math.max(node.maxLack, maxRequire - alignedRequire);
            pushHistory(node, HistoryRecord.RECORD_REQUIRED, alignedRequire);
            return alignedRequire;
        }
        // 本步骤的消耗。正常情况下，消耗所有的。
        int stepCost = node.getCurrentRemain();

        //回溯备用数据
        int oMaxRequire = maxRequire;
        int tNodeMinRequire = node.minStepRequire;
        boolean tKeepIngredient = node.hasKeepIngredient;
        boolean keepCurrent = false;
        if (!node.hasKeepIngredient && node.loopInputIngredientCount > 0) {
            maxRequire += node.loopInputIngredientCount;
            logger.log("Add keep loop use %s : %d", node.itemStack, node.loopInputIngredientCount);
            node.hasKeepIngredient = true;
            keepCurrent = true;
        }
        if (node.loopInputIngredientCount > 0) {
            //如果循环配方，当前步骤至少保留一次循环用量，下一步骤再进行判断。
            stepCost -= node.loopInputIngredientCount;
            if (stepCost < 0)
                stepCost = 0;
        }
        node.minStepRequire = maxRequire;

        MutableInt remainToCraft = new MutableInt(maxRequire - stepCost);
        logger.log("Item %s use -= %d", node.itemStack, stepCost);
        pushHistory(node, HistoryRecord.RECORD_REQUIRED, stepCost);
        for (int i = 0; i < node.edges.size(); i++) {
            int to = node.edges.get(i).getA();
            int weight = node.edges.get(i).getB();
            CraftNode toNode = (CraftNode) getNode(to);
            int maxRequiredForCurrentCraftNode = (remainToCraft.getValue() + weight - 1) / weight;
            if (toNode.hasLoopIngredient) maxRequiredForCurrentCraftNode = (node.singleTimeCount + weight - 1) / weight;
            logger.logEntryNewLevel("Craft[%d] * %d", toNode.id, maxRequiredForCurrentCraftNode);
            int available = dfsCalcCraftNode(toNode, maxRequiredForCurrentCraftNode);

            logger.logExitLevel("Craft Finish=%d", available);
            int collect = Math.min(available * weight, remainToCraft.getValue());
            if (available > 0) {
                //当前层是保留物品层。需要计算保留物品。特殊情况是，如果一次就完成了，那么这里不需要减去循环物品。
                if (keepCurrent && collect < remainToCraft.getValue()) {
                    collect -= node.loopInputIngredientCount;
                    if (collect < 0) collect = 0;
                    logger.log("Item keep loop %d in %d", collect, available * weight);
                }
                logger.log("Item use -= available %d", collect);
                pushHistory(node, HistoryRecord.RECORD_REQUIRED, collect);
                i--;
            }
            remainToCraft.subtract(collect);
            if (remainToCraft.getValue() <= 0) {
                remainToCraft.setValue(0);
                break;
            }
        }

        node.minStepRequire = tNodeMinRequire;
        node.hasKeepIngredient = tKeepIngredient;
        node.maxLack = Math.max(node.maxLack, remainToCraft.getValue());
        int crafted = maxRequire - remainToCraft.getValue();
        if (keepCurrent && crafted > oMaxRequire) {
            logger.log("Item exceed += %d", crafted - oMaxRequire);
            pushHistory(node, HistoryRecord.RECORD_CRAFTED, crafted - oMaxRequire);
        }
        if (remainToCraft.getValue() > 0)
            node.maxSuccess = oMaxRequire - remainToCraft.getValue();
        return Math.max(oMaxRequire - remainToCraft.getValue(), 0);
    }

    public int dfsCalcCraftNode(CraftNode node, int maxRequire) {
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
                ItemNode toNode = (ItemNode) getNode(edge.getA());

                logger.logEntryNewLevel("Item %s * %d", toNode.itemStack, simulateRequire * edge.getB());
                //当前物品可以获取的最大数量
                int currentRequire = dfsCalcItemNodeRequired(toNode,
                        simulateRequire * edge.getB(),
                        edge.getB());
                logger.log("Item Finish=%d", currentRequire);
                currentRequire /= edge.getB();
                logger.logExitLevel("Co Craft Finish=%d", currentRequire);
                //如果当前物品获取数量小于模拟数量，说明模拟数量不能顺利完成。此时可以直接回溯
                if (currentRequire < simulateRequire) {
                    //至少对于当前物品，这个数量是可以获取到的
                    simulateRequire = currentRequire;
                    //回溯
                    popHistoryAt(historyId);
                    while (resultId < results.size()) results.removeLast();
                    anyFail = true;
                    logger.log("Craft lack C:%d,%d", currentRequire, simulateRequire);
                    break;
                }
            }

            if (!anyFail) {
                logger.log("Craft add %d", simulateRequire);
                totalSuccess += simulateRequire;
                restRequire -= simulateRequire;
                simulateRequire = restRequire;
            }
        }
        if (totalSuccess > 0) {
            logger.log("Craft finally success %d", totalSuccess);
            results.addLast(new CraftResultNode(node.id, totalSuccess, true));
            for (Pair<Integer, Integer> to : node.revEdges) {
                ItemNode cn = (ItemNode) getNode(to.getA());
                logger.log("Item %s crafted += %d", cn.itemStack, to.getB() * totalSuccess);
                pushHistory(cn, HistoryRecord.RECORD_CRAFTED, to.getB() * totalSuccess);
            }
        }
        pushHistory(node, HistoryRecord.RECORD_SCHEDULED, totalSuccess);
        if (totalSuccess < maxRequire)
            node.maxSuccess = totalSuccess;
        return totalSuccess;
    }

    @Override
    public boolean process() {
        targetAvailable = dfsCalcItemNodeRequired(getItemNode(targetItem), targetCount, targetCount);
        return true;
    }
}
