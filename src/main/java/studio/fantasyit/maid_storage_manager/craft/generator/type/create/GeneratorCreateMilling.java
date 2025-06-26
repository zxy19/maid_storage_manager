package studio.fantasyit.maid_storage_manager.craft.generator.type.create;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.kinetics.millstone.MillingRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import org.jetbrains.annotations.NotNull;

public class GeneratorCreateMilling extends GeneratorCreate<MillingRecipe, RecipeType<MillingRecipe>, RecipeWrapper> {
    @Override
    public @NotNull ResourceLocation getType() {
        return AllRecipeTypes.MILLING.getId();
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
    @Override
    public Component getConfigName() {
        return Component.translatable("config.maid_storage_manager.crafting.generating.create.milling");
    }
}