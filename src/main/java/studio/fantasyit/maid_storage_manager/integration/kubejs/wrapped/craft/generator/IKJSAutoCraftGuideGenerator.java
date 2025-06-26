package studio.fantasyit.maid_storage_manager.integration.kubejs.wrapped.craft.generator;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.MaidPathFindingBFS;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import studio.fantasyit.maid_storage_manager.craft.generator.config.ConfigTypes;
import studio.fantasyit.maid_storage_manager.data.InventoryItem;
import studio.fantasyit.maid_storage_manager.integration.kubejs.helper.CacheOperator;
import studio.fantasyit.maid_storage_manager.integration.kubejs.helper.GraphOperator;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public interface IKJSAutoCraftGuideGenerator {
    ResourceLocation type();

    boolean isBlockValid(Level level, BlockPos pos);

    void generate(List<InventoryItem> inventory, Level level, BlockPos pos, GraphOperator operator, Map<ResourceLocation, List<BlockPos>> recognizedTypePositions);

    void onCache(RecipeManager manager, CacheOperator operator);

    Component configName();

    interface Full extends IKJSAutoCraftGuideGenerator {
        boolean allowMultiPosition();

        boolean canCacheGraph();

        boolean positionalAvailable(Supplier<Boolean> parent, ServerLevel level, EntityMaid maid, BlockPos pos, MaidPathFindingBFS pathFinding);

        List<ConfigTypes.ConfigType<?>> configurations();
    }
}
