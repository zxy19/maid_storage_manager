package studio.fantasyit.maid_storage_manager.craft.generator.type.vanilla;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.level.Level;
import studio.fantasyit.maid_storage_manager.craft.WorkBlockTags;
import studio.fantasyit.maid_storage_manager.craft.generator.type.base.SimpleGenerator;
import studio.fantasyit.maid_storage_manager.craft.type.StoneCuttingType;
import studio.fantasyit.maid_storage_manager.util.RecipeUtil;

import java.util.List;
import java.util.Optional;

public class GeneratorStoneCutter extends SimpleGenerator<StonecutterRecipe, Container> {
    @Override
    protected RecipeType<StonecutterRecipe> getRecipeType() {
        return RecipeType.STONECUTTING;
    }
    @Override
    protected ResourceLocation getCraftType() {
        return StoneCuttingType.TYPE;
    }
    @Override
    protected Optional<StonecutterRecipe> validateAndGetRealRecipe(Level level, StonecutterRecipe recipe, List<ItemStack> inputs, Container container) {
        List<StonecutterRecipe> stonecuttingRecipe = RecipeUtil.getStonecuttingRecipe(level, inputs.get(0));
        return stonecuttingRecipe.stream().filter(r -> r.getId().equals(recipe.getId())).findFirst();
    }
    @Override
    protected Container getWrappedContainer(Level level, StonecutterRecipe recipe, List<ItemStack> inputs) {
        return new SimpleContainer(inputs.get(0));
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
