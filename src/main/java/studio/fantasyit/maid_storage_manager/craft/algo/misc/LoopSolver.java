package studio.fantasyit.maid_storage_manager.craft.algo.misc;

import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.mutable.MutableInt;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.craft.algo.base.AbstractBiCraftGraph;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;

import java.util.*;

public class LoopSolver {
    private AbstractBiCraftGraph graph;
    Stack<Pair<Integer, MutableInt>> queue = new Stack<>();
    List<Integer> path = new LinkedList<>();
    HashSet<Long> used = new HashSet<>();
    HashMap<Integer, Integer> visited = new HashMap<>();

    public long compoundToLong(int a, int b) {
        return (((long) a) << 32) | (b & 0xffffffffL);
    }

    public LoopSolver(AbstractBiCraftGraph graph, int startNodeId) {
        this.graph = graph;
        queue.add(new Pair<>(startNodeId, new MutableInt(0)));
        path.add(startNodeId);
        visited.put(startNodeId, 0);
    }

    public boolean tick() {
        int c = 0;
        while (!queue.isEmpty()) {
            if (c++ > 1000)
                return false;
            Pair<Integer, MutableInt> nodeLayer = queue.peek();
            int nodeId = nodeLayer.getA();
            MutableInt index = nodeLayer.getB();
            AbstractBiCraftGraph.Node node = graph.getNode(nodeId);
            if (node.edges.size() <= index.intValue()) {
                if (node instanceof AbstractBiCraftGraph.CraftNode craftNode) {
                    craftNode.hasLoopIngredient = craftNode.edges
                            .stream().anyMatch(edge -> ((AbstractBiCraftGraph.ItemNode) graph.getNode(edge.getA())).isLoopedIngredient);
                }
                visited.put(node.id, visited.get(node.id) - 1);
                queue.pop();
                path.remove(path.size() - 1);
                continue;
            }
            Pair<Integer, Integer> edge = node.edges.get(index.intValue());
            AbstractBiCraftGraph.Node toNode = graph.getNode(edge.getA());
            index.add(1);

            if (toNode instanceof AbstractBiCraftGraph.ItemNode && path.contains(toNode.id)) {
                if (!used.contains(compoundToLong(toNode.id, node.id))) {
                    used.add(compoundToLong(toNode.id, node.id));
                    processLoop(path.indexOf(toNode.id));
                    c += 100;
                }
            } else if (toNode instanceof AbstractBiCraftGraph.ItemNode && visited.containsKey(toNode.id)) {
                continue;
            } else {
                queue.add(new Pair<>(toNode.id, new MutableInt(0)));
                visited.put(toNode.id, visited.computeIfAbsent(toNode.id, t -> 0) + 1);
                path.add(toNode.id);
            }
        }
        return true;
    }

    private void processLoop(int startNode) {
        if (path.size() - startNode > Config.craftingLoopSolverMaxSize)
            return;
        boolean isSelfProductLoop = false;
        boolean isMainBranchLoop = true;
        boolean hasLoop = false;
        int startCount = -1;
        int finallyGain = -1;
        int[] counts = new int[path.size()];
        for (int c = 1; c < 64; c++) {
            int currentCount = c;
            for (int i = path.size(); i > startNode; i--) {
                AbstractBiCraftGraph.Node node = graph.getNode((i == path.size()) ? path.get(startNode) : path.get(i));
                AbstractBiCraftGraph.Node nextNode = graph.getNode(path.get(i - 1));

                if (node instanceof AbstractBiCraftGraph.CraftNode craftNode && nextNode instanceof AbstractBiCraftGraph.ItemNode nextItemNode) {
                    for (Pair<Integer, Integer> n : craftNode.revEdges) {
                        if (n.getA() == nextNode.id) currentCount = currentCount * n.getB();
                    }
                } else if (node instanceof AbstractBiCraftGraph.ItemNode itemNode && nextNode instanceof AbstractBiCraftGraph.CraftNode craftNode) {
                    for (Pair<Integer, Integer> n : itemNode.revEdges) {
                        if (n.getA() == nextNode.id) currentCount = currentCount / n.getB();
                    }
                } else throw new RuntimeException("Invalid graph");

                counts[i - 1] = currentCount;

                if (visited.get(nextNode.id) > 1 || visited.get(node.id) > 1) {
                    isMainBranchLoop = false;
                }
            }
            if (currentCount >= c)
                hasLoop = true;
            if (currentCount > c) {
                isSelfProductLoop = true;
                startCount = c;
                finallyGain = currentCount;
                break;
            }
        }

        if (!hasLoop)
            return;

        if (startCount != -1 && hasIndirectItemConsumeOrUnexpectedSubProd(startNode, startCount)) {
            return;
        }

        if (isSelfProductLoop) {
            AbstractBiCraftGraph.ItemNode node = (AbstractBiCraftGraph.ItemNode) graph.getNode(path.get(startNode));
            if (node.loopInputIngredientCount == 0)
                node.loopInputIngredientCount = startCount;
            node.loopInputIngredientCount = Math.min(startCount, node.loopInputIngredientCount);
            node.singleTimeCount = finallyGain;
            node.isLoopedIngredient = true;
            for (int i = startNode + 2; i < path.size(); i += 2) {
                AbstractBiCraftGraph.ItemNode walkNode = (AbstractBiCraftGraph.ItemNode) graph.getNode(path.get(i));
                //环上所有节点也进行设置
                if (walkNode.loopInputIngredientCount == 0)
                    walkNode.loopInputIngredientCount = counts[i];
                walkNode.loopInputIngredientCount = Math.min(counts[i], walkNode.loopInputIngredientCount);
                walkNode.isLoopedIngredient = true;
                walkNode.singleTimeCount = counts[i];
            }
        } else {
            AbstractBiCraftGraph.ItemNode node = (AbstractBiCraftGraph.ItemNode) graph.getNode(path.get(startNode));
            //对于主分支非自增环，其实际上没有意义（因为所求直接就是产物，那么循环也不会带来任何收益，直接设置false）
            if (!isMainBranchLoop) {
                node.isLoopedIngredient = true;
            }
        }
    }

    public boolean hasIndirectItemConsumeOrUnexpectedSubProd(int startNode, int startCount) {
        List<ItemStack> inputs = new ArrayList<>();
        List<ItemStack> outputs = new ArrayList<>();
        int currentCount = startCount;
        for (int i = path.size(); i > startNode; i--) {
            AbstractBiCraftGraph.Node node = graph.getNode((i == path.size()) ? path.get(startNode) : path.get(i));
            AbstractBiCraftGraph.Node nextNode = graph.getNode(path.get(i - 1));
            if (node instanceof AbstractBiCraftGraph.CraftNode craftNode && nextNode instanceof AbstractBiCraftGraph.ItemNode nextItemNode) {
                CraftGuideData craftGuideData = craftNode.craftGuideData;
                for (Pair<Integer, Integer> n : craftNode.revEdges) {
                    if (n.getA() == nextNode.id) {
                        //当前合成将进行的次数
                        int currentStepRepeat = currentCount;
                        //计算当前步骤结束后产物的数量
                        currentCount = currentCount * n.getB();
                        int finallyGainItemCount = currentCount;

                        //当前步骤的输入。优先从之前的输出获取。如果不足，则标记从环外获取。
                        craftGuideData.getAllInputItems().forEach(_t -> {
                            ItemStack t = _t.copyWithCount(_t.getCount() * currentStepRepeat);
                            ItemStack remain = ItemStackUtil.removeIsMatchInList(outputs, t, ItemStackUtil::isSameInCrafting);
                            ItemStackUtil.addToList(inputs, remain, ItemStackUtil::isSameInCrafting);
                        });
                        if (Config.craftingLoopSolverPreventIndirect) {
                            //如果当前出物品数量已经小于当前从环外获取的物品数量，则意味着当前物品相当于全部从环外获取，也就是“间接物品获取”
                            boolean hasIndirectRecipe = inputs.stream()
                                    .filter(t -> ItemStackUtil.isSameInCrafting(t, nextItemNode.itemStack))
                                    .findFirst()
                                    .map(it -> it.getCount() >= finallyGainItemCount)
                                    .orElse(false);
                            if (hasIndirectRecipe) {
                                return true;
                            }
                        }
                        //移除输入，添加输出
                        craftGuideData.getAllOutputItems().forEach(_t -> {
                            ItemStack t = _t.copyWithCount(_t.getCount() * currentStepRepeat);
                            ItemStack remain = ItemStackUtil.removeIsMatchInList(inputs, t, ItemStackUtil::isSameInCrafting);
                            ItemStackUtil.addToList(outputs, remain, ItemStackUtil::isSameInCrafting);
                        });

                        break;
                    }
                }

            } else if (node instanceof AbstractBiCraftGraph.ItemNode itemNode && nextNode instanceof AbstractBiCraftGraph.CraftNode craftNode) {
                for (Pair<Integer, Integer> n : itemNode.revEdges) {
                    if (n.getA() == nextNode.id) currentCount = currentCount / n.getB();
                }
            } else throw new RuntimeException("Invalid graph");
        }

        if (Config.craftingLoopSolverPreventNewByProduct) {
            ItemStack mainItemStack = ((AbstractBiCraftGraph.ItemNode) graph.getNode(path.get(startNode))).itemStack;
            for (ItemStack byprod : outputs) {
                AbstractBiCraftGraph.ItemNode itemNode = graph.getItemNode(byprod);
                if (itemNode == null || itemNode.getCurrentRemain() == 0) {
                    if (!ItemStackUtil.isSameInCrafting(mainItemStack, byprod))
                        return true;
                }
            }
        }


        return false;
    }
}
