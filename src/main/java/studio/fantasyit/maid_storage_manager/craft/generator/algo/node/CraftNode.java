package studio.fantasyit.maid_storage_manager.craft.generator.algo.node;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.craft.data.CraftGuideData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;


public class CraftNode extends Node {
    public final Identifier recipeId;
    public final List<Function<List<ItemStack>, @Nullable CraftGuideData>> craftGuideSupplier;
    public HashSet<List<Integer>> used;
    public final List<IngredientNode> independentIngredients;
    public final List<IngredientNode> ingredientNodes;
    public final List<Integer> ingredientCounts;
    public final Identifier type;
    public final boolean isOneTime;

    public CraftNode(Identifier resourceLocation, int id,
                     Function<List<ItemStack>, @Nullable CraftGuideData> craftGuideSupplier,
                     List<IngredientNode> ingredients,
                     List<Integer> ingredientCounts, Identifier type, boolean isOneTime) {
        super(id);
        this.recipeId = resourceLocation;
        this.craftGuideSupplier = new ArrayList<>(List.of(craftGuideSupplier));
        this.used = new HashSet<>();
        this.ingredientNodes = ingredients;
        this.ingredientCounts = ingredientCounts;
        this.isOneTime = isOneTime;
        this.type = type;
        HashSet<Integer> independentIngredientsId = new HashSet<>();
        independentIngredients = new ArrayList<>();
        for (IngredientNode ingredientNode : ingredients) {
            if (!independentIngredientsId.contains(ingredientNode.id)) {
                independentIngredientsId.add(ingredientNode.id);
                independentIngredients.add(ingredientNode);
            }
        }
    }

    public void addCraftGuideSupplier(Function<List<ItemStack>, @Nullable CraftGuideData> craftGuideSupplier) {
        this.craftGuideSupplier.add(craftGuideSupplier);
    }

    @Override
    public String toString() {
        return "CraftNode#" + id + "[" + recipeId + "]";
    }
}