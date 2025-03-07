package studio.fantasyit.maid_storage_manager.util;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

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

    public static CraftingContainer wrapContainer(List<ItemStack> items, int w, int h) {
        return new ReadonlyCraftingContainer(items, w, h);
    }
    public static CraftingContainer wrapContainer(Container items, int w, int h) {
        List<ItemStack> itemList = new ArrayList<>();
        for (int i = 0; i < items.getContainerSize(); i++) {
            itemList.add(items.getItem(i));
        }
        return new ReadonlyCraftingContainer(itemList, w, h);
    }
    public static Optional<CraftingRecipe> getRecipe(Level level, CraftingContainer container) {
        RecipeManager recipeManager = level.getRecipeManager();
        return recipeManager.getRecipeFor(
                RecipeType.CRAFTING,
                container,
                level
        );
    }
}
