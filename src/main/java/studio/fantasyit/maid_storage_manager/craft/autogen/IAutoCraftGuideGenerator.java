package studio.fantasyit.maid_storage_manager.craft.autogen;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.data.InventoryItem;

import java.util.List;

public interface IAutoCraftGuideGenerator {
    ResourceLocation getType();

    boolean isBlockValid(Level level, BlockPos pos);

    List<CraftGuideData> generate(List<InventoryItem> inventory, Level level, BlockPos pos);
}