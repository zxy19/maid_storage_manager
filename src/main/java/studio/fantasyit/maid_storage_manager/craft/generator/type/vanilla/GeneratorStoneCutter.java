package studio.fantasyit.maid_storage_manager.craft.generator.type.vanilla;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.level.Level;
import studio.fantasyit.maid_storage_manager.craft.WorkBlockTags;
import studio.fantasyit.maid_storage_manager.craft.generator.type.base.SimpleGenerator;
import studio.fantasyit.maid_storage_manager.craft.type.StoneCuttingType;

import java.util.List;

public class GeneratorStoneCutter extends SimpleGenerator<StonecutterRecipe, SingleRecipeInput> {
    @Override
    protected RecipeType<StonecutterRecipe> getRecipeType() {
        return RecipeType.STONECUTTING;
    }
    @Override
    protected ResourceLocation getCraftType() {
        return StoneCuttingType.TYPE;
    }

    @Override
    protected SingleRecipeInput getWrappedContainer(StonecutterRecipe recipe, List<ItemStack> inputs) {
        return new SingleRecipeInput(inputs.get(0));
    }
    @Override
    public boolean isBlockValid(Level level, BlockPos pos) {
        return level.getBlockState(pos).is(WorkBlockTags.STONE_CUTTER);
    }
    @Override
    public Component getConfigName() {
        return Component.translatable("config.maid_storage_manager.crafting.generating.maid_storage_manager.stone_cutter");
    }
}
