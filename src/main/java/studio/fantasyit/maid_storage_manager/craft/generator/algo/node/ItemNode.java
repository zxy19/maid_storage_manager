package studio.fantasyit.maid_storage_manager.craft.generator.algo.node;


import net.minecraft.world.item.ItemStack;

public class ItemNode extends Node {
    public final ItemStack itemStack;
    public boolean isAvailable;

    public ItemNode(int id, boolean available, ItemStack itemStack) {
        super(id);
        this.itemStack = itemStack;
        this.isAvailable = available;
    }
}
