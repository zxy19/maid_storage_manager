package studio.fantasyit.maid_storage_manager.craft.algo.base.node;

import oshi.util.tuples.Pair;

import java.util.ArrayList;
import java.util.List;

public class Node {
    //不降序列优化
    public int maxSuccess;
    public boolean listed;
    public boolean clearMaxSuccessAfter;
    public int maxSuccessCount;
    public int lastMaxSuccess;
    //一般
    public int id;
    public boolean related;
    public final List<Pair<Integer, Integer>> edges;
    public final List<Pair<Integer, Integer>> revEdges;
    //强连通分量ID
    public int sccId;

    public Node(int id, boolean related) {
        this.id = id;
        this.related = related;
        this.edges = new ArrayList<>();
        this.revEdges = new ArrayList<>();
        maxSuccess = Integer.MAX_VALUE;
        lastMaxSuccess = maxSuccess;
        maxSuccessCount = 0;
        clearMaxSuccessAfter = false;
        listed = false;
    }

    public void addEdge(Node to, int weight) {
        this.edges.add(new Pair<>(to.id, weight));
        to.revEdges.add(new Pair<>(this.id, weight));
    }
}
