package studio.fantasyit.maid_storage_manager.craft;

import net.minecraft.CharPredicate;
import net.minecraft.world.item.ItemStack;
import oshi.util.tuples.Pair;

import java.util.*;
import java.util.function.Consumer;

public class AvailableCraftGraph {
    public static class CraftResultNode {


        public int index;
        public int count;
        public boolean related;

        public CraftResultNode(int index, int count, boolean related) {
            this.index = index;
            this.count = count;
            this.related = related;
        }

    }

    private int taskPerTick = 32;
    List<ItemStack> items;
    List<Integer> counts;
    List<CraftGuideData> craftGuideData;
    List<List<Pair<Integer, Integer>>> edges;
    List<Integer> inDegree = new ArrayList<>();

    //context related var
    //拓扑图队列
    Queue<CraftResultNode> queue = new java.util.LinkedList<>();
    //当前需要的总数
    List<Integer> totalRequire;
    List<Integer> currentRequire;
    List<Boolean> isRelated;
    Stack<CraftResultNode> results;
    Set<Integer> checkNodes;
    int buildIdx = 0;
    //正在建图的物品ID
    int contextItemIdx = 0;
    //当前上下文需要结果物品数量
    int contextRequireCount;

    private int item(int i) {
        return i + craftGuideData.size();
    }

    private int craft(int i) {
        return i;
    }

    private int getItemIndex(ItemStack itemStack) {
        for (int i = 0; i < this.items.size(); i++) {
            if (ItemStack.isSameItem(this.items.get(i), itemStack))
                return i;
        }
        return -1;
    }

    public AvailableCraftGraph(List<com.mojang.datafixers.util.Pair<ItemStack, Integer>> items,
                               List<CraftGuideData> craftGuideData) {
        this.items = new ArrayList<>();
        this.counts = new ArrayList<>();
        this.craftGuideData = new ArrayList<>();
        for (CraftGuideData inComing : craftGuideData) {
            boolean duplicate = false;
            for (CraftGuideData existing : this.craftGuideData) {
                if (existing.getOutput().getItems().stream().anyMatch(
                        i1 ->
                                inComing.getOutput().getItems().stream().anyMatch(
                                        i2 -> ItemStack.isSameItem(i1, i2) && !i1.isEmpty()
                                )
                )) {
                    duplicate = true;
                    break;
                }
            }
            if (!duplicate) {
                this.craftGuideData.add(inComing);
            }
        }
        this.edges = new ArrayList<>();
        this.totalRequire = new ArrayList<>();
        this.currentRequire = new ArrayList<>();
        this.isRelated = new ArrayList<>();
        for (int i = 0; i < craftGuideData.size(); i++) {
            edges.add(new ArrayList<>());
            counts.add(0);
            inDegree.add(0);
            totalRequire.add(0);
        }
        for (com.mojang.datafixers.util.Pair<ItemStack, Integer> itemStackIntegerPair : items) {
            ItemStack itemStack = itemStackIntegerPair.getFirst();
            if (itemStack.isEmpty()) continue;
            this.counts.set(item(this.addItemNode(itemStack.copyWithCount(1))), itemStackIntegerPair.getSecond());
        }
        //初始情况下，不要建图
        buildIdx = craftGuideData.size();
        contextItemIdx = -1;
        contextRequireCount = 0;
    }

    public void setSpeed(int taskPerTick) {
        this.taskPerTick = taskPerTick;
    }

    public void setCount(ItemStack itemStack, int count) {
        int idx = getItemIndex(itemStack);
        if (idx == -1)
            idx = addItemNode(itemStack);
        counts.set(item(idx), count);
    }

    public void addEdge(int from, int to, int count) {
        this.edges.get(from).add(new Pair<>(to, count));
        inDegree.set(to, inDegree.get(to) + 1);
    }

    protected int addItemNode(ItemStack itemStack) {
        items.add(itemStack);
        edges.add(new ArrayList<>());
        counts.add(0);
        inDegree.add(0);
        isRelated.add(false);
        totalRequire.add(0);
        currentRequire.add(0);
        return items.size() - 1;
    }

    public boolean buildGraph() {
        int count = 0;
        for (; buildIdx < craftGuideData.size(); buildIdx++) {
            if (count++ > this.taskPerTick) return false;
            CraftGuideData cgd = this.craftGuideData.get(buildIdx);
            List<ItemStack> items1 = cgd.output.items;
            for (int j = 0; j < items1.size(); j++) {
                ItemStack item = items1.get(j);
                if (item.isEmpty()) continue;
                int idx = getItemIndex(item);
                if (idx == -1) {
                    idx = addItemNode(item);
                }
                addEdge(item(idx), craft(buildIdx), item.getCount());
            }
            List<ItemStack> items2 = cgd.input1.items;
            for (int j = 0; j < items2.size(); j++) {
                ItemStack item = items2.get(j);
                if (item.isEmpty()) continue;
                int idx = getItemIndex(item);
                if (idx == -1) {
                    idx = addItemNode(item);
                }
                addEdge(craft(buildIdx), item(idx), item.getCount());
            }
            List<ItemStack> items3 = cgd.input2.items;
            for (int j = 0; j < items3.size(); j++) {
                ItemStack item = items3.get(j);
                if (item.isEmpty()) continue;
                int idx = getItemIndex(item);
                if (idx == -1) {
                    idx = addItemNode(item);
                }
                addEdge(craft(buildIdx), item(idx), item.getCount());
            }
        }
        if (contextItemIdx != -1) {
            addRequire(item(contextItemIdx), contextRequireCount);
            isRelated.set(item(contextItemIdx), true);
            queue.add(new CraftResultNode(item(contextItemIdx), contextRequireCount, true));

            // 添加所有的入度为0的边
            for (int i = 0; i < inDegree.size(); i++) {
                if (i != item(contextItemIdx) && inDegree.get(i) == 0) {
                    queue.add(new CraftResultNode(i, counts.get(i), false));
                }
            }
            contextItemIdx = -1;
        }

        return true;
    }
    public void reverseCurrentAndStartContext(ItemStack item, int count) {
        for (int i = 0; i < currentRequire.size(); i++) {
            totalRequire.set(i, totalRequire.get(i) - currentRequire.get(i));
        }
        startContext(item, count);
    }
    public void startContext(ItemStack item, int count) {
        int idx = getItemIndex(item);
        if (idx == -1) {
            idx = addItemNode(item);
        }

        checkNodes = new HashSet<>();
        checkNodes.add(item(idx));
        results = new Stack<>();

        buildIdx = 0;
        contextItemIdx = idx;
        contextRequireCount = count;


        //initialize
        isRelated = new ArrayList<>();
        currentRequire = new ArrayList<>();
        inDegree = new ArrayList<>();
        for (int i = 0; i < counts.size(); i++) {
            inDegree.add(0);
            isRelated.add(false);
            currentRequire.add(0);
            edges.get(i).clear();
        }
    }

    private void addRequire(int i, int count) {
        if (!isItem(i)) {
            currentRequire.set(i, Math.max(currentRequire.get(i), count));
            totalRequire.set(i, Math.max(totalRequire.get(i), count));
        } else {
            currentRequire.set(i, currentRequire.get(i) + count);
            totalRequire.set(i, totalRequire.get(i) + count);
        }
    }

    public boolean processQueues() {
        int dropCount = 0;
        while (!queue.isEmpty()) {
            //队列中的元素保证入度为0
            if (dropCount++ > 32) return false;
            CraftResultNode pair = queue.poll();
            if (pair.related && !isItem(pair.index) && currentRequire.get(pair.index) > 0)
                results.push(pair);

            //计算出抵扣完成后任然需要的数量
            // lastRest = count - (total - current)
            // currentReq = lastRest - current;
            int lastRest = Math.max(0, counts.get(pair.index) - (totalRequire.get(pair.index) - currentRequire.get(pair.index)));
            int needCount = Math.max(0, currentRequire.get(pair.index) - lastRest);
            //如果仍然需要进一步合成，则当前节点不影响检查结果。但是当前节点的所有前置节点都会影响
            boolean isCurrentChecking = checkNodes.contains(pair.index);
            if (!edges.get(pair.index).isEmpty()) {
                checkNodes.remove(pair.index);
            }
            for (Pair<Integer, Integer> edge : edges.get(pair.index)) {
                int to = edge.getA();
                int count2 = edge.getB();
                if (isCurrentChecking)
                    checkNodes.add(to);
                //相关性传播，与被合成物相关的都需要标记
                isRelated.set(to, isRelated.get(to) || pair.related);
                inDegree.set(to, inDegree.get(to) - 1);
                if (isItem(to))
                    addRequire(to, needCount * count2);
                else
                    addRequire(to, (needCount + count2 - 1) / count2);
                if (inDegree.get(to) == 0) {
                    queue.add(new CraftResultNode(to, currentRequire.get(to), isRelated.get(to)));
                }
            }
        }
        return true;
    }

    private boolean isItem(int to) {
        return to >= craftGuideData.size();
    }

    private ItemStack getItem(int to) {
        return items.get(to - craftGuideData.size());
    }

    public List<CraftLayer> getResults() {
        //影响结果的节点中，如果任何一个节点的可用数量小于实际，则不返回结果
        if (checkNodes.stream().anyMatch(index -> counts.get(index) < totalRequire.get(index))) {
            return null;
        }
        List<CraftLayer> res = new ArrayList<>();
        CraftGuideData lastOne = null;
        int lastOneIndex = 0;
        while (!results.isEmpty()) {
            CraftResultNode resultNode = results.pop();
            if (isItem(resultNode.index)) continue;

            lastOne = this.craftGuideData.get(resultNode.index);
            lastOneIndex = resultNode.index;
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
            lastOne.getInput1().items.forEach(addWithCountMultiple);
            lastOne.getInput2().items.forEach(addWithCountMultiple);
            res.add(new CraftLayer(
                    Optional.of(lastOne),
                    itemStacks,
                    resultNode.count
            ));
        }
        if (lastOne != null) {
            int finalLastOneIndex = lastOneIndex;
            res.add(new CraftLayer(Optional.empty(),
                    lastOne.getOutput().items
                            .stream()
                            .filter(itemStack -> !itemStack.isEmpty())
                            .map(itemStack -> itemStack.copyWithCount(currentRequire.get(finalLastOneIndex) * itemStack.getCount()))
                            .toList(),
                    contextRequireCount));
        }
        return res;
    }

    public List<Pair<ItemStack, Integer>> getFails() {
        return checkNodes.stream()
                .filter(index -> counts.get(index) < totalRequire.get(index) && isItem(index))
                .map(idx -> new Pair<>(getItem(idx),
                        totalRequire.get(idx) - counts.get(idx)))
                .toList();
    }

}
