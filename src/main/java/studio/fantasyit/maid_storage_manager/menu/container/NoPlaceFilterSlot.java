package studio.fantasyit.maid_storage_manager.menu.container;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

public class NoPlaceFilterSlot extends FilterSlot {
    public int index;

    public NoPlaceFilterSlot(int x, int y, ItemStack itemStack, int index) {
        super(new SimpleContainer(1), 0, x, y);
        this.container.setItem(0, itemStack);
        this.index = index;
        this.setActive(false);
    }
}