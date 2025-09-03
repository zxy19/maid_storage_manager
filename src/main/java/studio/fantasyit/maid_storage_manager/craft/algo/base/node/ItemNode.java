package studio.fantasyit.maid_storage_manager.craft.algo.base.node;

import net.minecraft.world.item.ItemStack;

public class ItemNode extends ItemNodeBasic {
    public final ItemStack itemStack;

    public ItemNode(int id, boolean related, ItemStack itemStack) {
        super(id, related);
        this.itemStack = itemStack;
    }

    @Override
    public String toString() {
        return String.format("ItemNode#%s[%s]", id, itemStack.getItem());
    }
}
