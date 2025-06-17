package studio.fantasyit.maid_storage_manager.craft.generator;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import studio.fantasyit.maid_storage_manager.craft.generator.algo.GeneratorGraph;
import studio.fantasyit.maid_storage_manager.data.InventoryItem;

import java.util.List;

public interface IAutoCraftGuideGenerator {
    ResourceLocation getType();

    boolean isBlockValid(Level level, BlockPos pos);

    void generate(List<InventoryItem> inventory, Level level, BlockPos pos, GeneratorGraph graph);

    void onCache(RecipeManager manager);
}