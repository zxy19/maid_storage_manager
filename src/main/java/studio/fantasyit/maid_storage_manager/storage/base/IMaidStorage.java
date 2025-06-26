package studio.fantasyit.maid_storage_manager.storage.base;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.maid.memory.ViewedInventoryMemory;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;
import studio.fantasyit.maid_storage_manager.storage.Target;

import java.util.List;

/**
 * 存储类型
 */
public interface IMaidStorage {
    /**
     * 获取存储类型
     *
     * @return 存储类型
     */
    ResourceLocation getType();

    /**
     * 是否支持收集物品
     */
    default boolean supportCollect() {
        return true;
    }

    /**
     * 是否支持放置物品
     */
    default boolean supportPlace() {
        return true;
    }

    /**
     * 是否支持查看物品
     */
    default boolean supportView() {
        return true;
    }

    /**
     * 目标是否为当前类型的存储
     *
     * @param level 服务器世界
     * @param maid  maid
     * @param block 位置
     * @param side  交互面
     * @return 是否为当前类型的存储
     */
    boolean isValidTarget(ServerLevel level, LivingEntity maid, BlockPos block, @Nullable Direction side);

    /**
     * 开始收集物品上下文
     *
     * @param level   服务器世界
     * @param maid    maid
     * @param storage 存储对象
     * @return 存储上下文
     */
    @Nullable IStorageContext onStartCollect(ServerLevel level, EntityMaid maid, Target storage);

    /**
     * 开始放置物品上下文
     *
     * @param level   服务器世界
     * @param maid    maid
     * @param storage 存储对象
     * @return 存储上下文
     */
    @Nullable IStorageContext onStartPlace(ServerLevel level, EntityMaid maid, Target storage);

    /**
     * 查看物品上下文
     *
     * @param level   服务器世界
     * @param maid    maid
     * @param storage 存储对象
     * @return 存储上下文
     */
    @Nullable IStorageContext onStartView(ServerLevel level, EntityMaid maid, Target storage);

    /**
     * 预处理过滤上下文
     *
     * @param level   服务器世界
     * @param maid    maid
     * @param storage 存储对象
     * @return 存储上下文
     */
    default @Nullable IStorageContext onPreviewFilter(ServerLevel level, EntityMaid maid, Target storage) {
        return null;
    }

    /**
     * 是否合成指南提供者
     *
     * @param blockPos 位置
     * @return 是否
     */
    default boolean isCraftGuideProvider(List<ViewedInventoryMemory.ItemCount> blockPos) {
        return blockPos
                .stream()
                .anyMatch(i -> i.getItem().is(ItemRegistry.CRAFT_GUIDE.get()));
    }
}