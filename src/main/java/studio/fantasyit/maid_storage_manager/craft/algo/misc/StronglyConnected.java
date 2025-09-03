package studio.fantasyit.maid_storage_manager.craft.algo.misc;

import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.craft.algo.base.AbstractBiCraftGraph;
import studio.fantasyit.maid_storage_manager.craft.algo.base.node.Node;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public class StronglyConnected {
    private final AbstractBiCraftGraph graph;
    private final int[] dfn;
    private final int[] low;
    private int dfncnt = 0;

    private final Stack<Node> stack = new Stack<>();
    private final Set<Integer> inStack = new HashSet<>();
    private int sccId;

    public StronglyConnected(AbstractBiCraftGraph graph) {
        this.graph = graph;
        this.dfn = new int[graph.getNodeCount()];
        this.low = new int[graph.getNodeCount()];

        dfncnt = 0;
        sccId = 0;
    }
    public void process(){
        for (int i = 0; i < graph.getNodeCount(); i++) {
            Node node = graph.getNode(i);
            if (dfn[node.id] == 0) {
                tarjan(node);
            }
        }
    }

    public void tarjan(Node node) {
        dfn[node.id] = low[node.id] = ++dfncnt;
        stack.push(node);
        inStack.add(node.id);

        for (Pair<Integer, Integer> to : node.edges) {
            if (dfn[to.getA()] == 0) {
                tarjan(graph.getNode(to.getA()));
                low[node.id] = Math.min(low[node.id], low[to.getA()]);
            } else if (inStack.contains(to.getA())) {
                low[node.id] = Math.min(low[node.id], dfn[to.getA()]);
            }
        }
        if (low[node.id] == dfn[node.id]) {
            ++sccId;
            Node tNode;
            do {
                tNode = stack.pop();
                tNode.sccId = sccId;
                inStack.remove(tNode.id);
            } while (tNode.id != node.id);
        }
    }
    public int maxSccId(){
        return sccId;
    }
}
