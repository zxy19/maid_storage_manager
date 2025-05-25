package studio.fantasyit.maid_storage_manager.craft.algo.base;

import net.minecraft.world.item.ItemStack;
import oshi.util.tuples.Pair;
import studio.fantasyit.maid_storage_manager.craft.data.CraftLayer;

import java.util.List;

public interface ICraftGraphLike {

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
}
