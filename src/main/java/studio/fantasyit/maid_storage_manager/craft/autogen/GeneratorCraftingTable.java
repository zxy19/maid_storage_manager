package studio.fantasyit.maid_storage_manager.craft.autogen;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.data.InventoryItem;
import studio.fantasyit.maid_storage_manager.storage.Target;

import java.util.List;

public class GeneratorCraftingTable implements IAutoCraftGuideGenerator {
    @Override
    public ResourceLocation getType() {
        return new ResourceLocation(MaidStorageManager.MODID, "crafting_table");
    }

    @Override
    public boolean isBlockValid(Level level, BlockPos pos) {
        return level.getBlockState(pos).is(Blocks.CRAFTING_TABLE);
    }

    protected ItemStack
    @Override
    public List<CraftGuideData> generate(List<InventoryItem> inventory, Level level, BlockPos pos) {
        RecipeManager recipeManager = level.getRecipeManager();
        Target target = Target.virtual(pos, null);
        return recipeManager.getAllRecipesFor(RecipeType.CRAFTING)
                .stream()
                .map(recipe -> {
                            step = new CraftGuideStepData(
                                    target,
                            recipe.getIngredients()
                                    .stream()
                                    .map(ingredient -> ingredient.getItems())
                            )
                })
                .toList();
    }
}
