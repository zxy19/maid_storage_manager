package studio.fantasyit.maid_storage_manager.integration.request;

import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;

public class JEIClient {
    public static IDrawable icon = new IDrawable() {
        @Override
        public int getWidth() {
            return 9;
        }

        @Override
        public int getHeight() {
            return 9;
        }

        @Override
        public void draw(GuiGraphics guiGraphics, int xOffset, int yOffset) {
            IngredientRequestClient.drawIcon(guiGraphics, xOffset, yOffset);
        }
    };

    public static void processRequestNearByClient(IRecipeLayoutDrawable<?> recipeLayout) {
        IRecipeSlotsView recipeSlotsView = recipeLayout.getRecipeSlotsView();
        List<IRecipeSlotView> slotViews = recipeSlotsView.getSlotViews(RecipeIngredientRole.INPUT);
        IngredientRequestClient.processRequestNearByClient(
                slotViews.stream().map(sv -> sv.getItemStacks().filter(i -> !i.isEmpty()).toList()).toList()
        );
    }
}
