package studio.fantasyit.maid_storage_manager.craft.generator.type.base;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.MaidPathFindingBFS;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import studio.fantasyit.maid_storage_manager.craft.generator.algo.GeneratorGraph;
import studio.fantasyit.maid_storage_manager.craft.generator.config.ConfigTypes;
import studio.fantasyit.maid_storage_manager.data.InventoryItem;
import studio.fantasyit.maid_storage_manager.registry.ItemRegistry;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.util.MoveUtil;
import studio.fantasyit.maid_storage_manager.util.StorageAccessUtil;

import java.util.List;
import java.util.Map;

public interface IAutoCraftGuideGenerator {
    ResourceLocation getType();

    default boolean allowMultiPosition() {
        return false;
    }

    default boolean canCacheGraph() {
        return true;
    }

    default void init() {
    }

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

    boolean isBlockValid(Level level, BlockPos pos);

    void generate(List<InventoryItem> inventory, Level level, BlockPos pos, GeneratorGraph graph, Map<ResourceLocation, List<BlockPos>> recognizedTypePositions);

    void onCache(RecipeManager manager);

    Component getConfigName();

    default List<ConfigTypes.ConfigType<?>> getConfigurations() {
        return List.of();
    }
}