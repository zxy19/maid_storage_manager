package studio.fantasyit.maid_storage_manager.storage.base;

import net.minecraft.world.item.ItemStack;

/**
 * 支持过滤
 */
public interface IFilterable {
    /**
     * 过滤器是否接受物品
     * @param itemStack 物品
     * @return 是否接受
     */
    boolean isAvailable(ItemStack itemStack);
    /**
     * 是否白名单
     */
    boolean isWhitelist();
}