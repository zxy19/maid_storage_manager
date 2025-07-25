package studio.fantasyit.maid_storage_manager.util;

import com.github.tartaricacid.touhoulittlemaid.crafting.AltarRecipe;
import com.github.tartaricacid.touhoulittlemaid.init.InitRecipes;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.brewing.IBrewingRecipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RecipeUtil {
    public static class ReadonlyCraftingContainer implements CraftingContainer {
        private final List<ItemStack> items;
        private final int w;
        private final int h;

        public ReadonlyCraftingContainer(List<ItemStack> items, int w, int h) {
            this.items = items;
            this.w = w;
            this.h = h;
        }

        @Override
        public int getWidth() {
            return w;
        }

        @Override
        public int getHeight() {
            return h;
        }

        @Override
        public List<ItemStack> getItems() {
            return items;
        }

        @Override
        public int getContainerSize() {
            return w * h;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public ItemStack getItem(int p_18941_) {
            return items.get(p_18941_);
        }

        @Override
        public ItemStack removeItem(int p_18942_, int p_18943_) {
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack removeItemNoUpdate(int p_18951_) {
            return ItemStack.EMPTY;
        }

        @Override
        public void setItem(int p_18944_, ItemStack p_18945_) {
        }

        @Override
        public void setChanged() {

        }

        @Override
        public boolean stillValid(Player p_18946_) {
            return true;
        }

        @Override
        public void clearContent() {
        }

        @Override
        public void fillStackedContents(StackedContents p_40281_) {
        }
    }


    public static CraftingInput wrapCraftingContainer(List<ItemStack> items, CraftingRecipe recipe) {
        if (recipe instanceof ShapedRecipe shapedRecipe)
            return wrapCraftingContainer(items, shapedRecipe.getWidth(), shapedRecipe.getHeight()).asCraftInput();
        return wrapCraftingContainer(items, 3, 3).asCraftInput();
    }


    public static CraftingContainer wrapCraftingContainer(List<ItemStack> items, int w, int h) {
        if (items.size() != w * h) {
            items = new ArrayList<>(items);
            for (int i = items.size(); i < w * h; i++) {
                items.add(ItemStack.EMPTY);
            }
        }
        return new ReadonlyCraftingContainer(items, w, h);
    }

    public static CraftingContainer wrapCraftingContainer(Container items, int w, int h) {
        List<ItemStack> itemList = new ArrayList<>();
        for (int i = 0; i < items.getContainerSize(); i++) {
            itemList.add(items.getItem(i));
        }
        return new ReadonlyCraftingContainer(itemList, w, h);
    }

    public static Optional<RecipeHolder<CraftingRecipe>> getCraftingRecipe(Level level, CraftingInput container) {
        RecipeManager recipeManager = level.getRecipeManager();
        return recipeManager.getRecipeFor(
                RecipeType.CRAFTING,
                container,
                level
        );
    }

    public static Optional<RecipeHolder<SmithingRecipe>> getSmithingRecipe(Level level, List<ItemStack> items) {
        if (items.size() < 3) {
            items = new ArrayList<>(items);
            while (items.size() < 3)
                items.add(ItemStack.EMPTY);
        }
        SmithingRecipeInput input = new SmithingRecipeInput(items.get(0), items.get(1), items.get(2));
        RecipeManager recipeManager = level.getRecipeManager();
        return recipeManager.getRecipeFor(
                RecipeType.SMITHING,
                input,
                level
        );
    }

    public static CraftingInput wrapAltarRecipeInventory(List<ItemStack> items) {
        CraftingContainer inv = wrapCraftingContainer(items, 6, 1);
        for (int i = 0; i < Math.min(6, items.size()); i++) {
            inv.setItem(i, items.get(i));
        }
        return inv.asCraftInput();
    }

    public static Optional<RecipeHolder<AltarRecipe>> getAltarRecipe(Level level, CraftingInput container) {
        RecipeManager recipeManager = level.getRecipeManager();
        return recipeManager.getRecipeFor(
                InitRecipes.ALTAR_CRAFTING.get(),
                container,
                level
        );
    }

    public static Optional<RecipeHolder<SmeltingRecipe>> getSmeltingRecipe(Level level, ItemStack itemStack) {
        RecipeManager recipeManager = level.getRecipeManager();
        return recipeManager.getRecipeFor(
                RecipeType.SMELTING,
                new SingleRecipeInput(itemStack),
                level
        );
    }

    public static Optional<IBrewingRecipe> getBrewingRecipe(Level level, ItemStack item1, ItemStack item2) {
        return level.potionBrewing()
                .getRecipes()
                .stream()
                .filter(r -> r.isInput(item1) && r.isIngredient(item2))
                .findFirst();
    }

    public static List<RecipeHolder<StonecutterRecipe>> getStonecuttingRecipe(Level level, ItemStack itemStack) {
        RecipeManager recipeManager = level.getRecipeManager();
        return recipeManager.getRecipesFor(
                RecipeType.STONECUTTING,
                new SingleRecipeInput(itemStack),
                level
        );
    }
}
