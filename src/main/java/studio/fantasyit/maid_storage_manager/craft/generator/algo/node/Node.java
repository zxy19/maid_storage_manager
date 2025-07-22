package studio.fantasyit.maid_storage_manager.craft.generator.algo.node;


import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.craft.generator.algo.ICachableGeneratorGraph;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

public class Node {
    public int id;
    public boolean inqueue;
    public boolean related;
    public final List<Pair<Integer, Integer>> edges;
    public final List<Pair<Integer, Integer>> edgesRev;
    public boolean isRemoved;


    public Node(int id) {
        this.id = id;
        this.edges = new ArrayList<>();
        this.edgesRev = new ArrayList<>();
        this.related = false;
    }

    public void addEdge(Node to, int weight) {
        this.edges.add(new Pair<>(to.id, weight));
        to.edgesRev.add(new Pair<>(this.id, weight));
    }

    public void forEachEdge(BiConsumer<Integer, Integer> visitor) {
        for (Pair<Integer, Integer> edge : this.edges) {
            visitor.accept(edge.getA(), edge.getB());
        }
    }

    public void forEachRev(BiConsumer<Integer, Integer> visitor) {
        for (Pair<Integer, Integer> edge : this.edgesRev) {
            visitor.accept(edge.getA(), edge.getB());
        }
    }

    public void removeAllEdges(ICachableGeneratorGraph graph) {
        this.edges.forEach(edge -> graph.getNode(edge.getA())
                .edgesRev
                .removeIf(edgeRev -> edgeRev.getA() == this.id && Objects.equals(edgeRev.getB(), edge.getB()))
        );
        this.edgesRev.forEach(edge -> graph.getNode(edge.getA())
                .edges
                .removeIf(edgeRev -> edgeRev.getA() == this.id && Objects.equals(edgeRev.getB(), edge.getB()))
        );
        this.edges.clear();
        this.edgesRev.clear();
        isRemoved = true;
    }

    public void setNonRemoved() {
        isRemoved = false;
    }
}