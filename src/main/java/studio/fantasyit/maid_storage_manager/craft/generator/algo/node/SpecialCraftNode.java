package studio.fantasyit.maid_storage_manager.craft.generator.algo.node;

import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.generator.algo.ICachableGeneratorGraph;

public abstract class SpecialCraftNode extends Node {
    public SpecialCraftNode(int id) {
        super(id);
    }

    public abstract void buildGraph(ICachableGeneratorGraph graph);

    public abstract void generate(ICachableGeneratorGraph graph);

    @Override
    abstract public String toString();

    public void addEdge(Node node, int weight) {
        if (node instanceof ItemNode)
            super.addEdge(node, weight);
        else
            throw new IllegalArgumentException("Cannot add edge from SpecialCraftNode to " + node);
    }

    private void addToQueue(ICachableGeneratorGraph graph) {
        graph.addToQueue(this);
    }

    private void addCraftGuide(ICachableGeneratorGraph graph, CraftGuideData craftGuideData) {
        graph.addCraftGuide(craftGuideData);
    }
}
