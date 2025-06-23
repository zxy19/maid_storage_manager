package studio.fantasyit.maid_storage_manager.craft.algo.misc;

import org.apache.commons.lang3.mutable.MutableInt;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.craft.algo.base.AbstractBiCraftGraph;

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
        boolean isSelfProductLoop = false;
        boolean isMainBranchLoop = true;
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
            if (currentCount > c) {
                isSelfProductLoop = true;
                startCount = c;
                finallyGain = currentCount;
                break;
            }
        }

        AbstractBiCraftGraph.ItemNode node = (AbstractBiCraftGraph.ItemNode) graph.getNode(path.get(startNode));
        if (isSelfProductLoop) {
            node.loopInputIngredientCount = Math.max(startCount, node.loopInputIngredientCount);
            node.singleTimeCount = finallyGain;
            node.isLoopedIngredient = true;
        } else {
            node.loopInputIngredientCount = 0;
            //对于主分支非自增环，其实际上没有意义（因为所求直接就是产物，那么循环也不会带来任何收益，直接设置false）
            if (!isMainBranchLoop) {
                node.isLoopedIngredient = true;
            }
        }

    }
}
