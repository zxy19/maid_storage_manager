package studio.fantasyit.maid_storage_manager.craft.generator.type.botania;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import studio.fantasyit.maid_storage_manager.craft.generator.algo.ICachableGeneratorGraph;
import studio.fantasyit.maid_storage_manager.craft.generator.type.base.IAutoCraftGuideGenerator;
import studio.fantasyit.maid_storage_manager.data.InventoryItem;

import java.util.List;
import java.util.Map;

public class GeneratorBotaniaRunicAltar implements IAutoCraftGuideGenerator {
    @Override
    public @NotNull ResourceLocation getType() {
        return ;
    }

    @Override
    public boolean isBlockValid(Level level, BlockPos pos) {
        return false;
    }

    @Override
    public void generate(List<InventoryItem> inventory, Level level, BlockPos pos, ICachableGeneratorGraph graph, Map<ResourceLocation, List<BlockPos>> recognizedTypePositions) {

    }

    @Override
    public void onCache(RecipeManager manager) {

    }

    @Override
    public Component getConfigName() {
        return null;
    }
}
