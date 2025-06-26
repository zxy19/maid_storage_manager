package studio.fantasyit.maid_storage_manager.storage.base;

import net.minecraft.world.item.ItemStack;

import java.util.function.Function;

/**
 * 交互物品上下文
 */
public interface IStorageInteractContext extends IStorageContext {
    /**
     * 处理物品。
     * @param process 处理并返回剩余的物品
     */
    void tick(Function<ItemStack, ItemStack> process);
}
