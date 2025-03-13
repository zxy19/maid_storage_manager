package studio.fantasyit.maid_storage_manager.craft;

import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.MaidStorageManager;
import studio.fantasyit.maid_storage_manager.storage.Storage;
import studio.fantasyit.maid_storage_manager.util.RecipeUtil;

import java.util.List;
import java.util.Optional;

public class CraftingTableRecipeStore {
    public static Storage V_RECIPE = new Storage(new ResourceLocation(MaidStorageManager.MODID, "v_recipe"), BlockPos.ZERO, Optional.empty());
    public static CraftingTableRecipeStore INSTANCE;

    public static CraftingTableRecipeStore getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CraftingTableRecipeStore();
        }
        return INSTANCE;
    }

    public List<CraftGuideData> craftGuideDataList = null;

    private CraftingTableRecipeStore() {
    }

    public CraftingTableRecipeStore init(Level level) {
        if (this.craftGuideDataList == null) {
            List<CraftingRecipe> allRecipes = RecipeUtil.getAllRecipes(level);
            craftGuideDataList = allRecipes.stream()
                    .map((CraftingRecipe craftingRecipe) -> createCraftGuideData(craftingRecipe, level.registryAccess()))
                    .toList();
        }
        return this;
    }

    public @Nullable List<CraftGuideData> get() {
        return craftGuideDataList;
    }

    public void invalidate() {
        this.craftGuideDataList = null;
    }

    private CraftGuideData createCraftGuideData(CraftingRecipe craftingRecipe, RegistryAccess accessor) {
        List<ItemStack> ingredients = null;
        ingredients = craftingRecipe
                .getIngredients()
                .stream()
                .map(Ingredient::getItems)
                .map(t -> t.length == 0 ? ItemStack.EMPTY : t[0])
                .toList();

        CraftGuideStepData input1 = new CraftGuideStepData(V_RECIPE, ingredients);
        return new CraftGuideData(
                input1,
                new CraftGuideStepData(V_RECIPE, List.of()),
                new CraftGuideStepData(V_RECIPE, List.of(craftingRecipe.getResultItem(accessor)))
        );
    }
}
