package studio.fantasyit.maid_storage_manager.craft.generator.type.base;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.MaidPathFindingBFS;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import studio.fantasyit.maid_storage_manager.craft.generator.algo.GeneratorGraph;
import studio.fantasyit.maid_storage_manager.craft.generator.config.ConfigTypes;
import studio.fantasyit.maid_storage_manager.data.InventoryItem;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.util.MoveUtil;
import studio.fantasyit.maid_storage_manager.util.StorageAccessUtil;

import java.util.List;
import java.util.Map;

/**
 * 自动合成指南生成器
 */
public interface IAutoCraftGuideGenerator {
    /**
     * 获取合成指南生成器类型。建议取RecipeType或者CraftType
     *
     * @return 合成指南生成器类型
     */
    @NotNull ResourceLocation getType();

    /**
     * 允许为多个位置生成合成指南。如果否则只为距离女仆最近的位置生成。
     *
     * @return
     */
    default boolean allowMultiPosition() {
        return false;
    }

    /**
     * 是否允许缓存图时保留合成指南。如果该类型的生成随方块数据而变化（即方块不消失的情况下也可能生成不同的类型），则需要关闭缓存
     *
     * @return 是否允许缓存
     */
    default boolean canCacheGraph() {
        return true;
    }

    /**
     * 生成器初始化。表示即将开始遍历方块
     */
    default void init() {
    }

    /**
     * 位置是否合法。用于判断和世界相关的成立条件。
     *
     * @param level       世界
     * @param maid        女仆
     * @param pos         位置
     * @param pathFinding 快速寻路对象
     * @return 是否合法
     */
    default boolean positionalAvailable(ServerLevel level, EntityMaid maid, BlockPos pos, MaidPathFindingBFS pathFinding) {
        if (StorageAccessUtil
                .getMarksWithSameContainer((ServerLevel) maid.level(), Target.virtual(pos, null))
                .stream()
                .anyMatch(t -> t.getB().is(ItemRegistry.NO_ACCESS.get())))
            return false;
        if (MoveUtil.getAllAvailablePosForTarget((ServerLevel) maid.level(), maid, pos, pathFinding).isEmpty())
            return false;
        return true;
    }

    /**
     * 方块是否合法。用于判断和世界无关的成立条件
     *
     * @param level 世界
     * @param pos   方块位置
     * @return 是否合法
     */
    boolean isBlockValid(Level level, BlockPos pos);

    /**
     * 生成配方。生成时请调用 GeneratorGraph#addNode() 添加配方。当配方被认为需要时，将会调用最后一个参数生成合成指南。
     *
     * @param inventory               女仆仓库
     * @param level                   世界
     * @param pos                     位置
     * @param graph                   生成图
     * @param recognizedTypePositions 已经生成过的生成器类型和位置
     */
    void generate(List<InventoryItem> inventory, Level level, BlockPos pos, GeneratorGraph graph, Map<ResourceLocation, List<BlockPos>> recognizedTypePositions);

    /**
     * 缓存回调。缓存发生在数据包加载后。你可以在此为配方添加缓存。<b>缓存的配方原料必须和下次添加同ID配方时完全一致，否则可能出现不可预料的错误</b>
     * @param manager 配方管理器
     */
    void onCache(RecipeManager manager);

    /**
     * 获取配置项名字。显示在Cloth Config中
     * @return 配置项名字
     */
    Component getConfigName();

    /**
     * 返回配置项目。
     * @see studio.fantasyit.maid_storage_manager.craft.generator.type.create.GeneratorCreateFanRecipes
     * @return 配置项目
     */
    default List<ConfigTypes.ConfigType<?>> getConfigurations() {
        return List.of();
    }
}