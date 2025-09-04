package studio.fantasyit.maid_storage_manager.craft.algo.misc;

import org.apache.commons.lang3.mutable.MutableInt;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.Config;
import studio.fantasyit.maid_storage_manager.craft.algo.base.AbstractBiCraftGraph;
import studio.fantasyit.maid_storage_manager.craft.algo.base.node.CraftNodeBasic;
import studio.fantasyit.maid_storage_manager.craft.algo.base.node.ItemNodeBasic;
import studio.fantasyit.maid_storage_manager.craft.algo.base.node.Node;
import studio.fantasyit.maid_storage_manager.craft.debug.CraftingDebugContext;
import studio.fantasyit.maid_storage_manager.craft.debug.IDebugContextSetter;

import java.util.*;

public class LoopSolver implements IDebugContextSetter {
    private AbstractBiCraftGraph graph;
    Stack<Pair<Integer, MutableInt>> queue = new Stack<>();
    List<Integer> path = new LinkedList<>();
    HashSet<Long> used = new HashSet<>();
    HashMap<Integer, Integer> visited = new HashMap<>();
    private CraftingDebugContext debugContext = CraftingDebugContext.Dummy.INSTANCE;

    public long compoundToLong(int a, int b) {
        return (((long) a) << 32) | (b & 0xffffffffL);
    }

    public LoopSolver(AbstractBiCraftGraph graph, int startNodeId) {
        this.graph = graph;
        queue.add(new Pair<>(startNodeId, new MutableInt(0)));
        path.add(startNodeId);
        visited.put(startNodeId, 0);
        debugContext.logNoLevel(CraftingDebugContext.TYPE.LOOP_RESOLVER, "LoopSolver created");
    }

    public boolean tick() {
        int c = 0;
        while (!queue.isEmpty()) {
            if (c++ > 1000)
                return false;
            Pair<Integer, MutableInt> nodeLayer = queue.peek();
            int nodeId = nodeLayer.getA();
            MutableInt index = nodeLayer.getB();
            Node node = graph.getNode(nodeId);
            if (node.edges.size() <= index.intValue()) {
                if (node instanceof CraftNodeBasic craftNode) {
                    craftNode.hasLoopIngredient = craftNode.edges
                            .stream().anyMatch(edge -> ((ItemNodeBasic) graph.getNode(edge.getA())).isLoopedIngredient);
                }
                visited.put(node.id, visited.get(node.id) - 1);
                queue.pop();
                path.remove(path.size() - 1);
                continue;
            }
            Pair<Integer, Integer> edge = node.edges.get(index.intValue());
            Node toNode = graph.getNode(edge.getA());
            index.add(1);

            if (toNode instanceof ItemNodeBasic && path.contains(toNode.id)) {
                if (!used.contains(compoundToLong(toNode.id, node.id))) {
                    used.add(compoundToLong(toNode.id, node.id));
                    processLoop(path.indexOf(toNode.id));
                    c += 100;
                }
            } else if (toNode instanceof ItemNodeBasic && visited.containsKey(toNode.id)) {
                continue;
            } else {
                queue.add(new Pair<>(toNode.id, new MutableInt(0)));
                visited.put(toNode.id, visited.computeIfAbsent(toNode.id, t -> 0) + 1);
                path.add(toNode.id);
            }
        }
        debugContext.logNoLevel(CraftingDebugContext.TYPE.LOOP_RESOLVER, "LoopSolver end");
        return true;
    }

    private void processLoop(int startNode) {
        if (path.size() - startNode > Config.craftingLoopSolverMaxSize) {
            return;
        }
        boolean isSelfProductLoop = false;
        boolean isMainBranchLoop = true;
        boolean hasLoop = false;
        int startCount = -1;
        int finallyGain = -1;
        int[] counts = new int[path.size()];
        for (int c = 1; c < 64; c++) {
            int currentCount = c;
            for (int i = path.size(); i > startNode; i--) {
                Node node = graph.getNode((i == path.size()) ? path.get(startNode) : path.get(i));
                Node nextNode = graph.getNode(path.get(i - 1));

                if (node instanceof CraftNodeBasic craftNode && nextNode instanceof ItemNodeBasic nextItemNode) {
                    for (Pair<Integer, Integer> n : craftNode.revEdges) {
                        if (n.getA() == nextNode.id) currentCount = currentCount * n.getB();
                    }
                } else if (node instanceof ItemNodeBasic itemNode && nextNode instanceof CraftNodeBasic craftNode) {
                    for (Pair<Integer, Integer> n : itemNode.revEdges) {
                        if (n.getA() == nextNode.id) currentCount = currentCount / n.getB();
                    }
                } else throw new RuntimeException("Invalid graph");

                counts[i - 1] = currentCount;

                if (visited.get(nextNode.id) > 1 || visited.get(node.id) > 1) {
                    isMainBranchLoop = false;
                }
            }
            if (currentCount >= c)
                hasLoop = true;
            if (currentCount > c) {
                isSelfProductLoop = true;
                startCount = c;
                finallyGain = currentCount;
                break;
            }
        }

        if (!hasLoop)
            return;

        debugContext.logNoLevel(CraftingDebugContext.TYPE.LOOP_RESOLVER,
                "Loop From %s,size %s,isSelfProductLoop %s,isMainBranchLoop %s,startCount %s,gain %s",
                graph.getNode(path.get(startNode)),
                path.size() - startNode,
                isSelfProductLoop,
                isMainBranchLoop,
                startCount,
                finallyGain
        );
        if (startCount != -1 && hasIndirectItemConsumeOrUnexpectedSubProd(startNode, startCount)) {
            return;
        }

        if (isSelfProductLoop) {
            ItemNodeBasic node = (ItemNodeBasic) graph.getNode(path.get(startNode));
            if (node.loopInputIngredientCount == 0)
                node.loopInputIngredientCount = startCount;
            node.loopInputIngredientCount = Math.min(startCount, node.loopInputIngredientCount);
            node.singleTimeCount = finallyGain;
            node.isLoopedIngredient = true;
            debugContext.logNoLevel(CraftingDebugContext.TYPE.LOOP_RESOLVER, "%s -> single(%s)", node, node.singleTimeCount);
            for (int i = startNode + 2; i < path.size(); i += 2) {
                ItemNodeBasic walkNode = (ItemNodeBasic) graph.getNode(path.get(i));
                //环上所有节点也进行设置
                if (walkNode.loopInputIngredientCount == 0)
                    walkNode.loopInputIngredientCount = counts[i];
                walkNode.loopInputIngredientCount = Math.min(counts[i], walkNode.loopInputIngredientCount);
                walkNode.isLoopedIngredient = true;
                walkNode.singleTimeCount = counts[i];
                debugContext.logNoLevel(CraftingDebugContext.TYPE.LOOP_RESOLVER, "* %s -> single(%s)", walkNode, walkNode.singleTimeCount);
            }
        } else {
            ItemNodeBasic node = (ItemNodeBasic) graph.getNode(path.get(startNode));
            //对于主分支非自增环，其实际上没有意义（因为所求直接就是产物，那么循环也不会带来任何收益，直接设置false）
            if (!isMainBranchLoop) {
                node.isLoopedIngredient = true;
                debugContext.logNoLevel(CraftingDebugContext.TYPE.LOOP_RESOLVER, "%s -> loop", node);
            }
        }
    }

    public boolean hasIndirectItemConsumeOrUnexpectedSubProd(int startNode, int startCount) {
        Map<Integer, Integer> inputs = new HashMap<>();
        Map<Integer, Integer> outputs = new HashMap<>();
        int currentCount = startCount;
        for (int i = path.size(); i > startNode; i--) {
            Node node = graph.getNode((i == path.size()) ? path.get(startNode) : path.get(i));
            Node nextNode = graph.getNode(path.get(i - 1));
            if (node instanceof CraftNodeBasic craftNode && nextNode instanceof ItemNodeBasic nextItemNode) {
                for (Pair<Integer, Integer> n : craftNode.revEdges) {
                    if (n.getA() == nextNode.id) {
                        //当前合成将进行的次数
                        int currentStepRepeat = currentCount;
                        //计算当前步骤结束后产物的数量
                        currentCount = currentCount * n.getB();
                        int finallyGainItemCount = currentCount;

                        //当前步骤的输入。优先从之前的输出获取。如果不足，则标记从环外获取。
                        // edge 即 required。也就是配方输入
                        craftNode.edges.forEach(_t -> {
                            int removeCount = _t.getB() * currentStepRepeat;
                            int restCount = Math.max(0, removeCount - outputs.getOrDefault(_t.getA(), 0));
                            int restOutput = Math.max(0, outputs.getOrDefault(_t.getA(), 0) - removeCount);
                            outputs.put(_t.getA(), restOutput);
                            inputs.put(_t.getA(), inputs.getOrDefault(_t.getA(), 0) + restCount);
                        });
                        if (Config.craftingLoopSolverPreventIndirect) {
                            //如果当前出物品数量已经小于当前从环外获取的物品数量，则意味着当前物品相当于全部从环外获取，也就是“间接物品获取”
                            if (inputs.getOrDefault(nextItemNode.id, -1) >= finallyGainItemCount) {
                                debugContext.logNoLevel(CraftingDebugContext.TYPE.LOOP_RESOLVER,
                                        "Indirect Item Consume %s",
                                        nextItemNode
                                );
                                return true;
                            }
                        }
                        //移除输入，添加输出
                        craftNode.revEdges.forEach(_t -> {
                            int addCount = _t.getB() * currentStepRepeat;
                            int restCount = Math.max(0, addCount - inputs.getOrDefault(_t.getA(), 0));
                            int restInput = Math.max(0, inputs.getOrDefault(_t.getA(), 0) - addCount);
                            inputs.put(_t.getA(), restInput);
                            outputs.put(_t.getA(), outputs.getOrDefault(_t.getA(), 0) + restCount);
                        });

                        break;
                    }
                }

            } else if (node instanceof ItemNodeBasic itemNode && nextNode instanceof CraftNodeBasic craftNode) {
                for (Pair<Integer, Integer> n : itemNode.revEdges) {
                    if (n.getA() == nextNode.id) currentCount = currentCount / n.getB();
                }
            } else throw new RuntimeException("Invalid graph");
        }

        if (Config.craftingLoopSolverPreventNewByProduct) {
            int startId = path.get(startNode);
            for (Map.Entry<Integer, Integer> byprod : outputs.entrySet()) {
                ItemNodeBasic itemNode = (ItemNodeBasic) graph.getNode(byprod.getKey());
                if (itemNode == null || itemNode.getCurrentRemain() == 0) {
                    if (byprod.getKey() != startId) {
                        debugContext.logNoLevel(CraftingDebugContext.TYPE.LOOP_RESOLVER,
                                "Unexpected By Product %s",
                                itemNode
                        );
                        return true;
                    }
                }
            }
        }


        return false;
    }


    @Override
    public void setDebugContext(CraftingDebugContext context) {
        this.debugContext = context;
    }
}
