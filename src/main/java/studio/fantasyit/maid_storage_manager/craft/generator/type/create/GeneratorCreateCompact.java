package studio.fantasyit.maid_storage_manager.craft.generator.type.create;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;

public class GeneratorCreateCompact extends GeneratorCreate<BasinRecipe, RecipeType<BasinRecipe>, Container> {
    @Override
    public ResourceLocation getType() {
        return new ResourceLocation(MaidStorageManager.MODID, "create_press_pot");
    }

    @Override
    public boolean isBlockValid(Level level, BlockPos pos) {
        if (level.getBlockState(pos).is(AllBlocks.BASIN.get()) && level.getBlockState(pos.above().above()).is(AllBlocks.MECHANICAL_PRESS.get())) {
            return true;
        }
        return false;
    }

    @Override
    RecipeType<BasinRecipe> getRecipeType() {
        return AllRecipeTypes.BASIN.getType();
    }
}