package studio.fantasyit.maid_storage_manager.craft.algo.graph;

import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.craft.algo.base.CraftResultNode;
import studio.fantasyit.maid_storage_manager.craft.algo.base.HistoryAndResultGraph;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;

import java.util.List;
import java.util.Stack;

public class FlattenSearchGraph extends HistoryAndResultGraph {
    Integer retVal = null;
    final Stack<Object> layers;
    private int speed = 90;
    private boolean waitInit = false;

    record DfsLayerItem(ItemNode node, int maxRequire, int stepCount,
                        MutableInt i,
                        MutableInt realMaxRequire,
                        MutableInt remainToCraft,
                        MutableInt oMaxRequire,
                        MutableInt tNodeMinRequire,
                        MutableBoolean tKeepIngredient,
                        MutableBoolean keepCurrently
    ) {
    }

    record DfsLayerCraft(CraftNode node, int maxRequire,
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
            layers.pop();
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
            setReturnValue(alignedRequire);
            return;
        }
        //CASE: 这次合成比当前合成链上上次请求合成的要多，说明是正权环，最终节点值无法到达0，直接断开。
        if (maxRequire >= node.minStepRequire) {
            int alignedRequire = (node.getCurrentRemain() / stepCount) * stepCount;
            pushHistory(node, HistoryRecord.RECORD_REQUIRED, alignedRequire);
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
    }

    private void dfsItemLoopCall(DfsLayerItem context) {
        int i = context.i.getValue();
        ItemNode node = context.node;
        MutableInt remainToCraft = context.remainToCraft;

        int to = node.edges.get(i).getA();
        int weight = node.edges.get(i).getB();
        CraftNode toNode = (CraftNode) getNode(to);
        int maxRequiredForCurrentCraftNode = (remainToCraft.getValue() + weight - 1) / weight;
        if (toNode.hasLoopIngredient) maxRequiredForCurrentCraftNode = 1;
        logger.logEntryNewLevel("Craft[%d] * %d", toNode.id, maxRequiredForCurrentCraftNode);
        dfsCraftAdd(toNode, maxRequiredForCurrentCraftNode);
    }

    private void dfsItemLoopReturn(DfsLayerItem context, int available) {
        int i = context.i.getValue();
        ItemNode node = context.node;
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
            setReturnValue(Math.max(context.oMaxRequire.getValue() - context.remainToCraft.getValue(), 0));
        }
    }

    public void dfsCraftAdd(CraftNode toNode, int maxRequiredForCurrentCraftNode) {
        DfsLayerCraft push = (DfsLayerCraft) layers.push(new DfsLayerCraft(
                toNode,
                maxRequiredForCurrentCraftNode,
                new MutableInt(maxRequiredForCurrentCraftNode),
                new MutableInt(),
                new MutableInt(maxRequiredForCurrentCraftNode),
                new MutableInt(toNode.edges.size()),
                new MutableInt(),
                new MutableInt(),
                new MutableBoolean()
        ));
        if (push.node.edges.isEmpty()) {
            setReturnValue(push.maxRequire);
        }
    }

    public void dfsItemAdd(ItemNode node, int maxRequire, int stepCount) {
        DfsLayerItem push = (DfsLayerItem) layers.push(new DfsLayerItem(node,
                maxRequire,
                stepCount,
                new MutableInt(),
                new MutableInt(maxRequire),
                new MutableInt(),
                new MutableInt(),
                new MutableInt(),
                new MutableBoolean(),
                new MutableBoolean()
        ));
        dfsItemPre(push);
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
                edge.getB());
    }

    public void dfsCraftRet(DfsLayerCraft context, int currentRequire) {
        logger.logExitLevel("Item Finish=%d", currentRequire);
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
            dfsItemAdd(getItemNodeOrCreate(targetItem), targetCount, 1);
        }
        int count = 0;
        while (!layers.isEmpty() && count < speed) {
            count++;
            Object top = layers.peek();

            if (top instanceof DfsLayerItem item) {
                if (retVal != null) {
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
            }
        }
        if (layers.isEmpty() && retVal != null) {
            targetAvailable = retVal;
        }

        return layers.isEmpty();
    }
}
