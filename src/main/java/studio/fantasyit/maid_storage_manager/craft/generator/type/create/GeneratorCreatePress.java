package studio.fantasyit.maid_storage_manager.craft.generator.type.create;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.kinetics.press.PressingRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public class GeneratorCreatePress extends GeneratorCreate<PressingRecipe, RecipeType<PressingRecipe>, RecipeWrapper> {
    @Override
    public ResourceLocation getType() {
        return AllRecipeTypes.PRESSING.getId();
    }

    @Override
    public boolean isBlockValid(Level level, BlockPos pos) {
        if (level.getBlockState(pos).is(AllBlocks.DEPOT.get()) && level.getBlockState(pos.above().above()).is(AllBlocks.MECHANICAL_PRESS.get())) {
            return true;
        }
        return false;
    }

    @Override
    RecipeType<PressingRecipe> getRecipeType() {
        return AllRecipeTypes.PRESSING.getType();
    }
    @Override
    public Component getConfigName() {
        return Component.translatable("config.maid_storage_manager.crafting.generating.create.pressing");
    }
}