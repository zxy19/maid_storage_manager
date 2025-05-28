package studio.fantasyit.maid_storage_manager.craft.algo.base;

import net.minecraft.world.item.ItemStack;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftLayer;

import java.util.List;
import java.util.Optional;

public interface ICraftGraphLike {
    @FunctionalInterface
    interface CraftAlgorithmInit<T extends ICraftGraphLike> {
        T init(List<Pair<ItemStack, Integer>> items, List<CraftGuideData> craftGuides);
    }

    /**
     * 回滚当前的并且重新开始context
     */
    void restoreCurrentAndStartContext(ItemStack item, int count);

    /**
     * 回滚当前的
     */
    void restoreCurrent();

    /**
     * 开始context
     */
    void startContext(ItemStack item, int count);

    /**
     * 设置物品数量
     *
     * @param itemStack
     * @param count
     */
    void setItemCount(ItemStack itemStack, int count);

    /**
     * 添加物品计数
     *
     * @param itemStack
     * @param count
     */
    void addItemCount(ItemStack itemStack, int count);

    /**
     * 建图回调
     */
    boolean buildGraph();

    /**
     * 获取结果
     */
    List<CraftLayer> getResults();

    /**
     * 获取收集失败的物品
     */
    List<Pair<ItemStack, Integer>> getFails();

    void setSpeed(int i);

    boolean processQueues();

    /**
     * 是否应该从此时开始使用单物品处理模式（特指某些算法中无法处理循环的情况）
     */
    default boolean shouldStartUsingSingleItemProcess() {
        return false;
    }

    /**
     * 复制，迁移到其他算法
     * @param init
     * @return
     */
    ICraftGraphLike createGraphWithItem(CraftAlgorithmInit<?> init);

    /**
     * 添加剩余物品。如果算法不能自己统计合成余产物，此处接收
     * @param item
     * @param count
     */
    default void addRemainItem(ItemStack item, int count) {
    }

    /**
     * 如果当前算法可以统计成功合成数量，返回
     * @return
     */
    default Optional<Integer> getMaxAvailable() {
        return Optional.empty();
    }
}