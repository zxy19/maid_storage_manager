package studio.fantasyit.maid_storage_manager.craft.algo.graph;

import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.craft.algo.base.CraftResultNode;
import studio.fantasyit.maid_storage_manager.craft.algo.base.HistoryAndResultGraph;
import studio.fantasyit.maid_storage_manager.craft.algo.misc.CraftPlanEvaluator;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class FlattenSearchGraph extends HistoryAndResultGraph {
    Integer retVal = null;
    final Stack<Object> layers;
    private Object lastLayer;
    private int speed = 90;
    private boolean waitInit = false;

    record DfsLayerItem(ItemNode node, int maxRequire, int stepCount, boolean estimating,
                        MutableInt i,
                        MutableInt realMaxRequire,
                        MutableInt remainToCraft,
                        MutableInt oMaxRequire,
                        MutableInt tNodeMinRequire,
                        MutableBoolean tKeepIngredient,
                        MutableBoolean keepCurrently,
                        MutableInt startAt
    ) {
    }

    record DfsLayerStartAt(ItemNode node, int maxRequire,
                           MutableInt i,
                           Integer historyId,
                           int resultId,
                           MutableInt maxCollected,
                           MutableInt minScore,
                           MutableInt startAt,
                           MutableInt maxRequiredForCurrentCraftNode
    ) {
    }

    record DfsLayerCraft(CraftNode node, int maxRequire, boolean estimating,
                         MutableInt simulateRequire,
                         MutableInt totalSuccess,
                         MutableInt restRequire,
                         MutableInt i,
                         MutableInt historyId,
                         MutableInt resultId,
                         MutableBoolean anyFail
    ) {
    }


    public FlattenSearchGraph(List<Pair<ItemStack, Integer>> items, List<CraftGuideData> craftGuides) {
        super(items, craftGuides);
        layers = new Stack<>();
    }

    private void setReturnValue(int value) {
        retVal = value;
        if (!layers.isEmpty())
            lastLayer = layers.pop();
    }


    private void dfsItemPre(DfsLayerItem context) {
        ItemNode node = context.node;
        int maxRequire = context.maxRequire;
        int stepCount = context.stepCount;
        logger.log("Item use available: %d", node.getCurrentRemain());
        //CASE:物品数量够用：直接返回不需要计算方案
        if (node.getCurrentRemain() >= maxRequire) {
            if (!node.isLoopedIngredient || node.hasKeepIngredient || node.loopInputIngredientCount == 0) {
                pushHistory(node, HistoryRecord.RECORD_REQUIRED, maxRequire);
                setReturnValue(maxRequire);
                return;
            } else if (node.getCurrentRemain() - node.loopInputIngredientCount >= maxRequire) {
                pushHistory(node, HistoryRecord.RECORD_REQUIRED, maxRequire);
                setReturnValue(maxRequire);
                return;
            }
        }

        //CASE:物品完全不能合成。此时的物品剩余数量就是可用数量，将其对齐到stepCount返回即可
        if (node.edges.isEmpty()) {
            int alignedRequire = (node.getCurrentRemain() / stepCount) * stepCount;
            pushHistory(node, HistoryRecord.RECORD_REQUIRED, alignedRequire);
            if (maxRequire > alignedRequire)
                node.maxLack = Math.max(node.maxLack, maxRequire - alignedRequire);
            setReturnValue(alignedRequire);
            return;
        }
        //CASE: 这次合成比当前合成链上上次请求合成的要多，说明是正权环，最终节点值无法到达0，直接断开。
        if (maxRequire >= node.minStepRequire) {
            int alignedRequire = (node.getCurrentRemain() / stepCount) * stepCount;
            pushHistory(node, HistoryRecord.RECORD_REQUIRED, alignedRequire);
            if (maxRequire > alignedRequire)
                node.maxLack = Math.max(node.maxLack, maxRequire - alignedRequire);
            setReturnValue(alignedRequire);
            return;
        }

        // 本步骤的消耗。正常情况下，消耗所有的。
        int stepCost = node.getCurrentRemain();

        //回溯备用数据
        int oMaxRequire = maxRequire;
        int tNodeMinRequire = node.minStepRequire;
        boolean tKeepIngredient = node.hasKeepIngredient;
        boolean keepCurrently = false;
        if (!node.hasKeepIngredient && node.loopInputIngredientCount > 0) {
            maxRequire += node.loopInputIngredientCount;
            node.hasKeepIngredient = true;
            logger.log("Add keep loop use %s : %d", node.itemStack, node.loopInputIngredientCount);
            keepCurrently = true;
        }
        if (node.loopInputIngredientCount > 0) {
            //如果循环配方，当前步骤至少保留一次循环用量，下一步骤再进行判断。
            stepCost -= node.loopInputIngredientCount;
            if (stepCost < 0)
                stepCost = 0;
        }
        node.minStepRequire = maxRequire;


        context.remainToCraft.setValue(maxRequire - stepCost);
        logger.log("Item %s use -= %d", node.itemStack, stepCost);
        pushHistory(node, HistoryRecord.RECORD_REQUIRED, stepCost);

        context.oMaxRequire.setValue(oMaxRequire);
        context.tNodeMinRequire.setValue(tNodeMinRequire);
        context.tKeepIngredient.setValue(tKeepIngredient);
        context.keepCurrently.setValue(keepCurrently);
        context.realMaxRequire.setValue(maxRequire);

        dfsStartAtAdd(node, maxRequire);
    }

    private void dfsItemStartAtReturn(DfsLayerItem context, int startAt) {
        context.startAt.setValue(startAt);
    }

    private void dfsItemLoopCall(DfsLayerItem context) {
        int i = context.i.getValue();
        ItemNode node = context.node;
        MutableInt remainToCraft = context.remainToCraft;

        int to = node.edges.get(i).getA();
        int weight = node.edges.get(i).getB();
        CraftNode toNode = (CraftNode) getNode(to);
        int maxRequiredForCurrentCraftNode = (remainToCraft.getValue() + weight - 1) / weight;
        if (toNode.hasLoopIngredient) maxRequiredForCurrentCraftNode = (node.singleTimeCount + weight - 1) / weight;
        logger.logEntryNewLevel("Craft[%d] * %d", toNode.id, maxRequiredForCurrentCraftNode);
        dfsCraftAdd(toNode, maxRequiredForCurrentCraftNode, context.estimating);
    }

    private void dfsItemLoopReturn(DfsLayerItem context, int available) {
        ItemNode node = context.node;
        int i = (context.i.getValue() + context.startAt.getValue()) % node.edges.size();
        int weight = node.edges.get(i).getB();
        logger.logExitLevel("Craft Finish=%d", available);
        int collect = Math.min(available * weight, context.remainToCraft.getValue());
        if (available > 0) {
            if (context.keepCurrently.getValue() && collect < context.remainToCraft.getValue()) {
                collect -= node.loopInputIngredientCount;
                if (collect < 0) collect = 0;
                logger.log("Item keep loop %d in %d", collect, available * weight);
            }
            logger.log("Item use -= %d", collect);
            pushHistory(node, HistoryRecord.RECORD_REQUIRED, collect);
        } else {
            context.i.add(1);
        }
        context.remainToCraft.subtract(collect);
        if (context.remainToCraft.getValue() <= 0) {
            context.remainToCraft.setValue(0);
        }
        //返回逻辑
        if (context.i.getValue() >= node.edges.size() || context.remainToCraft.getValue() <= 0) {
            node.minStepRequire = context.tNodeMinRequire.getValue();
            node.hasKeepIngredient = context.tKeepIngredient.getValue();
            node.maxLack = Math.max(node.maxLack, context.remainToCraft.getValue());
            int crafted = context.realMaxRequire.getValue() - context.remainToCraft.getValue();
            if (context.keepCurrently.getValue() && crafted > context.oMaxRequire.getValue()) {
                logger.log("Item exceed += %d", crafted - context.oMaxRequire.getValue());
                pushHistory(node, HistoryRecord.RECORD_CRAFTED, crafted - context.oMaxRequire.getValue());
            }
            if (context.remainToCraft.getValue() > 0)
                node.maxSuccess = context.oMaxRequire.getValue() - context.remainToCraft.getValue();
            setReturnValue(Math.max(context.oMaxRequire.getValue() - context.remainToCraft.getValue(), 0));
        }
    }

    public void dfsItemAdd(ItemNode node, int maxRequire, int stepCount, boolean estimating) {
        DfsLayerItem push = (DfsLayerItem) layers.push(new DfsLayerItem(node,
                maxRequire,
                stepCount,
                estimating,
                new MutableInt(),
                new MutableInt(maxRequire),
                new MutableInt(),
                new MutableInt(),
                new MutableInt(),
                new MutableBoolean(),
                new MutableBoolean(),
                new MutableInt(-1)
        ));
        dfsItemPre(push);
    }


    public void dfsStartAtAdd(ItemNode node, int maxRequire) {
        layers.add(new DfsLayerStartAt(node, maxRequire,
                new MutableInt(0),
                historyId.getValue(),
                results.size(),
                new MutableInt(0),
                new MutableInt(Integer.MAX_VALUE),
                new MutableInt(0),
                new MutableInt(0)
        ));
        if (Config.craftingShortestPathEvaluator == CraftPlanEvaluator.NONE)
            setReturnValue(0);
        else if (node.bestRecipeStartAt != -1)
            setReturnValue(node.bestRecipeStartAt);
            //循环配方，直接返回当前作为起点。寻找最短环作为目标
        else if (node.bestRecipeStartAtCalculating && node.isLoopedIngredient)
            setReturnValue(maxRequire);
        else if (node.edges.size() <= 1)
            setReturnValue(0);
        else
            node.bestRecipeStartAtCalculating = true;
    }

    public void dfsStartAtCall(DfsLayerStartAt context) {
        while (context.i.getValue() < context.node.edges.size())
            if (context.node.bestRecipeStartAt != -1 && context.i.getValue() != context.node.bestRecipeStartAt)
                context.i.add(1);
            else break;
        if (context.i.getValue() >= context.node.edges.size()) {
            context.node.bestRecipeStartAt = context.startAt.getValue();
            context.node.bestRecipeStartAtCalculating = false;
            setReturnValue(context.startAt.getValue());
            return;
        }

        int to = context.node.edges.get(context.i.getValue()).getA();
        int weight = context.node.edges.get(context.i.getValue()).getB();
        CraftNode toNode = (CraftNode) getNode(to);
        int maxRequiredForCurrentCraftNode = (context.maxRequire + weight - 1) / weight;
        if (toNode.hasLoopIngredient)
            maxRequiredForCurrentCraftNode = (context.node.singleTimeCount + weight - 1) / weight;
        logger.logEntryNewLevel("Craft[%d] * %d", toNode.id, maxRequiredForCurrentCraftNode);
        context.maxRequiredForCurrentCraftNode.setValue(maxRequiredForCurrentCraftNode);
        dfsCraftAdd(toNode, maxRequiredForCurrentCraftNode, true);
    }

    public void dfsStartAtRet(DfsLayerStartAt context, int available) {
        int weight = context.node.edges.get(context.i.getValue()).getB();
        logger.logExitLevel("Craft Finish=%d", available);
        if (context.node.isLoopedIngredient && available == context.maxRequiredForCurrentCraftNode.getValue())
            available = context.maxRequiredForCurrentCraftNode.getValue();
        int collect = Math.min(available * weight, context.maxRequire);


        Map<Integer, Integer> changeMap = popHistoryAtAndCollectChanges(context.historyId);
        List<CraftResultNode> addResults = new ArrayList<>();
        while (context.resultId < results.size()) addResults.add(results.removeLast());

        int score = Config.craftingShortestPathEvaluator.getScore(changeMap, addResults, this);

        if (collect > context.maxCollected.getValue() || (collect == context.maxCollected.getValue() && score < context.minScore.getValue())) {
            context.maxCollected.setValue(collect);
            context.minScore.setValue(score);
            context.startAt.setValue(context.i.getValue());
        }


        popHistoryAt(context.historyId);
        while (context.resultId < results.size()) results.removeLast();
        context.i.add(1);
    }

    public void dfsCraftAdd(CraftNode toNode, int maxRequiredForCurrentCraftNode, boolean estimating) {
        DfsLayerCraft push = (DfsLayerCraft) layers.push(new DfsLayerCraft(
                toNode,
                maxRequiredForCurrentCraftNode,
                estimating,
                new MutableInt(maxRequiredForCurrentCraftNode),
                new MutableInt(),
                new MutableInt(maxRequiredForCurrentCraftNode),
                new MutableInt(toNode.edges.size()),
                new MutableInt(),
                new MutableInt(),
                new MutableBoolean()
        ));
        if (push.node.maxSuccess < push.restRequire.getValue())
            push.restRequire.setValue(push.node.maxSuccess);
        //无原料合成，直接返回全部成功
        if (push.node.edges.isEmpty()) {
            push.totalSuccess.setValue(push.maxRequire);
            push.simulateRequire.setValue(0);
            push.restRequire.setValue(0);
        } else {
            for (Pair<Integer, Integer> toNodePair : push.node.edges) {
                Node toNodeN = getNode(toNodePair.getA());
                if (push.simulateRequire.getValue() * toNodePair.getB() > toNodeN.maxSuccess) {
                    push.simulateRequire.setValue(toNodeN.maxSuccess / toNodePair.getB());
                }
            }
        }
    }

    public void dfsCraftCall(DfsLayerCraft context) {
        if (context.simulateRequire.getValue() <= 0) {
            dfsCraftSetReturn(context);
            return;
        }
        if (context.i.getValue() >= context.node.edges.size()) {
            context.historyId.setValue(this.historyId.getValue());
            context.resultId.setValue(results.size());
            context.anyFail.setFalse();
            context.i.setValue(0);
        }
        Pair<Integer, Integer> edge = context.node.edges.get(context.i.getValue());
        ItemNode toNode = (ItemNode) getNode(edge.getA());

        logger.logEntryNewLevel("Item %s * %d", toNode.itemStack, context.simulateRequire.getValue() * edge.getB());
        dfsItemAdd(toNode,
                context.simulateRequire.getValue() * edge.getB(),
                edge.getB(),
                context.estimating);
    }

    public void dfsCraftRet(DfsLayerCraft context, int currentRequire) {
        Pair<Integer, Integer> edge = context.node.edges.get(context.i.getValue());
        logger.log("Item Finish=%d", currentRequire);
        currentRequire /= edge.getB();
        logger.logExitLevel("Co Craft Finish=%d", currentRequire);
        //如果当前物品获取数量小于模拟数量，说明模拟数量不能顺利完成。此时可以直接回溯
        if (currentRequire < context.simulateRequire.getValue()) {
            //至少对于当前物品，这个数量是可以获取到的
            context.simulateRequire.setValue(currentRequire);
            //回溯
            popHistoryAt(context.historyId.getValue());
            while (context.resultId.getValue() < results.size()) results.removeLast();
            context.anyFail.setTrue();
            //相当于continue
            context.i.setValue(context.node.edges.size());
        } else {
            context.i.add(1);
        }

        if (context.i.getValue() >= context.node.edges.size())
            if (!context.anyFail.getValue()) {
                logger.log("Craft add %d", context.simulateRequire.getValue());
                context.totalSuccess.add(context.simulateRequire.getValue());
                context.restRequire.subtract(context.simulateRequire.getValue());
                context.simulateRequire.setValue(context.restRequire.getValue());
            }
    }

    public void dfsCraftSetReturn(DfsLayerCraft context) {
        Integer totalSuccess = context.totalSuccess.getValue();
        if (totalSuccess > 0) {
            logger.log("Craft finally success %d", totalSuccess);
            results.addLast(new CraftResultNode(context.node.id, totalSuccess, true));
            for (Pair<Integer, Integer> to : context.node.revEdges) {
                ItemNode cn = (ItemNode) getNode(to.getA());
                logger.log("Item %s crafted += %d", cn.itemStack, to.getB() * totalSuccess);
                pushHistory(cn, HistoryRecord.RECORD_CRAFTED, to.getB() * totalSuccess);
            }
        }
        pushHistory(context.node, HistoryRecord.RECORD_SCHEDULED, totalSuccess);
        if (totalSuccess < context.maxRequire)
            context.node.maxSuccess = totalSuccess;
        setReturnValue(totalSuccess);
    }

    @Override
    public void startContext(ItemStack item, int count) {
        super.startContext(item, count);
        layers.clear();
        waitInit = true;
        retVal = null;
    }

    @Override
    public void setSpeed(int i) {
        speed = i * 5;
    }

    public int copyGetRetValue() {
        int retVal = this.retVal;
        this.retVal = null;
        return retVal;
    }

    @Override
    public boolean process() {
        if (waitInit) {
            waitInit = false;
            dfsItemAdd(getItemNodeOrCreate(targetItem), targetCount, 1, false);
        }
        int count = 0;
        while (!layers.isEmpty() && count < speed) {
            count++;
            Object top = layers.peek();

            if (top instanceof DfsLayerItem item) {
                if (retVal != null) {
                    if (lastLayer instanceof DfsLayerStartAt)
                        dfsItemStartAtReturn(item, copyGetRetValue());
                    else
                        dfsItemLoopReturn(item, copyGetRetValue());
                } else {
                    dfsItemLoopCall(item);
                }
            } else if (top instanceof DfsLayerCraft craft) {
                if (retVal != null) {
                    dfsCraftRet(craft, copyGetRetValue());
                } else {
                    dfsCraftCall(craft);
                }
            } else if (top instanceof DfsLayerStartAt startAt) {
                if (retVal != null) {
                    dfsStartAtRet(startAt, copyGetRetValue());
                } else {
                    dfsStartAtCall(startAt);
                }
            }
        }
        if (layers.isEmpty() && retVal != null) {
            targetAvailable = retVal;
        }

        return layers.isEmpty();
    }
}
