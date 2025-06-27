package studio.fantasyit.maid_storage_manager.craft.generator.algo.node;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_storage_manager.util.ItemStackUtil;

import java.util.List;
import java.util.UUID;

public class IngredientNode extends Node {
    public List<ItemStack> possibleItems;
    public List<ItemNode> possibleItemNodes;
    public boolean anyAvailable;
    public @Nullable UUID cachedUUID;

    public IngredientNode(int id, List<ItemNode> possibleItemNodes) {
        super(id);
        this.possibleItemNodes = possibleItemNodes;
        this.possibleItems = possibleItemNodes.stream().map(i -> i.itemStack).toList();
        this.anyAvailable = false;
    }

    public boolean isEqualTo(Ingredient ingredient) {
        ItemStack[] items = ingredient.getItems();
        if (items.length != possibleItems.size())
            return false;
        for (int i = 0; i < items.length; i++) {
            if (!ItemStackUtil.isSameInCrafting(items[i], possibleItems.get(i))) {
                return false;
            }
        }
        return true;
    }
}