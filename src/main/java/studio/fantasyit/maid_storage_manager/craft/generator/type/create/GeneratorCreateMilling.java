package studio.fantasyit.maid_storage_manager.craft.generator.type.create;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.kinetics.millstone.MillingRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;

public class GeneratorCreateMilling extends GeneratorCreate<MillingRecipe, RecipeType<MillingRecipe>, RecipeWrapper> {
    @Override
    public ResourceLocation getType() {
        return new ResourceLocation(MaidStorageManager.MODID, "create_milling");
    }

    @Override
    public boolean isBlockValid(Level level, BlockPos pos) {
        if (level.getBlockState(pos).is(AllBlocks.MILLSTONE.get())) {
            return true;
        }
        return false;
    }
    @Override
    RecipeType<MillingRecipe> getRecipeType() {
        return AllRecipeTypes.MILLING.getType();
    }
}