package studio.fantasyit.maid_storage_manager.craft.algo.base;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.craft.algo.misc.LoopSolver;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;

import java.util.*;

public abstract class AbstractBiCraftGraph implements ICraftGraphLike {

    public static class Node {
        public int id;
        public boolean related;
        public int maxSuccess;
        public final List<Pair<Integer, Integer>> edges;
        public final List<Pair<Integer, Integer>> revEdges;

        public Node(int id, boolean related) {
            this.id = id;
            this.related = related;
            this.edges = new ArrayList<>();
            this.revEdges = new ArrayList<>();
            maxSuccess = Integer.MAX_VALUE;
        }

        public void addEdge(Node to, int weight) {
            this.edges.add(new Pair<>(to.id, weight));
            to.revEdges.add(new Pair<>(this.id, weight));
        }
    }

    public static class ItemNode extends Node {
        public final ItemStack itemStack;
        public int minStepRequire;
        public int required;
        public int crafted;
        public int count;
        public boolean isLoopedIngredient;
        public int loopInputIngredientCount;
        public int singleTimeCount;
        public boolean hasKeepIngredient;
        public int maxLack;

        public int bestRecipeStartAt;
        public boolean bestRecipeStartAtCalculating;

        public ItemNode(int id, boolean related, ItemStack itemStack) {
            super(id, related);
            this.itemStack = itemStack;
            this.count = 0;
            reinit();
        }

        public void reinit() {
            this.crafted = 0;
            this.required = 0;
            this.isLoopedIngredient = false;
            this.loopInputIngredientCount = 0;
            this.hasKeepIngredient = false;
            this.minStepRequire = Integer.MAX_VALUE;
            this.maxLack = 0;
            this.singleTimeCount = 1;
            this.bestRecipeStartAt = -1;
            this.bestRecipeStartAtCalculating = false;
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
        public final List<CraftGuideData> sameData;
        public int scheduled;
        public boolean hasLoopIngredient;

        public CraftNode(int id, boolean related, CraftGuideData craftGuideData) {
            super(id, related);
            this.craftGuideData = craftGuideData;
            this.sameData = new ArrayList<>(List.of(craftGuideData));
            this.scheduled = 0;
            this.hasLoopIngredient = false;
        }

        public void addScheduled(int count) {
            this.scheduled += count;
        }

        public void addSame(CraftGuideData data) {
            this.sameData.add(data);
        }
    }


    public record HashableItemNodeCount(int id, int count) {
        @Override
        public int hashCode() {
            return Objects.hash(id, count);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            HashableItemNodeCount that = (HashableItemNodeCount) o;
            return id == that.id && count == that.count;
        }
    }

    public record ItemSetPair(HashSet<HashableItemNodeCount> inputs, HashSet<HashableItemNodeCount> outputs) {
        @Override
        public int hashCode() {
            return Objects.hash(inputs, outputs);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ItemSetPair itemSetPair &&
                    itemSetPair.inputs.size() == this.inputs.size() &&
                    itemSetPair.outputs.size() == this.outputs.size()
            ) {
                for (HashableItemNodeCount input : itemSetPair.inputs) {
                    if (!this.inputs.contains(input))
                        return false;
                }
                for (HashableItemNodeCount output : itemSetPair.outputs) {
                    if (!this.outputs.contains(output))
                        return false;
                }
                return true;
            }
            return false;
        }
    }

    List<Node> nodes;
    Map<ResourceLocation, List<ItemNode>> itemNodeMap;

    public Node getNode(int a) {
        return nodes.get(a);
    }

    public AbstractBiCraftGraph(List<Pair<ItemStack, Integer>> items, List<CraftGuideData> craftGuides) {
        this.nodes = new ArrayList<>();
        this.itemNodeMap = new HashMap<>();
        for (Pair<ItemStack, Integer> item : items) {
            ItemNode itemNode = getItemNodeOrCreate(item.getA());
            itemNode.addCount(item.getB());
        }
        for (CraftGuideData craftGuide : craftGuides) {
            addCraft(craftGuide);
        }
    }

    public @NotNull ItemNode getItemNodeOrCreate(ItemStack itemStack) {
        ItemNode tmp = getItemNode(itemStack);
        if (tmp == null)
            tmp = addItemNode(itemStack);
        return tmp;
    }

    public ItemNode getItemNode(ItemStack itemStack) {
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(itemStack.getItem());
        if (itemId == null) return null;
        if (!itemNodeMap.containsKey(itemId)) return null;
        for (ItemNode in : itemNodeMap.get(itemId)) {
            if (ItemStackUtil.isSameInCrafting(itemStack, in.itemStack)) {
                return in;
            }
        }
        return null;
    }

    public ItemNode addItemNode(ItemStack itemStack) {
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(itemStack.getItem());
        ItemNode itemNode = new ItemNode(nodes.size(), false, itemStack);
        nodes.add(itemNode);
        if (!itemNodeMap.containsKey(itemId))
            itemNodeMap.put(itemId, new ArrayList<>());
        itemNodeMap.get(itemId).add(itemNode);
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

    LoopSolver loopSolver;
    int buildGraphIndex;
    HashMap<ItemSetPair, Integer> existCrafting = new HashMap<>();

    public void rebuildGraph() {
        buildGraphIndex = 0;
    }

    public boolean buildGraph() {
        int stepCount = 0;
        for (; buildGraphIndex < nodes.size() && stepCount < 32; buildGraphIndex++) {
            Node node = nodes.get(buildGraphIndex);

            if (node instanceof CraftNode craftNode) {
                stepCount++;
                List<ItemNode> inputNodes = new ArrayList<>();
                List<Integer> inputCounts = new ArrayList<>();
                HashSet<HashableItemNodeCount> inputsHash = new HashSet<>();

                List<ItemNode> outputNodes = new ArrayList<>();
                List<Integer> outputCounts = new ArrayList<>();
                HashSet<HashableItemNodeCount> outputsHash = new HashSet<>();

                for (ItemStack input : craftNode.craftGuideData.getAllInputItemsWithOptional()) {
                    ItemNode in = getItemNodeOrCreate(input);
                    inputNodes.add(in);
                    inputCounts.add(input.getCount());
                    inputsHash.add(new HashableItemNodeCount(in.id, input.getCount()));
                }
                for (ItemStack output : craftNode.craftGuideData.getAllOutputItems()) {
                    ItemNode in = getItemNodeOrCreate(output);
                    outputNodes.add(in);
                    outputCounts.add(output.getCount());
                    outputsHash.add(new HashableItemNodeCount(in.id, output.getCount()));
                }

                ItemSetPair hash = new ItemSetPair(inputsHash, outputsHash);
                if (!existCrafting.containsKey(hash)) {
                    existCrafting.put(hash, craftNode.id);
                } else {
                    CraftNode sameNode = (CraftNode) getNode(existCrafting.get(hash));
                    sameNode.addSame(craftNode.craftGuideData);
                    continue;
                }

                for (int i = 0; i < inputNodes.size(); i++) {
                    craftNode.addEdge(inputNodes.get(i), inputCounts.get(i));
                }
                for (int i = 0; i < outputNodes.size(); i++) {
                    outputNodes.get(i).addEdge(craftNode, outputCounts.get(i));
                }
            }
        }
        return buildGraphIndex >= nodes.size();
    }

    @Override
    public boolean processQueues() {
        if (!loopSolver.tick()) return false;
        return process();
    }

    public abstract boolean process();

    @Override
    public void restoreCurrentAndStartContext(ItemStack item, int count) {
        restoreCurrent();
        startContext(item, count);
    }

    @Override
    public void restoreCurrent() {
        existCrafting = new HashMap<>();
        for (Node node : nodes) {
            node.related = false;
            node.maxSuccess = Integer.MAX_VALUE;
            if (node instanceof ItemNode itemNode) {
                itemNode.minStepRequire = Integer.MAX_VALUE;
                itemNode.required = 0;
                itemNode.crafted = 0;
                itemNode.bestRecipeStartAtCalculating = false;
                itemNode.bestRecipeStartAt = -1;
            } else if (node instanceof CraftNode craftNode) {
                craftNode.scheduled = 0;
                craftNode.hasLoopIngredient = false;
                craftNode.sameData.clear();
                craftNode.sameData.add(craftNode.craftGuideData);
            }
        }
    }

    @Override
    public void startContext(ItemStack item, int count) {
        for (Node node : nodes) {
            if (node instanceof ItemNode itemNode) {
                itemNode.count -= itemNode.required;
                itemNode.count += itemNode.crafted;
                if (itemNode.count < 0)
                    itemNode.count = 0;
                itemNode.reinit();
            } else if (node instanceof CraftNode craftNode) {
                craftNode.scheduled = 0;
            }
        }
        loopSolver = new LoopSolver(this, getItemNodeOrCreate(item).id);
    }

    @Override
    public ICraftGraphLike createGraphWithItem(CraftAlgorithmInit<?> init) {
        List<Pair<ItemStack, Integer>> items = new ArrayList<>();
        List<CraftGuideData> craftGuides = new ArrayList<>();
        for (Node node : this.nodes) {
            if (node instanceof ItemNode itemNode) {
                items.add(new Pair<>(itemNode.itemStack, itemNode.getCurrentRemain()));
            } else if (node instanceof CraftNode craftNode) {
                craftGuides.add(craftNode.craftGuideData);
            }
        }
        return init.init(items, craftGuides);
    }

    public int getNodeCount() {
        return nodes.size();
    }
}
