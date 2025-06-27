package studio.fantasyit.maid_storage_manager.craft.generator.type.misc;

import com.github.tartaricacid.touhoulittlemaid.crafting.AltarRecipe;
import com.github.tartaricacid.touhoulittlemaid.init.InitRecipes;
import com.github.tartaricacid.touhoulittlemaid.inventory.AltarRecipeInventory;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityAltar;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import studio.fantasyit.maid_storage_manager.craft.generator.type.base.SimpleGenerator;
import studio.fantasyit.maid_storage_manager.craft.type.AltarType;
import studio.fantasyit.maid_storage_manager.util.RecipeUtil;

import java.util.List;

public class GeneratorAltar extends SimpleGenerator<AltarRecipe, AltarRecipeInventory> {
    @Override
    protected RecipeType<AltarRecipe> getRecipeType() {
        return InitRecipes.ALTAR_CRAFTING;
    }

    @Override
    protected ResourceLocation getCraftType() {
        return AltarType.TYPE;
    }

    @Override
    protected AltarRecipeInventory getWrappedContainer(AltarRecipe recipe, List<ItemStack> inputs) {
        return RecipeUtil.wrapAltarRecipeInventory(inputs);
    }


    @Override
    public boolean isBlockValid(Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof TileEntityAltar;
    }

    @Override
    protected boolean isValid(AltarRecipe recipe) {
        return recipe.isItemCraft();
    }

    @Override
    public Component getConfigName() {
        return Component.translatable("config.maid_storage_manager.crafting.generating.maid_storage_manager.altar");
    }
}
