//package studio.fantasyit.maid_storage_manager.craft.algo;
//
//import net.minecraft.world.item.ItemStack;
//import org.anti_ad.a.b.a.a.a.P;
//import org.apache.commons.lang3.mutable.MutableInt;
//import org.jetbrains.annotations.Nullable;
//import oshi.util.tuples.Pair;
//import studio.fantasyit.maid_storage_manager.craft.algo.base.AbstractBiCraftGraph;
//import studio.fantasyit.maid_storage_manager.craft.algo.base.CraftResultNode;
//import studio.fantasyit.maid_storage_manager.craft.data.CraftLayer;
//
//import java.util.List;
//import java.util.Stack;
//
//public class SimpleSearchGraph extends AbstractBiCraftGraph {
//    public MutableInt historyId = new MutableInt();
//
//    protected record historyRecord(Node node, int id, int value) {
//        public historyRecord(Node node, int id, int value) {
//            this.node = node;
//            this.id = id;
//            this.value = value;
//        }
//        public static int RECORD_
//    }
//
//    public Stack<MutableInt> history = new Stack<>();
//
//    public void pushHistory(Node node, int id, int value) {
//
//    }
//
//    /**
//     * 返回最大可以完成的数量
//     * 所有步骤都放在一个DFS里进行，方便回溯消除副作用
//     *
//     * @param node
//     * @param craftNode
//     * @return
//     */
//    private int dfsItemInRecipeCalcCount(ItemNode node, CraftNode craftNode, @Nullable ItemNode root, int maxRequire, int nextItem, int nextRecipe, @Nullable List<CraftResultNode> results) {
//        if (node.getCurrentRemain() >= maxRequire) return 0;
//        if (node.edges.isEmpty()) return node.getCurrentRemain();
//        //这次合成比当前合成链上上次请求合成的要多，说明是正权环，最终节点值无法到达0，直接断开。
//        if (maxRequire > node.minStepRequire) return 0;
//        int tNodeMinRequire = node.minStepRequire;
//        //回溯备用数据
//        int tNodeCrafted = node.crafted;
//        int tNodeRequired = node.required;
//
//        //如此判断后，下方可判断Node必定为可合成的节点。且需要进行合成
//        //此处处理新加入物品的合成配方
//        //从第一合成开始搜索
//        int actuallyAvailable = dfsItemInRecipeCalcCount(node);
//        node.required += actuallyAvailable;
//
//        if (maxRequire != 0) {
//            //当前物品是合成树的最后一个物品
//            if (nextItem >= craftNode.edges.size()) {
//                return maxRequire;
//            } else {
//                maxRequire = dfsItemInRecipeCalcCount(node, craftNode, root, maxRequire, nextItem + 1, results);
//            }
//        }
//
//        node.minStepRequire = tNodeMinRequire;
//        return maxRequire;
//    }
//
//    @Override
//    public void startContext(ItemStack item, int count) {
//
//
//    }
//
//    @Override
//    public List<CraftLayer> getResults() {
//        return null;
//    }
//
//    @Override
//    public List<Pair<ItemStack, Integer>> getFails() {
//        return null;
//    }
//
//    @Override
//    public void setSpeed(int i) {
//
//    }
//
//    @Override
//    public boolean processQueues() {
//        return false;
//    }
//}
