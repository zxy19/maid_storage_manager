package studio.fantasyit.maid_storage_manager.craft.generator.type;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.crafting.IShapedRecipe;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideStepData;
import studio.fantasyit.maid_storage_manager.craft.generator.IAutoCraftGuideGenerator;
import studio.fantasyit.maid_storage_manager.craft.generator.algo.GeneratorGraph;
import studio.fantasyit.maid_storage_manager.craft.generator.cache.RecipeIngredientCache;
import studio.fantasyit.maid_storage_manager.craft.type.CraftingType;
import studio.fantasyit.maid_storage_manager.data.InventoryItem;
import studio.fantasyit.maid_storage_manager.storage.Target;
import studio.fantasyit.maid_storage_manager.util.RecipeUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GeneratorCraftingTable implements IAutoCraftGuideGenerator {
    @Override
    public ResourceLocation getType() {
        return new ResourceLocation(MaidStorageManager.MODID, "crafting_table");
    }

    @Override
    public boolean isBlockValid(Level level, BlockPos pos) {
        return level.getBlockState(pos).is(Blocks.CRAFTING_TABLE);
    }

    @Override
    public void generate(List<InventoryItem> inventory, Level level, BlockPos pos, GeneratorGraph graph) {
        level.getRecipeManager()
                .getAllRecipesFor(RecipeType.CRAFTING)
                .forEach((CraftingRecipe recipe) -> {
                    graph.addRecipe(recipe, (items) -> {
                        Optional<CraftingRecipe> realRecipe = RecipeUtil.getCraftingRecipe(level, RecipeUtil.wrapCraftingContainer(items, recipe));
                        if (realRecipe.isEmpty() || !realRecipe.get().getId().equals(recipe.getId())) {
                            return null;
                        }
                        CraftingRecipe craftingRecipe = realRecipe.get();
                        ArrayList<ItemStack> result = new ArrayList<>(List.of(craftingRecipe.getResultItem(level.registryAccess())));
                        result.addAll(craftingRecipe
                                .getRemainingItems(RecipeUtil.wrapCraftingContainer(items, recipe))
                                .stream()
                                .filter(i -> !i.isEmpty())
                                .toList()
                        );

                        CraftGuideStepData step = new CraftGuideStepData(
                                new Target(CraftingType.TYPE, pos),
                                wrapInputsForRecipe(items, recipe),
                                result,
                                CraftingType.TYPE,
                                false,
                                new CompoundTag()
                        );
                        return new CraftGuideData(
                                List.of(step),
                                CraftingType.TYPE
                        );
                    });
                });

    }

    @Override
    public void onCache(RecipeManager manager) {
        manager.getAllRecipesFor(RecipeType.CRAFTING).forEach(RecipeIngredientCache::addRecipeCache);
    }

    protected static List<ItemStack> wrapInputsForRecipe(List<ItemStack> items, CraftingRecipe recipe) {
        List<ItemStack> inputs = new ArrayList<>();
        for (int i = 0; i < 9; i++)
            inputs.add(ItemStack.EMPTY);
        if (recipe instanceof IShapedRecipe<?> shapedRecipe) {
            int c = 0;
            for (int y = 0; y < 3; y++) {
                for (int x = 0; x < 3; x++) {
                    if (x < shapedRecipe.getRecipeWidth() && y < shapedRecipe.getRecipeHeight() && c < items.size())
                        inputs.set(x + y * 3, items.get(c++));
                }
            }
        } else {
            for (int i = 0; i < items.size(); i++)
                inputs.set(i, items.get(i));
        }
        return inputs;
    }
}
