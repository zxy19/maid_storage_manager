package studio.fantasyit.maid_storage_manager.craft.algo.misc;

import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.craft.algo.base.AbstractBiCraftGraph;
import studio.fantasyit.maid_storage_manager.craft.algo.base.node.ItemNodeBasic;
import studio.fantasyit.maid_storage_manager.craft.algo.base.node.Node;
import studio.fantasyit.maid_storage_manager.craft.debug.CraftingDebugContext;
import studio.fantasyit.maid_storage_manager.craft.debug.IDebugContextSetter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class PrefilterByChunk implements IDebugContextSetter {
    private final StronglyConnected tarjon;
    private final AbstractBiCraftGraph graph;
    private int[] sccTotalInputs;
    private List<Integer>[] sccToOtherSccs;
    private CraftingDebugContext debugContext = CraftingDebugContext.Dummy.INSTANCE;

    public PrefilterByChunk(AbstractBiCraftGraph graph) {
        this.tarjon = new StronglyConnected(graph);
        this.graph = graph;
    }

    public void process() {
        tarjon.process();
        sccTotalInputs = new int[tarjon.maxSccId() + 1];
        sccToOtherSccs = new List[tarjon.maxSccId() + 1];
        boolean[] sccNotVisitable = new boolean[tarjon.maxSccId() + 1];
        for (int i = 0; i < sccToOtherSccs.length; i++) {
            sccToOtherSccs[i] = new ArrayList<>();
        }

        for (int i = 0; i < graph.getNodeCount(); i++) {
            Node node = graph.getNode(i);
            if (node instanceof ItemNodeBasic in) {
                sccTotalInputs[in.sccId] += in.count;
            }
            for (Pair<Integer, Integer> edge : node.edges) {
                if (node.sccId != graph.getNode(edge.getA()).sccId) {
                    sccToOtherSccs[node.sccId].add(graph.getNode(edge.getA()).sccId);
                }
            }
        }

        Queue<Integer> queue = new LinkedList<>();
        for (int i = 0; i < sccTotalInputs.length; i++) {
            if (sccTotalInputs[i] == 0 && sccToOtherSccs[i].isEmpty()) {
                queue.add(i);
            }
        }
        while (!queue.isEmpty()) {
            int tSccId = queue.poll();
            sccNotVisitable[tSccId] = true;
            for (int toScc : sccToOtherSccs[tSccId]) {
                if (sccToOtherSccs[toScc].stream().allMatch(sccId -> sccNotVisitable[sccId])) {
                    queue.add(toScc);
                }
            }
        }

        int count = 0;
        for (int i = 0; i < graph.getNodeCount(); i++) {
            Node node = graph.getNode(i);
            if (sccNotVisitable[node.sccId]) {
                node.maxSuccess = 0;
                count++;
            }
        }
        debugContext.logNoLevel(CraftingDebugContext.TYPE.PREFILTER,"%s nodes are not visitable", count);
    }

    @Override
    public void setDebugContext(CraftingDebugContext context) {
        this.debugContext = context;
    }
}
