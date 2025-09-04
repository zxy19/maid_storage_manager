package studio.fantasyit.maid_storage_manager.craft.algo.base;

import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.mutable.MutableInt;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.craft.algo.base.node.*;
import studio.fantasyit.maid_storage_manager.craft.algo.misc.CraftPlanEvaluator;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.work.CraftLayer;

import java.util.*;
import java.util.function.Consumer;

abstract public class HistoryAndResultGraph extends AbstractBiCraftGraph {

    public MutableInt historyId = new MutableInt();
    protected Deque<CraftResultNode> results = new LinkedList<>();
    protected ItemStack targetItem;
    protected int targetItemNodeId;
    protected int targetCount;
    protected int targetAvailable = -1;

    public HistoryAndResultGraph(List<Pair<ItemStack, Integer>> items, List<CraftGuideData> craftGuides) {
        super(items, craftGuides);
    }


    public record HistoryRecord(int historyStackId, Node node, int id, int value) {
        public final static int RECORD_CRAFTED = 0;
        public final static int RECORD_REQUIRED = 1;
        public final static int RECORD_SCHEDULED = 2;
    }

    public Stack<HistoryAndResultGraph.HistoryRecord> history = new Stack<>();

    public void pushHistory(Node node, int id, int value) {
        if (value == 0)
            return;
        this.history.push(new HistoryAndResultGraph.HistoryRecord(historyId.addAndGet(1), node, id, value));
        switch (id) {
            case HistoryAndResultGraph.HistoryRecord.RECORD_CRAFTED -> {
                ((ItemNodeBasic) node).crafted += value;
            }
            case HistoryAndResultGraph.HistoryRecord.RECORD_REQUIRED -> {
                ((ItemNodeBasic) node).required += value;
            }
            case HistoryAndResultGraph.HistoryRecord.RECORD_SCHEDULED -> {
                ((CraftNodeBasic) node).scheduled += value;
            }
        }
    }

    public void popHistoryAt(int index) {
        while (!this.history.isEmpty() && this.history.peek().historyStackId > index) {
            HistoryAndResultGraph.HistoryRecord pop = this.history.pop();
            switch (pop.id) {
                case HistoryAndResultGraph.HistoryRecord.RECORD_CRAFTED -> {
                    ((ItemNodeBasic) pop.node).crafted -= pop.value;
                }
                case HistoryAndResultGraph.HistoryRecord.RECORD_REQUIRED -> {
                    ((ItemNodeBasic) pop.node).required -= pop.value;
                }
                case HistoryAndResultGraph.HistoryRecord.RECORD_SCHEDULED -> {
                    ((CraftNodeBasic) pop.node).scheduled -= pop.value;
                }
            }
        }
    }

    public Map<Integer, Integer> popHistoryAtAndCollectChanges(int index) {
        Map<Integer, Integer> changes = new HashMap<>();
        while (!this.history.isEmpty() && this.history.peek().historyStackId > index) {
            HistoryAndResultGraph.HistoryRecord pop = this.history.pop();
            if (!changes.containsKey(pop.node.id))
                changes.put(pop.node.id, 0);
            changes.put(pop.node.id, changes.get(pop.node.id) - pop.value);
            switch (pop.id) {
                case HistoryAndResultGraph.HistoryRecord.RECORD_CRAFTED -> {
                    ((ItemNodeBasic) pop.node).crafted -= pop.value;
                }
                case HistoryAndResultGraph.HistoryRecord.RECORD_REQUIRED -> {
                    ((ItemNodeBasic) pop.node).required -= pop.value;
                }
                case HistoryAndResultGraph.HistoryRecord.RECORD_SCHEDULED -> {
                    ((CraftNodeBasic) pop.node).scheduled -= pop.value;
                }
            }
        }
        return changes;
    }

    @Override
    public void startContext(ItemStack item, int count) {
        super.startContext(item, count);
        targetItem = item;
    }

    @Override
    public void startContext(int itemNodeId, int count) {
        super.startContext(itemNodeId, count);
        targetItemNodeId = itemNodeId;
        targetCount = count;
        history.clear();
        historyId.setValue(0);
        results.clear();
        targetAvailable = -1;
    }

    public List<CraftResultNode> getRawResults() {
        return new ArrayList<>(this.results);
    }

    @Override
    public List<CraftLayer> getResults() {
        if (targetAvailable == 0) return List.of();
        if (this.results.isEmpty()) return List.of();
        List<CraftLayer> results = new ArrayList<>();
        CraftResultNode lastOne = this.results.peekLast();
        while (!this.results.isEmpty()) {
            CraftResultNode resultNode = this.results.removeFirst();
            CraftNode node = (CraftNode) getNode(resultNode.index);

            List<ItemStack> itemStacks = new ArrayList<>();
            Consumer<ItemStack> addWithCountMultiple = (itemStack) -> {
                if (itemStack.isEmpty()) return;
                int count = resultNode.count * itemStack.getCount();
                for (ItemStack existing : itemStacks) {
                    if (ItemStack.isSameItem(existing, itemStack)) {
                        existing.grow(count);
                        return;
                    }
                }
                itemStacks.add(itemStack.copyWithCount(count));
            };
            node.craftGuideData.getAllInputItemsWithOptional().forEach(addWithCountMultiple);
            CraftLayer craftLayer = new CraftLayer(
                    Optional.of(node.craftGuideData),
                    itemStacks,
                    resultNode.count
            );
            craftLayer.setUsableCraftData(node.sameData);

            results.add(craftLayer);
        }
        CraftNode lastNode = (CraftNode) getNode(lastOne.index);
        results.add(new CraftLayer(Optional.empty(),
                List.of(targetItem.copyWithCount(targetAvailable)),
                lastNode.scheduled));
        return results;
    }

    @Override
    public List<Pair<ItemStack, Integer>> getFails() {
        return nodes
                .stream()
                .filter(node -> node instanceof ItemNode)
                .map(node -> (ItemNode) node)
                .filter(node -> node.maxLack > 0)
                .map(node -> new Pair<>(node.itemStack, node.maxLack))
                .toList();
    }

    @Override
    public void setSpeed(int i) {
    }

    @Override
    public Optional<Integer> getMaxAvailable() {
        if (Config.craftingShortestPathEvaluator != CraftPlanEvaluator.NONE)
            return Optional.empty();
        if (targetAvailable == -1)
            return super.getMaxAvailable();
        return Optional.of(targetAvailable);
    }
}
