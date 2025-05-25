package studio.fantasyit.maid_storage_manager.craft.algo.base;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public abstract class AbstractBiCraftGraph implements ICraftGraphLike {
    protected Node getNode(int a) {
        return nodes.get(a);
    }

    public static class Node {
        public int id;
        public boolean related;
        public final List<Pair<Integer, Integer>> edges;
        public Node(int id, boolean related) {
            this.id = id;
            this.related = related;
            this.edges = new ArrayList<>();
        }
        public void addEdge(Node to, int weight) {
            this.edges.add(new Pair<>(to.id, weight));
        }

        public void forEachEdge(BiConsumer<Integer, Integer> visitor) {
            for (Pair<Integer, Integer> edge : this.edges) {
                visitor.accept(edge.getA(), edge.getB());
            }
        }
    }

    public static class ItemNode extends Node {
        public final ItemStack itemStack;
        public int minStepRequire;
        public int required;
        public int crafted;
        public int count;

        public ItemNode(int id, boolean related, ItemStack itemStack) {
            super(id, related);
            this.itemStack = itemStack;
            this.count = 0;
            this.crafted = 0;
            this.required = 0;
            this.minStepRequire = Integer.MAX_VALUE;
        }
        public void addCount(int count) {
            this.count += count;
        }

        public int getCurrentRemain() {
            return this.crafted + this.count - this.required;
        }
    }

    public static class CraftNode extends Node {
        public final CraftGuideData craftGuideData;
        public int scheduled;

        public CraftNode(int id, boolean related, CraftGuideData craftGuideData) {
            super(id, related);
            this.craftGuideData = craftGuideData;
            this.scheduled = 0;
        }

        public void addScheduled(int count) {
            this.scheduled += count;
        }
    }

    List<Node> nodes;

    public @NotNull ItemNode getItemNodeOrCreate(ItemStack itemStack) {
        ItemNode tmp = getItemNode(itemStack);
        if (tmp == null)
            tmp = addItemNode(itemStack);
        return tmp;
    }

    public ItemNode getItemNode(ItemStack itemStack) {
        for (Node node : nodes) {
            if (node instanceof ItemNode in) {
                if (ItemStackUtil.isSame(itemStack, in.itemStack, false)) {
                    return in;
                }
            }
        }
        return null;
    }

    public ItemNode addItemNode(ItemStack itemStack) {
        ItemNode itemNode = new ItemNode(nodes.size(), false, itemStack);
        nodes.add(itemNode);
        return itemNode;
    }

    public void addItemCount(ItemStack itemStack, int count) {
        ItemNode itemNode = getItemNodeOrCreate(itemStack);
        itemNode.count += count;
    }

    public void setItemCount(ItemStack itemStack, int count) {
        ItemNode itemNode = getItemNodeOrCreate(itemStack);
        itemNode.count = count;
    }

    public void addCraft(CraftGuideData craftGuideData) {
        CraftNode craftNode = new CraftNode(nodes.size(), false, craftGuideData);
        nodes.add(craftNode);
    }

    int buildGraphIndex;

    public void rebuildGraph() {
        buildGraphIndex = 0;
    }

    public boolean buildGraph() {
        int stepCount = 0;
        for (; buildGraphIndex < nodes.size() && stepCount < 32; buildGraphIndex++) {
            Node node = nodes.get(buildGraphIndex);
            if (node instanceof CraftNode craftNode) {
                stepCount++;
                for (ItemStack input : craftNode.craftGuideData.getAllInputItems()) {
                    ItemNode itemNode = getItemNodeOrCreate(input);
                    craftNode.addEdge(itemNode, input.getCount());
                }
                for (ItemStack output : craftNode.craftGuideData.getAllOutputItems()) {
                    ItemNode itemNode = getItemNodeOrCreate(output);
                    itemNode.addEdge(craftNode, output.getCount());
                }
            }
        }
        return buildGraphIndex >= nodes.size();
    }

    @Override
    public void restoreCurrentAndStartContext(ItemStack item, int count) {
        restoreCurrent();
        startContext(item, count);
    }

    @Override
    public void restoreCurrent() {
        for (Node node : nodes) {
            node.related = false;
            if (node instanceof ItemNode itemNode) {
                itemNode.minStepRequire = Integer.MAX_VALUE;
                itemNode.required = 0;
                itemNode.crafted = 0;
            } else if (node instanceof CraftNode craftNode) {
                craftNode.scheduled = 0;
            }
        }
    }

    @Override
    public void startContext(ItemStack item, int count) {
        buildGraphIndex = 0;
        for (Node node : nodes) {
            if (node instanceof ItemNode itemNode) {
                itemNode.count -= itemNode.required;
                itemNode.count += itemNode.crafted;
                if (itemNode.count < 0)
                    itemNode.count = 0;
                itemNode.minStepRequire = Integer.MAX_VALUE;
                itemNode.required = 0;
            } else if (node instanceof CraftNode craftNode) {
                craftNode.scheduled = 0;
            }
        }
    }
}
