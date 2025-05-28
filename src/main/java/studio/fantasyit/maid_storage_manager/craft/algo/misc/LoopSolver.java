package studio.fantasyit.maid_storage_manager.craft.algo.misc;

import org.apache.commons.lang3.mutable.MutableInt;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.craft.algo.base.AbstractBiCraftGraph;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public class LoopSolver {
    private AbstractBiCraftGraph graph;
    Stack<Pair<Integer, MutableInt>> queue = new Stack<>();
    List<Integer> path = new LinkedList<>();

    public LoopSolver(AbstractBiCraftGraph graph, int startNodeId) {
        this.graph = graph;
        queue.add(new Pair<>(startNodeId, new MutableInt(0)));
        path.add(startNodeId);
    }

    public boolean tick() {
        while (!queue.isEmpty()) {
            Pair<Integer, MutableInt> nodeLayer = queue.peek();
            int nodeId = nodeLayer.getA();
            MutableInt index = nodeLayer.getB();
            AbstractBiCraftGraph.Node node = graph.getNode(nodeId);
            if (node.edges.size() <= index.intValue()) {
                queue.pop();
                path.remove(path.size() - 1);
                continue;
            }
            AbstractBiCraftGraph.Node toNode = graph.getNode(node.edges.get(index.intValue()).getA());
            index.add(1);

            if (toNode instanceof AbstractBiCraftGraph.ItemNode && path.contains(toNode.id)) {
                processLoop(path.indexOf(toNode.id));
            } else {
                queue.add(new Pair<>(toNode.id, new MutableInt(0)));
                path.add(toNode.id);
            }
        }
        return true;
    }

    private void processLoop(int startNode) {
        boolean isSelfProductLoop = false;
        int startCount = -1;
        int[] counts = new int[path.size()];
        for (int c = 1; c < 64; c++) {
            int currentCount = c;
            for (int i = startNode; i < path.size(); i++) {
                AbstractBiCraftGraph.Node node = graph.getNode(path.get(i));
                AbstractBiCraftGraph.Node nextNode = graph.getNode((i == path.size() - 1) ? path.get(startNode) : path.get(i + 1));

                counts[i] = currentCount;

                if (node instanceof AbstractBiCraftGraph.CraftNode craftNode && nextNode instanceof AbstractBiCraftGraph.ItemNode nextItemNode) {
                    for (Pair<Integer, Integer> n : craftNode.revEdges) {
                        if (n.getA() == nextNode.id) currentCount = currentCount * n.getB();
                    }
                } else if (node instanceof AbstractBiCraftGraph.ItemNode itemNode && nextNode instanceof AbstractBiCraftGraph.CraftNode craftNode) {
                    for (Pair<Integer, Integer> n : itemNode.revEdges) {
                        if (n.getA() == nextNode.id) currentCount = currentCount / n.getB();
                    }
                } else throw new RuntimeException("Invalid graph");
            }
            if (currentCount > c) {
                isSelfProductLoop = true;
                startCount = c;
                break;
            }
        }
        AbstractBiCraftGraph.ItemNode node = (AbstractBiCraftGraph.ItemNode) graph.getNode(path.get(startNode));
        node.isLoopedIngredient = true;
        if (isSelfProductLoop) {
            node.loopInputIngredientCount = Math.max(startCount, node.loopInputIngredientCount);
        } else {
            node.loopInputIngredientCount = 0;
        }
    }
}
