package studio.fantasyit.maid_storage_manager.craft.generator.algo.node;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class IngredientNode extends Node {
    public final Ingredient ingredient;
    public List<ItemStack> possibleItems;
    public List<ItemNode> possibleItemNodes;
    public boolean anyAvailable;
    public @Nullable UUID cachedUUID;

    public IngredientNode(int id, Ingredient ingredient, List<ItemNode> possibleItemNodes) {
        super(id);
        this.ingredient = ingredient;
        this.possibleItemNodes = new ArrayList<>(possibleItemNodes);
        this.possibleItems = possibleItemNodes.stream().map(i -> i.itemStack).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        this.anyAvailable = false;
    }

    public boolean test(ItemStack stack) {
        return ingredient.test(stack);
    }

    public void addPossibleItem(ItemNode itemNode) {
        if (!possibleItemNodes.contains(itemNode)) {
            possibleItemNodes.add(itemNode);
            possibleItems.add(itemNode.itemStack);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("IngredientNode#").append(id).append("[");
        for (int i = 0; i < possibleItems.size(); i++) {
            sb.append(possibleItems.get(i));
            if (i >= 3) {
                sb.append(",...");
                break;
            } else {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}