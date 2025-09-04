package studio.fantasyit.maid_storage_manager.storage.base;

import net.minecraft.world.item.ItemStack;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 可抽取物品上下文
 */
public interface IStorageExtractableContext extends IStorageContext,IAsyncContext<Function<ItemStack, ItemStack>> {
    /**
     * 设置可抽取物品
     * @param itemList 可抽取物品
     * @param matchNbt 是否匹配nbt
     */
    void setExtract(List<ItemStack> itemList, ItemStackUtil.MATCH_TYPE matchNbt);

    /**
     * 根据存储的物品决定是否抽取
     * @param predicate 抽取条件
     */
    void setExtractByExisting(Predicate<ItemStack> predicate);
}